package com.example

import com.example.data.model.AccessoryCategory
import com.example.data.model.Invoice
import com.example.data.model.InvoiceClient
import com.example.data.model.POS_FINITION_OPTIONS
import com.example.data.model.STANDARD_USER_COLISAGES
import com.example.data.model.TileProduct
import com.example.data.model.buildCatalogIndex
import com.example.data.model.formatFinitionLabel
import com.example.data.model.isSoldByBag
import com.example.data.model.matchesAccessoryFilter
import com.example.data.model.matchesFinitionFilter
import com.example.data.model.resolveColisagePresetsForDimension
import com.example.data.model.splitFinitions
import com.example.data.model.toggleFinitionSelection
import com.example.ui.components.PACK_COURDON_PRESETS
import com.example.ui.components.buildFaiencePackCartItems
import com.example.ui.components.previewCartMetersConversion
import com.example.ui.components.resolveAccessoryDefaultPrice
import com.example.data.model.CartItem
import com.example.data.model.CourdonLengthMode
import com.example.data.model.FaienceInputMode
import com.example.data.model.PlintheInputMode
import com.example.data.model.PlintheRoomShape
import com.example.data.model.PosSeeds
import com.example.data.model.calculateFaienceMotifPlan
import com.example.data.model.calculatePlinthePlan
import com.example.data.model.canonicalJetonKey
import com.example.data.model.computeTopFavoriteProducts
import com.example.data.model.deduplicateCatalogProducts
import com.example.ui.screens.SurfaceZoneEntry
import com.example.util.DailyClosingReportEngine
import com.example.util.QrCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CeraluxPolishUpgradeUnitTest {

    private fun sampleTile(finition: String) = TileProduct(
        id = "p_fin",
        name = "60/60 BATI LUNA",
        usine = "BATI",
        dimension = "60/60",
        m2Carton = 1.44,
        pcs = 4,
        prix = 1200.0,
        prixAchat = 900.0,
        stock = 20,
        finition = finition
    )

    @Test
    fun `combined format 30_60 and Motif filter matches dimension field AND designation text`() {
        val catalog = listOf(
            TileProduct(
                id = "t1", name = "30/60 FCM LUNA BEIGE", usine = "FCM", dimension = "30/60",
                m2Carton = 1.08, pcs = 6, prix = 700.0, prixAchat = 500.0, stock = 50, categorie = "faïence"
            ),
            TileProduct(
                id = "m1", name = "MOTIF 30/60 DOUCHE LUNA", usine = "", dimension = "30/60",
                m2Carton = 1.0, pcs = 1, prix = 400.0, prixAchat = 250.0, stock = 20, pricingUnit = "piece", prixPiece = 400.0, categorie = "motif"
            ),
            TileProduct(
                id = "m_desig_only", name = "MOTIF AURA 30/60 BEIGE", usine = "NOVA", dimension = "Pièce",
                m2Carton = 1.0, pcs = 1, prix = 450.0, prixAchat = 300.0, stock = 15, pricingUnit = "piece", prixPiece = 450.0, categorie = "motif"
            ),
            TileProduct(
                id = "m_reversed", name = "MOTIF 60x30 MARBRE", usine = "FCM", dimension = "Pièce",
                m2Carton = 1.0, pcs = 1, prix = 420.0, prixAchat = 280.0, stock = 12, pricingUnit = "piece", prixPiece = 420.0, categorie = "motif"
            ),
            TileProduct(
                id = "m2", name = "MOTIF 45/90 WARDA", usine = "", dimension = "45/90",
                m2Carton = 1.0, pcs = 1, prix = 1200.0, prixAchat = 800.0, stock = 10, pricingUnit = "piece", prixPiece = 1200.0, categorie = "motif"
            )
        )
        val index = buildCatalogIndex(catalog)

        val motif3060 = index.filter(
            queryTokens = emptyList(),
            selectedDimension = "30/60",
            selectedUsine = "Toutes",
            selectedFinition = "Toutes",
            selectedColor = "Toutes",
            selectedAccessory = "Motif"
        )
        assertEquals(setOf("m1", "m_desig_only", "m_reversed"), motif3060.map { it.id }.toSet())

        val allMotifs = index.filter(
            queryTokens = emptyList(),
            selectedDimension = "Toutes",
            selectedUsine = "Toutes",
            selectedFinition = "Toutes",
            selectedColor = "Toutes",
            selectedAccessory = "Motif"
        )
        assertEquals(4, allMotifs.size)
        assertTrue(matchesAccessoryFilter(catalog[1], "Motif"))
        assertFalse(matchesAccessoryFilter(catalog[0], "Motif"))
    }

    @Test
    fun `finition filter excludes satine and rustic, syncs custom finitions, and supports multi-select AND`() {
        assertFalse(POS_FINITION_OPTIONS.contains("satiné"))
        assertFalse(POS_FINITION_OPTIONS.contains("rustic"))

        val f1 = toggleFinitionSelection("", "brillant")
        assertEquals("brillant", f1)
        val f2 = toggleFinitionSelection(f1, "rectifié")
        assertEquals("brillant + rectifié", f2)
        assertEquals(listOf("brillant", "rectifié"), splitFinitions(f2))
        assertEquals("Brillant • Rectifié", formatFinitionLabel(f2))

        val brillantRect = sampleTile("brillant + rectifié").copy(id = "p_br")
        val brillantOnly = sampleTile("brillant").copy(id = "p_b")
        val rectOnly = sampleTile("rectifié").copy(id = "p_r")
        val customStruct = sampleTile("structuré + mat").copy(id = "p_custom")

        // Multi-select filter ("brillant + rectifié") requires BOTH finitions
        assertTrue(matchesFinitionFilter(brillantRect, "brillant + rectifié"))
        assertFalse(matchesFinitionFilter(brillantOnly, "brillant + rectifié"))
        assertFalse(matchesFinitionFilter(rectOnly, "brillant + rectifié"))

        // Toggling off removes cleanly
        assertEquals("rectifié", toggleFinitionSelection(f2, "brillant"))

        // Custom finition added on any product automatically appears in CatalogIndex.finitions and filters properly
        val idx = buildCatalogIndex(listOf(brillantRect, brillantOnly, rectOnly, customStruct))
        assertTrue(idx.finitions.any { it.equals("structuré", ignoreCase = true) })
        val filteredMulti = idx.filter(
            queryTokens = emptyList(),
            selectedDimension = "Toutes",
            selectedUsine = "Toutes",
            selectedFinition = "brillant + rectifié",
            selectedColor = "Toutes"
        )
        assertEquals(listOf("p_br"), filteredMulti.map { it.id })

        val filteredCustom = idx.filter(
            queryTokens = emptyList(),
            selectedDimension = "Toutes",
            selectedUsine = "Toutes",
            selectedFinition = "structuré",
            selectedColor = "Toutes"
        )
        assertEquals(listOf("p_custom"), filteredCustom.map { it.id })
    }

    @Test
    fun `standard user colisages always include all 10 requested values`() {
        val expected = listOf(0.75, 0.77, 0.90, 1.00, 1.01, 1.21, 1.42, 1.44, 1.45, 1.56)
        assertEquals(expected, STANDARD_USER_COLISAGES)

        val presets = resolveColisagePresetsForDimension("60/60", emptyList())
        expected.forEach { v ->
            assertTrue("Expected $v in colisage presets $presets", presets.any { kotlin.math.abs(it - v) < 0.005 })
        }
    }

    @Test
    fun `courdon exact prices and bag units for ciment colle and pate a joint`() {
        assertEquals("150", resolveAccessoryDefaultPrice(AccessoryCategory.COURDON, "2/60", subtype = "Simple", colorLabel = "Doré"))
        assertEquals("150", resolveAccessoryDefaultPrice(AccessoryCategory.COURDON, "2/60", subtype = "Simple", colorLabel = "Argent"))
        assertEquals("200", resolveAccessoryDefaultPrice(AccessoryCategory.COURDON, "2/60", subtype = "Modèle", colorLabel = "Doré"))
        assertEquals("250", resolveAccessoryDefaultPrice(AccessoryCategory.COURDON, "3/60", subtype = "Modèle", colorLabel = "Doré"))
        assertEquals("280", resolveAccessoryDefaultPrice(AccessoryCategory.COURDON, "4/60", subtype = "Modèle", colorLabel = "Argent"))

        assertEquals("800", resolveAccessoryDefaultPrice(AccessoryCategory.PLINTHE, "8/45"))
        assertEquals("850", resolveAccessoryDefaultPrice(AccessoryCategory.PLINTHE, "9/45"))
        assertEquals("500", resolveAccessoryDefaultPrice(AccessoryCategory.CIMENT_COLLE, "25 kg"))
        assertEquals("700", resolveAccessoryDefaultPrice(AccessoryCategory.PATE_A_JOINT, "5 kg"))

        assertTrue(isSoldByBag("CIMENT COLLE 25 KG"))
        assertTrue(isSoldByBag("Pâte à Joint 5 kg Blanc"))
        assertTrue(isSoldByBag("PATE A JOINT GRIS CLAIR"))
        assertFalse(isSoldByBag("60/60 BATI LUNA"))
        assertFalse(isSoldByBag("Baguette F Argent"))
    }

    @Test
    fun `invoice model carries deductionRetour field for net total calculation`() {
        val inv = Invoice(
            id = "inv_ret_1",
            reference = "FAC-RET-001",
            date = "2026-11-22 10:00",
            vendeur = "Kader",
            magasin = "CASACERAM",
            client = InvoiceClient(nom = "Ahmed"),
            articles = emptyList(),
            lignesLibres = emptyList(),
            totalBrut = 20000.0,
            remise = 1000.0,
            deductionRetour = 3500.0,
            totalNet = 15500.0,
            montantPaye = 15500.0,
            resteAPayer = 0.0,
            modePaiement = "Espèces",
            statut = "Payé"
        )
        assertEquals(3500.0, inv.deductionRetour, 0.01)
        assertEquals(15500.0, inv.totalNet, 0.01)
    }

    @Test
    fun `casaceram brand and verified whatsapp and depot gps qr urls are configured`() {
        assertEquals("CASACERAM", PosSeeds.DEFAULT_STORE_INFO.nom)
        assertEquals("https://goo.gl/maps/nv4dvTfngu8WgYKy8", PosSeeds.DEFAULT_DEPOT_LOCATION_URL)
        assertEquals("https://goo.gl/maps/nv4dvTfngu8WgYKy8", PosSeeds.DEFAULT_STORE_INFO.depotLocationUrl)
        assertEquals("0676754124", PosSeeds.DEFAULT_STORE_INFO.whatsappQrPhone)
        assertEquals("https://wa.me/213676754124", QrCodeGenerator.buildWhatsAppQrUrl("0676754124"))
        assertEquals("https://goo.gl/maps/nv4dvTfngu8WgYKy8", QrCodeGenerator.buildDepotLocationQrUrl(""))
        assertEquals("https://goo.gl/maps/nv4dvTfngu8WgYKy8", QrCodeGenerator.buildDepotLocationQrUrl(PosSeeds.DEFAULT_DEPOT_LOCATION_URL))
    }

    @Test
    fun `deduplicateCatalogProducts eliminates duplicate seed jetons and factory aliases while preserving CC and distinct finitions`() {
        val sampleWithDuplicates = listOf(
            // Overlapping pairs (unpriced seed jeton vs priced user jeton)
            TileProduct(id = "p_jeton_60_60_bati", name = "60/60 BATI", usine = "BATI", dimension = "60×60", m2Carton = 1.44, pcs = 4, prix = 0.0, prixAchat = 0.0, stock = 0),
            TileProduct(id = "p_user_60_60_bati", name = "BATI", usine = "BATI", dimension = "60/60", m2Carton = 1.44, pcs = 4, prix = 1200.0, prixAchat = 850.0, stock = 100),
            // Alias pair: MCM vs NCM MCM
            TileProduct(id = "p_jeton_120_60_mcm", name = "120/60 MCM", usine = "MCM", dimension = "120×60", m2Carton = 1.44, pcs = 2, prix = 0.0, prixAchat = 0.0, stock = 0),
            TileProduct(id = "p_user_120_60_ncm", name = "NCM MCM", usine = "NCM", dimension = "120/60", m2Carton = 1.44, pcs = 2, prix = 1800.0, prixAchat = 1300.0, stock = 60, pricingUnit = "piece", prixPiece = 1800.0),
            // Alias pair: FCM MALEH vs FCM
            TileProduct(id = "p_jeton_30_60_fcm_maleh", name = "30/60 FCM MALEH", usine = "FCM MALEH", dimension = "30×60", m2Carton = 1.08, pcs = 6, prix = 0.0, prixAchat = 0.0, stock = 0),
            TileProduct(id = "p_user_30_60_fcm", name = "FCM", usine = "FCM", dimension = "30/60", m2Carton = 1.08, pcs = 6, prix = 750.0, prixAchat = 550.0, stock = 100),
            // CC choice must NEVER collide with 1er Choix
            TileProduct(id = "p_user_60_60_royal", name = "ROYAL", usine = "ROYAL", dimension = "60/60", m2Carton = 1.44, pcs = 4, prix = 1200.0, prixAchat = 850.0, stock = 90, choix = "1er Choix"),
            TileProduct(id = "p_user_60_60_royal_cc", name = "ROYAL CC", usine = "ROYAL", dimension = "60/60", m2Carton = 1.44, pcs = 4, prix = 900.0, prixAchat = 650.0, stock = 80, choix = "CC"),
            TileProduct(id = "p_jeton_60_60_royal", name = "60/60 ROYAL", usine = "ROYAL", dimension = "60×60", m2Carton = 1.44, pcs = 4, prix = 0.0, prixAchat = 0.0, stock = 0, choix = "1er Choix"),
            // Same named model with two different finitions must both survive
            TileProduct(id = "p_luna_mat", name = "LUNA BEIGE", usine = "BATI", dimension = "60/60", m2Carton = 1.44, pcs = 4, prix = 1250.0, prixAchat = 900.0, stock = 40, finition = "mat + rectifié"),
            TileProduct(id = "p_luna_polie", name = "LUNA BEIGE", usine = "BATI", dimension = "60/60", m2Carton = 1.44, pcs = 4, prix = 1350.0, prixAchat = 950.0, stock = 40, finition = "polie")
        )

        val deduped = deduplicateCatalogProducts(sampleWithDuplicates)
        val ids = deduped.map { it.id }.toSet()

        assertTrue("Expected priced p_user_60_60_bati", "p_user_60_60_bati" in ids)
        assertFalse("Duplicate p_jeton_60_60_bati must be removed", "p_jeton_60_60_bati" in ids)

        assertTrue("Expected priced p_user_120_60_ncm", "p_user_120_60_ncm" in ids)
        assertFalse("Duplicate alias p_jeton_120_60_mcm must be removed", "p_jeton_120_60_mcm" in ids)

        assertTrue("Expected priced p_user_30_60_fcm", "p_user_30_60_fcm" in ids)
        assertFalse("Duplicate alias p_jeton_30_60_fcm_maleh must be removed", "p_jeton_30_60_fcm_maleh" in ids)

        assertTrue("Expected 1er choix ROYAL", "p_user_60_60_royal" in ids)
        assertTrue("Expected CC ROYAL preserved", "p_user_60_60_royal_cc" in ids)
        assertFalse("Duplicate p_jeton_60_60_royal must be removed", "p_jeton_60_60_royal" in ids)

        assertTrue("Mat+Rectifié variant preserved", "p_luna_mat" in ids)
        assertTrue("Polie variant preserved", "p_luna_polie" in ids)

        // Verify zero duplicate jeton keys in DEFAULT_USER_CATALOG_MODELS itself
        val defaultJetonKeys = deduplicateCatalogProducts(PosSeeds.DEFAULT_USER_CATALOG_MODELS)
            .mapNotNull { canonicalJetonKey(it) }
        assertEquals(defaultJetonKeys.size, defaultJetonKeys.toSet().size)
    }

    @Test
    fun `anti-fraud sequence gap detector flags missing CAS invoice numbers and ignores devis`() {
        fun mkInv(id: String, ref: String, isDevis: Boolean = false, retour: Double = 0.0, notes: String = "") = Invoice(
            id = id,
            reference = ref,
            date = "2026-11-22 10:00",
            vendeur = "Kader",
            magasin = "CASACERAM",
            client = InvoiceClient(nom = "Client Test"),
            articles = emptyList(),
            lignesLibres = emptyList(),
            totalBrut = 10000.0,
            remise = 0.0,
            deductionRetour = retour,
            totalNet = 10000.0 - retour,
            montantPaye = 10000.0 - retour,
            resteAPayer = 0.0,
            modePaiement = if (isDevis) "Devis" else "Espèces",
            statut = if (isDevis) "Devis" else "Payé",
            notes = notes
        )

        val invoices = listOf(
            mkInv("1", "CAS-261122-KAD-041"),
            mkInv("2", "CAS-261122-KAD-043", retour = 2500.0, notes = "2 ctn 60/60 cassés"),
            mkInv("3", "DEV-261122-KAD-010", isDevis = true)
        )

        val gaps = DailyClosingReportEngine.detectInvoiceSequenceGaps(invoices)
        assertEquals(1, gaps.size)
        assertEquals("CAS", gaps.first().prefix)
        assertEquals(listOf(42), gaps.first().missingNumbers)

        val summary = DailyClosingReportEngine.computeSalesBreakdown(invoices, emptyList())
        assertEquals(2500.0, summary.totalReturnDeductions, 0.01)
        assertEquals(1, summary.returnDeductionAudits.size)
        assertEquals("CAS-261122-KAD-043", summary.returnDeductionAudits.first().reference)
        assertEquals("2 ctn 60/60 cassés", summary.returnDeductionAudits.first().reason)
        assertEquals(1, summary.sequenceGaps.size)
    }

    @Test
    fun `seller mode includes dedicated surface tab and favorites ranks most-sold models first`() {
        assertEquals(listOf("pos", "surface", "invoices", "cloture"), PosSeeds.SELLER_TABS)

        val zone = SurfaceZoneEntry(label = "Salon", lengthM = 5.0, widthM = 4.0)
        assertEquals(20.0, zone.exactAreaM2, 0.001)
        assertEquals(18.0, zone.perimeterMl, 0.001)

        val pA = TileProduct(id = "pA", name = "ALPHA 60/60", usine = "BATI", dimension = "60/60", m2Carton = 1.44, pcs = 4, prix = 1200.0, prixAchat = 900.0, stock = 0)
        val pB = TileProduct(id = "pB", name = "BETA 30/60", usine = "FCM", dimension = "30/60", m2Carton = 1.08, pcs = 6, prix = 800.0, prixAchat = 600.0, stock = 0)
        val pC = TileProduct(id = "pC", name = "CIMENT COLLE 25KG", usine = "SADER", dimension = "25kg", m2Carton = 1.0, pcs = 1, prix = 500.0, prixAchat = 350.0, stock = 0)

        fun saleWith(id: String, prod: TileProduct) = Invoice(
            id = id,
            reference = "CAS-261122-001",
            date = "2026-11-22 10:00",
            vendeur = "Kader",
            magasin = "CASACERAM",
            client = InvoiceClient(nom = "Client"),
            articles = listOf(
                CartItem(
                    id = "ci_$id",
                    productId = prod.id,
                    name = prod.name,
                    usine = prod.usine,
                    dimension = prod.dimension,
                    m2Carton = prod.m2Carton,
                    pcs = prod.pcs,
                    cartons = 5,
                    pieces = 20,
                    m2Total = 7.2,
                    prixUnitaire = prod.prix,
                    prixAchat = prod.prixAchat,
                    sousTotal = 5 * prod.m2Carton * prod.prix
                )
            ),
            lignesLibres = emptyList(),
            totalBrut = 5000.0,
            remise = 0.0,
            totalNet = 5000.0,
            montantPaye = 5000.0,
            resteAPayer = 0.0,
            modePaiement = "Espèces",
            statut = "Payé"
        )

        val invoices = listOf(
            saleWith("i1", pB),
            saleWith("i2", pB),
            saleWith("i3", pA)
        )
        val favs = computeTopFavoriteProducts(listOf(pA, pB, pC), invoices, limit = 3)
        assertEquals(listOf("pB", "pA", "pC"), favs.map { it.id })
    }

    @Test
    fun `FCM, FCM MALEH, and FC MALEH unify into one single FCM factory chip and filter`() {
        val catalog = listOf(
            TileProduct(id = "f1", name = "30/60 LUNA", usine = "FCM", dimension = "30/60", m2Carton = 1.08, pcs = 6, prix = 750.0, prixAchat = 550.0, stock = 10),
            TileProduct(id = "f2", name = "30/60 APOLO", usine = "FCM MALEH", dimension = "30/60", m2Carton = 1.08, pcs = 6, prix = 750.0, prixAchat = 550.0, stock = 10),
            TileProduct(id = "f3", name = "30/65 TERANO", usine = "FC MALEH", dimension = "30/65", m2Carton = 1.17, pcs = 6, prix = 800.0, prixAchat = 600.0, stock = 10),
            TileProduct(id = "b1", name = "60/60 ROYAL", usine = "BATI", dimension = "60/60", m2Carton = 1.44, pcs = 4, prix = 1200.0, prixAchat = 900.0, stock = 10)
        )
        val index = buildCatalogIndex(catalog)

        // Only ONE unified "FCM" chip alongside "BATI" — no separate "FCM MALEH" or "FC MALEH"
        assertEquals(listOf("Toutes", "BATI", "FCM"), index.usines)

        // Filtering by "FCM" returns all 3 products (FCM + FCM MALEH + FC MALEH)
        val fcmMatches = index.filter(
            queryTokens = emptyList(),
            selectedDimension = "Toutes",
            selectedUsine = "FCM",
            selectedFinition = "Toutes",
            selectedColor = "Toutes",
            selectedAccessory = "Tous"
        )
        assertEquals(setOf("f1", "f2", "f3"), fcmMatches.map { it.id }.toSet())

        // Searching "maleh" also finds all 3 FCM products
        val malehSearch = index.filter(
            queryTokens = listOf("maleh"),
            selectedDimension = "Toutes",
            selectedUsine = "Toutes",
            selectedFinition = "Toutes",
            selectedColor = "Toutes",
            selectedAccessory = "Tous"
        )
        assertEquals(setOf("f1", "f2", "f3"), malehSearch.map { it.id }.toSet())
    }

    @Test
    fun `faience motif calculator computes exact 30x60 example with deduction and 2 courdons per motif`() {
        // User's exact scenario: 20 m² total, 2.0 m height -> 10 ml, 30/60 motif -> 17 pcs, 3.06 m² deducted -> 16.94 m² faïence, 34 courdons
        val plan3060 = calculateFaienceMotifPlan(
            inputMode = FaienceInputMode.SURFACE_AND_HEIGHT,
            totalSurfaceInputM2 = 20.0,
            wallHeightM = 2.0,
            linearMeterInputMl = 0.0,
            motifHeightCm = 30.0,
            motifLengthCm = 60.0
        )
        assertEquals(10.0, plan3060.linearMetersMl, 0.001)
        assertEquals(0.18, plan3060.motifPieceAreaM2, 0.0001)
        assertEquals(17, plan3060.totalMotifPieces)
        assertEquals(3.06, plan3060.motifCoveredM2, 0.001)
        assertEquals(16.94, plan3060.netFaienceM2, 0.001)
        assertEquals(34, plan3060.totalCourdonPieces)
        assertTrue(plan3060.hasNetFaience)

        // Direct Linear Meter input mode (10 ml + 2 m height) yields identical results
        val planDirectMl = calculateFaienceMotifPlan(
            inputMode = FaienceInputMode.LINEAR_METER,
            totalSurfaceInputM2 = 0.0,
            wallHeightM = 2.0,
            linearMeterInputMl = 10.0,
            motifHeightCm = 30.0,
            motifLengthCm = 60.0
        )
        assertEquals(20.0, planDirectMl.totalSurfaceM2, 0.001)
        assertEquals(17, planDirectMl.totalMotifPieces)
        assertEquals(16.94, planDirectMl.netFaienceM2, 0.001)
        assertEquals(34, planDirectMl.totalCourdonPieces)

        // Direct Linear Meter WITHOUT height still computes motifs & courdons
        val planMlOnly = calculateFaienceMotifPlan(
            inputMode = FaienceInputMode.LINEAR_METER,
            totalSurfaceInputM2 = 0.0,
            wallHeightM = 0.0,
            linearMeterInputMl = 10.0,
            motifHeightCm = 30.0,
            motifLengthCm = 60.0
        )
        assertFalse(planMlOnly.hasNetFaience)
        assertEquals(17, planMlOnly.totalMotifPieces)
        assertEquals(34, planMlOnly.totalCourdonPieces)
    }

    @Test
    fun `faience motif calculator adapts cleanly to other formats like 25x75 and 30x90 and custom rows`() {
        // 25/75 on 20 m² / 2 m = 10 ml -> ceil(10/0.75) = 14 motifs (0.1875 m²/pc = 2.63 m²) -> 17.37 m² net faïence
        val plan2575Std = calculateFaienceMotifPlan(
            inputMode = FaienceInputMode.SURFACE_AND_HEIGHT,
            totalSurfaceInputM2 = 20.0,
            wallHeightM = 2.0,
            linearMeterInputMl = 0.0,
            motifHeightCm = 25.0,
            motifLengthCm = 75.0,
            courdonLengthMode = CourdonLengthMode.STANDARD_60CM
        )
        assertEquals(14, plan2575Std.totalMotifPieces)
        assertEquals(2.63, plan2575Std.motifCoveredM2, 0.001)
        assertEquals(17.37, plan2575Std.netFaienceM2, 0.001)
        assertEquals(34, plan2575Std.totalCourdonPieces) // 60cm standard courdons for 10 ml x 2

        val plan2575Match = calculateFaienceMotifPlan(
            inputMode = FaienceInputMode.SURFACE_AND_HEIGHT,
            totalSurfaceInputM2 = 20.0,
            wallHeightM = 2.0,
            linearMeterInputMl = 0.0,
            motifHeightCm = 25.0,
            motifLengthCm = 75.0,
            courdonLengthMode = CourdonLengthMode.MATCH_MOTIF
        )
        assertEquals(28, plan2575Match.totalCourdonPieces) // 14 motifs x 2

        // 30/90 on 27 m² / 2.5 m = 10.8 ml -> exact 12 motifs (guarded against floating-point overshoot)
        val plan3090 = calculateFaienceMotifPlan(
            inputMode = FaienceInputMode.SURFACE_AND_HEIGHT,
            totalSurfaceInputM2 = 27.0,
            wallHeightM = 2.5,
            linearMeterInputMl = 0.0,
            motifHeightCm = 30.0,
            motifLengthCm = 90.0
        )
        assertEquals(10.8, plan3090.linearMetersMl, 0.001)
        assertEquals(12, plan3090.totalMotifPieces)
        assertEquals(3.24, plan3090.motifCoveredM2, 0.001)
        assertEquals(23.76, plan3090.netFaienceM2, 0.001)
        assertEquals(36, plan3090.totalCourdonPieces) // ceil(10.8 / 0.60) = 18 x 2 = 36
    }

    @Test
    fun `plinthe calculator computes exact 8x45 and 8x60 for 5x5 room and 25m2 surface-only estimate`() {
        // Case 1: Exact 5m x 5m square room -> 25 m², 20 ml perimeter
        val exact5x5 = calculatePlinthePlan(
            inputMode = PlintheInputMode.ROOM_DIMENSIONS,
            surfaceInputM2 = 0.0,
            roomLengthM = 5.0,
            roomWidthM = 5.0,
            linearMeterInputMl = 0.0
        )
        assertEquals(25.0, exact5x5.floorSurfaceM2, 0.001)
        assertEquals(20.0, exact5x5.netLinearMetersMl, 0.001)
        assertEquals(45, exact5x5.pieces8x45) // ceil(20 / 0.45) = 45 pcs
        assertEquals(3, exact5x5.cartons8x45) // ceil(45 / 20) = 3 ctn (60 pcs = 27 ml)
        assertEquals(34, exact5x5.pieces8x60) // ceil(20 / 0.60) = 34 pcs (20.40 ml)
        assertEquals(20.40, exact5x5.coveredMl8x60, 0.001)

        // Case 2: Client only knows 25 m² surface (1 square room) -> 4 * sqrt(25) = 20 ml
        val surfaceOnly25 = calculatePlinthePlan(
            inputMode = PlintheInputMode.SURFACE_ESTIMATE,
            surfaceInputM2 = 25.0,
            roomLengthM = 0.0,
            roomWidthM = 0.0,
            linearMeterInputMl = 0.0,
            roomCount = 1,
            roomShape = PlintheRoomShape.SQUARE
        )
        assertEquals(5.0, surfaceOnly25.estimatedSquareSideM, 0.001)
        assertEquals(20.0, surfaceOnly25.netLinearMetersMl, 0.001)
        assertEquals(45, surfaceOnly25.pieces8x45)
        assertEquals(3, surfaceOnly25.cartons8x45)
        assertEquals(34, surfaceOnly25.pieces8x60)

        // Case 3: Multi-room surface estimate (60 m² across 3 rooms -> 4 * sqrt(180) = 53.67 ml)
        val multiRoom60 = calculatePlinthePlan(
            inputMode = PlintheInputMode.SURFACE_ESTIMATE,
            surfaceInputM2 = 60.0,
            roomLengthM = 0.0,
            roomWidthM = 0.0,
            linearMeterInputMl = 0.0,
            roomCount = 3,
            roomShape = PlintheRoomShape.SQUARE
        )
        assertEquals(53.67, multiRoom60.netLinearMetersMl, 0.01)
        assertEquals(90, multiRoom60.pieces8x60) // ceil(53.6656 / 0.60) = 90
        assertEquals(120, multiRoom60.pieces8x45) // ceil(53.6656 / 0.45) = 120 -> 6 ctn
        assertEquals(6, multiRoom60.cartons8x45)

        // Case 4: Exact carton boundary (18 ml = 40 pcs of 8/45 = 2 cartons exact) + door deduction
        val withDoors = calculatePlinthePlan(
            inputMode = PlintheInputMode.LINEAR_METER,
            surfaceInputM2 = 0.0,
            roomLengthM = 0.0,
            roomWidthM = 0.0,
            linearMeterInputMl = 19.8,
            doorCount = 2, // -1.80 m -> 18.00 ml exact
            doorWidthM = 0.90,
            piecesPerCarton8x45 = 20
        )
        assertEquals(18.0, withDoors.netLinearMetersMl, 0.001)
        assertEquals(40, withDoors.pieces8x45)
        assertEquals(2, withDoors.cartons8x45)
        assertEquals(30, withDoors.pieces8x60)
    }

    @Test
    fun `surface pack builder converts calculated faience motif and courdons into 3 exact cart items`() {
        val faienceModel = TileProduct(
            id = "p_30_60_luna",
            name = "30/60 FCM LUNA BEIGE",
            usine = "FCM",
            dimension = "30/60",
            m2Carton = 1.08,
            pcs = 6,
            prix = 750.0,
            prixAchat = 550.0,
            stock = 100
        )
        val plan = calculateFaienceMotifPlan(
            inputMode = FaienceInputMode.SURFACE_AND_HEIGHT,
            totalSurfaceInputM2 = 20.0,
            wallHeightM = 2.0,
            linearMeterInputMl = 0.0,
            motifHeightCm = 30.0,
            motifLengthCm = 60.0
        )
        // 16.94 m² / 1.08 m²/ctn = 15.68 -> 16 cartons (17.28 m²)
        val packItems = buildFaiencePackCartItems(
            formatLabel = "30/60",
            includeFaience = true,
            selectedFaienceProduct = faienceModel,
            customFaienceName = "",
            faienceCartons = 16,
            faienceNetM2 = plan.netFaienceM2,
            faiencePricePerM2 = 750.0,
            includeMotif = true,
            selectedMotifProduct = null,
            motifPieces = plan.totalMotifPieces, // 17
            motifPricePerPiece = 400.0,
            includeCourdon = true,
            courdonPreset = PACK_COURDON_PRESETS.first(), // 2/60 Simple 150 DA
            courdonColor = "Doré",
            courdonPieces = plan.totalCourdonPieces, // 34
            courdonPricePerPiece = 150.0
        )

        assertEquals(3, packItems.size)

        // 1. Faïence: 16 ctn * 1.08 = 17.28 m² * 750 = 12 960 DA
        assertEquals("p_30_60_luna", packItems[0].productId)
        assertEquals(16, packItems[0].cartons)
        assertEquals(17.28, packItems[0].m2Total, 0.001)
        assertEquals(12960.0, packItems[0].sousTotal, 0.01)

        // 2. Motifs: 17 pcs * 400 = 6 800 DA
        assertEquals(17, packItems[1].pieces)
        assertEquals("piece", packItems[1].pricingUnit)
        assertEquals(6800.0, packItems[1].sousTotal, 0.01)

        // 3. Courdons: 34 pcs * 150 = 5 100 DA
        assertEquals(34, packItems[2].pieces)
        assertEquals("2/60", packItems[2].dimension)
        assertEquals("piece", packItems[2].pricingUnit)
        assertEquals(5100.0, packItems[2].sousTotal, 0.01)

        // Total Pack = 24 860 DA
        assertEquals(24860.0, packItems.sumOf { it.sousTotal }, 0.01)
    }

    @Test
    fun `panier direct meters input converts wanted m2 to exact cartons and delivered surface`() {
        // 25 m² wanted with 1.44 m²/carton -> 18 cartons (25.92 m²)
        val (cartons25, delivered25) = previewCartMetersConversion(25.0, 1.44)
        assertEquals(18, cartons25)
        assertEquals(25.92, delivered25, 0.001)

        // Exact carton boundary: 14.4 m² with 1.44 m²/carton -> 10 cartons exact (no float overshoot to 11!)
        val (cartonsExact, deliveredExact) = previewCartMetersConversion(14.4, 1.44)
        assertEquals(10, cartonsExact)
        assertEquals(14.40, deliveredExact, 0.001)

        // Zero or invalid input safely returns 0
        assertEquals(0 to 0.0, previewCartMetersConversion(0.0, 1.44))
        assertEquals(0 to 0.0, previewCartMetersConversion(-5.0, 1.44))
    }
}
