package com.khanscreations.ruhani

import com.khanscreations.ruhani.data.model.GeoMath
import com.khanscreations.ruhani.data.model.MosqueSearchState
import com.khanscreations.ruhani.data.model.QiblaCompassState
import com.khanscreations.ruhani.data.remote.NominatimPlace
import com.khanscreations.ruhani.data.remote.OverpassApiService
import com.khanscreations.ruhani.data.remote.OverpassCenter
import com.khanscreations.ruhani.data.remote.OverpassElement
import com.khanscreations.ruhani.data.remote.OverpassResponse
import com.khanscreations.ruhani.data.remote.PhotonFeature
import com.khanscreations.ruhani.data.remote.PhotonGeometry
import com.khanscreations.ruhani.data.remote.PhotonProperties
import com.khanscreations.ruhani.data.remote.PhotonResponse
import com.khanscreations.ruhani.data.repository.MosqueRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class MosqueFinderUnitTest {

    @Test
    fun formatDistance_formatsMetersAndKilometers() {
        assertEquals("450 m", GeoMath.formatDistance(450f))
        assertEquals("1.5 km", GeoMath.formatDistance(1500f))
        assertEquals("12 km", GeoMath.formatDistance(12400f))
    }

    @Test
    fun estimateWalkMinutes_returnsAtLeastOneMinute() {
        assertEquals(1, GeoMath.estimateWalkMinutes(40f))
        assertEquals(5, GeoMath.estimateWalkMinutes(400f))
    }

    @Test
    fun qiblaBearing_andAlignmentDetection() {
        val bearing = GeoMath.calculateQiblaBearingDegrees(23.8103, 90.4125)
        assertTrue("Expected Qibla bearing near 277°, got $bearing", bearing in 270f..285f)
        assertEquals("W", GeoMath.compassCardinal(bearing))

        val aligned = QiblaCompassState(qiblaBearingDegrees = 277f, deviceAzimuthDegrees = 275f)
        assertTrue(aligned.isAlignedToQibla)

        val unaligned = QiblaCompassState(qiblaBearingDegrees = 277f, deviceAzimuthDegrees = 180f)
        assertFalse(unaligned.isAlignedToQibla)
    }

    @Test
    fun mosqueRepository_parsesNodesWaysAndDeduplicatesOverlaps() = runTest {
        val fakeService = FakeOverpassApiService(
            onOverpass = { _ ->
                OverpassResponse(
                    elements = listOf(
                        OverpassElement(
                            type = "node",
                            id = 101L,
                            lat = 23.7294,
                            lon = 90.4119,
                            tags = mapOf(
                                "amenity" to "place_of_worship",
                                "religion" to "muslim",
                                "name" to "Baitul Mukarram",
                                "addr:street" to "Topkhana Road",
                                "addr:city" to "Dhaka",
                                "opening_hours" to "24/7"
                            )
                        ),
                        OverpassElement(
                            type = "way",
                            id = 202L,
                            center = OverpassCenter(23.72942, 90.41192),
                            tags = mapOf(
                                "amenity" to "place_of_worship",
                                "religion" to "muslim",
                                "name" to "Baitul Mukarram"
                            )
                        ),
                        OverpassElement(
                            type = "way",
                            id = 303L,
                            center = OverpassCenter(23.7350, 90.4150),
                            tags = mapOf(
                                "amenity" to "place_of_worship",
                                "name:bn" to "কাকরাইল জামে মসজিদ",
                                "name:en" to "Kakrail Mosque"
                            )
                        ),
                        OverpassElement(
                            type = "node",
                            id = 404L,
                            lat = 23.7360,
                            lon = 90.4160,
                            tags = mapOf(
                                "amenity" to "place_of_worship",
                                "name" to "Kali Mandir"
                            )
                        )
                    )
                )
            }
        )

        val repo = MosqueRepository(
            overpassApiService = fakeService,
            includeCommunityLandmarks = false
        )
        val result = repo.searchNearbyMosques(23.73, 90.41, 23.73, 90.41, 3000)
        assertTrue(result is MosqueSearchState.Success)
        val mosques = (result as MosqueSearchState.Success).mosques
        assertEquals(2, mosques.size)
        assertEquals("Baitul Mukarram", mosques[0].name)
        assertEquals("Topkhana Road, Dhaka", mosques[0].address)
        assertEquals("24/7", mosques[0].openingHours)
        assertEquals("Kakrail Mosque", mosques[1].name)
    }

    @Test
    fun mosqueRepository_failsOverWhenPrimaryReturnsRemarkTimeout() = runTest {
        val calledUrls = mutableListOf<String>()
        val fakeService = FakeOverpassApiService(
            onOverpass = { url ->
                calledUrls.add(url)
                if (url == OverpassApiService.PRIMARY_URL) {
                    OverpassResponse(
                        elements = emptyList(),
                        remark = "runtime error: Query timed out in \"around\" at line 1."
                    )
                } else {
                    OverpassResponse(
                        elements = listOf(
                            OverpassElement(
                                type = "node",
                                id = 505L,
                                lat = 23.5162,
                                lon = 88.7785,
                                tags = mapOf(
                                    "amenity" to "place_of_worship",
                                    "name" to "দক্ষিণ পাড়া জামে মসজিদ"
                                )
                            )
                        )
                    )
                }
            }
        )

        val repo = MosqueRepository(
            overpassApiService = fakeService,
            includeCommunityLandmarks = false
        )
        val result = repo.searchNearbyMosques(23.516, 88.778, 23.516, 88.778, 3000)
        assertEquals(
            listOf(OverpassApiService.PRIMARY_URL, OverpassApiService.FALLBACK_URL),
            calledUrls
        )
        assertTrue(result is MosqueSearchState.Success)
        val mosques = (result as MosqueSearchState.Success).mosques
        assertEquals(1, mosques.size)
        assertEquals("দক্ষিণ পাড়া জামে মসজিদ", mosques.first().name)
    }

    @Test
    fun mosqueRepository_usesNominatimBoundedFallbackAndFiltersNonMosques() = runTest {
        val fakeService = FakeOverpassApiService(
            onOverpass = { _ -> throw IOException("Simulated Overpass outage") },
            onNominatim = { query, _ ->
                if (query == "mosque") {
                    listOf(
                        NominatimPlace(
                            place_id = 1L,
                            osm_type = "node",
                            osm_id = 10L,
                            lat = "23.5165",
                            lon = "88.7780",
                            name = "Darshana Central Jame Masjid",
                            display_name = "Darshana Central Jame Masjid, Darshana, Chuadanga",
                            category = "amenity",
                            type = "place_of_worship",
                            extratags = mapOf("religion" to "muslim"),
                            address = mapOf("road" to "Station Road", "town" to "Darshana")
                        ),
                        NominatimPlace(
                            place_id = 2L,
                            osm_type = "node",
                            osm_id = 11L,
                            lat = "23.5170",
                            lon = "88.7790",
                            name = "দরশনা কালী মন্দির",
                            display_name = "দরশনা কালী মন্দির, Darshana",
                            category = "amenity",
                            type = "place_of_worship"
                        ),
                        NominatimPlace(
                            place_id = 3L,
                            osm_type = "way",
                            osm_id = 12L,
                            lat = "23.5180",
                            lon = "88.7800",
                            name = "বায়তুল আমান মসজিদ",
                            display_name = "বায়তুল আমান মসজিদ, Darshana",
                            category = "amenity",
                            type = "place_of_worship"
                        ),
                        NominatimPlace(
                            place_id = 4L,
                            osm_type = "way",
                            osm_id = 13L,
                            lat = "23.5182",
                            lon = "88.7802",
                            name = "Mosque Road",
                            display_name = "Mosque Road, Darshana",
                            category = "highway",
                            type = "residential"
                        )
                    )
                } else {
                    emptyList()
                }
            }
        )

        val repo = MosqueRepository(
            overpassApiService = fakeService,
            includeCommunityLandmarks = false
        )
        val viewbox = repo.buildViewbox(23.516, 88.778, 3000)
        val bounds = viewbox.split(",").map { it.toDouble() }
        assertEquals(4, bounds.size)
        assertTrue("west < east", bounds[0] < bounds[2])
        assertTrue("north > south", bounds[1] > bounds[3])

        val result = repo.searchNearbyMosques(23.516, 88.778, 23.516, 88.778, 3000)
        assertTrue(result is MosqueSearchState.Success)
        val mosques = (result as MosqueSearchState.Success).mosques
        assertEquals(2, mosques.size)
        assertEquals("Darshana Central Jame Masjid", mosques[0].name)
        assertEquals("Station Road, Darshana", mosques[0].address)
        assertEquals("বায়তুল আমান মসজিদ", mosques[1].name)
    }

    @Test
    fun mosqueRepository_autoExpandsTo20kmWhen3kmHasNoMappedMosques() = runTest {
        val repoRef = MosqueRepository(includeCommunityLandmarks = false)
        val viewbox20km = repoRef.buildViewbox(24.1000, 89.1000, 20_000)

        val fakeService = FakeOverpassApiService(
            onOverpass = { _ -> OverpassResponse(elements = emptyList()) },
            onNominatim = { query, viewbox ->
                if (viewbox == viewbox20km && query == "মসজিদ") {
                    listOf(
                        NominatimPlace(
                            place_id = 91L,
                            osm_type = "node",
                            osm_id = 910L,
                            lat = "24.1850",
                            lon = "89.1600",
                            name = "Garabaria Jame Mosque",
                            display_name = "Garabaria Jame Mosque, Chuadanga",
                            category = "amenity",
                            type = "place_of_worship",
                            extratags = mapOf("religion" to "muslim")
                        ),
                        NominatimPlace(
                            place_id = 92L,
                            osm_type = "node",
                            osm_id = 920L,
                            lat = "24.1920",
                            lon = "89.1650",
                            name = "গাড়াবাড়িয়া উত্তরপাড়া জামে মসজিদ",
                            display_name = "গাড়াবাড়িয়া উত্তরপাড়া জামে মসজিদ, Chuadanga",
                            category = "amenity",
                            type = "place_of_worship",
                            extratags = mapOf("religion" to "muslim")
                        )
                    )
                } else {
                    emptyList()
                }
            }
        )

        val repo = MosqueRepository(
            overpassApiService = fakeService,
            includeCommunityLandmarks = false
        )
        val result = repo.searchNearbyMosques(24.1000, 89.1000, 24.1000, 89.1000, 3000)
        assertTrue(result is MosqueSearchState.Success)
        val mosques = (result as MosqueSearchState.Success).mosques
        assertEquals(2, mosques.size)
        assertEquals("Garabaria Jame Mosque", mosques[0].name)
        assertEquals("গাড়াবাড়িয়া উত্তরপাড়া জামে মসজিদ", mosques[1].name)
    }

    @Test
    fun mosqueRepository_darshanaSearchIncludesLocalLandmarksAndCaches20kmRegion() = runTest {
        var nominatimCallCount = 0
        val fakeService = FakeOverpassApiService(
            onOverpass = { _ -> OverpassResponse(elements = emptyList()) },
            onNominatim = { query, _ ->
                nominatimCallCount++
                if (query == "মসজিদ") {
                    listOf(
                        NominatimPlace(
                            place_id = 91L,
                            osm_type = "node",
                            osm_id = 910L,
                            lat = "23.6099587",
                            lon = "88.8593779",
                            name = "Garabaria Jame Mosque",
                            display_name = "Garabaria Jame Mosque, Chuadanga",
                            category = "amenity",
                            type = "place_of_worship",
                            extratags = mapOf("religion" to "muslim")
                        )
                    )
                } else {
                    emptyList()
                }
            }
        )

        val repo = MosqueRepository(
            overpassApiService = fakeService,
            includeCommunityLandmarks = true
        )

        // 1. Search 3 km around Darshana Junction (23.5265, 88.7971)
        val state3km = repo.searchNearbyMosques(23.5265, 88.7971, 23.5265, 88.7971, 3000)
        assertTrue(state3km is MosqueSearchState.Success)
        val mosques3km = (state3km as MosqueSearchState.Success).mosques
        assertTrue("Expected local Darshana mosques within 3 km", mosques3km.size >= 10)
        assertTrue(mosques3km.first().name.contains("Darshana Rail Bazar"))
        assertEquals(2, nominatimCallCount)

        // 2. Switching to 20 km uses the 20 km regional cache in 0 additional network calls
        val state20km = repo.searchNearbyMosques(23.5265, 88.7971, 23.5265, 88.7971, 20_000)
        assertTrue(state20km is MosqueSearchState.Success)
        val mosques20km = (state20km as MosqueSearchState.Success).mosques
        assertTrue(mosques20km.any { it.name == "Garabaria Jame Mosque" })
        assertEquals("Expected 0 extra Nominatim calls on cached radius switch", 2, nominatimCallCount)
    }

    @Test
    fun mosqueRepository_failsOverToPhotonWhenOverpassAndNominatimUnavailable() = runTest {
        val calledUrls = mutableListOf<String>()
        val fakeService = FakeOverpassApiService(
            onOverpass = { url ->
                calledUrls.add(url)
                throw IOException("Simulated Overpass timeout")
            },
            onNominatim = { _, _ -> throw IOException("Simulated Nominatim outage") },
            onPhoton = { query ->
                if (query == "mosque") {
                    PhotonResponse(
                        features = listOf(
                            PhotonFeature(
                                geometry = PhotonGeometry(listOf(88.7780, 23.5165)),
                                properties = PhotonProperties(
                                    osm_id = 77L,
                                    osm_type = "N",
                                    name = "Darshana Central Jame Masjid",
                                    city = "Darshana"
                                )
                            )
                        )
                    )
                } else {
                    PhotonResponse(emptyList())
                }
            }
        )

        val repo = MosqueRepository(
            overpassApiService = fakeService,
            includeCommunityLandmarks = false
        )
        val result = repo.searchNearbyMosques(23.516, 88.778, 23.516, 88.778, 3000)
        assertEquals(
            listOf(OverpassApiService.PRIMARY_URL, OverpassApiService.FALLBACK_URL),
            calledUrls
        )
        assertTrue(result is MosqueSearchState.Success)
        assertEquals(
            "Darshana Central Jame Masjid",
            (result as MosqueSearchState.Success).mosques.first().name
        )
    }

    @Test
    fun mosqueRepository_returnsErrorWhenAllBackendsFail() = runTest {
        val fakeService = FakeOverpassApiService(
            onOverpass = { _ -> throw IOException("No network") },
            onNominatim = { _, _ -> throw IOException("No network") },
            onPhoton = { _ -> throw IOException("No network") }
        )

        val repo = MosqueRepository(
            overpassApiService = fakeService,
            includeCommunityLandmarks = false
        )
        val result = repo.searchNearbyMosques(23.516, 88.778, 23.516, 88.778, 3000)
        assertTrue("Expected Error state when all backends fail", result is MosqueSearchState.Error)
    }

    private class FakeOverpassApiService(
        private val onOverpass: (url: String) -> OverpassResponse = { OverpassResponse(emptyList()) },
        private val onNominatim: (query: String, viewbox: String) -> List<NominatimPlace> = { _, _ -> emptyList() },
        private val onPhoton: (query: String) -> PhotonResponse = { PhotonResponse(emptyList()) }
    ) : OverpassApiService {
        override suspend fun queryNearbyMosques(
            url: String,
            query: String,
            userAgent: String
        ): OverpassResponse = onOverpass(url)

        override suspend fun searchNominatim(
            url: String,
            query: String,
            viewbox: String,
            bounded: Int,
            format: String,
            extratags: Int,
            addressdetails: Int,
            limit: Int,
            userAgent: String
        ): List<NominatimPlace> = onNominatim(query, viewbox)

        override suspend fun searchPhotonMosques(
            url: String,
            query: String,
            lat: Double,
            lon: Double,
            limit: Int,
            osmTag: String?,
            userAgent: String
        ): PhotonResponse = onPhoton(query)
    }
}
