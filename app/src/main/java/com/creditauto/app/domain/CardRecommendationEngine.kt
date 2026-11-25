package com.creditauto.app.domain

import java.util.Locale
import kotlin.math.max

object CardRecommendationEngine {

    private const val FOREIGN_TRANSACTION_FEE_PENALTY = 3.0

    fun recommend(
        cards: List<CreditCard>,
        context: CheckoutContext,
        pointValuations: Map<RewardCurrency, Double> = RewardCurrency.entries.associateWith { it.defaultCentsPerPoint }
    ): List<CardRecommendation> {
        val effectiveAcceptedNetworks = buildSet {
            addAll(context.store.acceptedNetworks)
            if (context.visaOnlyOverride) {
                retainAll(setOf(CardNetwork.VISA))
            }
            if (!context.allowAmex) {
                remove(CardNetwork.AMEX)
            }
        }

        val eligibleCards = cards.filter { card ->
            card.isOwned && card.isActive && card.network in effectiveAcceptedNetworks
        }

        if (eligibleCards.isEmpty()) return emptyList()

        val rawRanked = eligibleCards.map { card ->
            evaluateCard(card, context, pointValuations)
        }.sortedWith(
            compareByDescending<CardRecommendation> { it.isPinnedOverride }
                .thenByDescending { it.effectiveReturnPercent }
                .thenByDescending { it.rawMultiplier }
                .thenBy { it.card.annualFee }
                .thenBy { it.card.name }
        )

        val bestReturn = rawRanked.maxOf { it.effectiveReturnPercent }

        return rawRanked.map { rec ->
            rec.copy(
                deltaBelowBestPercent = max(0.0, bestReturn - rec.effectiveReturnPercent)
            )
        }
    }

    private fun evaluateCard(
        card: CreditCard,
        context: CheckoutContext,
        pointValuations: Map<RewardCurrency, Double>
    ): CardRecommendation {
        val storeNameLower = context.store.name.lowercase()
        val isPinned = context.store.preferredCardId != null && context.store.preferredCardId == card.id

        // Determine highest applicable multiplier among merchant bonus, category rate, mobile wallet rate, and base rate
        val merchantMatch = card.merchantBonusRates.entries
            .filter { (keyword, _) -> keyword.isNotBlank() && storeNameLower.contains(keyword.lowercase()) }
            .maxByOrNull { it.value }

        val categoryMultiplier = card.categoryRates[context.store.category]
        val mobileWalletMultiplier = if (context.isMobileWallet) card.mobileWalletRate else null

        data class CandidateRate(val multiplier: Double, val source: String)

        val candidates = buildList {
            add(CandidateRate(card.baseRate, "Base rate"))
            if (categoryMultiplier != null) {
                add(CandidateRate(categoryMultiplier, context.store.category.displayName))
            }
            if (mobileWalletMultiplier != null) {
                add(CandidateRate(mobileWalletMultiplier, "Google Pay tap"))
            }
            if (merchantMatch != null) {
                add(CandidateRate(merchantMatch.value, "${merchantMatch.key.replaceFirstChar { it.uppercase() }} bonus"))
            }
        }

        val bestCandidate = candidates.maxBy { it.multiplier }

        val cpp = card.customPointValueCents
            ?: pointValuations[card.rewardCurrency]
            ?: card.rewardCurrency.defaultCentsPerPoint

        val grossReturn = bestCandidate.multiplier * cpp
        val ftfPenalty = if (context.isForeignTransaction && card.hasForeignTransactionFee) {
            FOREIGN_TRANSACTION_FEE_PENALTY
        } else {
            0.0
        }
        val netReturn = grossReturn - ftfPenalty

        val warning = if (ftfPenalty > 0.0) {
            "Charges ~3% foreign transaction fee"
        } else {
            null
        }

        val multiplierStr = formatMultiplier(bestCandidate.multiplier, card.rewardCurrency)
        val reason = buildString {
            if (isPinned) append("Pinned for this store • ")
            append(multiplierStr)
            append(" on ")
            append(bestCandidate.source)
            if (card.rewardCurrency != RewardCurrency.CASH_BACK) {
                append(" @ ")
                append(String.format(Locale.US, "%.1f¢", cpp))
            }
        }

        return CardRecommendation(
            card = card,
            effectiveReturnPercent = netReturn,
            rawMultiplier = bestCandidate.multiplier,
            appliedPointValueCents = cpp,
            matchedSource = bestCandidate.source,
            reason = reason,
            warning = warning,
            isPinnedOverride = isPinned
        )
    }

    private fun formatMultiplier(multiplier: Double, currency: RewardCurrency): String {
        val formatted = if (multiplier % 1.0 == 0.0) {
            multiplier.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", multiplier)
        }
        return "$formatted${currency.unitLabel}"
    }
}
