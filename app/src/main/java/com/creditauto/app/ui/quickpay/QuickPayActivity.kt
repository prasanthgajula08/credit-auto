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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import com.creditauto.app.ui.components.AppIcons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.creditauto.app.MainActivity
import com.creditauto.app.data.location.LocationHelper
import com.creditauto.app.domain.CardRecommendation
import com.creditauto.app.domain.DetectedStore
import com.creditauto.app.domain.SpendCategory
import com.creditauto.app.ui.CreditAutoUiState
import com.creditauto.app.ui.CreditAutoViewModel
import com.creditauto.app.ui.components.HeroRecommendationCard
import com.creditauto.app.ui.components.RunnerUpCardRow
import com.creditauto.app.ui.components.categoryIcon
import com.creditauto.app.ui.theme.CreditAutoTheme
import kotlin.math.roundToInt

class QuickPayActivity : ComponentActivity() {

    private val viewModel: CreditAutoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CreditAutoTheme(transparentSystemBars = false) {
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
    uiState: CreditAutoUiState,
    onDismiss: () -> Unit,
    onRefreshLocation: () -> Unit,
    onSelectCategory: (SpendCategory) -> Unit,
    onSelectStore: (DetectedStore) -> Unit,
    onToggleNoAmex: () -> Unit,
    onToggleVisaOnly: () -> Unit,
    onOpenWallet: (CardRecommendation?) -> Unit,
    onOpenFullApp: () -> Unit
) {
    val store = uiState.session.selectedStore

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
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Material 3 standard bottom sheet drag handle (32x4 dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 32.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Store header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon(store.category),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = store.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val distText = store.distanceMeters?.let { " • ${it.roundToInt()}m" }.orEmpty()
                        Text(
                            text = "${store.category.displayName}$distText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (uiState.session.isDetectingStore) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        FilledTonalIconButton(
                            onClick = onRefreshLocation,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = "Refresh location",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    FilledTonalIconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Nearby stores switcher
                if (uiState.session.nearbyStores.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 10.dp)
                    ) {
                        items(
                            items = uiState.session.nearbyStores.take(6),
                            key = { "${it.source.name}_${it.name}" }
                        ) { nearby ->
                            FilterChip(
                                selected = nearby.name == store.name,
                                onClick = { onSelectStore(nearby) },
                                shape = CircleShape,
                                label = {
                                    Text(
                                        text = nearby.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }

                // Quick category pills
                val quickCategories = remember {
                    listOf(
                        SpendCategory.GROCERY,
                        SpendCategory.DINING,
                        SpendCategory.GAS,
                        SpendCategory.WHOLESALE_CLUB,
                        SpendCategory.DRUGSTORE,
                        SpendCategory.OTHER
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 6.dp)
                ) {
                    items(quickCategories, key = { it.name }) { cat ->
                        FilterChip(
                            selected = store.category == cat,
                            onClick = { onSelectCategory(cat) },
                            shape = CircleShape,
                            label = { Text(cat.shortLabel) }
                        )
                    }
                }

                // Network toggles + open full app
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                ) {
                    FilterChip(
                        selected = !uiState.session.allowAmex,
                        onClick = onToggleNoAmex,
                        shape = CircleShape,
                        label = { Text("No Amex") }
                    )
                    FilterChip(
                        selected = uiState.session.visaOnlyOverride,
                        onClick = onToggleVisaOnly,
                        shape = CircleShape,
                        label = { Text("Visa only") }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onOpenFullApp) {
                        Text("Full app")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            AppIcons.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

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
                            shape = RoundedCornerShape(22.dp),
                            onSelectForWallet = { onOpenWallet(runnerUp) }
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No matching cards in your wallet for these filters. Tap Full app to manage cards.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
