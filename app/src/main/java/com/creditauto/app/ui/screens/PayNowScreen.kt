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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.creditauto.app.domain.CardNetwork
import com.creditauto.app.domain.CardRecommendation
import com.creditauto.app.domain.DetectedStore
import com.creditauto.app.domain.SpendCategory
import com.creditauto.app.ui.CreditAutoUiState
import com.creditauto.app.ui.components.HeroRecommendationCard
import com.creditauto.app.ui.components.RunnerUpCardRow
import com.creditauto.app.ui.components.categoryIcon
import com.creditauto.app.update.UpdateStatus
import kotlin.math.roundToInt

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
    var showSavePlaceDialog by remember { mutableStateOf(false) }

    val session = uiState.session
    val store = session.selectedStore

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // In-app update available banner (1-tap download & install from GitHub Releases)
        if (updateStatus is UpdateStatus.Available || updateStatus is UpdateStatus.Downloading) {
            item(key = "update_banner") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SystemUpdate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            when (updateStatus) {
                                is UpdateStatus.Available -> {
                                    Text(
                                        text = "Update ${updateStatus.info.tagName} ready",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(onClick = onInstallUpdate) {
                                        Text("Update")
                                    }
                                }
                                is UpdateStatus.Downloading -> {
                                    Text(
                                        text = "Downloading ${updateStatus.info.tagName}... ${updateStatus.progressPercent}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                else -> Unit
                            }
                        }
                        if (updateStatus is UpdateStatus.Downloading) {
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { updateStatus.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // First-run sample wallet banner
        if (!uiState.settings.hasCustomizedWallet) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Using ${uiState.ownedCards.size} sample cards. Pick the cards you own in My Cards.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onGoToMyCards) {
                            Text("Setup")
                        }
                    }
                }
            }
        }

        // Detected Store Header Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = categoryIcon(store.category),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = store.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
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

                        IconButton(onClick = { showSavePlaceDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.BookmarkAdd,
                                contentDescription = "Save store"
                            )
                        }

                        if (session.isDetectingStore) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            IconButton(onClick = onRequestLocationAndDetect) {
                                Icon(
                                    imageVector = Icons.Rounded.MyLocation,
                                    contentDescription = "Detect nearby store",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (!hasLocationPermission) {
                        Spacer(modifier = Modifier.height(6.dp))
                        AssistChip(
                            onClick = onRequestLocationAndDetect,
                            label = { Text("Enable GPS to auto-detect stores") },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.MyLocation,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    } else if (session.statusMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = session.statusMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (session.nearbyStores.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nearby stores",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 4.dp)
                        ) {
                            items(
                                items = session.nearbyStores,
                                key = { "${it.source.name}_${it.name}" }
                            ) { nearby ->
                                val dist = nearby.distanceMeters?.let { " (${it.roundToInt()}m)" }.orEmpty()
                                FilterChip(
                                    selected = nearby.name == store.name && nearby.source == store.source,
                                    onClick = { onSelectStore(nearby) },
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

        // Manual store search (isolated state so typing doesn't recompose the whole screen)
        item(key = "store_search_bar") {
            StoreSearchBar(onManualSearch = onManualSearch)
        }

        // Quick category selector strip
        item(key = "category_strip") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = SpendCategory.entries,
                    key = { it.name }
                ) { category ->
                    FilterChip(
                        selected = store.category == category,
                        onClick = { onSelectCategory(category) },
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

        // Context constraint pills
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = session.isMobileWallet,
                        onClick = onToggleMobileWallet,
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Contactless,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Google Pay Tap") }
                    )
                }
                item {
                    FilterChip(
                        selected = !session.allowAmex,
                        onClick = onToggleAllowAmex,
                        label = { Text("No Amex") }
                    )
                }
                item {
                    FilterChip(
                        selected = session.visaOnlyOverride,
                        onClick = onToggleVisaOnly,
                        label = { Text("Visa Only") }
                    )
                }
                item {
                    FilterChip(
                        selected = session.isForeignTransaction,
                        onClick = onToggleForeign,
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

        // Hero best card recommendation
        val topCard = uiState.recommendations.firstOrNull()
        if (topCard != null) {
            item {
                HeroRecommendationCard(
                    recommendation = topCard,
                    onPayWithWallet = { onPayWithWallet(topCard) }
                )
            }
        } else {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No eligible cards found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add your credit cards in My Cards or relax the network filters above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onGoToMyCards) {
                            Text("Manage My Cards")
                        }
                    }
                }
            }
        }

        // Runner-up cards comparison
        if (uiState.recommendations.size > 1) {
            item {
                Text(
                    text = "Other cards in your wallet",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            itemsIndexed(
                items = uiState.recommendations.drop(1),
                key = { _, rec -> rec.card.id }
            ) { index, rec ->
                RunnerUpCardRow(
                    rank = index + 2,
                    recommendation = rec,
                    onSelectForWallet = { onPayWithWallet(rec) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showSavePlaceDialog) {
        QuickSaveStoreDialog(
            initialName = store.name,
            initialCategory = store.category,
            initialNetworks = store.acceptedNetworks,
            ownedCards = uiState.ownedCards,
            onDismiss = { showSavePlaceDialog = false },
            onConfirm = { name, category, networks, preferredCardId ->
                onSaveStorePlace(name, category, networks, preferredCardId)
                showSavePlaceDialog = false
            }
        )
    }
}

@Composable
private fun StoreSearchBar(
    onManualSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Type any store (Costco, Whole Foods, Target...)") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            if (searchQuery.isNotBlank()) {
                TextButton(
                    onClick = {
                        onManualSearch(searchQuery)
                        keyboardController?.hide()
                    }
                ) {
                    Text("Go")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onManualSearch(searchQuery)
                keyboardController?.hide()
            }
        )
    )
}

@Composable
private fun QuickSaveStoreDialog(
    initialName: String,
    initialCategory: SpendCategory,
    initialNetworks: Set<CardNetwork>,
    ownedCards: List<com.creditauto.app.domain.CreditCard>,
    onDismiss: () -> Unit,
    onConfirm: (String, SpendCategory, Set<CardNetwork>, String?) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var category by remember { mutableStateOf(initialCategory) }
    var networks by remember { mutableStateOf(initialNetworks) }
    var preferredCardId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Store Rule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Store name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Category", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SpendCategory.entries) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.shortLabel) }
                        )
                    }
                }
                Text("Accepted networks", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CardNetwork.entries.forEach { net ->
                        val selected = net in networks
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val next = networks.toMutableSet()
                                if (selected && next.size > 1) next.remove(net) else next.add(net)
                                networks = next
                            },
                            label = { Text(net.shortBadge) }
                        )
                    }
                }
                if (ownedCards.isNotEmpty()) {
                    Text("Pin specific card (optional)", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = preferredCardId == null,
                                onClick = { preferredCardId = null },
                                label = { Text("Auto") }
                            )
                        }
                        items(ownedCards) { card ->
                            FilterChip(
                                selected = preferredCardId == card.id,
                                onClick = { preferredCardId = card.id },
                                label = { Text(card.walletNickname.ifBlank { card.name }) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onConfirm(name, category, networks, preferredCardId)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
