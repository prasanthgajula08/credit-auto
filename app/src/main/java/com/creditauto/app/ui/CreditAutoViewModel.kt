package com.creditauto.app.ui

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creditauto.app.BuildConfig
import com.creditauto.app.CreditAutoApp
import com.creditauto.app.data.location.BackgroundLocationPrefetchManager
import com.creditauto.app.data.location.LocationHelper
import com.creditauto.app.domain.AppSettings
import com.creditauto.app.domain.CachedStoreSnapshot
import com.creditauto.app.domain.CardNetwork
import com.creditauto.app.domain.CardRecommendation
import com.creditauto.app.domain.CardRecommendationEngine
import com.creditauto.app.domain.CheckoutContext
import com.creditauto.app.domain.CreditCard
import com.creditauto.app.domain.DetectedStore
import com.creditauto.app.domain.MerchantCategoryClassifier
import com.creditauto.app.domain.RewardCurrency
import com.creditauto.app.domain.SavedPlace
import com.creditauto.app.domain.SpendCategory
import com.creditauto.app.domain.StoreSource
import com.creditauto.app.update.InstallLaunchResult
import com.creditauto.app.update.ReleaseUpdateInfo
import com.creditauto.app.update.UpdateStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

@Immutable
data class PayNowSessionState(
    val selectedStore: DetectedStore = DetectedStore(
        name = "General Purchase",
        category = SpendCategory.OTHER,
        source = StoreSource.MANUAL
    ),
    val nearbyStores: List<DetectedStore> = emptyList(),
    val isDetectingStore: Boolean = false,
    val statusMessage: String? = null,
    val lastLatitude: Double? = null,
    val lastLongitude: Double? = null,
    val isMobileWallet: Boolean = true,
    val isForeignTransaction: Boolean = false,
    val allowAmex: Boolean = true,
    val visaOnlyOverride: Boolean = false,
    val transientBanner: String? = null
)

@Immutable
data class CreditAutoUiState(
    val allCards: List<CreditCard> = emptyList(),
    val ownedCards: List<CreditCard> = emptyList(),
    val savedPlaces: List<SavedPlace> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val session: PayNowSessionState = PayNowSessionState(),
    val recommendations: List<CardRecommendation> = emptyList(),
    val isGoogleWalletInstalled: Boolean = true
)

class CreditAutoViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CreditAutoApp
    private val repository = app.repository
    private val storeDetector = app.storeDetector
    private val walletLauncher = app.walletLauncher
    private val appUpdater = app.appUpdater

    private val walletInstalledFlow = MutableStateFlow(true)
    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()
    private var pendingDownloadedApk: File? = null
    private var awaitingInstallPermission: Boolean = false
    private var lastAutoUpdateCheckMs: Long = 0L
    private var detectionJob: Job? = null

    private val sessionState = MutableStateFlow(
        PayNowSessionState(
            isMobileWallet = repository.settings.value.defaultUseMobileWallet
        )
    )

    init {
        // Query PackageManager once off the main thread and hydrate instant store cache from disk
        viewModelScope.launch {
            walletInstalledFlow.value = withContext(Dispatchers.IO) {
                walletLauncher.isGoogleWalletInstalled()
            }
        }
        viewModelScope.launch {
            repository.awaitLoaded()
            val cached = repository.cachedStores.value
            val now = System.currentTimeMillis()
            if (
                cached != null &&
                cached.stores.isNotEmpty() &&
                (now - cached.timestampMs) in 0..(25 * 60_000L) &&
                sessionState.value.nearbyStores.isEmpty()
            ) {
                sessionState.value = sessionState.value.copy(
                    selectedStore = cached.stores.first(),
                    nearbyStores = cached.stores,
                    lastLatitude = cached.latitude,
                    lastLongitude = cached.longitude
                )
            }
            if (repository.settings.value.autoCheckUpdates) {
                checkForUpdates(manual = false)
            }
        }
    }

    val uiState: StateFlow<CreditAutoUiState> = combine(
        repository.cards,
        repository.savedPlaces,
        repository.settings,
        sessionState,
        walletInstalledFlow
    ) { cards, places, settings, session, walletInstalled ->
        val context = CheckoutContext(
            store = session.selectedStore,
            isMobileWallet = session.isMobileWallet,
            isForeignTransaction = session.isForeignTransaction,
            allowAmex = session.allowAmex,
            visaOnlyOverride = session.visaOnlyOverride
        )
        val recommendations = CardRecommendationEngine.recommend(
            cards = cards,
            context = context,
            pointValuations = settings.pointValuations
        )
        CreditAutoUiState(
            allCards = cards,
            ownedCards = cards.filter { it.isOwned },
            savedPlaces = places,
            settings = settings,
            session = session,
            recommendations = recommendations,
            isGoogleWalletInstalled = walletInstalled
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreditAutoUiState()
        )

    fun detectNearbyStores(forceRefresh: Boolean = false) {
        detectionJob?.cancel()
        detectionJob = viewModelScope.launch {
            if (!LocationHelper.hasLocationPermission(app)) {
                sessionState.value = sessionState.value.copy(
                    isDetectingStore = false,
                    statusMessage = "Location permission needed to auto-detect the store you're in."
                )
                return@launch
            }

            repository.awaitLoaded()

            // Only show "Locating..." text if we don't already have nearby stores on screen
            val hadStores = sessionState.value.nearbyStores.isNotEmpty()
            sessionState.value = sessionState.value.copy(
                isDetectingStore = true,
                statusMessage = if (hadStores && !forceRefresh) null else "Locating nearby stores..."
            )

            val location = LocationHelper.getCurrentLocation(app, forceFresh = forceRefresh)
            if (location == null) {
                sessionState.value = sessionState.value.copy(
                    isDetectingStore = false,
                    statusMessage = if (hadStores) null else "Couldn't get location fix. Pick a category or type a store name."
                )
                return@launch
            }

            val savedPlaces = repository.savedPlaces.value
            val instantSavedMatches = storeDetector.matchSavedPlaces(
                latitude = location.latitude,
                longitude = location.longitude,
                savedPlaces = savedPlaces
            )

            // 1. Instant spatial cache hit (0ms network wait when pre-fetched or still at the same store!)
            val cachedSnapshot = repository.cachedStores.value
            if (
                !forceRefresh &&
                LocationHelper.isCacheValid(
                    snapshot = cachedSnapshot,
                    currentLat = location.latitude,
                    currentLon = location.longitude,
                    maxDistanceMeters = 100.0,
                    maxAgeMs = 20 * 60_000L
                ) &&
                cachedSnapshot != null
            ) {
                val cachedResorted = LocationHelper.recomputeDistancesAndSort(
                    cachedSnapshot.stores.filterNot { it.source == StoreSource.SAVED_PLACE },
                    location.latitude,
                    location.longitude
                )
                val merged = (instantSavedMatches + cachedResorted)
                    .distinctBy { it.name.lowercase().replace(Regex("[^a-z0-9]"), "") }
                    .take(15)
                val best = merged.firstOrNull() ?: sessionState.value.selectedStore
                sessionState.value = sessionState.value.copy(
                    selectedStore = best,
                    nearbyStores = merged,
                    isDetectingStore = false,
                    statusMessage = null,
                    lastLatitude = location.latitude,
                    lastLongitude = location.longitude
                )
                persistTileSummaryFor(best)
                return@launch
            }

            // 2. If user is at a Saved Place, show it IMMEDIATELY while remote POIs load in background
            if (instantSavedMatches.isNotEmpty()) {
                val bestSaved = instantSavedMatches.first()
                sessionState.value = sessionState.value.copy(
                    selectedStore = bestSaved,
                    nearbyStores = instantSavedMatches,
                    statusMessage = null,
                    lastLatitude = location.latitude,
                    lastLongitude = location.longitude
                )
                persistTileSummaryFor(bestSaved)
            }

            // 3. Fast parallel network lookup + persist into spatial cache for instant future opens
            val currentSettings = repository.settings.value
            val result = storeDetector.detectNearbyStores(
                latitude = location.latitude,
                longitude = location.longitude,
                savedPlaces = savedPlaces,
                googlePlacesApiKey = currentSettings.googlePlacesApiKey,
                radiusMeters = currentSettings.searchRadiusMeters
            )

            if (result.stores.isNotEmpty()) {
                repository.saveStoreCache(
                    CachedStoreSnapshot(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestampMs = System.currentTimeMillis(),
                        stores = result.stores
                    )
                )
            }

            val bestStore = result.stores.firstOrNull() ?: sessionState.value.selectedStore
            sessionState.value = sessionState.value.copy(
                selectedStore = bestStore,
                nearbyStores = if (result.stores.isNotEmpty()) result.stores else sessionState.value.nearbyStores,
                isDetectingStore = false,
                statusMessage = result.statusMessage,
                lastLatitude = location.latitude,
                lastLongitude = location.longitude
            )

            persistTileSummaryFor(bestStore)
        }
    }

    fun selectStore(store: DetectedStore) {
        sessionState.value = sessionState.value.copy(
            selectedStore = store,
            statusMessage = null
        )
        persistTileSummaryFor(store)
    }

    fun selectCategory(category: SpendCategory) {
        val store = DetectedStore(
            name = category.displayName,
            category = category,
            source = StoreSource.MANUAL
        )
        selectStore(store)
    }

    fun applyManualStoreQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        // Check saved places first by name match
        val matchedSaved = repository.savedPlaces.value.firstOrNull {
            it.name.equals(trimmed, ignoreCase = true) ||
                it.name.contains(trimmed, ignoreCase = true)
        }

        val store = if (matchedSaved != null) {
            DetectedStore(
                name = matchedSaved.name,
                category = matchedSaved.category,
                source = StoreSource.SAVED_PLACE,
                address = matchedSaved.address,
                latitude = matchedSaved.latitude,
                longitude = matchedSaved.longitude,
                acceptedNetworks = matchedSaved.acceptedNetworks,
                savedPlaceId = matchedSaved.id,
                preferredCardId = matchedSaved.preferredCardId
            )
        } else {
            val classified = MerchantCategoryClassifier.classify(trimmed)
            DetectedStore(
                name = trimmed,
                category = classified.category,
                source = StoreSource.MANUAL,
                acceptedNetworks = classified.acceptedNetworks
            )
        }

        selectStore(store)
    }

    fun toggleMobileWallet() {
        sessionState.value = sessionState.value.copy(
            isMobileWallet = !sessionState.value.isMobileWallet
        )
    }

    fun toggleForeignTransaction() {
        sessionState.value = sessionState.value.copy(
            isForeignTransaction = !sessionState.value.isForeignTransaction
        )
    }

    fun toggleAllowAmex() {
        sessionState.value = sessionState.value.copy(
            allowAmex = !sessionState.value.allowAmex
        )
    }

    fun toggleVisaOnly() {
        sessionState.value = sessionState.value.copy(
            visaOnlyOverride = !sessionState.value.visaOnlyOverride
        )
    }

    fun launchGoogleWallet(recommendation: CardRecommendation? = uiState.value.recommendations.firstOrNull()) {
        val currentState = uiState.value
        val outcome = walletLauncher.launchGoogleWallet(
            recommendation = recommendation,
            store = currentState.session.selectedStore,
            showReminderNotification = currentState.settings.showCheckoutNotification
        )
        sessionState.value = sessionState.value.copy(transientBanner = outcome.message)
    }

    fun clearBanner() {
        sessionState.value = sessionState.value.copy(transientBanner = null)
    }

    // --- Card CRUD ---
    fun toggleCardOwned(cardId: String, owned: Boolean) {
        viewModelScope.launch { repository.toggleCardOwned(cardId, owned) }
    }

    fun toggleCardActive(cardId: String, active: Boolean) {
        viewModelScope.launch { repository.toggleCardActive(cardId, active) }
    }

    fun saveCard(card: CreditCard) {
        viewModelScope.launch { repository.upsertCard(card) }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch { repository.deleteCard(cardId) }
    }

    // --- Saved Places CRUD ---
    fun savePlace(
        id: String? = null,
        name: String,
        category: SpendCategory,
        latitude: Double? = sessionState.value.lastLatitude,
        longitude: Double? = sessionState.value.lastLongitude,
        radiusMeters: Double = 150.0,
        acceptedNetworks: Set<CardNetwork> = CardNetwork.entries.toSet(),
        preferredCardId: String? = null,
        address: String = ""
    ) {
        viewModelScope.launch {
            val place = SavedPlace(
                id = id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                category = category,
                latitude = latitude,
                longitude = longitude,
                radiusMeters = radiusMeters,
                acceptedNetworks = acceptedNetworks,
                preferredCardId = preferredCardId,
                address = address.trim()
            )
            repository.upsertSavedPlace(place)
            sessionState.value = sessionState.value.copy(
                transientBanner = "Saved \"${place.name}\" for automatic recognition"
            )
        }
    }

    fun deletePlace(placeId: String) {
        viewModelScope.launch { repository.deleteSavedPlace(placeId) }
    }

    // --- Settings ---
    fun updatePointValuation(currency: RewardCurrency, cents: Double) {
        viewModelScope.launch { repository.updatePointValuation(currency, cents) }
    }

    fun updateGooglePlacesApiKey(apiKey: String) {
        viewModelScope.launch { repository.updateGooglePlacesApiKey(apiKey) }
    }

    fun updateSearchRadius(radiusMeters: Int) {
        viewModelScope.launch { repository.updateSearchRadius(radiusMeters) }
    }

    fun updateShowCheckoutNotification(enabled: Boolean) {
        viewModelScope.launch { repository.updateShowCheckoutNotification(enabled) }
    }

    fun updateDefaultUseMobileWallet(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDefaultUseMobileWallet(enabled)
            sessionState.value = sessionState.value.copy(isMobileWallet = enabled)
        }
    }

    fun setBackgroundPrefetchEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                if (LocationHelper.hasBackgroundLocationPermission(app)) {
                    val started = BackgroundLocationPrefetchManager.start(app)
                    repository.updateBackgroundPrefetchEnabled(started)
                    sessionState.value = sessionState.value.copy(
                        transientBanner = if (started) {
                            "Background store pre-fetch active — stores will load before you open the app"
                        } else {
                            "Could not start background location updates"
                        }
                    )
                } else {
                    repository.updateBackgroundPrefetchEnabled(false)
                    sessionState.value = sessionState.value.copy(
                        transientBanner = "Select \"Allow all the time\" in Location permissions to enable background pre-fetch"
                    )
                }
            } else {
                BackgroundLocationPrefetchManager.stop(app)
                repository.updateBackgroundPrefetchEnabled(false)
                sessionState.value = sessionState.value.copy(
                    transientBanner = "Background store pre-fetch paused"
                )
            }
        }
    }

    // --- In-app GitHub Releases Updater ---
    fun updateAutoCheckUpdates(enabled: Boolean) {
        viewModelScope.launch { repository.updateAutoCheckUpdates(enabled) }
    }

    fun updateUpdateRepoSlug(slug: String) {
        viewModelScope.launch {
            repository.updateUpdateRepoSlug(slug)
            sessionState.value = sessionState.value.copy(transientBanner = "Saved update repo")
        }
    }

    fun checkForUpdates(manual: Boolean = false) {
        viewModelScope.launch {
            repository.awaitLoaded()
            val settings = repository.settings.value
            val repo = settings.updateRepoSlug.trim()
            if (repo.isBlank()) {
                if (manual) _updateStatus.value = UpdateStatus.Error("Set a GitHub repo (owner/repo) first")
                return@launch
            }

            val now = System.currentTimeMillis()
            if (!manual && (now - lastAutoUpdateCheckMs) < 2 * 60_000L) {
                return@launch
            }
            lastAutoUpdateCheckMs = now

            if (manual) {
                _updateStatus.value = UpdateStatus.Checking
            }

            val result = appUpdater.checkForUpdate(repo, BuildConfig.VERSION_NAME)
            result.onSuccess { info ->
                if (info != null) {
                    _updateStatus.value = UpdateStatus.Available(info)
                } else if (manual) {
                    _updateStatus.value = UpdateStatus.UpToDate
                    sessionState.value = sessionState.value.copy(
                        transientBanner = "Points Maximizer v${BuildConfig.VERSION_NAME} is up to date"
                    )
                }
            }.onFailure { err ->
                if (manual) {
                    _updateStatus.value = UpdateStatus.Error(err.message ?: "Could not check GitHub Releases")
                }
            }
        }
    }

    fun downloadAndInstallUpdate(info: ReleaseUpdateInfo? = null) {
        val targetInfo = info ?: when (val s = _updateStatus.value) {
            is UpdateStatus.Available -> s.info
            else -> return
        }

        // If we already downloaded this APK and just returned from enabling install permission:
        val existingApk = pendingDownloadedApk
        if (existingApk != null && existingApk.exists()) {
            launchDownloadedApk(existingApk, targetInfo)
            return
        }

        viewModelScope.launch {
            _updateStatus.value = UpdateStatus.Downloading(targetInfo, 0)
            val result = appUpdater.downloadApk(app, targetInfo) { pct ->
                _updateStatus.value = UpdateStatus.Downloading(targetInfo, pct)
            }
            result.onSuccess { apkFile ->
                pendingDownloadedApk = apkFile
                launchDownloadedApk(apkFile, targetInfo)
            }.onFailure { err ->
                _updateStatus.value = UpdateStatus.Error(err.message ?: "Download failed")
                sessionState.value = sessionState.value.copy(
                    transientBanner = "Update download failed: ${err.message}"
                )
            }
        }
    }

    fun resumePendingInstallIfPermitted() {
        if (!awaitingInstallPermission) return
        val apk = pendingDownloadedApk ?: return
        val status = _updateStatus.value
        val info = (status as? UpdateStatus.Available)?.info ?: return
        if (apk.exists() && app.packageManager.canRequestPackageInstalls()) {
            awaitingInstallPermission = false
            launchDownloadedApk(apk, info)
        }
    }

    private fun launchDownloadedApk(apkFile: File, info: ReleaseUpdateInfo) {
        when (appUpdater.launchInstaller(app, apkFile)) {
            InstallLaunchResult.LAUNCHED_INSTALLER -> {
                awaitingInstallPermission = false
                _updateStatus.value = UpdateStatus.Available(info)
            }
            InstallLaunchResult.NEEDS_INSTALL_PERMISSION -> {
                awaitingInstallPermission = true
                _updateStatus.value = UpdateStatus.Available(info)
                sessionState.value = sessionState.value.copy(
                    transientBanner = "Enable \"Allow from this source\" for Points Maximizer, then return to install"
                )
            }
            InstallLaunchResult.FAILED -> {
                awaitingInstallPermission = false
                _updateStatus.value = UpdateStatus.Error("Could not launch Android installer")
            }
        }
    }

    private fun persistTileSummaryFor(store: DetectedStore) {
        viewModelScope.launch {
            val currentCards = repository.cards.value
            val currentSettings = repository.settings.value
            val s = sessionState.value
            val recs = CardRecommendationEngine.recommend(
                cards = currentCards,
                context = CheckoutContext(
                    store = store,
                    isMobileWallet = s.isMobileWallet,
                    isForeignTransaction = s.isForeignTransaction,
                    allowAmex = s.allowAmex,
                    visaOnlyOverride = s.visaOnlyOverride
                ),
                pointValuations = currentSettings.pointValuations
            )
            val top = recs.firstOrNull()
            if (top != null) {
                val pct = String.format(Locale.US, "%.1f%%", top.effectiveReturnPercent)
                repository.updateLastRecommendationSummary(
                    storeName = store.name,
                    cardSummary = "${top.card.walletNickname.ifBlank { top.card.name }} ($pct)"
                )
                walletLauncher.requestTileRefresh()
            }
        }
    }
}
