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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.DepotRepository
import com.example.data.UiState
import com.example.ui.DepotViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.screens.DeliveriesScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.RestockScreen
import com.example.ui.theme.BrickRed
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DepotNoteTheme
import com.example.ui.theme.HairlineSand
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.OchreAmberBright
import com.example.ui.theme.StoneMuted
import com.example.ui.theme.Terracotta
import com.example.ui.theme.TerracottaSoft
import com.example.ui.theme.WarmPaper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            DepotNoteTheme {
                AppNavigation()
            }
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
private fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            DepotNoteApp(currentUser = user)
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
private fun DepotNoteApp(currentUser: FirebaseUser) {
    val uid = currentUser.uid
    val viewModel: DepotViewModel = viewModel(
        key = uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                DepotViewModel(DepotRepository(db), uid)
            }
        }
    )

    val deliveryNotesState by viewModel.deliveryNotesState.collectAsStateWithLifecycle()
    val restockNotesState by viewModel.restockNotesState.collectAsStateWithLifecycle()
    val journalState by viewModel.journalState.collectAsStateWithLifecycle()
    val feedback by viewModel.feedback.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(DepotTab.DELIVERIES) }
    var showAccountDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val undoLabel = stringResource(R.string.action_undo)

    // Back press on secondary tabs returns to Deliveries main tab
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

    val pendingDeliveriesCount = (deliveryNotesState as? UiState.Success)?.data?.size ?: 0
    val pendingRestockCount = (restockNotesState as? UiState.Success)?.data?.size ?: 0

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmPaper)
    ) {
        val isWide = maxWidth >= 840.dp

        Scaffold(
            containerColor = WarmPaper,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                DepotTopBar(
                    activeTab = currentTab,
                    userDisplayName = currentUser.displayName ?: currentUser.email ?: "Compte",
                    onOpenAccount = { showAccountDialog = true }
                )
            },
            bottomBar = {
                if (!isWide) {
                    NavigationBar(
                        containerColor = CardWhite,
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
                                                    containerColor = if (tab == DepotTab.DELIVERIES) Terracotta else OchreAmberBright,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(badgeCount.toString())
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
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Terracotta,
                                    selectedTextColor = Terracotta,
                                    indicatorColor = TerracottaSoft,
                                    unselectedIconColor = StoneMuted,
                                    unselectedTextColor = StoneMuted
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
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
                        containerColor = CardWhite,
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
                                                    containerColor = if (tab == DepotTab.DELIVERIES) Terracotta else OchreAmberBright,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(badgeCount.toString())
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
                                    selectedIconColor = Terracotta,
                                    selectedTextColor = Terracotta,
                                    indicatorColor = TerracottaSoft,
                                    unselectedIconColor = StoneMuted,
                                    unselectedTextColor = StoneMuted
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (currentTab) {
                        DepotTab.DELIVERIES -> DeliveriesScreen(
                            state = deliveryNotesState,
                            onSaveNote = { id, name, phone, address, items ->
                                viewModel.saveDeliveryNote(id, name, phone, address, items)
                            },
                            onDeleteNote = { viewModel.deleteDeliveryNote(it) },
                            onCheckOffLine = { note, lineId -> viewModel.checkOffDeliveryLine(note, lineId) }
                        )

                        DepotTab.RESTOCK -> RestockScreen(
                            state = restockNotesState,
                            onAddRestock = { name, qty, urgent -> viewModel.addRestockNote(name, qty, urgent) },
                            onUpdateRestock = { id, name, qty, urgent -> viewModel.updateRestockNote(id, name, qty, urgent) },
                            onCheckOffPurchased = { viewModel.checkOffRestockNote(it) },
                            onDeleteRestock = { viewModel.deleteRestockNote(it) }
                        )

                        DepotTab.JOURNAL -> JournalScreen(
                            state = journalState,
                            onDeleteEntry = { viewModel.deleteJournalEntry(it) }
                        )
                    }
                }
            }
        }
    }

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Compte connecté",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkCharcoal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = currentUser.displayName ?: "Utilisateur Google",
                        style = MaterialTheme.typography.titleMedium,
                        color = InkCharcoal
                    )
                    if (!currentUser.email.isNullOrBlank()) {
                        Text(
                            text = currentUser.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StoneMuted
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccountDialog = false
                        signOutUser(credentialManager, scope)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrickRed),
                    modifier = Modifier.testTag("confirm_sign_out_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.auth_sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Fermer", color = StoneMuted)
                }
            }
        )
    }
}

@Composable
private fun DepotTopBar(
    activeTab: DepotTab,
    userDisplayName: String,
    onOpenAccount: () -> Unit
) {
    val initial = userDisplayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "D"
    Surface(
        color = CardWhite,
        shadowElevation = 1.dp,
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
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                    )
                    Column {
                        Text(
                            text = when (activeTab) {
                                DepotTab.DELIVERIES -> stringResource(R.string.deliveries_title)
                                DepotTab.RESTOCK -> stringResource(R.string.restock_title)
                                DepotTab.JOURNAL -> stringResource(R.string.journal_title)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = InkCharcoal
                        )
                    }
                }

                Surface(
                    onClick = onOpenAccount,
                    shape = CircleShape,
                    color = TerracottaSoft,
                    border = BorderStroke(1.dp, Terracotta.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .size(40.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("account_avatar_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleSmall,
                            color = Terracotta,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            HorizontalDivider(color = HairlineSand)
        }
    }
}
