package com.creditauto.app.wallet

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.TileService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.creditauto.app.R
import com.creditauto.app.domain.CardRecommendation
import com.creditauto.app.domain.DetectedStore
import com.creditauto.app.tile.QuickPayTileService
import java.util.Locale

data class WalletLaunchOutcome(
    val launched: Boolean,
    val targetLabel: String,
    val message: String
)

class GoogleWalletLauncher(private val context: Context) {

    private val walletPackages = listOf(
        "com.google.android.apps.walletnfcrel" to "Google Wallet",
        "com.google.android.apps.gmoney" to "Google Pay",
        "com.google.android.apps.nbu.paisa.user" to "Google Pay"
    )

    fun isGoogleWalletInstalled(): Boolean {
        val pm = context.packageManager
        return walletPackages.any { (pkg, _) -> pm.getLaunchIntentForPackage(pkg) != null }
    }

    fun launchGoogleWallet(
        recommendation: CardRecommendation? = null,
        store: DetectedStore? = null,
        showReminderNotification: Boolean = true
    ): WalletLaunchOutcome {
        if (showReminderNotification && recommendation != null) {
            postCheckoutReminderNotification(recommendation, store)
        }

        val pm = context.packageManager
        for ((pkg, label) in walletPackages) {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                return runCatching {
                    context.startActivity(launchIntent)
                    WalletLaunchOutcome(
                        launched = true,
                        targetLabel = label,
                        message = if (recommendation != null) {
                            "Opened $label — select ${recommendation.card.displayLabel()}"
                        } else {
                            "Opened $label"
                        }
                    )
                }.getOrElse {
                    WalletLaunchOutcome(false, label, "Failed to open $label")
                }
            }
        }

        // Fallback: open Android's contactless NFC Tap & Pay settings
        val nfcSettingsIntent = Intent(Settings.ACTION_NFC_PAYMENT_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(nfcSettingsIntent)
            WalletLaunchOutcome(
                launched = true,
                targetLabel = "Tap & Pay Settings",
                message = "Google Wallet not found — opened Android Tap & Pay settings"
            )
        }.getOrElse {
            WalletLaunchOutcome(
                launched = false,
                targetLabel = "Google Wallet",
                message = "Install Google Wallet from Play Store to pay via NFC"
            )
        }
    }

    fun requestTileRefresh() {
        runCatching {
            TileService.requestListeningState(
                context,
                ComponentName(context, QuickPayTileService::class.java)
            )
        }
    }

    private fun postCheckoutReminderNotification(
        recommendation: CardRecommendation,
        store: DetectedStore?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        ensureNotificationChannel()

        val pct = String.format(Locale.US, "%.1f%%", recommendation.effectiveReturnPercent)
        val title = "Swipe to: ${recommendation.card.displayLabel()}"
        val body = buildString {
            append("$pct effective return (${recommendation.reason})")
            if (store != null) {
                append(" at ${store.name}")
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tile_card)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setTimeoutAfter(90_000L)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureNotificationChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_checkout),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_checkout_desc)
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "checkout_card_reminder"
        private const val NOTIFICATION_ID = 4201
    }
}
