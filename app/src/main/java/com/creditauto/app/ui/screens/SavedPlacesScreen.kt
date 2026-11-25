package com.creditauto.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Place
import com.creditauto.app.ui.components.AppIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.creditauto.app.domain.CardNetwork
import com.creditauto.app.domain.CreditCard
import com.creditauto.app.domain.DetectedStore
import com.creditauto.app.domain.SavedPlace
import com.creditauto.app.domain.SpendCategory
import com.creditauto.app.domain.StoreSource
import com.creditauto.app.ui.CreditAutoUiState
import com.creditauto.app.ui.components.categoryIcon
import com.creditauto.app.ui.components.segmentedItemShape

@Composable
fun SavedPlacesScreen(
    uiState: CreditAutoUiState,
    onAddPlace: (
        name: String,
        category: SpendCategory,
        attachCurrentGps: Boolean,
        acceptedNetworks: Set<CardNetwork>,
        preferredCardId: String?
    ) -> Unit,
    onDeletePlace: (String) -> Unit,
    onUsePlaceNow: (DetectedStore) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "places_intro") {
                Text(
                    text = "Saved stores match instantaneously via local GPS before querying OpenStreetMap or Google Places — great for Visa-only Costco or pinned card rules.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (uiState.savedPlaces.isEmpty()) {
                item(key = "empty_places") {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No saved stores yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bookmark any store from the Pay tab or tap Save place below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                item(key = "saved_places_group") {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        uiState.savedPlaces.forEachIndexed { index, place ->
                            val pinnedCard = uiState.ownedCards.firstOrNull { it.id == place.preferredCardId }
                            SavedPlaceRow(
                                place = place,
                                pinnedCardName = pinnedCard?.displayLabel(),
                                shape = segmentedItemShape(index, uiState.savedPlaces.size),
                                onUseNow = {
                                    onUsePlaceNow(
                                        DetectedStore(
                                            name = place.name,
                                            category = place.category,
                                            source = StoreSource.SAVED_PLACE,
                                            address = place.address,
                                            latitude = place.latitude,
                                            longitude = place.longitude,
                                            acceptedNetworks = place.acceptedNetworks,
                                            savedPlaceId = place.id,
                                            preferredCardId = place.preferredCardId
                                        )
                                    )
                                },
                                onDelete = { onDeletePlace(place.id) }
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            icon = { Icon(AppIcons.AddLocation, contentDescription = null) },
            text = { Text("Save place") },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showAddSheet) {
        AddPlaceBottomSheet(
            hasCurrentGps = uiState.session.lastLatitude != null,
            ownedCards = uiState.ownedCards,
            onDismiss = { showAddSheet = false },
            onConfirm = { name, cat, attachGps, nets, pinnedId ->
                onAddPlace(name, cat, attachGps, nets, pinnedId)
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun SavedPlaceRow(
    place: SavedPlace,
    pinnedCardName: String?,
    shape: Shape,
    onUseNow: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onUseNow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
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
                    imageVector = categoryIcon(place.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val netInfo = if (place.acceptedNetworks.size < CardNetwork.entries.size) {
                    " • ${place.acceptedNetworks.joinToString("/") { it.shortBadge }} only"
                } else {
                    ""
                }
                Text(
                    text = "${place.category.displayName}$netInfo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (pinnedCardName != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Pinned: $pinnedCardName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                if (place.latitude != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Local GPS match (${place.radiusMeters.toInt()}m)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete saved place",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddPlaceBottomSheet(
    hasCurrentGps: Boolean,
    ownedCards: List<CreditCard>,
    onDismiss: () -> Unit,
    onConfirm: (String, SpendCategory, Boolean, Set<CardNetwork>, String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(SpendCategory.GROCERY) }
    var attachGps by remember { mutableStateOf(hasCurrentGps) }
    var networks by remember { mutableStateOf(CardNetwork.entries.toSet()) }
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
                text = "Save store & rule",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Store name (e.g. Local Costco)") },
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Attach current GPS location",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (hasCurrentGps) "Auto-detects when you arrive here" else "Detect location on Pay tab first",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = attachGps && hasCurrentGps,
                    onCheckedChange = { attachGps = it },
                    enabled = hasCurrentGps
                )
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
                        item(key = "auto") {
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
                    if (name.isNotBlank()) {
                        onConfirm(name, category, attachGps && hasCurrentGps, networks, preferredCardId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape
            ) {
                Text("Save place")
            }
        }
    }
}
