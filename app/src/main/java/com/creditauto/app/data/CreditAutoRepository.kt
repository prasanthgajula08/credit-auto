package com.creditauto.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.creditauto.app.domain.AppSettings
import com.creditauto.app.domain.CachedStoreSnapshot
import com.creditauto.app.domain.CardCatalogPresets
import com.creditauto.app.domain.CreditCard
import com.creditauto.app.domain.RewardCurrency
import com.creditauto.app.domain.SavedPlace
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "credit_auto_settings")

class CreditAutoRepository(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val cardsFile: File get() = File(context.filesDir, "cards_v1.json")
    private val placesFile: File get() = File(context.filesDir, "saved_places_v1.json")
    private val storeCacheFile: File get() = File(context.filesDir, "store_cache_v1.json")

    private val loadedSignal = CompletableDeferred<Unit>()

    private val _cards = MutableStateFlow<List<CreditCard>>(CardCatalogPresets.allPresets())
    val cards: StateFlow<List<CreditCard>> = _cards.asStateFlow()

    private val _savedPlaces = MutableStateFlow<List<SavedPlace>>(emptyList())
    val savedPlaces: StateFlow<List<SavedPlace>> = _savedPlaces.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _cachedStores = MutableStateFlow<CachedStoreSnapshot?>(null)
    val cachedStores: StateFlow<CachedStoreSnapshot?> = _cachedStores.asStateFlow()

    init {
        scope.launch {
            try {
                loadInitialData()
            } finally {
                loadedSignal.complete(Unit)
            }
        }
    }

    suspend fun awaitLoaded() {
        loadedSignal.await()
    }

    private suspend fun loadInitialData() {
        val loadedCards = readJsonOrNull<List<CreditCard>>(cardsFile)
        if (loadedCards != null && loadedCards.isNotEmpty()) {
            // Merge any newly added preset cards that aren't in saved list yet
            val existingIds = loadedCards.map { it.id }.toSet()
            val missingPresets = CardCatalogPresets.allPresets()
                .filter { it.id !in existingIds }
                .map { it.copy(isOwned = false) }
            _cards.value = loadedCards + missingPresets
        } else {
            val presets = CardCatalogPresets.allPresets()
            _cards.value = presets
            writeJsonAtomically(cardsFile, presets)
        }

        val loadedPlaces = readJsonOrNull<List<SavedPlace>>(placesFile)
        if (loadedPlaces != null) {
            _savedPlaces.value = loadedPlaces
        }

        val loadedCache = readJsonOrNull<CachedStoreSnapshot>(storeCacheFile)
        if (loadedCache != null) {
            _cachedStores.value = loadedCache
        }

        val prefs = context.settingsDataStore.data.first()
        _settings.value = prefsToSettings(prefs)
    }

    suspend fun saveStoreCache(snapshot: CachedStoreSnapshot) {
        _cachedStores.value = snapshot
        writeJsonAtomically(storeCacheFile, snapshot)
    }

    suspend fun toggleCardOwned(cardId: String, owned: Boolean) {
        val updated = _cards.value.map { card ->
            if (card.id == cardId) card.copy(isOwned = owned, isActive = true) else card
        }
        _cards.value = updated
        writeJsonAtomically(cardsFile, updated)
        markWalletCustomized()
    }

    suspend fun toggleCardActive(cardId: String, active: Boolean) {
        val updated = _cards.value.map { card ->
            if (card.id == cardId) card.copy(isActive = active) else card
        }
        _cards.value = updated
        writeJsonAtomically(cardsFile, updated)
    }

    suspend fun upsertCard(card: CreditCard) {
        val current = _cards.value.toMutableList()
        val idx = current.indexOfFirst { it.id == card.id }
        if (idx >= 0) {
            current[idx] = card
        } else {
            current.add(0, card)
        }
        _cards.value = current
        writeJsonAtomically(cardsFile, current)
        markWalletCustomized()
    }

    suspend fun deleteCard(cardId: String) {
        val updated = _cards.value.filterNot { it.id == cardId }
        _cards.value = updated
        writeJsonAtomically(cardsFile, updated)
        markWalletCustomized()
    }

    suspend fun upsertSavedPlace(place: SavedPlace) {
        val current = _savedPlaces.value.toMutableList()
        val idx = current.indexOfFirst { it.id == place.id }
        if (idx >= 0) {
            current[idx] = place
        } else {
            current.add(0, place)
        }
        _savedPlaces.value = current
        writeJsonAtomically(placesFile, current)
    }

    suspend fun deleteSavedPlace(placeId: String) {
        val updated = _savedPlaces.value.filterNot { it.id == placeId }
        _savedPlaces.value = updated
        writeJsonAtomically(placesFile, updated)
    }

    suspend fun updatePointValuation(currency: RewardCurrency, centsPerPoint: Double) {
        val currentMap = _settings.value.pointValuations.toMutableMap()
        currentMap[currency] = centsPerPoint.coerceIn(0.1, 10.0)
        val newSettings = _settings.value.copy(pointValuations = currentMap)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateGooglePlacesApiKey(apiKey: String) {
        val newSettings = _settings.value.copy(googlePlacesApiKey = apiKey.trim())
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateSearchRadius(radiusMeters: Int) {
        val newSettings = _settings.value.copy(searchRadiusMeters = radiusMeters.coerceIn(50, 500))
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateShowCheckoutNotification(enabled: Boolean) {
        val newSettings = _settings.value.copy(showCheckoutNotification = enabled)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateDefaultUseMobileWallet(enabled: Boolean) {
        val newSettings = _settings.value.copy(defaultUseMobileWallet = enabled)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateBackgroundPrefetchEnabled(enabled: Boolean) {
        val newSettings = _settings.value.copy(backgroundPrefetchEnabled = enabled)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateAutoCheckUpdates(enabled: Boolean) {
        val newSettings = _settings.value.copy(autoCheckUpdates = enabled)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateUpdateRepoSlug(repoSlug: String) {
        val cleaned = repoSlug.trim().removePrefix("https://github.com/").trim('/')
        val newSettings = _settings.value.copy(updateRepoSlug = cleaned)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateLastRecommendationSummary(storeName: String, cardSummary: String) {
        val newSettings = _settings.value.copy(
            lastDetectedStoreName = storeName,
            lastTopCardSummary = cardSummary
        )
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    private suspend fun markWalletCustomized() {
        if (!_settings.value.hasCustomizedWallet) {
            val newSettings = _settings.value.copy(hasCustomizedWallet = true)
            _settings.value = newSettings
            saveSettings(newSettings)
        }
    }

    private suspend fun saveSettings(settings: AppSettings) {
        val valuationsJson = json.encodeToString(settings.pointValuations)
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_POINT_VALUATIONS] = valuationsJson
            prefs[KEY_GOOGLE_PLACES_KEY] = settings.googlePlacesApiKey
            prefs[KEY_SEARCH_RADIUS] = settings.searchRadiusMeters
            prefs[KEY_SHOW_NOTIFICATION] = settings.showCheckoutNotification
            prefs[KEY_DEFAULT_MOBILE_WALLET] = settings.defaultUseMobileWallet
            prefs[KEY_BACKGROUND_PREFETCH] = settings.backgroundPrefetchEnabled
            prefs[KEY_AUTO_CHECK_UPDATES] = settings.autoCheckUpdates
            prefs[KEY_UPDATE_REPO_SLUG] = settings.updateRepoSlug
            prefs[KEY_HAS_CUSTOMIZED_WALLET] = settings.hasCustomizedWallet
            prefs[KEY_LAST_TOP_CARD] = settings.lastTopCardSummary
            prefs[KEY_LAST_STORE_NAME] = settings.lastDetectedStoreName
        }
    }

    private fun prefsToSettings(prefs: Preferences): AppSettings {
        val defaultValuations = RewardCurrency.entries.associateWith { it.defaultCentsPerPoint }
        val parsedValuations = prefs[KEY_POINT_VALUATIONS]?.let { raw ->
            runCatching { json.decodeFromString<Map<RewardCurrency, Double>>(raw) }.getOrNull()
        }
        return AppSettings(
            pointValuations = defaultValuations + (parsedValuations ?: emptyMap()),
            googlePlacesApiKey = prefs[KEY_GOOGLE_PLACES_KEY] ?: "",
            searchRadiusMeters = prefs[KEY_SEARCH_RADIUS] ?: 150,
            showCheckoutNotification = prefs[KEY_SHOW_NOTIFICATION] ?: true,
            defaultUseMobileWallet = prefs[KEY_DEFAULT_MOBILE_WALLET] ?: true,
            backgroundPrefetchEnabled = prefs[KEY_BACKGROUND_PREFETCH] ?: false,
            autoCheckUpdates = prefs[KEY_AUTO_CHECK_UPDATES] ?: true,
            updateRepoSlug = prefs[KEY_UPDATE_REPO_SLUG] ?: "prasanthgajula08/credit-auto",
            hasCustomizedWallet = prefs[KEY_HAS_CUSTOMIZED_WALLET] ?: false,
            lastTopCardSummary = prefs[KEY_LAST_TOP_CARD] ?: "",
            lastDetectedStoreName = prefs[KEY_LAST_STORE_NAME] ?: ""
        )
    }

    private suspend inline fun <reified T> readJsonOrNull(file: File): T? =
        withContext(Dispatchers.IO) {
            if (!file.exists()) return@withContext null
            runCatching { json.decodeFromString<T>(file.readText()) }.getOrNull()
        }

    private suspend inline fun <reified T> writeJsonAtomically(file: File, value: T) =
        withContext(Dispatchers.IO) {
            val tmp = File(file.parentFile, "${file.name}.tmp")
            tmp.writeText(json.encodeToString(value))
            if (!tmp.renameTo(file)) {
                file.writeText(tmp.readText())
                tmp.delete()
            }
        }

    companion object {
        private val KEY_POINT_VALUATIONS = stringPreferencesKey("point_valuations")
        private val KEY_GOOGLE_PLACES_KEY = stringPreferencesKey("google_places_api_key")
        private val KEY_SEARCH_RADIUS = intPreferencesKey("search_radius_meters")
        private val KEY_SHOW_NOTIFICATION = booleanPreferencesKey("show_checkout_notification")
        private val KEY_DEFAULT_MOBILE_WALLET = booleanPreferencesKey("default_mobile_wallet")
        private val KEY_BACKGROUND_PREFETCH = booleanPreferencesKey("background_prefetch_enabled")
        private val KEY_AUTO_CHECK_UPDATES = booleanPreferencesKey("auto_check_updates")
        private val KEY_UPDATE_REPO_SLUG = stringPreferencesKey("update_repo_slug")
        private val KEY_HAS_CUSTOMIZED_WALLET = booleanPreferencesKey("has_customized_wallet")
        private val KEY_LAST_TOP_CARD = stringPreferencesKey("last_top_card_summary")
        private val KEY_LAST_STORE_NAME = stringPreferencesKey("last_store_name")
    }
}
