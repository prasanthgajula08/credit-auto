package com.creditauto.app.data.location

import com.creditauto.app.domain.DetectedStore
import com.creditauto.app.domain.MerchantCategoryClassifier
import com.creditauto.app.domain.SavedPlace
import com.creditauto.app.domain.StoreSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class StoreDetectionResult(
    val stores: List<DetectedStore>,
    val statusMessage: String? = null
)

class NearbyStoreDetector(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val overpassMirrors = listOf(
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter"
    )

    /**
     * Instant offline matching of user's Saved Places by GPS distance (0ms network latency).
     */
    fun matchSavedPlaces(
        latitude: Double,
        longitude: Double,
        savedPlaces: List<SavedPlace>
    ): List<DetectedStore> {
        return savedPlaces.mapNotNull { place ->
            val pLat = place.latitude ?: return@mapNotNull null
            val pLon = place.longitude ?: return@mapNotNull null
            val dist = LocationHelper.haversineDistanceMeters(latitude, longitude, pLat, pLon)
            if (dist <= place.radiusMeters.coerceAtLeast(50.0)) {
                DetectedStore(
                    name = place.name,
                    category = place.category,
                    source = StoreSource.SAVED_PLACE,
                    distanceMeters = dist,
                    address = place.address,
                    latitude = pLat,
                    longitude = pLon,
                    acceptedNetworks = place.acceptedNetworks,
                    savedPlaceId = place.id,
                    preferredCardId = place.preferredCardId
                )
            } else {
                null
            }
        }.sortedBy { it.distanceMeters ?: Double.MAX_VALUE }
    }

    suspend fun detectNearbyStores(
        latitude: Double,
        longitude: Double,
        savedPlaces: List<SavedPlace>,
        googlePlacesApiKey: String,
        radiusMeters: Int = 150
    ): StoreDetectionResult = withContext(Dispatchers.IO) {
        val matchedSaved = matchSavedPlaces(latitude, longitude, savedPlaces)

        var remoteStores: List<DetectedStore> = emptyList()
        var warning: String? = null

        // Single wider query (min 250m) eliminates slow double sequential queries
        // and pre-populates the whole shopping plaza into our spatial cache.
        val effectiveRadius = radiusMeters.coerceAtLeast(250)

        if (googlePlacesApiKey.isNotBlank()) {
            val googleResult = runCatching {
                queryGooglePlacesNearby(latitude, longitude, effectiveRadius, googlePlacesApiKey)
            }
            if (googleResult.isSuccess) {
                remoteStores = googleResult.getOrDefault(emptyList())
            } else {
                warning = "Google Places lookup failed; fell back to OpenStreetMap"
            }
        }

        if (remoteStores.isEmpty()) {
            val osmResult = runCatching {
                queryOverpassParallel(latitude, longitude, effectiveRadius)
            }
            if (osmResult.isSuccess) {
                remoteStores = osmResult.getOrDefault(emptyList())
            } else if (matchedSaved.isEmpty()) {
                warning = "Could not reach nearby store lookup (offline or rate-limited). Pick a category below."
            }
        }

        val combined = deduplicateStores(matchedSaved + remoteStores)
        StoreDetectionResult(
            stores = combined,
            statusMessage = when {
                combined.isEmpty() && warning != null -> warning
                combined.isEmpty() -> "No stores found within ${effectiveRadius}m — pick a category or search manually."
                else -> warning
            }
        )
    }

    /**
     * Races all Overpass mirrors concurrently and returns the fastest successful response,
     * immediately cancelling slower mirrors.
     */
    private suspend fun queryOverpassParallel(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): List<DetectedStore> = coroutineScope {
        val query = """
            [out:json][timeout:5];
            (
              nwr["name"]["shop"](around:$radiusMeters,$latitude,$longitude);
              nwr["name"]["amenity"~"restaurant|cafe|fast_food|bar|pub|fuel|charging_station|pharmacy|cinema|theatre|parking"](around:$radiusMeters,$latitude,$longitude);
              nwr["name"]["tourism"~"hotel|motel|resort|museum|theme_park"](around:$radiusMeters,$latitude,$longitude);
            );
            out center 25;
        """.trimIndent()

        val encoded = "data=${URLEncoder.encode(query, "UTF-8")}"
        val resultsChannel = Channel<Result<List<DetectedStore>>>(overpassMirrors.size)

        val jobs = overpassMirrors.map { endpoint ->
            launch(Dispatchers.IO) {
                val res = runCatching {
                    val body = encoded.toRequestBody("application/x-www-form-urlencoded".toMediaType())
                    val request = Request.Builder()
                        .url(endpoint)
                        .header("User-Agent", "CreditAutoAndroid/1.0")
                        .post(body)
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw IllegalStateException("Overpass HTTP ${response.code}")
                        }
                        val raw = response.body?.string().orEmpty()
                        parseOverpassResponse(raw, latitude, longitude)
                    }
                }
                resultsChannel.trySend(res)
            }
        }

        var lastError: Throwable? = null
        repeat(overpassMirrors.size) {
            val result = resultsChannel.receive()
            if (result.isSuccess) {
                jobs.forEach { it.cancel() }
                return@coroutineScope result.getOrThrow()
            } else {
                lastError = result.exceptionOrNull()
            }
        }

        throw lastError ?: IllegalStateException("All Overpass mirrors failed")
    }

    private fun queryGooglePlacesNearby(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        apiKey: String
    ): List<DetectedStore> {
        val payload = """
            {
              "maxResultCount": 15,
              "rankPreference": "DISTANCE",
              "locationRestriction": {
                "circle": {
                  "center": {
                    "latitude": $latitude,
                    "longitude": $longitude
                  },
                  "radius": ${radiusMeters.toDouble()}
                }
              }
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://places.googleapis.com/v1/places:searchNearby")
            .header("Content-Type", "application/json")
            .header("X-Goog-Api-Key", apiKey)
            .header(
                "X-Goog-FieldMask",
                "places.displayName,places.types,places.location,places.formattedAddress"
            )
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Google Places HTTP ${response.code}")
            }
            val raw = response.body?.string().orEmpty()
            return parseGooglePlacesResponse(raw, latitude, longitude)
        }
    }

    internal fun parseOverpassResponse(
        rawJson: String,
        userLat: Double,
        userLon: Double
    ): List<DetectedStore> {
        if (rawJson.isBlank()) return emptyList()
        val root = json.parseToJsonElement(rawJson).jsonObject
        val elements = root["elements"]?.jsonArray ?: return emptyList()

        return elements.mapNotNull { elem ->
            val obj = elem.jsonObject
            val tagsObj = obj["tags"]?.jsonObject ?: return@mapNotNull null
            val tags = tagsObj.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") }
            val name = tags["name"]?.trim().orEmpty()
            if (name.isBlank()) return@mapNotNull null

            val (lat, lon) = extractOsmCoords(obj) ?: return@mapNotNull null
            val distance = LocationHelper.haversineDistanceMeters(userLat, userLon, lat, lon)
            val classification = MerchantCategoryClassifier.classify(
                merchantName = name,
                osmTags = tags
            )
            val address = buildOsmAddress(tags)

            DetectedStore(
                name = name,
                category = classification.category,
                source = StoreSource.OPEN_STREET_MAP,
                distanceMeters = distance,
                address = address,
                latitude = lat,
                longitude = lon,
                acceptedNetworks = classification.acceptedNetworks
            )
        }.sortedBy { it.distanceMeters ?: Double.MAX_VALUE }
    }

    internal fun parseGooglePlacesResponse(
        rawJson: String,
        userLat: Double,
        userLon: Double
    ): List<DetectedStore> {
        if (rawJson.isBlank()) return emptyList()
        val root = json.parseToJsonElement(rawJson).jsonObject
        val places = root["places"]?.jsonArray ?: return emptyList()

        return places.mapNotNull { elem ->
            val obj = elem.jsonObject
            val name = obj["displayName"]?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
            if (name.isBlank()) return@mapNotNull null

            val types = obj["types"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
            val loc = obj["location"]?.jsonObject
            val lat = loc?.get("latitude")?.jsonPrimitive?.doubleOrNull
            val lon = loc?.get("longitude")?.jsonPrimitive?.doubleOrNull
            val distance = if (lat != null && lon != null) {
                LocationHelper.haversineDistanceMeters(userLat, userLon, lat, lon)
            } else {
                null
            }
            val address = obj["formattedAddress"]?.jsonPrimitive?.contentOrNull ?: ""
            val classification = MerchantCategoryClassifier.classify(
                merchantName = name,
                googlePlaceTypes = types
            )

            DetectedStore(
                name = name,
                category = classification.category,
                source = StoreSource.GOOGLE_PLACES,
                distanceMeters = distance,
                address = address,
                latitude = lat,
                longitude = lon,
                acceptedNetworks = classification.acceptedNetworks
            )
        }.sortedBy { it.distanceMeters ?: Double.MAX_VALUE }
    }

    private fun extractOsmCoords(obj: JsonObject): Pair<Double, Double>? {
        val lat = obj["lat"]?.jsonPrimitive?.doubleOrNull
        val lon = obj["lon"]?.jsonPrimitive?.doubleOrNull
        if (lat != null && lon != null) return lat to lon

        val center = obj["center"]?.jsonObject
        val cLat = center?.get("lat")?.jsonPrimitive?.doubleOrNull
        val cLon = center?.get("lon")?.jsonPrimitive?.doubleOrNull
        if (cLat != null && cLon != null) return cLat to cLon
        return null
    }

    private fun buildOsmAddress(tags: Map<String, String>): String {
        val house = tags["addr:housenumber"].orEmpty()
        val street = tags["addr:street"].orEmpty()
        val city = tags["addr:city"].orEmpty()
        return listOf(
            listOf(house, street).filter { it.isNotBlank() }.joinToString(" "),
            city
        ).filter { it.isNotBlank() }.joinToString(", ")
    }

    private fun deduplicateStores(stores: List<DetectedStore>): List<DetectedStore> {
        val seen = LinkedHashSet<String>()
        return stores.filter { store ->
            val key = store.name.lowercase().replace(Regex("[^a-z0-9]"), "")
            seen.add(key)
        }.take(15)
    }
}
