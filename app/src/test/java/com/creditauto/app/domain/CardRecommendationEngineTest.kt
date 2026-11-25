package com.creditauto.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardRecommendationEngineTest {

    private fun preset(id: String, owned: Boolean = true): CreditCard =
        CardCatalogPresets.allPresets().first { it.id == id }.copy(isOwned = owned, isActive = true)

    @Test
    fun groceryStorePicksHighestEffectiveReturn() {
        val cards = listOf(
            preset("amex_gold"),             // 4x MR @ 1.5c = 6.0%
            preset("capital_one_venture_x"), // 2x Cap1 @ 1.3c = 2.6%
            preset("chase_freedom_unlimited")// 1.5x UR @ 1.5c = 2.25%
        )
        val store = DetectedStore(
            name = "Trader Joe's",
            category = SpendCategory.GROCERY,
            source = StoreSource.OPEN_STREET_MAP
        )

        val recs = CardRecommendationEngine.recommend(cards, CheckoutContext(store))
        assertEquals("amex_gold", recs.first().card.id)
        assertEquals(6.0, recs.first().effectiveReturnPercent, 0.01)
        assertEquals(0.0, recs.first().deltaBelowBestPercent, 0.01)
        assertTrue(recs[1].deltaBelowBestPercent > 3.0)
    }

    @Test
    fun costcoEnforcesVisaOnlyRestriction() {
        val cards = listOf(
            preset("amex_gold"),
            preset("citi_double_cash"),      // Mastercard 2%
            preset("capital_one_venture_x")  // Visa 2x @ 1.3c = 2.6%
        )
        val classified = MerchantCategoryClassifier.classify("Costco Wholesale")
        val store = DetectedStore(
            name = "Costco Wholesale",
            category = classified.category,
            source = StoreSource.MANUAL,
            acceptedNetworks = classified.acceptedNetworks
        )

        val recs = CardRecommendationEngine.recommend(cards, CheckoutContext(store))
        assertEquals(1, recs.size)
        assertEquals("capital_one_venture_x", recs.first().card.id)
    }

    @Test
    fun targetDoesNotTriggerSupermarketMultiplierAndUsesMerchantBonus() {
        val cards = listOf(
            preset("amex_gold"),             // 1x at Target (Department Store) = 1.5%
            preset("capital_one_venture_x"), // 2x = 2.6%
            preset("target_circle_card")     // 5% merchant bonus at Target
        )
        val classified = MerchantCategoryClassifier.classify("Target")
        assertEquals(SpendCategory.DEPARTMENT_STORE, classified.category)

        val store = DetectedStore(
            name = "Target",
            category = classified.category,
            source = StoreSource.OPEN_STREET_MAP
        )

        val recs = CardRecommendationEngine.recommend(cards, CheckoutContext(store))
        assertEquals("target_circle_card", recs.first().card.id)
        assertEquals(5.0, recs.first().effectiveReturnPercent, 0.01)
    }

    @Test
    fun googlePayMobileWalletBonusAppliesOnlyWhenMobileWalletEnabled() {
        val cards = listOf(
            preset("us_bank_altitude_reserve"), // 3x mobile wallet @ 1.5c = 4.5%, 1x base = 1.5%
            preset("citi_double_cash")          // 2% base
        )
        val store = DetectedStore(
            name = "Local Hardware",
            category = SpendCategory.HOME_IMPROVEMENT,
            source = StoreSource.MANUAL
        )

        val mobileRecs = CardRecommendationEngine.recommend(
            cards,
            CheckoutContext(store, isMobileWallet = true)
        )
        assertEquals("us_bank_altitude_reserve", mobileRecs.first().card.id)
        assertEquals(4.5, mobileRecs.first().effectiveReturnPercent, 0.01)

        val physicalRecs = CardRecommendationEngine.recommend(
            cards,
            CheckoutContext(store, isMobileWallet = false)
        )
        assertEquals("citi_double_cash", physicalRecs.first().card.id)
        assertEquals(2.0, physicalRecs.first().effectiveReturnPercent, 0.01)
    }

    @Test
    fun foreignTransactionPenalizesCardsWithForeignFee() {
        val cards = listOf(
            preset("chase_freedom_unlimited"), // 3x dining @ 1.5c = 4.5% - 3% FTF = 1.5%
            preset("chase_sapphire_preferred") // 3x dining @ 1.5c = 4.5%, no FTF
        )
        val store = DetectedStore(
            name = "Bistro Paris",
            category = SpendCategory.DINING,
            source = StoreSource.MANUAL
        )

        val recs = CardRecommendationEngine.recommend(
            cards,
            CheckoutContext(store, isForeignTransaction = true)
        )
        assertEquals("chase_sapphire_preferred", recs.first().card.id)
        assertEquals(4.5, recs.first().effectiveReturnPercent, 0.01)
        assertNotNull(recs.last().warning)
        assertEquals(1.5, recs.last().effectiveReturnPercent, 0.01)
    }

    @Test
    fun allowAmexFalseExcludesAmexCards() {
        val cards = listOf(
            preset("amex_gold"),
            preset("chase_sapphire_preferred")
        )
        val store = DetectedStore(
            name = "Neighborhood Diner",
            category = SpendCategory.DINING,
            source = StoreSource.MANUAL
        )

        val recs = CardRecommendationEngine.recommend(
            cards,
            CheckoutContext(store, allowAmex = false)
        )
        assertEquals(1, recs.size)
        assertFalse(recs.any { it.card.network == CardNetwork.AMEX })
        assertEquals("chase_sapphire_preferred", recs.first().card.id)
    }

    @Test
    fun pinnedSavedPlaceCardOverridesScoreOrder() {
        val cards = listOf(
            preset("amex_gold"),
            preset("chase_freedom_unlimited")
        )
        val store = DetectedStore(
            name = "My Favorite Cafe",
            category = SpendCategory.DINING,
            source = StoreSource.SAVED_PLACE,
            preferredCardId = "chase_freedom_unlimited"
        )

        val recs = CardRecommendationEngine.recommend(cards, CheckoutContext(store))
        assertEquals("chase_freedom_unlimited", recs.first().card.id)
        assertTrue(recs.first().isPinnedOverride)
    }

    @Test
    fun unownedAndInactiveCardsAreExcluded() {
        val cards = listOf(
            preset("amex_gold", owned = false),
            preset("chase_sapphire_preferred", owned = true).copy(isActive = false),
            preset("capital_one_venture_x", owned = true)
        )
        val store = DetectedStore(
            name = "Chipotle",
            category = SpendCategory.DINING,
            source = StoreSource.MANUAL
        )

        val recs = CardRecommendationEngine.recommend(cards, CheckoutContext(store))
        assertEquals(1, recs.size)
        assertEquals("capital_one_venture_x", recs.first().card.id)
    }
}
