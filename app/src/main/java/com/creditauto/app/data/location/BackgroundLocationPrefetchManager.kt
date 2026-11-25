package com.creditauto.app.data.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

object BackgroundLocationPrefetchManager {

    const val ACTION_PREFETCH_LOCATION = "com.creditauto.app.action.PREFETCH_LOCATION"
    private const val REQUEST_CODE = 7701

    @SuppressLint("MissingPermission")
    fun start(context: Context): Boolean {
        if (!LocationHelper.hasBackgroundLocationPermission(context)) return false
        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            5 * 60_000L // check roughly every 5 min when moving
        )
            .setMinUpdateIntervalMillis(2 * 60_000L)
            .setMinUpdateDistanceMeters(100f) // only wake when moved >= 100m
            .build()

        return runCatching {
            client.requestLocationUpdates(request, buildPendingIntent(context))
            true
        }.getOrDefault(false)
    }

    fun stop(context: Context) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        runCatching {
            client.removeLocationUpdates(buildPendingIntent(context))
        }
    }

    private fun buildPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, BackgroundLocationReceiver::class.java).apply {
            action = ACTION_PREFETCH_LOCATION
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
    }
}
