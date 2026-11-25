package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.DepotDatabase
import com.example.data.DepotRepository
import com.example.ui.DepotViewModel
import com.example.ui.screens.DeliveriesScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.RestockScreen
import com.example.ui.theme.BoldGreenBright
import com.example.ui.theme.BoldGreenSoft
import com.example.ui.theme.BoldRed
import com.example.ui.theme.BoldYellow
import com.example.ui.theme.Bordeaux
import com.example.ui.theme.BordeauxBorder
import com.example.ui.theme.BordeauxSoft
import com.example.ui.theme.DepotNoteTheme
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCanvas
import com.example.ui.theme.NavySurface
import com.example.ui.theme.OnBoldYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            DepotNoteTheme {
                DepotNoteApp()
            }
        }
    }
}

private enum class DepotTab(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DELIVERIES(
        labelRes = R.string.tab_deliveries,
        selectedIcon = Icons.Filled.LocalShipping,
        unselectedIcon = Icons.Outlined.LocalShipping,
        testTag = "nav_tab_deliveries"
    ),
    RESTOCK(
        labelRes = R.string.tab_restock,
        selectedIcon = Icons.Filled.Inventory2,
        unselectedIcon = Icons.Outlined.Inventory2,
        testTag = "nav_tab_restock"
    ),
    JOURNAL(
        labelRes = R.string.tab_journal,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
        testTag = "nav_tab_journal"
    )
}

@Composable
private fun DepotNoteApp() {
    val viewModel: DepotViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val dao = DepotDatabase.getInstance(app).depotDao()
                DepotViewModel(DepotRepository(dao))
            }
        }
    )

    val deliveryNotes by viewModel.deliveryNotes.collectAsStateWithLifecycle()
    val restockNotes by viewModel.restockNotes.collectAsStateWithLifecycle()
    val journalEntries by viewModel.journalEntries.collectAsStateWithLifecycle()
    val feedback by viewModel.feedback.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(DepotTab.DELIVERIES) }
    val snackbarHostState = remember { SnackbarHostState() }
    val undoLabel = stringResource(R.string.action_undo)

    BackHandler(enabled = currentTab != DepotTab.DELIVERIES) {
        currentTab = DepotTab.DELIVERIES
    }

    LaunchedEffect(feedback?.id) {
        val msg = feedback ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = msg.text,
            actionLabel = if (msg.undoAction != null) undoLabel else null,
            duration = if (msg.undoAction != null) SnackbarDuration.Long else SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed && msg.undoAction != null) {
            viewModel.performUndo(msg.undoAction)
        }
        viewModel.consumeFeedback()
    }

    val pendingDeliveriesCount = deliveryNotes.size
    val pendingRestockCount = restockNotes.size

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyCanvas)
    ) {
        val isWide = maxWidth >= 840.dp

        Scaffold(
            containerColor = NavyCanvas,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Bordeaux,
                        contentColor = TextPrimary,
                        actionColor = BoldYellow
                    )
                }
            },
            topBar = {
                DepotTopBar(activeTab = currentTab)
            },
            bottomBar = {
                if (!isWide) {
                    Column {
                        HorizontalDivider(color = BordeauxBorder, thickness = 1.dp)
                        NavigationBar(
                            containerColor = NavySurface,
                            tonalElevation = 0.dp,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            DepotTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                val badgeCount = when (tab) {
                                    DepotTab.DELIVERIES -> pendingDeliveriesCount
                                    DepotTab.RESTOCK -> pendingRestockCount
                                    DepotTab.JOURNAL -> 0
                                }
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (badgeCount > 0) {
                                                    Badge(
                                                        containerColor = if (tab == DepotTab.DELIVERIES) BoldRed else BoldYellow,
                                                        contentColor = if (tab == DepotTab.DELIVERIES) TextPrimary else OnBoldYellow
                                                    ) {
                                                        Text(badgeCount.toString(), fontWeight = FontWeight.ExtraBold)
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = stringResource(tab.labelRes)
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(tab.labelRes),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BoldYellow,
                                        selectedTextColor = BoldYellow,
                                        indicatorColor = Bordeaux,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
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
                if (isWide) {
                    NavigationRail(
                        containerColor = NavySurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        DepotTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            val badgeCount = when (tab) {
                                DepotTab.DELIVERIES -> pendingDeliveriesCount
                                DepotTab.RESTOCK -> pendingRestockCount
                                DepotTab.JOURNAL -> 0
                            }
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (badgeCount > 0) {
                                                Badge(
                                                    containerColor = if (tab == DepotTab.DELIVERIES) BoldRed else BoldYellow,
                                                    contentColor = if (tab == DepotTab.DELIVERIES) TextPrimary else OnBoldYellow
                                                ) {
                                                    Text(badgeCount.toString(), fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = stringResource(tab.labelRes)
                                        )
                                    }
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = BoldYellow,
                                    selectedTextColor = BoldYellow,
                                    indicatorColor = Bordeaux,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                    HorizontalDivider(color = NavyBorder, modifier = Modifier.fillMaxHeight().size(width = 1.dp, height = 1.dp))
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (currentTab) {
                        DepotTab.DELIVERIES -> DeliveriesScreen(
                            notes = deliveryNotes,
                            onSaveNote = { id, name, phone, address, waitingMs, urgent, articles ->
                                viewModel.saveDeliveryNote(id, name, phone, address, waitingMs, urgent, articles)
                            },
                            onAddArticleToNote = { noteId, text ->
                                viewModel.addArticleToExistingNote(noteId, text)
                            },
                            onUpdateWaitingInfo = { noteId, waitingMs, urgent ->
                                viewModel.updateNoteWaitingInfo(noteId, waitingMs, urgent)
                            },
                            onDeleteNote = { viewModel.deleteDeliveryNote(it) },
                            onCheckOffArticle = { note, article ->
                                viewModel.checkOffDeliveryArticle(note, article)
                            }
                        )

                        DepotTab.RESTOCK -> RestockScreen(
                            notes = restockNotes,
                            onAddRestock = { name, qty, urgent -> viewModel.addRestockNote(name, qty, urgent) },
                            onUpdateRestock = { id, name, qty, urgent -> viewModel.updateRestockNote(id, name, qty, urgent) },
                            onCheckOffPurchased = { viewModel.checkOffRestockNote(it) },
                            onDeleteRestock = { viewModel.deleteRestockNote(it) }
                        )

                        DepotTab.JOURNAL -> JournalScreen(
                            entries = journalEntries,
                            onDeleteEntry = { viewModel.deleteJournalEntry(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DepotTopBar(activeTab: DepotTab) {
    Surface(
        color = Bordeaux,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_depotnote_fg_1795641076800),
                        contentDescription = "DepotNote",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Column {
                        Text(
                            text = when (activeTab) {
                                DepotTab.DELIVERIES -> stringResource(R.string.deliveries_title)
                                DepotTab.RESTOCK -> stringResource(R.string.restock_title)
                                DepotTab.JOURNAL -> stringResource(R.string.journal_title)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "DepotNote",
                            style = MaterialTheme.typography.labelSmall,
                            color = BoldYellow
                        )
                    }
                }

                // Clean Offline Indicator Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BoldGreenSoft,
                    border = BorderStroke(1.dp, BoldGreenBright),
                    modifier = Modifier.testTag("offline_status_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OfflinePin,
                            contentDescription = null,
                            tint = BoldGreenBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.offline_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = BoldGreenBright,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            HorizontalDivider(color = BordeauxSoft, thickness = 1.dp)
        }
    }
}
