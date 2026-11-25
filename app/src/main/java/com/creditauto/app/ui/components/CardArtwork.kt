package com.creditauto.app.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creditauto.app.domain.CardNetwork
import com.creditauto.app.domain.CreditCard

private enum class CardPattern {
    SAPPHIRE_FACETS,
    AMEX_CLASSIC_FRAME,
    AMEX_PRISM,
    VENTURE_ARCS,
    SWOOP_WAVES,
    TOPO_LINES,
    TARGET_BULLSEYE,
    BILT_MINIMAL,
    BRUSHED_METAL
}

private enum class IssuerBrand {
    CHASE,
    AMEX,
    CAPITAL_ONE,
    CITI,
    DISCOVER,
    WELLS_FARGO,
    BILT,
    US_BANK,
    BOFA,
    TARGET,
    FIDELITY,
    ROBINHOOD,
    PRIME,
    GENERIC
}

private data class CardDesignSpec(
    val gradient: List<Color>,
    val pattern: CardPattern,
    val issuerBrand: IssuerBrand,
    val isLightSurface: Boolean = false,
    val isGoldChip: Boolean = true,
    val cardTitleOverride: String? = null
)

private fun resolveDesignSpec(card: CreditCard): CardDesignSpec {
    return when (card.id) {
        "chase_sapphire_preferred" -> CardDesignSpec(
            gradient = listOf(Color(0xFF0A2458), Color(0xFF15458F), Color(0xFF0D2E6B)),
            pattern = CardPattern.SAPPHIRE_FACETS,
            issuerBrand = IssuerBrand.CHASE,
            isGoldChip = false,
            cardTitleOverride = "SAPPHIRE PREFERRED"
        )
        "chase_sapphire_reserve" -> CardDesignSpec(
            gradient = listOf(Color(0xFF090F1E), Color(0xFF14213D), Color(0xFF0A1429)),
            pattern = CardPattern.SAPPHIRE_FACETS,
            issuerBrand = IssuerBrand.CHASE,
            isGoldChip = false,
            cardTitleOverride = "SAPPHIRE RESERVE"
        )
        "chase_freedom_unlimited" -> CardDesignSpec(
            gradient = listOf(Color(0xFF0E3470), Color(0xFF1E56A0), Color(0xFF0F2D5E)),
            pattern = CardPattern.SWOOP_WAVES,
            issuerBrand = IssuerBrand.CHASE,
            isGoldChip = false,
            cardTitleOverride = "FREEDOM UNLIMITED"
        )
        "chase_freedom_flex" -> CardDesignSpec(
            gradient = listOf(Color(0xFF102A63), Color(0xFF1D4D8F), Color(0xFF0E7490)),
            pattern = CardPattern.SWOOP_WAVES,
            issuerBrand = IssuerBrand.CHASE,
            isGoldChip = false,
            cardTitleOverride = "FREEDOM FLEX"
        )
        "amazon_prime_visa" -> CardDesignSpec(
            gradient = listOf(Color(0xFF141923), Color(0xFF1F2937), Color(0xFF0F172A)),
            pattern = CardPattern.BRUSHED_METAL,
            issuerBrand = IssuerBrand.PRIME,
            isGoldChip = false,
            cardTitleOverride = "PRIME VISA"
        )
        "amex_gold" -> CardDesignSpec(
            gradient = listOf(Color(0xFFD6B260), Color(0xFFB58A32), Color(0xFFE7CA7B)),
            pattern = CardPattern.AMEX_CLASSIC_FRAME,
            issuerBrand = IssuerBrand.AMEX,
            isLightSurface = true,
            isGoldChip = true,
            cardTitleOverride = "GOLD"
        )
        "amex_platinum" -> CardDesignSpec(
            gradient = listOf(Color(0xFFD8DEE8), Color(0xFFB0BAC8), Color(0xFFE5EAF0)),
            pattern = CardPattern.AMEX_CLASSIC_FRAME,
            issuerBrand = IssuerBrand.AMEX,
            isLightSurface = true,
            isGoldChip = false,
            cardTitleOverride = "PLATINUM"
        )
        "amex_blue_cash_preferred" -> CardDesignSpec(
            gradient = listOf(Color(0xFF06568E), Color(0xFF0B84C6), Color(0xFF053D69)),
            pattern = CardPattern.AMEX_PRISM,
            issuerBrand = IssuerBrand.AMEX,
            isGoldChip = false,
            cardTitleOverride = "BLUE CASH PREFERRED"
        )
        "amex_blue_cash_everyday" -> CardDesignSpec(
            gradient = listOf(Color(0xFF0C7BB3), Color(0xFF38BDF8), Color(0xFF0369A1)),
            pattern = CardPattern.AMEX_PRISM,
            issuerBrand = IssuerBrand.AMEX,
            isGoldChip = false,
            cardTitleOverride = "BLUE CASH EVERYDAY"
        )
        "amex_blue_business_plus" -> CardDesignSpec(
            gradient = listOf(Color(0xFF174EA6), Color(0xFF2563EB), Color(0xFF1E3A8A)),
            pattern = CardPattern.AMEX_PRISM,
            issuerBrand = IssuerBrand.AMEX,
            isGoldChip = false,
            cardTitleOverride = "BLUE BUSINESS PLUS"
        )
        "capital_one_venture_x" -> CardDesignSpec(
            gradient = listOf(Color(0xFF0B0F17), Color(0xFF1A2232), Color(0xFF090D14)),
            pattern = CardPattern.VENTURE_ARCS,
            issuerBrand = IssuerBrand.CAPITAL_ONE,
            isGoldChip = false,
            cardTitleOverride = "VENTURE X"
        )
        "capital_one_venture" -> CardDesignSpec(
            gradient = listOf(Color(0xFF102642), Color(0xFF1E3A5F), Color(0xFF0D1E34)),
            pattern = CardPattern.VENTURE_ARCS,
            issuerBrand = IssuerBrand.CAPITAL_ONE,
            isGoldChip = false,
            cardTitleOverride = "VENTURE"
        )
        "capital_one_savor" -> CardDesignSpec(
            gradient = listOf(Color(0xFF7B341E), Color(0xFFB45309), Color(0xFF632614)),
            pattern = CardPattern.SWOOP_WAVES,
            issuerBrand = IssuerBrand.CAPITAL_ONE,
            isGoldChip = true,
            cardTitleOverride = "SAVOR"
        )
        "capital_one_quicksilver" -> CardDesignSpec(
            gradient = listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8), Color(0xFFE2E8F0)),
            pattern = CardPattern.SWOOP_WAVES,
            issuerBrand = IssuerBrand.CAPITAL_ONE,
            isLightSurface = true,
            isGoldChip = false,
            cardTitleOverride = "QUICKSILVER"
        )
        "citi_custom_cash" -> CardDesignSpec(
            gradient = listOf(Color(0xFF044D40), Color(0xFF0F766E), Color(0xFF064E3B)),
            pattern = CardPattern.SWOOP_WAVES,
            issuerBrand = IssuerBrand.CITI,
            isGoldChip = false,
            cardTitleOverride = "CUSTOM CASH"
        )
        "citi_double_cash" -> CardDesignSpec(
            gradient = listOf(Color(0xFF0C4A6E), Color(0xFF0E7490), Color(0xFF15803D)),
            pattern = CardPattern.SWOOP_WAVES,
            issuerBrand = IssuerBrand.CITI,
            isGoldChip = false,
            cardTitleOverride = "DOUBLE CASH"
        )
        "citi_strata_premier" -> CardDesignSpec(
            gradient = listOf(Color(0xFF1B1948), Color(0xFF312E81), Color(0xFF1E1B4B)),
            pattern = CardPattern.TOPO_LINES,
            issuerBrand = IssuerBrand.CITI,
            isGoldChip = false,
            cardTitleOverride = "STRATA PREMIER"
        )
        "costco_anywhere_visa" -> CardDesignSpec(
            gradient = listOf(Color(0xFFC8CFD8), Color(0xFF9AA4B2), Color(0xFFDDE3EA)),
            pattern = CardPattern.BRUSHED_METAL,
            issuerBrand = IssuerBrand.CITI,
            isLightSurface = true,
            isGoldChip = false,
            cardTitleOverride = "COSTCO ANYWHERE"
        )
        "discover_it" -> CardDesignSpec(
            gradient = listOf(Color(0xFFEA580C), Color(0xFFC2410C), Color(0xFFF97316)),
            pattern = CardPattern.SWOOP_WAVES,
            issuerBrand = IssuerBrand.DISCOVER,
            isGoldChip = false,
            cardTitleOverride = "discover it"
        )
        "wells_fargo_autograph" -> CardDesignSpec(
            gradient = listOf(Color(0xFF7F1D1D), Color(0xFFB91C1C), Color(0xFF450A0A)),
            pattern = CardPattern.BRUSHED_METAL,
            issuerBrand = IssuerBrand.WELLS_FARGO,
            isGoldChip = true,
            cardTitleOverride = "AUTOGRAPH"
        )
        "wells_fargo_active_cash" -> CardDesignSpec(
            gradient = listOf(Color(0xFF991B1B), Color(0xFFDC2626), Color(0xFFB45309)),
            pattern = CardPattern.SWOOP_WAVES,
            issuerBrand = IssuerBrand.WELLS_FARGO,
            isGoldChip = true,
            cardTitleOverride = "ACTIVE CASH"
        )
        "bilt_mastercard" -> CardDesignSpec(
            gradient = listOf(Color(0xFF0A0A0D), Color(0xFF1A1A20), Color(0xFF09090B)),
            pattern = CardPattern.BILT_MINIMAL,
            issuerBrand = IssuerBrand.BILT,
            isGoldChip = false,
            cardTitleOverride = "BILT"
        )
        "us_bank_altitude_reserve" -> CardDesignSpec(
            gradient = listOf(Color(0xFF0F1B3D), Color(0xFF1E3A8A), Color(0xFF0C142B)),
            pattern = CardPattern.TOPO_LINES,
            issuerBrand = IssuerBrand.US_BANK,
            isGoldChip = false,
            cardTitleOverride = "ALTITUDE RESERVE"
        )
        "boa_customized_cash" -> CardDesignSpec(
            gradient = listOf(Color(0xFFA51C30), Color(0xFFDC2626), Color(0xFF881337)),
            pattern = CardPattern.SWOOP_WAVES,
            issuerBrand = IssuerBrand.BOFA,
            isGoldChip = false,
            cardTitleOverride = "CUSTOMIZED CASH"
        )
        "target_circle_card" -> CardDesignSpec(
            gradient = listOf(Color(0xFFCC0000), Color(0xFFE11D48), Color(0xFF991B1B)),
            pattern = CardPattern.TARGET_BULLSEYE,
            issuerBrand = IssuerBrand.TARGET,
            isGoldChip = false,
            cardTitleOverride = "TARGET CIRCLE"
        )
        "fidelity_rewards_visa" -> CardDesignSpec(
            gradient = listOf(Color(0xFF14532D), Color(0xFF166534), Color(0xFF052E16)),
            pattern = CardPattern.BRUSHED_METAL,
            issuerBrand = IssuerBrand.FIDELITY,
            isGoldChip = true,
            cardTitleOverride = "FIDELITY REWARDS"
        )
        "robinhood_gold_card" -> CardDesignSpec(
            gradient = listOf(Color(0xFFEAB308), Color(0xFFCA8A04), Color(0xFFFDE047)),
            pattern = CardPattern.BRUSHED_METAL,
            issuerBrand = IssuerBrand.ROBINHOOD,
            isLightSurface = true,
            isGoldChip = true,
            cardTitleOverride = "GOLD CARD"
        )
        else -> {
            val issuer = card.issuer.lowercase()
            val brand = when {
                issuer.contains("chase") -> IssuerBrand.CHASE
                issuer.contains("amex") || issuer.contains("american express") -> IssuerBrand.AMEX
                issuer.contains("capital one") -> IssuerBrand.CAPITAL_ONE
                issuer.contains("citi") -> IssuerBrand.CITI
                issuer.contains("discover") -> IssuerBrand.DISCOVER
                issuer.contains("wells fargo") -> IssuerBrand.WELLS_FARGO
                issuer.contains("bank of america") || issuer.contains("bofa") -> IssuerBrand.BOFA
                issuer.contains("us bank") -> IssuerBrand.US_BANK
                else -> IssuerBrand.GENERIC
            }
            CardDesignSpec(
                gradient = listOf(Color(card.colorHex), Color(card.accentHex).copy(alpha = 0.65f)),
                pattern = CardPattern.BRUSHED_METAL,
                issuerBrand = brand
            )
        }
    }
}

/**
 * Full-size ISO 7810 (1.586:1) physical-style credit card artwork with authentic
 * issuer logos, bespoke background textures, EMV chip, contactless icon, and Visa/MC/Amex/Discover marks.
 */
@Composable
fun RealisticCreditCardFace(
    card: CreditCard,
    badgeLabel: String,
    returnBadgeText: String,
    isPinned: Boolean,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val spec = resolveDesignSpec(card)
    val inkColor = if (spec.isLightSurface) Color(0xFF161922) else Color.White
    val subtleInk = inkColor.copy(alpha = if (spec.isLightSurface) 0.72f else 0.78f)
    val cardRadius = if (compact) 18.dp else 22.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(if (compact) 1.72f else 1.586f)
            .clip(RoundedCornerShape(cardRadius))
            .background(Brush.linearGradient(spec.gradient))
            .border(
                width = 1.dp,
                color = inkColor.copy(alpha = 0.16f),
                shape = RoundedCornerShape(cardRadius)
            )
    ) {
        // Bespoke vector background texture/pattern per card
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCardPattern(spec.pattern, inkColor)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (compact) 16.dp else 20.dp, vertical = if (compact) 14.dp else 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Authentic Issuer Logo + Return Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IssuerLogoMark(
                    brand = spec.issuerBrand,
                    fallbackText = card.issuer,
                    inkColor = inkColor,
                    compact = compact
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isPinned) {
                        Surface(
                            shape = CircleShape,
                            color = inkColor.copy(alpha = 0.16f)
                        ) {
                            Icon(
                                imageVector = AppIcons.PushPin,
                                contentDescription = "Pinned",
                                tint = inkColor,
                                modifier = Modifier
                                    .padding(5.dp)
                                    .size(12.dp)
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0F5223)
                    ) {
                        Text(
                            text = returnBadgeText,
                            color = Color(0xFFC4EED0),
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 12.sp else 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Middle Row: Realistic EMV Chip + Contactless Waves + Card Product Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EmvChipGraphic(isGold = spec.isGoldChip, compact = compact)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = AppIcons.Contactless,
                    contentDescription = "NFC",
                    tint = inkColor.copy(alpha = 0.72f),
                    modifier = Modifier.size(if (compact) 18.dp else 20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.walletNickname.ifBlank { spec.cardTitleOverride ?: card.name },
                        color = inkColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 14.sp else 16.sp,
                        letterSpacing = 0.6.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = badgeLabel,
                        color = subtleInk,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bottom Row: Masked card number + Authentic Network Logo (Visa / MC / Amex / Discover)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = if (card.lastFour.isNotBlank()) "••••  ••••  ••••  ${card.lastFour}" else card.name,
                    color = subtleInk,
                    fontFamily = if (card.lastFour.isNotBlank()) FontFamily.Monospace else FontFamily.Default,
                    fontSize = if (compact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                NetworkLogoMark(
                    network = card.network,
                    isLightCard = spec.isLightSurface,
                    compact = compact
                )
            }
        }
    }
}

/**
 * Compact 1.586:1 realistic mini card thumbnail used in Runner-Up lists, My Cards, and Card Catalog.
 */
@Composable
fun MiniCreditCardThumbnail(
    card: CreditCard,
    width: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val spec = resolveDesignSpec(card)
    val inkColor = if (spec.isLightSurface) Color(0xFF161922) else Color.White

    Box(
        modifier = modifier
            .width(width)
            .aspectRatio(1.586f)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(spec.gradient))
            .border(
                width = 0.75.dp,
                color = inkColor.copy(alpha = 0.22f),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCardPattern(spec.pattern, inkColor)
        }

        // Top-left mini issuer emblem
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 5.dp, top = 4.dp)
        ) {
            MiniIssuerGlyph(brand = spec.issuerBrand, inkColor = inkColor)
        }

        // Left-center mini chip
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 5.dp, top = 3.dp)
                .size(width = 9.dp, height = 6.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(if (spec.isGoldChip) Color(0xFFEAB308) else Color(0xFFCBD5E1))
        )

        // Bottom-right authentic mini network emblem
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 3.dp)
        ) {
            MiniNetworkGlyph(network = card.network, isLightCard = spec.isLightSurface)
        }
    }
}

@Composable
private fun EmvChipGraphic(isGold: Boolean, compact: Boolean) {
    val w = if (compact) 32.dp else 38.dp
    val h = if (compact) 23.dp else 27.dp
    val chipGradient = if (isGold) {
        listOf(Color(0xFFFDE68A), Color(0xFFD4A024), Color(0xFFF5D77F))
    } else {
        listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFFCBD5E1))
    }
    Box(
        modifier = Modifier
            .size(width = w, height = h)
            .clip(RoundedCornerShape(5.dp))
            .background(Brush.linearGradient(chipGradient))
            .border(0.75.dp, Color.Black.copy(alpha = 0.25f), RoundedCornerShape(5.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineColor = Color.Black.copy(alpha = 0.28f)
            val stroke = Stroke(width = 1.dp.toPx())
            drawLine(lineColor, Offset(size.width * 0.34f, 0f), Offset(size.width * 0.34f, size.height), stroke.width)
            drawLine(lineColor, Offset(size.width * 0.66f, 0f), Offset(size.width * 0.66f, size.height), stroke.width)
            drawLine(lineColor, Offset(0f, size.height * 0.35f), Offset(size.width * 0.34f, size.height * 0.35f), stroke.width)
            drawLine(lineColor, Offset(0f, size.height * 0.65f), Offset(size.width * 0.34f, size.height * 0.65f), stroke.width)
            drawLine(lineColor, Offset(size.width * 0.66f, size.height * 0.35f), Offset(size.width, size.height * 0.35f), stroke.width)
            drawLine(lineColor, Offset(size.width * 0.66f, size.height * 0.65f), Offset(size.width, size.height * 0.65f), stroke.width)
        }
    }
}

@Composable
private fun NetworkLogoMark(
    network: CardNetwork,
    isLightCard: Boolean,
    compact: Boolean
) {
    when (network) {
        CardNetwork.VISA -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Text(
                        text = "VISA",
                        color = if (isLightCard) Color(0xFF1A1F71) else Color.White,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        fontSize = if (compact) 18.sp else 21.sp,
                        letterSpacing = 0.5.sp
                    )
                    // Iconic gold corner accent slash on the V
                    Canvas(
                        modifier = Modifier
                            .size(width = 9.dp, height = 5.dp)
                            .align(Alignment.TopStart)
                    ) {
                        val p = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width * 0.65f, size.height)
                            lineTo(0f, size.height * 0.45f)
                            close()
                        }
                        drawPath(p, Color(0xFFF7B600))
                    }
                }
            }
        }

        CardNetwork.MASTERCARD -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val circleSize = if (compact) 22.dp else 26.dp
                Canvas(modifier = Modifier.size(width = circleSize * 1.62f, height = circleSize)) {
                    val r = size.height / 2f
                    val leftCenter = Offset(r, r)
                    val rightCenter = Offset(size.width - r, r)
                    drawCircle(color = Color(0xFFEB001B), radius = r, center = leftCenter)
                    drawCircle(color = Color(0xFFF79E1B), radius = r, center = rightCenter)
                    // Center overlap lens in signature Mastercard orange
                    drawOval(
                        color = Color(0xFFFF5F00),
                        topLeft = Offset(size.width / 2f - r * 0.38f, r * 0.16f),
                        size = Size(r * 0.76f, size.height - r * 0.32f)
                    )
                }
                if (!compact) {
                    Text(
                        text = "mastercard",
                        color = if (isLightCard) Color(0xFF1F2937) else Color.White.copy(alpha = 0.9f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }

        CardNetwork.AMEX -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF006FCF))
                    .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AMEX",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = if (compact) 10.sp else 11.sp,
                    letterSpacing = 0.6.sp
                )
            }
        }

        CardNetwork.DISCOVER -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val textColor = if (isLightCard) Color(0xFF111827) else Color.White
                Text(
                    text = "DISC",
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (compact) 12.sp else 13.sp,
                    letterSpacing = 0.6.sp
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 1.dp)
                        .size(if (compact) 11.dp else 12.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFDE047), Color(0xFFF97316))
                            )
                        )
                )
                Text(
                    text = "VER",
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (compact) 12.sp else 13.sp,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}

@Composable
private fun MiniNetworkGlyph(network: CardNetwork, isLightCard: Boolean) {
    when (network) {
        CardNetwork.VISA -> Text(
            text = "VISA",
            color = if (isLightCard) Color(0xFF1A1F71) else Color.White,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            fontSize = 7.5.sp
        )
        CardNetwork.MASTERCARD -> Canvas(modifier = Modifier.size(width = 16.dp, height = 10.dp)) {
            val r = size.height / 2f
            drawCircle(Color(0xFFEB001B), r, Offset(r, r))
            drawCircle(Color(0xFFF79E1B), r, Offset(size.width - r, r))
            drawOval(
                color = Color(0xFFFF5F00),
                topLeft = Offset(size.width / 2f - r * 0.36f, r * 0.16f),
                size = Size(r * 0.72f, size.height - r * 0.32f)
            )
        }
        CardNetwork.AMEX -> Box(
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF006FCF))
                .padding(horizontal = 3.dp, vertical = 0.5.dp)
        ) {
            Text("AMEX", color = Color.White, fontWeight = FontWeight.Black, fontSize = 6.sp)
        }
        CardNetwork.DISCOVER -> Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFFF97316))
        )
    }
}

@Composable
private fun IssuerLogoMark(
    brand: IssuerBrand,
    fallbackText: String,
    inkColor: Color,
    compact: Boolean
) {
    val textSize = if (compact) 13.sp else 15.sp
    when (brand) {
        IssuerBrand.CHASE -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "CHASE",
                color = inkColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = textSize,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(5.dp))
            ChaseOctagonLogo(color = Color(0xFF60A5FA), size = if (compact) 15.dp else 17.dp)
        }

        IssuerBrand.CAPITAL_ONE -> Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Capital One",
                    color = inkColor,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = textSize
                )
            }
            // Iconic Capital One curved red swoosh arc
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(width = 34.dp, height = 10.dp)
            ) {
                val p = Path().apply {
                    moveTo(0f, size.height * 0.75f)
                    quadraticTo(size.width * 0.65f, -size.height * 0.2f, size.width, size.height * 0.5f)
                }
                drawPath(p, color = Color(0xFFD03027), style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
            }
        }

        IssuerBrand.CITI -> Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Text(
                    text = "citi",
                    color = inkColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 17.sp else 19.sp,
                    letterSpacing = 0.3.sp
                )
                Canvas(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 1.dp)
                        .size(width = 14.dp, height = 6.dp)
                ) {
                    val p = Path().apply {
                        moveTo(0f, size.height)
                        quadraticTo(size.width * 0.5f, -size.height * 0.4f, size.width, size.height)
                    }
                    drawPath(p, color = Color(0xFFEE1818), style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
                }
            }
        }

        IssuerBrand.AMEX -> Text(
            text = "AMERICAN EXPRESS",
            color = inkColor,
            fontWeight = FontWeight.Black,
            fontSize = if (compact) 11.sp else 12.sp,
            letterSpacing = 1.4.sp
        )

        IssuerBrand.DISCOVER -> Text(
            text = "DISCOVER",
            color = inkColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = textSize,
            letterSpacing = 1.2.sp
        )

        IssuerBrand.WELLS_FARGO -> Column {
            Text(
                text = "WELLS FARGO",
                color = inkColor,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                fontSize = if (compact) 12.sp else 13.sp,
                letterSpacing = 0.8.sp
            )
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .width(42.dp)
                    .height(2.dp)
                    .background(Color(0xFFFACC15))
            )
        }

        IssuerBrand.BILT -> Text(
            text = "B I L T",
            color = inkColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (compact) 15.sp else 17.sp,
            letterSpacing = 2.5.sp
        )

        IssuerBrand.US_BANK -> Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFDC2626))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "U.S. BANK",
                color = inkColor,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 12.sp else 13.sp,
                letterSpacing = 0.8.sp
            )
        }

        IssuerBrand.BOFA -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Bank of America",
                color = inkColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (compact) 12.sp else 13.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Canvas(modifier = Modifier.size(width = 16.dp, height = 10.dp)) {
                drawLine(Color.White, Offset(0f, 2f), Offset(size.width * 0.7f, 0f), 2.dp.toPx())
                drawLine(Color(0xFF60A5FA), Offset(2f, size.height * 0.5f), Offset(size.width, size.height * 0.35f), 2.dp.toPx())
                drawLine(Color.White, Offset(4f, size.height), Offset(size.width, size.height * 0.8f), 2.dp.toPx())
            }
        }

        IssuerBrand.TARGET -> Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(16.dp)) {
                drawCircle(Color.White, size.minDimension / 2f)
                drawCircle(Color(0xFFCC0000), size.minDimension * 0.32f)
                drawCircle(Color.White, size.minDimension * 0.16f)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "TARGET",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = textSize,
                letterSpacing = 0.8.sp
            )
        }

        IssuerBrand.PRIME -> Column {
            Text(
                text = "prime",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (compact) 16.sp else 18.sp
            )
            Canvas(modifier = Modifier.size(width = 40.dp, height = 7.dp)) {
                val p = Path().apply {
                    moveTo(2f, 1f)
                    quadraticTo(size.width * 0.5f, size.height + 2f, size.width - 3f, 2f)
                }
                drawPath(p, color = Color(0xFF22D3EE), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }
        }

        IssuerBrand.FIDELITY -> Text(
            text = "Fidelity",
            color = inkColor,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            fontSize = if (compact) 15.sp else 17.sp
        )

        IssuerBrand.ROBINHOOD -> Text(
            text = "Robinhood",
            color = inkColor,
            fontWeight = FontWeight.Bold,
            fontSize = textSize
        )

        IssuerBrand.GENERIC -> Text(
            text = fallbackText.uppercase(),
            color = inkColor,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 11.sp else 12.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun MiniIssuerGlyph(brand: IssuerBrand, inkColor: Color) {
    when (brand) {
        IssuerBrand.CHASE -> ChaseOctagonLogo(color = Color(0xFF93C5FD), size = 9.dp)
        IssuerBrand.CAPITAL_ONE -> Canvas(modifier = Modifier.size(width = 14.dp, height = 6.dp)) {
            val p = Path().apply {
                moveTo(0f, size.height)
                quadraticTo(size.width * 0.6f, -2f, size.width, size.height * 0.6f)
            }
            drawPath(p, Color(0xFFD03027), style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
        }
        IssuerBrand.CITI -> Text("citi", color = inkColor, fontWeight = FontWeight.Bold, fontSize = 7.sp)
        IssuerBrand.AMEX -> Text("AMEX", color = inkColor, fontWeight = FontWeight.Black, fontSize = 6.sp)
        IssuerBrand.BILT -> Text("BILT", color = inkColor, fontWeight = FontWeight.Bold, fontSize = 6.5.sp, letterSpacing = 0.8.sp)
        IssuerBrand.TARGET -> Canvas(modifier = Modifier.size(9.dp)) {
            drawCircle(Color.White, size.minDimension / 2f)
            drawCircle(Color(0xFFCC0000), size.minDimension * 0.3f)
            drawCircle(Color.White, size.minDimension * 0.15f)
        }
        IssuerBrand.PRIME -> Text("prime", color = Color(0xFF22D3EE), fontWeight = FontWeight.Bold, fontSize = 6.5.sp)
        IssuerBrand.DISCOVER -> Text("it", color = Color.White, fontWeight = FontWeight.Black, fontSize = 7.5.sp)
        IssuerBrand.WELLS_FARGO -> Text("WF", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold, fontSize = 6.5.sp)
        IssuerBrand.ROBINHOOD -> Text("RH", color = inkColor, fontWeight = FontWeight.Black, fontSize = 6.5.sp)
        else -> Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(inkColor.copy(alpha = 0.55f))
        )
    }
}

@Composable
private fun ChaseOctagonLogo(color: Color, size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val pad = w * 0.14f
        val bar = w * 0.25f
        // Iconic 4-bar Chase octagon
        drawRoundRect(color, Offset(pad, 0f), Size(w * 0.58f, bar), CornerRadius(1.5f))
        drawRoundRect(color, Offset(w - bar, pad), Size(bar, h * 0.58f), CornerRadius(1.5f))
        drawRoundRect(color, Offset(w - pad - w * 0.58f, h - bar), Size(w * 0.58f, bar), CornerRadius(1.5f))
        drawRoundRect(color, Offset(0f, h - pad - h * 0.58f), Size(bar, h * 0.58f), CornerRadius(1.5f))
    }
}

private fun DrawScope.drawCardPattern(pattern: CardPattern, ink: Color) {
    when (pattern) {
        CardPattern.SAPPHIRE_FACETS -> {
            val p1 = Path().apply {
                moveTo(size.width * 0.45f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width * 0.72f, size.height)
                close()
            }
            val p2 = Path().apply {
                moveTo(size.width * 0.72f, 0f)
                lineTo(size.width, size.height * 0.65f)
                lineTo(size.width * 0.35f, size.height)
                close()
            }
            drawPath(p1, ink.copy(alpha = 0.06f))
            drawPath(p2, ink.copy(alpha = 0.05f))
        }

        CardPattern.AMEX_CLASSIC_FRAME -> {
            val inset = 9.dp.toPx()
            drawRoundRect(
                color = ink.copy(alpha = 0.18f),
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                cornerRadius = CornerRadius(12.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
            // Subtle center crest circle watermark
            drawCircle(
                color = ink.copy(alpha = 0.06f),
                radius = size.height * 0.36f,
                center = Offset(size.width * 0.52f, size.height * 0.52f),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        CardPattern.AMEX_PRISM -> {
            drawRect(
                color = Color.White.copy(alpha = 0.07f),
                topLeft = Offset(size.width * 0.52f, -size.height * 0.1f),
                size = Size(size.height * 0.7f, size.height * 0.7f)
            )
            drawRect(
                color = Color.White.copy(alpha = 0.05f),
                topLeft = Offset(size.width * 0.68f, size.height * 0.25f),
                size = Size(size.height * 0.65f, size.height * 0.65f)
            )
        }

        CardPattern.VENTURE_ARCS -> {
            val center = Offset(size.width * 0.85f, size.height * 0.2f)
            listOf(0.35f, 0.55f, 0.75f).forEach { factor ->
                drawCircle(
                    color = ink.copy(alpha = 0.06f),
                    radius = size.width * factor,
                    center = center,
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
        }

        CardPattern.SWOOP_WAVES -> {
            val wave = Path().apply {
                moveTo(0f, size.height * 0.78f)
                cubicTo(
                    size.width * 0.35f, size.height * 0.45f,
                    size.width * 0.65f, size.height * 1.05f,
                    size.width, size.height * 0.4f
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(wave, ink.copy(alpha = 0.07f))
        }

        CardPattern.TOPO_LINES -> {
            val stroke = Stroke(width = 1.dp.toPx())
            for (i in 1..4) {
                drawArc(
                    color = ink.copy(alpha = 0.07f),
                    startAngle = 140f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(size.width * (0.2f + i * 0.1f), -size.height * 0.3f),
                    size = Size(size.width * 0.8f, size.height * 1.4f),
                    style = stroke
                )
            }
        }

        CardPattern.TARGET_BULLSEYE -> {
            val c = Offset(size.width * 0.86f, size.height * 0.5f)
            drawCircle(Color.White.copy(alpha = 0.09f), size.height * 0.45f, c, style = Stroke(10.dp.toPx()))
            drawCircle(Color.White.copy(alpha = 0.09f), size.height * 0.16f, c)
        }

        CardPattern.BILT_MINIMAL -> {
            // Subtle anodized diagonal light catch
            val sheen = Path().apply {
                moveTo(size.width * 0.6f, 0f)
                lineTo(size.width * 0.85f, 0f)
                lineTo(size.width * 0.45f, size.height)
                lineTo(size.width * 0.2f, size.height)
                close()
            }
            drawPath(sheen, Color.White.copy(alpha = 0.035f))
        }

        CardPattern.BRUSHED_METAL -> {
            val sheen = Path().apply {
                moveTo(size.width * 0.15f, 0f)
                lineTo(size.width * 0.55f, 0f)
                lineTo(size.width * 0.25f, size.height)
                lineTo(-size.width * 0.15f, size.height)
                close()
            }
            drawPath(sheen, ink.copy(alpha = 0.06f))
        }
    }
}
