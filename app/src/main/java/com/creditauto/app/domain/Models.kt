package com.creditauto.app.domain

import kotlinx.serialization.Serializable

@Serializable
enum class SpendCategory(val displayName: String, val shortLabel: String) {
    GROCERY("Groceries", "Grocery"),
    DINING("Dining & Cafes", "Dining"),
    GAS("Gas & EV Charging", "Gas"),
    TRAVEL("Travel & Hotels", "Travel"),
    TRANSIT("Transit & Rideshare", "Transit"),
    DRUGSTORE("Drugstores & Pharmacy", "Pharmacy"),
    WHOLESALE_CLUB("Wholesale Clubs", "Wholesale"),
    DEPARTMENT_STORE("Superstores & Retail", "Retail"),
    ONLINE_SHOPPING("Online Shopping", "Online"),
    HOME_IMPROVEMENT("Home Improvement", "Home"),
    ENTERTAINMENT("Entertainment", "Fun"),
    STREAMING("Streaming", "Streaming"),
    UTILITIES("Utilities & Telecom", "Utilities"),
    OTHER("Everything Else", "Other")
}

@Serializable
enum class CardNetwork(val displayName: String, val shortBadge: String) {
    VISA("Visa", "VISA"),
    MASTERCARD("Mastercard", "MC"),
    AMEX("American Express", "AMEX"),
    DISCOVER("Discover", "DISC")
}

@Serializable
enum class RewardCurrency(
    val displayName: String,
    val unitLabel: String,
    val defaultCentsPerPoint: Double
) {
    CASH_BACK("Cash Back", "%", 1.0),
    CHASE_UR("Chase Ultimate Rewards", "x", 1.5),
    AMEX_MR("Amex Membership Rewards", "x", 1.5),
    CAPITAL_ONE_MILES("Capital One Miles", "x", 1.3),
    CITI_TYP("Citi ThankYou Points", "x", 1.3),
    BILT_POINTS("Bilt Points", "x", 1.7),
    WELLS_FARGO_POINTS("Wells Fargo Rewards", "x", 1.2),
    US_BANK_POINTS("US Bank Points", "x", 1.5),
    CUSTOM_POINTS("Custom Points", "x", 1.0)
}

@Serializable
data class CreditCard(
    val id: String,
    val name: String,
    val issuer: String,
    val network: CardNetwork,
    val lastFour: String = "",
    val walletNickname: String = "",
    val colorHex: Long = 0xFF1E293B,
    val accentHex: Long = 0xFF38BDF8,
    val isOwned: Boolean = false,
    val isActive: Boolean = true,
    val rewardCurrency: RewardCurrency = RewardCurrency.CASH_BACK,
    val customPointValueCents: Double? = null,
    val baseRate: Double = 1.0,
    val categoryRates: Map<SpendCategory, Double> = emptyMap(),
    val merchantBonusRates: Map<String, Double> = emptyMap(),
    val mobileWalletRate: Double? = null,
    val hasForeignTransactionFee: Boolean = false,
    val annualFee: Int = 0,
    val notes: String = ""
) {
    fun displayLabel(): String = buildString {
        append(walletNickname.ifBlank { name })
        if (lastFour.isNotBlank()) {
            append(" •••• ")
            append(lastFour)
        }
    }
}

@Serializable
data class SavedPlace(
    val id: String,
    val name: String,
    val category: SpendCategory,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Double = 150.0,
    val acceptedNetworks: Set<CardNetwork> = CardNetwork.entries.toSet(),
    val preferredCardId: String? = null,
    val address: String = ""
)

@Serializable
enum class StoreSource(val label: String) {
    SAVED_PLACE("Saved Place"),
    GOOGLE_PLACES("Google Places"),
    OPEN_STREET_MAP("OpenStreetMap"),
    MANUAL("Manual")
}

@Serializable
data class DetectedStore(
    val name: String,
    val category: SpendCategory,
    val source: StoreSource,
    val distanceMeters: Double? = null,
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val acceptedNetworks: Set<CardNetwork> = CardNetwork.entries.toSet(),
    val savedPlaceId: String? = null,
    val preferredCardId: String? = null
)

@Serializable
data class CachedStoreSnapshot(
    val latitude: Double,
    val longitude: Double,
    val timestampMs: Long,
    val stores: List<DetectedStore>
)

data class CheckoutContext(
    val store: DetectedStore,
    val isMobileWallet: Boolean = true,
    val isForeignTransaction: Boolean = false,
    val allowAmex: Boolean = true,
    val visaOnlyOverride: Boolean = false
)

data class CardRecommendation(
    val card: CreditCard,
    val effectiveReturnPercent: Double,
    val rawMultiplier: Double,
    val appliedPointValueCents: Double,
    val matchedSource: String,
    val reason: String,
    val warning: String? = null,
    val isPinnedOverride: Boolean = false,
    val deltaBelowBestPercent: Double = 0.0
)

@Serializable
data class AppSettings(
    val pointValuations: Map<RewardCurrency, Double> = RewardCurrency.entries.associateWith { it.defaultCentsPerPoint },
    val googlePlacesApiKey: String = "",
    val searchRadiusMeters: Int = 150,
    val showCheckoutNotification: Boolean = true,
    val defaultUseMobileWallet: Boolean = true,
    val backgroundPrefetchEnabled: Boolean = false,
    val autoCheckUpdates: Boolean = true,
    val updateRepoSlug: String = "prasanthgajula08/credit-auto",
    val hasCustomizedWallet: Boolean = false,
    val lastTopCardSummary: String = "",
    val lastDetectedStoreName: String = ""
)
