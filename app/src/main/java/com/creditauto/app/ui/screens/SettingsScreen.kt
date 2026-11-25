package com.creditauto.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import com.creditauto.app.ui.components.AppIcons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

                    val radiusPresets = remember { listOf(100, 150, 250, 400) }
                    Text("Nearby search radius", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        radiusPresets.forEach { preset ->
                            val selected = settings.searchRadiusMeters == preset
                            FilterChip(
                                selected = selected,
                                onClick = { onUpdateSearchRadius(preset) },
                                shape = CircleShape,
                                label = { Text("${preset}m") }
                            )
                        }
                    }
                }

                SettingsDivider()

                var showApiKeyEditor by remember { mutableStateOf(false) }
                ExpandableSettingsRow(
                    title = "Google Places API key",
                    subtitle = if (settings.googlePlacesApiKey.isNotBlank()) {
                        "Configured • Using Google Places"
                    } else {
                        "Optional • Using free OpenStreetMap"
                    },
                    expanded = showApiKeyEditor,
                    onToggle = { showApiKeyEditor = !showApiKeyEditor }
                ) {
                    OutlinedTextField(
                        value = apiKeyDraft,
                        onValueChange = { apiKeyDraft = it },
                        label = { Text("Google Places API key") },
                        placeholder = { Text("Leave blank for free OpenStreetMap") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = {
                                onUpdateGooglePlacesKey(apiKeyDraft)
                                showApiKeyEditor = false
                            },
                            shape = CircleShape
                        ) {
                            Text("Save key")
                        }
                        if (settings.googlePlacesApiKey.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    apiKeyDraft = ""
                                    onUpdateGooglePlacesKey("")
                                    showApiKeyEditor = false
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
            var editingCurrency by remember { mutableStateOf<RewardCurrency?>(null) }

            SettingsSection(title = "Point valuations (¢ per point)") {
                Text(
                    text = "Tap − / + to adjust by 0.1¢, or tap any value to enter an exact amount.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp)
                )
                pointCurrencies.forEachIndexed { index, currency ->
                    val currentVal = settings.pointValuations[currency]
                        ?: currency.defaultCentsPerPoint
                    PointValuationStepperRow(
                        currency = currency,
                        currentValue = currentVal,
                        onStep = { delta ->
                            val next = ((currentVal + delta) * 10.0).roundToInt() / 10.0
                            onUpdatePointValuation(currency, next.coerceIn(0.5, 5.0))
                        },
                        onTapValue = { editingCurrency = currency }
                    )
                    if (index < pointCurrencies.lastIndex) {
                        SettingsDivider()
                    }
                }
            }

            val dialogCurrency = editingCurrency
            if (dialogCurrency != null) {
                val initial = settings.pointValuations[dialogCurrency]
                    ?: dialogCurrency.defaultCentsPerPoint
                PointValueEditDialog(
                    currency = dialogCurrency,
                    initialValue = initial,
                    onDismiss = { editingCurrency = null },
                    onSave = { updated ->
                        onUpdatePointValuation(dialogCurrency, updated)
                        editingCurrency = null
                    }
                )
            }
        }

        // --- APP UPDATES SECTION ---
        item(key = "section_app_updates") {
            var repoDraft by remember(settings.updateRepoSlug) {
                mutableStateOf(settings.updateRepoSlug)
            }
            var showRepoEditor by remember { mutableStateOf(false) }

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
                    Button(
                        onClick = onCheckForUpdates,
                        shape = CircleShape,
                        enabled = updateStatus !is UpdateStatus.Checking && updateStatus !is UpdateStatus.Downloading
                    ) {
                        Icon(
                            AppIcons.SystemUpdate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (updateStatus is UpdateStatus.Checking) "Checking…"
                            else "Check for updates"
                        )
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
                SettingsDivider()
                ExpandableSettingsRow(
                    title = "GitHub repository",
                    subtitle = settings.updateRepoSlug.ifBlank { "Not configured" },
                    expanded = showRepoEditor,
                    onToggle = { showRepoEditor = !showRepoEditor }
                ) {
                    OutlinedTextField(
                        value = repoDraft,
                        onValueChange = { repoDraft = it },
                        label = { Text("GitHub repository (owner/repo)") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilledTonalButton(
                        onClick = {
                            onUpdateRepoSlug(repoDraft)
                            showRepoEditor = false
                        },
                        shape = CircleShape
                    ) {
                        Text("Save repo")
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
private fun PointValuationStepperRow(
    currency: RewardCurrency,
    currentValue: Double,
    onStep: (Double) -> Unit,
    onTapValue: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = String.format(Locale.US, "Default %.1f¢", currency.defaultCentsPerPoint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilledTonalIconButton(
                onClick = { onStep(-0.1) },
                enabled = currentValue > 0.51,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = AppIcons.Remove,
                    contentDescription = "Decrease ${currency.displayName}",
                    modifier = Modifier.size(18.dp)
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onTapValue)
            ) {
                Text(
                    text = String.format(Locale.US, "%.1f¢", currentValue),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(56.dp)
                        .padding(vertical = 6.dp)
                )
            }

            FilledTonalIconButton(
                onClick = { onStep(0.1) },
                enabled = currentValue < 4.99,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Increase ${currency.displayName}",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PointValueEditDialog(
    currency: RewardCurrency,
    initialValue: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var draft by remember { mutableStateOf(String.format(Locale.US, "%.2f", initialValue)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(currency.displayName) },
        text = {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text("Cents per point (¢)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = draft.trim().toDoubleOrNull()
                    if (parsed != null) onSave(parsed.coerceIn(0.1, 10.0))
                },
                shape = CircleShape
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = { onSave(currency.defaultCentsPerPoint) }
                ) {
                    Text("Reset")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun ExpandableSettingsRow(
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevron_rotation"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
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
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(rotation)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    )
}
