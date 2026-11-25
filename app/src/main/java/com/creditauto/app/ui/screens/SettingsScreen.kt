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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- CHECKOUT SECTION ---
        item(key = "section_checkout") {
            SettingsSection(title = "Checkout") {
                SettingsSwitchRow(
                    title = "Card reminder notification",
                    subtitle = "Shows the winning card & last 4 digits at the top while you switch to Google Wallet.",
                    checked = settings.showCheckoutNotification,
                    onCheckedChange = onUpdateShowNotification
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "Google Pay tap by default",
                    subtitle = "Applies mobile-wallet bonus multipliers (e.g. 3x on US Bank Altitude Reserve).",
                    checked = settings.defaultUseMobileWallet,
                    onCheckedChange = onUpdateDefaultMobileWallet
                )
            }
        }

        // --- STORE DETECTION SECTION ---
        item(key = "section_store_detection") {
            SettingsSection(title = "Store detection") {
                SettingsSwitchRow(
                    title = "Background store pre-fetch",
                    subtitle = if (settings.backgroundPrefetchEnabled && hasBackgroundLocationPermission) {
                        "Active — preloads nearby stores when you move ≥100m so checkout opens instantly."
                    } else {
                        "Preloads nearby stores as you arrive. Requires \"Allow all the time\" location."
                    },
                    checked = settings.backgroundPrefetchEnabled && hasBackgroundLocationPermission,
                    onCheckedChange = onToggleBackgroundPrefetch
                )

                SettingsDivider()

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeProvider = if (settings.googlePlacesApiKey.isNotBlank()) {
                        "Google Places API (with OpenStreetMap fallback)"
                    } else {
                        "OpenStreetMap Overpass API (Free)"
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Provider: $activeProvider",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    var radiusDraft by remember(settings.searchRadiusMeters) {
                        mutableStateOf(settings.searchRadiusMeters.toFloat())
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nearby search radius", style = MaterialTheme.typography.bodyMedium)
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "${radiusDraft.roundToInt()}m",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
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
                        label = { Text("Optional Google Places API key") },
                        placeholder = { Text("Leave blank for free OpenStreetMap") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = { onUpdateGooglePlacesKey(apiKeyDraft) },
                            shape = CircleShape
                        ) {
                            Text("Save key")
                        }
                        if (settings.googlePlacesApiKey.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    apiKeyDraft = ""
                                    onUpdateGooglePlacesKey("")
                                },
                                shape = CircleShape
                            ) {
                                Text("Clear")
                            }
                        }
                    }
                }
            }
        }

        // --- POINT VALUATIONS SECTION ---
        item(key = "section_point_valuations") {
            val pointCurrencies = remember {
                RewardCurrency.entries.filter { it != RewardCurrency.CASH_BACK }
            }
            SettingsSection(title = "Point valuations (¢ per point)") {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Customize what each points currency is worth to you so points and cash back compare accurately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    pointCurrencies.forEach { currency ->
                        val persistedVal = settings.pointValuations[currency]
                            ?: currency.defaultCentsPerPoint
                        var sliderVal by remember(currency, persistedVal) {
                            mutableStateOf(persistedVal.toFloat())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currency.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.1f¢", (sliderVal * 10f).roundToInt() / 10.0),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
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

        // --- APP UPDATES SECTION ---
        item(key = "section_app_updates") {
            var repoDraft by remember(settings.updateRepoSlug) {
                mutableStateOf(settings.updateRepoSlug)
            }
            SettingsSection(title = "App updates • v${BuildConfig.VERSION_NAME}") {
                SettingsSwitchRow(
                    title = "Auto-check GitHub Releases",
                    subtitle = "Shows a 1-tap Update banner whenever a newer APK is published.",
                    checked = settings.autoCheckUpdates,
                    onCheckedChange = onToggleAutoCheckUpdates
                )
                SettingsDivider()
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = repoDraft,
                        onValueChange = { repoDraft = it },
                        label = { Text("GitHub repository (owner/repo)") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onUpdateRepoSlug(repoDraft) },
                            shape = CircleShape
                        ) {
                            Text("Save repo")
                        }
                        Button(
                            onClick = onCheckForUpdates,
                            shape = CircleShape,
                            enabled = updateStatus !is UpdateStatus.Checking && updateStatus !is UpdateStatus.Downloading
                        ) {
                            Icon(
                                Icons.Rounded.SystemUpdate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (updateStatus is UpdateStatus.Checking) "Checking…"
                                else "Check for updates"
                            )
                        }
                    }

                    when (updateStatus) {
                        is UpdateStatus.Available -> {
                            Button(
                                onClick = onInstallUpdate,
                                shape = CircleShape,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Download & install ${updateStatus.info.tagName}")
                            }
                        }
                        is UpdateStatus.Downloading -> {
                            Text(
                                text = "Downloading ${updateStatus.info.tagName}… ${updateStatus.progressPercent}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LinearProgressIndicator(
                                progress = { updateStatus.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape)
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

        item(key = "settings_bottom_spacer") { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent = if (checked) {
                {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                }
            } else {
                null
            }
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    )
}
