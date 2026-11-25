package com.creditauto.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.creditauto.app.domain.CardRecommendation
import com.creditauto.app.domain.SpendCategory
import java.util.Locale

fun categoryIcon(category: SpendCategory): ImageVector = when (category) {
    SpendCategory.GROCERY -> AppIcons.Grocery
    SpendCategory.DINING -> AppIcons.Dining
    SpendCategory.GAS -> AppIcons.Gas
    SpendCategory.TRAVEL -> AppIcons.Flight
    SpendCategory.TRANSIT -> AppIcons.Transit
    SpendCategory.DRUGSTORE -> AppIcons.Pharmacy
    SpendCategory.WHOLESALE_CLUB -> AppIcons.Warehouse
    SpendCategory.DEPARTMENT_STORE -> AppIcons.Storefront
    SpendCategory.ONLINE_SHOPPING -> Icons.Rounded.ShoppingCart
    SpendCategory.HOME_IMPROVEMENT -> AppIcons.HomeTools
    SpendCategory.ENTERTAINMENT -> AppIcons.Movie
    SpendCategory.STREAMING -> AppIcons.Streaming
    SpendCategory.UTILITIES -> AppIcons.Bolt
    SpendCategory.OTHER -> AppIcons.ShoppingBag
}

fun segmentedItemShape(index: Int, totalCount: Int): Shape = when {
    totalCount <= 1 -> RoundedCornerShape(24.dp)
    index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
    index == totalCount - 1 -> RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    else -> RoundedCornerShape(6.dp)
}

@Composable
fun HeroRecommendationCard(
    recommendation: CardRecommendation,
    onPayWithWallet: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val card = recommendation.card
    val returnText = String.format(Locale.US, "%.1f%% back", recommendation.effectiveReturnPercent)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(if (compact) 14.dp else 16.dp)) {
            RealisticCreditCardFace(
                card = card,
                badgeLabel = if (recommendation.isPinnedOverride) "Pinned for this store" else "Best rewards match",
                returnBadgeText = returnText,
                isPinned = recommendation.isPinnedOverride,
                compact = compact
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = recommendation.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            if (recommendation.warning != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = recommendation.warning,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onPayWithWallet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = AppIcons.Wallet,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pay with Google Wallet",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun RunnerUpCardRow(
    rank: Int,
    recommendation: CardRecommendation,
    onSelectForWallet: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp)
) {
    val card = recommendation.card
    val pct = String.format(Locale.US, "%.1f%%", recommendation.effectiveReturnPercent)
    val delta = String.format(Locale.US, "-%.1f%%", recommendation.deltaBelowBestPercent)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onSelectForWallet),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniCreditCardThumbnail(card = card, width = 58.dp)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.displayLabel(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "#$rank • ${recommendation.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = pct,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                if (recommendation.deltaBelowBestPercent > 0.05) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = delta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
