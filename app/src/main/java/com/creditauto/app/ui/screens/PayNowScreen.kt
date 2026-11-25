package com.creditauto.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.creditauto.app.domain.CardNetwork
import com.creditauto.app.domain.CardRecommendation
import com.creditauto.app.domain.CreditCard
import com.creditauto.app.domain.DetectedStore
import com.creditauto.app.domain.SpendCategory
import com.creditauto.app.ui.CreditAutoUiState
import com.creditauto.app.ui.components.HeroRecommendationCard
import com.creditauto.app.ui.components.RunnerUpCardRow
import com.creditauto.app.ui.components.categoryIcon
import com.creditauto.app.ui.components.segmentedItemShape
import com.creditauto.app.update.UpdateStatus
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayNowScreen(
    uiState: CreditAutoUiState,
    updateStatus: UpdateStatus,
    hasLocationPermission: Boolean,
    onInstallUpdate: () -> Unit,
    onRequestLocationAndDetect: () -> Unit,
    onSelectStore: (DetectedStore) -> Unit,
    onSelectCategory: (SpendCategory) -> Unit,
    onManualSearch: (String) -> Unit,
    onToggleMobileWallet: () -> Unit,
    onToggleAllowAmex: () -> Unit,
    onToggleVisaOnly: () -> Unit,
    onToggleForeign: () -> Unit,
    onPayWithWallet: (CardRecommendation?) -> Unit,
    onGoToMyCards: () -> Unit,
    onSaveStorePlace: (name: String, category: SpendCategory, networks: Set<CardNetwork>, preferredCardId: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSavePlaceSheet by remember { mutableStateOf(false) }

    val session = uiState.session
    val store = session.selectedStore
    val runnerUps = remember(uiState.recommendations) { uiState.recommendations.drop(1) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // In-app update available banner
        if (updateStatus is UpdateStatus.Available || updateStatus is UpdateStatus.Downloading) {
            item(key = "update_banner") {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SystemUpdate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            when (updateStatus) {
                                is UpdateStatus.Available -> {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Update ${updateStatus.info.tagName} ready",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = "Tap to download and install",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Button(
                                        onClick = onInstallUpdate,
                                        shape = CircleShape
                                    ) {
                                        Text("Update")
                                    }
                                }
                                is UpdateStatus.Downloading -> {
                                    Text(
                                        text = "Downloading ${updateStatus.info.tagName}… ${updateStatus.progressPercent}%",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                else -> Unit
                            }
                        }
                        if (updateStatus is UpdateStatus.Downloading) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { updateStatus.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }
        }

        // First-run sample wallet banner
        if (!uiState.settings.hasCustomizedWallet) {
            item(key = "sample_wallet_banner") {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Showing ${uiState.ownedCards.size} sample cards. Pick your own cards to personalize rewards.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = onGoToMyCards,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Setup")
                        }
                    }
                }
            }
        }

        // Google Material 3 Pill Search Bar
        item(key = "store_search_bar") {
            StoreSearchPillBar(onManualSearch = onManualSearch)
        }

        // Detected Store Expressive Tonal Card
        item(key = "detected_store_card") {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIcon(store.category),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
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
                            val distanceLabel = store.distanceMeters?.let { " • ${it.roundToInt()}m" }.orEmpty()
                            val networkNote = if (store.acceptedNetworks.size == 1) {
                                " • ${store.acceptedNetworks.first().displayName} only"
                            } else {
                                ""
                            }
                            Text(
                                text = "${store.category.displayName}$distanceLabel • ${store.source.label}$networkNote",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        FilledTonalIconButton(
                            onClick = { showSavePlaceSheet = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BookmarkAdd,
                                contentDescription = "Save store",
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        if (session.isDetectingStore) {
                            Box(
                                modifier = Modifier.size(38.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp
                                )
                            }
                        } else {
                            FilledTonalIconButton(
                                onClick = onRequestLocationAndDetect,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MyLocation,
                                    contentDescription = "Detect nearby store",
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    if (!hasLocationPermission) {
                        Spacer(modifier = Modifier.height(10.dp))
                        AssistChip(
                            onClick = onRequestLocationAndDetect,
                            shape = CircleShape,
                            label = { Text("Enable location to auto-detect stores") },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.MyLocation,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    } else if (session.statusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = session.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (session.nearbyStores.size > 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nearby places",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 6.dp)
                        ) {
                            items(
                                items = session.nearbyStores,
                                key = { "${it.source.name}_${it.name}" }
                            ) { nearby ->
                                val selected = nearby.name == store.name && nearby.source == store.source
                                val dist = nearby.distanceMeters?.let { " · ${it.roundToInt()}m" }.orEmpty()
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectStore(nearby) },
                                    shape = CircleShape,
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                Icons.Rounded.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                    label = {
                                        Text(
                                            text = "${nearby.name}$dist",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hero best card recommendation
        val topCard = uiState.recommendations.firstOrNull()
        if (topCard != null) {
            item(key = "hero_card") {
                HeroRecommendationCard(
                    recommendation = topCard,
                    onPayWithWallet = { onPayWithWallet(topCard) }
                )
            }
        } else {
            item(key = "empty_cards_notice") {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No matching cards",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add your cards in the Cards tab or adjust the network filters below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = onGoToMyCards, shape = CircleShape) {
                            Text("Manage cards")
                        }
                    }
                }
            }
        }

        // Quick category selector strip
        item(key = "category_strip") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Quick category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        items = SpendCategory.entries,
                        key = { it.name }
                    ) { category ->
                        FilterChip(
                            selected = store.category == category,
                            onClick = { onSelectCategory(category) },
                            shape = CircleShape,
                            leadingIcon = {
                                Icon(
                                    imageVector = categoryIcon(category),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(category.shortLabel) }
                        )
                    }
                }
            }
        }

        // Checkout filters strip
        item(key = "checkout_filters") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item(key = "filter_gpay") {
                    FilterChip(
                        selected = session.isMobileWallet,
                        onClick = onToggleMobileWallet,
                        shape = CircleShape,
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Contactless,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Google Pay tap") }
                    )
                }
                item(key = "filter_no_amex") {
                    FilterChip(
                        selected = !session.allowAmex,
                        onClick = onToggleAllowAmex,
                        shape = CircleShape,
                        label = { Text("No Amex") }
                    )
                }
                item(key = "filter_visa_only") {
                    FilterChip(
                        selected = session.visaOnlyOverride,
                        onClick = onToggleVisaOnly,
                        shape = CircleShape,
                        label = { Text("Visa only") }
                    )
                }
                item(key = "filter_foreign") {
                    FilterChip(
                        selected = session.isForeignTransaction,
                        onClick = onToggleForeign,
                        shape = CircleShape,
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Public,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Foreign") }
                    )
                }
            }
        }

        // Runner-up cards comparison in M3 Expressive segmented group
        if (runnerUps.isNotEmpty()) {
            item(key = "runner_ups_header") {
                Text(
                    text = "Other cards in your wallet",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item(key = "runner_ups_group") {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    runnerUps.forEachIndexed { index, rec ->
                        RunnerUpCardRow(
                            rank = index + 2,
                            recommendation = rec,
                            shape = segmentedItemShape(index, runnerUps.size),
                            onSelectForWallet = { onPayWithWallet(rec) }
                        )
                    }
                }
            }
        }

        item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showSavePlaceSheet) {
        SaveStoreBottomSheet(
            initialName = store.name,
            initialCategory = store.category,
            initialNetworks = store.acceptedNetworks,
            ownedCards = uiState.ownedCards,
            onDismiss = { showSavePlaceSheet = false },
            onConfirm = { name, category, networks, preferredCardId ->
                onSaveStorePlace(name, category, networks, preferredCardId)
                showSavePlaceSheet = false
            }
        )
    }
}

@Composable
private fun StoreSearchPillBar(
    onManualSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape),
        placeholder = {
            Text(
                text = "Search any store (Costco, Whole Foods…)",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (searchQuery.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    FilledTonalButton(
                        onClick = {
                            onManualSearch(searchQuery)
                            keyboardController?.hide()
                        },
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .height(34.dp)
                    ) {
                        Text("Pick")
                    }
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onManualSearch(searchQuery)
                keyboardController?.hide()
            }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SaveStoreBottomSheet(
    initialName: String,
    initialCategory: SpendCategory,
    initialNetworks: Set<CardNetwork>,
    ownedCards: List<CreditCard>,
    onDismiss: () -> Unit,
    onConfirm: (String, SpendCategory, Set<CardNetwork>, String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(initialName) }
    var category by remember { mutableStateOf(initialCategory) }
    var networks by remember { mutableStateOf(initialNetworks) }
    var preferredCardId by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Save store rule",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Store name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Category", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SpendCategory.entries, key = { it.name }) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            shape = CircleShape,
                            label = { Text(cat.shortLabel) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Accepted networks", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CardNetwork.entries.forEach { net ->
                        val selected = net in networks
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val next = networks.toMutableSet()
                                if (selected && next.size > 1) next.remove(net) else next.add(net)
                                networks = next
                            },
                            shape = CircleShape,
                            label = { Text(net.displayName) }
                        )
                    }
                }
            }

            if (ownedCards.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Pin a specific card (optional)", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item(key = "auto_card") {
                            FilterChip(
                                selected = preferredCardId == null,
                                onClick = { preferredCardId = null },
                                shape = CircleShape,
                                label = { Text("Automatic") }
                            )
                        }
                        items(ownedCards, key = { it.id }) { card ->
                            FilterChip(
                                selected = preferredCardId == card.id,
                                onClick = { preferredCardId = card.id },
                                shape = CircleShape,
                                label = { Text(card.walletNickname.ifBlank { card.name }) }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (name.isNotBlank()) onConfirm(name, category, networks, preferredCardId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape
            ) {
                Text("Save store")
            }
        }
    }
}
