package com.creditauto.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.creditauto.app.BuildConfig
import com.creditauto.app.domain.RewardCurrency
import com.creditauto.app.ui.CreditAutoUiState
import com.creditauto.app.update.UpdateStatus
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    uiState: CreditAutoUiState,
    updateStatus: UpdateStatus,
    hasBackgroundLocationPermission: Boolean,
    onToggleBackgroundPrefetch: (Boolean) -> Unit,
    onToggleAutoCheckUpdates: (Boolean) -> Unit,
    onUpdateRepoSlug: (String) -> Unit,
    onCheckForUpdates: () -> Unit,
    onInstallUpdate: () -> Unit,
    onUpdatePointValuation: (RewardCurrency, Double) -> Unit,
    onUpdateGooglePlacesKey: (String) -> Unit,
    onUpdateSearchRadius: (Int) -> Unit,
    onUpdateShowNotification: (Boolean) -> Unit,
    onUpdateDefaultMobileWallet: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings
    var apiKeyDraft by remember(settings.googlePlacesApiKey) {
        mutableStateOf(settings.googlePlacesApiKey)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Checkout Preferences
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Checkout Preferences",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Card reminder notification", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Shows the winning card & last 4 digits at the top of your screen while you swipe in Google Wallet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = settings.showCheckoutNotification,
                            onCheckedChange = onUpdateShowNotification
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Google Pay tap by default", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Enables mobile-wallet bonus multipliers (e.g. 3x on US Bank Altitude Reserve).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = settings.defaultUseMobileWallet,
                            onCheckedChange = onUpdateDefaultMobileWallet
                        )
                    }
                }
            }
        }

        // Store Detection Settings
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Store Detection",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Background store pre-fetch (Instant checkout)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (settings.backgroundPrefetchEnabled && hasBackgroundLocationPermission) {
                                    "Active — preloads nearby stores automatically when you move ≥100m (low battery) so stores appear instantly."
                                } else {
                                    "Preloads nearby stores as you arrive so the store & best card are already ready the instant you open the app. Requires \"Allow all the time\" location."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = settings.backgroundPrefetchEnabled && hasBackgroundLocationPermission,
                            onCheckedChange = onToggleBackgroundPrefetch
                        )
                    }

                    val activeProvider = if (settings.googlePlacesApiKey.isNotBlank()) {
                        "Google Places API (with OpenStreetMap fallback)"
                    } else {
                        "OpenStreetMap Overpass API (100% Free, no key required)"
                    }
                    Text(
                        text = "Active lookup: $activeProvider",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    var radiusDraft by remember(settings.searchRadiusMeters) {
                        mutableStateOf(settings.searchRadiusMeters.toFloat())
                    }
                    Text(
                        text = "Nearby search radius: ${radiusDraft.roundToInt()}m",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = radiusDraft,
                        onValueChange = { radiusDraft = it },
                        onValueChangeFinished = { onUpdateSearchRadius(radiusDraft.roundToInt()) },
                        valueRange = 50f..400f,
                        steps = 6
                    )

                    OutlinedTextField(
                        value = apiKeyDraft,
                        onValueChange = { apiKeyDraft = it },
                        label = { Text("Optional Google Places API Key") },
                        placeholder = { Text("Leave blank to use free OpenStreetMap") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onUpdateGooglePlacesKey(apiKeyDraft) }) {
                            Text("Save Key")
                        }
                        if (settings.googlePlacesApiKey.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    apiKeyDraft = ""
                                    onUpdateGooglePlacesKey("")
                                }
                            ) {
                                Text("Clear")
                            }
                        }
                    }
                }
            }
        }

        // Point Valuations
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Point Valuations (¢ per point)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Adjust how much each point currency is worth to you so Credit Auto compares points vs cash back accurately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    RewardCurrency.entries
                        .filter { it != RewardCurrency.CASH_BACK }
                        .forEach { currency ->
                            val persistedVal = settings.pointValuations[currency]
                                ?: currency.defaultCentsPerPoint
                            var sliderVal by remember(currency, persistedVal) {
                                mutableStateOf(persistedVal.toFloat())
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currency.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f¢", (sliderVal * 10f).roundToInt() / 10.0),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = sliderVal,
                                onValueChange = { sliderVal = it },
                                onValueChangeFinished = {
                                    val snapped = (sliderVal * 10f).roundToInt() / 10.0
                                    onUpdatePointValuation(currency, snapped)
                                },
                                valueRange = 0.5f..3.0f
                            )
                        }
                }
            }
        }

        // In-App GitHub Releases Auto-Updater
        item {
            var repoDraft by remember(settings.updateRepoSlug) {
                mutableStateOf(settings.updateRepoSlug)
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.SystemUpdate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "App Updates (v${BuildConfig.VERSION_NAME})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-check GitHub Releases", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Shows a 1-tap Update banner whenever a newer APK is published.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = settings.autoCheckUpdates,
                            onCheckedChange = onToggleAutoCheckUpdates
                        )
                    }

                    OutlinedTextField(
                        value = repoDraft,
                        onValueChange = { repoDraft = it },
                        label = { Text("GitHub Repo (owner/repo)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onUpdateRepoSlug(repoDraft) }) {
                            Text("Save Repo")
                        }
                        Button(
                            onClick = onCheckForUpdates,
                            enabled = updateStatus !is UpdateStatus.Checking && updateStatus !is UpdateStatus.Downloading
                        ) {
                            Text(
                                if (updateStatus is UpdateStatus.Checking) "Checking..."
                                else "Check for Updates"
                            )
                        }
                    }

                    when (updateStatus) {
                        is UpdateStatus.Available -> {
                            Button(
                                onClick = onInstallUpdate,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Download & Install ${updateStatus.info.tagName}")
                            }
                        }
                        is UpdateStatus.Downloading -> {
                            Text(
                                text = "Downloading ${updateStatus.info.tagName}... ${updateStatus.progressPercent}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LinearProgressIndicator(
                                progress = { updateStatus.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is UpdateStatus.UpToDate -> {
                            Text(
                                text = "You're on the latest version (v${BuildConfig.VERSION_NAME}).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        is UpdateStatus.Error -> {
                            Text(
                                text = updateStatus.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
