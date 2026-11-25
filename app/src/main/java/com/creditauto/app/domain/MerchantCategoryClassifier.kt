package com.creditauto.app.domain

object MerchantCategoryClassifier {

    data class ClassificationResult(
        val category: SpendCategory,
        val acceptedNetworks: Set<CardNetwork> = CardNetwork.entries.toSet()
    )

    private val allNetworks = CardNetwork.entries.toSet()
    private val visaOnly = setOf(CardNetwork.VISA)

    /**
     * Classifies a merchant by brand name first (to handle issuer quirks like
     * Costco being Visa-only or Target/Walmart not coding as Grocery), then falls
     * back to OpenStreetMap tags or Google Places types.
     */
    fun classify(
        merchantName: String,
        osmTags: Map<String, String> = emptyMap(),
        googlePlaceTypes: List<String> = emptyList()
    ): ClassificationResult {
        val lower = merchantName.lowercase()

        // 1. High-priority merchant quirks & well-known chains
        when {
            lower.contains("costco gas") ->
                return ClassificationResult(SpendCategory.GAS, visaOnly)
            lower.contains("costco") ->
                return ClassificationResult(SpendCategory.WHOLESALE_CLUB, visaOnly)
            lower.contains("sam's club") || lower.contains("sams club") || lower.contains("bj's wholesale") ->
                return ClassificationResult(SpendCategory.WHOLESALE_CLUB, allNetworks)
            // Target & Walmart are explicitly excluded from Grocery bonuses by most issuers
            lower.contains("target") || lower.contains("walmart") || lower.contains("meijer") ->
                return ClassificationResult(SpendCategory.DEPARTMENT_STORE, allNetworks)
            lower.containsAny(
                "whole foods", "trader joe", "kroger", "h-e-b", "heb ", "publix",
                "safeway", "aldi", "lidl", "sprouts", "wegmans", "albertsons",
                "vons", "ralphs", "giant eagle", "stop & shop", "food lion",
                "h mart", "99 ranch", "patel brothers", "india bazaar", "fresh market",
                "hy-vee", "winco", "smith's", "fred meyer", "harris teeter"
            ) -> return ClassificationResult(SpendCategory.GROCERY, allNetworks)
            lower.containsAny("cvs", "walgreens", "rite aid", "duane reade") ->
                return ClassificationResult(SpendCategory.DRUGSTORE, allNetworks)
            lower.containsAny(
                "shell", "chevron", "exxon", "mobil", "bp ", "sunoco", "texaco",
                "valero", "citgo", "marathon", "quiktrip", "racetrac", "buc-ee",
                "wawa", "sheetz", "7-eleven", "circle k", "love's", "pilot travel",
                "sinclair", "arco", "phillips 66", "conoco", "speedway", "supercharger",
                "electrify america", "evgo", "chargepoint"
            ) -> return ClassificationResult(SpendCategory.GAS, allNetworks)
            lower.containsAny("home depot", "lowe's", "lowes", "ace hardware", "menards", "harbor freight") ->
                return ClassificationResult(SpendCategory.HOME_IMPROVEMENT, allNetworks)
            lower.containsAny(
                "starbucks", "dunkin", "chipotle", "chick-fil-a", "mcdonald",
                "panera", "subway", "wendy's", "taco bell", "burger king",
                "sweetgreen", "cava", "shake shack", "in-n-out", "five guys",
                "olive garden", "cheesecake factory", "panda express", "popeyes",
                "domino's", "pizza hut", "papa john", "jersey mike", "jimmy john",
                "dutch bros", "peet's coffee", "blue bottle"
            ) -> return ClassificationResult(SpendCategory.DINING, allNetworks)
            lower.containsAny("amc ", "regal ", "cinemark", "alamo drafthouse", "topgolf", "bowling", "live nation", "ticketmaster") ->
                return ClassificationResult(SpendCategory.ENTERTAINMENT, allNetworks)
            lower.containsAny("marriott", "hilton", "hyatt", "ihg", "sheraton", "westin", "holiday inn", "courtyard", "hampton inn", "airbnb", "delta air", "united air", "american air", "southwest air") ->
                return ClassificationResult(SpendCategory.TRAVEL, allNetworks)
            lower.containsAny("uber", "lyft", "metro", "subway station", "parking", "garage", "bart", "mta ", "amtrak") ->
                return ClassificationResult(SpendCategory.TRANSIT, allNetworks)
            lower.containsAny("amazon") ->
                return ClassificationResult(SpendCategory.ONLINE_SHOPPING, allNetworks)
            lower.containsAny("verizon", "at&t", "t-mobile", "comcast", "xfinity", "spectrum") ->
                return ClassificationResult(SpendCategory.UTILITIES, allNetworks)
        }

        // 2. OpenStreetMap tags
        if (osmTags.isNotEmpty()) {
            classifyOsmTags(osmTags)?.let { return ClassificationResult(it, allNetworks) }
        }

        // 3. Google Places types
        if (googlePlaceTypes.isNotEmpty()) {
            classifyGoogleTypes(googlePlaceTypes)?.let { return ClassificationResult(it, allNetworks) }
        }

        // 4. Generic name keyword heuristics
        return ClassificationResult(classifyByKeywords(lower), allNetworks)
    }

    private fun classifyOsmTags(tags: Map<String, String>): SpendCategory? {
        val shop = tags["shop"]?.lowercase()
        val amenity = tags["amenity"]?.lowercase()
        val tourism = tags["tourism"]?.lowercase()
        val leisure = tags["leisure"]?.lowercase()

        return when {
            amenity in setOf("restaurant", "cafe", "fast_food", "bar", "pub", "food_court", "ice_cream", "biergarten") ->
                SpendCategory.DINING
            amenity in setOf("fuel", "charging_station") ->
                SpendCategory.GAS
            amenity in setOf("pharmacy") ->
                SpendCategory.DRUGSTORE
            amenity in setOf("cinema", "theatre", "nightclub", "casino", "arts_centre") ->
                SpendCategory.ENTERTAINMENT
            amenity in setOf("parking", "bus_station", "ferry_terminal", "taxi") ->
                SpendCategory.TRANSIT
            shop in setOf("supermarket", "greengrocer", "grocery", "butcher", "bakery", "deli", "seafood", "organic", "health_food", "farm") ->
                SpendCategory.GROCERY
            shop in setOf("wholesale") ->
                SpendCategory.WHOLESALE_CLUB
            shop in setOf("department_store", "mall", "general", "variety_store", "clothes", "shoes", "electronics") ->
                SpendCategory.DEPARTMENT_STORE
            shop in setOf("chemist", "medical_supply") ->
                SpendCategory.DRUGSTORE
            shop in setOf("doityourself", "hardware", "paint", "garden_centre", "trade", "building_materials") ->
                SpendCategory.HOME_IMPROVEMENT
            tourism in setOf("hotel", "motel", "resort", "hostel", "guest_house", "apartment") ->
                SpendCategory.TRAVEL
            tourism in setOf("theme_park", "museum", "zoo", "aquarium", "attraction") ||
                leisure in setOf("bowling_alley", "amusement_arcade", "water_park", "stadium", "escape_game") ->
                SpendCategory.ENTERTAINMENT
            else -> null
        }
    }

    private fun classifyGoogleTypes(types: List<String>): SpendCategory? {
        val set = types.map { it.lowercase() }.toSet()
        return when {
            set.any { it in setOf("supermarket", "grocery_store", "asian_grocery_store", "food_store", "butcher_shop", "market") } ->
                SpendCategory.GROCERY
            set.any { it in setOf("restaurant", "cafe", "coffee_shop", "bakery", "bar", "fast_food_restaurant", "meal_takeaway", "ice_cream_shop", "pizza_restaurant", "sandwich_shop") } ->
                SpendCategory.DINING
            set.any { it in setOf("gas_station", "electric_vehicle_charging_station") } ->
                SpendCategory.GAS
            set.any { it in setOf("pharmacy", "drugstore") } ->
                SpendCategory.DRUGSTORE
            set.any { it in setOf("warehouse_store", "wholesaler") } ->
                SpendCategory.WHOLESALE_CLUB
            set.any { it in setOf("hardware_store", "home_improvement_store") } ->
                SpendCategory.HOME_IMPROVEMENT
            set.any { it in setOf("hotel", "motel", "resort_hotel", "lodging", "extended_stay_hotel", "airport") } ->
                SpendCategory.TRAVEL
            set.any { it in setOf("transit_station", "subway_station", "train_station", "bus_station", "parking") } ->
                SpendCategory.TRANSIT
            set.any { it in setOf("movie_theater", "amusement_park", "bowling_alley", "concert_hall", "stadium", "performing_arts_theater", "night_club") } ->
                SpendCategory.ENTERTAINMENT
            set.any { it in setOf("department_store", "shopping_mall", "discount_store", "clothing_store", "electronics_store") } ->
                SpendCategory.DEPARTMENT_STORE
            else -> null
        }
    }

    private fun classifyByKeywords(lower: String): SpendCategory = when {
        lower.containsAny("grocery", "market", "supermarket", "foods", "bazaar", "produce", "mart") -> SpendCategory.GROCERY
        lower.containsAny("coffee", "cafe", "grill", "pizza", "sushi", "taco", "burger", "kitchen", "bistro", "bakery", "diner", "bar", "bbq", "thai", "indian", "ramen", "noodle", "tea", "steakhouse", "deli", "restaurant") -> SpendCategory.DINING
        lower.containsAny("gas", "fuel", "petrol", "charging", "station") -> SpendCategory.GAS
        lower.containsAny("pharmacy", "drug", "rx") -> SpendCategory.DRUGSTORE
        lower.containsAny("wholesale", "club") -> SpendCategory.WHOLESALE_CLUB
        lower.containsAny("hardware", "lumber", "home center", "nursery") -> SpendCategory.HOME_IMPROVEMENT
        lower.containsAny("hotel", "inn", "resort", "suites", "airline", "airways") -> SpendCategory.TRAVEL
        lower.containsAny("cinema", "theater", "theatre", "arcade", "museum") -> SpendCategory.ENTERTAINMENT
        else -> SpendCategory.OTHER
    }

    private fun String.containsAny(vararg terms: String): Boolean =
        terms.any { this.contains(it) }
}
