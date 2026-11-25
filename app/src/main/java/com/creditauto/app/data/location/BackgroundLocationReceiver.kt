package com.creditauto.app.data.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.creditauto.app.CreditAutoApp
import com.creditauto.app.domain.CachedStoreSnapshot
import com.creditauto.app.domain.CardRecommendationEngine
import com.creditauto.app.domain.CheckoutContext
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

class BackgroundLocationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BackgroundLocationPrefetchManager.ACTION_PREFETCH_LOCATION) return
        val location = LocationResult.extractResult(intent)?.lastLocation ?: return
        val app = context.applicationContext as? CreditAutoApp ?: return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(8_500L) {
                    app.repository.awaitLoaded()
                    val settings = app.repository.settings.value
                    if (!settings.backgroundPrefetchEnabled) return@withTimeoutOrNull

                    val existingCache = app.repository.cachedStores.value
                    // Skip network fetch if we already have fresh stores near this exact spot
                    if (
                        LocationHelper.isCacheValid(
                            snapshot = existingCache,
                            currentLat = location.latitude,
                            currentLon = location.longitude,
                            maxDistanceMeters = 80.0,
                            maxAgeMs = 10 * 60_000L
                        )
                    ) {
                        return@withTimeoutOrNull
                    }

                    val result = app.storeDetector.detectNearbyStores(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        savedPlaces = app.repository.savedPlaces.value,
                        googlePlacesApiKey = settings.googlePlacesApiKey,
                        radiusMeters = settings.searchRadiusMeters
                    )

                    if (result.stores.isNotEmpty()) {
                        val snapshot = CachedStoreSnapshot(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestampMs = System.currentTimeMillis(),
                            stores = result.stores
                        )
                        app.repository.saveStoreCache(snapshot)

                        // Keep Quick Settings Tile subtitle pre-warmed with top card!
                        val closest = result.stores.first()
                        val recs = CardRecommendationEngine.recommend(
                            cards = app.repository.cards.value,
                            context = CheckoutContext(
                                store = closest,
                                isMobileWallet = settings.defaultUseMobileWallet
                            ),
                            pointValuations = settings.pointValuations
                        )
                        val top = recs.firstOrNull()
                        if (top != null) {
                            val pct = String.format(Locale.US, "%.1f%%", top.effectiveReturnPercent)
                            val label = top.card.walletNickname.ifBlank { top.card.name }
                            app.repository.updateLastRecommendationSummary(
                                storeName = closest.name,
                                cardSummary = "$label ($pct)"
                            )
                            app.walletLauncher.requestTileRefresh()
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class BootPrefetchReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as? CreditAutoApp ?: return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                app.repository.awaitLoaded()
                if (app.repository.settings.value.backgroundPrefetchEnabled) {
                    BackgroundLocationPrefetchManager.start(context.applicationContext)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
