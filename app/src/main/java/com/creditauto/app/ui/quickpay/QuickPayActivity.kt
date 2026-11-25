package com.creditauto.app.ui.quickpay

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.creditauto.app.MainActivity
import com.creditauto.app.data.location.LocationHelper
import com.creditauto.app.domain.SpendCategory
import com.creditauto.app.ui.CreditAutoViewModel
import com.creditauto.app.ui.components.HeroRecommendationCard
import com.creditauto.app.ui.components.RunnerUpCardRow
import com.creditauto.app.ui.theme.CreditAutoTheme
import kotlin.math.roundToInt

class QuickPayActivity : ComponentActivity() {

    private val viewModel: CreditAutoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CreditAutoTheme {
                val uiState by viewModel.uiState.collectAsState()

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    viewModel.detectNearbyStores()
                }

                LaunchedEffect(Unit) {
                    if (LocationHelper.hasLocationPermission(this@QuickPayActivity)) {
                        viewModel.detectNearbyStores()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }

                QuickPayOverlaySheet(
                    uiState = uiState,
                    onDismiss = { finish() },
                    onRefreshLocation = { viewModel.detectNearbyStores(forceRefresh = true) },
                    onSelectCategory = { viewModel.selectCategory(it) },
                    onSelectStore = { viewModel.selectStore(it) },
                    onToggleNoAmex = { viewModel.toggleAllowAmex() },
                    onToggleVisaOnly = { viewModel.toggleVisaOnly() },
                    onOpenWallet = { rec ->
                        viewModel.launchGoogleWallet(rec)
                        finish()
                    },
                    onOpenFullApp = {
                        startActivity(
                            Intent(this@QuickPayActivity, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickPayOverlaySheet(
    uiState: com.creditauto.app.ui.CreditAutoUiState,
    onDismiss: () -> Unit,
    onRefreshLocation: () -> Unit,
    onSelectCategory: (SpendCategory) -> Unit,
    onSelectStore: (com.creditauto.app.domain.DetectedStore) -> Unit,
    onToggleNoAmex: () -> Unit,
    onToggleVisaOnly: () -> Unit,
    onOpenWallet: (com.creditauto.app.domain.CardRecommendation?) -> Unit,
    onOpenFullApp: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outline)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Header with store info + refresh + close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.session.selectedStore.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val distText = uiState.session.selectedStore.distanceMeters?.let {
                            " • ${it.roundToInt()}m"
                        }.orEmpty()
                        Text(
                            text = "${uiState.session.selectedStore.category.displayName}$distText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (uiState.session.isDetectingStore) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = onRefreshLocation) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh location")
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                // Nearby stores switcher if multiple found
                if (uiState.session.nearbyStores.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(uiState.session.nearbyStores.take(5)) { store ->
                            FilterChip(
                                selected = store.name == uiState.session.selectedStore.name,
                                onClick = { onSelectStore(store) },
                                label = {
                                    Text(
                                        text = store.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }

                // Quick category pills
                val quickCategories = listOf(
                    SpendCategory.GROCERY,
                    SpendCategory.DINING,
                    SpendCategory.GAS,
                    SpendCategory.WHOLESALE_CLUB,
                    SpendCategory.DRUGSTORE,
                    SpendCategory.OTHER
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(quickCategories) { cat ->
                        FilterChip(
                            selected = uiState.session.selectedStore.category == cat,
                            onClick = { onSelectCategory(cat) },
                            label = { Text(cat.shortLabel) }
                        )
                    }
                }

                // Network restriction quick toggles
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    FilterChip(
                        selected = !uiState.session.allowAmex,
                        onClick = onToggleNoAmex,
                        label = { Text("No Amex") }
                    )
                    FilterChip(
                        selected = uiState.session.visaOnlyOverride,
                        onClick = onToggleVisaOnly,
                        label = { Text("Visa Only") }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onOpenFullApp) {
                        Text("Full App")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val topRec = uiState.recommendations.firstOrNull()
                if (topRec != null) {
                    HeroRecommendationCard(
                        recommendation = topRec,
                        onPayWithWallet = { onOpenWallet(topRec) },
                        compact = true
                    )

                    val runnerUp = uiState.recommendations.getOrNull(1)
                    if (runnerUp != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        RunnerUpCardRow(
                            rank = 2,
                            recommendation = runnerUp,
                            onSelectForWallet = { onOpenWallet(runnerUp) }
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No matching cards in your wallet for these filters. Tap Full App to add or enable cards.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Unspecified
                        )
                    }
                }
            }
        }
    }
}
