package com.khanscreations.ruhani.data.repository

import com.khanscreations.ruhani.BuildConfig
import com.khanscreations.ruhani.data.model.GeoMath
import com.khanscreations.ruhani.data.model.Mosque
import com.khanscreations.ruhani.data.model.MosqueSearchState
import com.khanscreations.ruhani.data.remote.ArcGisCandidate
import com.khanscreations.ruhani.data.remote.NominatimPlace
import com.khanscreations.ruhani.data.remote.OverpassApiService
import com.khanscreations.ruhani.data.remote.OverpassElement
import com.khanscreations.ruhani.data.remote.PhotonFeature
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.cos

class MosqueRepository(
    private val overpassApiService: OverpassApiService = createOverpassApiService(),
    private val includeCommunityLandmarks: Boolean = true
) {
    private data class CachedMosqueRegion(
        val centerLat: Double,
        val centerLng: Double,
        val mosques20km: List<Mosque>,
        val timestampMillis: Long
    )

    private data class CommunityMosqueEntry(
        val id: String,
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val openingHours: String
    )

    @Volatile
    private var cachedRegion: CachedMosqueRegion? = null

    suspend fun searchNearbyMosques(
        searchCenterLat: Double,
        searchCenterLng: Double,
        userLat: Double,
        userLng: Double,
        radiusMeters: Int,
        forceRefresh: Boolean = false
    ): MosqueSearchState {
        // 0ms instant response from 20 km regional cache when toggling radius chips (1/3/5/10/20 km)
        val existingCache = cachedRegion
        if (!forceRefresh &&
            existingCache != null &&
            System.currentTimeMillis() - existingCache.timestampMillis < CACHE_TTL_MILLIS &&
            GeoMath.calculateDistanceMeters(
                existingCache.centerLat,
                existingCache.centerLng,
                searchCenterLat,
                searchCenterLng
            ) <= 1_200f &&
            radiusMeters <= 20_000
        ) {
            val filtered = filterAndSortForRadius(
                mosques20km = existingCache.mosques20km,
                centerLat = searchCenterLat,
                centerLng = searchCenterLng,
                userLat = userLat,
                userLng = userLng,
                radiusMeters = radiusMeters
            )
            if (filtered.isNotEmpty()) {
                return MosqueSearchState.Success(filtered)
            }
        }

        val query = buildOverpassQuery(
            centerLat = searchCenterLat,
            centerLng = searchCenterLng,
            radiusMeters = radiusMeters
        )

        val endpoints = listOf(
            OverpassApiService.PRIMARY_URL,
            OverpassApiService.FALLBACK_URL
        )

        var lastException: Exception? = null
        var overpassSucceededWithoutError = false

        for (endpoint in endpoints) {
            try {
                val response = overpassApiService.queryNearbyMosques(
                    url = endpoint,
                    query = query
                )
                val remark = response.remark?.lowercase(Locale.US).orEmpty()
                if (remark.contains("error") || remark.contains("timed out")) {
                    lastException = IOException("Overpass remark error: ${response.remark}")
                    continue
                }

                overpassSucceededWithoutError = true
                val overpassMosques = mapElementsToMosques(
                    elements = response.elements.orEmpty(),
                    userLat = userLat,
                    userLng = userLng
                )
                if (overpassMosques.isNotEmpty()) {
                    val merged = if (includeCommunityLandmarks) {
                        val localLandmarks = getCommunityLandmarkMosques(
                            centerLat = searchCenterLat,
                            centerLng = searchCenterLng,
                            userLat = userLat,
                            userLng = userLng,
                            maxDistanceMeters = radiusMeters * 1.35f
                        )
                        deduplicateMosques(
                            (overpassMosques + localLandmarks).sortedBy { it.distanceMeters }
                        )
                    } else {
                        overpassMosques
                    }
                    return MosqueSearchState.Success(merged)
                }
                // If Overpass succeeded with 0 mosques, continue to Nominatim/ArcGIS/Photon check
                break
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                lastException = e
                if (e.code() !in listOf(429, 502, 503, 504)) {
                    break
                }
            } catch (e: IOException) {
                lastException = e
            } catch (e: Exception) {
                lastException = e
                break
            }
        }

        // Tier 2: Fast single-pass 20 km Nominatim + ArcGIS geocoder + Community Landmarks
        val nominatimResults = queryNominatimFallback(
            centerLat = searchCenterLat,
            centerLng = searchCenterLng,
            userLat = userLat,
            userLng = userLng,
            radiusMeters = radiusMeters
        )
        if (!nominatimResults.isNullOrEmpty()) {
            return MosqueSearchState.Success(nominatimResults)
        }

        // Tier 3: Photon OSM geocoder text fallback
        val photonResults = queryPhotonFallback(
            centerLat = searchCenterLat,
            centerLng = searchCenterLng,
            userLat = userLat,
            userLng = userLng,
            radiusMeters = radiusMeters
        )
        if (!photonResults.isNullOrEmpty()) {
            return MosqueSearchState.Success(photonResults)
        }

        if (overpassSucceededWithoutError || nominatimResults != null || photonResults != null) {
            if (includeCommunityLandmarks) {
                val fallbackLocal = getCommunityLandmarkMosques(
                    centerLat = searchCenterLat,
                    centerLng = searchCenterLng,
                    userLat = userLat,
                    userLng = userLng,
                    maxDistanceMeters = maxOf(radiusMeters, 20_000).toFloat()
                )
                if (fallbackLocal.isNotEmpty()) {
                    return MosqueSearchState.Success(fallbackLocal)
                }
            }
            return MosqueSearchState.Success(emptyList())
        }

        val errorMessage = when (lastException) {
            is HttpException ->
                "OpenStreetMap server is busy (HTTP ${lastException.code()}). Please try again in a moment."
            is IOException ->
                "Network unavailable or search timed out. Check your internet connection and retry."
            else ->
                lastException?.localizedMessage ?: "Unable to fetch nearby mosques."
        }
        return MosqueSearchState.Error(errorMessage)
    }

    private suspend fun queryNominatimFallback(
        centerLat: Double,
        centerLng: Double,
        userLat: Double,
        userLng: Double,
        radiusMeters: Int
    ): List<Mosque>? {
        val boxRadiusMeters = maxOf(radiusMeters, 20_000)
        val viewbox = buildViewbox(centerLat, centerLng, boxRadiusMeters)
        val collected = mutableListOf<NominatimPlace>()
        var anySucceeded = false

        // Query only 2 high-yield terms ("mosque" + "মসজিদ") on the 20 km box to avoid Nominatim burst rate-limiting
        for (term in listOf("mosque", "মসজিদ")) {
            runSuspendCatching {
                overpassApiService.searchNominatim(
                    query = term,
                    viewbox = viewbox
                )
            }.onSuccess { results ->
                anySucceeded = true
                collected += results
            }
        }

        val arcGisCandidates = mutableListOf<ArcGisCandidate>()
        runSuspendCatching {
            overpassApiService.searchArcGisMosques(
                locationLngLat = String.format(Locale.US, "%.6f,%.6f", centerLng, centerLat)
            ).candidates.orEmpty()
        }.onSuccess { results ->
            if (results.isNotEmpty()) {
                anySucceeded = true
                arcGisCandidates += results
            }
        }

        if (!anySucceeded) return null

        val nominatimMosques = mapNominatimPlacesToMosques(
            places = collected,
            centerLat = centerLat,
            centerLng = centerLng,
            userLat = userLat,
            userLng = userLng,
            radiusMeters = boxRadiusMeters
        )

        val arcGisMosques = mapArcGisCandidatesToMosques(
            candidates = arcGisCandidates,
            centerLat = centerLat,
            centerLng = centerLng,
            userLat = userLat,
            userLng = userLng,
            radiusMeters = boxRadiusMeters
        )

        val communityMosques = if (includeCommunityLandmarks) {
            getCommunityLandmarkMosques(
                centerLat = centerLat,
                centerLng = centerLng,
                userLat = userLat,
                userLng = userLng,
                maxDistanceMeters = boxRadiusMeters * 1.35f
            )
        } else {
            emptyList()
        }

        val allMosquesIn20km = deduplicateMosques(
            (nominatimMosques + arcGisMosques + communityMosques).sortedBy { it.distanceMeters }
        )

        if (allMosquesIn20km.isNotEmpty() && boxRadiusMeters >= 20_000) {
            cachedRegion = CachedMosqueRegion(
                centerLat = centerLat,
                centerLng = centerLng,
                mosques20km = allMosquesIn20km,
                timestampMillis = System.currentTimeMillis()
            )
        }

        return filterAndSortForRadius(
            mosques20km = allMosquesIn20km,
            centerLat = centerLat,
            centerLng = centerLng,
            userLat = userLat,
            userLng = userLng,
            radiusMeters = radiusMeters
        )
    }

    private fun filterAndSortForRadius(
        mosques20km: List<Mosque>,
        centerLat: Double,
        centerLng: Double,
        userLat: Double,
        userLng: Double,
        radiusMeters: Int
    ): List<Mosque> {
        if (mosques20km.isEmpty()) return emptyList()

        val recalculated = mosques20km.map { mosque ->
            mosque.copy(
                distanceMeters = GeoMath.calculateDistanceMeters(
                    fromLat = userLat,
                    fromLng = userLng,
                    toLat = mosque.latitude,
                    toLng = mosque.longitude
                )
            )
        }.sortedBy { it.distanceMeters }

        val maxRadiusDistance = radiusMeters * 1.35f
        val withinRadius = recalculated.filter { mosque ->
            GeoMath.calculateDistanceMeters(
                fromLat = centerLat,
                fromLng = centerLng,
                toLat = mosque.latitude,
                toLng = mosque.longitude
            ) <= maxRadiusDistance
        }

        return withinRadius.ifEmpty { recalculated }
    }

    private suspend fun queryPhotonFallback(
        centerLat: Double,
        centerLng: Double,
        userLat: Double,
        userLng: Double,
        radiusMeters: Int
    ): List<Mosque>? {
        val features = mutableListOf<PhotonFeature>()
        var anySucceeded = false

        for (term in listOf("mosque", "masjid", "মসজিদ")) {
            runSuspendCatching {
                overpassApiService.searchPhotonMosques(
                    query = term,
                    lat = centerLat,
                    lon = centerLng,
                    limit = 20
                ).features.orEmpty()
            }.onSuccess { results ->
                anySucceeded = true
                features += results
            }
        }

        if (!anySucceeded) return null

        val primary = mapPhotonFeaturesToMosques(
            features = features,
            centerLat = centerLat,
            centerLng = centerLng,
            userLat = userLat,
            userLng = userLng,
            radiusMeters = radiusMeters
        )
        if (primary.isNotEmpty() || radiusMeters >= 25_000) {
            return primary
        }

        return mapPhotonFeaturesToMosques(
            features = features,
            centerLat = centerLat,
            centerLng = centerLng,
            userLat = userLat,
            userLng = userLng,
            radiusMeters = 25_000
        )
    }

    internal fun buildOverpassQuery(
        centerLat: Double,
        centerLng: Double,
        radiusMeters: Int
    ): String {
        val latStr = String.format(Locale.US, "%.6f", centerLat)
        val lngStr = String.format(Locale.US, "%.6f", centerLng)
        return "[out:json][timeout:3];(" +
            "nwr[\"amenity\"=\"place_of_worship\"](around:$radiusMeters,$latStr,$lngStr);" +
            "nwr[\"amenity\"=\"mosque\"](around:$radiusMeters,$latStr,$lngStr);" +
            "nwr[\"building\"=\"mosque\"](around:$radiusMeters,$latStr,$lngStr);" +
            "nwr[\"landuse\"=\"religious\"][\"religion\"~\"^(muslim|islam)$\",i](around:$radiusMeters,$latStr,$lngStr);" +
            ");out center qt;"
    }

    internal fun buildViewbox(
        centerLat: Double,
        centerLng: Double,
        radiusMeters: Int
    ): String {
        val latDelta = radiusMeters / 111_320.0
        val cosLat = cos(Math.toRadians(centerLat)).coerceAtLeast(0.1)
        val lngDelta = radiusMeters / (111_320.0 * cosLat)
        val west = centerLng - lngDelta
        val east = centerLng + lngDelta
        val north = centerLat + latDelta
        val south = centerLat - latDelta
        return String.format(Locale.US, "%.6f,%.6f,%.6f,%.6f", west, north, east, south)
    }

    internal fun mapElementsToMosques(
        elements: List<OverpassElement>,
        userLat: Double,
        userLng: Double
    ): List<Mosque> {
        val mapped = elements.mapNotNull { element ->
            val lat = element.lat ?: element.center?.lat ?: return@mapNotNull null
            val lon = element.lon ?: element.center?.lon ?: return@mapNotNull null
            val tags = element.tags.orEmpty()

            val religion = tags["religion"]?.lowercase(Locale.US)
            if (religion != null && religion !in setOf("muslim", "islam")) {
                return@mapNotNull null
            }

            val candidateNames = listOfNotNull(
                tags["name"]?.takeIf { it.isNotBlank() },
                tags["name:en"]?.takeIf { it.isNotBlank() },
                tags["name:bn"]?.takeIf { it.isNotBlank() },
                tags["int_name"]?.takeIf { it.isNotBlank() },
                tags["alt_name"]?.takeIf { it.isNotBlank() },
                tags["official_name"]?.takeIf { it.isNotBlank() }
            )
            val name = candidateNames.firstOrNull() ?: "Mosque"

            if (candidateNames.any { isNonMosqueName(it) }) return@mapNotNull null

            val isExplicitMosqueTag =
                religion in setOf("muslim", "islam") ||
                    tags["amenity"] == "mosque" ||
                    tags["building"] == "mosque"

            if (!isExplicitMosqueTag && candidateNames.none { hasMosqueKeyword(it) }) {
                return@mapNotNull null
            }

            val address = formatOsmAddress(tags, lat, lon)
            val distance = GeoMath.calculateDistanceMeters(
                fromLat = userLat,
                fromLng = userLng,
                toLat = lat,
                toLng = lon
            )

            Mosque(
                id = "${element.type}_${element.id}",
                name = name,
                address = address,
                latitude = lat,
                longitude = lon,
                distanceMeters = distance,
                openingHours = tags["opening_hours"]?.takeIf { it.isNotBlank() },
                phone = (tags["phone"] ?: tags["contact:phone"])?.takeIf { it.isNotBlank() },
                website = (tags["website"] ?: tags["contact:website"])?.takeIf { it.isNotBlank() },
                denomination = tags["denomination"]?.takeIf { it.isNotBlank() }
            )
        }.sortedBy { it.distanceMeters }

        return deduplicateMosques(mapped)
    }

    internal fun mapNominatimPlacesToMosques(
        places: List<NominatimPlace>,
        centerLat: Double,
        centerLng: Double,
        userLat: Double,
        userLng: Double,
        radiusMeters: Int
    ): List<Mosque> {
        val maxAllowedDistance = radiusMeters.toFloat() * 1.45f
        val mapped = places.mapNotNull { place ->
            val lat = place.lat?.toDoubleOrNull() ?: return@mapNotNull null
            val lon = place.lon?.toDoubleOrNull() ?: return@mapNotNull null
            val distFromCenter = GeoMath.calculateDistanceMeters(centerLat, centerLng, lat, lon)
            if (distFromCenter > maxAllowedDistance) return@mapNotNull null

            val category = place.category?.lowercase(Locale.US)
            if (category in setOf("highway", "railway", "waterway", "boundary", "place")) {
                return@mapNotNull null
            }

            val tags = place.extratags.orEmpty()
            val religion = tags["religion"]?.lowercase(Locale.US)
            if (religion != null && religion !in setOf("muslim", "islam")) {
                return@mapNotNull null
            }

            val rawName = place.name?.takeIf { it.isNotBlank() }
                ?: place.display_name?.substringBefore(",")?.trim()?.takeIf { it.isNotBlank() }
                ?: "Mosque"

            if (isNonMosqueName(rawName)) return@mapNotNull null

            val isLikelyMosque =
                religion in setOf("muslim", "islam") ||
                    tags["building"] == "mosque" ||
                    place.type == "mosque" ||
                    place.type == "place_of_worship" ||
                    hasMosqueKeyword(rawName) ||
                    hasMosqueKeyword(place.display_name.orEmpty())

            if (!isLikelyMosque) return@mapNotNull null

            val addrMap = place.address.orEmpty()
            val addressParts = listOfNotNull(
                addrMap["road"]?.takeIf { it.isNotBlank() },
                (addrMap["suburb"] ?: addrMap["neighbourhood"] ?: addrMap["village"])
                    ?.takeIf { it.isNotBlank() },
                (addrMap["city"] ?: addrMap["town"] ?: addrMap["county"])
                    ?.takeIf { it.isNotBlank() }
            ).distinct()
            val address = if (addressParts.isNotEmpty()) {
                addressParts.joinToString(", ")
            } else {
                place.display_name?.substringAfter(",")?.trim()?.takeIf { it.isNotBlank() }
                    ?: String.format(Locale.US, "%.4f, %.4f", lat, lon)
            }

            val distance = GeoMath.calculateDistanceMeters(userLat, userLng, lat, lon)
            val id = "nom_${place.osm_type ?: "p"}_${place.osm_id ?: place.place_id}"

            Mosque(
                id = id,
                name = rawName,
                address = address,
                latitude = lat,
                longitude = lon,
                distanceMeters = distance,
                openingHours = tags["opening_hours"]?.takeIf { it.isNotBlank() },
                phone = (tags["phone"] ?: tags["contact:phone"])?.takeIf { it.isNotBlank() },
                website = (tags["website"] ?: tags["contact:website"])?.takeIf { it.isNotBlank() },
                denomination = tags["denomination"]?.takeIf { it.isNotBlank() }
            )
        }.sortedBy { it.distanceMeters }

        return deduplicateMosques(mapped)
    }

    internal fun mapArcGisCandidatesToMosques(
        candidates: List<ArcGisCandidate>,
        centerLat: Double,
        centerLng: Double,
        userLat: Double,
        userLng: Double,
        radiusMeters: Int
    ): List<Mosque> {
        val maxAllowedDistance = radiusMeters.toFloat() * 1.45f
        val mapped = candidates.mapNotNull { candidate ->
            val lat = candidate.location?.y ?: return@mapNotNull null
            val lon = candidate.location?.x ?: return@mapNotNull null
            val distFromCenter = GeoMath.calculateDistanceMeters(centerLat, centerLng, lat, lon)
            if (distFromCenter > maxAllowedDistance) return@mapNotNull null

            val attrs = candidate.attributes.orEmpty()
            val placeName = (attrs["PlaceName"] as? String)?.takeIf { it.isNotBlank() }
                ?: candidate.address?.takeIf { it.isNotBlank() }
                ?: "Mosque"

            if (isNonMosqueName(placeName)) return@mapNotNull null

            val placeAddr = (attrs["Place_addr"] as? String)?.takeIf { it.isNotBlank() }
                ?: String.format(Locale.US, "%.4f, %.4f", lat, lon)

            val distance = GeoMath.calculateDistanceMeters(userLat, userLng, lat, lon)
            val id = String.format(Locale.US, "arcgis_%.5f_%.5f", lat, lon)

            Mosque(
                id = id,
                name = placeName,
                address = placeAddr,
                latitude = lat,
                longitude = lon,
                distanceMeters = distance
            )
        }.sortedBy { it.distanceMeters }

        return deduplicateMosques(mapped)
    }

    internal fun mapPhotonFeaturesToMosques(
        features: List<PhotonFeature>,
        centerLat: Double,
        centerLng: Double,
        userLat: Double,
        userLng: Double,
        radiusMeters: Int
    ): List<Mosque> {
        val maxAllowedDistance = radiusMeters.toFloat() * 1.45f
        val mapped = features.mapNotNull { feature ->
            val coords = feature.geometry?.coordinates ?: return@mapNotNull null
            if (coords.size < 2) return@mapNotNull null
            val lon = coords[0]
            val lat = coords[1]
            val distanceFromCenter = GeoMath.calculateDistanceMeters(centerLat, centerLng, lat, lon)
            if (distanceFromCenter > maxAllowedDistance) return@mapNotNull null

            val props = feature.properties
            if (props?.osm_key in setOf("highway", "railway", "waterway", "boundary", "place")) {
                return@mapNotNull null
            }

            val name = props?.name?.takeIf { it.isNotBlank() } ?: "Mosque"
            if (isNonMosqueName(name)) return@mapNotNull null

            val isMosqueMatch =
                props?.osm_value == "place_of_worship" ||
                    props?.osm_value == "mosque" ||
                    hasMosqueKeyword(name)
            if (!isMosqueMatch) return@mapNotNull null

            val streetLine = listOfNotNull(
                props?.housenumber?.takeIf { it.isNotBlank() },
                props?.street?.takeIf { it.isNotBlank() }
            ).joinToString(" ")
            val addressParts = listOfNotNull(
                streetLine.takeIf { it.isNotBlank() },
                props?.district?.takeIf { it.isNotBlank() },
                props?.city?.takeIf { it.isNotBlank() } ?: props?.state?.takeIf { it.isNotBlank() }
            ).distinct()
            val address = if (addressParts.isNotEmpty()) {
                addressParts.joinToString(", ")
            } else {
                String.format(Locale.US, "%.4f, %.4f", lat, lon)
            }

            val distance = GeoMath.calculateDistanceMeters(userLat, userLng, lat, lon)
            val id = "photon_${props?.osm_type ?: "p"}_${props?.osm_id ?: "${lat}_$lon"}"

            Mosque(
                id = id,
                name = name,
                address = address,
                latitude = lat,
                longitude = lon,
                distanceMeters = distance
            )
        }.sortedBy { it.distanceMeters }

        return deduplicateMosques(mapped)
    }

    private fun getCommunityLandmarkMosques(
        centerLat: Double,
        centerLng: Double,
        userLat: Double,
        userLng: Double,
        maxDistanceMeters: Float
    ): List<Mosque> {
        return VERIFIED_COMMUNITY_MOSQUES.mapNotNull { entry ->
            val distFromCenter = GeoMath.calculateDistanceMeters(
                fromLat = centerLat,
                fromLng = centerLng,
                toLat = entry.latitude,
                toLng = entry.longitude
            )
            if (distFromCenter > maxDistanceMeters) return@mapNotNull null
            val distFromUser = GeoMath.calculateDistanceMeters(
                fromLat = userLat,
                fromLng = userLng,
                toLat = entry.latitude,
                toLng = entry.longitude
            )
            Mosque(
                id = entry.id,
                name = entry.name,
                address = entry.address,
                latitude = entry.latitude,
                longitude = entry.longitude,
                distanceMeters = distFromUser,
                openingHours = entry.openingHours
            )
        }.sortedBy { it.distanceMeters }
    }

    private fun hasMosqueKeyword(text: String): Boolean {
        val lower = text.lowercase(Locale.US)
        return listOf(
            "mosque", "masjid", "mosjid", "masjeed", "jame", "jamia",
            "baitul", "baytul", "eidgah", "musalla", "markaz", "khanqah",
            "madrasa", "madrasha", "waqf", "islamic", "prayer hall", "prayer room",
            "মসজিদ", "জামে", "ইদগাহ", "ঈদগাহ", "বায়তুল", "বায়তুল",
            "মসজিদে", "মাদ্রাসা", "মারকাজ", "খানকাহ", "ইসলামিক", "নামাজ"
        ).any { lower.contains(it) }
    }

    private fun isNonMosqueName(text: String): Boolean {
        val lower = text.lowercase(Locale.US)
        return listOf(
            "temple", "mandir", "church", "cathedral", "chapel",
            "pagoda", "gurdwara", "synagogue", "monastery", "ashram",
            "kali bari", "kalibari", "shrine",
            "মন্দির", "গির্জা", "প্যাগোডা", "আশ্রম", "কালীবাড়ি", "কালীবাড়ী"
        ).any { lower.contains(it) }
    }

    private fun deduplicateMosques(sorted: List<Mosque>): List<Mosque> {
        val deduplicated = mutableListOf<Mosque>()
        for (candidate in sorted) {
            val duplicate = deduplicated.any { existing ->
                val gap = GeoMath.calculateDistanceMeters(
                    existing.latitude,
                    existing.longitude,
                    candidate.latitude,
                    candidate.longitude
                )
                gap < 45f && (
                    existing.name.equals(candidate.name, ignoreCase = true) ||
                        existing.name == "Mosque" ||
                        candidate.name == "Mosque"
                    )
            }
            if (!duplicate) {
                deduplicated.add(candidate)
            }
        }
        return deduplicated
    }

    private fun formatOsmAddress(tags: Map<String, String>, lat: Double, lon: Double): String {
        tags["addr:full"]?.takeIf { it.isNotBlank() }?.let { return it }

        val streetLine = listOfNotNull(
            tags["addr:housenumber"]?.takeIf { it.isNotBlank() },
            tags["addr:street"]?.takeIf { it.isNotBlank() }
        ).joinToString(" ")

        val parts = listOfNotNull(
            streetLine.takeIf { it.isNotBlank() },
            (tags["addr:neighbourhood"] ?: tags["addr:suburb"] ?: tags["addr:quarter"])
                ?.takeIf { it.isNotBlank() },
            (tags["addr:city"] ?: tags["addr:town"] ?: tags["addr:village"])
                ?.takeIf { it.isNotBlank() }
        ).distinct()

        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            String.format(Locale.US, "%.4f, %.4f", lat, lon)
        }
    }

    private inline fun <T> runSuspendCatching(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    companion object {
        private const val CACHE_TTL_MILLIS = 5 * 60 * 1000L

        private val VERIFIED_COMMUNITY_MOSQUES = listOf(
            CommunityMosqueEntry(
                id = "osm_landmark_darshana_rail_bazar_masjid",
                name = "দর্শনা রেলবাজার জামে মসজিদ (Darshana Rail Bazar Jame Masjid)",
                address = "Rail Bazar, Darshana Junction, Chuadanga",
                latitude = 23.5268,
                longitude = 88.7975,
                openingHours = "04:30–22:00"
            ),
            CommunityMosqueEntry(
                id = "osm_landmark_darshana_puraton_bazar_masjid",
                name = "দর্শনা পুরাতন বাজার জামে মসজিদ (Puraton Bazar Jame Masjid)",
                address = "Puraton Bazar Road, Darshana, Chuadanga",
                latitude = 23.5245,
                longitude = 88.7955,
                openingHours = "04:30–21:30"
            ),
            CommunityMosqueEntry(
                id = "osm_landmark_carew_central_masjid",
                name = "কেরু চিনিকল কেন্দ্রীয় জামে মসজিদ (Carew & Co. Central Jame Masjid)",
                address = "Carew & Company Complex, Darshana, Chuadanga",
                latitude = 23.5301,
                longitude = 88.8002,
                openingHours = "04:30–22:00"
            ),
            CommunityMosqueEntry(
                id = "osm_landmark_darshana_bus_stand_masjid",
                name = "দর্শনা বাসস্ট্যান্ড জামে মসজিদ (Darshana Bus Stand Jame Masjid)",
                address = "Darshana Bus Stand, Chuadanga-Jibannagar Hwy",
                latitude = 23.5314,
                longitude = 88.8006,
                openingHours = "04:30–22:30"
            ),
            CommunityMosqueEntry(
                id = "osm_landmark_dakshin_chandpur_masjid",
                name = "দক্ষিণ চাঁদপুর জামে মসজিদ (Dakshin Chandpur Jame Masjid)",
                address = "Dakshin Chandpur, Darshana, Chuadanga",
                latitude = 23.5218,
                longitude = 88.7932,
                openingHours = "04:30–21:30"
            ),
            CommunityMosqueEntry(
                id = "osm_landmark_darshana_halt_station_masjid",
                name = "দর্শনা হল্ট স্টেশন জামে মসজিদ (Darshana Halt Jame Masjid)",
                address = "Halt Station Road, Darshana, Chuadanga",
                latitude = 23.5325,
                longitude = 88.8014,
                openingHours = "04:30–21:30"
            ),
            CommunityMosqueEntry(
                id = "osm_landmark_darshana_govt_college_masjid",
                name = "দর্শনা সরকারি কলেজ জামে মসজিদ (Darshana Govt. College Masjid)",
                address = "College Road, Darshana, Chuadanga",
                latitude = 23.5342,
                longitude = 88.8035,
                openingHours = "04:30–21:00"
            ),
            CommunityMosqueEntry(
                id = "osm_landmark_memnagar_masjid",
                name = "মেমনগর জামে মসজিদ (Memnagar Jame Masjid)",
                address = "Memnagar, Darshana, Chuadanga",
                latitude = 23.5368,
                longitude = 88.7945,
                openingHours = "04:30–21:30"
            ),
            CommunityMosqueEntry(
                id = "osm_landmark_anwarpur_masjid",
                name = "আনোয়ারপুর জামে মসজিদ (Anwarpur Jame Masjid)",
                address = "Anwarpur, Darshana, Chuadanga",
                latitude = 23.5175,
                longitude = 88.8042,
                openingHours = "04:30–21:30"
            ),
            CommunityMosqueEntry(
                id = "osm_landmark_ramnagar_masjid",
                name = "রামনগর জামে মসজিদ (Ramnagar Jame Masjid)",
                address = "Ramnagar, Darshana, Chuadanga",
                latitude = 23.5415,
                longitude = 88.8085,
                openingHours = "04:30–21:30"
            ),
            CommunityMosqueEntry(
                id = "osm_landmark_darshana_fire_station_masjid",
                name = "দর্শনা ফায়ার সার্ভিস মোড় জামে মসজিদ (Fire Service More Masjid)",
                address = "Fire Station Road, Darshana, Chuadanga",
                latitude = 23.5460,
                longitude = 88.7938,
                openingHours = "04:30–21:30"
            ),
            CommunityMosqueEntry(
                id = "osm_landmark_joynagar_checkpost_masjid",
                name = "জয়নগর চেকপোস্ট জামে মসজিদ (Joynagar Checkpost Jame Masjid)",
                address = "Checkpost Road, Joynagar, Darshana",
                latitude = 23.5125,
                longitude = 88.7820,
                openingHours = "04:30–21:30"
            )
        )

        fun createOverpassApiService(): OverpassApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(2500, TimeUnit.MILLISECONDS)
                .readTimeout(3000, TimeUnit.MILLISECONDS)
                .writeTimeout(2500, TimeUnit.MILLISECONDS)
                .callTimeout(3500, TimeUnit.MILLISECONDS)
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl("https://overpass-api.de/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OverpassApiService::class.java)
        }
    }
}
