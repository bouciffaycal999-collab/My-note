package com.example.data.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToLong

data class WaRecipient(
    val name: String = "",
    val phone: String = ""
)

data class TileProduct(
    val id: String,
    val name: String,
    val usine: String,
    val dimension: String,
    val m2Carton: Double,
    val pcs: Int,
    val prix: Double,
    val prixAchat: Double,
    val stock: Int,
    val color: String = "#94a3b8",
    val colorName: String = "Gris Minéral",
    val image: String = "",
    val finition: String = "brillant",
    val choix: String = "1er Choix",
    val categorie: String = "sol",
    val description: String = "",
    val pricingUnit: String = "carton", // "carton" | "piece"
    val prixPiece: Double = 0.0,
    val bain: String = "",
    val calibre: String = "",
    val prixGros: Double = 0.0
) {
    val isSoldByPiece: Boolean get() = pricingUnit == "piece"
}

/**
 * Resolves the active unit price for a tile product based on the selected customer's pricing tier.
 * "Revendeur" and "Maçon" customers automatically receive prixGros when configured (> 0).
 */
fun resolveEffectiveUnitPrice(product: TileProduct, customerTier: String?): Double {
    val tier = customerTier?.trim().orEmpty()
    val isProTier = tier.equals("Revendeur", ignoreCase = true) ||
        tier.equals("Maçon", ignoreCase = true) ||
        tier.equals("Macon", ignoreCase = true)
    return if (isProTier && product.prixGros > 0.0) product.prixGros else product.prix
}

data class CartItem(
    val id: String,
    val productId: String,
    val name: String,
    val usine: String,
    val dimension: String,
    val m2Carton: Double,
    val pcs: Int,
    val prixUnitaire: Double,
    val prixAchat: Double,
    val cartons: Int,
    val pieces: Int = 0,
    val m2Total: Double,
    val sousTotal: Double,
    val bain: String = "",
    val calibre: String = "",
    val color: String = "#94a3b8",
    val colorName: String = "",
    val image: String = "",
    val prixPiece: Double = 0.0,
    val pricingUnit: String = "carton",
    val choix: String = "",
    val deliveredCartons: Int = -1,
    val returnedCartons: Int = 0
) {
    val isSoldByPiece: Boolean get() = pricingUnit == "piece"
    val isCc: Boolean
        get() = choix.equals("CC", ignoreCase = true) || choix.contains("2", ignoreCase = true)
    val effectiveDeliveredCartons: Int
        get() = if (deliveredCartons < 0) cartons else deliveredCartons.coerceIn(0, cartons)
    val pendingDeliveryCartons: Int
        get() = (cartons - effectiveDeliveredCartons - returnedCartons).coerceAtLeast(0)
    val returnableCartons: Int
        get() = (cartons - returnedCartons).coerceAtLeast(0)
    val effectiveLoosePieceRate: Double
        get() = if (prixPiece > 0.0) prixPiece else ((m2Carton / pcs.coerceAtLeast(1)) * prixUnitaire * 100.0).roundToLong() / 100.0
    val piecesSubtotalDa: Double
        get() = when {
            cartons == 0 && (isSoldByPiece || pieces > 0) -> sousTotal
            cartons > 0 && pieces > 0 -> ((pieces * effectiveLoosePieceRate) * 100.0).roundToLong() / 100.0
            else -> 0.0
        }
    val cartonsSubtotalDa: Double
        get() = when {
            cartons > 0 && pieces > 0 -> (sousTotal - piecesSubtotalDa).coerceAtLeast(0.0)
            cartons > 0 -> sousTotal
            else -> 0.0
        }
}

data class FreeLineItem(
    val id: String,
    val designation: String,
    val quantite: Double,
    val unite: String,
    val prixUnitaire: Double,
    val sousTotal: Double
)

data class InvoiceClient(
    val id: String? = null,
    val nom: String,
    val tel: String = "",
    val adresse: String = ""
)

data class Invoice(
    val id: String,
    val reference: String,
    val date: String,
    val vendeur: String,
    val magasin: String,
    val client: InvoiceClient,
    val articles: List<CartItem>,
    val lignesLibres: List<FreeLineItem>,
    val totalBrut: Double,
    val remise: Double,
    val totalNet: Double,
    val montantPaye: Double,
    val resteAPayer: Double,
    val modePaiement: String,
    val statut: String,
    val notes: String = "",
    val vendeurTel: String = "",
    val hidePrices: Boolean = false,
    val hideDetails: Boolean = false,
    val whatsappSentBeforePrint: Boolean = false,
    val destination: String = "",
    val deductionRetour: Double = 0.0
) {
    val isDevis: Boolean
        get() = statut.equals("Devis", ignoreCase = true) || reference.startsWith("DEV-", ignoreCase = true)
    val isAvoir: Boolean
        get() = statut.equals("Avoir", ignoreCase = true) || reference.startsWith("AVR-", ignoreCase = true)
    val isRealSale: Boolean
        get() = !isDevis && !isAvoir
    val totalPendingDeliveryCartons: Int
        get() = if (!isRealSale) 0 else articles.sumOf { it.pendingDeliveryCartons }
    val hasPendingDelivery: Boolean
        get() = totalPendingDeliveryCartons > 0
}

data class HeldCart(
    val id: String,
    val label: String,
    val createdAt: String,
    val items: List<CartItem>,
    val freeLines: List<FreeLineItem> = emptyList()
) {
    val totalDa: Double
        get() = items.sumOf { it.sousTotal } + freeLines.sumOf { it.sousTotal }
    val lineCount: Int
        get() = items.size + freeLines.size
}

data class VersementReceipt(
    val id: String,
    val customerId: String,
    val customerName: String,
    val customerTel: String,
    val date: String,
    val montant: Double,
    val ancienSolde: Double,
    val nouveauSolde: Double,
    val settledInvoiceRefs: List<String>,
    val auteur: String
)

data class Customer(
    val id: String,
    val nom: String,
    val tel: String,
    val adresse: String,
    val dette: Double,
    val totalAchats: Double,
    val tier: String = "Particulier" // "Particulier" | "Maçon" | "Revendeur"
)

data class SupplierArrivalLine(
    val productId: String,
    val productName: String,
    val cartons: Int,
    val m2Carton: Double,
    val prixAchatUnit: Double,
    val bain: String = "",
    val calibre: String = ""
) {
    val lineCostDa: Double
        get() = ((cartons * m2Carton.coerceAtLeast(0.01) * prixAchatUnit.coerceAtLeast(0.0)) * 100.0).roundToLong() / 100.0
}

data class SupplierArrivalResult(
    val supplier: Supplier,
    val movement: SupplierMovement,
    val totalCartonsAdded: Int,
    val totalCostDa: Double,
    val paidNowDa: Double,
    val updatedProductCount: Int
)

data class InventoryAuditSummary(
    val adjustedProducts: Int,
    val netCartonDelta: Int,
    val netValueDeltaDa: Double
)

data class Supplier(
    val id: String,
    val nom: String,
    val contact: String,
    val tel: String,
    val adresse: String,
    val usineRepresentee: String,
    val soldeDette: Double,
    val totalAchats: Double
)

data class SupplierMovement(
    val id: String,
    val supplierId: String,
    val date: String,
    val type: String, // "versement" | "achat"
    val montant: Double,
    val motif: String,
    val pieceJointeRef: String? = null,
    val auteur: String
)

data class CaisseMovement(
    val id: String,
    val date: String,
    val type: String, // "entree" | "sortie"
    val motif: String,
    val montant: Double,
    val auteur: String,
    val referenceFacture: String? = null
)

data class DailyCaisseCount(
    val id: String,
    val date: String,
    val magasin: String,
    val auteur: String,
    val fondOuverture: Double,
    val ventesEspecesJour: Double,
    val entreesDiversJour: Double,
    val sortiesJour: Double,
    val theorique: Double,
    val compteReel: Double,
    val ecart: Double,
    val notes: String = ""
)

data class StaffUser(
    val id: String,
    val name: String,
    val role: String, // "admin" | "gerant" | "vendeur"
    val pin: String,
    val magasin: String,
    val allowedTabs: List<String>,
    val canEditInvoices: Boolean,
    val canDeleteInvoices: Boolean,
    val canModifyStock: Boolean,
    val canViewSuppliers: Boolean,
    val canViewCaisse: Boolean,
    val canManageUsers: Boolean,
    val active: Boolean = true,
    val phone: String = "",
    val canViewPrixAchat: Boolean = (role == "admin" || role == "gerant"),
    val canModifyPrices: Boolean = (role == "admin" || role == "gerant")
)

data class RolePolicy(
    val roleId: String, // "admin" | "gerant" | "vendeur"
    val displayName: String,
    val description: String = "",
    val canViewPrixAchat: Boolean = false,
    val canModifyPrices: Boolean = false,
    val canModifyStock: Boolean = false,
    val canEditInvoices: Boolean = true,
    val canDeleteInvoices: Boolean = false,
    val canViewSuppliers: Boolean = false,
    val canViewCaisse: Boolean = false,
    val canManageUsers: Boolean = false,
    val allowedTabs: List<String> = emptyList()
)

data class EffectivePermissions(
    val roleId: String,
    val canViewPrixAchat: Boolean,
    val canModifyPrices: Boolean,
    val canModifyStock: Boolean,
    val canAddProducts: Boolean,
    val canEditInvoices: Boolean,
    val canDeleteInvoices: Boolean,
    val canViewSuppliers: Boolean,
    val canViewCaisse: Boolean,
    val canManageUsers: Boolean,
    val canHideInvoicePrices: Boolean,
    val allowedTabs: List<String>
)

data class StoreInfo(
    val nom: String,
    val nomArabe: String,
    val slogan: String,
    val adresse: String,
    val tel: String,
    val tel2: String,
    val email: String,
    val rc: String,
    val nif: String,
    val nis: String,
    val ai: String,
    val gerantWaPhone: String = "",
    val invoiceWaRecipients: List<WaRecipient> = PosSeeds.DEFAULT_INVOICE_WA_RECIPIENTS,
    val dispoWaRecipients: List<WaRecipient> = PosSeeds.DEFAULT_DISPO_WA_RECIPIENTS,
    val whatsappQrPhone: String = "0676754124",
    val depotLocationUrl: String = PosSeeds.DEFAULT_DEPOT_LOCATION_URL
)

data class AppSettings(
    val printFormat: String = "A4",
    val a5Density: String = "compact",
    val hideBainTeinteFacture: Boolean = true,
    val colorTheme: String = "ceramica_showroom",
    val touchFriendlyMode: Boolean = true,
    val activeStore: String = "Depot Remchi",
    val enableSoundAlerts: Boolean = true,
    val deviceSellerName: String = "",
    val deviceSellerPhone: String = "",
    val enforcePriceFloor: Boolean = false,
    val minMarginPercent: Double = 0.0,
    val maxSellerDiscountPercent: Double = 10.0
)

fun resolveFloorUnitPrice(prixAchat: Double, minMarginPercent: Double = 0.0): Double {
    if (prixAchat <= 0.0) return 0.0
    val multiplier = 1.0 + (minMarginPercent.coerceAtLeast(0.0) / 100.0)
    return ((prixAchat * multiplier) * 100.0).roundToLong() / 100.0
}

fun isPriceBelowFloor(
    unitPrice: Double,
    prixAchat: Double,
    settings: AppSettings,
    isAdmin: Boolean
): Boolean {
    if (isAdmin || !settings.enforcePriceFloor || prixAchat <= 0.0) return false
    val floor = resolveFloorUnitPrice(prixAchat, settings.minMarginPercent)
    return unitPrice + 0.01 < floor
}

data class DepotNotification(
    val id: String,
    val invoiceId: String,
    val reference: String,
    val date: String,
    val vendeur: String,
    val magasin: String,
    val client: String,
    val clientTel: String,
    val totalNet: Double,
    val m2Total: Double,
    val cartonsTotal: Int,
    val articlesSummary: String,
    val read: Boolean = false
)

data class DiversPreset(
    val designation: String,
    val unite: String,
    val prix: Double
)

object PosSeeds {
    val ALL_TABS = listOf(
        "pos", "surface", "stock", "invoices", "cloture", "dashboard",
        "customers", "suppliers", "caisse", "settings"
    )

    val MANAGER_TABS = ALL_TABS

    val SELLER_TABS = listOf("pos", "surface", "invoices", "cloture")

    val VALID_ROLES = listOf("admin", "vendeur")

    val DEFAULT_ROLE_POLICIES = listOf(
        RolePolicy(
            roleId = "admin",
            displayName = "Administrateur",
            description = "Accès complet : prix d'achat, situation fournisseurs, caisse, factures, paramètres et PIN",
            canViewPrixAchat = true,
            canModifyPrices = true,
            canModifyStock = true,
            canEditInvoices = true,
            canDeleteInvoices = true,
            canViewSuppliers = true,
            canViewCaisse = true,
            canManageUsers = true,
            allowedTabs = ALL_TABS
        ),
        RolePolicy(
            roleId = "vendeur",
            displayName = "Vendeur",
            description = "Mode Vendeur rapide : Vente, Surface, Factures & Clôture fin de journée",
            canViewPrixAchat = false,
            canModifyPrices = true,
            canModifyStock = false,
            canEditInvoices = true,
            canDeleteInvoices = false,
            canViewSuppliers = false,
            canViewCaisse = false,
            canManageUsers = false,
            allowedTabs = SELLER_TABS
        )
    )

    fun defaultPolicyForRole(role: String): RolePolicy =
        DEFAULT_ROLE_POLICIES.find { it.roleId == role } ?: DEFAULT_ROLE_POLICIES.last()

    fun resolveEffectivePermissions(
        user: StaffUser?,
        policies: List<RolePolicy> = DEFAULT_ROLE_POLICIES
    ): EffectivePermissions {
        if (user == null || user.role != "admin") {
            return EffectivePermissions(
                roleId = "vendeur",
                canViewPrixAchat = false,
                canModifyPrices = true,
                canModifyStock = false,
                canAddProducts = true,
                canEditInvoices = true,
                canDeleteInvoices = false,
                canViewSuppliers = false,
                canViewCaisse = false,
                canManageUsers = false,
                canHideInvoicePrices = false,
                allowedTabs = SELLER_TABS
            )
        }
        return EffectivePermissions(
            roleId = "admin",
            canViewPrixAchat = true,
            canModifyPrices = true,
            canModifyStock = true,
            canAddProducts = true,
            canEditInvoices = true,
            canDeleteInvoices = true,
            canViewSuppliers = true,
            canViewCaisse = true,
            canManageUsers = true,
            canHideInvoicePrices = true,
            allowedTabs = ALL_TABS
        )
    }

    val SITES = listOf("Depot Remchi", "Magasin Ihab", "Magasin Zohir")

    val DEFAULT_INVOICE_WA_RECIPIENTS = listOf(
        WaRecipient(name = "Administrateur (0676754124)", phone = "0676754124")
    )

    val DEFAULT_DISPO_WA_RECIPIENTS = listOf(
        WaRecipient(name = "Principal / Admin (0676754124)", phone = "0676754124")
    )

    const val DEFAULT_DEPOT_LOCATION_URL = "https://goo.gl/maps/nv4dvTfngu8WgYKy8"

    val DEFAULT_STORE_INFO = StoreInfo(
        nom = "CASACERAM",
        nomArabe = "",
        slogan = "Dalle de Sol, Faïence Murale, Parquet Céramique & Accessoires de Pose",
        adresse = "ZONE INDUSTRIELLE REMCHI",
        tel = "0676754124",
        tel2 = "0674369056",
        email = "",
        rc = "",
        nif = "",
        nis = "",
        ai = "",
        gerantWaPhone = "",
        invoiceWaRecipients = DEFAULT_INVOICE_WA_RECIPIENTS,
        dispoWaRecipients = DEFAULT_DISPO_WA_RECIPIENTS,
        whatsappQrPhone = "0676754124",
        depotLocationUrl = DEFAULT_DEPOT_LOCATION_URL
    )

    val DEFAULT_SETTINGS = AppSettings()

    val DEFAULT_USERS = listOf(
        StaffUser(
            id = "u_vendeur",
            name = "Vendeur",
            role = "vendeur",
            pin = "",
            magasin = "Depot Remchi",
            allowedTabs = SELLER_TABS,
            canEditInvoices = true,
            canDeleteInvoices = false,
            canModifyStock = false,
            canViewSuppliers = false,
            canViewCaisse = false,
            canManageUsers = false,
            active = true,
            phone = "",
            canViewPrixAchat = false,
            canModifyPrices = true
        ),
        StaffUser(
            id = "u_kader",
            name = "Administrateur",
            role = "admin",
            pin = "5697",
            magasin = "Depot Remchi",
            allowedTabs = ALL_TABS,
            canEditInvoices = true,
            canDeleteInvoices = true,
            canModifyStock = true,
            canViewSuppliers = true,
            canViewCaisse = true,
            canManageUsers = true,
            active = true,
            phone = "0676754124",
            canViewPrixAchat = true,
            canModifyPrices = true
        )
    )

    val DEFAULT_CUSTOMERS = listOf(
        Customer("c1", "Ahmed Benali", "0550 12 34 56", "Remchi Centre", 45000.0, 320000.0),
        Customer("c2", "Youcef Carreleur Professionnel", "0661 88 99 00", "Tlemcen Mansourah", 18500.0, 480000.0),
        Customer("c3", "Mohamed Chérif", "0770 44 55 66", "Hennaya", 0.0, 145000.0),
        Customer("c4", "Khaled Berrached", "0555 90 80 70", "Ain Temouchent", 62000.0, 750000.0)
    )

    val DEFAULT_SUPPLIERS = listOf(
        Supplier(
            id = "f1",
            nom = "Sarl Usine BCRAM Céramique",
            contact = "Direction Commerciale",
            tel = "041 55 66 77",
            adresse = "Zone Industrielle Oran",
            usineRepresentee = "BCRAM",
            soldeDette = 320000.0,
            totalAchats = 1850000.0
        ),
        Supplier(
            id = "f2",
            nom = "NOVA Céramique Oran",
            contact = "M. Kaddour",
            tel = "0550 77 88 99",
            adresse = "Es-Sénia, Oran",
            usineRepresentee = "NOVA",
            soldeDette = 150000.0,
            totalAchats = 1200000.0
        ),
        Supplier(
            id = "f3",
            nom = "CEVICO Céramique Tlemcen",
            contact = "Service Vente Usine",
            tel = "043 22 33 44",
            adresse = "Zone Industrielle Remchi",
            usineRepresentee = "CEVICO",
            soldeDette = 0.0,
            totalAchats = 890000.0
        ),
        Supplier(
            id = "f4",
            nom = "Comptoir National Céramique NCM",
            contact = "M. Boumediene",
            tel = "0662 11 22 33",
            adresse = "Sidi Bel Abbès",
            usineRepresentee = "NCM",
            soldeDette = 85000.0,
            totalAchats = 650000.0
        ),
        Supplier(
            id = "f5",
            nom = "BATI Céramique Alger",
            contact = "Représentant Ouest",
            tel = "0771 33 44 55",
            adresse = "Rouiba, Alger",
            usineRepresentee = "BATI",
            soldeDette = 210000.0,
            totalAchats = 2400000.0
        )
    )

    val DIVERS_PRESETS = listOf(
        DiversPreset("Colle Céramique C1 (25kg)", "sac", 750.0),
        DiversPreset("Colle Porcelaine C2 Haute Adhérence (25kg)", "sac", 1250.0),
        DiversPreset("Joint de Carrelage Hydrofuge (5kg)", "sac", 450.0),
        DiversPreset("Croisillons 2mm (Paquet 200u)", "u", 600.0),
        DiversPreset("Baguette Aluminium Chrome 2.5m", "u", 850.0),
        DiversPreset("Forfait Livraison & Transport Camion", "forfait", 3000.0),
        DiversPreset("Main d'Œuvre / Manutention Déchargement", "forfait", 2000.0)
    )

    val WHATSAPP_MANAGEMENT = "213676754124"
    val WHATSAPP_DEPOT = "213676754124"

    val DEFAULT_USER_CATALOG_MODELS: List<TileProduct> = listOf(
        // === ACCESSOIRES ===
        TileProduct(
            id = "p_user_plinthe_8_45",
            name = "PLINTHE 8/45",
            usine = "",
            dimension = "8/45",
            m2Carton = 1.0,
            pcs = 20,
            prix = 800.0,
            prixAchat = 500.0,
            stock = 150,
            color = "#E2D4C0",
            colorName = "Beige",
            categorie = "plinthe",
            pricingUnit = "carton",
            prixPiece = 0.0,
            description = "Plinthe 8/45 vendue par carton (défaut 800 DA/crt)"
        ),
        TileProduct(
            id = "p_user_plinthe_9_45",
            name = "PLINTHE 9/45",
            usine = "",
            dimension = "9/45",
            m2Carton = 1.0,
            pcs = 20,
            prix = 850.0,
            prixAchat = 550.0,
            stock = 120,
            color = "#E2D4C0",
            colorName = "Beige",
            categorie = "plinthe",
            pricingUnit = "carton",
            prixPiece = 0.0,
            description = "Plinthe 9/45 vendue par carton (défaut 850 DA/crt)"
        ),
        TileProduct(
            id = "p_user_plinthe_8_60",
            name = "PLINTHE 8/60",
            usine = "",
            dimension = "8/60",
            m2Carton = 1.0,
            pcs = 1,
            prix = 150.0,
            prixAchat = 90.0,
            stock = 300,
            color = "#94A3B8",
            colorName = "Gris",
            categorie = "plinthe",
            pricingUnit = "piece",
            prixPiece = 150.0,
            description = "Plinthe 8/60 à la pièce (défaut 150 DA/pc)"
        ),
        TileProduct(
            id = "p_user_plinthe_7_60",
            name = "PLINTHE 7/60",
            usine = "",
            dimension = "7/60",
            m2Carton = 1.0,
            pcs = 1,
            prix = 150.0,
            prixAchat = 90.0,
            stock = 200,
            color = "#94A3B8",
            colorName = "Gris",
            categorie = "plinthe",
            pricingUnit = "piece",
            prixPiece = 150.0,
            description = "Plinthe 7/60 à la pièce (défaut 150 DA/pc)"
        ),
        TileProduct(
            id = "p_user_friza_10_60",
            name = "FRIZA 10/60",
            usine = "",
            dimension = "10/60",
            m2Carton = 1.0,
            pcs = 1,
            prix = 250.0,
            prixAchat = 150.0,
            stock = 150,
            color = "#94A3B8",
            colorName = "Gris",
            categorie = "frise",
            pricingUnit = "piece",
            prixPiece = 250.0,
            description = "Frise 10/60 à la pièce (défaut 250 DA/pc)"
        ),
        TileProduct(
            id = "p_user_friza_10_60_dore",
            name = "FRIZE 10/60 DORÉ",
            usine = "",
            dimension = "10/60",
            m2Carton = 1.0,
            pcs = 1,
            prix = 300.0,
            prixAchat = 180.0,
            stock = 150,
            color = "#D4AF37",
            colorName = "Doré",
            categorie = "frise",
            pricingUnit = "piece",
            prixPiece = 300.0,
            description = "Frise 10/60 Doré à la pièce (défaut 300 DA/pc)"
        ),
        TileProduct(
            id = "p_user_motif_30_60",
            name = "MOTIF 30/60",
            usine = "",
            dimension = "30/60",
            m2Carton = 1.0,
            pcs = 1,
            prix = 400.0,
            prixAchat = 250.0,
            stock = 100,
            color = "#D97706",
            colorName = "Beige/Décor",
            categorie = "motif",
            pricingUnit = "piece",
            prixPiece = 400.0,
            description = "Motif 30/60 douche/cuisine (défaut 400 DA/pc)"
        ),
        TileProduct(
            id = "p_user_motif_30_60_acaro",
            name = "MOTIF 3D ACARO RELIEF",
            usine = "",
            dimension = "30/60",
            m2Carton = 1.0,
            pcs = 1,
            prix = 400.0,
            prixAchat = 250.0,
            stock = 100,
            color = "#D97706",
            colorName = "Relief",
            finition = "relief 3d",
            categorie = "motif",
            pricingUnit = "piece",
            prixPiece = 400.0,
            description = "Motif 3D Acaro Relief 30/60 (400 DA/pc)"
        ),
        TileProduct(
            id = "p_user_motif_45_90",
            name = "MOTIF 45/90",
            usine = "",
            dimension = "45/90",
            m2Carton = 1.0,
            pcs = 1,
            prix = 600.0,
            prixAchat = 350.0,
            stock = 60,
            color = "#D97706",
            colorName = "Décor",
            categorie = "motif",
            pricingUnit = "piece",
            prixPiece = 600.0,
            description = "Motif 45/90 (défaut 600 DA/pc)"
        ),
        TileProduct(
            id = "p_user_motif_45_90_warda",
            name = "MOTIF 45/90 WARDA",
            usine = "",
            dimension = "45/90",
            m2Carton = 1.0,
            pcs = 1,
            prix = 1200.0,
            prixAchat = 700.0,
            stock = 40,
            color = "#D4AF37",
            colorName = "Doré/Warda",
            categorie = "motif",
            pricingUnit = "piece",
            prixPiece = 1200.0,
            description = "Motif 45/90 Warda (1200 DA/pc)"
        ),
        TileProduct(
            id = "p_user_baguette_f_argent",
            name = "BAGUETTE F ARGENT",
            usine = "",
            dimension = "Pièce",
            m2Carton = 1.0,
            pcs = 1,
            prix = 500.0,
            prixAchat = 300.0,
            stock = 100,
            color = "#C0C8D2",
            colorName = "Argent",
            categorie = "accessoire",
            pricingUnit = "piece",
            prixPiece = 500.0
        ),
        TileProduct(
            id = "p_user_baguette_f_dore",
            name = "BAGUETTE F DORÉ",
            usine = "",
            dimension = "Pièce",
            m2Carton = 1.0,
            pcs = 1,
            prix = 500.0,
            prixAchat = 300.0,
            stock = 100,
            color = "#D4AF37",
            colorName = "Doré",
            categorie = "accessoire",
            pricingUnit = "piece",
            prixPiece = 500.0
        ),
        TileProduct(
            id = "p_user_baguette_f_noir",
            name = "BAGUETTE F NOIR",
            usine = "",
            dimension = "Pièce",
            m2Carton = 1.0,
            pcs = 1,
            prix = 600.0,
            prixAchat = 350.0,
            stock = 80,
            color = "#1E293B",
            colorName = "Noir",
            categorie = "accessoire",
            pricingUnit = "piece",
            prixPiece = 600.0
        ),
        TileProduct(
            id = "p_user_baguette_f_beige",
            name = "BAGUETTE F BEIGE",
            usine = "",
            dimension = "Pièce",
            m2Carton = 1.0,
            pcs = 1,
            prix = 600.0,
            prixAchat = 350.0,
            stock = 80,
            color = "#E2D4C0",
            colorName = "Beige",
            categorie = "accessoire",
            pricingUnit = "piece",
            prixPiece = 600.0
        ),
        TileProduct(
            id = "p_user_baguette_1coin",
            name = "BAGUETTE 1 COIN",
            usine = "",
            dimension = "Pièce",
            m2Carton = 1.0,
            pcs = 1,
            prix = 300.0,
            prixAchat = 180.0,
            stock = 150,
            color = "#C0C8D2",
            colorName = "Argent",
            categorie = "accessoire",
            pricingUnit = "piece",
            prixPiece = 300.0
        ),
        TileProduct(
            id = "p_user_baguette_2coin",
            name = "BAGUETTE 2 COINS",
            usine = "",
            dimension = "Pièce",
            m2Carton = 1.0,
            pcs = 1,
            prix = 350.0,
            prixAchat = 200.0,
            stock = 150,
            color = "#C0C8D2",
            colorName = "Argent",
            categorie = "accessoire",
            pricingUnit = "piece",
            prixPiece = 350.0
        ),
        TileProduct(
            id = "p_user_ciment_colle_25kg",
            name = "CIMENT COLLE 25 KG",
            usine = "",
            dimension = "25 kg",
            m2Carton = 1.0,
            pcs = 1,
            prix = 500.0,
            prixAchat = 350.0,
            stock = 250,
            color = "#64748B",
            colorName = "Gris",
            categorie = "accessoire",
            pricingUnit = "piece",
            prixPiece = 500.0,
            description = "Ciment Colle sac de 25 kg vendu par sac unité (500 DA)"
        ),
        TileProduct(
            id = "p_user_pate_a_joint_5kg",
            name = "PÂTE À JOINT 5 KG",
            usine = "",
            dimension = "5 kg",
            m2Carton = 1.0,
            pcs = 1,
            prix = 700.0,
            prixAchat = 450.0,
            stock = 200,
            color = "#F8FAFC",
            colorName = "Blanc",
            categorie = "accessoire",
            pricingUnit = "piece",
            prixPiece = 700.0,
            description = "Pâte à Joint sac de 5 kg vendu en unité (700 DA)"
        ),

        // === 120/60 & 80/80 ===
        TileProduct(
            id = "p_user_120_60_elva",
            name = "ELVA",
            usine = "ELVA",
            dimension = "120/60",
            m2Carton = 1.44,
            pcs = 2,
            prix = 1700.0,
            prixAchat = 1200.0,
            stock = 60,
            color = "#E2D4C0",
            colorName = "Beige",
            pricingUnit = "carton",
            prixPiece = 1500.0
        ),
        TileProduct(
            id = "p_user_120_60_bati",
            name = "BATI",
            usine = "BATI",
            dimension = "120/60",
            m2Carton = 1.44,
            pcs = 2,
            prix = 1500.0,
            prixAchat = 1100.0,
            stock = 80,
            color = "#FFFFFF",
            colorName = "Blanc",
            pricingUnit = "piece",
            prixPiece = 1500.0
        ),
        TileProduct(
            id = "p_user_120_60_bcram",
            name = "BCRAM",
            usine = "BCRAM",
            dimension = "120/60",
            m2Carton = 1.44,
            pcs = 2,
            prix = 1500.0,
            prixAchat = 1100.0,
            stock = 80,
            color = "#94A3B8",
            colorName = "Gris",
            pricingUnit = "piece",
            prixPiece = 1500.0
        ),
        TileProduct(
            id = "p_user_120_60_porcelaine",
            name = "PORCELAINE",
            usine = "PORCELAINE",
            dimension = "120/60",
            m2Carton = 1.44,
            pcs = 2,
            prix = 2500.0,
            prixAchat = 1800.0,
            stock = 50,
            color = "#F8FAFC",
            colorName = "Blanc Marbré",
            finition = "polie",
            pricingUnit = "piece",
            prixPiece = 2500.0
        ),
        TileProduct(
            id = "p_user_120_60_ncm",
            name = "NCM MCM",
            usine = "NCM",
            dimension = "120/60",
            m2Carton = 1.44,
            pcs = 2,
            prix = 1800.0,
            prixAchat = 1300.0,
            stock = 60,
            color = "#CBD5E1",
            colorName = "Gris Clair",
            pricingUnit = "piece",
            prixPiece = 1800.0
        ),
        TileProduct(
            id = "p_user_120_60_tecno_ceram",
            name = "TECNO CERAM",
            usine = "TECNO CERAM",
            dimension = "120/60",
            m2Carton = 1.44,
            pcs = 2,
            prix = 1700.0,
            prixAchat = 1200.0,
            stock = 50,
            color = "#E2D4C0",
            colorName = "Beige",
            pricingUnit = "carton",
            prixPiece = 1500.0
        ),
        TileProduct(
            id = "p_user_80_80_porcelaine",
            name = "PORCELAINE",
            usine = "PORCELAINE",
            dimension = "80/80",
            m2Carton = 1.28,
            pcs = 2,
            prix = 1200.0,
            prixAchat = 850.0,
            stock = 50,
            color = "#F8FAFC",
            colorName = "Blanc Polie",
            finition = "polie",
            pricingUnit = "piece",
            prixPiece = 1200.0
        ),

        // === 60/60 (tous 1.44 m²/crt) ===
        TileProduct(
            id = "p_user_60_60_bcram",
            name = "BCRAM",
            usine = "BCRAM",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 950.0,
            prixAchat = 700.0,
            stock = 100,
            color = "#94A3B8",
            colorName = "Gris",
            prixPiece = 300.0
        ),
        TileProduct(
            id = "p_user_60_60_bati",
            name = "BATI",
            usine = "BATI",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 1200.0,
            prixAchat = 850.0,
            stock = 100,
            color = "#E2D4C0",
            colorName = "Beige",
            finition = "rectifié",
            prixPiece = 400.0
        ),
        TileProduct(
            id = "p_user_60_60_nova",
            name = "NOVA",
            usine = "NOVA",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 900.0,
            prixAchat = 650.0,
            stock = 100,
            color = "#CBD5E1",
            colorName = "Gris Clair",
            prixPiece = 300.0
        ),
        TileProduct(
            id = "p_user_60_60_ncm",
            name = "NCM",
            usine = "NCM",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 900.0,
            prixAchat = 650.0,
            stock = 100,
            color = "#CBD5E1",
            colorName = "Gris",
            prixPiece = 300.0
        ),
        TileProduct(
            id = "p_user_60_60_royal_cc",
            name = "ROYAL CC",
            usine = "ROYAL",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 900.0,
            prixAchat = 650.0,
            stock = 80,
            color = "#E2D4C0",
            colorName = "Beige",
            choix = "CC",
            prixPiece = 300.0
        ),
        TileProduct(
            id = "p_user_60_60_royal",
            name = "ROYAL",
            usine = "ROYAL",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 1200.0,
            prixAchat = 850.0,
            stock = 90,
            color = "#E2D4C0",
            colorName = "Beige",
            finition = "rectifié",
            prixPiece = 400.0
        ),
        TileProduct(
            id = "p_user_60_60_nova_cc",
            name = "NOVA CC",
            usine = "NOVA",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 750.0,
            prixAchat = 550.0,
            stock = 80,
            color = "#CBD5E1",
            colorName = "Gris",
            choix = "CC",
            prixPiece = 300.0
        ),
        TileProduct(
            id = "p_user_60_60_bcram_cc",
            name = "BCRAM CC",
            usine = "BCRAM",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 750.0,
            prixAchat = 550.0,
            stock = 80,
            color = "#94A3B8",
            colorName = "Gris",
            choix = "CC",
            prixPiece = 300.0
        ),
        TileProduct(
            id = "p_user_60_60_ncm_cc",
            name = "NCM CC",
            usine = "NCM",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 750.0,
            prixAchat = 550.0,
            stock = 80,
            color = "#94A3B8",
            colorName = "Gris",
            choix = "CC",
            prixPiece = 300.0
        ),
        TileProduct(
            id = "p_user_60_60_nest_gris",
            name = "NEST GRIS",
            usine = "NEST",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 1100.0,
            prixAchat = 800.0,
            stock = 100,
            color = "#94A3B8",
            colorName = "Gris",
            prixPiece = 400.0
        ),
        TileProduct(
            id = "p_user_60_60_nest_cc",
            name = "NEST CC",
            usine = "NEST",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 900.0,
            prixAchat = 650.0,
            stock = 60,
            color = "#94A3B8",
            colorName = "Gris",
            choix = "CC",
            prixPiece = 300.0
        ),

        // === 60/60 INOVA Porcelaine (4 Nouveaux modèles à 1000 DA/m²) ===
        TileProduct(
            id = "p_user_60_60_inova_parki_gris",
            name = "PARKI GRIS",
            usine = "INOVA",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 1000.0,
            prixAchat = 750.0,
            stock = 120,
            color = "#94A3B8",
            colorName = "Gris",
            finition = "polie",
            prixPiece = 400.0,
            description = "60/60 Inova Porcelaine Parki Gris (1000 DA/m²)"
        ),
        TileProduct(
            id = "p_user_60_60_inova_bleu_gauloise",
            name = "BLEU GAULOISE",
            usine = "INOVA",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 1000.0,
            prixAchat = 750.0,
            stock = 100,
            color = "#3B82F6",
            colorName = "Bleu",
            finition = "polie",
            prixPiece = 400.0,
            description = "60/60 Inova Porcelaine Bleu Gauloise (1000 DA/m²)"
        ),
        TileProduct(
            id = "p_user_60_60_inova_beige_fonce_marbre",
            name = "BEIGE FONCÉ MARBRE",
            usine = "INOVA",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 1000.0,
            prixAchat = 750.0,
            stock = 100,
            color = "#C4AD8E",
            colorName = "Beige Foncé",
            finition = "polie",
            prixPiece = 400.0,
            description = "60/60 Inova Porcelaine Beige Foncé Marbre (1000 DA/m²)"
        ),
        TileProduct(
            id = "p_user_60_60_inova_marin_mouchete",
            name = "MARIN MOUCHETÉ",
            usine = "INOVA",
            dimension = "60/60",
            m2Carton = 1.44,
            pcs = 4,
            prix = 1000.0,
            prixAchat = 750.0,
            stock = 100,
            color = "#1E3A8A",
            colorName = "Bleu Marine",
            finition = "polie",
            prixPiece = 400.0,
            description = "60/60 Inova Porcelaine Marin Moucheté (1000 DA/m²)"
        ),

        // === 45/45 ===
        TileProduct(
            id = "p_user_45_45_cmc",
            name = "CMC",
            usine = "CMC",
            dimension = "45/45",
            m2Carton = 1.00,
            pcs = 5,
            prix = 700.0,
            prixAchat = 500.0,
            stock = 100,
            color = "#CBD5E1",
            colorName = "Gris",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_45_45_bcram",
            name = "BCRAM",
            usine = "BCRAM",
            dimension = "45/45",
            m2Carton = 1.42,
            pcs = 7,
            prix = 800.0,
            prixAchat = 600.0,
            stock = 120,
            color = "#94A3B8",
            colorName = "Gris",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_45_45_ncm",
            name = "NCM",
            usine = "NCM",
            dimension = "45/45",
            m2Carton = 1.42,
            pcs = 7,
            prix = 780.0,
            prixAchat = 580.0,
            stock = 100,
            color = "#CBD5E1",
            colorName = "Gris Clair",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_45_45_cbm",
            name = "CBM",
            usine = "CBM",
            dimension = "45/45",
            m2Carton = 1.45,
            pcs = 7,
            prix = 750.0,
            prixAchat = 550.0,
            stock = 100,
            color = "#E2D4C0",
            colorName = "Beige",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_45_45_safcer",
            name = "SAFCER",
            usine = "SAFCER",
            dimension = "45/45",
            m2Carton = 1.45,
            pcs = 7,
            prix = 750.0,
            prixAchat = 550.0,
            stock = 100,
            color = "#E2D4C0",
            colorName = "Beige",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_45_45_corso",
            name = "CORSO",
            usine = "CORSO",
            dimension = "45/45",
            m2Carton = 1.42,
            pcs = 7,
            prix = 800.0,
            prixAchat = 600.0,
            stock = 80,
            color = "#C4AD8E",
            colorName = "Beige",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_45_45_bati",
            name = "BATI",
            usine = "BATI",
            dimension = "45/45",
            m2Carton = 1.42,
            pcs = 7,
            prix = 750.0,
            prixAchat = 550.0,
            stock = 100,
            color = "#CBD5E1",
            colorName = "Gris",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_45_45_nova",
            name = "NOVA",
            usine = "NOVA",
            dimension = "45/45",
            m2Carton = 1.42,
            pcs = 7,
            prix = 780.0,
            prixAchat = 580.0,
            stock = 100,
            color = "#E2D4C0",
            colorName = "Beige",
            prixPiece = 150.0
        ),

        // === 30/60, 30/65, 25/60, 30/30, 33/33 ===
        TileProduct(
            id = "p_user_30_60_fcm",
            name = "FCM",
            usine = "FCM",
            dimension = "30/60",
            m2Carton = 1.08,
            pcs = 6,
            prix = 700.0,
            prixAchat = 500.0,
            stock = 100,
            color = "#E2D4C0",
            colorName = "Beige",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_30_60_bordj",
            name = "BORDJ",
            usine = "BORDJ",
            dimension = "30/60",
            m2Carton = 1.44,
            pcs = 8,
            prix = 700.0,
            prixAchat = 500.0,
            stock = 120,
            color = "#CBD5E1",
            colorName = "Gris",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_30_65_fcm",
            name = "FCM",
            usine = "FCM",
            dimension = "30/65",
            m2Carton = 1.17,
            pcs = 6,
            prix = 720.0,
            prixAchat = 520.0,
            stock = 100,
            color = "#E2D4C0",
            colorName = "Beige",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_30_60_cmc",
            name = "CMC",
            usine = "CMC",
            dimension = "30/60",
            m2Carton = 1.08,
            pcs = 6,
            prix = 700.0,
            prixAchat = 500.0,
            stock = 90,
            color = "#CBD5E1",
            colorName = "Gris",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_30_60_royal_cc",
            name = "ROYAL CC",
            usine = "ROYAL",
            dimension = "30/60",
            m2Carton = 1.08,
            pcs = 6,
            prix = 850.0,
            prixAchat = 600.0,
            stock = 70,
            color = "#E2D4C0",
            colorName = "Beige",
            choix = "CC",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_30_30_bati",
            name = "BATI",
            usine = "BATI",
            dimension = "30/30",
            m2Carton = 1.44,
            pcs = 16,
            prix = 900.0,
            prixAchat = 650.0,
            stock = 80,
            color = "#CBD5E1",
            colorName = "Gris",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_33_33_fc",
            name = "FC",
            usine = "FC",
            dimension = "33/33",
            m2Carton = 1.42,
            pcs = 13,
            prix = 850.0,
            prixAchat = 600.0,
            stock = 80,
            color = "#E2D4C0",
            colorName = "Beige",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_30_60_fcm_relief",
            name = "FCM RELIEF 3D",
            usine = "FCM",
            dimension = "30/60",
            m2Carton = 1.08,
            pcs = 6,
            prix = 750.0,
            prixAchat = 550.0,
            stock = 80,
            color = "#E2D4C0",
            colorName = "Beige Relief",
            finition = "relief 3d",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_30_60_diva_fcm",
            name = "DIVA FCM",
            usine = "FCM",
            dimension = "30/60",
            m2Carton = 1.08,
            pcs = 6,
            prix = 900.0,
            prixAchat = 650.0,
            stock = 80,
            color = "#CBD5E1",
            colorName = "Gris",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_25_60_bordj",
            name = "BORDJ",
            usine = "BORDJ",
            dimension = "25/60",
            m2Carton = 1.50,
            pcs = 10,
            prix = 700.0,
            prixAchat = 500.0,
            stock = 100,
            color = "#E2D4C0",
            colorName = "Beige",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_25_60_nova",
            name = "NOVA",
            usine = "NOVA",
            dimension = "25/60",
            m2Carton = 1.50,
            pcs = 10,
            prix = 720.0,
            prixAchat = 520.0,
            stock = 100,
            color = "#CBD5E1",
            colorName = "Gris",
            prixPiece = 150.0
        ),
        TileProduct(
            id = "p_user_30_60_ncm",
            name = "NCM",
            usine = "NCM",
            dimension = "30/60",
            m2Carton = 1.26,
            pcs = 7,
            prix = 720.0,
            prixAchat = 520.0,
            stock = 90,
            color = "#CBD5E1",
            colorName = "Gris",
            prixPiece = 150.0
        ),

        // === 45/90 (1.21 m²/crt sauf vendus en pièce) ===
        TileProduct(
            id = "p_user_45_90_brik",
            name = "BRIK",
            usine = "",
            dimension = "45/90",
            m2Carton = 1.21,
            pcs = 3,
            prix = 700.0,
            prixAchat = 450.0,
            stock = 60,
            color = "#C4AD8E",
            colorName = "Brique/Beige",
            pricingUnit = "piece",
            prixPiece = 700.0
        ),
        TileProduct(
            id = "p_user_45_90_madera",
            name = "MADERA",
            usine = "",
            dimension = "45/90",
            m2Carton = 1.21,
            pcs = 3,
            prix = 700.0,
            prixAchat = 450.0,
            stock = 60,
            color = "#854D0E",
            colorName = "Bois/Marron",
            pricingUnit = "piece",
            prixPiece = 700.0
        ),
        TileProduct(
            id = "p_user_45_90_duraston",
            name = "DURASTON",
            usine = "",
            dimension = "45/90",
            m2Carton = 1.21,
            pcs = 3,
            prix = 1700.0,
            prixAchat = 1200.0,
            stock = 70,
            color = "#94A3B8",
            colorName = "Gris Pierre",
            prixPiece = 700.0
        ),
        TileProduct(
            id = "p_user_45_90_savana",
            name = "SAVANA",
            usine = "",
            dimension = "45/90",
            m2Carton = 1.21,
            pcs = 3,
            prix = 1100.0,
            prixAchat = 800.0,
            stock = 80,
            color = "#E2D4C0",
            colorName = "Beige",
            prixPiece = 700.0
        ),
        TileProduct(
            id = "p_user_45_90_rustic",
            name = "RUSTIC",
            usine = "",
            dimension = "45/90",
            m2Carton = 1.21,
            pcs = 3,
            prix = 1100.0,
            prixAchat = 800.0,
            stock = 80,
            color = "#C4AD8E",
            colorName = "Rustique",
            finition = "rustique",
            prixPiece = 700.0
        ),
        TileProduct(
            id = "p_user_45_90_nero",
            name = "NERO",
            usine = "",
            dimension = "45/90",
            m2Carton = 1.21,
            pcs = 3,
            prix = 1100.0,
            prixAchat = 800.0,
            stock = 80,
            color = "#1E293B",
            colorName = "Noir",
            prixPiece = 700.0
        ),
        TileProduct(
            id = "p_user_45_90_blanc",
            name = "BLANC",
            usine = "",
            dimension = "45/90",
            m2Carton = 1.21,
            pcs = 3,
            prix = 1100.0,
            prixAchat = 800.0,
            stock = 80,
            color = "#FFFFFF",
            colorName = "Blanc",
            prixPiece = 700.0
        ),
        TileProduct(
            id = "p_user_45_90_oxford",
            name = "OXFORD",
            usine = "",
            dimension = "45/90",
            m2Carton = 1.21,
            pcs = 3,
            prix = 1100.0,
            prixAchat = 800.0,
            stock = 80,
            color = "#CBD5E1",
            colorName = "Gris Clair",
            prixPiece = 700.0
        ),
        TileProduct(
            id = "p_user_45_90_natura",
            name = "NATURA",
            usine = "",
            dimension = "45/90",
            m2Carton = 1.21,
            pcs = 3,
            prix = 1100.0,
            prixAchat = 800.0,
            stock = 80,
            color = "#E2D4C0",
            colorName = "Naturel Beige",
            prixPiece = 700.0
        ),
        TileProduct(
            id = "p_user_45_90_travertino",
            name = "TRAVERTINO",
            usine = "",
            dimension = "45/90",
            m2Carton = 1.21,
            pcs = 3,
            prix = 1100.0,
            prixAchat = 800.0,
            stock = 80,
            color = "#E2D4C0",
            colorName = "Travertin",
            prixPiece = 700.0
        ),

        // === 15/60 (tous 1.00 m²/crt) ===
        TileProduct(
            id = "p_user_15_60_sit_ceram_rock_gris",
            name = "SIT CERAM ROCK GRIS",
            usine = "SIT CERAM",
            dimension = "15/60",
            m2Carton = 1.00,
            pcs = 11,
            prix = 1000.0,
            prixAchat = 750.0,
            stock = 100,
            color = "#94A3B8",
            colorName = "Gris",
            description = "15/60 Sit Ceram Rock Gris (1.00 m²/crt, 1000 DA/m²)"
        ),
        TileProduct(
            id = "p_user_15_60_sit_ceram_rock_blanc",
            name = "SIT CERAM ROCK BLANC",
            usine = "SIT CERAM",
            dimension = "15/60",
            m2Carton = 1.00,
            pcs = 11,
            prix = 1000.0,
            prixAchat = 750.0,
            stock = 100,
            color = "#FFFFFF",
            colorName = "Blanc",
            description = "15/60 Sit Ceram Rock Blanc (1.00 m²/crt, 1000 DA/m²)"
        ),
        TileProduct(
            id = "p_user_15_60_sit_ceram_rock_sauvage",
            name = "SIT CERAM ROCK SAUVAGE",
            usine = "SIT CERAM",
            dimension = "15/60",
            m2Carton = 1.00,
            pcs = 11,
            prix = 1000.0,
            prixAchat = 750.0,
            stock = 100,
            color = "#854D0E",
            colorName = "Sauvage",
            description = "15/60 Sit Ceram Rock Sauvage (1.00 m²/crt, 1000 DA/m²)"
        ),
        TileProduct(
            id = "p_user_15_60_inova_parki",
            name = "INOVA PARKI PORCELAINE",
            usine = "INOVA",
            dimension = "15/60",
            m2Carton = 1.00,
            pcs = 11,
            prix = 900.0,
            prixAchat = 650.0,
            stock = 100,
            color = "#C4AD8E",
            colorName = "Parquet Porcelaine",
            description = "15/60 Inova Parki Porcelaine (1.00 m²/crt, 900 DA/m²)"
        ),

        // === AUTRES MODÈLES ===
        TileProduct(
            id = "p_user_pedra_gris",
            name = "PEDRA",
            usine = "",
            dimension = "45/45",
            m2Carton = 1.42,
            pcs = 7,
            prix = 700.0,
            prixAchat = 500.0,
            stock = 80,
            color = "#94A3B8",
            colorName = "Toutes Couleurs",
            prixPiece = 150.0,
            description = "Pedra toutes les couleurs (700 DA/m²)"
        ),
        TileProduct(
            id = "p_user_alaves",
            name = "ALAVES",
            usine = "",
            dimension = "45/45",
            m2Carton = 1.42,
            pcs = 7,
            prix = 700.0,
            prixAchat = 500.0,
            stock = 80,
            color = "#E2D4C0",
            colorName = "Beige",
            prixPiece = 150.0,
            description = "Alaves (700 DA/m²)"
        )
    )
}

private val daSymbols = DecimalFormatSymbols(Locale.US).apply {
    groupingSeparator = ' '
    decimalSeparator = '.'
}
private val daIntFormat = DecimalFormat("#,##0", daSymbols)
private val m2Format = DecimalFormat("0.##", DecimalFormatSymbols(Locale.US))

fun formatDa(amount: Double): String {
    val rounded = amount.roundToLong()
    return "${daIntFormat.format(rounded)} DA"
}

fun formatNumber(amount: Double): String {
    val rounded = amount.roundToLong()
    return daIntFormat.format(rounded)
}

fun formatM2(m2: Double): String {
    val clean = (m2 * 100.0).let { kotlin.math.round(it) / 100.0 }
    return m2Format.format(clean)
}

fun toDzWhatsAppDigits(rawPhone: String): String? {
    val digits = rawPhone.filter { it.isDigit() }
    if (digits.isBlank()) return null
    return when {
        digits.startsWith("213") -> digits
        digits.startsWith("0") -> "213" + digits.drop(1)
        else -> "213$digits"
    }
}

private val LEADING_DIM_REGEX = Regex("""^\s*\d+\s*[/×x*]\s*\d+\s*""", RegexOption.IGNORE_CASE)
private val ANY_DIM_TOKEN_REGEX = Regex("""\b\d{1,3}\s*[/×x*]\s*\d{1,3}\b""", RegexOption.IGNORE_CASE)
private val CATEGORY_WORDS_REGEX = Regex(
    """(?i)\b(les\s+pleinthes|les\s+plinthes|pleinthes|pleinthe|plinthes|plinthe|plinta|dalle\s+de\s+sol|faience|faïence|parquet)\b"""
)
private val LEADING_SOL_MUR_REGEX = Regex("""(?i)^\s*(sol|mur)\b\s*""")
private val EDGE_CC_TOKEN_REGEX = Regex("""(?i)(^|\s+)CC(\s+|$)""")
private val TRAILING_RECT_TOKEN_REGEX = Regex("""(?i)\s+(RECT|RCTF|RCT)\s*$""")
private val WHITESPACE_REGEX = Regex("""\s+""")
private val COMBINING_DIACRITICS_REGEX = Regex("""\p{InCombiningDiacriticalMarks}+""")
private val SEARCH_SEPARATORS_REGEX = Regex("""[/×*._,\-'’]+""")
private val DIM_X_REGEX = Regex("""(?<=\d)[xX](?=\d)""")

fun formatDimensionSlash(dimension: String): String {
    return dimension.trim()
        .replace('×', '/')
        .replace('x', '/')
        .replace('X', '/')
        .replace('*', '/')
        .replace(Regex("""\s*/\s*"""), "/")
}

fun normalizeDimensionKey(dimension: String): String = formatDimensionSlash(dimension).lowercase(Locale.ROOT)

fun isPlinthe8x60(dimension: String): Boolean {
    val key = normalizeDimensionKey(dimension)
    return key == "8/60" || key == "60/8"
}

fun isPlinthe8x45(dimension: String): Boolean {
    val key = normalizeDimensionKey(dimension)
    return key == "8/45" || key == "45/8"
}

fun isTile120x60(dimension: String): Boolean {
    val key = normalizeDimensionKey(dimension)
    return key == "120/60" || key == "60/120"
}

/**
 * Tile formats that can be sold EITHER by m²/carton OR directly by individual piece:
 * 120/60, 80/80, and 45/90.
 */
fun isPieceEligibleTileDimension(dimension: String): Boolean {
    val key = normalizeDimensionKey(dimension)
    return key in setOf("120/60", "60/120", "80/80", "45/90", "90/45")
}

fun pieceAreaM2ForDimension(dimension: String, m2Carton: Double = 0.0, pcsPerCarton: Int = 0): Double {
    val key = normalizeDimensionKey(dimension)
    return when (key) {
        "120/60", "60/120" -> 0.72
        "80/80" -> 0.64
        "45/90", "90/45" -> 0.405
        else -> {
            val parts = key.split('/')
            if (parts.size == 2) {
                val w = parts[0].toDoubleOrNull() ?: 0.0
                val h = parts[1].toDoubleOrNull() ?: 0.0
                val area = (w / 100.0) * (h / 100.0)
                if (area > 0.005) return area
            }
            if (m2Carton > 0.0 && pcsPerCarton > 0) m2Carton / pcsPerCarton else 0.72
        }
    }
}

fun isTile60x60(dimension: String): Boolean = normalizeDimensionKey(dimension) == "60/60"

fun isTile45x45(dimension: String): Boolean = normalizeDimensionKey(dimension) == "45/45"

fun isFrise10x60(dimension: String): Boolean {
    val key = normalizeDimensionKey(dimension)
    return key == "10/60" || key == "60/10" || key == "10/30" || key == "30/10"
}

fun isCordonDimension(dimension: String): Boolean {
    val key = normalizeDimensionKey(dimension)
    return key in setOf("2/30", "30/2", "2/60", "60/2", "3/60", "60/3", "4/60", "60/4")
}

fun isAccessoryProduct(product: TileProduct): Boolean {
    val cat = product.categorie.trim().lowercase(Locale.ROOT)
    if (cat in setOf("plinthe", "accessoire", "frise")) return true
    val key = normalizeDimensionKey(product.dimension)
    if (key in setOf("8/45", "45/8", "8/60", "60/8", "8/33", "33/8", "9/45", "45/9", "7/60", "60/7", "10/60", "60/10", "10/30", "30/10", "2/30", "2/60", "3/60", "4/60")) {
        return true
    }
    val normName = normalizeSearchText(product.name)
    return normName.startsWith("cordon ") ||
        normName.startsWith("baguette ") ||
        normName.startsWith("taco ") ||
        normName.startsWith("ciment ") ||
        normName.startsWith("pate a joint") ||
        normName.startsWith("friza ") ||
        normName.startsWith("frise ") ||
        normName.startsWith("motif ")
}

fun isJetonProduct(product: TileProduct): Boolean {
    if (product.id.startsWith("p_jeton_") || product.id.contains("_jeton")) return true
    val trimmed = product.name.trim()
    val norm = normalizeSearchText(trimmed)
    val normUsine = normalizeSearchText(product.usine)
    val dimSlash = formatDimensionSlash(product.dimension)
    return trimmed.equals("VIERGE", ignoreCase = true) ||
        trimmed.equals("JETON", ignoreCase = true) ||
        norm.startsWith("jeton ") ||
        norm.contains(" jeton") ||
        (product.usine.isNotBlank() && (
            trimmed.equals(product.usine.trim(), ignoreCase = true) ||
            trimmed.equals("$dimSlash ${product.usine.trim()}", ignoreCase = true) ||
            (normUsine.isNotBlank() && (
                norm == normUsine ||
                norm == "ncm mcm" ||
                norm == "60 $normUsine" ||
                norm == "45 $normUsine" ||
                norm == "120 $normUsine" ||
                norm == "30 $normUsine" ||
                norm == "80 $normUsine" ||
                norm == "60 60 $normUsine" ||
                norm == "45 45 $normUsine" ||
                norm == "120 60 $normUsine"
            ))
        ))
}

/**
 * Normalizes Algerian tile factory aliases so duplicate seed jetons (e.g. "FCM MALEH" vs "FCM",
 * "MCM" vs "NCM", "TECHNO" vs "TECNO CERAM", "VIVA PORCELAINE" vs "PORCELAINE") map to one canonical factory key.
 */
fun canonicalFactoryAlias(rawFactory: String): String {
    val norm = normalizeSearchText(rawFactory)
    if (norm.isBlank()) return ""
    return when (norm) {
        "fcm", "fcm maleh", "fc maleh", "f c maleh", "fcm el maleh", "fc el maleh", "f c m" -> "fcm"
        "mcm", "ncm", "ncm mcm", "mcm ncm" -> "ncm"
        "techno", "tecno", "tecno ceram", "techno ceram" -> "tecno"
        "viva porcelaine", "porcelaine" -> "porcelaine"
        "bordj", "bordj ceram", "bordj ceramique" -> "bordj"
        "bcram", "b cram", "b-cram" -> "bcram"
        else -> norm
    }
}

/**
 * Returns the clean unified showroom display name for a factory filter chip
 * so aliases like "FCM", "FCM MALEH", and "FC MALEH" always appear as one single "FCM" button.
 */
fun canonicalFactoryDisplayLabel(rawFactory: String): String {
    val trimmed = rawFactory.trim()
    if (trimmed.isBlank()) return ""
    return when (canonicalFactoryAlias(trimmed)) {
        "fcm" -> "FCM"
        "ncm" -> "NCM"
        "tecno" -> "TECNO CERAM"
        "porcelaine" -> "PORCELAINE"
        "bordj" -> "BORDJ"
        "bcram" -> "BCRAM"
        else -> trimmed
    }
}

/**
 * Returns a canonical deduplication key (`dim|factory|choice`) if the product is a factory jeton/blank tile,
 * or null if it is a named decorative model / accessory.
 * Distinguishes `1er` vs `cc` (2ème choix) so CC jetons are never collapsed into 1er Choix jetons.
 */
fun canonicalJetonKey(product: TileProduct): String? {
    val dimKey = normalizeDimensionKey(product.dimension)
    if (dimKey.isBlank()) return null

    val normChoice = normalizeSearchText(product.choix)
    val normName = normalizeSearchText(product.name)
    val nameTokens = normName.split(' ').filter { it.isNotBlank() }
    val isCcChoice = normChoice == "cc" || normChoice.contains("2eme") || nameTokens.contains("cc")
    val choiceKey = if (isCcChoice) "cc" else "1er"

    val dimTokens = dimKey.split('/').filter { it.isNotBlank() }.toSet()
    val strippedName = nameTokens
        .filterNot { tok ->
            tok in dimTokens || tok == "jeton" || tok == "vierge" || tok == "cc" ||
                tok == "1er" || tok == "choix" || tok == "2eme"
        }
        .joinToString(" ")

    val factoryAlias = canonicalFactoryAlias(product.usine)
    val nameAlias = canonicalFactoryAlias(strippedName)

    val isFactoryOnlyJeton = isJetonProduct(product) ||
        (factoryAlias.isNotBlank() && (strippedName.isBlank() || nameAlias == factoryAlias))

    if (!isFactoryOnlyJeton) return null
    val resolvedFactory = factoryAlias.ifBlank { nameAlias }
    if (resolvedFactory.isBlank()) return null
    return "$dimKey|$resolvedFactory|$choiceKey"
}

private fun catalogProductPreferenceScore(p: TileProduct): Int {
    var score = 0
    if (p.id.startsWith("p_user_")) score += 100
    if (!p.id.startsWith("p_jeton_")) score += 40
    if (p.prix > 0.0 || p.prixPiece > 0.0) score += 60
    if (p.prixAchat > 0.0) score += 15
    if (p.stock > 0) score += 20
    if (p.image.isNotBlank()) score += 25
    if (p.finition.isNotBlank() && !p.finition.equals("brillant", ignoreCase = true)) score += 5
    return score
}

/**
 * Eliminates duplicate jetons (e.g. unpriced `p_jeton_*` from seed JSON overlapping priced `p_user_*`
 * or factory aliases like MCM/NCM, FCM MALEH/FCM, TECHNO/TECNO CERAM) as well as exact catalog duplicates,
 * always keeping the richest / user-configured entry.
 */
fun deduplicateCatalogProducts(products: List<TileProduct>): List<TileProduct> {
    if (products.size <= 1) return products
    val byId = LinkedHashMap<String, TileProduct>(products.size)
    products.forEach { p ->
        val existing = byId[p.id]
        if (existing == null || catalogProductPreferenceScore(p) > catalogProductPreferenceScore(existing)) {
            byId[p.id] = p
        }
    }

    val jetonWinners = LinkedHashMap<String, TileProduct>()
    val exactWinners = LinkedHashMap<String, TileProduct>()
    val orderedKeys = ArrayList<Pair<Boolean, String>>(byId.size)

    byId.values.forEach { p ->
        val jKey = canonicalJetonKey(p)
        if (jKey != null) {
            val prev = jetonWinners[jKey]
            if (prev == null) {
                jetonWinners[jKey] = p
                orderedKeys.add(true to jKey)
            } else if (catalogProductPreferenceScore(p) > catalogProductPreferenceScore(prev)) {
                jetonWinners[jKey] = p
            }
        } else {
            val dim = normalizeDimensionKey(p.dimension)
            val normName = normalizeSearchText(p.name)
            val normUsine = canonicalFactoryAlias(p.usine)
            val normFin = splitFinitions(p.finition).map { normalizeSearchText(it) }.sorted().joinToString(",")
            val normChoice = normalizeSearchText(p.choix)
            val exactKey = "$normName|$dim|$normUsine|$normFin|$normChoice|${p.pricingUnit.lowercase(Locale.ROOT)}"
            val prev = exactWinners[exactKey]
            if (prev == null) {
                exactWinners[exactKey] = p
                orderedKeys.add(false to exactKey)
            } else if (catalogProductPreferenceScore(p) > catalogProductPreferenceScore(prev)) {
                exactWinners[exactKey] = p
            }
        }
    }

    return orderedKeys.mapNotNull { (isJeton, key) ->
        if (isJeton) jetonWinners[key] else exactWinners[key]
    }
}

/**
 * Computes the top quick-access models (Favorites / Top 12) for 1-tap showroom billing:
 * 1. Ranks catalog products by actual sale frequency from past real invoices.
 * 2. Fills remaining slots up to [limit] with high-demand showroom staples (60/60, 30/60, 45/45, Ciment Colle, Courdons, Plinthes).
 */
fun computeTopFavoriteProducts(
    products: List<TileProduct>,
    invoices: List<Invoice>,
    limit: Int = 12
): List<TileProduct> {
    if (products.isEmpty() || limit <= 0) return emptyList()
    val byId = products.associateBy { it.id }
    val salesCount = linkedMapOf<String, Int>()
    invoices.asSequence()
        .filter { it.isRealSale }
        .forEach { inv ->
            inv.articles.forEach { item ->
                if (!item.productId.startsWith("onetime_") && byId.containsKey(item.productId)) {
                    salesCount[item.productId] = (salesCount[item.productId] ?: 0) + 1
                }
            }
        }
    val topSold = salesCount.entries
        .sortedByDescending { it.value }
        .mapNotNull { byId[it.key] }
        .take(limit)
    if (topSold.size >= limit) return topSold

    val pickedIds = topSold.map { it.id }.toMutableSet()
    val staplePriority: (TileProduct) -> Int = { p ->
        val n = normalizeSearchText(p.name)
        val dim = normalizeDimensionKey(p.dimension)
        var s = 0
        if (p.prix > 0.0 || p.prixPiece > 0.0) s += 50
        if (n.contains("colle") || n.contains("joint")) s += 35
        if (dim == "60x60") s += 30
        if (dim == "30x60") s += 28
        if (dim == "45x45") s += 24
        if (n.contains("courdon") || n.contains("cordon") || n.contains("listel") || dim.startsWith("2x60") || dim.startsWith("3x60") || dim.startsWith("4x60")) s += 26
        if (dim == "8x60" || dim == "8x45" || n.contains("plinth")) s += 22
        if (dim == "120x60") s += 20
        s
    }
    val extras = products.asSequence()
        .filter { it.id !in pickedIds }
        .sortedWith(compareByDescending<TileProduct> { staplePriority(it) }.thenBy { it.name })
        .take(limit - topSold.size)
        .toList()
    return topSold + extras
}

fun resolveDefaultPricingUnitForDimension(dimension: String): String = when {
    isPlinthe8x60(dimension) -> "piece"
    isPlinthe8x45(dimension) -> "carton"
    isFrise10x60(dimension) -> "piece"
    isCordonDimension(dimension) -> "piece"
    isTile120x60(dimension) -> "piece"
    else -> "carton"
}

/**
 * Colisage (m²/carton) rules by format and factory based on Algerian ceramic standard:
 * - Tous les 60/60: 1.44
 * - Tous les 45/45: BCRAM, BATI, NCM, NOVA: 1.42; CBM, SAFCER: 1.45; CMC: 1.00
 * - 30/65 FCM: 1.17
 * - 30/60: FCM: 1.08; BORDJ: 1.44; NCM: 1.26
 * - 25/60: BORDJ: 1.50; NOVA: 1.50
 * - 45/90: 1.21 (sauf ceux vendus en pièce)
 * - 80/80: vendu par pièce
 * - 15/60: 1.00 m²/carton
 */
fun resolveDefaultColisage(dimension: String, usine: String = ""): Double {
    val normDim = normalizeDimensionKey(dimension)
    val normUsine = normalizeSearchText(usine)
    return when (normDim) {
        "60/60" -> 1.44
        "45/45" -> when {
            normUsine.contains("cmc") -> 1.00
            normUsine.contains("cbm") || normUsine.contains("safcer") -> 1.45
            normUsine.contains("bcram") || normUsine.contains("bati") || normUsine.contains("ncm") || normUsine.contains("nova") -> 1.42
            else -> 1.42
        }
        "30/65" -> 1.17
        "30/60" -> when {
            normUsine.contains("bordj") -> 1.44
            normUsine.contains("ncm") -> 1.26
            else -> 1.08
        }
        "25/60" -> 1.50
        "45/90" -> 1.21
        "15/60" -> 1.00
        "120/60", "60/120" -> 1.44
        "80/80" -> 1.28
        "33/33", "30/30" -> 1.44
        else -> 1.44
    }
}

/**
 * Default piece price calculation:
 * - 45/45, 30/60, 30/65, 25/60, 25/70, 33/33, 30/30: 150 DA
 * - 60/60: 300 DA (coupe normale), 400 DA (coupe laser / rectifié / poli)
 * - 8/60, 7/60: 150 DA
 * - 10/60: 250 DA (300 DA doré)
 * - 120/60: 1500 DA (or m2 price / catalog rate)
 * - 80/80: 1200 DA
 * - 45/90: 700 DA (1200 DA warda)
 */
fun resolveDefaultPiecePriceForDimension(
    dimension: String,
    finition: String = "",
    m2Price: Double = 0.0,
    modelName: String = ""
): Double {
    val normDim = normalizeDimensionKey(dimension)
    val normFin = normalizeSearchText(finition)
    val normName = normalizeSearchText(modelName)

    return when {
        normDim == "60/60" -> {
            val isLaser = normFin.contains("rect") || normFin.contains("poli") ||
                normFin.contains("laser") || normName.contains("laser") || normName.contains("rect")
            if (isLaser) 400.0 else 300.0
        }
        normDim in setOf("45/45", "30/60", "30/65", "25/60", "25/70", "33/33", "30/30") -> 150.0
        isPlinthe8x60(normDim) || normDim in setOf("8/60", "60/8", "7/60", "60/7", "8/33", "33/8") -> 150.0
        normDim in setOf("10/60", "60/10") -> if (normName.contains("dore") || normFin.contains("dore")) 300.0 else 250.0
        isTile120x60(normDim) -> if (m2Price > 0.0) m2Price else 1500.0
        normDim == "80/80" -> 1200.0
        normDim == "45/90" -> if (normName.contains("warda")) 1200.0 else 700.0
        else -> if (m2Price > 0.0) m2Price else 150.0
    }
}

fun normalizeCategoryForStorage(rawCategory: String, dimension: String): String {
    val clean = rawCategory.trim().lowercase(Locale.ROOT)
    return when {
        isPlinthe8x60(dimension) || isPlinthe8x45(dimension) -> "plinthe"
        isFrise10x60(dimension) || clean.contains("fris") -> "frise"
        isCordonDimension(dimension) || clean.contains("access") -> "accessoire"
        clean.contains("plinth") || clean.contains("pleinth") || clean.contains("plint") -> "plinthe"
        clean.contains("faienc") || clean.contains("faïenc") -> "faience"
        clean.contains("parquet") -> "parquet"
        clean.isBlank() -> "sol"
        else -> clean
    }
}

fun normalizeModelNameForStorage(rawName: String, dimension: String = "", usine: String = ""): String {
    var s = rawName.trim()
    s = CATEGORY_WORDS_REGEX.replace(s, " ")
    s = LEADING_SOL_MUR_REGEX.replace(s, " ")
    val dimSlash = formatDimensionSlash(dimension)
    if (dimSlash.isNotBlank()) {
        val parts = dimSlash.split('/')
        if (parts.size == 2) {
            val dimRegex = Regex("""(?i)\b${Regex.escape(parts[0])}\s*[/×x*]\s*${Regex.escape(parts[1])}\b""")
            s = dimRegex.replace(s, " ")
        }
    }
    s = LEADING_DIM_REGEX.replace(s, " ")
    val cleanUsine = CATEGORY_WORDS_REGEX.replace(usine.trim(), " ").trim()
    if (cleanUsine.isNotBlank() && s.trim().startsWith(cleanUsine, ignoreCase = true)) {
        val after = s.trim().substring(cleanUsine.length)
        if (after.isEmpty() || after.first().isWhitespace() || after.first() == '-' || after.first() == '/') {
            s = after.trimStart(' ', '-', '/')
        }
    }
    return WHITESPACE_REGEX.replace(s, " ").trim()
}

fun isSoldByBag(name: String): Boolean {
    val norm = normalizeSearchText(name)
    return norm.contains("ciment") || norm.contains("colle") || norm.contains("pate a joint") || norm.contains("joint")
}

fun splitFinitions(raw: String): List<String> {
    if (raw.isBlank()) return emptyList()
    val explicitParts = raw.split(Regex("\\s*[+,/|•_]\\s*"))
        .map { it.trim() }
        .filter { it.isNotBlank() }
    if (explicitParts.size > 1) return explicitParts.distinctBy { normalizeSearchText(it) }
    val trimmed = raw.trim()
    val norm = normalizeSearchText(trimmed)
    // Recognize space-separated combined known finitions like "polie rectifié" or "brillant rectifié"
    val multiKeywords = listOf(
        "extra polie" to "extra polie",
        "extra poli" to "extra polie",
        "semi mat" to "semi-mat",
        "semimat" to "semi-mat",
        "relief 3d" to "relief 3d",
        "relief3d" to "relief 3d",
        "rectifie" to "rectifié",
        "polie" to "polie",
        "poli" to "polie",
        "brillant" to "brillant",
        "mat" to "mat",
        "lappato" to "lappato",
        "sucre" to "sucré",
        "antiderapant" to "antidérapant"
    )
    var remaining = " $norm "
    val found = mutableListOf<String>()
    for ((needle, canonical) in multiKeywords) {
        val padded = " $needle "
        if (remaining.contains(padded)) {
            if (found.none { normalizeSearchText(it) == normalizeSearchText(canonical) }) {
                found.add(canonical)
            }
            remaining = remaining.replace(padded, " ")
        }
    }
    return if (found.size >= 2 && remaining.trim().isEmpty()) found else listOf(trimmed)
}

fun toggleFinitionSelection(current: String, option: String): String {
    val normOpt = normalizeSearchText(option)
    if (normOpt.isBlank()) return ""
    val parts = splitFinitions(current).toMutableList()
    val existingIdx = parts.indexOfFirst { normalizeSearchText(it) == normOpt }
    if (existingIdx >= 0) {
        parts.removeAt(existingIdx)
    } else {
        parts.add(option.trim())
    }
    return parts.joinToString(" + ")
}

private fun formatSingleFinitionLabel(finition: String): String {
    return when (normalizeSearchText(finition)) {
        "rectifie", "rect" -> "Rectifié"
        "polie", "poli" -> "Polie"
        "extra polie", "extra poli" -> "Extra Polie"
        "mat", "matt" -> "Mat"
        "semi mat", "semimat" -> "Semi-Mat"
        "relief 3d", "relief3d", "3d" -> "Relief 3D"
        "satine" -> "Satiné"
        "rustic", "rustique" -> "Rustic"
        "lappato" -> "Lappato"
        "sucre" -> "Sucré"
        "antiderapant" -> "Antidérapant"
        "", "brillant" -> ""
        else -> finition.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
        }
    }
}

fun formatFinitionLabel(finition: String): String {
    val parts = splitFinitions(finition)
    if (parts.isEmpty()) return ""
    if (parts.size == 1) return formatSingleFinitionLabel(parts.first())
    return parts.mapNotNull { part ->
        val lbl = formatSingleFinitionLabel(part)
        lbl.ifBlank { if (normalizeSearchText(part) == "brillant") "Brillant" else null }
    }.distinct().joinToString(" • ")
}

fun formatFinitionChipLabel(finition: String): String {
    return when (normalizeSearchText(finition)) {
        "brillant" -> "Brillant"
        "mat", "matt" -> "Mat"
        "semi mat", "semimat" -> "Semi-Mat"
        "satine" -> "Satiné"
        "rustic", "rustique" -> "Rustic"
        "relief 3d", "relief3d", "3d" -> "Relief 3D"
        "polie", "poli" -> "Polie"
        "rectifie", "rect" -> "Rectifié"
        "extra polie", "extra poli" -> "Extra Polie"
        "" -> ""
        else -> formatFinitionLabel(finition).ifBlank {
            finition.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
        }
    }
}

fun formatProductDesignation(product: TileProduct): String {
    return formatRawDesignation(
        name = product.name,
        usine = product.usine,
        dimension = product.dimension,
        finition = product.finition,
        choix = product.choix
    )
}

fun formatCartItemDesignation(item: CartItem, catalogProduct: TileProduct? = null): String {
    val isCatalogMatch = catalogProduct != null && !item.productId.startsWith("onetime_")
    val effectiveChoix = when {
        item.choix.isNotBlank() -> item.choix
        isCatalogMatch -> catalogProduct!!.choix
        else -> ""
    }
    val effectiveName = if (isCatalogMatch && item.name == catalogProduct!!.name) {
        catalogProduct.name
    } else {
        item.name
    }
    return formatRawDesignation(
        name = effectiveName,
        usine = if (isCatalogMatch) catalogProduct!!.usine else item.usine,
        dimension = if (isCatalogMatch) catalogProduct!!.dimension else item.dimension,
        finition = if (isCatalogMatch) catalogProduct!!.finition else "",
        choix = effectiveChoix
    )
}

fun formatRawDesignation(
    name: String,
    usine: String,
    dimension: String,
    finition: String = "",
    choix: String = ""
): String {
    val dimSlash = formatDimensionSlash(dimension)
    val rawCleanUsine = CATEGORY_WORDS_REGEX.replace(usine.trim(), " ")
        .let { WHITESPACE_REGEX.replace(it, " ").trim() }
    val normChoix = choix.trim()
    val isExplicitNonCc = normChoix.equals("1er Choix", ignoreCase = true) ||
        normChoix.equals("STD", ignoreCase = true)
    val isCcChoice = !isExplicitNonCc && (
        normChoix.equals("CC", ignoreCase = true) ||
            normChoix.contains("2", ignoreCase = true) ||
            EDGE_CC_TOKEN_REGEX.containsMatchIn(name)
        )

    var model = name.trim()
    model = CATEGORY_WORDS_REGEX.replace(model, " ")
    model = LEADING_SOL_MUR_REGEX.replace(model, " ")
    if (dimSlash.isNotBlank()) {
        val parts = dimSlash.split('/')
        if (parts.size == 2) {
            val dimRegex = Regex("""(?i)\b${Regex.escape(parts[0])}\s*[/×x*]\s*${Regex.escape(parts[1])}\b""")
            model = dimRegex.replace(model, " ")
        }
    } else {
        model = LEADING_DIM_REGEX.replace(model, " ")
    }
    if (isCcChoice || isExplicitNonCc) {
        model = EDGE_CC_TOKEN_REGEX.replace(model, " ").trim()
    }
    if (rawCleanUsine.isNotBlank() && model.trim().startsWith(rawCleanUsine, ignoreCase = true)) {
        val after = model.trim().substring(rawCleanUsine.length)
        if (after.isEmpty() || after.first().isWhitespace() || after.first() == '-' || after.first() == '/') {
            model = after.trimStart(' ', '-', '/')
        }
    }
    val finLabel = formatFinitionLabel(finition)
    if (finLabel.equals("Rectifié", ignoreCase = true)) {
        model = TRAILING_RECT_TOKEN_REGEX.replace(model, "").trim()
    }

    // Global deduplication across usine + model tokens (prevents "FCM MALEH FCM" or repeated factory/model words)
    val cleanUsineRaw = if (
        rawCleanUsine.isNotBlank() &&
        ANY_DIM_TOKEN_REGEX.matches(rawCleanUsine) &&
        formatDimensionSlash(rawCleanUsine).equals(dimSlash, ignoreCase = true)
    ) "" else rawCleanUsine

    val seenTokens = LinkedHashSet<String>()
    val cleanUsine = buildList {
        WHITESPACE_REGEX.replace(cleanUsineRaw, " ").trim()
            .split(' ')
            .filter { it.isNotBlank() }
            .forEach { tok ->
                val key = normalizeSearchText(tok)
                val isSameAsDim = dimSlash.isNotBlank() && formatDimensionSlash(tok).equals(dimSlash, ignoreCase = true)
                if (!isSameAsDim && key.isNotBlank() && seenTokens.add(key)) {
                    add(tok)
                }
            }
    }.joinToString(" ").trim()

    val tokens = WHITESPACE_REGEX.replace(model, " ").trim()
        .split(' ')
        .filter { it.isNotBlank() }
    val dedupedTokens = buildList<String> {
        tokens.forEach { tok ->
            val key = normalizeSearchText(tok)
            val normTok = formatDimensionSlash(tok)
            val isSameAsDim = dimSlash.isNotBlank() && normTok.equals(dimSlash, ignoreCase = true)
            if (!isSameAsDim && key.isNotBlank() && seenTokens.add(key)) {
                add(tok)
            }
        }
    }
    model = dedupedTokens.joinToString(" ").trim()

    val displayDim = when {
        dimSlash.isBlank() -> ""
        dimSlash.equals("Pièce", ignoreCase = true) || dimSlash.equals("Piece", ignoreCase = true) -> ""
        model.startsWith(dimSlash, ignoreCase = true) -> ""
        else -> dimSlash
    }

    val modelPart = if (model.equals("VIERGE", ignoreCase = true) || model.equals("JETON", ignoreCase = true)) {
        ""
    } else {
        model.ifBlank {
            if (cleanUsine.isNotBlank() || displayDim.isNotBlank()) "" else name.trim()
        }
    }

    val base = listOf(displayDim, cleanUsine, modelPart)
        .filter { it.isNotBlank() }
        .joinToString(" ")

    val suffixes = buildList {
        if (finLabel.isNotBlank() && !base.contains(finLabel, ignoreCase = true)) add(finLabel)
        if (isCcChoice) {
            add("CC")
        } else if (normChoix.isNotBlank() && !isExplicitNonCc) {
            add(normChoix)
        }
    }
    return if (suffixes.isEmpty()) base else "$base • ${suffixes.joinToString(" • ")}"
}

fun normalizeSearchText(text: String): String {
    if (text.isBlank()) return ""
    var hasNonAscii = false
    for (i in 0 until text.length) {
        if (text[i].code >= 128) {
            hasNonAscii = true
            break
        }
    }
    val base = if (hasNonAscii) {
        val decomposed = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
        COMBINING_DIACRITICS_REGEX.replace(decomposed, "").lowercase(Locale.ROOT)
    } else {
        text.lowercase(Locale.ROOT)
    }
    val withDimSpace = DIM_X_REGEX.replace(base, " ")
    val cleaned = SEARCH_SEPARATORS_REGEX.replace(withDimSpace, " ")
    return WHITESPACE_REGEX.replace(cleaned, " ").trim()
}

fun buildProductSearchBlob(product: TileProduct): String {
    val jetonHint = if (isJetonProduct(product)) "vierge jeton libre" else ""
    val dimSlash = formatDimensionSlash(product.dimension)
    val dimParts = dimSlash.split('/')
    val dimReverse = if (dimParts.size == 2) "${dimParts[1]}/${dimParts[0]} ${dimParts[1]} ${dimParts[0]}" else ""
    val dimCompact = if (dimParts.size == 2) "${dimParts[0]}${dimParts[1]} ${dimParts[1]}${dimParts[0]}" else ""
    val raw = "${formatProductDesignation(product)} ${product.name} ${product.usine} ${product.dimension} $dimSlash $dimReverse $dimCompact ${product.finition} ${product.choix} ${product.colorName} ${product.categorie} $jetonHint"
    val spaced = normalizeSearchText(raw)
    val joined = normalizeSearchText(raw.replace("'", "").replace("’", "").replace("-", ""))
    val base = if (joined == spaced) spaced else "$spaced $joined"
    return if (dimSlash.isNotBlank()) "$base ${dimSlash.lowercase(Locale.ROOT)}" else base
}

fun matchesProductSearch(
    product: TileProduct,
    normalizedQueryTokens: List<String>,
    precomputedBlob: String? = null
): Boolean {
    if (normalizedQueryTokens.isEmpty()) return true
    val blob = precomputedBlob ?: buildProductSearchBlob(product)
    return normalizedQueryTokens.all { token -> blob.contains(token) }
}

data class IndexedTileProduct(
    val product: TileProduct,
    val designation: String,
    val searchBlob: String,
    val normDim: String,
    val normUsine: String,
    val normFinition: String,
    val normColorName: String,
    val finishBadge: String,
    val is8x45: Boolean,
    val isJeton: Boolean
)

data class CatalogIndex(
    val entries: List<IndexedTileProduct> = emptyList(),
    val searchPairs: List<Pair<TileProduct, String>> = emptyList(),
    val byId: Map<String, TileProduct> = emptyMap(),
    val designationById: Map<String, String> = emptyMap(),
    val dimensions: List<String> = listOf("Toutes") + POS_DIMENSIONS_ORDER,
    val tileDimensions: List<String> = listOf("Toutes") + POS_TILE_DIMENSIONS_ORDER,
    val usines: List<String> = listOf("Toutes"),
    val finitions: List<String> = listOf("Toutes") + POS_FINITION_OPTIONS,
    val vocabulary: Set<String> = emptySet()
) {
    fun filter(
        queryTokens: List<String>,
        selectedDimension: String,
        selectedUsine: String,
        selectedFinition: String,
        selectedColor: String,
        selectedAccessory: String = "Tous"
    ): List<TileProduct> {
        val checkDim = selectedDimension != "Toutes"
        val checkUsine = selectedUsine != "Toutes"
        val checkFinition = selectedFinition != "Toutes" && selectedFinition.isNotBlank()
        val checkColor = selectedColor != "Toutes" && selectedColor.isNotBlank()
        val checkAccessory = selectedAccessory != "Tous" && selectedAccessory != "Toutes" && selectedAccessory.isNotBlank()
        if (queryTokens.isEmpty() && !checkDim && !checkUsine && !checkFinition && !checkColor && !checkAccessory) {
            return entries.map { it.product }
        }
        val normDim = if (checkDim) normalizeDimensionKey(selectedDimension) else ""
        val dimRegex = if (checkDim) compileDimensionMatchRegex(normDim) else null
        val normUsine = if (checkUsine) canonicalFactoryAlias(selectedUsine) else ""
        val jetonMatches = ArrayList<TileProduct>(8)
        val regularMatches = ArrayList<TileProduct>(entries.size.coerceAtMost(64))
        for (entry in entries) {
            if (queryTokens.isNotEmpty() && !queryTokens.all { entry.searchBlob.contains(it) }) continue
            if (checkDim && !matchesDimensionFilter(entry, normDim, dimRegex)) continue
            if (checkUsine && entry.normUsine != normUsine) continue
            if (checkFinition && !matchesFinitionFilter(entry.product, selectedFinition)) continue
            if (checkColor && !matchesColorFilter(entry.product, selectedColor)) continue
            if (checkAccessory && !matchesAccessoryFilter(entry.product, selectedAccessory)) continue
            if (entry.isJeton) {
                jetonMatches.add(entry.product)
            } else {
                regularMatches.add(entry.product)
            }
        }
        return if (jetonMatches.isEmpty()) regularMatches else jetonMatches + regularMatches
    }

    companion object {
        val EMPTY = CatalogIndex()
    }
}

fun buildCatalogIndex(products: List<TileProduct>): CatalogIndex {
    if (products.isEmpty()) return CatalogIndex.EMPTY
    val entries = ArrayList<IndexedTileProduct>(products.size)
    val searchPairs = ArrayList<Pair<TileProduct, String>>(products.size)
    val byId = HashMap<String, TileProduct>(products.size)
    val designationById = HashMap<String, String>(products.size)
    val vocab = LinkedHashSet<String>(products.size * 4)

    val orderedDimKeys = POS_DIMENSIONS_ORDER.map { normalizeDimensionKey(it) }.toSet()
    val extraDimsMap = LinkedHashMap<String, String>()
    val usinesMap = LinkedHashMap<String, String>()
    val standardFinKeys = POS_FINITION_OPTIONS.map { normalizeSearchText(it) }.toSet()
    val extraFinMap = LinkedHashMap<String, String>()

    for (p in products) {
        val designation = formatProductDesignation(p)
        val dimSlash = formatDimensionSlash(p.dimension)
        val normDim = normalizeDimensionKey(p.dimension)
        val normUsine = canonicalFactoryAlias(p.usine)
        val normFin = normalizeSearchText(p.finition)
        val normColor = normalizeSearchText(p.colorName)
        val isJeton = isJetonProduct(p)
        val is8x45 = normDim == "8/45"
        val finishBadge = formatFinitionLabel(p.finition)

        val jetonHint = if (isJeton) "vierge jeton libre" else ""
        val factoryAliasHint = when (normUsine) {
            "fcm" -> "fcm fc maleh fcm maleh"
            "ncm" -> "ncm mcm"
            "tecno" -> "tecno techno ceram"
            "porcelaine" -> "porcelaine viva"
            else -> ""
        }
        val dimParts = dimSlash.split('/')
        val dimReverse = if (dimParts.size == 2) "${dimParts[1]}/${dimParts[0]} ${dimParts[1]} ${dimParts[0]}" else ""
        val dimCompact = if (dimParts.size == 2) "${dimParts[0]}${dimParts[1]} ${dimParts[1]}${dimParts[0]}" else ""
        val raw = "$designation ${p.name} ${p.usine} $factoryAliasHint ${p.dimension} $dimSlash $dimReverse $dimCompact ${p.finition} ${p.choix} ${p.colorName} ${p.categorie} $jetonHint"
        val spaced = normalizeSearchText(raw)
        val joined = if (raw.indexOf('\'') >= 0 || raw.indexOf('’') >= 0 || raw.indexOf('-') >= 0) {
            normalizeSearchText(raw.replace("'", "").replace("’", "").replace("-", ""))
        } else spaced
        val baseBlob = if (joined == spaced) spaced else "$spaced $joined"
        val blob = if (dimSlash.isNotBlank()) "$baseBlob ${dimSlash.lowercase(Locale.ROOT)}" else baseBlob

        val entry = IndexedTileProduct(
            product = p,
            designation = designation,
            searchBlob = blob,
            normDim = normDim,
            normUsine = normUsine,
            normFinition = normFin,
            normColorName = normColor,
            finishBadge = finishBadge,
            is8x45 = is8x45,
            isJeton = isJeton
        )
        entries.add(entry)
        searchPairs.add(p to blob)
        byId[p.id] = p
        designationById[p.id] = designation

        spaced.split(' ').forEach { tok -> if (tok.length >= 2) vocab.add(tok) }

        if (dimSlash.isNotBlank() &&
            !dimSlash.equals("Pièce", ignoreCase = true) &&
            !dimSlash.equals("Piece", ignoreCase = true) &&
            normDim !in orderedDimKeys
        ) {
            extraDimsMap.putIfAbsent(normDim, dimSlash)
        }
        val displayUsine = canonicalFactoryDisplayLabel(p.usine)
        if (displayUsine.isNotBlank() && normUsine.isNotBlank()) {
            usinesMap.putIfAbsent(normUsine, displayUsine)
        }
        splitFinitions(p.finition).forEach { part ->
            val rawNorm = normalizeSearchText(part)
            val canonicalNorm = when (rawNorm) {
                "poli" -> "polie"
                "extra poli" -> "extra polie"
                "rect" -> "rectifie"
                "matt" -> "mat"
                "semimat" -> "semi mat"
                "relief3d" -> "relief 3d"
                else -> rawNorm
            }
            if (canonicalNorm.isNotBlank() && canonicalNorm !in standardFinKeys) {
                extraFinMap.putIfAbsent(canonicalNorm, part.trim())
            }
        }
    }

    POS_COLOR_OPTIONS.forEach { opt ->
        normalizeSearchText(opt.label).split(' ').forEach { if (it.length >= 2) vocab.add(it) }
    }
    POS_FINITION_OPTIONS.forEach { fin ->
        normalizeSearchText(fin).split(' ').forEach { if (it.length >= 2) vocab.add(it) }
    }

    val extraTileDims = extraDimsMap.entries
        .filter { it.key !in POS_ACCESSORY_DIM_KEYS }
        .sortedBy { it.key }
        .map { it.value }

    return CatalogIndex(
        entries = entries,
        searchPairs = searchPairs,
        byId = byId,
        designationById = designationById,
        dimensions = listOf("Toutes") + POS_DIMENSIONS_ORDER + extraDimsMap.entries.sortedBy { it.key }.map { it.value },
        tileDimensions = listOf("Toutes") + POS_TILE_DIMENSIONS_ORDER + extraTileDims,
        usines = listOf("Toutes") + usinesMap.entries.sortedBy { it.key }.map { it.value },
        finitions = listOf("Toutes") + extraFinMap.entries.sortedBy { it.key }.map { it.value } + POS_FINITION_OPTIONS,
        vocabulary = vocab
    )
}

data class SearchCorrectionSuggestion(
    val suggestedQuery: String,
    val correctedTokens: Map<String, String>,
    val matchCount: Int
)

fun levenshteinDistance(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length
    val prev = IntArray(b.length + 1) { it }
    val curr = IntArray(b.length + 1)
    for (i in 1..a.length) {
        curr[0] = i
        val ca = a[i - 1]
        for (j in 1..b.length) {
            val cost = if (ca == b[j - 1]) 0 else 1
            curr[j] = minOf(
                prev[j] + 1,
                curr[j - 1] + 1,
                prev[j - 1] + cost
            )
        }
        System.arraycopy(curr, 0, prev, 0, prev.size)
    }
    return prev[b.length]
}

fun buildCatalogSearchVocabulary(products: List<TileProduct>): Set<String> {
    val vocab = LinkedHashSet<String>()
    products.forEach { p ->
        val raw = "${formatProductDesignation(p)} ${p.name} ${p.usine} ${p.dimension} ${p.finition} ${p.colorName} ${p.choix}"
        normalizeSearchText(raw).split(' ').forEach { tok ->
            if (tok.length >= 2) vocab.add(tok)
        }
    }
    POS_COLOR_OPTIONS.forEach { opt ->
        normalizeSearchText(opt.label).split(' ').forEach { if (it.length >= 2) vocab.add(it) }
    }
    POS_FINITION_OPTIONS.forEach { fin ->
        normalizeSearchText(fin).split(' ').forEach { if (it.length >= 2) vocab.add(it) }
    }
    return vocab
}

fun suggestSearchCorrections(
    rawQuery: String,
    products: List<TileProduct>,
    searchIndex: List<Pair<TileProduct, String>>? = null,
    vocabulary: Set<String>? = null,
    maxSuggestions: Int = 3
): List<SearchCorrectionSuggestion> {
    val tokens = normalizeSearchText(rawQuery).split(' ').filter { it.isNotBlank() }
    if (tokens.isEmpty() || products.isEmpty()) return emptyList()
    val index = searchIndex ?: products.map { it to buildProductSearchBlob(it) }
    val directMatches = index.count { (p, blob) -> matchesProductSearch(p, tokens, blob) }
    if (directMatches > 0) return emptyList()

    val vocab = vocabulary ?: buildCatalogSearchVocabulary(products)
    val tokenCandidates: List<List<Pair<String, Int>>> = tokens.map { tok ->
        val hasAnyDirect = index.any { (_, blob) -> blob.contains(tok) }
        if (hasAnyDirect) {
            listOf(tok to 0)
        } else {
            val maxDist = when {
                tok.length <= 2 -> 1
                tok.length <= 4 -> 1
                tok.length <= 8 -> 2
                else -> 3
            }
            val scored = vocab.mapNotNull { candidate ->
                if (candidate == tok) return@mapNotNull null
                if (kotlin.math.abs(candidate.length - tok.length) > maxDist + 1) return@mapNotNull null
                val dist = levenshteinDistance(tok, candidate)
                val prefixPenalty = if (candidate.startsWith(tok.take(2)) || tok.startsWith(candidate.take(2))) 0 else 1
                if (dist <= maxDist) Triple(candidate, dist, prefixPenalty) else null
            }.sortedWith(compareBy<Triple<String, Int, Int>>({ it.second }, { it.third }, { it.first.length }))
                .take(4)
                .map { it.first to it.second }
            if (scored.isEmpty()) listOf(tok to 0) else scored
        }
    }

    // Generate candidate full-query combinations (bounded for instant performance)
    var combos: List<Pair<List<String>, Int>> = listOf(emptyList<String>() to 0)
    for (cands in tokenCandidates) {
        val next = ArrayList<Pair<List<String>, Int>>()
        for ((prefix, cost) in combos) {
            for ((candWord, candCost) in cands) {
                next.add((prefix + candWord) to (cost + candCost))
            }
        }
        combos = next.sortedBy { it.second }.take(24)
    }

    val results = combos.mapNotNull { (candidateTokens, totalCost) ->
        if (candidateTokens == tokens) return@mapNotNull null
        val matchCount = index.count { (p, blob) -> matchesProductSearch(p, candidateTokens, blob) }
        if (matchCount <= 0) return@mapNotNull null
        val corrections = tokens.zip(candidateTokens)
            .filter { (orig, corr) -> orig != corr }
            .toMap()
        if (corrections.isEmpty()) return@mapNotNull null
        Triple(
            SearchCorrectionSuggestion(
                suggestedQuery = candidateTokens.joinToString(" "),
                correctedTokens = corrections,
                matchCount = matchCount
            ),
            totalCost,
            matchCount
        )
    }.distinctBy { it.first.suggestedQuery }
        .sortedWith(compareBy<Triple<SearchCorrectionSuggestion, Int, Int>>({ it.second }, { -it.third }))
        .take(maxSuggestions)
        .map { it.first }

    return results
}

data class ColorFilterOption(
    val label: String,
    val hex: String
)

val POS_ACCESSORY_DIM_KEYS = setOf(
    "8/33", "33/8", "8/45", "45/8", "8/60", "60/8", "9/45", "45/9",
    "10/60", "60/10", "10/30", "30/10", "7/60", "60/7",
    "2/30", "30/2", "2/60", "60/2", "3/60", "60/3", "4/60", "60/4"
)

val POS_DIMENSIONS_ORDER = listOf(
    "120/60", "60/60", "45/45", "45/90", "30/60", "30/65",
    "33/33", "30/30", "25/60", "25/70", "80/80", "90/90",
    "15/75", "20/75", "25/75", "30/90", "15/60", "8/33",
    "8/45", "8/60", "9/45", "10/60", "10/30", "7/60"
)

val POS_TILE_DIMENSIONS_ORDER = POS_DIMENSIONS_ORDER.filter {
    normalizeDimensionKey(it) !in POS_ACCESSORY_DIM_KEYS
}

val POS_ACCESSORY_FILTER_OPTIONS = listOf(
    "Tous",
    "Motif",
    "Plinthe",
    "8/45",
    "8/60",
    "8/33",
    "9/45",
    "10/60",
    "7/60",
    "10/30",
    "Courdon",
    "Baguette",
    "Ciment Colle",
    "Pâte à Joint"
)

fun matchesAccessoryFilter(product: TileProduct, selectedAccessory: String): Boolean {
    if (selectedAccessory == "Tous" || selectedAccessory == "Toutes" || selectedAccessory.isBlank()) return true
    val normSel = normalizeSearchText(selectedAccessory)
    val normDim = normalizeDimensionKey(product.dimension)
    val blob = normalizeSearchText("${product.name} ${product.categorie} ${product.dimension} ${product.description}")
    return when {
        selectedAccessory.contains('/') -> normDim == normalizeDimensionKey(selectedAccessory)
        normSel == "motif" ->
            blob.contains("motif") ||
                blob.contains("decor") ||
                blob.contains("mozaik") ||
                blob.contains("mosaic") ||
                blob.contains("frise") ||
                blob.contains("frize") ||
                blob.contains("friza") ||
                blob.contains("listel") ||
                formatProductDesignation(product).contains("motif", ignoreCase = true) ||
                product.categorie.equals("motif", ignoreCase = true)
        normSel == "plinthe" ->
            blob.contains("plinthe") || blob.contains("pleinth") ||
                normDim in setOf("8/45", "8/60", "8/33", "9/45", "7/60")
        normSel == "courdon" ->
            blob.contains("courdon") || blob.contains("cordon") || isCordonDimension(product.dimension)
        normSel == "baguette" -> blob.contains("baguette")
        normSel == "ciment colle" -> blob.contains("ciment") || blob.contains("colle")
        normSel == "pate a joint" -> blob.contains("joint")
        else -> blob.contains(normSel)
    }
}

val POS_FINITION_OPTIONS = listOf(
    "brillant",
    "mat",
    "semi-mat",
    "relief 3d",
    "polie",
    "rectifié",
    "extra polie"
)

val POS_COLOR_OPTIONS = listOf(
    ColorFilterOption("Beige", "#E2D4C0"),
    ColorFilterOption("Beige Clair", "#F3ECE1"),
    ColorFilterOption("Beige Foncé", "#C4AD8E"),
    ColorFilterOption("Gris", "#94A3B8"),
    ColorFilterOption("Gris Clair", "#CBD5E1"),
    ColorFilterOption("Gris Foncé", "#475569"),
    ColorFilterOption("Blanc", "#FFFFFF"),
    ColorFilterOption("Noir", "#1E293B"),
    ColorFilterOption("Rose", "#F472B6"),
    ColorFilterOption("Bleu", "#3B82F6"),
    ColorFilterOption("Mauve", "#A855F7"),
    ColorFilterOption("Vert", "#22C55E"),
    ColorFilterOption("Blanc Marbré", "#F1F5F9")
)

fun compileDimensionMatchRegex(normDim: String): Regex? {
    val parts = normDim.split('/')
    if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) return null
    val a = Regex.escape(parts[0])
    val b = Regex.escape(parts[1])
    return Regex("(?<!\\d)(?:$a\\s*[/xX×*]\\s*$b|$b\\s*[/xX×*]\\s*$a)(?!\\d)")
}

fun matchesDimensionFilter(
    entry: IndexedTileProduct,
    normDim: String,
    compiledRegex: Regex? = compileDimensionMatchRegex(normDim)
): Boolean {
    if (normDim.isBlank() || normDim == "toutes") return true
    val parts = normDim.split('/')
    val revDim = if (parts.size == 2) "${parts[1]}/${parts[0]}" else ""
    if (entry.normDim == normDim || (revDim.isNotBlank() && entry.normDim == revDim)) return true
    if (compiledRegex != null) {
        val rawText = "${entry.designation} ${entry.product.name} ${entry.product.dimension} ${entry.product.description}"
        if (compiledRegex.containsMatchIn(rawText)) return true
    }
    return false
}

fun matchesProductDimension(product: TileProduct, selectedDimension: String): Boolean {
    val normDim = normalizeDimensionKey(selectedDimension)
    if (normDim.isBlank() || normDim == "toutes") return true
    val prodNormDim = normalizeDimensionKey(product.dimension)
    val parts = normDim.split('/')
    val revDim = if (parts.size == 2) "${parts[1]}/${parts[0]}" else ""
    if (prodNormDim == normDim || (revDim.isNotBlank() && prodNormDim == revDim)) return true
    val regex = compileDimensionMatchRegex(normDim) ?: return false
    val rawText = "${formatProductDesignation(product)} ${product.name} ${product.dimension} ${product.description}"
    return regex.containsMatchIn(rawText)
}

private fun matchesSingleFinitionFilter(product: TileProduct, singleFinition: String): Boolean {
    val normSel = normalizeSearchText(singleFinition)
    if (normSel.isBlank() || normSel == "toutes") return true
    val normParts = splitFinitions(product.finition).map { normalizeSearchText(it) }
    if (normSel in normParts) return true
    val normProdFin = normalizeSearchText(product.finition)
    if (normProdFin == normSel) return true
    val blob = normalizeSearchText("${product.finition} ${product.name}")
    val words = blob.split(' ').filter { it.isNotBlank() }.toSet()
    return when (normSel) {
        "brillant" -> "brillant" in normParts || "brillant" in words
        "mat" -> "mat" in normParts || "matt" in normParts || ("mat" in words && "semi" !in words)
        "semi mat" -> "semi mat" in normParts || "semimat" in normParts ||
            ("semi" in words && "mat" in words) || "semimat" in words
        "relief 3d" -> "relief 3d" in normParts || "relief3d" in normParts || "relief" in words || "3d" in words
        "polie" -> ("polie" in normParts || "poli" in normParts || "polie" in words || "poli" in words) &&
            "extra polie" !in normParts && "extra" !in words
        "rectifie" -> "rectifie" in normParts || "rect" in normParts || "rectifie" in words || "rect" in words
        "extra polie" -> "extra polie" in normParts || "extra poli" in normParts ||
            ("extra" in words && ("polie" in words || "poli" in words))
        else -> {
            val tokens = normSel.split(' ').filter { it.isNotBlank() }
            tokens.isNotEmpty() && tokens.all { it in words }
        }
    }
}

fun matchesFinitionFilter(product: TileProduct, selectedFinition: String): Boolean {
    if (selectedFinition == "Toutes" || selectedFinition.isBlank()) return true
    val selectedParts = splitFinitions(selectedFinition)
        .filter { it.isNotBlank() && !it.equals("Toutes", ignoreCase = true) }
    if (selectedParts.isEmpty()) return true
    return selectedParts.all { part -> matchesSingleFinitionFilter(product, part) }
}

fun matchesFinitionFilters(product: TileProduct, selectedFinitions: Collection<String>): Boolean {
    val active = selectedFinitions.filter { it.isNotBlank() && !it.equals("Toutes", ignoreCase = true) }
    if (active.isEmpty()) return true
    return active.all { part -> matchesSingleFinitionFilter(product, part) }
}

fun matchesColorFilter(product: TileProduct, selectedColor: String): Boolean {
    if (selectedColor == "Toutes" || selectedColor.isBlank()) return true
    val normSel = normalizeSearchText(selectedColor)
    val normColorName = normalizeSearchText(product.colorName)
    if (normColorName == normSel) return true

    val blob = normalizeSearchText("${product.colorName} ${product.name}")
    val words = blob.split(' ').filter { it.isNotBlank() }.toSet()

    return when (normSel) {
        "beige" -> normColorName.startsWith("beige") || "beige" in words || "creme" in words || "crema" in words
        "beige clair" -> "beige" in words && ("clair" in words || "claire" in words)
        "beige fonce" -> "beige" in words && ("fonce" in words || "foncee" in words)
        "gris" -> normColorName.startsWith("gris") || "gris" in words || "grey" in words || "gray" in words
        "gris clair" -> "gris" in words && ("clair" in words || "claire" in words)
        "gris fonce" -> "gris" in words && ("fonce" in words || "foncee" in words)
        "blanc" -> normColorName.startsWith("blanc") || "blanc" in words || "blanche" in words || "white" in words
        "blanc marbre" -> ("blanc" in words || "white" in words) &&
            ("marbre" in words || "statuario" in words || "calacatta" in words || "carrara" in words)
        "noir" -> normColorName.startsWith("noir") || "noir" in words || "noire" in words || "black" in words
        "rose" -> normColorName.startsWith("rose") || "rose" in words || "pink" in words
        "bleu" -> normColorName.startsWith("bleu") || "bleu" in words || "bleue" in words || "blue" in words
        "mauve" -> normColorName.startsWith("mauve") || "mauve" in words || "move" in words || "violet" in words
        "vert" -> normColorName.startsWith("vert") || "vert" in words || "verte" in words || "green" in words
        else -> normSel.split(' ').all { it in words }
    }
}

private val KNOWN_FACTORY_PREFIXES = setOf(
    "BCRAM", "NOVA", "CEVICO", "NCM", "BATI", "CBM", "CMC", "BORDJ",
    "CONFORT", "CORSO", "DJEMILLA", "DJMILA", "ELVA", "FC", "FCM",
    "ROYAL", "ALMAS", "CERAM", "DIVERS", "OPERA", "MCM"
)

fun extractMatchingPlintheName(rawName: String, dimension: String = "", usine: String = ""): String {
    var s = normalizeModelNameForStorage(rawName, dimension, usine)
    s = EDGE_CC_TOKEN_REGEX.replace(s, " ")
    var tokens = WHITESPACE_REGEX.replace(s, " ").trim().split(' ').filter { it.isNotBlank() }
    while (tokens.size > 1 && tokens.first().uppercase(Locale.ROOT) in KNOWN_FACTORY_PREFIXES) {
        if (tokens.size >= 3 && (tokens[0].equals("FCM", true) || tokens[0].equals("FC", true)) && tokens[1].equals("MALEH", true)) {
            tokens = tokens.drop(2)
        } else {
            tokens = tokens.drop(1)
        }
    }
    return tokens.joinToString(" ").trim().ifBlank { s.ifBlank { rawName.trim() } }
}

fun isMotifEligibleDimension(dimension: String): Boolean {
    val key = normalizeDimensionKey(dimension)
    return key == "30/60" || key == "25/60" || key == "25/70"
}

fun isTile30x65(dimension: String): Boolean = normalizeDimensionKey(dimension) == "30/65"

private val MOTIF_SKIP_TOKENS = setOf("motif", "motifs", "mosaic", "mozaik", "mosaik", "decor", "listel")
private val TONE_STRIP_TOKENS = setOf("clair", "claire", "fonce", "foncee")

fun extractMotifBaseKey(rawName: String, dimension: String = "", usine: String = ""): String {
    var s = normalizeModelNameForStorage(rawName, dimension, usine)
    s = EDGE_CC_TOKEN_REGEX.replace(s, " ")
    var tokens = WHITESPACE_REGEX.replace(s, " ").trim()
        .split(' ')
        .filter { it.isNotBlank() }
    while (tokens.isNotEmpty() && normalizeSearchText(tokens.first()) in setOf("motif", "motifs")) {
        tokens = tokens.drop(1)
    }
    while (tokens.size > 1 && tokens.first().uppercase(Locale.ROOT) in KNOWN_FACTORY_PREFIXES) {
        if (tokens.size >= 3 && (tokens[0].equals("FCM", true) || tokens[0].equals("FC", true)) && tokens[1].equals("MALEH", true)) {
            tokens = tokens.drop(2)
        } else {
            tokens = tokens.drop(1)
        }
    }
    val cleaned = tokens.filter { tok ->
        val normTok = normalizeSearchText(tok)
        normTok.isNotBlank() &&
            normTok !in TONE_STRIP_TOKENS &&
            normTok !in setOf("cuisine", "douche", "mozaik", "mosaic", "mosaik")
    }
    return cleaned.joinToString(" ").trim()
        .replace(Regex("""^\s*[-/]+\s*|\s*[-/]+\s*$"""), "")
        .trim()
}

private fun formatMotifTitleWord(token: String): String {
    if (token.all { it.isDigit() } || token.contains('/')) return token
    return token.lowercase(Locale.ROOT).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
    }
}

fun buildMotifProductsForProduct(
    source: TileProduct,
    existingProducts: List<TileProduct>
): List<TileProduct> {
    if (!isMotifEligibleDimension(source.dimension)) return emptyList()
    val normChoice = normalizeSearchText(source.choix)
    if (normChoice == "cc" || normChoice.contains("2eme")) return emptyList()

    val normName = normalizeSearchText(source.name)
    val nameWords = normName.split(' ').toSet()
    if (nameWords.any { it in MOTIF_SKIP_TOKENS } || EDGE_CC_TOKEN_REGEX.containsMatchIn(source.name)) {
        return emptyList()
    }

    val dimKey = normalizeDimensionKey(source.dimension)
    val dimDisplay = formatDimensionSlash(source.dimension)
    val rawBase = extractMotifBaseKey(source.name, source.dimension, source.usine)
    if (rawBase.isBlank()) return emptyList()
    val normBaseKey = normalizeSearchText(rawBase)

    val prettyBase = rawBase.split(' ')
        .filter { it.isNotBlank() && it != "/" && it != "-" }
        .joinToString(" ") { formatMotifTitleWord(it) }

    fun hasExistingMotif(kind: String): Boolean {
        val kindAliases = if (kind.equals("mozaik", true)) setOf("mozaik", "mosaic", "mosaik") else setOf(kind.lowercase(Locale.ROOT))
        return existingProducts.any { existing ->
            normalizeDimensionKey(existing.dimension) == dimKey &&
                normalizeSearchText(existing.name).contains("motif") &&
                extractMotifBaseKey(existing.name, existing.dimension, existing.usine)
                    .let { normalizeSearchText(it) } == normBaseKey &&
                normalizeSearchText(existing.name).split(' ').any { it in kindAliases }
        }
    }

    val safeSlugBase = normBaseKey.replace(' ', '_').replace(Regex("""[^a-z0-9_]"""), "")
    val safeDimSlug = dimKey.replace('/', '_')
    return listOf("Cuisine", "Douche", "Mozaik").mapNotNull { kind ->
        if (hasExistingMotif(kind)) null
        else {
            val kindSlug = kind.lowercase(Locale.ROOT)
            val motifName = "Motif $dimDisplay $prettyBase $kind"
            val semanticColor = when (kindSlug) {
                "mozaik" -> "#7C3AED"
                "douche" -> "#0284C7"
                else -> "#D97706"
            }
            TileProduct(
                id = "p_motif_${safeDimSlug}_${safeSlugBase}_$kindSlug",
                name = motifName,
                usine = "",
                dimension = dimDisplay,
                m2Carton = 1.0,
                pcs = source.pcs.coerceAtLeast(1),
                prix = 450.0,
                prixAchat = 200.0,
                stock = 0,
                color = semanticColor,
                colorName = source.colorName,
                image = source.image,
                finition = source.finition.ifBlank { "brillant" },
                choix = "1er Choix",
                categorie = "faience",
                description = "$motifName — Décor assorti $dimDisplay",
                pricingUnit = "piece",
                prixPiece = 450.0
            )
        }
    }
}

fun extractMatchingFriseName(rawName: String, dimension: String = "", usine: String = ""): String {
    var model = extractMatchingPlintheName(rawName, dimension, usine)
    model = WHITESPACE_REGEX.replace(model, " ").trim()
    val norm = normalizeSearchText(model)
    if (norm.startsWith("friza ")) {
        model = model.substring(6).trim()
    } else if (norm.startsWith("frise ")) {
        model = model.substring(6).trim()
    }
    return model.ifBlank { rawName.trim() }
}

fun buildFriseFor120x60(
    source: TileProduct,
    existingProducts: List<TileProduct>,
    frisePrice: Double = 350.0
): TileProduct? {
    if (!isTile120x60(source.dimension)) return null
    val normChoice = normalizeSearchText(source.choix)
    if (normChoice == "cc" || normChoice.contains("2eme")) return null
    val modelName = extractMatchingFriseName(source.name, source.dimension, source.usine)
    if (modelName.isBlank() || isJetonProduct(source)) return null
    val friseName = "FRIZA $modelName"
    val normTargetKey = normalizeSearchText(friseName)
    val alreadyExists = existingProducts.any { existing ->
        isFrise10x60(existing.dimension) &&
            normalizeSearchText("FRIZA ${extractMatchingFriseName(existing.name, existing.dimension, existing.usine)}") == normTargetKey
    }
    if (alreadyExists) return null
    val safeSlug = normalizeSearchText(modelName).replace(' ', '_').replace(Regex("""[^a-z0-9_]"""), "")
    return TileProduct(
        id = "p_frise_10_60_$safeSlug",
        name = friseName,
        usine = "",
        dimension = "10/60",
        m2Carton = 1.0,
        pcs = 1,
        prix = frisePrice,
        prixAchat = 0.0,
        stock = 0,
        color = source.color,
        colorName = source.colorName,
        image = source.image,
        finition = source.finition.ifBlank { "brillant" },
        choix = "1er Choix",
        categorie = "frise",
        description = "10/60 $friseName — Frise assortie pour ${formatProductDesignation(source)}",
        pricingUnit = "piece",
        prixPiece = frisePrice
    )
}

enum class TileVisualKind {
    TILE,
    PLINTHE,
    FRISE,
    CORDON,
    BAGUETTE,
    TACO,
    SACK,
    MOTIF_CUISINE,
    MOTIF_DOUCHE,
    MOTIF_MOZAIK,
    MOTIF_SALLE_BAIN
}

data class TileVisualGeometry(
    val widthFraction: Float,
    val heightFraction: Float,
    val kind: TileVisualKind
)

fun resolveTileVisualGeometry(
    dimension: String,
    categorie: String = "",
    productName: String = ""
): TileVisualGeometry {
    val normName = normalizeSearchText(productName)
    val normCat = categorie.trim().lowercase(Locale.ROOT)
    val dimKey = normalizeDimensionKey(dimension)

    val motifKind = when {
        "mozaik" in normName || "mosaic" in normName || "mosaik" in normName -> TileVisualKind.MOTIF_MOZAIK
        "douche" in normName -> TileVisualKind.MOTIF_DOUCHE
        "salle de bain" in normName || "sdb" in normName -> TileVisualKind.MOTIF_SALLE_BAIN
        "motif" in normName || "cuisine" in normName || "a caro" in normName -> TileVisualKind.MOTIF_CUISINE
        else -> null
    }
    if (motifKind != null) {
        return when (dimKey) {
            "45/90" -> TileVisualGeometry(0.86f, 0.44f, motifKind)
            "25/70", "25/75" -> TileVisualGeometry(0.78f, 0.34f, motifKind)
            "25/60" -> TileVisualGeometry(0.74f, 0.36f, motifKind)
            else -> TileVisualGeometry(0.74f, 0.40f, motifKind)
        }
    }

    if ("baguette" in normName) return TileVisualGeometry(0.90f, 0.16f, TileVisualKind.BAGUETTE)
    if ("taco" in normName) return TileVisualGeometry(0.38f, 0.38f, TileVisualKind.TACO)
    if ("ciment" in normName || "colle" in normName || "pate a joint" in normName) {
        return TileVisualGeometry(0.54f, 0.72f, TileVisualKind.SACK)
    }

    return when (dimKey) {
        "2/30", "30/2" -> TileVisualGeometry(0.46f, 0.12f, TileVisualKind.CORDON)
        "2/60", "60/2" -> TileVisualGeometry(0.86f, 0.12f, TileVisualKind.CORDON)
        "3/60", "60/3" -> TileVisualGeometry(0.86f, 0.18f, TileVisualKind.CORDON)
        "4/60", "60/4" -> TileVisualGeometry(0.86f, 0.24f, TileVisualKind.CORDON)
        "8/33", "33/8" -> TileVisualGeometry(0.50f, 0.19f, TileVisualKind.PLINTHE)
        "8/45", "45/8", "9/45", "45/9" -> TileVisualGeometry(0.66f, 0.20f, TileVisualKind.PLINTHE)
        "8/60", "60/8", "7/60", "60/7" -> TileVisualGeometry(0.90f, 0.22f, TileVisualKind.PLINTHE)
        "10/30", "30/10" -> TileVisualGeometry(0.52f, 0.26f, TileVisualKind.FRISE)
        "10/60", "60/10" -> TileVisualGeometry(0.90f, 0.28f, TileVisualKind.FRISE)
        "120/60", "60/120" -> TileVisualGeometry(0.96f, 0.48f, TileVisualKind.TILE)
        "90/90" -> TileVisualGeometry(0.90f, 0.90f, TileVisualKind.TILE)
        "80/80" -> TileVisualGeometry(0.82f, 0.82f, TileVisualKind.TILE)
        "60/60" -> TileVisualGeometry(0.72f, 0.72f, TileVisualKind.TILE)
        "45/45" -> TileVisualGeometry(0.52f, 0.52f, TileVisualKind.TILE)
        "33/33" -> TileVisualGeometry(0.42f, 0.42f, TileVisualKind.TILE)
        "30/30" -> TileVisualGeometry(0.38f, 0.38f, TileVisualKind.TILE)
        "45/90" -> TileVisualGeometry(0.88f, 0.46f, TileVisualKind.TILE)
        "30/90" -> TileVisualGeometry(0.90f, 0.32f, TileVisualKind.TILE)
        "30/60", "30/65" -> TileVisualGeometry(0.74f, 0.40f, TileVisualKind.TILE)
        "25/60", "25/70", "25/75" -> TileVisualGeometry(0.78f, 0.34f, TileVisualKind.TILE)
        "15/60", "15/75", "20/75" -> TileVisualGeometry(0.86f, 0.27f, TileVisualKind.TILE)
        else -> {
            val kind = when {
                normCat == "plinthe" -> TileVisualKind.PLINTHE
                normCat == "frise" -> TileVisualKind.FRISE
                "cordon" in normName -> TileVisualKind.CORDON
                else -> TileVisualKind.TILE
            }
            TileVisualGeometry(0.70f, 0.70f, kind)
        }
    }
}

fun resolveSemanticSwatchHex(
    colorHex: String,
    productName: String = "",
    colorName: String = ""
): String {
    val normName = normalizeSearchText(productName)
    val normColor = normalizeSearchText(colorName)
    if ("mozaik" in normName || "mosaic" in normName || "mosaik" in normName) return "#7C3AED"
    if ("douche" in normName) return "#0284C7"
    if ("salle de bain" in normName || "sdb" in normName) return "#059669"
    if ("motif" in normName || "cuisine" in normName) return "#D97706"

    if ("baguette" in normName || "cordon" in normName || "taco" in normName || "pate a joint" in normName || "ciment" in normName) {
        return when {
            "noir" in normName || "black" in normName || "noir" in normColor -> "#111827"
            "dore" in normName || "gold" in normName || "nadjma" in normName || "dore" in normColor -> "#D4AF37"
            "argent" in normName || "silver" in normName || "argent" in normColor -> "#C0C8D2"
            "beige" in normName || "fleur" in normName || "beige" in normColor -> "#E2D4C0"
            "blanc" in normName || "white" in normName || "blanc" in normColor -> "#F8FAFC"
            "gris" in normName || "ciment" in normName || "gris" in normColor -> "#64748B"
            else -> colorHex
        }
    }
    return colorHex
}


