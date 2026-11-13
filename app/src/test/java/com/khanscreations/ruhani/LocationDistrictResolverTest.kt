package com.khanscreations.ruhani

import com.khanscreations.ruhani.domain.LocationDistrictResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Covers the coordinate -> district mapping behind the "detect from location"
 * action and the auto-detect on launch.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocationDistrictResolverTest {

    @Test
    fun `a coordinate exactly on a district centroid maps back to that district`() {
        val dhaka = LocationDistrictResolver.nearestDistrict(23.8103, 90.4125)
        assertNotNull(dhaka)
        assertEquals("Dhaka", dhaka!!.district.id)
        assertTrue("distance should be ~0 for the centroid itself", dhaka.distanceKm < 0.1)
    }

    @Test
    fun `a user in Chuadanga gets Chuadanga, not the capital`() {
        val detection = LocationDistrictResolver.nearestDistrict(23.6402, 88.8418)
        assertNotNull(detection)
        assertEquals("Chuadanga", detection!!.district.id)
        assertTrue(detection.isWithinBangladesh)
    }

    @Test
    fun `a location outside Bangladesh is detected but not auto-applied`() {
        // Central London: the nearest of the 64 centroids is thousands of km away.
        val detection = LocationDistrictResolver.nearestDistrict(51.5074, -0.1278)
        assertNotNull(detection)
        assertFalse(detection!!.isWithinBangladesh)
        assertTrue(detection.distanceKm > LocationDistrictResolver.MAX_AUTO_APPLY_DISTANCE_KM)
    }

    @Test
    fun `every district is nearest to itself`() {
        // Guards against a mis-typed latitude/longitude in the catalogue: if two
        // entries swapped coordinates the mapping would silently drift.
        com.khanscreations.ruhani.domain.RuhaniContentCatalog.bdDistricts.forEach { district ->
            val detection = LocationDistrictResolver.nearestDistrict(district.latitude, district.longitude)
            assertEquals(
                "centroid of ${district.id} should map to itself",
                district.id,
                detection?.district?.id
            )
        }
    }

    @Test
    fun `haversine matches a known distance`() {
        // Dhaka -> Chattogram is roughly 216 km.
        val km = LocationDistrictResolver.haversineKm(23.8103, 90.4125, 22.3569, 91.7832)
        assertTrue("expected ~216 km, got $km", km in 200.0..230.0)
    }
}
