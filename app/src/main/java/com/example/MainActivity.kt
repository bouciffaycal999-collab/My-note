package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Image
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.AppDatabase
import com.example.data.repository.PosRepository
import com.example.ui.PosUiState
import com.example.ui.PosViewModel
import com.example.ui.components.InvoicePreviewPrintSheet
import com.example.ui.components.SmartTileCalculatorSheet
import com.example.ui.screens.CaisseScreen
import com.example.ui.screens.ClosingReportScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.NotificationsSheet
import com.example.ui.screens.PosScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StaffLockScreen
import com.example.ui.screens.StockScreen
import com.example.ui.screens.SuppliersScreen
import com.example.ui.screens.SurfaceHelperScreen
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
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo950
import com.example.ui.theme.KaderPosTheme
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.OnGold
import com.example.ui.theme.Purple300
import com.example.ui.theme.Purple500
import com.example.ui.theme.Purple950
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.util.SaleSystemNotifier
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val pendingNotificationNav = MutableStateFlow<Pair<Boolean, String?>>(false to null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        window.decorView.layoutDirection = android.view.View.LAYOUT_DIRECTION_LTR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.isForceDarkAllowed = false
        }
        SaleSystemNotifier.ensureChannels(applicationContext)
        captureNotificationIntent(intent)
        setContent {
            KaderPosTheme {
                val navTarget by pendingNotificationNav.collectAsStateWithLifecycle()
                KaderCeramicPosApp(
                    pendingNotificationTarget = navTarget,
                    onConsumeNotificationTarget = { pendingNotificationNav.value = false to null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        captureNotificationIntent(intent)
    }

    private fun captureNotificationIntent(intent: Intent?) {
        if (intent == null) return
        val openNotifs = intent.getBooleanExtra(SaleSystemNotifier.EXTRA_OPEN_NOTIFICATIONS, false)
        val invoiceId = intent.getStringExtra(SaleSystemNotifier.EXTRA_INVOICE_ID)
        if (openNotifs || !invoiceId.isNullOrBlank()) {
            pendingNotificationNav.value = true to invoiceId
        }
    }
}

private data class NavTabMeta(
    val key: String,
    val label: String,
    val icon: ImageVector
)

private val ALL_NAV_TABS = listOf(
    NavTabMeta("pos", "Vente", Icons.Default.Storefront),
    NavTabMeta("surface", "Surface", Icons.Default.SquareFoot),
    NavTabMeta("stock", "Stock", Icons.Default.Inventory2),
    NavTabMeta("invoices", "Factures", Icons.AutoMirrored.Filled.ReceiptLong),
    NavTabMeta("cloture", "Clôture", Icons.Default.Summarize),
    NavTabMeta("dashboard", "Stats", Icons.Default.BarChart),
    NavTabMeta("customers", "Clients", Icons.Default.Group),
    NavTabMeta("suppliers", "Fournisseurs", Icons.Default.Factory),
    NavTabMeta("caisse", "Caisse", Icons.Default.PointOfSale),
    NavTabMeta("settings", "Réglages", Icons.Default.Settings)
)

@Composable
fun KaderCeramicPosApp(
    pendingNotificationTarget: Pair<Boolean, String?> = false to null,
    onConsumeNotificationTarget: () -> Unit = {}
) {
    val viewModel: PosViewModel = viewModel(
        key = "offline_local",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val localRepo = PosRepository(app.applicationContext, AppDatabase.getInstance(app.applicationContext))
                PosViewModel(
                    repository = localRepo,
                    appContext = app.applicationContext
                )
            }
        }
    )

    KaderPosSignedInContent(
        viewModel = viewModel,
        pendingNotificationTarget = pendingNotificationTarget,
        onConsumeNotificationTarget = onConsumeNotificationTarget
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KaderPosSignedInContent(
    viewModel: PosViewModel,
    pendingNotificationTarget: Pair<Boolean, String?>,
    onConsumeNotificationTarget: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMoreTabsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(pendingNotificationTarget, state.isLoading, state.isLocked) {
        val (shouldOpen, invoiceId) = pendingNotificationTarget
        if (shouldOpen && !state.isLoading && !state.isLocked) {
            if (!invoiceId.isNullOrBlank()) {
                viewModel.openInvoiceById(invoiceId)
            } else {
                viewModel.openNotificationsSheet()
            }
            onConsumeNotificationTarget()
        }
    }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    if (state.isLoading) {
        Surface(modifier = Modifier.fillMaxSize(), color = Slate950) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = BannerGold)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    "Chargement Casaceram...",
                    color = Slate100,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }
        return
    }

    if (state.isLocked) {
        StaffLockScreen(
            users = state.users,
            initialSelectedUser = state.currentUser,
            pinError = state.pinError,
            onClearError = { viewModel.clearPinError() },
            onUnlock = { user, pin -> viewModel.attemptUnlock(user, pin) }
        )
        return
    }

    val effectivePerms = state.effectivePermissions
    val isAdmin = state.currentUser?.role == "admin"
    val allowedTabKeys = effectivePerms.allowedTabs
    val allowedTabs = remember(allowedTabKeys) {
        ALL_NAV_TABS.filter { allowedTabKeys.contains(it.key) }
    }

    if (state.activeTab != "pos") {
        BackHandler { viewModel.selectTab("pos") }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isNarrow = maxWidth < 360.dp
        val isMediumOrExpanded = maxWidth >= 600.dp
        val primaryPhoneTabs = remember(allowedTabs) { allowedTabs.take(4) }
        val overflowPhoneTabs = remember(allowedTabs) { allowedTabs.drop(4) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Slate950,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Surface(
                    color = Navy950,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, BannerGold.copy(alpha = 0.7f)),
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.img_casaceram_minimal_logo),
                                        contentDescription = "Logo CASACERAM",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(2.dp)
                                    )
                                }
                                Text(
                                    text = state.storeInfo.nom.ifBlank { "CASACERAM" },
                                    style = if (isNarrow) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                                    color = BannerGold,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    onClick = {
                                        if (isAdmin) viewModel.switchToSellerMode() else viewModel.lockSession()
                                    },
                                    shape = RoundedCornerShape(7.dp),
                                    color = if (isAdmin) Amber950 else Emerald950,
                                    border = BorderStroke(1.dp, if (isAdmin) BannerGold else BannerEmerald),
                                    modifier = Modifier.testTag("topbar_role_badge")
                                ) {
                                    Text(
                                        text = if (isAdmin) "ADMIN" else "VENDEUR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isAdmin) Amber300 else Emerald300,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = { viewModel.lockSession() },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = if (isAdmin) Amber500 else BannerGold,
                                        contentColor = OnGold
                                    ),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("topbar_lock_btn")
                                ) {
                                    Icon(
                                        imageVector = if (isAdmin) Icons.Default.Lock else Icons.Default.AdminPanelSettings,
                                        contentDescription = if (isAdmin) "Changer / Verrouiller session" else "Accès Administrateur (PIN)",
                                        tint = OnGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = BannerGold.copy(alpha = 0.45f), thickness = 1.dp)
                    }
                }
            },
            bottomBar = {
                if (!isMediumOrExpanded) {
                    Column {
                        HorizontalDivider(color = Slate800, thickness = 1.dp)
                        NavigationBar(
                            containerColor = Navy950,
                            tonalElevation = 0.dp
                        ) {
                            primaryPhoneTabs.forEach { tab ->
                                NavigationBarItem(
                                    selected = state.activeTab == tab.key,
                                    onClick = { viewModel.selectTab(tab.key) },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = {
                                        Text(
                                            tab.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Amber400,
                                        selectedTextColor = BannerGold,
                                        indicatorColor = Amber950,
                                        unselectedIconColor = Slate400,
                                        unselectedTextColor = Slate400
                                    ),
                                    modifier = Modifier.testTag("nav_${tab.key}")
                                )
                            }
                            if (overflowPhoneTabs.isNotEmpty()) {
                                val isOverflowSelected = overflowPhoneTabs.any { it.key == state.activeTab }
                                NavigationBarItem(
                                    selected = isOverflowSelected,
                                    onClick = { showMoreTabsSheet = true },
                                    icon = { Icon(Icons.Default.Menu, contentDescription = "Plus") },
                                    label = { Text("Plus", maxLines = 1, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Amber400,
                                        selectedTextColor = BannerGold,
                                        indicatorColor = Amber950,
                                        unselectedIconColor = Slate400,
                                        unselectedTextColor = Slate400
                                    ),
                                    modifier = Modifier.testTag("nav_more")
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isMediumOrExpanded) {
                    NavigationRail(
                        containerColor = Navy950,
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(6.dp))
                        allowedTabs.forEach { tab ->
                            NavigationRailItem(
                                selected = state.activeTab == tab.key,
                                onClick = { viewModel.selectTab(tab.key) },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label, maxLines = 1, fontWeight = FontWeight.Bold) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Amber400,
                                    selectedTextColor = BannerGold,
                                    indicatorColor = Amber950,
                                    unselectedIconColor = Slate400,
                                    unselectedTextColor = Slate400
                                ),
                                modifier = Modifier.testTag("rail_${tab.key}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ActiveTabContent(
                        state = state,
                        viewModel = viewModel
                    )
                }
            }
        }

        if (showMoreTabsSheet) {
            BackHandler { showMoreTabsSheet = false }
            ModalBottomSheet(
                onDismissRequest = { showMoreTabsSheet = false },
                containerColor = Slate950,
                contentColor = Slate100
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Autres Modules",
                        style = MaterialTheme.typography.titleLarge,
                        color = Slate100,
                        fontWeight = FontWeight.Black
                    )
                    overflowPhoneTabs.forEach { tab ->
                        val isSel = state.activeTab == tab.key
                        val (chipBg, chipInk) = when (tab.key) {
                            "dashboard" -> Purple950 to Purple300
                            "customers" -> Cyan950 to Cyan300
                            "suppliers" -> Amber950 to Amber300
                            "caisse" -> Emerald950 to Emerald300
                            else -> Indigo950 to Indigo400
                        }
                        Surface(
                            onClick = {
                                viewModel.selectTab(tab.key)
                                showMoreTabsSheet = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("more_nav_${tab.key}"),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) Navy950 else Slate900,
                            contentColor = if (isSel) OnDark else Slate100,
                            border = if (isSel) BorderStroke(1.5.dp, BannerGold) else BorderStroke(1.dp, Slate800),
                            shadowElevation = if (isSel) 4.dp else 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) Amber500 else chipBg
                                ) {
                                    Icon(
                                        tab.icon,
                                        contentDescription = null,
                                        tint = if (isSel) OnGold else chipInk,
                                        modifier = Modifier.padding(8.dp).size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    tab.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSel) OnDark else Slate100,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        state.calculatorTarget?.let { target ->
            val activeEditCustomerTier = state.editingInvoice?.client?.id?.let { cid ->
                state.customers.find { it.id == cid }?.tier
            }
            SmartTileCalculatorSheet(
                product = target.product,
                editingCartItem = target.editingCartItem,
                isOneTimeProduct = target.isOneTimeProduct,
                canViewPrixAchat = effectivePerms.canViewPrixAchat,
                canModifyPrices = effectivePerms.canModifyPrices,
                canEditColisage = target.isOneTimeProduct || effectivePerms.canModifyStock,
                customerTier = activeEditCustomerTier,
                onDismiss = { viewModel.closeCalculator() },
                onConfirmProductCart = { product, cartons, pieces, m2Total, prixPerM2, bain, calibre, editId, prixPiece ->
                    viewModel.confirmCalculatorToCart(
                        product = product,
                        cartons = cartons,
                        pieces = pieces,
                        m2Total = m2Total,
                        prixPerM2 = prixPerM2,
                        bain = bain,
                        calibre = calibre,
                        editingItemId = editId,
                        prixPiece = prixPiece
                    )
                },
                onConfirmStandaloneFreeLine = { freeLine ->
                    viewModel.addFreeLine(freeLine)
                },
                onQuickAddColle = { bags, surfaceM2 ->
                    val colleProduct = state.products.find {
                        it.name.contains("colle", ignoreCase = true)
                    }
                    val unitPrice = colleProduct?.prix ?: 0.0
                    viewModel.addFreeLine(
                        com.example.data.model.FreeLineItem(
                            id = "colle_${java.util.UUID.randomUUID().toString().take(8)}",
                            designation = "Colle Ciment (${com.example.data.model.formatM2(surfaceM2)} m²)",
                            quantite = bags.toDouble(),
                            unite = "sacs",
                            prixUnitaire = unitPrice,
                            sousTotal = bags * unitPrice
                        )
                    )
                }
            )
        }

        state.previewInvoice?.let { inv ->
            InvoicePreviewPrintSheet(
                invoice = inv,
                storeInfo = state.storeInfo,
                settings = state.settings,
                autoEmailPdf = false,
                autoWhatsappPhoto = state.autoWhatsappPhoto,
                canHideInvoicePrices = effectivePerms.canHideInvoicePrices,
                isSellerModified = state.isPreviewSellerModified,
                onToggleHidePrices = { id, hide -> viewModel.setInvoiceHidePrices(id, hide) },
                onToggleHideDetails = { id, hide -> viewModel.setInvoiceHideDetails(id, hide) },
                onMarkWhatsappSent = { id -> viewModel.markInvoiceWhatsappSent(id) },
                onUpdateSettings = { viewModel.saveSettings(it) },
                onDismiss = { viewModel.closeInvoicePreview() }
            )
        }

        if (state.showNotificationsSheet) {
            NotificationsSheet(
                notifications = state.notifications,
                storeInfo = state.storeInfo,
                onOpenInvoice = { invoiceId -> viewModel.openInvoiceById(invoiceId) },
                onDismiss = { viewModel.closeNotificationsSheet() }
            )
        }
    }
}

@Composable
private fun ActiveTabContent(
    state: PosUiState,
    viewModel: PosViewModel
) {
    val perms = state.effectivePermissions
    val canViewPrixAchat = perms.canViewPrixAchat
    val isAdmin = state.currentUser?.role == "admin"
    val tabContext = androidx.compose.ui.platform.LocalContext.current
    when (state.activeTab) {
        "pos" -> PosScreen(
            products = state.products,
            catalogIndex = state.catalogIndex,
            cart = state.cart,
            freeLines = state.freeLines,
            customers = state.customers,
            storeInfo = state.storeInfo,
            editingInvoice = state.editingInvoice,
            canModifyPrices = perms.canModifyPrices,
            canEditColisage = perms.canModifyStock,
            onOpenCalculatorForProduct = { viewModel.openProductCalculator(it) },
            onRequestDepotDispo = { viewModel.requestDepotAvailability(it) },
            onOpenOneTimeProduct = { viewModel.openOneTimeProductCalculator() },
            onAddGuidedCartProduct = { product, cartons, pieces, m2, prix, prixPc ->
                viewModel.addGuidedCartProduct(product, cartons, pieces, m2, prix, prixPc)
            },
            onEditCartItem = { viewModel.openCartItemCalculator(it) },
            onUpdateCartItemDirect = { id, name, cartons, pieces, m2Ctn, pcs, prix, prixPiece ->
                viewModel.updateCartItemDirect(id, name, cartons, pieces, m2Ctn, pcs, prix, prixPiece)
            },
            onUpdateCartItemDirectWithUnit = { id, name, cartons, pieces, m2Ctn, pcs, prix, prixPiece, unit ->
                viewModel.updateCartItemDirect(id, name, cartons, pieces, m2Ctn, pcs, prix, prixPiece, unit)
            },
            onUpdateCartItemMeters = { id, meters -> viewModel.updateCartItemMeters(id, meters) },
            onAdjustCartItemCarton = { id, delta -> viewModel.adjustCartItemCartons(id, delta) },
            onAdjustCartItemLoosePiece = { id, delta -> viewModel.adjustCartItemLoosePieces(id, delta) },
            onToggleCartItemCc = { id -> viewModel.toggleCartItemCc(id) },
            onRemoveCartItem = { viewModel.removeCartItem(it) },
            onAddFreeLine = { viewModel.addFreeLine(it) },
            onUpdateFreeLine = { viewModel.updateFreeLine(it) },
            onAdjustFreeLineQty = { id, delta -> viewModel.adjustFreeLineQuantity(id, delta) },
            onRemoveFreeLine = { viewModel.removeFreeLine(it) },
            onClearCart = { viewModel.clearCart() },
            onFinalizeSale = { client, remise, paye, mode, notes ->
                viewModel.finalizeSale(client, remise, paye, mode, notes)
            },
            onFinalizeSaleWithDestination = { client, remise, paye, mode, notes, destination ->
                viewModel.finalizeSale(client, remise, paye, mode, notes, destination)
            },
            onFinalizeSaleExtended = { client, remise, paye, mode, notes, destination, isDevis, deferredDepotDelivery ->
                viewModel.finalizeSale(
                    client = client,
                    remise = remise,
                    montantPaye = paye,
                    modePaiement = mode,
                    notes = notes,
                    destination = destination,
                    isDevis = isDevis,
                    deferredDepotDelivery = deferredDepotDelivery
                )
            },
            returnDeductionDa = state.returnDeductionDa,
            onSetReturnDeduction = { viewModel.setReturnDeductionDa(it) },
            canUpdateProductPhoto = perms.canAddProducts || perms.canModifyStock,
            onUpdateProductPhoto = { productId, imagePath ->
                viewModel.updateProductPhoto(productId, imagePath)
            },
            heldCarts = state.heldCarts,
            settings = state.settings,
            isAdmin = isAdmin,
            onHoldCart = { label -> viewModel.holdCurrentCart(label) },
            onResumeHeldCart = { id -> viewModel.resumeHeldCart(id) },
            onDeleteHeldCart = { id -> viewModel.deleteHeldCart(id) },
            onVerifyAdminPin = { pin -> viewModel.verifyAdminPin(pin) },
            isBackupOverdue = state.isBackupOverdue,
            onQuickExportBackup = { viewModel.exportAndShareFullBackup(tabContext) }
        )

        "surface" -> SurfaceHelperScreen(
            products = state.products,
            canModifyPrices = state.effectivePermissions.canModifyPrices,
            onAddPackToCart = { items, summary -> viewModel.addSurfacePackToCart(items, summary) },
            onOpenPos = { viewModel.selectTab("pos") }
        )

        "stock" -> StockScreen(
            products = state.products,
            catalogIndex = state.catalogIndex,
            storeInfo = state.storeInfo,
            canModifyStock = perms.canModifyStock,
            canAddProducts = perms.canAddProducts,
            canViewPrixAchat = canViewPrixAchat,
            onAdjustStock = { id, delta -> viewModel.adjustStock(id, delta) },
            onSetStock = { id, stock -> viewModel.setStock(id, stock) },
            onSaveProduct = { viewModel.saveProduct(it) },
            onRequestDepotDispo = { viewModel.requestDepotAvailability(it) },
            onSaveProductWithPlinthe = { prod, createPlinthe, plinthePrix ->
                viewModel.saveProduct(prod, createPlinthe, plinthePrix)
            },
            onSaveProductWithAuto = { prod, createPlinthe, plinthePrix, createMotifs, createFrise ->
                viewModel.saveProduct(prod, createPlinthe, plinthePrix, createMotifs, createFrise)
            },
            onUpdateProductPhoto = { productId, imagePath ->
                viewModel.updateProductPhoto(productId, imagePath)
            },
            onImportSpreadsheetCatalog = { products, replaceAll ->
                viewModel.importSpreadsheetCatalog(products, replaceAll)
            },
            onDeleteProduct = { productId -> viewModel.deleteProduct(productId) },
            onApplyQuickInventory = { counts -> viewModel.applyQuickInventoryCounts(counts) },
            onShowMessage = { msg -> viewModel.showMessage(msg) }
        )

        "invoices" -> InvoicesScreen(
            invoices = state.invoices,
            canEdit = perms.canEditInvoices,
            canDelete = perms.canDeleteInvoices,
            onPreviewInvoice = { viewModel.openInvoicePreview(it) },
            onEditInvoice = { viewModel.startEditingInvoice(it) },
            onDeleteInvoice = { viewModel.deleteInvoice(it) },
            onConvertDevis = { id, mode -> viewModel.convertDevisToInvoice(id, mode) },
            onRecordReturn = { id, map, mode -> viewModel.recordInvoiceReturn(id, map, mode) },
            onUpdateDelivery = { id, map -> viewModel.updateInvoiceDelivery(id, map) },
            storeInfo = state.storeInfo,
            totalInvoiceCount = state.totalInvoiceCount,
            sqlSearchResults = state.invoiceSqlSearchResults,
            onSearchSql = { query -> viewModel.searchInvoicesSql(query) },
            onLoadMoreInvoices = { viewModel.loadMoreInvoices() },
            onRecordBackupDone = { viewModel.recordBackupDone() }
        )

        "cloture" -> ClosingReportScreen(
            invoices = state.invoices,
            products = state.products,
            storeInfo = state.storeInfo,
            settings = state.settings,
            isAdmin = isAdmin,
            currentUserName = state.currentUser?.name ?: "Vendeur",
            currentUserPhone = state.currentUser?.phone ?: "",
            importedCategoryMap = state.importedCategoryMap,
            onImportClosingJsonList = { rawList, onResult ->
                viewModel.importClosingReports(rawList, onResult)
            },
            onShowMessage = { viewModel.showMessage(it) },
            onOpenDashboard = { viewModel.selectTab("dashboard") },
            lastBackupTimestampMs = state.lastBackupTimestampMs,
            isBackupOverdue = state.isBackupOverdue,
            onBuildFullBackupFile = { ctx -> viewModel.buildFullBackupFile(ctx) }
        )

        "dashboard" -> DashboardScreen(
            invoices = state.invoices,
            customers = state.customers,
            canViewPrixAchat = canViewPrixAchat,
            products = state.products,
            extraCategoryMap = state.importedCategoryMap
        )

        "customers" -> CustomersScreen(
            customers = state.customers,
            onSaveCustomer = { viewModel.saveCustomer(it) },
            onPayDebt = { id, amt -> viewModel.payCustomerDebt(id, amt) },
            invoices = state.invoices,
            lastVersementReceipt = state.lastVersementReceipt,
            storeInfo = state.storeInfo,
            onDismissVersementReceipt = { viewModel.dismissVersementReceipt() }
        )

        "suppliers" -> SuppliersScreen(
            suppliers = state.suppliers,
            movements = state.supplierMovements,
            onSaveSupplier = { viewModel.saveSupplier(it) },
            onRecordVersement = { id, amt, motif, ref -> viewModel.recordSupplierVersement(id, amt, motif, ref) },
            onRecordInvoice = { id, amt, motif, ref -> viewModel.recordSupplierInvoice(id, amt, motif, ref) },
            onRecordInvoiceWithPaid = { id, amt, motif, ref, paidNow -> viewModel.recordSupplierInvoice(id, amt, motif, ref, paidNow) },
            products = state.products,
            onRecordArrivage = { id, lines, paidNow, blRef, note ->
                viewModel.recordSupplierArrivage(id, lines, paidNow, blRef, note)
            }
        )

        "caisse" -> CaisseScreen(
            movements = state.caisseMovements,
            dailyCounts = state.dailyCaisseCounts,
            activeStore = state.settings.activeStore,
            onAddMovement = { type, motif, amt -> viewModel.addCaisseMovement(type, motif, amt) },
            onSaveDailyCount = { f, v, e, s, th, c, n ->
                viewModel.saveDailyCaisseCount(f, v, e, s, th, c, n)
            }
        )

        "settings" -> {
            val settingsCtx = androidx.compose.ui.platform.LocalContext.current
            SettingsScreen(
                storeInfo = state.storeInfo,
                settings = state.settings,
                autoWhatsappPhoto = state.autoWhatsappPhoto,
                isAdmin = isAdmin,
                onChangeAdminPin = { viewModel.changeAdminPin(it) },
                onSaveStoreInfo = { viewModel.saveStoreInfo(it) },
                onSaveSettings = { viewModel.saveSettings(it) },
                onSetAutoWhatsappPhoto = { viewModel.setAutoWhatsappPhoto(it) },
                onResetCatalog = { viewModel.resetCatalog() },
                onExportJson = { viewModel.exportAllDataJson(it) },
                onImportJson = { viewModel.importAllDataJson(it) },
                productsCount = state.products.size,
                activeProducts = state.products,
                onClearActiveCatalog = { viewModel.clearActiveCatalog() },
                onExportArchivedCatalog = { onLoaded -> viewModel.loadArchivedCatalog(onLoaded) },
                onImportSpreadsheetCatalog = { products, replaceAll -> viewModel.importSpreadsheetCatalog(products, replaceAll) },
                onExportFullBackup = { viewModel.exportAndShareFullBackup(settingsCtx) },
                onRestoreFullBackup = { raw -> viewModel.restoreFullBackupJson(raw) }
            )
        }
    }
}

