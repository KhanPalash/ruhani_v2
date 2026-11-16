package com.khanscreations.ruhani.data.model

import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val isPrecise: Boolean,
    val areaName: String? = null,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class Mosque(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Float,
    val openingHours: String? = null,
    val phone: String? = null,
    val website: String? = null,
    val denomination: String? = null
)

enum class SearchRadius(val meters: Int, val label: String) {
    ONE_KM(1000, "1 km"),
    THREE_KM(3000, "3 km"),
    FIVE_KM(5000, "5 km"),
    TEN_KM(10000, "10 km"),
    TWENTY_KM(20000, "20 km")
}

enum class OsmMapLayer {
    MINIMAL_LIGHT,
    STANDARD_OSM,
    TERRAIN
}

data class QiblaCompassState(
    val qiblaBearingDegrees: Float? = null,
    val deviceAzimuthDegrees: Float? = null,
    val hasCompassSensor: Boolean = true
) {
    val relativeQiblaDegrees: Float?
        get() {
            val qibla = qiblaBearingDegrees ?: return null
            val heading = deviceAzimuthDegrees ?: return qibla
            return GeoMath.normalizeDegrees(qibla - heading)
        }

    val signedOffsetDegrees: Float?
        get() {
            val qibla = qiblaBearingDegrees ?: return null
            val heading = deviceAzimuthDegrees ?: return null
            return GeoMath.shortestSignedAngle(heading, qibla)
        }

    val isAlignedToQibla: Boolean
        get() = signedOffsetDegrees?.let { abs(it) <= 5f } == true
}

sealed interface MosqueSearchState {
    data object Idle : MosqueSearchState
    data object Loading : MosqueSearchState
    data class Success(val mosques: List<Mosque>) : MosqueSearchState
    data class Error(val message: String) : MosqueSearchState
}

object GeoMath {
    private const val EARTH_RADIUS_METERS = 6_371_000.0
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LNG = 39.826206

    fun calculateDistanceMeters(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double
    ): Float {
        val dLat = Math.toRadians(toLat - fromLat)
        val dLng = Math.toRadians(toLng - fromLng)
        val lat1 = Math.toRadians(fromLat)
        val lat2 = Math.toRadians(toLat)
        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (EARTH_RADIUS_METERS * c).toFloat()
    }

    fun calculateQiblaBearingDegrees(fromLat: Double, fromLng: Double): Float {
        val lat1 = Math.toRadians(fromLat)
        val lat2 = Math.toRadians(KAABA_LAT)
        val dLng = Math.toRadians(KAABA_LNG - fromLng)
        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        val bearing = Math.toDegrees(atan2(y, x))
        return normalizeDegrees(bearing.toFloat())
    }

    fun normalizeDegrees(degrees: Float): Float {
        val mod = degrees % 360f
        return if (mod < 0f) mod + 360f else mod
    }

    fun shortestSignedAngle(fromDegrees: Float, toDegrees: Float): Float {
        var diff = normalizeDegrees(toDegrees) - normalizeDegrees(fromDegrees)
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return diff
    }

    fun formatDistance(distanceMeters: Float): String {
        return if (distanceMeters < 1000f) {
            "${distanceMeters.roundToInt()} m"
        } else {
            val km = distanceMeters / 1000f
            if (km < 10f) String.format(Locale.US, "%.1f km", km) else "${km.roundToInt()} km"
        }
    }

    fun estimateWalkMinutes(distanceMeters: Float): Int {
        return (distanceMeters / 80f).roundToInt().coerceAtLeast(1)
    }

    fun compassCardinal(bearingDegrees: Float): String {
        val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = (((normalizeDegrees(bearingDegrees) + 22.5f) % 360f) / 45f).toInt().coerceIn(0, 7)
        return directions[index]
    }
}
