package com.creditauto.app.domain

object CardCatalogPresets {

    fun allPresets(): List<CreditCard> = listOf(
        // --- CHASE ---
        CreditCard(
            id = "chase_sapphire_preferred",
            name = "Chase Sapphire Preferred",
            issuer = "Chase",
            network = CardNetwork.VISA,
            colorHex = 0xFF0A2540,
            accentHex = 0xFF38BDF8,
            isOwned = true,
            rewardCurrency = RewardCurrency.CHASE_UR,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.DINING to 3.0,
                SpendCategory.TRAVEL to 2.0,
                SpendCategory.TRANSIT to 2.0,
                SpendCategory.STREAMING to 3.0
            ),
            hasForeignTransactionFee = false,
            annualFee = 95,
            notes = "3x dining & streaming, 2x travel, no FTF"
        ),
        CreditCard(
            id = "chase_sapphire_reserve",
            name = "Chase Sapphire Reserve",
            issuer = "Chase",
            network = CardNetwork.VISA,
            colorHex = 0xFF0B132B,
            accentHex = 0xFF60A5FA,
            rewardCurrency = RewardCurrency.CHASE_UR,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.DINING to 3.0,
                SpendCategory.TRAVEL to 3.0,
                SpendCategory.TRANSIT to 3.0
            ),
            hasForeignTransactionFee = false,
            annualFee = 550,
            notes = "3x dining & travel/transit, no FTF"
        ),
        CreditCard(
            id = "chase_freedom_unlimited",
            name = "Chase Freedom Unlimited",
            issuer = "Chase",
            network = CardNetwork.VISA,
            colorHex = 0xFF1D4ED8,
            accentHex = 0xFF93C5FD,
            isOwned = true,
            rewardCurrency = RewardCurrency.CHASE_UR,
            baseRate = 1.5,
            categoryRates = mapOf(
                SpendCategory.DINING to 3.0,
                SpendCategory.DRUGSTORE to 3.0
            ),
            hasForeignTransactionFee = true,
            annualFee = 0,
            notes = "3x dining & drugstores, 1.5x everything else"
        ),
        CreditCard(
            id = "chase_freedom_flex",
            name = "Chase Freedom Flex",
            issuer = "Chase",
            network = CardNetwork.MASTERCARD,
            colorHex = 0xFF1E3A8A,
            accentHex = 0xFF34D399,
            rewardCurrency = RewardCurrency.CHASE_UR,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.DINING to 3.0,
                SpendCategory.DRUGSTORE to 3.0,
                SpendCategory.GAS to 5.0
            ),
            hasForeignTransactionFee = true,
            annualFee = 0,
            notes = "Edit rotating 5x quarterly category (up to $1,500/qtr)"
        ),
        CreditCard(
            id = "amazon_prime_visa",
            name = "Prime Visa",
            issuer = "Chase",
            network = CardNetwork.VISA,
            colorHex = 0xFF111827,
            accentHex = 0xFF22D3EE,
            rewardCurrency = RewardCurrency.CASH_BACK,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.DINING to 2.0,
                SpendCategory.GAS to 2.0,
                SpendCategory.TRANSIT to 2.0
            ),
            merchantBonusRates = mapOf(
                "whole foods" to 5.0,
                "amazon" to 5.0
            ),
            hasForeignTransactionFee = false,
            annualFee = 0,
            notes = "5% at Whole Foods & Amazon, 2% dining/gas/transit"
        ),

        // --- AMERICAN EXPRESS ---
        CreditCard(
            id = "amex_gold",
            name = "Amex Gold Card",
            issuer = "American Express",
            network = CardNetwork.AMEX,
            colorHex = 0xFFB45309,
            accentHex = 0xFFFDE68A,
            isOwned = true,
            rewardCurrency = RewardCurrency.AMEX_MR,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.DINING to 4.0,
                SpendCategory.GROCERY to 4.0
            ),
            hasForeignTransactionFee = false,
            annualFee = 325,
            notes = "4x U.S. supermarkets & worldwide restaurants"
        ),
        CreditCard(
            id = "amex_platinum",
            name = "Amex Platinum Card",
            issuer = "American Express",
            network = CardNetwork.AMEX,
            colorHex = 0xFF475569,
            accentHex = 0xFFE2E8F0,
            rewardCurrency = RewardCurrency.AMEX_MR,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.TRAVEL to 5.0
            ),
            hasForeignTransactionFee = false,
            annualFee = 695,
            notes = "5x flights booked directly with airlines"
        ),
        CreditCard(
            id = "amex_blue_cash_preferred",
            name = "Blue Cash Preferred",
            issuer = "American Express",
            network = CardNetwork.AMEX,
            colorHex = 0xFF0369A1,
            accentHex = 0xFF7DD3FC,
            rewardCurrency = RewardCurrency.CASH_BACK,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.GROCERY to 6.0,
                SpendCategory.STREAMING to 6.0,
                SpendCategory.GAS to 3.0,
                SpendCategory.TRANSIT to 3.0
            ),
            hasForeignTransactionFee = true,
            annualFee = 95,
            notes = "6% U.S. supermarkets (up to $6k/yr) & streaming, 3% gas & transit"
        ),
        CreditCard(
            id = "amex_blue_cash_everyday",
            name = "Blue Cash Everyday",
            issuer = "American Express",
            network = CardNetwork.AMEX,
            colorHex = 0xFF0284C7,
            accentHex = 0xFFBAE6FD,
            rewardCurrency = RewardCurrency.CASH_BACK,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.GROCERY to 3.0,
                SpendCategory.GAS to 3.0,
                SpendCategory.ONLINE_SHOPPING to 3.0
            ),
            hasForeignTransactionFee = true,
            annualFee = 0,
            notes = "3% U.S. supermarkets, gas & U.S. online retail"
        ),

        // --- CAPITAL ONE ---
        CreditCard(
            id = "capital_one_venture_x",
            name = "Capital One Venture X",
            issuer = "Capital One",
            network = CardNetwork.VISA,
            colorHex = 0xFF0F172A,
            accentHex = 0xFF94A3B8,
            isOwned = true,
            rewardCurrency = RewardCurrency.CAPITAL_ONE_MILES,
            baseRate = 2.0,
            categoryRates = mapOf(
                SpendCategory.TRAVEL to 2.0
            ),
            hasForeignTransactionFee = false,
            annualFee = 395,
            notes = "2x flat on everything, Visa Infinite, great at Costco & abroad"
        ),
        CreditCard(
            id = "capital_one_savor",
            name = "Capital One Savor",
            issuer = "Capital One",
            network = CardNetwork.MASTERCARD,
            colorHex = 0xFF7C2D12,
            accentHex = 0xFFFDBA74,
            rewardCurrency = RewardCurrency.CASH_BACK,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.DINING to 3.0,
                SpendCategory.GROCERY to 3.0,
                SpendCategory.ENTERTAINMENT to 3.0,
                SpendCategory.STREAMING to 3.0
            ),
            hasForeignTransactionFee = false,
            annualFee = 0,
            notes = "3% dining, grocery, entertainment & streaming, no FTF"
        ),
        CreditCard(
            id = "capital_one_quicksilver",
            name = "Capital One Quicksilver",
            issuer = "Capital One",
            network = CardNetwork.MASTERCARD,
            colorHex = 0xFF334155,
            accentHex = 0xFFE2E8F0,
            rewardCurrency = RewardCurrency.CASH_BACK,
            baseRate = 1.5,
            hasForeignTransactionFee = false,
            annualFee = 0,
            notes = "Flat 1.5% cash back on every purchase, $0 annual fee, no FTF"
        ),
        CreditCard(
            id = "capital_one_venture",
            name = "Capital One Venture",
            issuer = "Capital One",
            network = CardNetwork.MASTERCARD,
            colorHex = 0xFF1E293B,
            accentHex = 0xFF38BDF8,
            rewardCurrency = RewardCurrency.CAPITAL_ONE_MILES,
            baseRate = 2.0,
            hasForeignTransactionFee = false,
            annualFee = 95,
            notes = "2x miles on every purchase, no FTF"
        ),

        // --- CITI ---
        CreditCard(
            id = "citi_custom_cash",
            name = "Citi Custom Cash",
            issuer = "Citi",
            network = CardNetwork.MASTERCARD,
            colorHex = 0xFF065F46,
            accentHex = 0xFF6EE7B7,
            rewardCurrency = RewardCurrency.CITI_TYP,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.GAS to 5.0
            ),
            hasForeignTransactionFee = true,
            annualFee = 0,
            notes = "5% on your chosen top category (up to $500/cycle) — customize category"
        ),
        CreditCard(
            id = "citi_double_cash",
            name = "Citi Double Cash",
            issuer = "Citi",
            network = CardNetwork.MASTERCARD,
            colorHex = 0xFF155E75,
            accentHex = 0xFF67E8F9,
            rewardCurrency = RewardCurrency.CASH_BACK,
            baseRate = 2.0,
            hasForeignTransactionFee = true,
            annualFee = 0,
            notes = "Flat 2% cash back everywhere"
        ),
        CreditCard(
            id = "citi_strata_premier",
            name = "Citi Strata Premier",
            issuer = "Citi",
            network = CardNetwork.MASTERCARD,
            colorHex = 0xFF1E1B4B,
            accentHex = 0xFFA5B4FC,
            rewardCurrency = RewardCurrency.CITI_TYP,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.DINING to 3.0,
                SpendCategory.GROCERY to 3.0,
                SpendCategory.GAS to 3.0,
                SpendCategory.TRAVEL to 3.0
            ),
            hasForeignTransactionFee = false,
            annualFee = 95,
            notes = "3x restaurants, supermarkets, gas/EV & travel"
        ),
        CreditCard(
            id = "costco_anywhere_visa",
            name = "Costco Anywhere Visa",
            issuer = "Citi",
            network = CardNetwork.VISA,
            colorHex = 0xFF334155,
            accentHex = 0xFFF87171,
            rewardCurrency = RewardCurrency.CASH_BACK,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.GAS to 4.0,
                SpendCategory.DINING to 3.0,
                SpendCategory.TRAVEL to 3.0,
                SpendCategory.WHOLESALE_CLUB to 2.0
            ),
            merchantBonusRates = mapOf(
                "costco" to 2.0,
                "costco gas" to 5.0
            ),
            hasForeignTransactionFee = false,
            annualFee = 0,
            notes = "5% Costco gas, 4% other gas/EV, 3% dining/travel, 2% Costco"
        ),

        // --- DISCOVER / WELLS FARGO / BILT / US BANK / BOA / RETAIL ---
        CreditCard(
            id = "discover_it",
            name = "Discover it Cash Back",
            issuer = "Discover",
            network = CardNetwork.DISCOVER,
            colorHex = 0xFFC2410C,
            accentHex = 0xFFFED7AA,
            rewardCurrency = RewardCurrency.CASH_BACK,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.GROCERY to 5.0
            ),
            hasForeignTransactionFee = false,
            annualFee = 0,
            notes = "Edit rotating 5% quarterly category (up to $1,500/qtr)"
        ),
        CreditCard(
            id = "wells_fargo_autograph",
            name = "Wells Fargo Autograph",
            issuer = "Wells Fargo",
            network = CardNetwork.VISA,
            colorHex = 0xFF991B1B,
            accentHex = 0xFFFCA5A5,
            rewardCurrency = RewardCurrency.WELLS_FARGO_POINTS,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.DINING to 3.0,
                SpendCategory.TRAVEL to 3.0,
                SpendCategory.GAS to 3.0,
                SpendCategory.TRANSIT to 3.0,
                SpendCategory.STREAMING to 3.0,
                SpendCategory.UTILITIES to 3.0
            ),
            hasForeignTransactionFee = false,
            annualFee = 0,
            notes = "3x dining, travel, gas, transit, streaming & phone plans"
        ),
        CreditCard(
            id = "wells_fargo_active_cash",
            name = "Wells Fargo Active Cash",
            issuer = "Wells Fargo",
            network = CardNetwork.VISA,
            colorHex = 0xFF7F1D1D,
            accentHex = 0xFFFDE047,
            rewardCurrency = RewardCurrency.CASH_BACK,
            baseRate = 2.0,
            hasForeignTransactionFee = true,
            annualFee = 0,
            notes = "Flat 2% cash back Visa (accepted at Costco)"
        ),
        CreditCard(
            id = "bilt_mastercard",
            name = "Bilt Mastercard",
            issuer = "Wells Fargo",
            network = CardNetwork.MASTERCARD,
            colorHex = 0xFF18181B,
            accentHex = 0xFFD4D4D8,
            rewardCurrency = RewardCurrency.BILT_POINTS,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.DINING to 3.0,
                SpendCategory.TRAVEL to 2.0
            ),
            hasForeignTransactionFee = false,
            annualFee = 0,
            notes = "3x dining, 2x travel, 1x rent (2x on Rent Day)"
        ),
        CreditCard(
            id = "us_bank_altitude_reserve",
            name = "US Bank Altitude Reserve",
            issuer = "US Bank",
            network = CardNetwork.VISA,
            colorHex = 0xFF172554,
            accentHex = 0xFFC084FC,
            rewardCurrency = RewardCurrency.US_BANK_POINTS,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.TRAVEL to 3.0
            ),
            mobileWalletRate = 3.0,
            hasForeignTransactionFee = false,
            annualFee = 400,
            notes = "3x on ALL Google Pay / mobile wallet tap-to-pay purchases!"
        ),
        CreditCard(
            id = "boa_customized_cash",
            name = "BofA Customized Cash",
            issuer = "Bank of America",
            network = CardNetwork.VISA,
            colorHex = 0xFF9F1239,
            accentHex = 0xFFFDA4AF,
            rewardCurrency = RewardCurrency.CASH_BACK,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.ONLINE_SHOPPING to 3.0,
                SpendCategory.GROCERY to 2.0,
                SpendCategory.WHOLESALE_CLUB to 2.0
            ),
            hasForeignTransactionFee = true,
            annualFee = 0,
            notes = "3% choice category, 2% grocery & wholesale clubs (edit for Preferred Rewards)"
        ),
        CreditCard(
            id = "target_circle_card",
            name = "Target Circle Card",
            issuer = "TD Bank",
            network = CardNetwork.MASTERCARD,
            colorHex = 0xFFDC2626,
            accentHex = 0xFFFECACA,
            rewardCurrency = RewardCurrency.CASH_BACK,
            baseRate = 1.0,
            categoryRates = mapOf(
                SpendCategory.DINING to 2.0,
                SpendCategory.GAS to 2.0
            ),
            merchantBonusRates = mapOf(
                "target" to 5.0
            ),
            hasForeignTransactionFee = false,
            annualFee = 0,
            notes = "5% instant discount at Target"
        ),
        CreditCard(
            id = "fidelity_rewards_visa",
            name = "Fidelity Rewards Visa",
            issuer = "Elan / Fidelity",
            network = CardNetwork.VISA,
            colorHex = 0xFF14532D,
            accentHex = 0xFF86EFAC,
            rewardCurrency = RewardCurrency.CASH_BACK,
            baseRate = 2.0,
            hasForeignTransactionFee = false,
            annualFee = 0,
            notes = "Flat 2% cash back Visa, no FTF (works at Costco)"
        ),
        CreditCard(
            id = "amex_blue_business_plus",
            name = "Amex Blue Business Plus",
            issuer = "American Express",
            network = CardNetwork.AMEX,
            colorHex = 0xFF1D4ED8,
            accentHex = 0xFFBAE6FD,
            rewardCurrency = RewardCurrency.AMEX_MR,
            baseRate = 2.0,
            hasForeignTransactionFee = true,
            annualFee = 0,
            notes = "2x Amex MR on everything up to $50k/yr, $0 annual fee"
        ),
        CreditCard(
            id = "robinhood_gold_card",
            name = "Robinhood Gold Card",
            issuer = "Coastal Community Bank",
            network = CardNetwork.VISA,
            colorHex = 0xFF854D0E,
            accentHex = 0xFFFEF08A,
            rewardCurrency = RewardCurrency.CASH_BACK,
            baseRate = 3.0,
            hasForeignTransactionFee = false,
            annualFee = 0,
            notes = "Flat 3% cash back Visa on everything, no FTF"
        )
    )
}
