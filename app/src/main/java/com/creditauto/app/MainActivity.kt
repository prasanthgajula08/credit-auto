package com.creditauto.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Settings
import com.creditauto.app.ui.components.AppIcons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.creditauto.app.data.location.LocationHelper
import com.creditauto.app.ui.CreditAutoViewModel
import com.creditauto.app.ui.screens.MyCardsScreen
import com.creditauto.app.ui.screens.PayNowScreen
import com.creditauto.app.ui.screens.SavedPlacesScreen
import com.creditauto.app.ui.screens.SettingsScreen
import com.creditauto.app.ui.theme.CreditAutoTheme

private enum class MainTab(
    val label: String,
    val headerTitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    PAY_NOW("Pay", "Credit Auto", AppIcons.Contactless, AppIcons.ContactlessOutlined),
    MY_CARDS("Cards", "My cards", AppIcons.CreditCard, AppIcons.CreditCard),
    PLACES("Places", "Saved places", Icons.Rounded.Place, Icons.Outlined.Place),
    SETTINGS("Settings", "Settings", Icons.Rounded.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: CreditAutoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CreditAutoTheme {
                CreditAutoRoot(
                    viewModel = viewModel,
                    checkLocationPermission = { LocationHelper.hasLocationPermission(this) },
                    checkBackgroundLocationPermission = { LocationHelper.hasBackgroundLocationPermission(this) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditAutoRoot(
    viewModel: CreditAutoViewModel,
    checkLocationPermission: () -> Boolean,
    checkBackgroundLocationPermission: () -> Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    var activeTab by remember { mutableStateOf(MainTab.PAY_NOW) }
    var hasLocationPermission by remember { mutableStateOf(checkLocationPermission()) }
    var hasBackgroundPermission by remember { mutableStateOf(checkBackgroundLocationPermission()) }
    var pendingEnableBackgroundPrefetch by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasBackgroundPermission = granted || checkBackgroundLocationPermission()
        pendingEnableBackgroundPrefetch = false
        viewModel.setBackgroundPrefetchEnabled(hasBackgroundPermission)
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasLocationPermission = checkLocationPermission()
        hasBackgroundPermission = checkBackgroundLocationPermission()
        if (hasLocationPermission) {
            viewModel.detectNearbyStores(forceRefresh = false)
            if (pendingEnableBackgroundPrefetch) {
                if (hasBackgroundPermission) {
                    pendingEnableBackgroundPrefetch = false
                    viewModel.setBackgroundPrefetchEnabled(true)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
        } else {
            pendingEnableBackgroundPrefetch = false
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasLocationPermission = checkLocationPermission()
                hasBackgroundPermission = checkBackgroundLocationPermission()
                if (hasLocationPermission) {
                    viewModel.detectNearbyStores(forceRefresh = false)
                }
                if (pendingEnableBackgroundPrefetch && hasBackgroundPermission) {
                    pendingEnableBackgroundPrefetch = false
                    viewModel.setBackgroundPrefetchEnabled(true)
                }
                viewModel.resumePendingInstallIfPermitted()
                if (uiState.settings.autoCheckUpdates) {
                    viewModel.checkForUpdates(manual = false)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.session.transientBanner) {
        val message = uiState.session.transientBanner
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearBanner()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = activeTab.headerTitle,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                MainTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { activeTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)

        when (activeTab) {
            MainTab.PAY_NOW -> PayNowScreen(
                uiState = uiState,
                updateStatus = updateStatus,
                hasLocationPermission = hasLocationPermission,
                onInstallUpdate = { viewModel.downloadAndInstallUpdate() },
                onRequestLocationAndDetect = {
                    if (checkLocationPermission()) {
                        hasLocationPermission = true
                        viewModel.detectNearbyStores(forceRefresh = true)
                    } else {
                        val toRequest = buildList {
                            add(Manifest.permission.ACCESS_FINE_LOCATION)
                            add(Manifest.permission.ACCESS_COARSE_LOCATION)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        permissionsLauncher.launch(toRequest.toTypedArray())
                    }
                },
                onSelectStore = { viewModel.selectStore(it) },
                onSelectCategory = { viewModel.selectCategory(it) },
                onManualSearch = { viewModel.applyManualStoreQuery(it) },
                onToggleMobileWallet = { viewModel.toggleMobileWallet() },
                onToggleAllowAmex = { viewModel.toggleAllowAmex() },
                onToggleVisaOnly = { viewModel.toggleVisaOnly() },
                onToggleForeign = { viewModel.toggleForeignTransaction() },
                onPayWithWallet = { viewModel.launchGoogleWallet(it) },
                onGoToMyCards = { activeTab = MainTab.MY_CARDS },
                onSaveStorePlace = { name, cat, nets, preferredCardId ->
                    viewModel.savePlace(
                        name = name,
                        category = cat,
                        acceptedNetworks = nets,
                        preferredCardId = preferredCardId
                    )
                },
                modifier = contentModifier
            )

            MainTab.MY_CARDS -> MyCardsScreen(
                uiState = uiState,
                onToggleCardOwned = { id, owned -> viewModel.toggleCardOwned(id, owned) },
                onToggleCardActive = { id, active -> viewModel.toggleCardActive(id, active) },
                onSaveCard = { viewModel.saveCard(it) },
                onDeleteCard = { viewModel.deleteCard(it) },
                modifier = contentModifier
            )

            MainTab.PLACES -> SavedPlacesScreen(
                uiState = uiState,
                onAddPlace = { name, cat, attachGps, nets, preferredCardId ->
                    viewModel.savePlace(
                        name = name,
                        category = cat,
                        latitude = if (attachGps) uiState.session.lastLatitude else null,
                        longitude = if (attachGps) uiState.session.lastLongitude else null,
                        acceptedNetworks = nets,
                        preferredCardId = preferredCardId
                    )
                },
                onDeletePlace = { viewModel.deletePlace(it) },
                onUsePlaceNow = { store ->
                    viewModel.selectStore(store)
                    activeTab = MainTab.PAY_NOW
                },
                modifier = contentModifier
            )

            MainTab.SETTINGS -> SettingsScreen(
                uiState = uiState,
                updateStatus = updateStatus,
                hasBackgroundLocationPermission = hasBackgroundPermission,
                onToggleBackgroundPrefetch = { wantEnable ->
                    if (!wantEnable) {
                        pendingEnableBackgroundPrefetch = false
                        viewModel.setBackgroundPrefetchEnabled(false)
                    } else if (checkBackgroundLocationPermission()) {
                        hasBackgroundPermission = true
                        viewModel.setBackgroundPrefetchEnabled(true)
                    } else if (!checkLocationPermission()) {
                        pendingEnableBackgroundPrefetch = true
                        permissionsLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        pendingEnableBackgroundPrefetch = true
                        backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                },
                onToggleAutoCheckUpdates = { viewModel.updateAutoCheckUpdates(it) },
                onUpdateRepoSlug = { viewModel.updateUpdateRepoSlug(it) },
                onCheckForUpdates = { viewModel.checkForUpdates(manual = true) },
                onInstallUpdate = { viewModel.downloadAndInstallUpdate() },
                onUpdatePointValuation = { curr, cpp -> viewModel.updatePointValuation(curr, cpp) },
                onUpdateGooglePlacesKey = { viewModel.updateGooglePlacesApiKey(it) },
                onUpdateSearchRadius = { viewModel.updateSearchRadius(it) },
                onUpdateShowNotification = { viewModel.updateShowCheckoutNotification(it) },
                onUpdateDefaultMobileWallet = { viewModel.updateDefaultUseMobileWallet(it) },
                modifier = contentModifier
            )
        }
    }
}
