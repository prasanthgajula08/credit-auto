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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.creditauto.app.domain.CardNetwork
import com.creditauto.app.domain.DetectedStore
import com.creditauto.app.domain.SavedPlace
import com.creditauto.app.domain.SpendCategory
import com.creditauto.app.domain.StoreSource
import com.creditauto.app.ui.CreditAutoUiState
import com.creditauto.app.ui.components.categoryIcon

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
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Rounded.AddLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Saved Store & Rule")
            }
        }

        item {
            Text(
                text = "Saved stores take priority over OpenStreetMap/Google Places when you're nearby — ideal for merchant quirks like Visa-only Costco or local restaurants.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.savedPlaces.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No saved places yet. Tap the bookmark icon while at any store on the Pay Now screen, or add one here.",
                        modifier = Modifier.padding(18.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        items(uiState.savedPlaces, key = { it.id }) { place ->
            val pinnedCard = uiState.ownedCards.firstOrNull { it.id == place.preferredCardId }
            SavedPlaceRow(
                place = place,
                pinnedCardName = pinnedCard?.displayLabel(),
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

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showAddDialog) {
        AddPlaceDialog(
            hasCurrentGps = uiState.session.lastLatitude != null,
            ownedCards = uiState.ownedCards,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cat, attachGps, nets, pinnedId ->
                onAddPlace(name, cat, attachGps, nets, pinnedId)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SavedPlaceRow(
    place: SavedPlace,
    pinnedCardName: String?,
    onUseNow: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
        onClick = onUseNow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = categoryIcon(place.category),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
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
                    Text(
                        text = "Pinned: $pinnedCardName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (place.latitude != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Auto-detects via GPS (${place.radiusMeters.toInt()}m)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete saved place")
            }
        }
    }
}

@Composable
private fun AddPlaceDialog(
    hasCurrentGps: Boolean,
    ownedCards: List<com.creditauto.app.domain.CreditCard>,
    onDismiss: () -> Unit,
    onConfirm: (String, SpendCategory, Boolean, Set<CardNetwork>, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(SpendCategory.GROCERY) }
    var attachGps by remember { mutableStateOf(hasCurrentGps) }
    var networks by remember { mutableStateOf(CardNetwork.entries.toSet()) }
    var preferredCardId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Saved Store") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Store name (e.g. Local Costco)") },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (hasCurrentGps) "Attach current GPS location" else "Detect GPS on Pay Now first",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Switch(
                        checked = attachGps && hasCurrentGps,
                        onCheckedChange = { attachGps = it },
                        enabled = hasCurrentGps
                    )
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
                    if (name.isNotBlank()) {
                        onConfirm(name, category, attachGps && hasCurrentGps, networks, preferredCardId)
                    }
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
