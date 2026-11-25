package com.creditauto.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LibraryAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.creditauto.app.domain.CardNetwork
import com.creditauto.app.domain.CreditCard
import com.creditauto.app.domain.RewardCurrency
import com.creditauto.app.domain.SpendCategory
import com.creditauto.app.ui.CreditAutoUiState
import java.util.Locale
import java.util.UUID

@Composable
fun MyCardsScreen(
    uiState: CreditAutoUiState,
    onToggleCardOwned: (String, Boolean) -> Unit,
    onToggleCardActive: (String, Boolean) -> Unit,
    onSaveCard: (CreditCard) -> Unit,
    onDeleteCard: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCatalogDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CreditCard?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showCatalogDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.LibraryAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Card Catalog")
                }
                OutlinedButton(
                    onClick = {
                        isCreatingNew = true
                        editingCard = CreditCard(
                            id = "custom_${UUID.randomUUID()}",
                            name = "",
                            issuer = "",
                            network = CardNetwork.VISA,
                            isOwned = true,
                            isActive = true,
                            rewardCurrency = RewardCurrency.CASH_BACK,
                            baseRate = 1.5
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Custom Card")
                }
            }
        }

        item {
            Text(
                text = "Your Wallet (${uiState.ownedCards.size} cards)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.ownedCards.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Tap \"Card Catalog\" to pick from 22 popular US cards or \"Custom Card\" to add your own.",
                        modifier = Modifier.padding(18.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        items(uiState.ownedCards, key = { it.id }) { card ->
            OwnedCardItem(
                card = card,
                onToggleActive = { onToggleCardActive(card.id, it) },
                onEdit = {
                    isCreatingNew = false
                    editingCard = card
                },
                onRemove = { onToggleCardOwned(card.id, false) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showCatalogDialog) {
        CardCatalogDialog(
            allCards = uiState.allCards,
            onToggleOwned = onToggleCardOwned,
            onDismiss = { showCatalogDialog = false }
        )
    }

    val cardToEdit = editingCard
    if (cardToEdit != null) {
        CardEditorDialog(
            card = cardToEdit,
            isNew = isCreatingNew,
            onDismiss = { editingCard = null },
            onSave = { updated ->
                onSaveCard(updated)
                editingCard = null
            },
            onDelete = if (!isCreatingNew && cardToEdit.id.startsWith("custom_")) {
                {
                    onDeleteCard(cardToEdit.id)
                    editingCard = null
                }
            } else {
                null
            }
        )
    }
}

@Composable
private fun OwnedCardItem(
    card: CreditCard,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(card.colorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CreditCard,
                        contentDescription = null,
                        tint = Color(card.accentHex),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.displayLabel(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${card.issuer} • ${card.network.displayName} • ${card.rewardCurrency.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Switch(
                    checked = card.isActive,
                    onCheckedChange = onToggleActive
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val highlights = buildList {
                card.mobileWalletRate?.let { add("${fmt(it)}${card.rewardCurrency.unitLabel} Google Pay") }
                card.categoryRates.entries
                    .sortedByDescending { it.value }
                    .take(4)
                    .forEach { (cat, rate) ->
                        add("${fmt(rate)}${card.rewardCurrency.unitLabel} ${cat.shortLabel}")
                    }
                add("${fmt(card.baseRate)}${card.rewardCurrency.unitLabel} Base")
            }.joinToString("  •  ")

            Text(
                text = highlights,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Rates & Last 4")
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Remove from wallet",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CardCatalogDialog(
    allCards: List<CreditCard>,
    onToggleOwned: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(allCards, query) {
        if (query.isBlank()) allCards
        else allCards.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.issuer.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Your Credit Cards") },
        text = {
            Column(modifier = Modifier.height(440.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search Chase, Amex, Citi, Costco...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filtered, key = { it.id }) { card ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (card.isOwned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            onClick = { onToggleOwned(card.id, !card.isOwned) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(card.colorHex))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = card.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = card.notes.ifBlank { "${card.issuer} • ${card.network.displayName}" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                                Checkbox(
                                    checked = card.isOwned,
                                    onCheckedChange = { onToggleOwned(card.id, it) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Done")
            }
        }
    )
}

@Composable
private fun CardEditorDialog(
    card: CreditCard,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (CreditCard) -> Unit,
    onDelete: (() -> Unit)?
) {
    var name by remember { mutableStateOf(card.name) }
    var issuer by remember { mutableStateOf(card.issuer) }
    var nickname by remember { mutableStateOf(card.walletNickname) }
    var lastFour by remember { mutableStateOf(card.lastFour) }
    var network by remember { mutableStateOf(card.network) }
    var currency by remember { mutableStateOf(card.rewardCurrency) }
    var baseRateText by remember { mutableStateOf(fmt(card.baseRate)) }
    var mobileWalletRateText by remember { mutableStateOf(card.mobileWalletRate?.let(::fmt).orEmpty()) }
    var hasFtf by remember { mutableStateOf(card.hasForeignTransactionFee) }

    val categoryRateStrings = remember {
        mutableStateMapOf<SpendCategory, String>().apply {
            card.categoryRates.forEach { (cat, rate) -> put(cat, fmt(rate)) }
        }
    }

    val editableCategories = remember {
        SpendCategory.entries.filter { it != SpendCategory.OTHER }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Add Custom Card" else "Edit ${card.name}") },
        text = {
            LazyColumn(
                modifier = Modifier.height(460.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "name_fields") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Card name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = issuer,
                                onValueChange = { issuer = it },
                                label = { Text("Issuer") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = lastFour,
                                onValueChange = { if (it.length <= 4) lastFour = it.filter(Char::isDigit) },
                                label = { Text("Last 4") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(100.dp)
                            )
                        }
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text("Google Wallet nickname (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item(key = "network_currency") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Network", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CardNetwork.entries.forEach { net ->
                                FilterChip(
                                    selected = network == net,
                                    onClick = { network = net },
                                    label = { Text(net.shortBadge) }
                                )
                            }
                        }

                        Text("Reward currency", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(RewardCurrency.entries, key = { it.name }) { curr ->
                                FilterChip(
                                    selected = currency == curr,
                                    onClick = { currency = curr },
                                    label = { Text(curr.displayName) }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = baseRateText,
                                onValueChange = { baseRateText = it },
                                label = { Text("Base rate") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = mobileWalletRateText,
                                onValueChange = { mobileWalletRateText = it },
                                label = { Text("Google Pay bonus") },
                                placeholder = { Text("e.g. 3") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Charges foreign transaction fee (~3%)")
                            Switch(checked = hasFtf, onCheckedChange = { hasFtf = it })
                        }

                        Text(
                            text = "Category bonus multipliers (leave blank for base rate)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(editableCategories, key = { it.name }) { cat ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cat.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = categoryRateStrings[cat].orEmpty(),
                            onValueChange = { categoryRateStrings[cat] = it },
                            placeholder = { Text("—") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(86.dp)
                        )
                    }
                }

                if (onDelete != null) {
                    item(key = "delete_btn") {
                        TextButton(onClick = onDelete) {
                            Text("Delete custom card", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim()
                    if (cleanName.isBlank()) return@Button
                    val parsedCategories = categoryRateStrings.entries.mapNotNull { (cat, str) ->
                        val parsed = str.trim().toDoubleOrNull()
                        if (parsed != null && parsed > 0.0) cat to parsed else null
                    }.toMap()

                    onSave(
                        card.copy(
                            name = cleanName,
                            issuer = issuer.trim().ifBlank { "Custom" },
                            walletNickname = nickname.trim(),
                            lastFour = lastFour.trim(),
                            network = network,
                            rewardCurrency = currency,
                            baseRate = baseRateText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 1.0,
                            mobileWalletRate = mobileWalletRateText.toDoubleOrNull()?.takeIf { it > 0.0 },
                            hasForeignTransactionFee = hasFtf,
                            categoryRates = parsedCategories,
                            isOwned = true
                        )
                    )
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

private fun fmt(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.US, "%.1f", value)
