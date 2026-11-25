package com.creditauto.app.data.location

import com.creditauto.app.domain.CachedStoreSnapshot
import com.creditauto.app.domain.CardNetwork
import com.creditauto.app.domain.DetectedStore
import com.creditauto.app.domain.MerchantCategoryClassifier
import com.creditauto.app.domain.SpendCategory
import com.creditauto.app.domain.StoreSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MerchantCategoryClassifierAndParserTest {

    @Test
    fun classifiesMerchantQuirksAccurately() {
        val costco = MerchantCategoryClassifier.classify("Costco Wholesale")
        assertEquals(SpendCategory.WHOLESALE_CLUB, costco.category)
        assertEquals(setOf(CardNetwork.VISA), costco.acceptedNetworks)

        val costcoGas = MerchantCategoryClassifier.classify("Costco Gas Station")
        assertEquals(SpendCategory.GAS, costcoGas.category)
        assertEquals(setOf(CardNetwork.VISA), costcoGas.acceptedNetworks)

        val wholeFoods = MerchantCategoryClassifier.classify("Whole Foods Market")
        assertEquals(SpendCategory.GROCERY, wholeFoods.category)

        val cvs = MerchantCategoryClassifier.classify("CVS Pharmacy")
        assertEquals(SpendCategory.DRUGSTORE, cvs.category)
    }

    @Test
    fun parsesOverpassJsonNodesAndWaysSortedByDistance() {
        val sampleJson = """
            {
              "elements": [
                {
                  "type": "way",
                  "id": 101,
                  "center": { "lat": 37.7755, "lon": -122.4190 },
                  "tags": {
                    "name": "Farther Cafe",
                    "amenity": "cafe",
                    "addr:housenumber": "100",
                    "addr:street": "Market St"
                  }
                },
                {
                  "type": "node",
                  "id": 102,
                  "lat": 37.7750,
                  "lon": -122.4194,
                  "tags": {
                    "name": "Trader Joe's",
                    "shop": "supermarket"
                  }
                }
              ]
            }
        """.trimIndent()

        val detector = NearbyStoreDetector()
        val stores = detector.parseOverpassResponse(sampleJson, 37.7749, -122.4194)

        assertEquals(2, stores.size)
        assertEquals("Trader Joe's", stores[0].name)
        assertEquals(SpendCategory.GROCERY, stores[0].category)
        assertEquals(StoreSource.OPEN_STREET_MAP, stores[0].source)

        assertEquals("Farther Cafe", stores[1].name)
        assertEquals(SpendCategory.DINING, stores[1].category)
        assertEquals("100 Market St", stores[1].address)
        assertTrue((stores[0].distanceMeters ?: 999.0) < (stores[1].distanceMeters ?: 0.0))
    }

    @Test
    fun parsesGooglePlacesNewResponse() {
        val sampleJson = """
            {
              "places": [
                {
                  "displayName": { "text": "Shell" },
                  "types": ["gas_station", "convenience_store"],
                  "formattedAddress": "500 Main St",
                  "location": { "latitude": 37.7750, "longitude": -122.4195 }
                }
              ]
            }
        """.trimIndent()

        val detector = NearbyStoreDetector()
        val stores = detector.parseGooglePlacesResponse(sampleJson, 37.7749, -122.4194)

        assertEquals(1, stores.size)
        assertEquals("Shell", stores[0].name)
        assertEquals(SpendCategory.GAS, stores[0].category)
        assertEquals(StoreSource.GOOGLE_PLACES, stores[0].source)
        assertEquals("500 Main St", stores[0].address)
    }

    @Test
    fun haversineDistanceMatchesKnownDistance() {
        // ~111 meters per 0.001 degree latitude
        val meters = LocationHelper.haversineDistanceMeters(37.000, -122.000, 37.001, -122.000)
        assertTrue("Expected ~111m, got $meters", meters in 108.0..114.0)
    }

    @Test
    fun spatialCacheValidatesDistanceAndAgeAndResortsStores() {
        val now = 1_000_000L
        val storeA = DetectedStore(
            name = "Store A",
            category = SpendCategory.GROCERY,
            source = StoreSource.OPEN_STREET_MAP,
            latitude = 37.0000,
            longitude = -122.0000
        )
        val storeB = DetectedStore(
            name = "Store B",
            category = SpendCategory.DINING,
            source = StoreSource.OPEN_STREET_MAP,
            latitude = 37.0004,
            longitude = -122.0000
        )
        val snapshot = CachedStoreSnapshot(
            latitude = 37.0000,
            longitude = -122.0000,
            timestampMs = now - 60_000L, // 1 min ago
            stores = listOf(storeA, storeB)
        )

        // Still ~44m away & 1 min old -> cache HIT
        assertTrue(LocationHelper.isCacheValid(snapshot, 37.0004, -122.0000, nowMs = now))

        // Moved ~550m away -> cache MISS
        assertFalse(LocationHelper.isCacheValid(snapshot, 37.0050, -122.0000, nowMs = now))

        // 30 minutes old -> cache MISS
        assertFalse(
            LocationHelper.isCacheValid(
                snapshot,
                37.0000,
                -122.0000,
                nowMs = now + 30 * 60_000L
            )
        )

        // When standing at Store B's coordinates, recomputeDistancesAndSort puts Store B first
        val resorted = LocationHelper.recomputeDistancesAndSort(snapshot.stores, 37.0004, -122.0000)
        assertEquals("Store B", resorted.first().name)
    }
}
