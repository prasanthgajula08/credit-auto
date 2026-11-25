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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import com.creditauto.app.ui.components.AppIcons
import com.creditauto.app.ui.components.MiniCreditCardThumbnail
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    var showCatalogSheet by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CreditCard?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    val activeCount = remember(uiState.ownedCards) { uiState.ownedCards.count { it.isActive } }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "wallet_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Your wallet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$activeCount active of ${uiState.ownedCards.size} saved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
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
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Custom card")
                    }
                }
            }

            if (uiState.ownedCards.isEmpty()) {
                item(key = "empty_wallet") {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
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
                                    AppIcons.CreditCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Add your credit cards",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pick from 27 preloaded US cards or add your own custom card rules.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
        }

        // Material 3 Extended FAB for Card Catalog
        ExtendedFloatingActionButton(
            onClick = { showCatalogSheet = true },
            icon = { Icon(AppIcons.LibraryAdd, contentDescription = null) },
            text = { Text("Card catalog") },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showCatalogSheet) {
        CardCatalogBottomSheet(
            allCards = uiState.allCards,
            onToggleOwned = onToggleCardOwned,
            onDismiss = { showCatalogSheet = false }
        )
    }

    val cardToEdit = editingCard
    if (cardToEdit != null) {
        CardEditorBottomSheet(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OwnedCardItem(
    card: CreditCard,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MiniCreditCardThumbnail(card = card, width = 62.dp)

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.displayLabel(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${card.issuer} • ${card.rewardCurrency.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Switch(
                    checked = card.isActive,
                    onCheckedChange = onToggleActive,
                    thumbContent = if (card.isActive) {
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

            Spacer(modifier = Modifier.height(10.dp))

            val pills = remember(card) {
                buildList {
                    card.mobileWalletRate?.let { add("${fmt(it)}${card.rewardCurrency.unitLabel} Google Pay") }
                    card.categoryRates.entries
                        .sortedByDescending { it.value }
                        .take(4)
                        .forEach { (cat, rate) ->
                            add("${fmt(rate)}${card.rewardCurrency.unitLabel} ${cat.shortLabel}")
                        }
                    add("${fmt(card.baseRate)}${card.rewardCurrency.unitLabel} Base")
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                pills.forEach { pillText ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = pillText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit rates & last 4")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardCatalogBottomSheet(
    allCards: List<CreditCard>,
    onToggleOwned: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val filtered = remember(allCards, query) {
        if (query.isBlank()) allCards
        else allCards.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.issuer.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(580.dp)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Select your cards",
                    style = MaterialTheme.typography.headlineSmall
                )
                Button(onClick = onDismiss, shape = CircleShape) {
                    Text("Done")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search Chase, Amex, Capital One, Citi…") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtered, key = { it.id }) { card ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (card.isOwned) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        onClick = { onToggleOwned(card.id, !card.isOwned) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MiniCreditCardThumbnail(card = card, width = 52.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = card.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = card.notes.ifBlank { "${card.issuer} • ${card.network.displayName}" },
                                    style = MaterialTheme.typography.bodySmall,
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardEditorBottomSheet(
    card: CreditCard,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (CreditCard) -> Unit,
    onDelete: (() -> Unit)?
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isNew) "New custom card" else "Edit card",
                    style = MaterialTheme.typography.headlineSmall
                )
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
                    },
                    shape = CircleShape
                ) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "basic_fields") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Card name") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = issuer,
                                onValueChange = { issuer = it },
                                label = { Text("Issuer") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = lastFour,
                                onValueChange = { if (it.length <= 4) lastFour = it.filter(Char::isDigit) },
                                label = { Text("Last 4") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(104.dp)
                            )
                        }
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text("Google Wallet nickname (optional)") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item(key = "network_and_rates") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Network", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CardNetwork.entries.forEach { net ->
                                FilterChip(
                                    selected = network == net,
                                    onClick = { network = net },
                                    shape = CircleShape,
                                    label = { Text(net.shortBadge) }
                                )
                            }
                        }

                        Text("Reward currency", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(RewardCurrency.entries, key = { it.name }) { curr ->
                                FilterChip(
                                    selected = currency == curr,
                                    onClick = { currency = curr },
                                    shape = CircleShape,
                                    label = { Text(curr.displayName) }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = baseRateText,
                                onValueChange = { baseRateText = it },
                                label = { Text("Base rate") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = mobileWalletRateText,
                                onValueChange = { mobileWalletRateText = it },
                                label = { Text("Google Pay bonus") },
                                placeholder = { Text("e.g. 3") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Foreign transaction fee (~3%)", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = hasFtf, onCheckedChange = { hasFtf = it })
                        }

                        Text(
                            text = "Category bonus multipliers",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
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
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = categoryRateStrings[cat].orEmpty(),
                            onValueChange = { categoryRateStrings[cat] = it },
                            placeholder = { Text("—") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(88.dp)
                        )
                    }
                }

                if (onDelete != null) {
                    item(key = "delete_custom") {
                        TextButton(onClick = onDelete) {
                            Text("Delete custom card", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

private fun fmt(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.US, "%.1f", value)
