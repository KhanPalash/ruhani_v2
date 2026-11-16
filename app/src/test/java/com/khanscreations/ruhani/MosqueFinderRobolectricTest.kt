package com.khanscreations.ruhani

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.test.core.app.ApplicationProvider
import com.khanscreations.ruhani.data.model.Mosque
import com.khanscreations.ruhani.data.model.MosqueSearchState
import com.khanscreations.ruhani.data.model.UserLocation
import com.khanscreations.ruhani.data.remote.NominatimPlace
import com.khanscreations.ruhani.data.remote.OverpassApiService
import com.khanscreations.ruhani.data.remote.OverpassResponse
import com.khanscreations.ruhani.data.remote.PhotonResponse
import com.khanscreations.ruhani.data.repository.MosqueRepository
import com.khanscreations.ruhani.ui.components.syncMosqueOverlaysToOverlayManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MosqueFinderRobolectricTest {

    @Test
    fun `overpass JSON response maps through repository and OverlayManager into individual Mosque Markers with custom icon`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val osmBaseDir = File(context.cacheDir, "osmdroid_test").apply { mkdirs() }
        val osmTileDir = File(osmBaseDir, "tiles").apply { mkdirs() }
        Configuration.getInstance().apply {
            osmdroidBasePath = osmBaseDir
            osmdroidTileCache = osmTileDir
            userAgentValue = "Ruhani-MasjidNear-Test/1.0"
        }

        val overpassJson = """
            {
              "version": 0.6,
              "elements": [
                {
                  "type": "node",
                  "id": 1001,
                  "lat": 23.5168,
                  "lon": 88.7792,
                  "tags": {
                    "amenity": "place_of_worship",
                    "religion": "muslim",
                    "name": "Darshana Central Jame Masjid",
                    "addr:street": "Station Road"
                  }
                },
                {
                  "type": "way",
                  "id": 2002,
                  "center": { "lat": 23.5191, "lon": 88.7815 },
                  "tags": {
                    "amenity": "place_of_worship",
                    "name": "দক্ষিণ পাড়া মসজিদ"
                  }
                },
                {
                  "type": "relation",
                  "id": 3003,
                  "center": { "lat": 23.5210, "lon": 88.7840 },
                  "tags": {
                    "building": "mosque",
                    "name:en": "Al-Aqsa Mosque"
                  }
                },
                {
                  "type": "node",
                  "id": 4004,
                  "lat": 23.5175,
                  "lon": 88.7805,
                  "tags": {
                    "amenity": "place_of_worship",
                    "religion": "hindu",
                    "name": "Darshana Mandir"
                  }
                }
              ]
            }
        """.trimIndent()

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val parsedResponse = moshi.adapter(OverpassResponse::class.java).fromJson(overpassJson)
        assertNotNull(parsedResponse)

        val fakeService = object : OverpassApiService {
            override suspend fun queryNearbyMosques(
                url: String,
                query: String,
                userAgent: String
            ): OverpassResponse = parsedResponse!!

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
            ): List<NominatimPlace> = emptyList()

            override suspend fun searchPhotonMosques(
                url: String,
                query: String,
                lat: Double,
                lon: Double,
                limit: Int,
                osmTag: String?,
                userAgent: String
            ): PhotonResponse = PhotonResponse(emptyList())
        }

        val repo = MosqueRepository(
            overpassApiService = fakeService,
            includeCommunityLandmarks = false
        )
        val searchState = repo.searchNearbyMosques(23.5160, 88.7780, 23.5160, 88.7780, 3000)
        assertTrue(searchState is MosqueSearchState.Success)
        val mosques = (searchState as MosqueSearchState.Success).mosques
        assertEquals(3, mosques.size)

        val mapView = MapView(context)
        val liveLocation = UserLocation(
            latitude = 23.5160,
            longitude = 88.7780,
            accuracyMeters = 8f,
            isPrecise = true
        )
        var selectedCallbackMosque: Mosque? = null

        syncMosqueOverlaysToOverlayManager(
            mapView = mapView,
            context = context,
            liveLocation = liveLocation,
            searchCenterLat = liveLocation.latitude,
            searchCenterLng = liveLocation.longitude,
            radiusMeters = 3000,
            mosques = mosques,
            selectedMosqueId = mosques.first().id,
            onSelectMosque = { selectedCallbackMosque = it }
        )

        val overlays = mapView.overlayManager.overlays()
        val polygons = overlays.filterIsInstance<Polygon>()
        val markers = overlays.filterIsInstance<Marker>()

        assertEquals(1, polygons.size)
        // 1 user live location marker + 3 individual mosque markers
        assertEquals(4, markers.size)

        val mosqueMarkers = markers.filter { it.id != "user_live_location" }
        assertEquals(3, mosqueMarkers.size)

        val expectedIds = mosques.map { it.id }.toSet()
        assertEquals(expectedIds, mosqueMarkers.map { it.id }.toSet())

        mosqueMarkers.forEach { marker ->
            val mappedMosque = marker.relatedObject as? Mosque
            assertNotNull("Marker must carry Mosque relatedObject", mappedMosque)
            assertEquals(mappedMosque!!.name, marker.title)
            assertEquals(mappedMosque.latitude, marker.position.latitude, 1e-6)
            assertEquals(mappedMosque.longitude, marker.position.longitude, 1e-6)
            assertTrue(
                "Marker icon must be a custom BitmapDrawable",
                marker.icon is BitmapDrawable
            )
            assertTrue((marker.icon as BitmapDrawable).bitmap.width > 0)
        }

        // Re-syncing must replace overlays cleanly without duplicating Markers
        syncMosqueOverlaysToOverlayManager(
            mapView = mapView,
            context = context,
            liveLocation = liveLocation,
            searchCenterLat = liveLocation.latitude,
            searchCenterLng = liveLocation.longitude,
            radiusMeters = 3000,
            mosques = mosques,
            selectedMosqueId = null,
            onSelectMosque = { selectedCallbackMosque = it }
        )
        assertEquals(
            3,
            mapView.overlayManager.overlays().filterIsInstance<Marker>().count { it.id != "user_live_location" }
        )
        assertEquals(null, selectedCallbackMosque)

        mapView.onDetach()
    }
}
