package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.data.model.CatalogIndex
import com.example.data.model.POS_ACCESSORY_FILTER_OPTIONS
import com.example.data.model.POS_COLOR_OPTIONS
import com.example.data.model.POS_DIMENSIONS_ORDER
import com.example.data.model.POS_FINITION_OPTIONS
import com.example.data.model.PosSeeds
import com.example.data.model.SearchCorrectionSuggestion
import com.example.data.model.StoreInfo
import com.example.data.model.TileProduct
import com.example.data.model.buildCatalogIndex
import com.example.data.model.buildCatalogSearchVocabulary
import com.example.data.model.buildProductSearchBlob
import com.example.data.model.formatDa
import com.example.data.model.formatDimensionSlash
import com.example.data.model.formatFinitionChipLabel
import com.example.data.model.formatFinitionLabel
import com.example.data.model.formatM2
import com.example.data.model.formatProductDesignation
import com.example.data.model.isJetonProduct
import com.example.data.model.isMotifEligibleDimension
import com.example.data.model.isPlinthe8x45
import com.example.data.model.isPlinthe8x60
import com.example.data.model.isTile120x60
import com.example.data.model.isTile45x45
import com.example.data.model.isTile60x60
import com.example.data.model.matchesColorFilter
import com.example.data.model.matchesFinitionFilter
import com.example.data.model.matchesProductSearch
import com.example.data.model.STANDARD_USER_COLISAGES
import com.example.data.model.normalizeDimensionKey
import com.example.data.model.normalizeSearchText
import com.example.data.model.splitFinitions
import com.example.data.model.suggestSearchCorrections
import com.example.data.model.toggleFinitionSelection
import com.example.ui.components.ClearableOutlinedTextField
import com.example.ui.components.DispoDepotRecipientsDialog
import com.example.ui.components.NonImeAlphanumericKeypad
import com.example.ui.components.NonImeNumericKeypad
import com.example.ui.components.NumericDisplayField
import com.example.ui.components.ProductPhotoLightboxDialog
import com.example.ui.components.SuppressSystemSoftKeyboard
import com.example.ui.components.TileSwatchImage
import com.example.ui.components.applyAlphanumericKeypadInput
import com.example.ui.components.applyKeypadInput
import com.example.ui.components.parseCeramicColor
import com.example.ui.components.saveProductPhotoFromUri
import com.example.ui.theme.Amber300
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber950
import com.example.ui.theme.BannerGold
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.OnGold
import com.example.ui.theme.PosNumericTextStyle
import com.example.ui.theme.Purple300
import com.example.ui.theme.Purple400
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
import com.example.ui.theme.Yellow500
import com.example.ui.theme.Yellow950
import com.example.ui.theme.dimensionBadgeTheme
import com.example.util.CatalogSpreadsheetEngine
import com.example.util.DailyClosingReportEngine
import com.example.util.SpreadsheetImportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Composable
fun StockScreen(
    products: List<TileProduct>,
    catalogIndex: CatalogIndex? = null,
    storeInfo: StoreInfo = PosSeeds.DEFAULT_STORE_INFO,
    canModifyStock: Boolean,
    canAddProducts: Boolean = true,
    canViewPrixAchat: Boolean = false,
    onAdjustStock: (String, Int) -> Unit,
    onSetStock: (String, Int) -> Unit,
    onSaveProduct: (TileProduct) -> Unit,
    onRequestDepotDispo: (TileProduct) -> Unit,
    onSaveProductWithPlinthe: ((TileProduct, Boolean, Double) -> Unit)? = null,
    onSaveProductWithAuto: ((TileProduct, Boolean, Double, Boolean, Boolean) -> Unit)? = null,
    onUpdateProductPhoto: ((String, String) -> Unit)? = null,
    onImportSpreadsheetCatalog: ((List<TileProduct>, Boolean) -> Unit)? = null,
    onDeleteProduct: ((String) -> Unit)? = null,
    onApplyQuickInventory: ((Map<String, Int>) -> Unit)? = null,
    onShowMessage: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var dimFilter by remember { mutableStateOf("Toutes") }
    var accessoryFilter by remember { mutableStateOf("Tous") }
    var usineFilter by remember { mutableStateOf("Toutes") }
    var finitionFilter by remember { mutableStateOf("Toutes") }
    var colorFilter by remember { mutableStateOf("Toutes") }
    var filtersExpanded by remember { mutableStateOf(true) }
    var isGridView by remember { mutableStateOf(true) }

    var ficheProduct by remember { mutableStateOf<TileProduct?>(null) }
    var showNewFiche by remember { mutableStateOf(false) }
    var previewProduct by remember { mutableStateOf<TileProduct?>(null) }
    var dispoTargetProduct by remember { mutableStateOf<TileProduct?>(null) }
    var pendingDeleteProduct by remember { mutableStateOf<TileProduct?>(null) }
    var showExcelDialog by remember { mutableStateOf(false) }
    var showQuickInventory by remember { mutableStateOf(false) }
    var pendingSpreadsheetImport by remember { mutableStateOf<SpreadsheetImportResult?>(null) }
    var excelFeedback by remember { mutableStateOf<String?>(null) }

    val excelImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val parsed = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            CatalogSpreadsheetEngine.parseSpreadsheet(stream, uri.lastPathSegment.orEmpty())
                        }
                    }.getOrNull()
                }
                if (parsed == null) {
                    val errMsg = "Impossible de lire ce fichier Excel (.xlsx / .csv)."
                    excelFeedback = errMsg
                    onShowMessage?.invoke(errMsg)
                } else if (parsed.products.isEmpty()) {
                    val warn = parsed.warnings.firstOrNull() ?: "Aucun article valide trouvé dans ce fichier."
                    excelFeedback = warn
                    onShowMessage?.invoke(warn)
                } else {
                    excelFeedback = null
                    pendingSpreadsheetImport = parsed
                }
            }
        }
    }

    fun launchExcelImportPicker() {
        excelImportLauncher.launch(
            arrayOf(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/csv",
                "text/comma-separated-values",
                "*/*"
            )
        )
    }

    fun shareExcelFile(file: File, subject: String) {
        runCatching {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooserIntent = Intent.createChooser(sendIntent, subject).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        }.onSuccess {
            excelFeedback = "Fichier prêt à partager : ${file.name}"
        }.onFailure { err ->
            excelFeedback = err.localizedMessage ?: "Impossible de partager le fichier Excel."
        }
    }

    val hasActiveFilter = searchQuery.isNotBlank() ||
        dimFilter != "Toutes" || accessoryFilter != "Tous" || usineFilter != "Toutes" || finitionFilter != "Toutes" || colorFilter != "Toutes"

    val effectiveIndex = remember(catalogIndex, products) {
        if (catalogIndex != null && catalogIndex.entries.size == products.size) {
            catalogIndex
        } else {
            buildCatalogIndex(products)
        }
    }
    val dimensions = effectiveIndex.tileDimensions
    val accessoryOptions = POS_ACCESSORY_FILTER_OPTIONS
    val usines = effectiveIndex.usines
    val finitions = effectiveIndex.finitions
    val colorOptions = remember {
        listOf("Toutes") + POS_COLOR_OPTIONS.map { it.label }
    }
    val colorHexMap = remember {
        POS_COLOR_OPTIONS.associate { it.label to it.hex }
    }

    val queryTokens = remember(searchQuery) {
        normalizeSearchText(searchQuery).split(' ').filter { it.isNotBlank() }
    }

    val filtered = remember(effectiveIndex, queryTokens, dimFilter, accessoryFilter, usineFilter, finitionFilter, colorFilter) {
        effectiveIndex.filter(
            queryTokens = queryTokens,
            selectedDimension = dimFilter,
            selectedUsine = usineFilter,
            selectedFinition = finitionFilter,
            selectedColor = colorFilter,
            selectedAccessory = accessoryFilter
        )
    }

    var searchSuggestions by remember { mutableStateOf<List<SearchCorrectionSuggestion>>(emptyList()) }
    LaunchedEffect(searchQuery, filtered.size, effectiveIndex) {
        if (searchQuery.trim().length < 2 || filtered.isNotEmpty()) {
            searchSuggestions = emptyList()
            return@LaunchedEffect
        }
        delay(160L)
        searchSuggestions = withContext(Dispatchers.Default) {
            suggestSearchCorrections(searchQuery, products, effectiveIndex.searchPairs, effectiveIndex.vocabulary)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Slate950)) {
        val isNarrow = maxWidth < 360.dp
        val gridColumns = when {
            maxWidth >= 1360.dp -> 5
            maxWidth >= 1000.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Doré Luxe Studio Header + Compact 3 Filter Strips
            Surface(color = Slate900) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isNarrow) 10.dp else 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Catalogue Articles",
                                    style = if (isNarrow) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
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
                                        text = "${filtered.size}",
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
                                                dimFilter = "Toutes"
                                                accessoryFilter = "Tous"
                                                usineFilter = "Toutes"
                                                finitionFilter = "Toutes"
                                                colorFilter = "Toutes"
                                            }
                                            .testTag("stock_reset_filters_btn")
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
                            if (!canModifyStock) {
                                Text(
                                    text = if (canAddProducts) "Mode Vendeur • Ajout produits actif" else "Consultation catalogue",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Cyan300,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (filtersExpanded) Amber950 else Slate950,
                                border = BorderStroke(1.dp, if (filtersExpanded) Amber500.copy(alpha = 0.55f) else Slate700),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { filtersExpanded = !filtersExpanded }
                                    .testTag("stock_toggle_filters_btn")
                            ) {
                                Text(
                                    text = if (filtersExpanded) "Filtres ▴" else "Filtres ▾",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (filtersExpanded) Amber300 else Slate400,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }

                            // Grid / List segmented toggle
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
                                            .testTag("stock_view_grid_btn")
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
                                            .testTag("stock_view_list_btn")
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

                            if (onImportSpreadsheetCatalog != null && (canAddProducts || canModifyStock)) {
                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = Emerald950,
                                    border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.65f)),
                                    modifier = Modifier
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .clickable { showExcelDialog = true }
                                        .testTag("stock_excel_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = if (isNarrow) 7.dp else 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.TableChart, contentDescription = "Excel", tint = Emerald300, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Excel",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Emerald300,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            if (canAddProducts || canModifyStock) {
                                Button(
                                    onClick = { showNewFiche = true },
                                    modifier = Modifier
                                        .height(40.dp)
                                        .testTag("stock_add_model_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnGold),
                                    contentPadding = PaddingValues(horizontal = if (isNarrow) 8.dp else 11.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Modèle", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }

                    ClearableOutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Ex: 120 bati, bleu 120, 60 arbe...",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Amber400) },
                        modifier = Modifier.fillMaxWidth().testTag("stock_search_input")
                    )

                    if (searchSuggestions.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("stock_search_fuzzy_suggestions"),
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
                                        modifier = Modifier.testTag("stock_fuzzy_chip_$idx")
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

                    if (filtersExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            StockFilterLine(
                                title = "Format",
                                options = dimensions,
                                selected = dimFilter,
                                activeColor = Cyan500,
                                labelColor = Cyan300,
                                matchesOption = { opt, sel -> normalizeDimensionKey(opt) == normalizeDimensionKey(sel) },
                                testTagPrefix = "stock_filter_dim",
                                onSelect = { opt ->
                                    val next = if (opt != "Toutes" && normalizeDimensionKey(dimFilter) == normalizeDimensionKey(opt)) "Toutes" else opt
                                    dimFilter = next
                                    if (next != "Toutes" && accessoryFilter.contains('/')) accessoryFilter = "Tous"
                                }
                            )
                            StockFilterLine(
                                title = "Acces.",
                                options = accessoryOptions,
                                selected = accessoryFilter,
                                activeColor = BannerGold,
                                labelColor = Yellow300,
                                matchesOption = { opt, sel -> normalizeSearchText(opt) == normalizeSearchText(sel) },
                                testTagPrefix = "stock_filter_accessory",
                                onSelect = { opt ->
                                    val next = if (opt != "Tous" && normalizeSearchText(accessoryFilter) == normalizeSearchText(opt)) "Tous" else opt
                                    accessoryFilter = next
                                    if (next != "Tous" && next.contains('/')) dimFilter = "Toutes"
                                }
                            )
                            StockFilterLine(
                                title = "Usine",
                                options = usines,
                                selected = usineFilter,
                                activeColor = Amber500,
                                labelColor = Amber300,
                                matchesOption = { opt, sel -> normalizeSearchText(opt) == normalizeSearchText(sel) },
                                testTagPrefix = "stock_filter_usine",
                                onSelect = { opt ->
                                    usineFilter = if (opt != "Toutes" && normalizeSearchText(usineFilter) == normalizeSearchText(opt)) "Toutes" else opt
                                }
                            )
                            StockFilterLine(
                                title = "Finition",
                                options = finitions,
                                selected = finitionFilter,
                                activeColor = Purple500,
                                labelColor = Purple300,
                                matchesOption = { opt, sel ->
                                    if (opt == "Toutes") {
                                        sel == "Toutes" || sel.isBlank()
                                    } else {
                                        splitFinitions(sel).any { normalizeSearchText(it) == normalizeSearchText(opt) }
                                    }
                                },
                                formatOption = { f ->
                                    if (f == "Toutes") f
                                    else {
                                        val base = formatFinitionChipLabel(f).ifBlank { f.replaceFirstChar { it.uppercase() } }
                                        val isOn = splitFinitions(finitionFilter).any { normalizeSearchText(it) == normalizeSearchText(f) }
                                        if (isOn) "✓ $base" else base
                                    }
                                },
                                testTagPrefix = "stock_filter_finition",
                                onSelect = { opt ->
                                    finitionFilter = if (opt == "Toutes") {
                                        "Toutes"
                                    } else {
                                        val cur = if (finitionFilter == "Toutes") "" else finitionFilter
                                        toggleFinitionSelection(cur, opt).ifBlank { "Toutes" }
                                    }
                                }
                            )
                            StockFilterLine(
                                title = "Couleur",
                                options = colorOptions,
                                selected = colorFilter,
                                activeColor = Emerald500,
                                labelColor = Emerald300,
                                matchesOption = { opt, sel -> normalizeSearchText(opt) == normalizeSearchText(sel) },
                                colorHexForOption = { opt -> colorHexMap[opt] },
                                testTagPrefix = "stock_filter_color",
                                onSelect = { opt ->
                                    colorFilter = if (opt != "Toutes" && normalizeSearchText(colorFilter) == normalizeSearchText(opt)) "Toutes" else opt
                                }
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = Amber500.copy(alpha = 0.25f))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Slate900,
                        border = BorderStroke(1.2.dp, Slate700),
                        modifier = Modifier.widthIn(max = 420.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Amber400, modifier = Modifier.size(48.dp))
                            Text(
                                text = if (products.isEmpty()) "Catalogue vide — Prêt pour vos modèles" else "Aucun article dans ce filtre",
                                style = MaterialTheme.typography.titleMedium,
                                color = Slate100,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = if (products.isEmpty()) {
                                    "Ajoutez vos modèles avec « + Modèle » (plinthe 8/60 ou 8/45 créée automatiquement) ou importez votre fichier Excel (.xlsx)."
                                } else {
                                    "Modifiez les filtres ou la recherche pour afficher d'autres articles."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                            if (products.isEmpty() && (canAddProducts || canModifyStock)) {
                                Button(
                                    onClick = { showNewFiche = true },
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("stock_empty_add_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnGold)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ajouter mon premier modèle", fontWeight = FontWeight.Black)
                                }
                                if (onImportSpreadsheetCatalog != null) {
                                    OutlinedButton(
                                        onClick = { showExcelDialog = true },
                                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("stock_empty_excel_btn"),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.3.dp, Emerald500)
                                    ) {
                                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = Emerald400, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Importer Excel (.xlsx)", color = Emerald400, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    contentPadding = PaddingValues(if (isNarrow) 8.dp else 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (isNarrow) 8.dp else 10.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isNarrow) 8.dp else 10.dp),
                    modifier = Modifier.fillMaxSize().testTag("stock_catalog_grid")
                ) {
                    items(filtered, key = { it.id }, contentType = { "tile_product" }) { p ->
                        StockGridProductCard(
                            product = p,
                            precomputedDesignation = effectiveIndex.designationById[p.id],
                            isNarrow = isNarrow,
                            canModifyStock = canModifyStock,
                            canViewPrixAchat = canViewPrixAchat,
                            onPreviewImage = { previewProduct = p },
                            onEditFiche = { ficheProduct = p },
                            onRequestDelete = if (canModifyStock && onDeleteProduct != null) { { pendingDeleteProduct = p } } else null,
                            onRequestDispo = {
                                onRequestDepotDispo(p)
                                dispoTargetProduct = p
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(if (isNarrow) 8.dp else 12.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isNarrow) 8.dp else 10.dp),
                    modifier = Modifier.fillMaxSize().testTag("stock_catalog_list")
                ) {
                    items(filtered, key = { it.id }, contentType = { "tile_product" }) { p ->
                        StockListProductCard(
                            product = p,
                            precomputedDesignation = effectiveIndex.designationById[p.id],
                            isNarrow = isNarrow,
                            canModifyStock = canModifyStock,
                            canViewPrixAchat = canViewPrixAchat,
                            onPreviewImage = { previewProduct = p },
                            onEditFiche = { ficheProduct = p },
                            onRequestDelete = if (canModifyStock && onDeleteProduct != null) { { pendingDeleteProduct = p } } else null,
                            onRequestDispo = {
                                onRequestDepotDispo(p)
                                dispoTargetProduct = p
                            }
                        )
                    }
                }
            }
        }
    }

    if (showNewFiche || ficheProduct != null) {
        ProductFicheDialog(
            initial = ficheProduct,
            existingUsines = usines.drop(1),
            existingFinitions = finitions.drop(1),
            canViewPrixAchat = canViewPrixAchat,
            onDelete = if (ficheProduct != null && canModifyStock && onDeleteProduct != null) {
                {
                    val toDelete = ficheProduct
                    ficheProduct = null
                    showNewFiche = false
                    pendingDeleteProduct = toDelete
                }
            } else null,
            onDismiss = {
                ficheProduct = null
                showNewFiche = false
            },
            onSave = { prod, createMatchingPlinthe, plinthePrix, createMotifs, createFrise ->
                when {
                    onSaveProductWithAuto != null -> onSaveProductWithAuto(prod, createMatchingPlinthe, plinthePrix, createMotifs, createFrise)
                    onSaveProductWithPlinthe != null -> onSaveProductWithPlinthe(prod, createMatchingPlinthe, plinthePrix)
                    else -> onSaveProduct(prod)
                }
                ficheProduct = null
                showNewFiche = false
            }
        )
    }

    if (showQuickInventory && onApplyQuickInventory != null) {
        StockInventoryDialog(
            products = products,
            canViewPrixAchat = canViewPrixAchat,
            onDismiss = { showQuickInventory = false },
            onApplyInventory = { deltaMap ->
                onApplyQuickInventory(deltaMap)
                showQuickInventory = false
            }
        )
    }

    pendingDeleteProduct?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDeleteProduct = null },
            containerColor = Slate900,
            titleContentColor = Rose300,
            textContentColor = Slate100,
            title = { Text("Supprimer cet article ?", fontWeight = FontWeight.Black) },
            text = {
                Text(
                    "Voulez-vous supprimer « ${formatProductDesignation(target)} » du catalogue ? Idéal pour nettoyer les doublons ou les articles arrêtés.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProduct?.invoke(target.id)
                        pendingDeleteProduct = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark),
                    modifier = Modifier.testTag("stock_confirm_delete_btn")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Supprimer", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDeleteProduct = null },
                    modifier = Modifier.testTag("stock_cancel_delete_btn")
                ) {
                    Text("Annuler", color = Slate200, fontWeight = FontWeight.Bold)
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
            onOpenCalculator = null,
            onUpdatePhoto = if ((canAddProducts || canModifyStock) && onUpdateProductPhoto != null) {
                { savedPath ->
                    onUpdateProductPhoto(prod.id, savedPath)
                    previewProduct = prod.copy(image = savedPath)
                }
            } else null
        )
    }

    if (showExcelDialog && onImportSpreadsheetCatalog != null) {
        BackHandler { showExcelDialog = false }
        Dialog(onDismissRequest = { showExcelDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Surface(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).padding(16.dp).testTag("stock_excel_dialog"),
                    shape = RoundedCornerShape(20.dp),
                    color = Slate900,
                    border = BorderStroke(1.5.dp, Emerald500)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Catalogue Excel (.xlsx)",
                                style = MaterialTheme.typography.titleLarge,
                                color = BannerGold,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { showExcelDialog = false },
                                modifier = Modifier.height(40.dp).testTag("stock_excel_close_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Rose500, contentColor = OnDark),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("FERMER", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Text(
                            text = "1) Téléchargez le modèle Excel (.xlsx), remplissez vos articles rapidement sur téléphone ou PC, 2) Partagez-le vers le téléphone Vendeur, 3) Importez le fichier ici.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )

                        Button(
                            onClick = {
                                showExcelDialog = false
                                launchExcelImportPicker()
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("stock_import_excel_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Importer un Fichier Excel (.xlsx)", fontWeight = FontWeight.Black)
                        }

                        HorizontalDivider(color = Slate800)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val saved = withContext(Dispatchers.IO) {
                                            val dir = File(context.cacheDir, "catalog_exports").apply { mkdirs() }
                                            val f = CatalogSpreadsheetEngine.exportBlankTemplateXlsx(File(dir, "CASACERAM_MODELE_VIERGE.xlsx"))
                                            DailyClosingReportEngine.saveFileToPublicDownloads(
                                                context,
                                                f,
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                            )
                                        }
                                        val msg = if (saved.isSuccess) {
                                            "Modèle enregistré dans Téléchargements/CASACERAM"
                                        } else {
                                            "Impossible d'enregistrer dans Téléchargements"
                                        }
                                        excelFeedback = msg
                                        onShowMessage?.invoke(msg)
                                    }
                                },
                                modifier = Modifier.weight(1f).height(46.dp).testTag("stock_download_template_xlsx_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnGold),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Modèle (.xlsx)", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val f = withContext(Dispatchers.IO) {
                                            val dir = File(context.cacheDir, "catalog_exports").apply { mkdirs() }
                                            CatalogSpreadsheetEngine.exportBlankTemplateXlsx(File(dir, "CASACERAM_MODELE_VIERGE.xlsx"))
                                        }
                                        shareExcelFile(f, "Modèle Excel CASACERAM (.xlsx)")
                                    }
                                },
                                modifier = Modifier.weight(1f).height(46.dp).testTag("stock_share_template_xlsx_btn"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.2.dp, Cyan500),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Partager Modèle", color = Cyan400, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val saved = withContext(Dispatchers.IO) {
                                            val dir = File(context.cacheDir, "catalog_exports").apply { mkdirs() }
                                            val f = CatalogSpreadsheetEngine.exportProductsXlsx(File(dir, "CASACERAM_CATALOGUE_ACTIF.xlsx"), products)
                                            DailyClosingReportEngine.saveFileToPublicDownloads(
                                                context,
                                                f,
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                            )
                                        }
                                        val msg = if (saved.isSuccess) {
                                            "Catalogue (.xlsx) enregistré dans Téléchargements/CASACERAM"
                                        } else {
                                            "Impossible d'enregistrer dans Téléchargements"
                                        }
                                        excelFeedback = msg
                                        onShowMessage?.invoke(msg)
                                    }
                                },
                                modifier = Modifier.weight(1f).height(46.dp).testTag("stock_download_active_xlsx_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnGold),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Télécharger Actif", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val f = withContext(Dispatchers.IO) {
                                            val dir = File(context.cacheDir, "catalog_exports").apply { mkdirs() }
                                            CatalogSpreadsheetEngine.exportProductsXlsx(File(dir, "CASACERAM_CATALOGUE_ACTIF.xlsx"), products)
                                        }
                                        shareExcelFile(f, "Catalogue CASACERAM (.xlsx)")
                                    }
                                },
                                modifier = Modifier.weight(1f).height(46.dp).testTag("stock_share_active_xlsx_btn"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.2.dp, Amber500),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Amber400, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Partager Actif", color = Amber400, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                            }
                        }

                        if (!excelFeedback.isNullOrBlank()) {
                            Text(
                                text = excelFeedback ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                color = Emerald300,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    pendingSpreadsheetImport?.let { res ->
        BackHandler { pendingSpreadsheetImport = null }
        Dialog(onDismissRequest = { pendingSpreadsheetImport = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Surface(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp).padding(16.dp).testTag("stock_excel_import_confirm_dialog"),
                    shape = RoundedCornerShape(20.dp),
                    color = Slate900,
                    border = BorderStroke(1.5.dp, Emerald500)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "${res.products.size} article(s) détecté(s) (${res.format})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Emerald400,
                            fontWeight = FontWeight.Black
                        )
                        res.products.take(4).forEach { prod ->
                            Text(
                                text = "• ${formatProductDesignation(prod)} (${formatDa(prod.prix)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate100
                            )
                        }
                        Button(
                            onClick = {
                                onImportSpreadsheetCatalog?.invoke(res.products, false)
                                pendingSpreadsheetImport = null
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("stock_excel_import_merge_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnGold)
                        ) {
                            Text("Ajouter / Mettre à jour le catalogue", fontWeight = FontWeight.Black)
                        }
                        Button(
                            onClick = {
                                onImportSpreadsheetCatalog?.invoke(res.products, true)
                                pendingSpreadsheetImport = null
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("stock_excel_import_replace_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = OnGold)
                        ) {
                            Text("Remplacer tout le catalogue actif", fontWeight = FontWeight.Black)
                        }
                        OutlinedButton(
                            onClick = { pendingSpreadsheetImport = null },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Slate700)
                        ) {
                            Text("Annuler", color = Slate400, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
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
private fun StockFilterLine(
    title: String,
    options: List<String>,
    selected: String,
    activeColor: Color,
    labelColor: Color,
    matchesOption: (String, String) -> Boolean = { a, b -> a.equals(b, ignoreCase = true) },
    formatOption: (String) -> String = { it },
    colorHexForOption: ((String) -> String?)? = null,
    testTagPrefix: String? = null,
    onSelect: (String) -> Unit
) {
    val selectedTextColor = if (activeColor == Amber500 || activeColor == Amber400 || activeColor == Cyan500 || activeColor == Yellow500) OnGold else OnDark
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.width(56.dp).height(28.dp),
            shape = RoundedCornerShape(7.dp),
            color = activeColor.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, activeColor.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
            }
        }
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(options, key = { it }) { opt ->
                val isAllOption = opt == "Toutes" || opt == "Tous"
                val isSel = if (isAllOption) (selected == "Toutes" || selected == "Tous") else (selected != "Toutes" && selected != "Tous" && matchesOption(opt, selected))
                val dotHex = colorHexForOption?.invoke(opt)
                Surface(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelect(opt) }
                        .let { mod -> if (testTagPrefix != null) mod.testTag("${testTagPrefix}_${opt}") else mod },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) activeColor else Slate950,
                    border = BorderStroke(1.dp, if (isSel) activeColor else Slate800)
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
                            text = formatOption(opt),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSel) selectedTextColor else Slate200,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StockGridProductCard(
    product: TileProduct,
    precomputedDesignation: String? = null,
    isNarrow: Boolean,
    canModifyStock: Boolean,
    canViewPrixAchat: Boolean,
    onPreviewImage: () -> Unit,
    onEditFiche: () -> Unit,
    onRequestDelete: (() -> Unit)? = null,
    onRequestDispo: () -> Unit
) {
    val designation = precomputedDesignation ?: remember(product.id, product.name, product.usine, product.dimension, product.finition, product.choix) {
        formatProductDesignation(product)
    }
    val isJeton = remember(product.id, product.name) { isJetonProduct(product) }
    Surface(
        color = if (isJeton) Yellow950 else Slate900,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(if (isJeton) 2.dp else 1.dp, if (isJeton) BannerGold else Slate800),
        shadowElevation = if (isJeton) 6.dp else 3.dp
    ) {
        Column(
            modifier = Modifier.padding(if (isNarrow) 9.dp else 11.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box {
                TileSwatchImage(
                    imageUrl = product.image,
                    colorHex = product.color,
                    contentDescription = designation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isNarrow) 108.dp else 124.dp)
                        .clickable(onClick = onPreviewImage)
                        .testTag("stock_photo_${product.id}"),
                    cornerRadius = 14.dp
                )
                val dimTheme = remember(product.dimension) { dimensionBadgeTheme(product.dimension) }
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isJeton) BannerGold else dimTheme.containerColor,
                    border = BorderStroke(1.dp, if (isJeton) Yellow300 else dimTheme.borderColor)
                ) {
                    Text(
                        text = if (isJeton) "★ JETON" else product.dimension,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isJeton) OnGold else dimTheme.textColor,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
                val finLabel = formatFinitionLabel(product.finition).ifBlank { product.finition.replaceFirstChar { it.uppercase() } }
                if (finLabel.isNotBlank()) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(5.dp),
                        shape = RoundedCornerShape(7.dp),
                        color = Purple950.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, Purple500.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = finLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = Purple300,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = designation,
                style = MaterialTheme.typography.titleSmall,
                color = Slate100,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${formatM2(product.m2Carton)} m²/ctn",
                style = MaterialTheme.typography.labelSmall,
                color = Cyan300,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val priceStr = if (product.isSoldByPiece && product.prixPiece > 0.0) {
                    "${formatDa(product.prixPiece)}/pc"
                } else {
                    "${formatDa(product.prix)}/m²"
                }
                Text(
                    text = priceStr,
                    style = PosNumericTextStyle.copy(fontSize = if (isNarrow) 14.sp else 15.sp),
                    color = Emerald400
                )
                if (canViewPrixAchat && product.prixAchat > 0.0) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Purple950) {
                        Text(
                            text = "Ach: ${formatDa(product.prixAchat)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Purple300,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onRequestDispo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("stock_dispo_btn_${product.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnGold),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Warehouse, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dispo Dépôt", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
            }

            if (canModifyStock) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onEditFiche)
                            .testTag("stock_edit_btn_${product.id}"),
                        shape = RoundedCornerShape(8.dp),
                        color = Amber950,
                        border = BorderStroke(1.dp, Amber500.copy(alpha = 0.65f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Modifier",
                                color = Amber300,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    if (onRequestDelete != null) {
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onRequestDelete)
                                .testTag("stock_delete_btn_${product.id}"),
                            shape = RoundedCornerShape(8.dp),
                            color = Rose950,
                            border = BorderStroke(1.dp, Rose500.copy(alpha = 0.8f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Supprimer article",
                                    tint = Rose300,
                                    modifier = Modifier.size(18.dp)
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
private fun StockListProductCard(
    product: TileProduct,
    precomputedDesignation: String? = null,
    isNarrow: Boolean,
    canModifyStock: Boolean,
    canViewPrixAchat: Boolean,
    onPreviewImage: () -> Unit,
    onEditFiche: () -> Unit,
    onRequestDelete: (() -> Unit)? = null,
    onRequestDispo: () -> Unit
) {
    val designation = precomputedDesignation ?: remember(product.id, product.name, product.usine, product.dimension, product.finition, product.choix) {
        formatProductDesignation(product)
    }
    val isJeton = remember(product.id, product.name) { isJetonProduct(product) }
    Surface(
        color = if (isJeton) Yellow950 else Slate900,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(if (isJeton) 2.dp else 1.dp, if (isJeton) BannerGold else Slate800),
        shadowElevation = if (isJeton) 5.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(if (isNarrow) 10.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box {
                    TileSwatchImage(
                        imageUrl = product.image,
                        colorHex = product.color,
                        contentDescription = designation,
                        modifier = Modifier
                            .size(if (isNarrow) 62.dp else 70.dp)
                            .clickable(onClick = onPreviewImage)
                            .testTag("stock_photo_${product.id}"),
                        cornerRadius = 13.dp
                    )
                    val dimTheme = remember(product.dimension) { dimensionBadgeTheme(product.dimension) }
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(2.dp),
                        shape = RoundedCornerShape(5.dp),
                        color = dimTheme.containerColor,
                        border = BorderStroke(0.8.dp, dimTheme.borderColor)
                    ) {
                        Text(
                            text = product.dimension,
                            style = MaterialTheme.typography.labelSmall,
                            color = dimTheme.textColor,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = designation,
                        style = if (isNarrow) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                        color = Slate100,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    val priceStr = if (product.isSoldByPiece && product.prixPiece > 0.0) {
                        "${formatDa(product.prixPiece)}/pc (${formatDa(product.prix)}/m²)"
                    } else {
                        "${formatDa(product.prix)}/m²"
                    }
                    Text(
                        text = "${formatM2(product.m2Carton)} m²/ctn • $priceStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = Emerald400,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val finLabel = formatFinitionLabel(product.finition).ifBlank { product.finition.replaceFirstChar { it.uppercase() } }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (finLabel.isNotBlank() && !designation.contains(finLabel, ignoreCase = true)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Purple950,
                                border = BorderStroke(0.8.dp, Purple500.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = finLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Purple300,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (product.isSoldByPiece) Purple950 else Slate850
                        ) {
                            Text(
                                text = if (product.isSoldByPiece) "Vente: Pièce" else "Vente: Carton",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (product.isSoldByPiece) Purple300 else Slate400,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (canViewPrixAchat && product.prixAchat > 0.0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Purple950
                            ) {
                                Text(
                                    text = "Achat: ${formatDa(product.prixAchat)}/m²",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Purple300,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRequestDispo,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("stock_dispo_btn_${product.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = OnGold),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Warehouse, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dispo Dépôt", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
                }
                if (canModifyStock) {
                    OutlinedButton(
                        onClick = onEditFiche,
                        modifier = Modifier.height(42.dp).testTag("stock_edit_btn_${product.id}"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, Amber500)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Éditer fiche", tint = Amber400, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fiche", color = Amber400, fontWeight = FontWeight.Black)
                    }
                    if (onRequestDelete != null) {
                        Surface(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onRequestDelete)
                                .testTag("stock_delete_btn_${product.id}"),
                            shape = RoundedCornerShape(10.dp),
                            color = Rose500,
                            border = BorderStroke(1.2.dp, Rose300.copy(alpha = 0.7f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer article", tint = OnDark, modifier = Modifier.size(19.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class FicheNumField(val decimal: Boolean) { M2_CTN(true), PRIX(false), PRIX_PIECE(false), PRIX_ACHAT(false), PLINTHE_PRIX(false) }
private enum class FicheAlphaField { NAME, USINE, DIMENSION, FINITION }

@Composable
private fun ProductFicheDialog(
    initial: TileProduct?,
    existingUsines: List<String> = emptyList(),
    existingFinitions: List<String> = emptyList(),
    canViewPrixAchat: Boolean,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onSave: (TileProduct, Boolean, Double, Boolean, Boolean) -> Unit
) {
    BackHandler { onDismiss() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var usine by remember { mutableStateOf(initial?.usine ?: "") }
    var dimension by remember { mutableStateOf(initial?.dimension ?: "60×60") }
    var finition by remember { mutableStateOf(initial?.finition ?: "") }
    var colorHex by remember { mutableStateOf(initial?.color ?: "#94A3B8") }
    var colorName by remember { mutableStateOf(initial?.colorName ?: "") }
    var pricingUnit by remember { mutableStateOf(initial?.pricingUnit ?: "carton") }
    var photoPath by remember { mutableStateOf(initial?.image ?: "") }
    var m2CtnStr by remember { mutableStateOf(formatM2(initial?.m2Carton ?: 1.44)) }
    var pcsStr by remember { mutableStateOf((initial?.pcs ?: 4).toString()) }
    var prixStr by remember { mutableStateOf((initial?.prix ?: 1200.0).toInt().toString()) }
    var prixPieceStr by remember { mutableStateOf((initial?.prixPiece ?: 0.0).toInt().toString()) }
    var prixAchatStr by remember { mutableStateOf((initial?.prixAchat ?: 950.0).toInt().toString()) }
    var autoCreatePlinthe by remember { mutableStateOf(initial == null) }
    var autoCreateMotifs by remember { mutableStateOf(initial == null) }
    var autoCreateFrise by remember { mutableStateOf(initial == null) }
    var plinthePrixStr by remember { mutableStateOf("250") }

    var activeField by remember { mutableStateOf(FicheNumField.PRIX) }
    var activeAlphaField by remember { mutableStateOf<FicheAlphaField?>(null) }
    var showKeypad by remember { mutableStateOf(false) }
    var showAlphaKeypad by remember { mutableStateOf(false) }
    var isPristine by remember { mutableStateOf(true) }
    var isAlphaPristine by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val saved = withContext(Dispatchers.IO) { saveProductPhotoFromUri(context, uri) }
                if (!saved.isNullOrBlank()) {
                    photoPath = saved
                }
            }
        }
    }

    fun applyDimensionAutoSpecs(rawDim: String) {
        dimension = rawDim
        if (initial == null) {
            when {
                isPlinthe8x60(rawDim) -> {
                    pricingUnit = "piece"
                    usine = ""
                    m2CtnStr = "1.0"
                    pcsStr = "15"
                }
                isPlinthe8x45(rawDim) -> {
                    pricingUnit = "carton"
                    usine = ""
                    m2CtnStr = "1.0"
                    pcsStr = "20"
                }
                isTile120x60(rawDim) -> {
                    pricingUnit = "piece"
                    m2CtnStr = "1.44"
                    pcsStr = "2"
                }
                isTile60x60(rawDim) -> {
                    pricingUnit = "carton"
                    m2CtnStr = "1.44"
                    pcsStr = "4"
                    plinthePrixStr = "250"
                }
                isTile45x45(rawDim) -> {
                    pricingUnit = "carton"
                    m2CtnStr = "1.42"
                    pcsStr = "7"
                    plinthePrixStr = "950"
                }
            }
        }
    }

    val isEligibleForAutoPlinthe = initial == null && (isTile60x60(dimension) || isTile45x45(dimension))
    val isEligibleForAutoMotifs = initial == null && isMotifEligibleDimension(dimension)
    val isEligibleForAutoFrise = initial == null && isTile120x60(dimension)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            SuppressSystemSoftKeyboard {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .fillMaxHeight(0.92f)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("product_fiche_dialog"),
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
                            Text(
                                text = if (initial == null) "Nouveau Modèle" else "Modifier Modèle",
                                style = MaterialTheme.typography.titleLarge,
                                color = BannerGold,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.height(44.dp).testTag("fiche_close_btn"),
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

                    // Photo du Modèle (affichée dans Stock et Vente)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Slate950,
                        border = BorderStroke(1.dp, if (photoPath.isNotBlank()) Purple500.copy(alpha = 0.65f) else Slate800)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TileSwatchImage(
                                imageUrl = photoPath,
                                colorHex = colorHex,
                                contentDescription = name.ifBlank { "Aperçu photo" },
                                modifier = Modifier.size(64.dp),
                                cornerRadius = 12.dp
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (photoPath.isNotBlank()) "Photo du produit active" else "Photo du modèle (optionnel)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (photoPath.isNotBlank()) Purple300 else Slate100,
                                    fontWeight = FontWeight.Black
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            showKeypad = false
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp).testTag("fiche_pick_photo_btn"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Purple500, contentColor = OnDark),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (photoPath.isNotBlank()) "Changer" else "Choisir Photo",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1
                                        )
                                    }
                                    if (photoPath.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = { photoPath = "" },
                                            modifier = Modifier.height(40.dp).testTag("fiche_clear_photo_btn"),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Rose500),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Retirer photo", tint = Rose400, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    fun openAlphaField(field: FicheAlphaField) {
                        activeAlphaField = field
                        showAlphaKeypad = true
                        isAlphaPristine = false
                        showKeypad = false
                    }

                    ClearableOutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Désignation / Nom du modèle") },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                    openAlphaField(FicheAlphaField.NAME)
                                }
                            }
                            .onFocusChanged { if (it.isFocused) openAlphaField(FicheAlphaField.NAME) }
                            .testTag("fiche_name_input")
                    )
                val isPlintheForm = (initial?.categorie?.trim()?.equals("plinthe", ignoreCase = true) == true) ||
                    isPlinthe8x60(dimension) || isPlinthe8x45(dimension)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClearableOutlinedTextField(
                        value = if (isPlintheForm && initial == null) "" else usine,
                        onValueChange = { if (!(isPlintheForm && initial == null)) usine = it },
                        label = { Text(if (isPlintheForm) "Usine (Sans usine)" else "Usine (optionnel)") },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier
                            .weight(1f)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                    openAlphaField(FicheAlphaField.USINE)
                                }
                            }
                            .onFocusChanged { if (it.isFocused) openAlphaField(FicheAlphaField.USINE) }
                            .testTag("fiche_usine_input")
                    )
                    ClearableOutlinedTextField(
                        value = dimension,
                        onValueChange = { applyDimensionAutoSpecs(it) },
                        label = { Text("Dimension (Format)") },
                        onDismissKeypad = { showKeypad = false },
                        modifier = Modifier
                            .weight(1f)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                    openAlphaField(FicheAlphaField.DIMENSION)
                                }
                            }
                            .onFocusChanged { if (it.isFocused) openAlphaField(FicheAlphaField.DIMENSION) }
                            .testTag("fiche_dimension_input")
                    )
                }

                if (!isPlintheForm) {
                    val factoryChoices = remember(existingUsines) {
                        (listOf("BCRAM", "NOVA", "CEVICO", "NCM", "BATI") + existingUsines)
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .distinctBy { normalizeSearchText(it) }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Usine :", style = MaterialTheme.typography.labelSmall, color = Slate400, fontWeight = FontWeight.Bold)
                        factoryChoices.forEach { fac ->
                            val selectedFac = normalizeSearchText(usine) == normalizeSearchText(fac) && usine.isNotBlank()
                            FilterChip(
                                selected = selectedFac,
                                onClick = {
                                    usine = if (selectedFac) "" else fac
                                    showKeypad = false
                                },
                                label = { Text(fac, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Amber500,
                                    selectedLabelColor = OnGold,
                                    containerColor = Slate950,
                                    labelColor = Slate400
                                )
                            )
                        }
                    }
                }

                // Quick Dimension Presets (all 24 POS dimensions)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Format :", style = MaterialTheme.typography.labelSmall, color = Slate400, fontWeight = FontWeight.Bold)
                    POS_DIMENSIONS_ORDER.forEach { dim ->
                        val dimTheme = dimensionBadgeTheme(dim)
                        val isSel = normalizeDimensionKey(dimension) == normalizeDimensionKey(dim)
                        FilterChip(
                            selected = isSel,
                            onClick = { applyDimensionAutoSpecs(dim); showKeypad = false },
                            label = { Text(dim, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = dimTheme.containerColor,
                                selectedLabelColor = dimTheme.textColor,
                                containerColor = Slate950,
                                labelColor = dimTheme.borderColor
                            ),
                            border = BorderStroke(1.dp, if (isSel) dimTheme.textColor else dimTheme.borderColor.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("fiche_dim_chip_${dim.replace("×", "x").replace("/", "_")}")
                        )
                    }
                }

                // Finitions (multi-sélection : ex. Polie + Rectifié, Mat + Rectifié, Satiné, Rustic...)
                ClearableOutlinedTextField(
                    value = finition,
                    onValueChange = { finition = it },
                    label = { Text("Finitions (multi-choix : Polie + Rectifié, Mat + Rectifié...)") },
                    onDismissKeypad = { showKeypad = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                openAlphaField(FicheAlphaField.FINITION)
                            }
                        }
                        .onFocusChanged { if (it.isFocused) openAlphaField(FicheAlphaField.FINITION) }
                        .testTag("fiche_finition_input")
                )
                val finitionChoices = remember(existingFinitions) {
                    (POS_FINITION_OPTIONS + existingFinitions.flatMap { splitFinitions(it) })
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .distinctBy { normalizeSearchText(it) }
                }
                val activeFinitionTokens = remember(finition) {
                    splitFinitions(finition).map { normalizeSearchText(it) }.toSet()
                }
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = finition.isBlank(),
                        onClick = { finition = ""; showKeypad = false },
                        label = { Text("Aucune", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate700,
                            selectedLabelColor = OnDark,
                            containerColor = Slate950,
                            labelColor = Slate400
                        ),
                        modifier = Modifier.testTag("fiche_finition_none_chip")
                    )
                    finitionChoices.forEach { fin ->
                        val selectedFin = normalizeSearchText(fin) in activeFinitionTokens
                        FilterChip(
                            selected = selectedFin,
                            onClick = {
                                finition = toggleFinitionSelection(finition, fin)
                                showKeypad = false
                            },
                            label = {
                                Text(
                                    (if (selectedFin) "✓ " else "") + (formatFinitionChipLabel(fin).ifBlank { fin.replaceFirstChar { it.uppercase() } }),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple500,
                                selectedLabelColor = OnDark,
                                containerColor = Slate950,
                                labelColor = Slate400
                            ),
                            modifier = Modifier.testTag("fiche_finition_chip_${normalizeSearchText(fin)}")
                        )
                    }
                }

                // Couleur (optionnelle — 13 couleurs exactes, appuyer pour choisir, ré-appuyer pour désélectionner)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Couleur :", style = MaterialTheme.typography.labelSmall, color = Slate400, fontWeight = FontWeight.Bold)
                    FilterChip(
                        selected = colorName.isBlank(),
                        onClick = {
                            colorName = ""
                            colorHex = "#94A3B8"
                            showKeypad = false
                        },
                        label = { Text("Aucune", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate700,
                            selectedLabelColor = OnDark,
                            containerColor = Slate950,
                            labelColor = Slate400
                        ),
                        modifier = Modifier.testTag("fiche_color_none_chip")
                    )
                    POS_COLOR_OPTIONS.forEach { opt ->
                        val swatchColor = remember(opt.hex) {
                            runCatching { Color(android.graphics.Color.parseColor(opt.hex)) }.getOrElse { Color.Gray }
                        }
                        val isColorSelected = colorName.isNotBlank() && normalizeSearchText(colorName) == normalizeSearchText(opt.label)
                        FilterChip(
                            selected = isColorSelected,
                            onClick = {
                                if (isColorSelected) {
                                    colorName = ""
                                    colorHex = "#94A3B8"
                                } else {
                                    colorHex = opt.hex
                                    colorName = opt.label
                                }
                                showKeypad = false
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                )
                            },
                            label = { Text(opt.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber500,
                                selectedLabelColor = OnGold,
                                containerColor = Slate950,
                                labelColor = Slate400
                            ),
                            modifier = Modifier.testTag("fiche_color_chip_${normalizeSearchText(opt.label)}")
                        )
                    }
                }

                // Mode de vente: Carton (m²) vs Pièce
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vendu par :", style = MaterialTheme.typography.labelMedium, color = Slate400, fontWeight = FontWeight.Bold)
                    FilterChip(
                        selected = pricingUnit == "carton",
                        onClick = { pricingUnit = "carton"; showKeypad = false },
                        label = { Text("Carton (m²)", fontWeight = FontWeight.Black) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Cyan500,
                            selectedLabelColor = OnGold,
                            containerColor = Slate950,
                            labelColor = Slate400
                        ),
                        modifier = Modifier.testTag("fiche_unit_carton")
                    )
                    FilterChip(
                        selected = pricingUnit == "piece",
                        onClick = { pricingUnit = "piece"; showKeypad = false },
                        label = { Text("Pièce", fontWeight = FontWeight.Black) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Amber500,
                            selectedLabelColor = OnGold,
                            containerColor = Slate950,
                            labelColor = Slate400
                        ),
                        modifier = Modifier.testTag("fiche_unit_piece")
                    )
                }

                NumericDisplayField(
                    label = "Colisage (m² / Carton)",
                    value = m2CtnStr,
                    isActive = showKeypad && activeField == FicheNumField.M2_CTN,
                    onSelect = { activeField = FicheNumField.M2_CTN; isPristine = true; showKeypad = true; showAlphaKeypad = false },
                    onClear = { m2CtnStr = ""; isPristine = false },
                    suffix = "m²/ctn",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "fiche_m2ctn_field"
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val parsedColis = m2CtnStr.replace(',', '.').toDoubleOrNull() ?: 0.0
                    STANDARD_USER_COLISAGES.forEach { preset ->
                        val isSel = kotlin.math.abs(parsedColis - preset) < 0.005
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                m2CtnStr = formatM2(preset)
                                showKeypad = false
                            },
                            label = { Text(formatM2(preset), fontWeight = FontWeight.Black) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Cyan500,
                                selectedLabelColor = OnGold,
                                containerColor = Slate950,
                                labelColor = Slate200
                            ),
                            modifier = Modifier.testTag("fiche_colisage_chip_${formatM2(preset).replace(".", "_")}")
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericDisplayField(
                        label = "Prix Vente/m²",
                        value = prixStr,
                        isActive = showKeypad && activeField == FicheNumField.PRIX,
                        onSelect = { activeField = FicheNumField.PRIX; isPristine = true; showKeypad = true; showAlphaKeypad = false },
                        onClear = { prixStr = ""; isPristine = false },
                        suffix = "DA",
                        accentColor = Emerald500,
                        modifier = Modifier.weight(1f),
                        testTag = "fiche_prix_field"
                    )
                    NumericDisplayField(
                        label = if (pricingUnit == "piece") "Prix / Pièce *" else "Prix / Pièce",
                        value = prixPieceStr,
                        isActive = showKeypad && activeField == FicheNumField.PRIX_PIECE,
                        onSelect = { activeField = FicheNumField.PRIX_PIECE; isPristine = true; showKeypad = true; showAlphaKeypad = false },
                        onClear = { prixPieceStr = ""; isPristine = false },
                        suffix = "DA",
                        accentColor = Cyan500,
                        modifier = Modifier.weight(1f),
                        testTag = "fiche_prix_piece_field"
                    )
                }

                if (canViewPrixAchat) {
                    NumericDisplayField(
                        label = "Prix Achat/m² (Confidentiel Admin)",
                        value = prixAchatStr,
                        isActive = showKeypad && activeField == FicheNumField.PRIX_ACHAT,
                        onSelect = { activeField = FicheNumField.PRIX_ACHAT; isPristine = true; showKeypad = true; showAlphaKeypad = false },
                        onClear = { prixAchatStr = ""; isPristine = false },
                        suffix = "DA",
                        accentColor = Purple500,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "fiche_prix_achat_field"
                    )
                }

                if (isEligibleForAutoPlinthe) {
                    val plintheDimLabel = if (isTile60x60(dimension)) "8/60 (Pièce)" else "8/45 (Carton)"
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (autoCreatePlinthe) Amber950 else Slate950,
                        border = BorderStroke(1.2.dp, if (autoCreatePlinthe) Amber500 else Slate800)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Créer plinthe assortie $plintheDimLabel",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (autoCreatePlinthe) Amber300 else Slate100,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Ajoute automatiquement la plinthe correspondante du même nom",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400
                                    )
                                }
                                Switch(
                                    checked = autoCreatePlinthe,
                                    onCheckedChange = { autoCreatePlinthe = it },
                                    modifier = Modifier.testTag("fiche_auto_plinthe_switch")
                                )
                            }
                            if (autoCreatePlinthe) {
                                NumericDisplayField(
                                    label = if (isTile60x60(dimension)) "Prix Plinthe 8/60 (DA/pièce)" else "Prix Plinthe 8/45 (DA/m² ctn)",
                                    value = plinthePrixStr,
                                    isActive = showKeypad && activeField == FicheNumField.PLINTHE_PRIX,
                                    onSelect = { activeField = FicheNumField.PLINTHE_PRIX; isPristine = true; showKeypad = true; showAlphaKeypad = false },
                                    onClear = { plinthePrixStr = ""; isPristine = false },
                                    suffix = "DA",
                                    accentColor = Amber500,
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "fiche_plinthe_prix_field"
                                )
                            }
                        }
                    }
                }

                if (isEligibleForAutoMotifs) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (autoCreateMotifs) Cyan950 else Slate950,
                        border = BorderStroke(1.2.dp, if (autoCreateMotifs) Cyan500 else Slate800)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "Créer Motifs Cuisine / Douche / Mozaik",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (autoCreateMotifs) Cyan300 else Slate100,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Ajoute les motifs vendus à la pièce (sans doublon Clair/Foncé)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate400
                                )
                            }
                            Switch(
                                checked = autoCreateMotifs,
                                onCheckedChange = { autoCreateMotifs = it },
                                modifier = Modifier.testTag("fiche_auto_motifs_switch")
                            )
                        }
                    }
                }

                if (isEligibleForAutoFrise) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (autoCreateFrise) Amber950 else Slate950,
                        border = BorderStroke(1.2.dp, if (autoCreateFrise) Amber500 else Slate800)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "Créer Frise 10/60 assortie (FRIZA)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (autoCreateFrise) Amber300 else Slate100,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Ajoute automatiquement la frise 10/60 vendue à la pièce",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate400
                                )
                            }
                            Switch(
                                checked = autoCreateFrise,
                                onCheckedChange = { autoCreateFrise = it },
                                modifier = Modifier.testTag("fiche_auto_frise_switch")
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = showAlphaKeypad && activeAlphaField != null, enter = expandVertically(), exit = shrinkVertically()) {
                    val curAlpha = when (activeAlphaField) {
                        FicheAlphaField.NAME -> name
                        FicheAlphaField.USINE -> usine
                        FicheAlphaField.DIMENSION -> dimension
                        FicheAlphaField.FINITION -> finition
                        null -> ""
                    }
                    NonImeAlphanumericKeypad(
                        onKeyPress = { key ->
                            val (next, nextPristine) = applyAlphanumericKeypadInput(
                                current = curAlpha,
                                key = key,
                                isPristine = isAlphaPristine,
                                uppercase = activeAlphaField != FicheAlphaField.FINITION
                            )
                            when (activeAlphaField) {
                                FicheAlphaField.NAME -> name = next
                                FicheAlphaField.USINE -> if (!(isPlintheForm && initial == null)) usine = next
                                FicheAlphaField.DIMENSION -> applyDimensionAutoSpecs(next)
                                FicheAlphaField.FINITION -> finition = next
                                null -> Unit
                            }
                            isAlphaPristine = nextPristine
                        },
                        onClear = {
                            when (activeAlphaField) {
                                FicheAlphaField.NAME -> name = ""
                                FicheAlphaField.USINE -> if (!(isPlintheForm && initial == null)) usine = ""
                                FicheAlphaField.DIMENSION -> dimension = ""
                                FicheAlphaField.FINITION -> finition = ""
                                null -> Unit
                            }
                            isAlphaPristine = false
                        },
                        onValidate = {
                            showAlphaKeypad = false
                            activeAlphaField = null
                        },
                        validateLabel = "OK"
                    )
                }

                AnimatedVisibility(visible = showKeypad, enter = expandVertically(), exit = shrinkVertically()) {
                    NonImeNumericKeypad(
                        onKeyPress = { key ->
                            val cur = when (activeField) {
                                FicheNumField.M2_CTN -> m2CtnStr
                                FicheNumField.PRIX -> prixStr
                                FicheNumField.PRIX_PIECE -> prixPieceStr
                                FicheNumField.PRIX_ACHAT -> prixAchatStr
                                FicheNumField.PLINTHE_PRIX -> plinthePrixStr
                            }
                            val (next, nextPristine) = applyKeypadInput(cur, key, activeField.decimal, isPristine)
                            when (activeField) {
                                FicheNumField.M2_CTN -> m2CtnStr = next
                                FicheNumField.PRIX -> prixStr = next
                                FicheNumField.PRIX_PIECE -> prixPieceStr = next
                                FicheNumField.PRIX_ACHAT -> prixAchatStr = next
                                FicheNumField.PLINTHE_PRIX -> plinthePrixStr = next
                            }
                            isPristine = nextPristine
                        },
                        allowDecimal = activeField.decimal,
                        onValidate = { showKeypad = false },
                        validateLabel = "OK"
                    )
                    } // End of scrollable body Column

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. PINNED Footer Buttons (Always 100% visible)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val cleanDim = dimension.trim().ifBlank { "60×60" }
                                    val safePrixAchat = if (canViewPrixAchat) {
                                        (prixAchatStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                                    } else {
                                        initial?.prixAchat ?: 0.0
                                    }
                                    val effectiveCategorie = when {
                                        isPlinthe8x60(cleanDim) || isPlinthe8x45(cleanDim) -> "plinthe"
                                        else -> initial?.categorie?.takeIf { it.isNotBlank() } ?: "sol"
                                    }
                                    val cleanUsine = if (effectiveCategorie == "plinthe" && (initial == null || usine.equals("BCRAM", ignoreCase = true))) {
                                        ""
                                    } else {
                                        usine.trim()
                                    }
                                    val plinthePrixVal = (plinthePrixStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                                    onSave(
                                        TileProduct(
                                            id = initial?.id ?: "p_${UUID.randomUUID().toString().take(8)}",
                                            name = name.trim(),
                                            usine = cleanUsine,
                                            dimension = cleanDim,
                                            m2Carton = (m2CtnStr.toDoubleOrNull() ?: 1.44).coerceAtLeast(0.01),
                                            pcs = (pcsStr.toIntOrNull() ?: 4).coerceAtLeast(1),
                                            prix = (prixStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
                                            prixAchat = safePrixAchat,
                                            stock = initial?.stock ?: 0,
                                            color = colorHex,
                                            colorName = colorName.trim(),
                                            image = photoPath.trim(),
                                            finition = finition.trim().lowercase(),
                                            choix = initial?.choix ?: "",
                                            categorie = effectiveCategorie,
                                            description = initial?.description ?: listOf(name.trim(), cleanUsine).filter { it.isNotBlank() }.joinToString(" - "),
                                            pricingUnit = pricingUnit,
                                            prixPiece = (prixPieceStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
                                            bain = "",
                                            calibre = "",
                                            prixGros = 0.0
                                        ),
                                        isEligibleForAutoPlinthe && autoCreatePlinthe,
                                        plinthePrixVal,
                                        isEligibleForAutoMotifs && autoCreateMotifs,
                                        isEligibleForAutoFrise && autoCreateFrise
                                    )
                                }
                            },
                            enabled = name.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("fiche_save_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (initial == null) "Ajouter au Catalogue" else "Enregistrer Modèle",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                        }

                        if (initial != null && onDelete != null) {
                            OutlinedButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("fiche_delete_product_btn"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, Rose500),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose300)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Rose400, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Supprimer cet article", color = Rose300, fontWeight = FontWeight.Black)
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
