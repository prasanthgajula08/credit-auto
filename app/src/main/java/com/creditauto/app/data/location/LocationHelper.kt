package com.creditauto.app.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.creditauto.app.domain.CachedStoreSnapshot
import com.creditauto.app.domain.DetectedStore
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object LocationHelper {

    private const val FAST_LOCATION_MAX_AGE_MS = 3 * 60_000L
    private const val FAST_LOCATION_MAX_ACCURACY_M = 120f

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun hasBackgroundLocationPermission(context: Context): Boolean {
        if (!hasLocationPermission(context)) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns immediately (~10ms) if Android's Fused Location Provider already has a recent
     * fix (< 3 min old, <= 120m accuracy). Otherwise performs a fast Wi-Fi/cell balanced fix
     * (~250ms indoors) instead of blocking 6s waiting for GPS satellites inside a store.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(
        context: Context,
        forceFresh: Boolean = false
    ): Location? {
        if (!hasLocationPermission(context)) return null
        val client = LocationServices.getFusedLocationProviderClient(context)

        val cached = try {
            withTimeoutOrNull(800L) { client.lastLocation.await() }
        } catch (_: Exception) {
            null
        }

        if (!forceFresh && cached != null && isLocationFresh(cached)) {
            return cached
        }

        // Balanced accuracy uses Wi-Fi + cell triangulation which locks rapidly indoors
        val balanced = withTimeoutOrNull(2_500L) {
            val tokenSource = CancellationTokenSource()
            try {
                client.getCurrentLocation(
                    if (forceFresh) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    tokenSource.token
                ).await()
            } catch (_: Exception) {
                null
            } finally {
                tokenSource.cancel()
            }
        }
        if (balanced != null) return balanced

        // If high-accuracy timed out indoors during forceFresh, try balanced quickly
        if (forceFresh) {
            val fallbackBalanced = withTimeoutOrNull(1_800L) {
                val tokenSource = CancellationTokenSource()
                try {
                    client.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        tokenSource.token
                    ).await()
                } catch (_: Exception) {
                    null
                } finally {
                    tokenSource.cancel()
                }
            }
            if (fallbackBalanced != null) return fallbackBalanced
        }

        return cached
    }

    private fun isLocationFresh(location: Location): Boolean {
        val ageMs = (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000L
        return ageMs in 0..FAST_LOCATION_MAX_AGE_MS &&
            (!location.hasAccuracy() || location.accuracy <= FAST_LOCATION_MAX_ACCURACY_M)
    }

    fun isCacheValid(
        snapshot: CachedStoreSnapshot?,
        currentLat: Double,
        currentLon: Double,
        nowMs: Long = System.currentTimeMillis(),
        maxDistanceMeters: Double = 100.0,
        maxAgeMs: Long = 20 * 60_000L
    ): Boolean {
        if (snapshot == null || snapshot.stores.isEmpty()) return false
        val ageMs = nowMs - snapshot.timestampMs
        if (ageMs < 0 || ageMs > maxAgeMs) return false
        val movedMeters = haversineDistanceMeters(
            snapshot.latitude,
            snapshot.longitude,
            currentLat,
            currentLon
        )
        return movedMeters <= maxDistanceMeters
    }

    fun recomputeDistancesAndSort(
        stores: List<DetectedStore>,
        currentLat: Double,
        currentLon: Double
    ): List<DetectedStore> {
        return stores.map { store ->
            val sLat = store.latitude
            val sLon = store.longitude
            if (sLat != null && sLon != null) {
                store.copy(distanceMeters = haversineDistanceMeters(currentLat, currentLon, sLat, sLon))
            } else {
                store
            }
        }.sortedBy { it.distanceMeters ?: Double.MAX_VALUE }
    }

    fun haversineDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusMeters = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a.coerceIn(0.0, 1.0)))
        return earthRadiusMeters * c
    }
}
