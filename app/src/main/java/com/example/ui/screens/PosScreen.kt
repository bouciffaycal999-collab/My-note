package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ActiveTileContext
import com.example.data.model.AppSettings
import com.example.data.model.CartItem
import com.example.data.model.CatalogIndex
import com.example.data.model.Customer
import com.example.data.model.FreeLineItem
import com.example.data.model.HeldCart
import com.example.data.model.Invoice
import com.example.data.model.InvoiceClient
import com.example.data.model.POS_ACCESSORY_FILTER_OPTIONS
import com.example.data.model.POS_COLOR_OPTIONS
import com.example.data.model.POS_DIMENSIONS_ORDER
import com.example.data.model.POS_FINITION_OPTIONS
import com.example.data.model.PosSeeds
import com.example.data.model.SearchCorrectionSuggestion
import com.example.data.model.StoreInfo
import com.example.data.model.TileCalculatorMath
import com.example.data.model.TileDimensionUnit
import com.example.data.model.TileProduct
import com.example.data.model.buildCatalogIndex
import com.example.data.model.buildCatalogSearchVocabulary
import com.example.data.model.buildProductSearchBlob
import com.example.data.model.extractActiveTileContexts
import com.example.data.model.formatCartItemDesignation
import com.example.data.model.formatDa
import com.example.data.model.formatDimensionSlash
import com.example.data.model.formatFinitionChipLabel
import com.example.data.model.formatFinitionLabel
import com.example.data.model.formatM2
import com.example.data.model.formatProductDesignation
import com.example.data.model.isAccessoryProduct
import com.example.data.model.isJetonProduct
import com.example.data.model.isPieceEligibleTileDimension
import com.example.data.model.isPlinthe8x45
import com.example.data.model.isPlinthe8x60
import com.example.data.model.isSoldByBag
import com.example.data.model.isTile120x60
import com.example.data.model.matchesColorFilter
import com.example.data.model.matchesFinitionFilter
import com.example.data.model.matchesProductSearch
import com.example.data.model.normalizeDimensionKey
import com.example.data.model.normalizeSearchText
import com.example.data.model.resolveSemanticSwatchHex
import com.example.data.model.suggestSearchCorrections
import com.example.ui.components.ClearableOutlinedTextField
import com.example.ui.components.DispoDepotRecipientsDialog
import com.example.ui.components.GuidedAccessoryLibreWizardDialog
import com.example.ui.components.GuidedArticleLibreWizardDialog
import com.example.ui.components.NonImeAlphanumericKeypad
import com.example.ui.components.NonImeNumericKeypad
import com.example.ui.components.NumericDisplayField
import com.example.ui.components.PosCartPanel
import com.example.ui.components.PosCompactBottomFilterDrawer
import com.example.ui.components.ProductPhotoLightboxDialog
import com.example.ui.components.SuppressSystemSoftKeyboard
import com.example.ui.components.applyAlphanumericKeypadInput
import com.example.ui.components.applyKeypadInput
import com.example.ui.components.applyKeypadQuickDelta
import com.example.ui.components.parseCeramicColor
import com.example.ui.theme.Amber300
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber950
import com.example.ui.theme.BannerCyan
import com.example.ui.theme.BannerEmerald
import com.example.ui.theme.BannerGold
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.OnGold
import com.example.ui.theme.PosNumericTextStyle
import com.example.ui.theme.Purple300
import com.example.ui.theme.Purple500
import com.example.ui.theme.Purple950
import com.example.ui.theme.Rose300
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.Yellow300
import com.example.ui.theme.Yellow400
import com.example.ui.theme.Yellow500
import com.example.ui.theme.Yellow950
import com.example.ui.theme.dimensionBadgeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

private val cleanFmt = DecimalFormat("0.####", DecimalFormatSymbols(Locale.US))

private fun fmtClean(v: Double): String {
    val clean = (v * 10000.0).let { kotlin.math.round(it) / 10000.0 }
    return cleanFmt.format(clean)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    products: List<TileProduct>,
    catalogIndex: CatalogIndex? = null,
    cart: List<CartItem>,
    freeLines: List<FreeLineItem>,
    customers: List<Customer>,
    storeInfo: StoreInfo = PosSeeds.DEFAULT_STORE_INFO,
    editingInvoice: Invoice?,
    canModifyPrices: Boolean = true,
    canEditColisage: Boolean = true,
    onOpenCalculatorForProduct: (TileProduct) -> Unit,
    onRequestDepotDispo: (TileProduct) -> Unit,
    onOpenOneTimeProduct: () -> Unit,
    onAddGuidedCartProduct: ((TileProduct, Int, Int, Double, Double, Double) -> Unit)? = null,
    onEditCartItem: (CartItem) -> Unit,
    onUpdateCartItemDirect: (String, String, Int, Int, Double, Int, Double, Double) -> Unit,
    onUpdateCartItemDirectWithUnit: ((String, String, Int, Int, Double, Int, Double, Double, String) -> Unit)? = null,
    onUpdateCartItemMeters: (String, Double) -> Unit = { _, _ -> },
    onAdjustCartItemCarton: (String, Int) -> Unit,
    onAdjustCartItemLoosePiece: (String, Int) -> Unit = { _, _ -> },
    onToggleCartItemCc: (String) -> Unit = {},
    onRemoveCartItem: (String) -> Unit,
    onAddFreeLine: (FreeLineItem) -> Unit,
    onUpdateFreeLine: (FreeLineItem) -> Unit,
    onAdjustFreeLineQty: (String, Double) -> Unit,
    onRemoveFreeLine: (String) -> Unit,
    onClearCart: () -> Unit,
    onFinalizeSale: (InvoiceClient, Double, Double, String, String) -> Unit,
    onFinalizeSaleWithDestination: ((InvoiceClient, Double, Double, String, String, String) -> Unit)? = null,
    onFinalizeSaleExtended: ((InvoiceClient, Double, Double, String, String, String, Boolean, Boolean) -> Unit)? = null,
    canUpdateProductPhoto: Boolean = false,
    onUpdateProductPhoto: ((String, String) -> Unit)? = null,
    heldCarts: List<HeldCart> = emptyList(),
    settings: AppSettings = PosSeeds.DEFAULT_SETTINGS,
    isAdmin: Boolean = false,
    onHoldCart: (String) -> Unit = {},
    onResumeHeldCart: (String) -> Unit = {},
    onDeleteHeldCart: (String) -> Unit = {},
    onVerifyAdminPin: (String) -> Boolean = { true },
    returnDeductionDa: Double = 0.0,
    onSetReturnDeduction: (Double) -> Unit = {},
    topFavoriteProducts: List<TileProduct> = emptyList(),
    isBackupOverdue: Boolean = false,
    onQuickExportBackup: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDimension by remember { mutableStateOf("Toutes") }
    var selectedUsine by remember { mutableStateOf("Toutes") }
    var selectedFinitions by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedColor by remember { mutableStateOf("Toutes") }
    var selectedAccessory by remember { mutableStateOf("Tous") }
    var isGridView by remember { mutableStateOf(true) }
    var filtersExpanded by remember { mutableStateOf(false) }

    var showCartSheet by remember { mutableStateOf(false) }
    var showHeldCartsDialog by remember { mutableStateOf(false) }
    var reopenCartSheetAfterDialog by remember { mutableStateOf(false) }
    var showDiversDialog by remember { mutableStateOf(false) }
    var showGuidedArticleLibre by remember { mutableStateOf(false) }
    var showGuidedAccessoryLibre by remember { mutableStateOf(false) }
    var lastCreatedTileContext by remember { mutableStateOf<ActiveTileContext?>(null) }
    var editingFreeLine by remember { mutableStateOf<FreeLineItem?>(null) }
    var quickEditCartItem by remember { mutableStateOf<CartItem?>(null) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var previewProduct by remember { mutableStateOf<TileProduct?>(null) }
    var dispoTargetProduct by remember { mutableStateOf<TileProduct?>(null) }

    val openArticleLibreAction: () -> Unit = {
        if (onAddGuidedCartProduct != null) {
            showGuidedArticleLibre = true
        } else {
            onOpenOneTimeProduct()
        }
    }
    val hasTileContextForAccessories = remember(cart, lastCreatedTileContext) {
        extractActiveTileContexts(cart, lastCreatedTileContext).isNotEmpty()
    }

    val activeFilterCount = listOf(selectedDimension, selectedUsine, selectedColor).count { it != "Toutes" } +
        selectedFinitions.size +
        (if (selectedAccessory != "Tous") 1 else 0)
    val hasActiveFilter = searchQuery.isNotBlank() || activeFilterCount > 0

    val effectiveIndex = remember(catalogIndex, products) {
        if (catalogIndex != null && catalogIndex.entries.size == products.size) {
            catalogIndex
        } else {
            buildCatalogIndex(products)
        }
    }
    val dimensions = effectiveIndex.tileDimensions
    val usines = effectiveIndex.usines
    val finitions = effectiveIndex.finitions
    val colorOptions = remember {
        listOf("Toutes") + POS_COLOR_OPTIONS.map { it.label }
    }
    val colorHexMap = remember {
        POS_COLOR_OPTIONS.associate { it.label to it.hex }
    }

    val catalogById = effectiveIndex.byId
    val queryTokens = remember(searchQuery) {
        normalizeSearchText(searchQuery).split(' ').filter { it.isNotBlank() }
    }

    val selectedFinitionCombined = remember(selectedFinitions) {
        if (selectedFinitions.isEmpty()) "Toutes" else selectedFinitions.joinToString(" + ")
    }

    val filteredProducts = remember(
        effectiveIndex, queryTokens, selectedDimension, selectedUsine, selectedFinitionCombined, selectedColor, selectedAccessory
    ) {
        effectiveIndex.filter(
            queryTokens = queryTokens,
            selectedDimension = selectedDimension,
            selectedUsine = selectedUsine,
            selectedFinition = selectedFinitionCombined,
            selectedColor = selectedColor,
            selectedAccessory = selectedAccessory
        )
    }

    var searchSuggestions by remember { mutableStateOf<List<SearchCorrectionSuggestion>>(emptyList()) }
    LaunchedEffect(searchQuery, filteredProducts.size, effectiveIndex) {
        if (searchQuery.trim().length < 2 || filteredProducts.isNotEmpty()) {
            searchSuggestions = emptyList()
            return@LaunchedEffect
        }
        delay(160L)
        searchSuggestions = withContext(Dispatchers.Default) {
            suggestSearchCorrections(searchQuery, products, effectiveIndex.searchPairs, effectiveIndex.vocabulary)
        }
    }

    val totalBrut = remember(cart, freeLines) {
        cart.sumOf { it.sousTotal } + freeLines.sumOf { it.sousTotal }
    }
    val totalLinesCount = cart.size + freeLines.size
    val searchFocusRequester = remember { FocusRequester() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when {
                    keyEvent.isCtrlPressed && keyEvent.key == Key.K -> {
                        runCatching { searchFocusRequester.requestFocus() }
                        true
                    }
                    keyEvent.isAltPressed && keyEvent.key == Key.N -> {
                        openArticleLibreAction()
                        true
                    }
                    keyEvent.isAltPressed && keyEvent.key == Key.A -> {
                        showGuidedAccessoryLibre = true
                        true
                    }
                    keyEvent.key == Key.F12 && (cart.isNotEmpty() || freeLines.isNotEmpty()) -> {
                        showCheckoutDialog = true
                        true
                    }
                    else -> false
                }
            }
    ) {
        val isNarrow = maxWidth < 360.dp
        val isCompactHeight = maxHeight < 600.dp
        val isWide = maxWidth >= 840.dp
        val isLaptop = maxWidth >= 1024.dp
        val sideCartWidth = if (maxWidth >= 1240.dp) 430.dp else 390.dp
        val gridColumns = when {
            maxWidth >= 1460.dp -> 4
            maxWidth >= 1120.dp -> 3
            maxWidth >= 600.dp && !isWide -> 3
            else -> 2
        }

        // On Laptop/Wide screens, auto-expand filters once so the showroom filter matrix is immediately clickable
        var didInitWideFilters by remember { mutableStateOf(false) }
        LaunchedEffect(isWide) {
            if (isWide && !didInitWideFilters) {
                didInitWideFilters = true
                filtersExpanded = true
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Main Catalog Pane: Slim Top Header -> Expansive Product Grid -> Bottom Thumb-Zone Dock (Filters + Search + Article Libre/Accessoire) -> Phone Panier Bar
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .imePadding()
            ) {
                // Ultra-slim Top Showroom Header Bar (gives maximum vertical height to product cards)
                Surface(
                    color = Slate900,
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = if (isNarrow) 10.dp else 12.dp,
                                vertical = if (isCompactHeight) 4.dp else 6.dp
                            ),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (editingInvoice != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Amber950,
                                border = BorderStroke(1.5.dp, Amber500)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Modification Facture #${editingInvoice.reference}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Amber300,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = onClearCart) {
                                        Text("Annuler", color = Rose400, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (isNarrow) "Showroom" else "Catalogue Showroom",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Slate100,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = Amber950,
                                    border = BorderStroke(1.dp, Amber500.copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = "${filteredProducts.size}/${products.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Amber300,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (hasActiveFilter) {
                                    Surface(
                                        shape = RoundedCornerShape(7.dp),
                                        color = Rose950,
                                        border = BorderStroke(1.dp, Rose500.copy(alpha = 0.55f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(7.dp))
                                            .clickable {
                                                searchQuery = ""
                                                selectedDimension = "Toutes"
                                                selectedUsine = "Toutes"
                                                selectedFinitions = emptySet()
                                                selectedColor = "Toutes"
                                                selectedAccessory = "Tous"
                                            }
                                            .testTag("pos_reset_filters_btn")
                                    ) {
                                        Text(
                                            text = "Réinit.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Rose400,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (heldCarts.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(9.dp),
                                        color = Cyan950,
                                        border = BorderStroke(1.2.dp, Cyan400),
                                        modifier = Modifier
                                            .heightIn(min = 36.dp)
                                            .clip(RoundedCornerShape(9.dp))
                                            .clickable { showHeldCartsDialog = true }
                                            .testTag("pos_held_carts_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Pause,
                                                contentDescription = "Paniers en attente",
                                                tint = Cyan300,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Attente (${heldCarts.size})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Cyan300,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = Slate950,
                                    border = BorderStroke(1.dp, Slate800)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isGridView) Amber500 else Slate950,
                                            modifier = Modifier
                                                .clickable { isGridView = true }
                                                .testTag("pos_view_grid_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.GridView,
                                                contentDescription = "Vue Grille",
                                                tint = if (isGridView) OnGold else Slate400,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp).size(17.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (!isGridView) Amber500 else Slate950,
                                            modifier = Modifier
                                                .clickable { isGridView = false }
                                                .testTag("pos_view_list_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TableRows,
                                                contentDescription = "Vue Liste",
                                                tint = if (!isGridView) OnGold else Slate400,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp).size(17.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (isBackupOverdue && onQuickExportBackup != null) {
                        Surface(
                            onClick = onQuickExportBackup,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = if (isNarrow) 6.dp else 10.dp, vertical = 3.dp)
                                .testTag("pos_backup_overdue_banner"),
                            shape = RoundedCornerShape(10.dp),
                            color = Amber950,
                            border = BorderStroke(1.2.dp, Amber500)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Sauvegarde recommandée",
                                    tint = Amber300,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Sauvegarde > 3 jours • Touchez ici pour sauvegarder la base",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Amber300,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    val (pinnedJetons, remainingProducts) = remember(filteredProducts, searchQuery) {
                        if (searchQuery.isNotBlank()) {
                            val jetons = filteredProducts.filter { isJetonProduct(it) }
                            val rest = filteredProducts.filterNot { isJetonProduct(it) }
                            jetons to rest
                        } else {
                            emptyList<TileProduct>() to filteredProducts
                        }
                    }
                    val displayProducts = if (pinnedJetons.isNotEmpty()) remainingProducts else filteredProducts

                    if (pinnedJetons.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = if (isNarrow) 6.dp else 10.dp, vertical = 4.dp)
                                .testTag("pos_pinned_jeton_banner"),
                            shape = RoundedCornerShape(12.dp),
                            color = Yellow950,
                            border = BorderStroke(1.8.dp, BannerGold),
                            shadowElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = BannerGold,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "JETON ÉPINGLÉ (TOUJOURS EN HAUT)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BannerGold,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Text(
                                        text = "${pinnedJetons.size} fixe",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400
                                    )
                                }
                                pinnedJetons.forEach { jetonProd ->
                                    ProductTileListRow(
                                        product = jetonProd,
                                        precomputedDesignation = effectiveIndex.designationById[jetonProd.id],
                                        isNarrow = isNarrow,
                                        onOpenCalculator = { onOpenCalculatorForProduct(jetonProd) },
                                        onCheckDepotDispo = {
                                            onRequestDepotDispo(jetonProd)
                                            dispoTargetProduct = jetonProd
                                        },
                                        onPreviewImage = { previewProduct = jetonProd }
                                    )
                                }
                            }
                        }
                    }

                    if (displayProducts.isEmpty() && pinnedJetons.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = Slate400, modifier = Modifier.size(52.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Aucun modèle trouvé", style = MaterialTheme.typography.titleMedium, color = Slate100, fontWeight = FontWeight.Bold)
                                Text("Essayez d'ajuster les filtres ou créez un Article Libre.", style = MaterialTheme.typography.bodyMedium, color = Slate400)
                            }
                        }
                    } else if (displayProducts.isNotEmpty()) {
                        if (isGridView) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(gridColumns),
                                contentPadding = PaddingValues(if (isNarrow) 6.dp else 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(if (isNarrow) 6.dp else 8.dp),
                                verticalArrangement = Arrangement.spacedBy(if (isNarrow) 6.dp else 8.dp),
                                modifier = Modifier.weight(1f).fillMaxWidth().testTag("pos_catalog_grid")
                            ) {
                                items(displayProducts, key = { it.id }, contentType = { "tile_product" }) { product ->
                                    ProductTileCard(
                                        product = product,
                                        precomputedDesignation = effectiveIndex.designationById[product.id],
                                        isNarrow = isNarrow,
                                        onOpenCalculator = { onOpenCalculatorForProduct(product) },
                                        onCheckDepotDispo = {
                                            onRequestDepotDispo(product)
                                            dispoTargetProduct = product
                                        },
                                        onPreviewImage = { previewProduct = product }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = if (isNarrow) 6.dp else 10.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f).fillMaxWidth().testTag("pos_catalog_list")
                            ) {
                                itemsIndexed(displayProducts, key = { _, product -> product.id }, contentType = { _, _ -> "tile_product" }) { _, product ->
                                    ProductTileListRow(
                                        product = product,
                                        precomputedDesignation = effectiveIndex.designationById[product.id],
                                        isNarrow = isNarrow,
                                        onOpenCalculator = { onOpenCalculatorForProduct(product) },
                                        onCheckDepotDispo = {
                                            onRequestDepotDispo(product)
                                            dispoTargetProduct = product
                                        },
                                        onPreviewImage = { previewProduct = product }
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Thumb-Zone Control Dock: Compact 2-Row Tabbed Filters + Search Bar + ARTICLE LIBRE & ACCESSOIRE
                Surface(
                    color = Slate900,
                    border = BorderStroke(1.dp, Slate800),
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pos_bottom_controls_dock")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = if (isNarrow) 8.dp else 10.dp,
                                vertical = if (isCompactHeight) 5.dp else 7.dp
                            ),
                        verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 5.dp else 6.dp)
                    ) {
                        // Ultra-compact 2-row tabbed filter drawer (expands upward above search)
                        PosCompactBottomFilterDrawer(
                            expanded = filtersExpanded,
                            dimensions = dimensions,
                            selectedDimension = selectedDimension,
                            onSelectDimension = { next ->
                                selectedDimension = next
                                if (next != "Toutes" && selectedAccessory.contains('/')) selectedAccessory = "Tous"
                            },
                            selectedAccessory = selectedAccessory,
                            onSelectAccessory = { next ->
                                selectedAccessory = next
                                if (next != "Tous" && next.contains('/')) selectedDimension = "Toutes"
                            },
                            usines = usines,
                            selectedUsine = selectedUsine,
                            onSelectUsine = { selectedUsine = it },
                            finitions = finitions,
                            selectedFinitions = selectedFinitions,
                            onToggleFinition = { fin ->
                                val normTarget = normalizeSearchText(fin)
                                val existing = selectedFinitions.firstOrNull { normalizeSearchText(it) == normTarget }
                                selectedFinitions = if (existing != null) {
                                    selectedFinitions - existing
                                } else {
                                    selectedFinitions + fin
                                }
                            },
                            onClearFinitions = { selectedFinitions = emptySet() },
                            colorOptions = colorOptions,
                            colorHexMap = colorHexMap,
                            selectedColor = selectedColor,
                            onSelectColor = { selectedColor = it }
                        )

                        if (searchSuggestions.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pos_search_fuzzy_suggestions"),
                                shape = RoundedCornerShape(10.dp),
                                color = Amber950.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, Amber500.copy(alpha = 0.7f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Voulez-vous dire :",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Amber300,
                                        fontWeight = FontWeight.Black
                                    )
                                    searchSuggestions.forEachIndexed { idx, sug ->
                                        Surface(
                                            onClick = { searchQuery = sug.suggestedQuery },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Amber500,
                                            modifier = Modifier.testTag("pos_fuzzy_chip_$idx")
                                        ) {
                                            Text(
                                                text = "${sug.suggestedQuery} (${sug.matchCount})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = OnGold,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (isWide) {
                            // Wide/Tablet bottom dock row: Search + Filtres Toggle + ARTICLE LIBRE + ACCESSOIRE
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ClearableOutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            "Rechercher • Ex: 120 bati, bleu 120, 60 arbe...",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Amber400) },
                                    modifier = Modifier
                                        .weight(1.6f)
                                        .focusRequester(searchFocusRequester)
                                        .testTag("pos_search_input")
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (filtersExpanded || activeFilterCount > 0) Amber950 else Slate850,
                                    border = BorderStroke(
                                        1.3.dp,
                                        if (filtersExpanded || activeFilterCount > 0) Amber500 else Slate700
                                    ),
                                    modifier = Modifier
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { filtersExpanded = !filtersExpanded }
                                        .testTag("pos_toggle_filters_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = null,
                                            tint = if (filtersExpanded || activeFilterCount > 0) Amber300 else Slate200,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (activeFilterCount > 0) "Filtres ($activeFilterCount)" else "Filtres",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (filtersExpanded || activeFilterCount > 0) Amber300 else Slate100,
                                            fontWeight = FontWeight.Black
                                        )
                                        Icon(
                                            imageVector = if (filtersExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                            contentDescription = if (filtersExpanded) "Masquer les filtres" else "Afficher les filtres",
                                            tint = if (filtersExpanded || activeFilterCount > 0) Amber300 else Slate400,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Button(
                                    onClick = openArticleLibreAction,
                                    modifier = Modifier
                                        .height(50.dp)
                                        .testTag("pos_onetime_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Amber500,
                                        contentColor = OnGold
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp)
                                ) {
                                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ARTICLE LIBRE",
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                }
                                OutlinedButton(
                                    onClick = { showGuidedAccessoryLibre = true },
                                    modifier = Modifier
                                        .height(50.dp)
                                        .testTag("pos_accessory_libre_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Cyan950,
                                        contentColor = Cyan300
                                    ),
                                    border = BorderStroke(
                                        width = if (hasTileContextForAccessories) 1.5.dp else 1.2.dp,
                                        color = if (hasTileContextForAccessories) BannerGold else Cyan500.copy(alpha = 0.75f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (hasTileContextForAccessories) BannerGold else Cyan400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ACCESSOIRE",
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                }
                            }
                        } else {
                            // Phone/Compact Thumb-Zone Row 1: Search Bar + Compact Filtres Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ClearableOutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            "Rechercher • 120 bati, bleu...",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Amber400) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(searchFocusRequester)
                                        .testTag("pos_search_input")
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (filtersExpanded || activeFilterCount > 0) Amber950 else Slate850,
                                    border = BorderStroke(
                                        1.3.dp,
                                        if (filtersExpanded || activeFilterCount > 0) Amber500 else Slate700
                                    ),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { filtersExpanded = !filtersExpanded }
                                        .testTag("pos_toggle_filters_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = null,
                                            tint = if (filtersExpanded || activeFilterCount > 0) Amber300 else Slate200,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Text(
                                            text = if (activeFilterCount > 0) "($activeFilterCount)" else "Filtres",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (filtersExpanded || activeFilterCount > 0) Amber300 else Slate100,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1
                                        )
                                        Icon(
                                            imageVector = if (filtersExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                            contentDescription = if (filtersExpanded) "Masquer les filtres" else "Afficher les filtres",
                                            tint = if (filtersExpanded || activeFilterCount > 0) Amber300 else Slate400,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }

                            // Phone/Compact Thumb-Zone Row 2: ARTICLE LIBRE + ACCESSOIRE (easy one-hand tap!)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = openArticleLibreAction,
                                    modifier = Modifier
                                        .weight(1.05f)
                                        .height(44.dp)
                                        .testTag("pos_onetime_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Amber500,
                                        contentColor = OnGold
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ARTICLE LIBRE",
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showGuidedAccessoryLibre = true },
                                    modifier = Modifier
                                        .weight(1.0f)
                                        .height(44.dp)
                                        .testTag("pos_accessory_libre_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Cyan950,
                                        contentColor = Cyan300
                                    ),
                                    border = BorderStroke(
                                        width = if (hasTileContextForAccessories) 1.5.dp else 1.2.dp,
                                        color = if (hasTileContextForAccessories) BannerGold else Cyan500.copy(alpha = 0.75f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (hasTileContextForAccessories) BannerGold else Cyan400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ACCESSOIRE",
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Docked Panier Bar on Phones/Medium Screens (never overlaps product card buttons)
                if (!isWide) {
                    val hasItems = totalLinesCount > 0
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (isNarrow) 8.dp else 12.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showCartSheet = true }
                            .testTag("pos_floating_cart_bar"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (hasItems) Navy950 else Navy900,
                        border = BorderStroke(1.5.dp, if (hasItems) Amber500 else Slate700),
                        tonalElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                BadgedBox(
                                    badge = {
                                        if (hasItems) {
                                            Badge(containerColor = Amber500, contentColor = OnGold) {
                                                Text(totalLinesCount.toString(), fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Panier",
                                        tint = if (hasItems) BannerGold else Amber400,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (!hasItems) "Panier vide • Appuyer pour ouvrir" else "$totalLinesCount article(s) • ${cart.sumOf { it.cartons }} Ctn • ${formatM2(cart.sumOf { it.m2Total })} m²",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (hasItems) OnDark.copy(alpha = 0.9f) else Slate200,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val barNetTotal = (totalBrut - returnDeductionDa).coerceAtLeast(0.0)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = formatDa(barNetTotal),
                                            style = PosNumericTextStyle.copy(fontSize = if (isNarrow) 17.sp else 19.sp),
                                            color = if (hasItems) BannerEmerald else Emerald400
                                        )
                                        if (hasItems && returnDeductionDa > 0.01) {
                                            Text(
                                                text = "(-${formatDa(returnDeductionDa)} ret.)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Rose300,
                                                fontWeight = FontWeight.Black,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { showCartSheet = true },
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("pos_open_panier_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasItems) Emerald500 else Amber500,
                                    contentColor = if (hasItems) OnDark else OnGold
                                )
                            ) {
                                Text(
                                    text = if (hasItems) "Voir Panier" else "Panier",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // Persistent Side Cart Pane on Tablets & Laptop/Desktop Screens
            if (isWide) {
                Surface(
                    modifier = Modifier
                        .width(sideCartWidth)
                        .fillMaxHeight()
                        .testTag("pos_desktop_side_cart"),
                    color = Slate900,
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    PosCartPanel(
                        cart = cart,
                        freeLines = freeLines,
                        catalogById = catalogById,
                        totalBrut = totalBrut,
                        returnDeductionDa = returnDeductionDa,
                        onSetReturnDeduction = onSetReturnDeduction,
                        editingInvoice = editingInvoice,
                        canModifyPrices = canModifyPrices,
                        onUpdateCartItemDirect = onUpdateCartItemDirect,
                        onUpdateCartItemMeters = onUpdateCartItemMeters,
                        onAdjustCartItemCarton = onAdjustCartItemCarton,
                        onAdjustCartItemLoosePiece = onAdjustCartItemLoosePiece,
                        onToggleCartItemCc = onToggleCartItemCc,
                        onRemoveCartItem = onRemoveCartItem,
                        onEditFreeLine = {
                            editingFreeLine = it
                            showDiversDialog = true
                        },
                        onAdjustFreeLineQty = onAdjustFreeLineQty,
                        onRemoveFreeLine = onRemoveFreeLine,
                        onClearCart = onClearCart,
                        onOpenOneTimeProduct = openArticleLibreAction,
                        onOpenDivers = {
                            showGuidedAccessoryLibre = true
                        },
                        onCloseSheet = null,
                        onOpenCheckout = { showCheckoutDialog = true },
                        heldCartsCount = heldCarts.size,
                        onHoldCart = if (editingInvoice == null) ({ onHoldCart("") }) else null,
                        onOpenHeldCarts = if (heldCarts.isNotEmpty()) ({ showHeldCartsDialog = true }) else null
                    )
                }
            }
        }
    }

    if (showCartSheet) {
        Dialog(
            onDismissRequest = { showCartSheet = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = true
            )
        ) {
            BackHandler { showCartSheet = false }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .fillMaxHeight(0.95f)
                    .imePadding()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                shape = RoundedCornerShape(22.dp),
                color = Slate900,
                border = BorderStroke(1.5.dp, Slate700),
                shadowElevation = 8.dp
            ) {
                PosCartPanel(
                    cart = cart,
                    freeLines = freeLines,
                    catalogById = catalogById,
                    totalBrut = totalBrut,
                    returnDeductionDa = returnDeductionDa,
                    onSetReturnDeduction = onSetReturnDeduction,
                    editingInvoice = editingInvoice,
                    canModifyPrices = canModifyPrices,
                    onUpdateCartItemDirect = onUpdateCartItemDirect,
                    onUpdateCartItemMeters = onUpdateCartItemMeters,
                    onAdjustCartItemCarton = onAdjustCartItemCarton,
                    onAdjustCartItemLoosePiece = onAdjustCartItemLoosePiece,
                    onToggleCartItemCc = onToggleCartItemCc,
                    onRemoveCartItem = onRemoveCartItem,
                    onEditFreeLine = {
                        reopenCartSheetAfterDialog = true
                        showCartSheet = false
                        editingFreeLine = it
                        showDiversDialog = true
                    },
                    onAdjustFreeLineQty = onAdjustFreeLineQty,
                    onRemoveFreeLine = onRemoveFreeLine,
                    onClearCart = onClearCart,
                    onOpenOneTimeProduct = {
                        reopenCartSheetAfterDialog = false
                        showCartSheet = false
                        openArticleLibreAction()
                    },
                    onOpenDivers = {
                        reopenCartSheetAfterDialog = false
                        showCartSheet = false
                        showGuidedAccessoryLibre = true
                    },
                    onCloseSheet = { showCartSheet = false },
                    onOpenCheckout = {
                        reopenCartSheetAfterDialog = false
                        showCartSheet = false
                        showCheckoutDialog = true
                    },
                    heldCartsCount = heldCarts.size,
                    onHoldCart = if (editingInvoice == null) ({
                        onHoldCart("")
                        showCartSheet = false
                    }) else null,
                    onOpenHeldCarts = if (heldCarts.isNotEmpty()) ({
                        showCartSheet = false
                        showHeldCartsDialog = true
                    }) else null
                )
            }
        }
    }

    if (showHeldCartsDialog) {
        HeldCartsModalDialog(
            heldCarts = heldCarts,
            onDismiss = { showHeldCartsDialog = false },
            onResume = { id ->
                onResumeHeldCart(id)
                showHeldCartsDialog = false
                showCartSheet = true
            },
            onDelete = { id ->
                onDeleteHeldCart(id)
            }
        )
    }

    quickEditCartItem?.let { targetItem ->
        EditCartItemQuickDialog(
            item = targetItem,
            canModifyPrices = canModifyPrices,
            onDismiss = {
                quickEditCartItem = null
                if (reopenCartSheetAfterDialog) {
                    reopenCartSheetAfterDialog = false
                    showCartSheet = true
                }
            },
            onOpenFullCalculator = {
                reopenCartSheetAfterDialog = false
                quickEditCartItem = null
                showCartSheet = false
                onEditCartItem(targetItem)
            },
            onSave = { name, cartons, pieces, m2Ctn, pcs, prix, prixPiece, unit ->
                if (onUpdateCartItemDirectWithUnit != null) {
                    onUpdateCartItemDirectWithUnit(targetItem.id, name, cartons, pieces, m2Ctn, pcs, prix, prixPiece, unit)
                } else {
                    onUpdateCartItemDirect(targetItem.id, name, cartons, pieces, m2Ctn, pcs, prix, prixPiece)
                }
                quickEditCartItem = null
                if (reopenCartSheetAfterDialog) {
                    reopenCartSheetAfterDialog = false
                    showCartSheet = true
                }
            }
        )
    }

    if (showDiversDialog) {
        DiversLineDialog(
            editingLine = editingFreeLine,
            onDismiss = {
                showDiversDialog = false
                editingFreeLine = null
                if (reopenCartSheetAfterDialog) {
                    reopenCartSheetAfterDialog = false
                    showCartSheet = true
                }
            },
            onSaveLine = { line ->
                if (editingFreeLine != null) {
                    onUpdateFreeLine(line)
                } else {
                    onAddFreeLine(line)
                }
                showDiversDialog = false
                editingFreeLine = null
                if (reopenCartSheetAfterDialog) {
                    reopenCartSheetAfterDialog = false
                    showCartSheet = true
                }
            }
        )
    }

    if (showGuidedArticleLibre) {
        GuidedArticleLibreWizardDialog(
            products = products,
            dimensions = dimensions,
            usines = usines,
            finitions = finitions,
            onDismiss = { showGuidedArticleLibre = false },
            onAddToCart = { product, cartons, pieces, m2Total, prixPerM2, prixPiece, createdCtx ->
                lastCreatedTileContext = createdCtx
                onAddGuidedCartProduct?.invoke(product, cartons, pieces, m2Total, prixPerM2, prixPiece)
                showGuidedArticleLibre = false
            },
            onAddAndOpenAccessories = { product, cartons, pieces, m2Total, prixPerM2, prixPiece, createdCtx ->
                lastCreatedTileContext = createdCtx
                onAddGuidedCartProduct?.invoke(product, cartons, pieces, m2Total, prixPerM2, prixPiece)
                showGuidedArticleLibre = false
                showGuidedAccessoryLibre = true
            }
        )
    }

    if (showGuidedAccessoryLibre) {
        GuidedAccessoryLibreWizardDialog(
            cart = cart,
            lastCreatedTileContext = lastCreatedTileContext,
            products = products,
            onDismiss = { showGuidedAccessoryLibre = false },
            onAddAccessoryToCart = { product, cartons, pieces, m2Total, prixPerM2, prixPiece ->
                if (onAddGuidedCartProduct != null) {
                    onAddGuidedCartProduct(product, cartons, pieces, m2Total, prixPerM2, prixPiece)
                } else {
                    // Fallback to free line when PosScreen is rendered standalone without onAddGuidedCartProduct
                    val qty = if (cartons > 0) cartons.toDouble() else pieces.toDouble()
                    val unitRate = if (prixPiece > 0.0) prixPiece else prixPerM2
                    onAddFreeLine(
                        FreeLineItem(
                            id = "free_${UUID.randomUUID().toString().take(8)}",
                            designation = product.name,
                            quantite = qty,
                            unite = if (isSoldByBag(product.name)) "sac" else if (cartons > 0) "crt" else "pcs",
                            prixUnitaire = unitRate,
                            sousTotal = kotlin.math.round(qty * unitRate * 100.0) / 100.0
                        )
                    )
                }
            }
        )
    }

    if (showCheckoutDialog) {
        CheckoutModalDialog(
            totalBrut = totalBrut,
            initialReturnDeduction = returnDeductionDa,
            onUpdateReturnDeduction = onSetReturnDeduction,
            customers = customers,
            editingInvoice = editingInvoice,
            canModifyPrices = canModifyPrices,
            settings = settings,
            isAdmin = isAdmin,
            onVerifyAdminPin = onVerifyAdminPin,
            onDismiss = { showCheckoutDialog = false },
            onConfirm = { client, remise, paye, mode, notes, destination ->
                showCheckoutDialog = false
                if (onFinalizeSaleExtended != null) {
                    onFinalizeSaleExtended(client, remise, paye, mode, notes, destination, false, false)
                } else if (onFinalizeSaleWithDestination != null) {
                    onFinalizeSaleWithDestination(client, remise, paye, mode, notes, destination)
                } else {
                    onFinalizeSale(client, remise, paye, mode, notes)
                }
            },
            onConfirmExtended = { client, remise, paye, mode, notes, destination, isDevis, deferredDepotDelivery ->
                showCheckoutDialog = false
                if (onFinalizeSaleExtended != null) {
                    onFinalizeSaleExtended(client, remise, paye, mode, notes, destination, isDevis, deferredDepotDelivery)
                } else if (onFinalizeSaleWithDestination != null) {
                    onFinalizeSaleWithDestination(client, remise, paye, mode, notes, destination)
                } else {
                    onFinalizeSale(client, remise, paye, mode, notes)
                }
            }
        )
    }

    previewProduct?.let { prod ->
        ProductPhotoLightboxDialog(
            product = prod,
            onDismiss = { previewProduct = null },
            onCheckDepotDispo = {
                previewProduct = null
                onRequestDepotDispo(prod)
                dispoTargetProduct = prod
            },
            onOpenCalculator = { onOpenCalculatorForProduct(prod) },
            onUpdatePhoto = if (canUpdateProductPhoto && onUpdateProductPhoto != null) {
                { savedPath ->
                    onUpdateProductPhoto(prod.id, savedPath)
                    previewProduct = prod.copy(image = savedPath)
                }
            } else null
        )
    }

    dispoTargetProduct?.let { prod ->
        DispoDepotRecipientsDialog(
            product = prod,
            recipients = storeInfo.dispoWaRecipients.ifEmpty { PosSeeds.DEFAULT_DISPO_WA_RECIPIENTS },
            onDismiss = { dispoTargetProduct = null }
        )
    }
}

@Composable
private fun CompactPhotoIconButton(
    product: TileProduct,
    designation: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swatchHex = remember(product.color, product.name, product.colorName) {
        resolveSemanticSwatchHex(colorHex = product.color, productName = product.name, colorName = product.colorName)
    }
    val swatchColor = remember(swatchHex) { parseCeramicColor(swatchHex) }
    val iconTint = if (swatchColor.luminance() > 0.52f) OnGold else Color(0xFFFFF6EA)
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(34.dp)
            .testTag("product_photo_${product.id}"),
        shape = RoundedCornerShape(8.dp),
        color = swatchColor,
        border = BorderStroke(1.2.dp, Slate700)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Voir photo : $designation",
                tint = iconTint.copy(alpha = 0.92f),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun ProductTileCard(
    product: TileProduct,
    precomputedDesignation: String? = null,
    isNarrow: Boolean = false,
    onOpenCalculator: () -> Unit,
    onCheckDepotDispo: () -> Unit,
    onPreviewImage: () -> Unit
) {
    val designation = precomputedDesignation ?: remember(product.id, product.name, product.usine, product.dimension, product.finition, product.choix) {
        formatProductDesignation(product)
    }
    val is8x45 = remember(product.dimension) { isPlinthe8x45(product.dimension) }
    val isBag = remember(product.name) { isSoldByBag(product.name) }
    val isJeton = remember(product.id, product.name, product.usine, product.dimension) {
        isJetonProduct(product)
    }
    val finishBadge = remember(product.finition) { formatFinitionLabel(product.finition) }
    val hoverSource = remember { MutableInteractionSource() }
    val isHovered by hoverSource.collectIsHoveredAsState()

    Surface(
        onClick = onOpenCalculator,
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(hoverSource)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        color = when {
            isJeton -> Yellow950
            isHovered -> Slate850
            else -> Slate900
        },
        border = BorderStroke(
            width = if (isJeton || isHovered) 2.dp else 1.2.dp,
            color = if (isJeton || isHovered) BannerGold else Slate800
        ),
        shadowElevation = if (isJeton || isHovered) 6.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(if (isNarrow) 9.dp else 11.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    CompactPhotoIconButton(
                        product = product,
                        designation = designation,
                        onClick = onPreviewImage
                    )
                    val dimTheme = remember(product.dimension) { dimensionBadgeTheme(product.dimension) }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = dimTheme.containerColor,
                        border = BorderStroke(1.dp, dimTheme.borderColor)
                    ) {
                        Text(
                            text = product.dimension,
                            style = MaterialTheme.typography.labelSmall,
                            color = dimTheme.textColor,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (isJeton) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BannerGold,
                            border = BorderStroke(1.dp, Yellow300),
                            modifier = Modifier.testTag("jeton_badge_${product.id}")
                        ) {
                            Text(
                                text = "★ JETON",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnGold,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (finishBadge.isNotBlank() && !designation.contains(finishBadge, ignoreCase = true)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Purple950,
                            border = BorderStroke(1.dp, Purple500.copy(alpha = 0.55f))
                        ) {
                            Text(
                                text = finishBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = Purple300,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Prominent Product Name (clearly scannable in showroom)
            Text(
                text = designation,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = if (isNarrow) 13.5.sp else 15.sp,
                    lineHeight = if (isNarrow) 17.sp else 19.sp
                ),
                color = if (isJeton) BannerGold else Color.White,
                fontWeight = FontWeight.Black,
                minLines = 2,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Prominent Price + m²/carton highlight strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val primaryPrice = when {
                    isBag && product.prixPiece > 0.0 -> "${formatDa(product.prixPiece)}/sac"
                    isBag -> "${formatDa(product.prix)}/sac"
                    is8x45 -> "${formatDa(product.prix)}/ctn"
                    product.isSoldByPiece && product.prixPiece > 0.0 -> "${formatDa(product.prixPiece)}/pc"
                    else -> "${formatDa(product.prix)}/m²"
                }
                Text(
                    text = primaryPrice,
                    style = PosNumericTextStyle.copy(fontSize = if (isNarrow) 13.5.sp else 15.sp),
                    color = Emerald300,
                    maxLines = 1
                )
                when {
                    isBag -> {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = Amber950,
                            border = BorderStroke(1.3.dp, Amber500)
                        ) {
                            Text(
                                text = "1 sac",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                color = Amber300,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                    is8x45 -> {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = Amber950,
                            border = BorderStroke(1.3.dp, Amber500)
                        ) {
                            Text(
                                text = "Carton",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                color = Amber300,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                    product.isSoldByPiece -> {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = Yellow950,
                            border = BorderStroke(1.3.dp, Yellow500)
                        ) {
                            Text(
                                text = "1 pc",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                color = Yellow300,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                    else -> {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = Cyan950,
                            border = BorderStroke(1.5.dp, BannerCyan),
                            modifier = Modifier.testTag("product_m2_badge_${product.id}")
                        ) {
                            Text(
                                text = "${formatM2(product.m2Carton)} m²/crt",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onOpenCalculator,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(40.dp)
                        .testTag("btn_calc_${product.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber500,
                        contentColor = OnGold
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Calc", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, maxLines = 1)
                }
                OutlinedButton(
                    onClick = onCheckDepotDispo,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(40.dp)
                        .testTag("btn_dispo_${product.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Cyan950.copy(alpha = 0.55f),
                        contentColor = Cyan300
                    ),
                    border = BorderStroke(1.dp, Cyan500.copy(alpha = 0.7f)),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Warehouse, contentDescription = "Dispo Dépôt", tint = Cyan400, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Dispo", color = Cyan300, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun ProductTileListRow(
    product: TileProduct,
    precomputedDesignation: String? = null,
    isNarrow: Boolean = false,
    onOpenCalculator: () -> Unit,
    onCheckDepotDispo: () -> Unit,
    onPreviewImage: () -> Unit
) {
    val designation = precomputedDesignation ?: remember(product.id, product.name, product.usine, product.dimension, product.finition, product.choix) {
        formatProductDesignation(product)
    }
    val is8x45 = remember(product.dimension) { isPlinthe8x45(product.dimension) }
    val isBag = remember(product.name) { isSoldByBag(product.name) }
    val isJeton = remember(product.id, product.name, product.usine, product.dimension) {
        isJetonProduct(product)
    }
    val finishBadge = remember(product.finition) { formatFinitionLabel(product.finition) }
    val hoverSource = remember { MutableInteractionSource() }
    val isHovered by hoverSource.collectIsHoveredAsState()

    Surface(
        onClick = onOpenCalculator,
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(hoverSource)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        color = when {
            isJeton -> Yellow950
            isHovered -> Slate850
            else -> Slate900
        },
        border = BorderStroke(
            width = if (isJeton || isHovered) 2.dp else 1.2.dp,
            color = if (isJeton || isHovered) BannerGold else Slate800
        ),
        shadowElevation = if (isJeton || isHovered) 5.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isNarrow) 9.dp else 11.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compact color/camera icon — tap ONLY when you want to view the product photo
            CompactPhotoIconButton(
                product = product,
                designation = designation,
                onClick = onPreviewImage
            )

            // Full article specification & prominent metadata strip
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (isJeton) {
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = BannerGold,
                            modifier = Modifier.testTag("jeton_badge_${product.id}")
                        ) {
                            Text(
                                text = "★ JETON",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnGold,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = designation,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = if (isNarrow) 13.5.sp else 14.5.sp,
                            lineHeight = 17.sp
                        ),
                        color = if (isJeton) BannerGold else Color.White,
                        fontWeight = FontWeight.Black,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val dimTheme = remember(product.dimension) { dimensionBadgeTheme(product.dimension) }
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = dimTheme.containerColor,
                        border = BorderStroke(0.9.dp, dimTheme.borderColor)
                    ) {
                        Text(
                            text = product.dimension,
                            style = MaterialTheme.typography.labelSmall,
                            color = dimTheme.textColor,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    val primaryPrice = when {
                        isBag && product.prixPiece > 0.0 -> "${formatDa(product.prixPiece)}/sac"
                        isBag -> "${formatDa(product.prix)}/sac"
                        is8x45 -> "${formatDa(product.prix)}/ctn"
                        product.isSoldByPiece && product.prixPiece > 0.0 -> "${formatDa(product.prixPiece)}/pc"
                        else -> "${formatDa(product.prix)}/m²"
                    }
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Emerald950,
                        border = BorderStroke(0.9.dp, Emerald500.copy(alpha = 0.65f))
                    ) {
                        Text(
                            text = primaryPrice,
                            style = PosNumericTextStyle.copy(fontSize = 12.sp),
                            color = Emerald300,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    when {
                        isBag -> {
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = Amber950,
                                border = BorderStroke(1.dp, Amber500)
                            ) {
                                Text(
                                    text = "Sac",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Amber300,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        is8x45 -> {
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = Amber950,
                                border = BorderStroke(1.dp, Amber500)
                            ) {
                                Text(
                                    text = "Carton",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Amber300,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        product.isSoldByPiece -> {
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = Yellow950,
                                border = BorderStroke(1.dp, Yellow500)
                            ) {
                                Text(
                                    text = "Pièce",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Yellow300,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        else -> {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Cyan950,
                                border = BorderStroke(1.3.dp, BannerCyan),
                                modifier = Modifier.testTag("product_m2_badge_${product.id}")
                            ) {
                                Text(
                                    text = "${formatM2(product.m2Carton)} m²/crt",
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (isJeton) {
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = Amber950,
                            border = BorderStroke(0.9.dp, Amber500)
                        ) {
                            Text(
                                text = "VIERGE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Amber300,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                    } else if (finishBadge.isNotBlank() && !designation.contains(finishBadge, ignoreCase = true)) {
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = Purple950,
                            border = BorderStroke(0.9.dp, Purple500.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = finishBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = Purple300,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                }
            }

            // Compact right-hand actions (Calculer + Dispo)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCheckDepotDispo,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("btn_dispo_${product.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Cyan950.copy(alpha = 0.55f),
                        contentColor = Cyan300
                    ),
                    border = BorderStroke(1.dp, Cyan500.copy(alpha = 0.7f)),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Warehouse, contentDescription = "Dispo Dépôt", tint = Cyan400, modifier = Modifier.size(14.dp))
                    if (!isNarrow) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Dispo", color = Cyan300, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Button(
                    onClick = onOpenCalculator,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("btn_calc_${product.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnGold),
                    contentPadding = PaddingValues(horizontal = if (isNarrow) 8.dp else 10.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = "Calculer", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("+", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

private enum class CartInlinePriceField { UNIT_PRICE, PIECE_PRICE }

@Composable
private fun CartPanelContent(
    cart: List<CartItem>,
    freeLines: List<FreeLineItem>,
    catalogById: Map<String, TileProduct> = emptyMap(),
    totalBrut: Double,
    returnDeductionDa: Double = 0.0,
    onSetReturnDeduction: (Double) -> Unit = {},
    editingInvoice: Invoice?,
    canModifyPrices: Boolean = true,
    onUpdateCartItemDirect: (String, String, Int, Int, Double, Int, Double, Double) -> Unit,
    onAdjustCartItemCarton: (String, Int) -> Unit,
    onAdjustCartItemLoosePiece: (String, Int) -> Unit = { _, _ -> },
    onToggleCartItemCc: (String) -> Unit = {},
    onRemoveCartItem: (String) -> Unit,
    onEditFreeLine: (FreeLineItem) -> Unit,
    onAdjustFreeLineQty: (String, Double) -> Unit,
    onRemoveFreeLine: (String) -> Unit,
    onClearCart: () -> Unit,
    onOpenOneTimeProduct: () -> Unit,
    onOpenDivers: () -> Unit,
    onCloseSheet: (() -> Unit)?,
    onOpenCheckout: () -> Unit,
    heldCartsCount: Int = 0,
    onHoldCart: (() -> Unit)? = null,
    onOpenHeldCarts: (() -> Unit)? = null
) {
    val cartListState = rememberLazyListState()
    var inlineEditItemId by remember { mutableStateOf<String?>(null) }
    var inlineEditField by remember { mutableStateOf(CartInlinePriceField.UNIT_PRICE) }
    var inlineEditValue by remember { mutableStateOf("") }
    var inlineEditPristine by remember { mutableStateOf(true) }

    var editingReturnDeduction by remember { mutableStateOf(false) }
    var returnDraftValue by remember { mutableStateOf("") }
    var returnDraftPristine by remember { mutableStateOf(true) }

    val activeInlineItem = remember(cart, inlineEditItemId) {
        if (inlineEditItemId == null) null else cart.find { it.id == inlineEditItemId }
    }
    if (inlineEditItemId != null && activeInlineItem == null) {
        inlineEditItemId = null
    }

    fun startReturnDeductionEdit() {
        inlineEditItemId = null
        editingReturnDeduction = true
        returnDraftValue = if (returnDeductionDa > 0.0) kotlin.math.round(returnDeductionDa).toInt().toString() else ""
        returnDraftPristine = true
    }

    fun applyReturnDeductionEdit() {
        val parsed = (returnDraftValue.trim().replace(',', '.').toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
        onSetReturnDeduction(parsed)
        editingReturnDeduction = false
    }

    fun startInlineEdit(item: CartItem, field: CartInlinePriceField) {
        editingReturnDeduction = false
        val currentVal = when (field) {
            CartInlinePriceField.UNIT_PRICE -> {
                if (item.isSoldByPiece && item.cartons == 0 && item.prixPiece > 0.0) item.prixPiece
                else item.prixUnitaire
            }
            CartInlinePriceField.PIECE_PRICE -> item.effectiveLoosePieceRate
        }
        inlineEditItemId = item.id
        inlineEditField = field
        inlineEditValue = if (currentVal % 1.0 == 0.0) currentVal.toInt().toString() else fmtClean(currentVal)
        inlineEditPristine = true
    }

    fun applyInlineEdit() {
        val item = activeInlineItem ?: return
        val parsed = (inlineEditValue.trim().replace(',', '.').toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
        val isPieceOnlyRow = item.isSoldByPiece && item.cartons == 0
        when (inlineEditField) {
            CartInlinePriceField.UNIT_PRICE -> {
                if (isPieceOnlyRow) {
                    onUpdateCartItemDirect(
                        item.id, item.name, item.cartons, item.pieces,
                        item.m2Carton, item.pcs, parsed, parsed
                    )
                } else {
                    onUpdateCartItemDirect(
                        item.id, item.name, item.cartons, item.pieces,
                        item.m2Carton, item.pcs, parsed, item.prixPiece
                    )
                }
            }
            CartInlinePriceField.PIECE_PRICE -> {
                onUpdateCartItemDirect(
                    item.id, item.name, item.cartons, item.pieces,
                    item.m2Carton, item.pcs, item.prixUnitaire, parsed
                )
            }
        }
        inlineEditItemId = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("panier_panel_content"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Header inside Panier
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Navy950
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (editingInvoice != null) "Édition #${editingInvoice.reference}" else "Contenu du Panier",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnDark,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${cart.size + freeLines.size} article(s) • ${cart.sumOf { it.cartons }} cartons • ${formatM2(cart.sumOf { it.m2Total })} m²",
                        style = MaterialTheme.typography.labelSmall,
                        color = BannerGold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (heldCartsCount > 0 && onOpenHeldCarts != null) {
                        Surface(
                            onClick = onOpenHeldCarts,
                            shape = RoundedCornerShape(9.dp),
                            color = Cyan950,
                            border = BorderStroke(1.dp, Cyan400),
                            modifier = Modifier.height(38.dp).testTag("panier_open_held_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text("Att. ($heldCartsCount)", style = MaterialTheme.typography.labelSmall, color = Cyan300, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    if ((cart.isNotEmpty() || freeLines.isNotEmpty()) && onHoldCart != null) {
                        Surface(
                            onClick = {
                                inlineEditItemId = null
                                onHoldCart()
                            },
                            shape = RoundedCornerShape(9.dp),
                            color = Amber950,
                            border = BorderStroke(1.dp, Amber400),
                            modifier = Modifier.height(38.dp).testTag("pos_hold_cart_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = "Mettre en attente", tint = Amber300, modifier = Modifier.size(14.dp))
                                Text("Attente", style = MaterialTheme.typography.labelSmall, color = Amber300, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    if (cart.isNotEmpty() || freeLines.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                inlineEditItemId = null
                                onClearCart()
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Vider panier", tint = Rose400)
                        }
                    }
                    if (onCloseSheet != null) {
                        Button(
                            onClick = onCloseSheet,
                            modifier = Modifier.height(40.dp).testTag("panier_close_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer panier", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FERMER", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Quick Add Inside Panier
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (onCloseSheet != null) {
                OutlinedButton(
                    onClick = onCloseSheet,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.3.dp, Cyan500),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Cyan400, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Catalogue", color = Cyan400, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
            }
            Button(
                onClick = onOpenOneTimeProduct,
                modifier = Modifier
                    .weight(1.1f)
                    .height(38.dp)
                    .testTag("panier_add_onetime_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnGold),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Article Libre", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
            Button(
                onClick = onOpenDivers,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .testTag("panier_add_accessory_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnGold),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Accessoire", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }

        HorizontalDivider(color = Slate800)

        if (cart.isEmpty() && freeLines.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Slate400, modifier = Modifier.size(52.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Votre panier est vide", color = Slate100, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Ajoutez du catalogue, un Article Libre ou un Accessoire", color = Slate400, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                state = cartListState,
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
                    .testTag("panier_items_list"),
                contentPadding = PaddingValues(top = 2.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(cart, key = { index, item -> "${item.id.ifBlank { "c" }}_$index" }) { _, item ->
                    val isOneTime = item.productId.startsWith("onetime_")
                    val isPieceOnlyRow = item.isSoldByPiece && item.cartons == 0
                    val isBagRow = isSoldByBag(item.name)
                    val is8x45Row = isPlinthe8x45(item.dimension)
                    val isOneTimeExactM2Row = isOneTime && !item.isSoldByPiece && item.cartons == 0 && item.pieces == 0 && item.m2Total > 0.0
                    val canAddLoosePieces = !isPieceOnlyRow && !is8x45Row && !isOneTimeExactM2Row && item.cartons > 0
                    val catalogProd = catalogById[item.productId]
                    val itemDesignation = formatCartItemDesignation(item, catalogProd)
                    val isCcActive = item.isCc || (
                        !item.choix.equals("1er Choix", ignoreCase = true) &&
                            itemDesignation.contains(Regex("""(?:^|\s|•)CC(?:\s|$)"""))
                        )
                    val canEditThisPrice = canModifyPrices || isOneTime
                    val isEditingUnitPrice = inlineEditItemId == item.id && inlineEditField == CartInlinePriceField.UNIT_PRICE
                    val isEditingPiecePrice = inlineEditItemId == item.id && inlineEditField == CartInlinePriceField.PIECE_PRICE
                    val priceUnitSuffix = when {
                        isBagRow -> "/sac"
                        isPieceOnlyRow -> "/pc"
                        is8x45Row -> "/ctn"
                        else -> "/m²"
                    }
                    val displayUnitPrice = if (isPieceOnlyRow && item.prixPiece > 0.0) item.prixPiece else item.prixUnitaire

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("panier_cart_item_${item.id}"),
                        shape = RoundedCornerShape(15.dp),
                        color = Slate950,
                        border = BorderStroke(
                            if (isEditingUnitPrice || isEditingPiecePrice) 2.dp else 1.4.dp,
                            when {
                                isEditingUnitPrice || isEditingPiecePrice -> Emerald400
                                isCcActive -> Amber500
                                isOneTime -> Cyan500
                                item.isSoldByPiece -> Yellow500.copy(alpha = 0.7f)
                                else -> Slate700
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            // Row 1: Designation + CC + Delete (clean, zero calculator redirect)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            text = itemDesignation,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Slate100,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.weight(1f, fill = false),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isOneTime) {
                                            Surface(
                                                shape = RoundedCornerShape(5.dp),
                                                color = Cyan950,
                                                border = BorderStroke(1.dp, Cyan500)
                                            ) {
                                                Text(
                                                    text = "LIBRE",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Cyan300,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = when {
                                            isBagRow -> "${item.pieces.coerceAtLeast(item.cartons)} sac(s) • Vente au sac"
                                            isPieceOnlyRow -> "${item.pieces} pcs • Vente à la pièce"
                                            is8x45Row -> "${item.cartons} carton(s) • Plinthe 8/45"
                                            isOneTimeExactM2Row -> "${formatM2(item.m2Total)} m² exact"
                                            else -> "${formatM2(item.m2Total)} m² (${formatM2(item.m2Carton)} m²/crt)"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Cyan300,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .clickable { onToggleCartItemCc(item.id) }
                                        .testTag("cart_cc_btn_${item.id}"),
                                    shape = RoundedCornerShape(11.dp),
                                    color = if (isCcActive) Amber500 else Slate900,
                                    border = BorderStroke(1.4.dp, if (isCcActive) Amber300 else Slate700)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "CC",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isCcActive) OnGold else Slate200,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .clickable {
                                            if (inlineEditItemId == item.id) inlineEditItemId = null
                                            onRemoveCartItem(item.id)
                                        }
                                        .testTag("cart_remove_btn_${item.id}"),
                                    shape = RoundedCornerShape(11.dp),
                                    color = Rose500,
                                    border = BorderStroke(1.4.dp, Rose300.copy(alpha = 0.8f)),
                                    shadowElevation = 2.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer article", tint = OnDark, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            // Row 2: Qty (- / +) + Direct Inline Price Tap Box + Line Subtotal
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .clickable { onAdjustCartItemCarton(item.id, -1) }
                                            .testTag("cart_qty_minus_${item.id}"),
                                        color = Rose950,
                                        shape = RoundedCornerShape(11.dp),
                                        border = BorderStroke(1.3.dp, Rose500)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Remove, contentDescription = "Diminuer quantité", tint = Rose300, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Text(
                                        text = when {
                                            isBagRow -> "${item.pieces.coerceAtLeast(item.cartons)} sac"
                                            isPieceOnlyRow -> "${item.pieces} pcs"
                                            isOneTimeExactM2Row -> "${formatM2(item.m2Total)} m²"
                                            is8x45Row -> "${item.cartons} ctn"
                                            else -> "${item.cartons} ctn"
                                        },
                                        style = PosNumericTextStyle.copy(fontSize = 14.sp),
                                        color = if (item.isSoldByPiece) Yellow400 else Amber400
                                    )
                                    Surface(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .clickable { onAdjustCartItemCarton(item.id, 1) }
                                            .testTag("cart_qty_plus_${item.id}"),
                                        color = Emerald950,
                                        shape = RoundedCornerShape(11.dp),
                                        border = BorderStroke(1.3.dp, Emerald500)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Add, contentDescription = "Augmenter quantité", tint = Emerald300, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }

                                // Direct tappable price pill — edits inline inside Panier!
                                Surface(
                                    onClick = { if (canEditThisPrice) startInlineEdit(item, CartInlinePriceField.UNIT_PRICE) },
                                    enabled = canEditThisPrice,
                                    modifier = Modifier
                                        .heightIn(min = 42.dp)
                                        .testTag("cart_inline_price_btn_${item.id}"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isEditingUnitPrice) Emerald950 else Slate900,
                                    border = BorderStroke(
                                        if (isEditingUnitPrice) 2.dp else 1.2.dp,
                                        when {
                                            isEditingUnitPrice -> Emerald400
                                            canEditThisPrice -> Amber500.copy(alpha = 0.75f)
                                            else -> Slate800
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${formatDa(displayUnitPrice)}$priceUnitSuffix",
                                            style = PosNumericTextStyle.copy(fontSize = 13.sp),
                                            color = if (isEditingUnitPrice) Emerald300 else Amber300,
                                            maxLines = 1
                                        )
                                        if (canEditThisPrice) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Modifier le prix",
                                                tint = if (isEditingUnitPrice) Emerald300 else Amber400,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = formatDa(item.sousTotal),
                                    style = PosNumericTextStyle.copy(fontSize = 15.sp),
                                    color = Emerald400,
                                    maxLines = 1
                                )
                            }

                            // Row 3: Independent Loose Pieces (0..5) + Direct Inline Piece Price Tap Box
                            if (canAddLoosePieces) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("cart_loose_pieces_row_${item.id}"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Slate900,
                                    border = BorderStroke(
                                        1.2.dp,
                                        when {
                                            isEditingPiecePrice -> Emerald400
                                            item.pieces > 0 -> Yellow500.copy(alpha = 0.75f)
                                            else -> Slate800
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Surface(
                                                onClick = { onAdjustCartItemLoosePiece(item.id, -1) },
                                                enabled = item.pieces > 0,
                                                modifier = Modifier
                                                    .size(width = 36.dp, height = 34.dp)
                                                    .testTag("cart_loose_piece_minus_${item.id}"),
                                                color = if (item.pieces > 0) Rose950 else Slate850,
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, if (item.pieces > 0) Rose500 else Slate700)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        Icons.Default.Remove,
                                                        contentDescription = "Retirer 1 pièce",
                                                        tint = if (item.pieces > 0) Rose300 else Slate400,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = if (item.pieces > 0) "+${item.pieces} pc" else "0/5 pc",
                                                style = PosNumericTextStyle.copy(fontSize = 13.sp),
                                                color = if (item.pieces > 0) Yellow300 else Slate400
                                            )
                                            Surface(
                                                onClick = { onAdjustCartItemLoosePiece(item.id, 1) },
                                                enabled = item.pieces < 5,
                                                modifier = Modifier
                                                    .size(width = 36.dp, height = 34.dp)
                                                    .testTag("cart_loose_piece_plus_${item.id}"),
                                                color = if (item.pieces < 5) Yellow950 else Slate850,
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, if (item.pieces < 5) Yellow500 else Slate700)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        contentDescription = "Ajouter 1 pièce (max 5)",
                                                        tint = if (item.pieces < 5) Yellow300 else Slate400,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        if (item.pieces > 0) {
                                            Surface(
                                                onClick = { if (canEditThisPrice) startInlineEdit(item, CartInlinePriceField.PIECE_PRICE) },
                                                enabled = canEditThisPrice,
                                                modifier = Modifier.testTag("cart_inline_piece_price_btn_${item.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isEditingPiecePrice) Emerald950 else Slate850,
                                                border = BorderStroke(
                                                    1.2.dp,
                                                    if (isEditingPiecePrice) Emerald400 else Yellow500.copy(alpha = 0.7f)
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Text(
                                                        text = "${formatDa(item.effectiveLoosePieceRate)}/pc",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isEditingPiecePrice) Emerald300 else Yellow300,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    if (canEditThisPrice) {
                                                        Icon(
                                                            Icons.Default.Edit,
                                                            contentDescription = "Modifier prix pièce",
                                                            tint = Yellow300,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = "Pièces au détail (hors m²)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Slate400,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                itemsIndexed(freeLines, key = { index, line -> "${line.id.ifBlank { "f" }}_$index" }) { _, line ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp))
                            .clickable { onEditFreeLine(line) }
                            .testTag("panier_free_item_${line.id}"),
                        shape = RoundedCornerShape(15.dp),
                        color = Slate950,
                        border = BorderStroke(1.4.dp, Amber500)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(line.designation, style = MaterialTheme.typography.titleSmall, color = Slate100, fontWeight = FontWeight.Black)
                                    Text(
                                        text = "${fmtClean(line.quantite)} ${line.unite} × ${formatDa(line.prixUnitaire)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Amber300,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .clickable { onEditFreeLine(line) }
                                            .testTag("free_edit_btn_${line.id}"),
                                        shape = RoundedCornerShape(11.dp),
                                        color = Amber950,
                                        border = BorderStroke(1.4.dp, Amber500)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Edit, contentDescription = "Modifier article divers", tint = Amber300, modifier = Modifier.size(19.dp))
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .clickable { onRemoveFreeLine(line.id) }
                                            .testTag("free_remove_btn_${line.id}"),
                                        shape = RoundedCornerShape(11.dp),
                                        color = Rose500,
                                        border = BorderStroke(1.4.dp, Rose300.copy(alpha = 0.8f)),
                                        shadowElevation = 2.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Delete, contentDescription = "Supprimer article divers", tint = OnDark, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .clickable { onAdjustFreeLineQty(line.id, -1.0) },
                                        color = Rose950,
                                        shape = RoundedCornerShape(11.dp),
                                        border = BorderStroke(1.3.dp, Rose500)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Remove, contentDescription = "Diminuer quantité", tint = Rose300, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Text(
                                        text = "${fmtClean(line.quantite)} ${line.unite}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Amber400,
                                        fontWeight = FontWeight.Black
                                    )
                                    Surface(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .clickable { onAdjustFreeLineQty(line.id, 1.0) },
                                        color = Emerald950,
                                        shape = RoundedCornerShape(11.dp),
                                        border = BorderStroke(1.3.dp, Emerald500)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Add, contentDescription = "Augmenter quantité", tint = Emerald300, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = formatDa(line.sousTotal),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Emerald400,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // Inline Price / Piece-Price Docked Editor (edits directly inside Panier — no modal/calculator jump!)
        AnimatedVisibility(
            visible = activeInlineItem != null,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            activeInlineItem?.let { editItem ->
                val isPieceMode = inlineEditField == CartInlinePriceField.PIECE_PRICE || (editItem.isSoldByPiece && editItem.cartons == 0)
                val editLabel = if (isPieceMode) "Prix / Pièce (DA)" else if (isPlinthe8x45(editItem.dimension)) "Prix / Carton (DA)" else "Prix / m² (DA)"
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cart_inline_price_editor"),
                    shape = RoundedCornerShape(14.dp),
                    color = Slate950,
                    border = BorderStroke(1.8.dp, Emerald500)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = editLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Emerald300,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "${inlineEditValue.ifBlank { "0" }} DA",
                                    style = PosNumericTextStyle.copy(fontSize = 18.sp),
                                    color = BannerGold,
                                    modifier = Modifier.testTag("cart_inline_price_value")
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { inlineEditItemId = null },
                                    modifier = Modifier.height(38.dp).testTag("cart_inline_price_cancel_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.2.dp, Slate700),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Annuler", tint = Slate200, modifier = Modifier.size(16.dp))
                                }
                                Button(
                                    onClick = { applyInlineEdit() },
                                    modifier = Modifier.height(38.dp).testTag("cart_inline_price_apply_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Valider prix", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("OK", fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        NonImeNumericKeypad(
                            onKeyPress = { key ->
                                val (nextVal, nextPristine) = applyKeypadInput(
                                    current = inlineEditValue,
                                    key = key,
                                    allowDecimal = true,
                                    isPristine = inlineEditPristine
                                )
                                inlineEditValue = nextVal
                                inlineEditPristine = nextPristine
                            },
                            allowDecimal = true,
                            quickChips = listOf("+50" to 50.0, "+100" to 100.0, "-50" to -50.0),
                            onQuickDelta = { delta ->
                                inlineEditValue = applyKeypadQuickDelta(
                                    current = inlineEditValue,
                                    delta = delta,
                                    allowDecimal = true
                                )
                                inlineEditPristine = false
                            },
                            compact = true,
                            ultraCompact = true,
                            onValidate = { applyInlineEdit() }
                        )
                    }
                }
            }
        }

        // Inline Déduction Retour (DA) Docked Keypad (single numeric deduction for returned items)
        AnimatedVisibility(
            visible = editingReturnDeduction,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cart_return_deduction_editor"),
                shape = RoundedCornerShape(14.dp),
                color = Slate950,
                border = BorderStroke(1.8.dp, Rose500)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Déduction Retour Client (DA)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Rose300,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "- ${returnDraftValue.ifBlank { "0" }} DA",
                                style = PosNumericTextStyle.copy(fontSize = 18.sp),
                                color = Rose400,
                                modifier = Modifier.testTag("cart_return_deduction_value")
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    onSetReturnDeduction(0.0)
                                    editingReturnDeduction = false
                                },
                                modifier = Modifier.height(38.dp).testTag("cart_return_deduction_clear_btn"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.2.dp, Slate700),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("0 DA", color = Slate200, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { applyReturnDeductionEdit() },
                                modifier = Modifier.height(38.dp).testTag("cart_return_deduction_apply_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Valider déduction retour", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("OK", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    NonImeNumericKeypad(
                        onKeyPress = { key ->
                            val (nextVal, nextPristine) = applyKeypadInput(
                                current = returnDraftValue,
                                key = key,
                                allowDecimal = false,
                                isPristine = returnDraftPristine
                            )
                            returnDraftValue = nextVal
                            returnDraftPristine = nextPristine
                        },
                        allowDecimal = false,
                        quickChips = listOf("+500" to 500.0, "+1000" to 1000.0, "+5000" to 5000.0),
                        onQuickDelta = { delta ->
                            returnDraftValue = applyKeypadQuickDelta(
                                current = returnDraftValue,
                                delta = delta,
                                allowDecimal = false
                            )
                            returnDraftPristine = false
                        },
                        compact = true,
                        ultraCompact = true,
                        onValidate = { applyReturnDeductionEdit() }
                    )
                }
            }
        }

        HorizontalDivider(color = Slate800)

        // Pinned Bottom Summary, Déduction Retour & Conclure la vente CTA (always 100% visible)
        val effectiveNetInCart = (totalBrut - returnDeductionDa).coerceAtLeast(0.0)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (cart.isNotEmpty() || freeLines.isNotEmpty()) {
                Surface(
                    onClick = {
                        if (editingReturnDeduction) applyReturnDeductionEdit() else startReturnDeductionEdit()
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (returnDeductionDa > 0.01) Rose950 else Slate950,
                    border = BorderStroke(
                        1.2.dp,
                        if (editingReturnDeduction || returnDeductionDa > 0.01) Rose500 else Slate700
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("panier_return_deduction_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = null,
                                tint = if (returnDeductionDa > 0.01) Rose300 else Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (returnDeductionDa > 0.01) "Déduction Retour" else "Déduire un retour (DA)",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (returnDeductionDa > 0.01) Rose300 else Slate200,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (returnDeductionDa > 0.01) "-${formatDa(returnDeductionDa)}" else "0 DA",
                                style = PosNumericTextStyle.copy(fontSize = 13.5.sp),
                                color = if (returnDeductionDa > 0.01) Rose400 else Slate400
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Saisir déduction retour",
                                tint = if (returnDeductionDa > 0.01) Rose300 else Amber400,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (returnDeductionDa > 0.01) "Net à Payer" else "Total Brut",
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate200,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatDa(effectiveNetInCart),
                    style = PosNumericTextStyle.copy(fontSize = 21.sp),
                    color = Emerald400
                )
            }

            Button(
                onClick = {
                    if (activeInlineItem != null) applyInlineEdit()
                    if (editingReturnDeduction) applyReturnDeductionEdit()
                    onOpenCheckout()
                },
                enabled = cart.isNotEmpty() || freeLines.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("pos_checkout_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
            ) {
                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (editingInvoice != null) "Mettre à jour la facture" else "Conclure la vente • ${formatDa(effectiveNetInCart)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private enum class QuickCartEditTarget(val allowDecimal: Boolean) {
    CARTONS(false),
    PIECES(false),
    M2_CARTON(true),
    PRICE(true),
    PRICE_PIECE(true)
}

@Composable
private fun EditCartItemQuickDialog(
    item: CartItem,
    canModifyPrices: Boolean = true,
    onDismiss: () -> Unit,
    onOpenFullCalculator: () -> Unit,
    onSave: (name: String, cartons: Int, pieces: Int, m2Carton: Double, pcs: Int, prix: Double, prixPiece: Double, pricingUnit: String) -> Unit
) {
    BackHandler { onDismiss() }
    val isOneTime = item.productId.startsWith("onetime_")
    val canEditPrice = canModifyPrices || isOneTime
    val is120x60Item = isPieceEligibleTileDimension(item.dimension)
    val is8x60Item = isPlinthe8x60(item.dimension)
    val is8x45Item = isPlinthe8x45(item.dimension)

    var activeUnit by remember(item.id) {
        mutableStateOf(
            when {
                is8x60Item -> "piece"
                is8x45Item -> "carton"
                else -> item.pricingUnit
            }
        )
    }
    val isPieceOnlyMode = !is8x45Item && (is8x60Item || (activeUnit == "piece" && (!is120x60Item || item.cartons == 0)))

    var nameStr by remember { mutableStateOf(item.name) }
    var cartonsStr by remember { mutableStateOf(item.cartons.toString()) }
    var piecesStr by remember { mutableStateOf(item.pieces.toString()) }
    var m2CartonStr by remember { mutableStateOf(fmtClean(item.m2Carton)) }
    var pcsStr by remember { mutableStateOf(item.pcs.coerceAtLeast(1).toString()) }
    var priceStr by remember {
        mutableStateOf(
            if (item.prixUnitaire % 1.0 == 0.0) item.prixUnitaire.toInt().toString()
            else item.prixUnitaire.toString()
        )
    }
    val initialPiecePrice = if (item.prixPiece > 0.0) item.prixPiece else ((item.m2Carton / item.pcs.coerceAtLeast(1)) * item.prixUnitaire)
    var pricePieceStr by remember {
        mutableStateOf(
            if (initialPiecePrice % 1.0 == 0.0) initialPiecePrice.toInt().toString()
            else ((initialPiecePrice * 100.0).roundToInt() / 100.0).toString()
        )
    }

    var activeTarget by remember {
        mutableStateOf(if (isPieceOnlyMode || activeUnit == "piece") QuickCartEditTarget.PIECES else QuickCartEditTarget.CARTONS)
    }
    var showKeypad by remember { mutableStateOf(true) }
    var showAlphaKeypad by remember { mutableStateOf(false) }
    var isPristine by remember { mutableStateOf(true) }
    var isAlphaPristine by remember { mutableStateOf(false) }

    val cartonsVal = if (isPieceOnlyMode) 0 else (cartonsStr.toIntOrNull() ?: 0).coerceAtLeast(0)
    val rawPiecesVal = (piecesStr.toIntOrNull() ?: 0).coerceAtLeast(0)
    val piecesVal = when {
        is8x45Item -> 0
        isPieceOnlyMode -> rawPiecesVal
        cartonsVal > 0 -> rawPiecesVal.coerceIn(0, 5)
        else -> rawPiecesVal
    }
    val m2CtnVal = if (is8x45Item) 1.0 else (m2CartonStr.toDoubleOrNull() ?: item.m2Carton).coerceAtLeast(0.01)
    val priceVal = (priceStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val pricePieceVal = if (is8x45Item) 0.0 else (pricePieceStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val safePcs = (pcsStr.toIntOrNull() ?: item.pcs).coerceAtLeast(1)

    val (previewM2, previewTotal) = when {
        is8x45Item -> 0.0 to (((cartonsVal * priceVal) * 100.0).roundToInt() / 100.0)
        isPieceOnlyMode || (activeUnit == "piece" && cartonsVal == 0) -> {
            0.0 to (((piecesVal * pricePieceVal) * 100.0).roundToInt() / 100.0)
        }
        else -> TileCalculatorMath.calculateCartItemTotals(
            cartons = cartonsVal,
            pieces = piecesVal,
            m2PerCarton = m2CtnVal,
            pcsPerCarton = safePcs,
            prixPerM2 = priceVal,
            prixParPiece = pricePieceVal
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        SuppressSystemSoftKeyboard {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .fillMaxHeight(0.92f)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            shape = RoundedCornerShape(22.dp),
            color = Slate900,
            border = BorderStroke(1.5.dp, Slate700)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .testTag("quick_edit_cart_dialog"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(shape = RoundedCornerShape(14.dp), color = Navy950, border = BorderStroke(1.dp, Navy800)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Modifier Article du Panier", style = MaterialTheme.typography.titleMedium, color = BannerGold, fontWeight = FontWeight.Black)
                            Text(item.name, style = MaterialTheme.typography.bodySmall, color = OnDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FERMER", fontWeight = FontWeight.Black)
                        }
                    }
                }

                // Scrollable form body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                if (is120x60Item) {
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("quick_edit_120x60_unit_toggle"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = activeUnit == "piece",
                            onClick = {
                                activeUnit = "piece"
                                activeTarget = QuickCartEditTarget.PIECES
                                showKeypad = true
                                showAlphaKeypad = false
                            },
                            label = { Text("À la Pièce", fontWeight = FontWeight.Black) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Yellow500,
                                selectedLabelColor = OnGold,
                                containerColor = Slate950,
                                labelColor = Slate200
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = activeUnit == "carton",
                            onClick = {
                                activeUnit = "carton"
                                pcsStr = "2"
                                if ((cartonsStr.toIntOrNull() ?: 0) == 0) cartonsStr = "1"
                                activeTarget = QuickCartEditTarget.CARTONS
                                showKeypad = true
                                showAlphaKeypad = false
                            },
                            label = { Text("Carton (${fmtClean(m2CtnVal)} m²/c)", fontWeight = FontWeight.Black) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber500,
                                selectedLabelColor = OnGold,
                                containerColor = Slate950,
                                labelColor = Slate200
                            ),
                            modifier = Modifier.weight(1.25f)
                        )
                    }
                }

                if (isOneTime) {
                    ClearableOutlinedTextField(
                        value = nameStr,
                        onValueChange = { nameStr = it },
                        label = { Text("Nom du produit") },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                if (it.isFocused) {
                                    showAlphaKeypad = true
                                    showKeypad = false
                                    isAlphaPristine = false
                                }
                            }
                    )
                }

                if (isPieceOnlyMode) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        NumericDisplayField(
                            label = "Pièces",
                            value = piecesStr,
                            isActive = showKeypad && activeTarget == QuickCartEditTarget.PIECES,
                            onSelect = {
                                activeTarget = QuickCartEditTarget.PIECES
                                isPristine = true
                                showKeypad = true
                                showAlphaKeypad = false
                            },
                            onClear = { piecesStr = ""; isPristine = false },
                            suffix = "pcs",
                            accentColor = Yellow500,
                            compact = true,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_edit_pieces_field"
                        )
                        NumericDisplayField(
                            label = if (canEditPrice) "Prix / Pièce" else "Prix / Pcs (Fixe)",
                            value = pricePieceStr,
                            isActive = canEditPrice && showKeypad && activeTarget == QuickCartEditTarget.PRICE_PIECE,
                            onSelect = {
                                if (canEditPrice) {
                                    activeTarget = QuickCartEditTarget.PRICE_PIECE
                                    isPristine = true
                                    showKeypad = true
                                    showAlphaKeypad = false
                                }
                            },
                            onClear = if (canEditPrice) ({ pricePieceStr = ""; isPristine = false }) else null,
                            enabled = canEditPrice,
                            suffix = "DA/p",
                            accentColor = Emerald500,
                            compact = true,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_edit_price_piece_field"
                        )
                    }
                } else if (is8x45Item) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        NumericDisplayField(
                            label = "Cartons (8/45)",
                            value = cartonsStr,
                            isActive = showKeypad && activeTarget == QuickCartEditTarget.CARTONS,
                            onSelect = {
                                activeTarget = QuickCartEditTarget.CARTONS
                                isPristine = true
                                showKeypad = true
                                showAlphaKeypad = false
                            },
                            onClear = { cartonsStr = ""; isPristine = false },
                            suffix = "ctn",
                            accentColor = Amber500,
                            compact = true,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_edit_cartons_field"
                        )
                        NumericDisplayField(
                            label = if (canEditPrice) "Prix / Carton" else "Prix / Ctn (Fixe)",
                            value = priceStr,
                            isActive = canEditPrice && showKeypad && activeTarget == QuickCartEditTarget.PRICE,
                            onSelect = {
                                if (canEditPrice) {
                                    activeTarget = QuickCartEditTarget.PRICE
                                    isPristine = true
                                    showKeypad = true
                                    showAlphaKeypad = false
                                }
                            },
                            onClear = if (canEditPrice) ({ priceStr = ""; isPristine = false }) else null,
                            enabled = canEditPrice,
                            suffix = "DA/c",
                            accentColor = Emerald500,
                            compact = true,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_edit_price_field"
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        NumericDisplayField(
                            label = "Cartons",
                            value = cartonsStr,
                            isActive = showKeypad && activeTarget == QuickCartEditTarget.CARTONS,
                            onSelect = {
                                activeTarget = QuickCartEditTarget.CARTONS
                                isPristine = true
                                showKeypad = true
                                showAlphaKeypad = false
                            },
                            onClear = { cartonsStr = ""; isPristine = false },
                            suffix = "ctn",
                            accentColor = Amber500,
                            compact = true,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_edit_cartons_field"
                        )
                        NumericDisplayField(
                            label = "m² / Ctn",
                            value = m2CartonStr,
                            isActive = showKeypad && activeTarget == QuickCartEditTarget.M2_CARTON,
                            onSelect = {
                                activeTarget = QuickCartEditTarget.M2_CARTON
                                isPristine = true
                                showKeypad = true
                                showAlphaKeypad = false
                            },
                            onClear = { m2CartonStr = ""; isPristine = false },
                            suffix = "m²",
                            accentColor = Cyan500,
                            compact = true,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_edit_m2ctn_field"
                        )
                        NumericDisplayField(
                            label = if (canEditPrice) "Prix / m²" else "Prix / m²",
                            value = priceStr,
                            isActive = canEditPrice && showKeypad && activeTarget == QuickCartEditTarget.PRICE,
                            onSelect = {
                                if (canEditPrice) {
                                    activeTarget = QuickCartEditTarget.PRICE
                                    isPristine = true
                                    showKeypad = true
                                    showAlphaKeypad = false
                                }
                            },
                            onClear = if (canEditPrice) ({ priceStr = ""; isPristine = false }) else null,
                            enabled = canEditPrice,
                            suffix = "DA",
                            accentColor = Emerald500,
                            compact = true,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_edit_price_field"
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        NumericDisplayField(
                            label = "Pièces (max 5)",
                            value = piecesVal.toString(),
                            isActive = showKeypad && activeTarget == QuickCartEditTarget.PIECES,
                            onSelect = {
                                activeTarget = QuickCartEditTarget.PIECES
                                isPristine = true
                                showKeypad = true
                                showAlphaKeypad = false
                            },
                            onClear = { piecesStr = "0"; isPristine = false },
                            suffix = "pcs",
                            accentColor = Yellow500,
                            compact = true,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_edit_pieces_field"
                        )
                        NumericDisplayField(
                            label = if (canEditPrice) "Prix / Pièce" else "Prix / Pcs (Fixe)",
                            value = pricePieceStr,
                            isActive = canEditPrice && showKeypad && activeTarget == QuickCartEditTarget.PRICE_PIECE,
                            onSelect = {
                                if (canEditPrice) {
                                    activeTarget = QuickCartEditTarget.PRICE_PIECE
                                    isPristine = true
                                    showKeypad = true
                                    showAlphaKeypad = false
                                }
                            },
                            onClear = if (canEditPrice) ({ pricePieceStr = ""; isPristine = false }) else null,
                            enabled = canEditPrice,
                            suffix = "DA/p",
                            accentColor = Yellow500,
                            compact = true,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_edit_price_piece_field"
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Navy950,
                    border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    is8x45Item -> "$cartonsVal cartons"
                                    isPieceOnlyMode -> "$piecesVal pièces"
                                    else -> "${formatM2(previewM2)} m² cartons"
                                },
                                color = OnDark,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = when {
                                    is8x45Item -> "${formatDa(priceVal)} / carton"
                                    isPieceOnlyMode -> "${formatDa(pricePieceVal)} / pièce"
                                    else -> "${formatDa(priceVal * m2CtnVal)} / carton" + if (piecesVal > 0 && pricePieceVal > 0.0) " • ↳ +${piecesVal}p (${formatDa(piecesVal * pricePieceVal)})" else ""
                                },
                                color = BannerGold,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(formatDa(previewTotal), color = BannerEmerald, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    }
                }

                AnimatedVisibility(visible = showAlphaKeypad && isOneTime, enter = expandVertically(), exit = shrinkVertically()) {
                    NonImeAlphanumericKeypad(
                        onKeyPress = { key ->
                            val (next, nextPristine) = applyAlphanumericKeypadInput(nameStr, key, isAlphaPristine, uppercase = true)
                            nameStr = next
                            isAlphaPristine = nextPristine
                        },
                        onClear = { nameStr = ""; isAlphaPristine = false },
                        onValidate = { showAlphaKeypad = false },
                        validateLabel = "OK"
                    )
                }

                AnimatedVisibility(visible = showKeypad, enter = expandVertically(), exit = shrinkVertically()) {
                    val curVal = when (activeTarget) {
                        QuickCartEditTarget.CARTONS -> cartonsStr
                        QuickCartEditTarget.PIECES -> piecesStr
                        QuickCartEditTarget.M2_CARTON -> m2CartonStr
                        QuickCartEditTarget.PRICE -> priceStr
                        QuickCartEditTarget.PRICE_PIECE -> pricePieceStr
                    }
                    NonImeNumericKeypad(
                        onKeyPress = { key ->
                            val (next, nextPristine) = applyKeypadInput(curVal, key, activeTarget.allowDecimal, isPristine)
                            when (activeTarget) {
                                QuickCartEditTarget.CARTONS -> cartonsStr = next
                                QuickCartEditTarget.PIECES -> piecesStr = next
                                QuickCartEditTarget.M2_CARTON -> m2CartonStr = next
                                QuickCartEditTarget.PRICE -> priceStr = next
                                QuickCartEditTarget.PRICE_PIECE -> pricePieceStr = next
                            }
                            isPristine = nextPristine
                        },
                        allowDecimal = activeTarget.allowDecimal,
                        quickChips = when (activeTarget) {
                            QuickCartEditTarget.CARTONS -> listOf("+1" to 1.0, "+5" to 5.0, "+10" to 10.0)
                            QuickCartEditTarget.PIECES -> listOf("+1" to 1.0, "+5" to 5.0, "+10" to 10.0)
                            QuickCartEditTarget.PRICE, QuickCartEditTarget.PRICE_PIECE -> listOf("+50" to 50.0, "+100" to 100.0, "-50" to -50.0)
                            else -> emptyList()
                        },
                        onQuickDelta = { delta ->
                            val next = applyKeypadQuickDelta(curVal, delta, activeTarget.allowDecimal)
                            when (activeTarget) {
                                QuickCartEditTarget.CARTONS -> cartonsStr = next
                                QuickCartEditTarget.PIECES -> piecesStr = next
                                QuickCartEditTarget.M2_CARTON -> m2CartonStr = next
                                QuickCartEditTarget.PRICE -> priceStr = next
                                QuickCartEditTarget.PRICE_PIECE -> pricePieceStr = next
                            }
                            isPristine = false
                        },
                        compact = true,
                        ultraCompact = true,
                        onValidate = null
                    )
                }
                } // End of scrollable form body

                // Pinned Footer (Action buttons)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenFullCalculator,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, Cyan500)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = Cyan400, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calc m²", color = Cyan400, fontWeight = FontWeight.Black, maxLines = 1)
                    }
                    Button(
                        onClick = {
                            onSave(nameStr.trim().ifBlank { item.name }, cartonsVal, piecesVal, m2CtnVal, safePcs, priceVal, pricePieceVal, activeUnit)
                        },
                        enabled = cartonsVal > 0 || piecesVal > 0,
                        modifier = Modifier.weight(1.4f).height(52.dp).testTag("quick_edit_save_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                    ) {
                        Text("Enregistrer", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
        }
    }
}

private enum class DiversDialogMode { STANDARD, M2_PER_CARTON }

private enum class DiversNumTarget(val allowDecimal: Boolean) {
    QTY(true),
    PRICE(true),
    CALC_M2_CTN(true),
    REQ_M2(true),
    CALC_PRICE(true)
}

@Composable
private fun DiversLineDialog(
    editingLine: FreeLineItem? = null,
    onDismiss: () -> Unit,
    onSaveLine: (FreeLineItem) -> Unit
) {
    BackHandler { onDismiss() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var dialogMode by remember { mutableStateOf(DiversDialogMode.STANDARD) }
    var designation by remember { mutableStateOf(editingLine?.designation ?: "") }
    var unite by remember { mutableStateOf(editingLine?.unite ?: "sac") }
    var qtyStr by remember { mutableStateOf(editingLine?.let { fmtClean(it.quantite) } ?: "1") }
    var priceStr by remember {
        mutableStateOf(
            editingLine?.let {
                if (it.prixUnitaire % 1.0 == 0.0) it.prixUnitaire.toInt().toString() else it.prixUnitaire.toString()
            } ?: "750"
        )
    }

    var calcM2CtnStr by remember { mutableStateOf("1.44") }
    var requiredAreaStr by remember { mutableStateOf(editingLine?.let { fmtClean(it.quantite) } ?: "10") }
    var calcPriceStr by remember {
        mutableStateOf(
            editingLine?.let {
                if (it.prixUnitaire % 1.0 == 0.0) it.prixUnitaire.toInt().toString() else it.prixUnitaire.toString()
            } ?: "1200"
        )
    }

    var activeTarget by remember { mutableStateOf(DiversNumTarget.QTY) }
    var showKeypad by remember { mutableStateOf(false) }
    var showAlphaKeypad by remember { mutableStateOf(false) }
    var isPristine by remember { mutableStateOf(true) }
    var isAlphaPristine by remember { mutableStateOf(false) }

    val qtyVal = qtyStr.toDoubleOrNull() ?: 0.0
    val priceVal = priceStr.toDoubleOrNull() ?: 0.0

    val m2CtnVal = (calcM2CtnStr.toDoubleOrNull() ?: 1.44).coerceAtLeast(0.01)
    val reqM2Val = (requiredAreaStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val cartonsNeeded = if (reqM2Val <= 0.0) 0 else kotlin.math.ceil(reqM2Val / m2CtnVal).toInt().coerceAtLeast(1)
    val deliveredAreaM2 = ((cartonsNeeded * m2CtnVal) * 100.0).roundToInt() / 100.0
    val calcPriceVal = calcPriceStr.toDoubleOrNull() ?: 0.0
    val calcTotalDa = ((deliveredAreaM2 * calcPriceVal) * 100.0).roundToInt() / 100.0

    fun selectNumTarget(t: DiversNumTarget) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        activeTarget = t
        isPristine = true
        showKeypad = true
        showAlphaKeypad = false
    }

    fun getTargetStr(): String = when (activeTarget) {
        DiversNumTarget.QTY -> qtyStr
        DiversNumTarget.PRICE -> priceStr
        DiversNumTarget.CALC_M2_CTN -> calcM2CtnStr
        DiversNumTarget.REQ_M2 -> requiredAreaStr
        DiversNumTarget.CALC_PRICE -> calcPriceStr
    }

    fun setTargetStr(v: String) {
        when (activeTarget) {
            DiversNumTarget.QTY -> qtyStr = v
            DiversNumTarget.PRICE -> priceStr = v
            DiversNumTarget.CALC_M2_CTN -> calcM2CtnStr = v
            DiversNumTarget.REQ_M2 -> requiredAreaStr = v
            DiversNumTarget.CALC_PRICE -> calcPriceStr = v
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        SuppressSystemSoftKeyboard {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 580.dp)
                .padding(14.dp),
            shape = RoundedCornerShape(22.dp),
            color = Slate900,
            border = BorderStroke(1.5.dp, Slate700)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .testTag("divers_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (editingLine != null) "Modifier Ligne Divers" else "Divers & Calcul m²/Carton",
                            style = MaterialTheme.typography.titleLarge,
                            color = BannerGold,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FERMER", fontWeight = FontWeight.Black)
                        }
                    }
                }

                // Mode Switch: Standard Divers vs m²/Carton Calculator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = dialogMode == DiversDialogMode.STANDARD,
                        onClick = {
                            dialogMode = DiversDialogMode.STANDARD
                            showKeypad = false
                        },
                        label = { Text("Article Divers / Colle", fontWeight = FontWeight.Black) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Amber500,
                            selectedLabelColor = OnGold,
                            containerColor = Slate950,
                            labelColor = Slate400
                        ),
                        modifier = Modifier.weight(1f).testTag("divers_mode_standard")
                    )
                    FilterChip(
                        selected = dialogMode == DiversDialogMode.M2_PER_CARTON,
                        onClick = {
                            dialogMode = DiversDialogMode.M2_PER_CARTON
                            selectNumTarget(DiversNumTarget.REQ_M2)
                        },
                        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Calc m² / Carton", fontWeight = FontWeight.Black) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Cyan500,
                            selectedLabelColor = OnGold,
                            containerColor = Slate950,
                            labelColor = Slate400
                        ),
                        modifier = Modifier.weight(1f).testTag("divers_mode_m2_calc")
                    )
                }

                if (dialogMode == DiversDialogMode.STANDARD) {
                    Text("Raccourcis rapides :", style = MaterialTheme.typography.labelMedium, color = Slate400, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PosSeeds.DIVERS_PRESETS.forEach { preset ->
                            FilterChip(
                                selected = designation == preset.designation,
                                onClick = {
                                    designation = preset.designation
                                    unite = preset.unite
                                    priceStr = preset.prix.toInt().toString()
                                    showKeypad = false
                                    showAlphaKeypad = false
                                },
                                label = { Text("${preset.designation.take(18)} (${preset.prix.toInt()})", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Amber500,
                                    selectedLabelColor = OnGold,
                                    containerColor = Slate950,
                                    labelColor = Slate100
                                )
                            )
                        }
                    }

                    ClearableOutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Désignation") },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                if (it.isFocused) {
                                    showAlphaKeypad = true
                                    showKeypad = false
                                    isAlphaPristine = false
                                }
                            }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Unité:", style = MaterialTheme.typography.labelMedium, color = Slate400, fontWeight = FontWeight.Bold)
                        listOf("sac", "kg", "m²", "ctn", "ml", "pcs", "u").forEach { unitOpt ->
                            FilterChip(
                                selected = unite == unitOpt,
                                onClick = { unite = unitOpt },
                                label = { Text(unitOpt, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Cyan500,
                                    selectedLabelColor = OnGold,
                                    containerColor = Slate950,
                                    labelColor = Slate400
                                )
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        NumericDisplayField(
                            label = "Quantité ($unite)",
                            value = qtyStr,
                            isActive = showKeypad && activeTarget == DiversNumTarget.QTY,
                            onSelect = { selectNumTarget(DiversNumTarget.QTY) },
                            onClear = { qtyStr = ""; isPristine = false },
                            modifier = Modifier.weight(1f),
                            testTag = "divers_qty_field"
                        )
                        NumericDisplayField(
                            label = "Prix Unitaire",
                            value = priceStr,
                            isActive = showKeypad && activeTarget == DiversNumTarget.PRICE,
                            onSelect = { selectNumTarget(DiversNumTarget.PRICE) },
                            onClear = { priceStr = ""; isPristine = false },
                            suffix = "DA",
                            modifier = Modifier.weight(1.2f),
                            testTag = "divers_price_field"
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("1.44", "1.42", "1.50", "1.08", "1.62", "1.80").forEach { presetM2 ->
                            val isSel = kotlin.math.abs(m2CtnVal - (presetM2.toDoubleOrNull() ?: 0.0)) < 0.01
                            FilterChip(
                                selected = isSel,
                                onClick = { calcM2CtnStr = presetM2 },
                                label = { Text("$presetM2 m²/c", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Cyan500,
                                    selectedLabelColor = OnGold,
                                    containerColor = Slate950,
                                    labelColor = Slate400
                                )
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        NumericDisplayField(
                            label = "Surface requise",
                            value = requiredAreaStr,
                            isActive = showKeypad && activeTarget == DiversNumTarget.REQ_M2,
                            onSelect = { selectNumTarget(DiversNumTarget.REQ_M2) },
                            onClear = { requiredAreaStr = ""; isPristine = false },
                            suffix = "m²",
                            accentColor = Amber500,
                            modifier = Modifier.weight(1f),
                            testTag = "divers_calc_area_field"
                        )
                        NumericDisplayField(
                            label = "m² / Carton",
                            value = calcM2CtnStr,
                            isActive = showKeypad && activeTarget == DiversNumTarget.CALC_M2_CTN,
                            onSelect = { selectNumTarget(DiversNumTarget.CALC_M2_CTN) },
                            onClear = { calcM2CtnStr = ""; isPristine = false },
                            suffix = "m²/c",
                            accentColor = Cyan500,
                            modifier = Modifier.weight(1f),
                            testTag = "divers_calc_m2ctn_field"
                        )
                        NumericDisplayField(
                            label = "Prix / m²",
                            value = calcPriceStr,
                            isActive = showKeypad && activeTarget == DiversNumTarget.CALC_PRICE,
                            onSelect = { selectNumTarget(DiversNumTarget.CALC_PRICE) },
                            onClear = { calcPriceStr = ""; isPristine = false },
                            suffix = "DA",
                            accentColor = Emerald500,
                            modifier = Modifier.weight(1f),
                            testTag = "divers_calc_price_field"
                        )
                    }

                    ClearableOutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Désignation (auto si vide)") },
                        placeholder = {
                            Text("Carrelage (${fmtClean(m2CtnVal)} m²/ctn)", color = Slate400)
                        },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                if (it.isFocused) {
                                    showAlphaKeypad = true
                                    showKeypad = false
                                    isAlphaPristine = false
                                }
                            }
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Navy950
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Colisage", style = MaterialTheme.typography.labelMedium, color = OnDark.copy(alpha = 0.85f))
                                Text("${fmtClean(m2CtnVal)} m² / carton", style = MaterialTheme.typography.labelLarge, color = BannerGold, fontWeight = FontWeight.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$cartonsNeeded ctn • ${formatM2(deliveredAreaM2)} m² livrés",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OnDark,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = formatDa(calcTotalDa),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = BannerEmerald,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = showAlphaKeypad, enter = expandVertically(), exit = shrinkVertically()) {
                    NonImeAlphanumericKeypad(
                        onKeyPress = { key ->
                            val (next, nextPristine) = applyAlphanumericKeypadInput(designation, key, isAlphaPristine, uppercase = true)
                            designation = next
                            isAlphaPristine = nextPristine
                        },
                        onClear = { designation = ""; isAlphaPristine = false },
                        onValidate = { showAlphaKeypad = false },
                        validateLabel = "OK"
                    )
                }

                AnimatedVisibility(visible = showKeypad, enter = expandVertically(), exit = shrinkVertically()) {
                    NonImeNumericKeypad(
                        onKeyPress = { key ->
                            val cur = getTargetStr()
                            val (next, nextPristine) = applyKeypadInput(cur, key, activeTarget.allowDecimal, isPristine)
                            setTargetStr(next)
                            isPristine = nextPristine
                        },
                        allowDecimal = activeTarget.allowDecimal,
                        compact = true,
                        ultraCompact = true,
                        onValidate = { showKeypad = false },
                        validateLabel = "OK"
                    )
                }

                val isSaveEnabled = if (dialogMode == DiversDialogMode.STANDARD) {
                    designation.isNotBlank() && qtyVal > 0
                } else {
                    deliveredAreaM2 > 0.0
                }
                val btnAmount = if (dialogMode == DiversDialogMode.STANDARD) qtyVal * priceVal else calcTotalDa

                Button(
                    onClick = {
                        if (dialogMode == DiversDialogMode.STANDARD) {
                            if (designation.isNotBlank() && qtyVal > 0) {
                                onSaveLine(
                                    FreeLineItem(
                                        id = editingLine?.id ?: "div_${UUID.randomUUID().toString().take(8)}",
                                        designation = designation.trim(),
                                        quantite = qtyVal,
                                        unite = unite,
                                        prixUnitaire = priceVal,
                                        sousTotal = ((qtyVal * priceVal) * 100.0).roundToInt() / 100.0
                                    )
                                )
                            }
                        } else if (deliveredAreaM2 > 0.0) {
                            val finalDesignation = designation.trim().ifBlank {
                                "Carrelage (${fmtClean(m2CtnVal)} m²/ctn • $cartonsNeeded ctn)"
                            }
                            onSaveLine(
                                FreeLineItem(
                                    id = editingLine?.id ?: "div_${UUID.randomUUID().toString().take(8)}",
                                    designation = finalDesignation,
                                    quantite = deliveredAreaM2,
                                    unite = "m²",
                                    prixUnitaire = calcPriceVal,
                                    sousTotal = calcTotalDa
                                )
                            )
                        }
                    },
                    enabled = isSaveEnabled,
                    modifier = Modifier.fillMaxWidth().height(54.dp).testTag("divers_save_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                ) {
                    Text(
                        text = if (editingLine != null) "Enregistrer (${formatDa(btnAmount)})" else "Ajouter (${formatDa(btnAmount)})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        }
    }
}

private enum class CheckoutNumTarget { REMISE, RETOUR, PAYE }
private enum class CheckoutAlphaTarget { CLIENT_NAME, CLIENT_PHONE, DESTINATION, NOTES }

@Composable
private fun CheckoutModalDialog(
    totalBrut: Double,
    initialReturnDeduction: Double = 0.0,
    onUpdateReturnDeduction: (Double) -> Unit = {},
    customers: List<Customer>,
    editingInvoice: Invoice?,
    canModifyPrices: Boolean = true,
    settings: AppSettings = PosSeeds.DEFAULT_SETTINGS,
    isAdmin: Boolean = false,
    onVerifyAdminPin: (String) -> Boolean = { true },
    onDismiss: () -> Unit,
    onConfirm: (InvoiceClient, Double, Double, String, String, String) -> Unit,
    onConfirmExtended: ((InvoiceClient, Double, Double, String, String, String, Boolean, Boolean) -> Unit)? = null
) {
    BackHandler { onDismiss() }
    var selectedCustomerId by remember { mutableStateOf(editingInvoice?.client?.id) }
    var clientName by remember { mutableStateOf(editingInvoice?.client?.nom ?: "Client Comptoir") }
    var clientPhone by remember { mutableStateOf(editingInvoice?.client?.tel ?: "") }
    var destination by remember { mutableStateOf(editingInvoice?.destination ?: "") }
    var remiseStr by remember { mutableStateOf((editingInvoice?.remise ?: 0.0).toInt().toString()) }
    var retourStr by remember {
        val initRet = (editingInvoice?.deductionRetour ?: initialReturnDeduction).coerceAtLeast(0.0)
        mutableStateOf(if (initRet > 0.0) kotlin.math.round(initRet).toInt().toString() else "0")
    }
    // Cash-Only (Espèces) or Crédit — No Chèque / Virement
    val initialMode = if (editingInvoice?.modePaiement == "Crédit") "Crédit" else "Espèces"
    var modePaiement by remember { mutableStateOf(initialMode) }
    val remiseVal = (remiseStr.toDoubleOrNull() ?: 0.0).coerceIn(0.0, totalBrut)
    val retourVal = (retourStr.toDoubleOrNull() ?: 0.0).coerceIn(0.0, (totalBrut - remiseVal).coerceAtLeast(0.0))
    val totalNet = (totalBrut - remiseVal - retourVal).coerceAtLeast(0.0)
    var payeStr by remember {
        mutableStateOf(kotlin.math.round(editingInvoice?.montantPaye ?: totalNet).toInt().toString())
    }
    var notes by remember { mutableStateOf(editingInvoice?.notes ?: "") }
    var activeTarget by remember { mutableStateOf(CheckoutNumTarget.PAYE) }
    var activeAlphaTarget by remember { mutableStateOf<CheckoutAlphaTarget?>(null) }
    var showKeypad by remember { mutableStateOf(false) }
    var showAlphaKeypad by remember { mutableStateOf(false) }
    var isPristine by remember { mutableStateOf(true) }
    var isAlphaPristine by remember { mutableStateOf(false) }

    val parsedPaye = payeStr.toDoubleOrNull() ?: 0.0
    val payeVal = when {
        modePaiement == "Crédit" -> 0.0
        kotlin.math.abs(totalNet - parsedPaye) < 1.0 && parsedPaye >= kotlin.math.floor(totalNet) -> totalNet
        else -> parsedPaye.coerceIn(0.0, totalNet)
    }
    val reste = (totalNet - payeVal).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = true)
    ) {
        SuppressSystemSoftKeyboard {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 580.dp)
                .fillMaxHeight(0.95f)
                .imePadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(22.dp),
            color = Slate900,
            border = BorderStroke(1.5.dp, Slate700)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // 1. PINNED Header
                Surface(shape = RoundedCornerShape(14.dp), color = Navy950) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Encaissement Espèces", style = MaterialTheme.typography.titleLarge, color = OnDark, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FERMER", fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. SCROLLABLE Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                Text("Client :", style = MaterialTheme.typography.labelMedium, color = Slate400, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCustomerId == null,
                        onClick = {
                            selectedCustomerId = null
                            clientName = "Client Comptoir"
                            clientPhone = ""
                            showKeypad = false
                            showAlphaKeypad = false
                        },
                        label = { Text("Client Comptoir", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Cyan500,
                            selectedLabelColor = OnGold,
                            containerColor = Slate950,
                            labelColor = Slate400
                        )
                    )
                    customers.forEach { c ->
                        FilterChip(
                            selected = selectedCustomerId == c.id,
                            onClick = {
                                selectedCustomerId = c.id
                                clientName = c.nom
                                clientPhone = c.tel
                                showKeypad = false
                                showAlphaKeypad = false
                            },
                            label = { Text(c.nom, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber500,
                                selectedLabelColor = OnGold
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ClearableOutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Nom Client") },
                        autoClearValues = setOf("Client Comptoir"),
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier
                            .weight(1.2f)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    activeAlphaTarget = CheckoutAlphaTarget.CLIENT_NAME
                                    showAlphaKeypad = true
                                    isAlphaPristine = false
                                    showKeypad = false
                                }
                            }
                            .testTag("checkout_client_name_input")
                    )
                    ClearableOutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Tél") },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    activeAlphaTarget = CheckoutAlphaTarget.CLIENT_PHONE
                                    showAlphaKeypad = true
                                    isAlphaPristine = false
                                    showKeypad = false
                                }
                            }
                            .testTag("checkout_client_phone_input")
                    )
                }

                ClearableOutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination (optionnel — ligne imprimée si vide)") },
                    onDismissKeypad = { showKeypad = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            if (it.isFocused) {
                                activeAlphaTarget = CheckoutAlphaTarget.DESTINATION
                                showAlphaKeypad = true
                                isAlphaPristine = false
                                showKeypad = false
                            }
                        }
                        .testTag("checkout_destination_input")
                )

                // Cash Only Modes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Espèces", "Crédit").forEach { mode ->
                        val isSel = modePaiement == mode
                        Button(
                            onClick = {
                                modePaiement = mode
                                if (mode == "Crédit") {
                                    payeStr = "0"
                                    showKeypad = false
                                } else if ((payeStr.toDoubleOrNull() ?: 0.0) == 0.0) {
                                    payeStr = kotlin.math.round(totalNet).toInt().toString()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("checkout_mode_$mode"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) (if (mode == "Espèces") Emerald500 else Rose500) else Slate850,
                                contentColor = if (isSel) OnDark else Slate100
                            )
                        ) {
                            Text(
                                text = if (mode == "Espèces") "Espèces (Cash)" else "À Crédit",
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    NumericDisplayField(
                        label = if (canModifyPrices) "Remise" else "Remise (Verrouillé)",
                        value = remiseStr,
                        isActive = canModifyPrices && showKeypad && activeTarget == CheckoutNumTarget.REMISE,
                        onSelect = {
                            if (canModifyPrices) {
                                activeTarget = CheckoutNumTarget.REMISE
                                isPristine = true
                                showKeypad = true
                                showAlphaKeypad = false
                            }
                        },
                        onClear = if (canModifyPrices) ({
                            remiseStr = ""
                            val nextNet = (totalBrut - retourVal).coerceAtLeast(0.0)
                            if (modePaiement != "Crédit") payeStr = kotlin.math.round(nextNet).toInt().toString()
                            isPristine = false
                        }) else null,
                        enabled = canModifyPrices,
                        suffix = "DA",
                        accentColor = Amber500,
                        modifier = Modifier.weight(1f),
                        testTag = "checkout_remise_field"
                    )
                    NumericDisplayField(
                        label = "Déduction Retour",
                        value = retourStr,
                        isActive = showKeypad && activeTarget == CheckoutNumTarget.RETOUR,
                        onSelect = {
                            activeTarget = CheckoutNumTarget.RETOUR
                            isPristine = true
                            showKeypad = true
                            showAlphaKeypad = false
                        },
                        onClear = {
                            retourStr = ""
                            onUpdateReturnDeduction(0.0)
                            val nextNet = (totalBrut - remiseVal).coerceAtLeast(0.0)
                            if (modePaiement != "Crédit") payeStr = kotlin.math.round(nextNet).toInt().toString()
                            isPristine = false
                        },
                        suffix = "DA",
                        accentColor = Rose500,
                        modifier = Modifier.weight(1f),
                        testTag = "checkout_retour_field"
                    )
                }

                NumericDisplayField(
                    label = "Montant Payé",
                    value = if (modePaiement == "Crédit") "0" else payeStr,
                    isActive = showKeypad && activeTarget == CheckoutNumTarget.PAYE,
                    onSelect = {
                        if (modePaiement == "Crédit") modePaiement = "Espèces"
                        activeTarget = CheckoutNumTarget.PAYE
                        isPristine = true
                        showKeypad = true
                        showAlphaKeypad = false
                    },
                    onClear = { payeStr = ""; isPristine = false },
                    suffix = "DA",
                    accentColor = Emerald500,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "checkout_paye_field"
                )

                val missingReturnReason = retourVal > 0.01 && notes.trim().length < 3
                ClearableOutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = {
                        Text(
                            text = if (retourVal > 0.01) "Motif du retour OBLIGATOIRE (ex: 2 ctn 60/60)" else "Notes / Livraison (optionnel)",
                            color = if (missingReturnReason) Rose400 else Slate400,
                            fontWeight = if (retourVal > 0.01) FontWeight.Black else FontWeight.Normal
                        )
                    },
                    onDismissKeypad = { showKeypad = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            if (it.isFocused) {
                                activeAlphaTarget = CheckoutAlphaTarget.NOTES
                                showAlphaKeypad = true
                                isAlphaPristine = false
                                showKeypad = false
                            }
                        }
                        .testTag("checkout_notes_input")
                )
                if (missingReturnReason) {
                    Text(
                        text = "⚠ Saisissez un motif (min. 3 caractères) pour justifier la déduction retour de ${formatDa(retourVal)}.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Rose400,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("checkout_return_reason_warning")
                    )
                }

                AnimatedVisibility(visible = showAlphaKeypad && activeAlphaTarget != null, enter = expandVertically(), exit = shrinkVertically()) {
                    val curAlpha = when (activeAlphaTarget) {
                        CheckoutAlphaTarget.CLIENT_NAME -> clientName
                        CheckoutAlphaTarget.CLIENT_PHONE -> clientPhone
                        CheckoutAlphaTarget.DESTINATION -> destination
                        CheckoutAlphaTarget.NOTES -> notes
                        null -> ""
                    }
                    NonImeAlphanumericKeypad(
                        onKeyPress = { key ->
                            val (next, nextPristine) = applyAlphanumericKeypadInput(curAlpha, key, isAlphaPristine, uppercase = true)
                            when (activeAlphaTarget) {
                                CheckoutAlphaTarget.CLIENT_NAME -> clientName = next
                                CheckoutAlphaTarget.CLIENT_PHONE -> clientPhone = next
                                CheckoutAlphaTarget.DESTINATION -> destination = next
                                CheckoutAlphaTarget.NOTES -> notes = next
                                null -> Unit
                            }
                            isAlphaPristine = nextPristine
                        },
                        onClear = {
                            when (activeAlphaTarget) {
                                CheckoutAlphaTarget.CLIENT_NAME -> clientName = ""
                                CheckoutAlphaTarget.CLIENT_PHONE -> clientPhone = ""
                                CheckoutAlphaTarget.DESTINATION -> destination = ""
                                CheckoutAlphaTarget.NOTES -> notes = ""
                                null -> Unit
                            }
                            isAlphaPristine = false
                        },
                        onValidate = {
                            showAlphaKeypad = false
                            activeAlphaTarget = null
                        },
                        validateLabel = "OK"
                    )
                }

                AnimatedVisibility(visible = showKeypad, enter = expandVertically(), exit = shrinkVertically()) {
                    NonImeNumericKeypad(
                        onKeyPress = { key ->
                            val cur = when (activeTarget) {
                                CheckoutNumTarget.REMISE -> remiseStr
                                CheckoutNumTarget.RETOUR -> retourStr
                                CheckoutNumTarget.PAYE -> payeStr
                            }
                            val (next, nextPristine) = applyKeypadInput(cur, key, true, isPristine)
                            when (activeTarget) {
                                CheckoutNumTarget.REMISE -> {
                                    remiseStr = next
                                    val nextRem = (next.toDoubleOrNull() ?: 0.0).coerceIn(0.0, totalBrut)
                                    val nextNet = (totalBrut - nextRem - retourVal).coerceAtLeast(0.0)
                                    if (modePaiement != "Crédit") payeStr = kotlin.math.round(nextNet).toInt().toString()
                                }
                                CheckoutNumTarget.RETOUR -> {
                                    retourStr = next
                                    val nextRet = (next.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                                    onUpdateReturnDeduction(nextRet)
                                    val nextNet = (totalBrut - remiseVal - nextRet).coerceAtLeast(0.0)
                                    if (modePaiement != "Crédit") payeStr = kotlin.math.round(nextNet).toInt().toString()
                                }
                                CheckoutNumTarget.PAYE -> {
                                    payeStr = next
                                }
                            }
                            isPristine = nextPristine
                        },
                        quickChips = listOf("+500" to 500.0, "+1000" to 1000.0, "+5000" to 5000.0),
                        onQuickDelta = { delta ->
                            val cur = when (activeTarget) {
                                CheckoutNumTarget.REMISE -> remiseStr
                                CheckoutNumTarget.RETOUR -> retourStr
                                CheckoutNumTarget.PAYE -> payeStr
                            }
                            val next = applyKeypadQuickDelta(cur, delta, false)
                            when (activeTarget) {
                                CheckoutNumTarget.REMISE -> {
                                    remiseStr = next
                                    val nextRem = (next.toDoubleOrNull() ?: 0.0).coerceIn(0.0, totalBrut)
                                    val nextNet = (totalBrut - nextRem - retourVal).coerceAtLeast(0.0)
                                    if (modePaiement != "Crédit") payeStr = kotlin.math.round(nextNet).toInt().toString()
                                }
                                CheckoutNumTarget.RETOUR -> {
                                    retourStr = next
                                    val nextRet = (next.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                                    onUpdateReturnDeduction(nextRet)
                                    val nextNet = (totalBrut - remiseVal - nextRet).coerceAtLeast(0.0)
                                    if (modePaiement != "Crédit") payeStr = kotlin.math.round(nextNet).toInt().toString()
                                }
                                CheckoutNumTarget.PAYE -> {
                                    payeStr = next
                                }
                            }
                            isPristine = false
                        },
                        allowDecimal = false,
                        onValidate = { showKeypad = false },
                        validateLabel = "OK"
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Navy950
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (retourVal > 0.01) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Déduction Retour :", color = Rose300, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("-${formatDa(retourVal)}", color = Rose400, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Net :", color = OnDark.copy(alpha = 0.8f), style = MaterialTheme.typography.titleSmall)
                            Text(formatDa(totalNet), color = OnDark, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Reste / Crédit :", color = OnDark.copy(alpha = 0.85f), style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = formatDa(reste),
                                color = if (reste > 0) BannerGold else BannerEmerald,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                } // End of scrollable body Column

                Spacer(modifier = Modifier.height(8.dp))

                val missingReturnReason = retourVal > 0.01 && notes.trim().length < 3
                val buildClient = {
                    InvoiceClient(
                        id = selectedCustomerId,
                        nom = clientName.ifBlank { "Client Comptoir" },
                        tel = clientPhone.trim()
                    )
                }

                // 3. PINNED Footer Buttons (Always 100% visible — outside verticalScroll!)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            onUpdateReturnDeduction(retourVal)
                            if (onConfirmExtended != null) {
                                onConfirmExtended(
                                    buildClient(),
                                    remiseVal,
                                    payeVal,
                                    modePaiement,
                                    notes.trim(),
                                    destination.trim(),
                                    editingInvoice?.isDevis == true,
                                    false
                                )
                            } else {
                                onConfirm(
                                    buildClient(),
                                    remiseVal,
                                    payeVal,
                                    modePaiement,
                                    notes.trim(),
                                    destination.trim()
                                )
                            }
                        },
                        enabled = !missingReturnReason,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("checkout_confirm_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                    ) {
                        Text(
                            text = when {
                                missingReturnReason -> "Motif du retour requis"
                                editingInvoice?.isDevis == true -> "Mettre à jour le Devis"
                                editingInvoice != null -> "Enregistrer Modifications"
                                else -> "Confirmer & Facturer • ${formatDa(totalNet)}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (editingInvoice == null && onConfirmExtended != null) {
                        OutlinedButton(
                            onClick = {
                                onUpdateReturnDeduction(retourVal)
                                onConfirmExtended(
                                    buildClient(),
                                    remiseVal,
                                    0.0,
                                    "Devis",
                                    notes.trim(),
                                    destination.trim(),
                                    true,
                                    false
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("pos_save_devis_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Cyan400)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Cyan300, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Enregistrer en Devis / Proforma",
                                style = MaterialTheme.typography.labelMedium,
                                color = Cyan300,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun HeldCartsModalDialog(
    heldCarts: List<HeldCart>,
    onDismiss: () -> Unit,
    onResume: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    BackHandler { onDismiss() }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .padding(16.dp)
                .testTag("held_carts_sheet"),
            shape = RoundedCornerShape(20.dp),
            color = Slate900,
            border = BorderStroke(1.5.dp, Cyan500.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Paniers en attente", style = MaterialTheme.typography.titleMedium, color = OnDark, fontWeight = FontWeight.Black)
                        Text("Reprendre un client sans perdre son panier", style = MaterialTheme.typography.labelSmall, color = Cyan300)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Slate200)
                    }
                }
                if (heldCarts.isEmpty()) {
                    Text("Aucun panier en attente.", color = Slate400, style = MaterialTheme.typography.bodyMedium)
                } else {
                    heldCarts.forEach { hc ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Slate950,
                            border = BorderStroke(1.dp, Slate800)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(hc.label, style = MaterialTheme.typography.titleSmall, color = Slate100, fontWeight = FontWeight.Black)
                                    Text(
                                        text = "${hc.lineCount} ligne(s) • ${formatDa(hc.totalDa)} • ${hc.createdAt.takeLast(8).take(5)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Amber300,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = { onResume(hc.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(40.dp).testTag("held_cart_resume_${hc.id}")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Reprendre", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall)
                                    }
                                    IconButton(
                                        onClick = { onDelete(hc.id) },
                                        modifier = Modifier.testTag("held_cart_delete_${hc.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer panier en attente", tint = Rose400)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PosDynamicFilterStrip(
    title: String,
    options: List<String>,
    selected: String,
    activeColor: androidx.compose.ui.graphics.Color,
    accentTextColor: androidx.compose.ui.graphics.Color,
    compact: Boolean = false,
    matchesOption: (String, String) -> Boolean = { a, b -> a.equals(b, ignoreCase = true) },
    formatLabel: (String) -> String = { it },
    colorHexForOption: ((String) -> String?)? = null,
    customOptionTheme: ((String) -> com.example.ui.theme.DimensionVisualTheme?)? = null,
    testTagPrefix: String,
    onSelect: (String) -> Unit
) {
    val selectedTextColor = if (activeColor == Amber500 || activeColor == Amber400 || activeColor == Cyan500 || activeColor == Yellow500) OnGold else OnDark
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.width(56.dp),
            shape = RoundedCornerShape(7.dp),
            color = activeColor.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, activeColor.copy(alpha = 0.5f))
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = accentTextColor,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = if (compact) 3.dp else 5.dp)
            )
        }
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            lazyItems(options, key = { it }) { option ->
                val isAllOption = option == "Toutes" || option == "Tous"
                val isSelectedAll = selected == "Toutes" || selected == "Tous"
                val isSelected = if (isAllOption) {
                    isSelectedAll
                } else {
                    !isSelectedAll && matchesOption(option, selected)
                }
                val dotHex = colorHexForOption?.invoke(option)
                val optTheme = if (!isAllOption) customOptionTheme?.invoke(option) else null

                val containerColor = when {
                    isSelected -> optTheme?.containerColor ?: activeColor
                    optTheme != null -> optTheme.containerColor.copy(alpha = 0.35f)
                    else -> Slate950
                }
                val borderColor = when {
                    isSelected -> optTheme?.borderColor ?: activeColor
                    optTheme != null -> optTheme.borderColor.copy(alpha = 0.6f)
                    else -> Slate800
                }
                val chipTextColor = when {
                    isSelected -> optTheme?.textColor ?: selectedTextColor
                    optTheme != null -> optTheme.textColor.copy(alpha = 0.9f)
                    else -> Slate200
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = containerColor,
                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                    modifier = Modifier
                        .height(if (compact) 24.dp else 28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelect(option) }
                        .testTag("${testTagPrefix}_${option}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (!dotHex.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(parseCeramicColor(dotHex))
                            )
                        }
                        Text(
                            text = formatLabel(option),
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelSmall,
                            color = chipTextColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
