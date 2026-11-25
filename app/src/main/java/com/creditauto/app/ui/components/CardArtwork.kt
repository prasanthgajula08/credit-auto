package com.creditauto.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creditauto.app.R
import com.creditauto.app.domain.CardNetwork
import com.creditauto.app.domain.CreditCard

@DrawableRes
fun cardImageResFor(cardId: String): Int? = when (cardId) {
    "chase_sapphire_preferred" -> R.drawable.card_chase_sapphire_preferred
    "chase_sapphire_reserve" -> R.drawable.card_chase_sapphire_reserve
    "chase_freedom_unlimited" -> R.drawable.card_chase_freedom_unlimited
    "chase_freedom_flex" -> R.drawable.card_chase_freedom_flex
    "amazon_prime_visa" -> R.drawable.card_amazon_prime_visa
    "amex_gold" -> R.drawable.card_amex_gold
    "amex_platinum" -> R.drawable.card_amex_platinum
    "amex_blue_cash_preferred" -> R.drawable.card_amex_blue_cash_preferred
    "amex_blue_cash_everyday" -> R.drawable.card_amex_blue_cash_everyday
    "amex_blue_business_plus" -> R.drawable.card_amex_blue_business_plus
    "capital_one_venture_x" -> R.drawable.card_capital_one_venture_x
    "capital_one_venture" -> R.drawable.card_capital_one_venture
    "capital_one_savor" -> R.drawable.card_capital_one_savor
    "capital_one_quicksilver" -> R.drawable.card_capital_one_quicksilver
    "citi_custom_cash" -> R.drawable.card_citi_custom_cash
    "citi_double_cash" -> R.drawable.card_citi_double_cash
    "citi_strata_premier" -> R.drawable.card_citi_strata_premier
    "discover_it" -> R.drawable.card_discover_it
    "wells_fargo_autograph" -> R.drawable.card_wells_fargo_autograph
    "wells_fargo_active_cash" -> R.drawable.card_wells_fargo_active_cash
    "bilt_mastercard" -> R.drawable.card_bilt_mastercard
    "us_bank_altitude_reserve" -> R.drawable.card_us_bank_altitude_reserve
    "boa_customized_cash" -> R.drawable.card_boa_customized_cash
    else -> null
}

@DrawableRes
fun networkLogoResFor(network: CardNetwork): Int = when (network) {
    CardNetwork.VISA -> R.drawable.logo_visa
    CardNetwork.MASTERCARD -> R.drawable.logo_mastercard
    CardNetwork.AMEX -> R.drawable.logo_amex
    CardNetwork.DISCOVER -> R.drawable.logo_discover
}

@DrawableRes
fun issuerLogoResFor(card: CreditCard): Int? {
    val id = card.id.lowercase()
    val issuer = card.issuer.lowercase()
    val name = card.name.lowercase()
    return when {
        id.contains("prime") || name.contains("prime") -> R.drawable.logo_amazon_prime
        id.contains("bilt") || name.contains("bilt") -> R.drawable.logo_bilt
        id.contains("target") || name.contains("target") -> R.drawable.logo_target
        id.contains("fidelity") || issuer.contains("fidelity") -> R.drawable.logo_fidelity
        id.contains("robinhood") || name.contains("robinhood") -> R.drawable.logo_robinhood
        issuer.contains("chase") -> R.drawable.logo_chase
        issuer.contains("american express") || issuer.contains("amex") -> R.drawable.logo_amex
        issuer.contains("capital one") -> R.drawable.logo_capital_one
        issuer.contains("citi") -> R.drawable.logo_citi
        issuer.contains("discover") -> R.drawable.logo_discover
        issuer.contains("wells fargo") -> R.drawable.logo_wells_fargo
        issuer.contains("bank of america") || issuer.contains("bofa") -> R.drawable.logo_bofa
        issuer.contains("us bank") -> R.drawable.logo_us_bank
        else -> null
    }
}

/**
 * Displays the real official credit card face photo (for catalog cards) or a clean
 * branded card face using official downloaded bank & network PNG logos (for custom cards).
 */
@Composable
fun RealisticCreditCardFace(
    card: CreditCard,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val realImageRes = cardImageResFor(card.id)
    val radius = if (compact) 16.dp else 20.dp

    if (realImageRes != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1.586f)
                .clip(RoundedCornerShape(radius))
                .background(Color(card.colorHex))
        ) {
            Image(
                painter = painterResource(id = realImageRes),
                contentDescription = card.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Subtle pill overlay ONLY if user saved their last 4 digits
            if (card.lastFour.isNotBlank()) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.62f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "•••• ${card.lastFour}",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    } else {
        // Fallback for custom cards or cards without a card photo:
        // uses REAL official Wikimedia PNG issuer + network logos on a crisp white pill!
        val baseColor = Color(card.colorHex)
        val accentColor = Color(card.accentHex)
        val issuerLogo = issuerLogoResFor(card)
        val networkLogo = networkLogoResFor(card.network)

        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1.586f)
                .clip(RoundedCornerShape(radius))
                .background(
                    Brush.linearGradient(
                        listOf(baseColor, baseColor.copy(alpha = 0.88f), accentColor.copy(alpha = 0.45f))
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(radius)
                )
                .padding(if (compact) 16.dp else 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (issuerLogo != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.94f)
                        ) {
                            Image(
                                painter = painterResource(id = issuerLogo),
                                contentDescription = card.issuer,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .height(if (compact) 18.dp else 22.dp)
                            )
                        }
                    } else {
                        Text(
                            text = card.issuer.uppercase(),
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Text(
                    text = card.walletNickname.ifBlank { card.name },
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (card.lastFour.isNotBlank()) "•••• ${card.lastFour}" else card.issuer,
                        color = Color.White.copy(alpha = 0.82f),
                        fontFamily = if (card.lastFour.isNotBlank()) FontFamily.Monospace else FontFamily.Default,
                        fontSize = 13.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.95f)
                    ) {
                        Image(
                            painter = painterResource(id = networkLogo),
                            contentDescription = card.network.displayName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .height(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact 1.586:1 real card thumbnail used in Runner-Up rows, My Cards, and Card Catalog.
 */
@Composable
fun MiniCreditCardThumbnail(
    card: CreditCard,
    width: Dp = 60.dp,
    modifier: Modifier = Modifier
) {
    val realImageRes = cardImageResFor(card.id)
    if (realImageRes != null) {
        Image(
            painter = painterResource(id = realImageRes),
            contentDescription = card.name,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .width(width)
                .aspectRatio(1.586f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(card.colorHex))
        )
    } else {
        val networkLogo = networkLogoResFor(card.network)
        val issuerLogo = issuerLogoResFor(card)
        Box(
            modifier = modifier
                .width(width)
                .aspectRatio(1.586f)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(card.colorHex), Color(card.accentHex).copy(alpha = 0.55f))
                    )
                )
                .padding(4.dp)
        ) {
            if (issuerLogo != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Image(
                        painter = painterResource(id = issuerLogo),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .padding(horizontal = 3.dp, vertical = 1.5.dp)
                            .height(9.dp)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.White.copy(alpha = 0.94f),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Image(
                    painter = painterResource(id = networkLogo),
                    contentDescription = card.network.displayName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(horizontal = 3.dp, vertical = 1.5.dp)
                        .height(9.dp)
                )
            }
        }
    }
}

/**
 * Clean official network badge pill (real Visa / Mastercard / Amex / Discover PNG).
 */
@Composable
fun OfficialNetworkBadge(
    network: CardNetwork,
    height: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = networkLogoResFor(network)),
            contentDescription = network.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .height(height)
        )
    }
}

/**
 * Clean official issuer badge pill (real Chase / Amex / Capital One / Citi / etc. PNG).
 */
@Composable
fun OfficialIssuerBadge(
    card: CreditCard,
    height: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    val res = issuerLogoResFor(card) ?: return
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = res),
            contentDescription = card.issuer,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .height(height)
        )
    }
}
