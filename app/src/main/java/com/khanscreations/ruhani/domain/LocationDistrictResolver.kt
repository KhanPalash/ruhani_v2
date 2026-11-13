package com.khanscreations.ruhani.domain

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Resolves the nearest Bangladeshi district from the device location.
 *
 * Only approximate (coarse) location is used: mapping a coordinate onto a
 * district centroid needs city-level accuracy, so the app never asks for
 * ACCESS_FINE_LOCATION and never keeps a location history. The coordinate is
 * used once, converted to a district, and discarded.
 *
 * No Play Services dependency: this talks to the platform [LocationManager]
 * directly, which keeps the release APK small and works on devices without
 * Google Play Services.
 */
object LocationDistrictResolver {

    const val COARSE_PERMISSION: String = Manifest.permission.ACCESS_COARSE_LOCATION

    /**
     * A detected location further than this from every district centroid is
     * treated as "not in Bangladesh" and is not applied automatically. The 64
     * centroids tile the country, so anywhere inside Bangladesh is well within
     * this radius; the margin only exists to absorb coastal/char areas. The
     * exact border is not checked — the point is to avoid mapping a user in
     * London onto Sylhet and showing prayer times that are hours off.
     */
    const val MAX_AUTO_APPLY_DISTANCE_KM: Double = 150.0

    private const val EARTH_RADIUS_KM = 6371.0
    private const val FRESH_FIX_MILLIS = 10 * 60 * 1000L
    private const val DEFAULT_TIMEOUT_MILLIS = 15_000L

    data class DistrictDetection(
        val district: BdDistrict,
        val distanceKm: Double
    ) {
        val isWithinBangladesh: Boolean
            get() = distanceKm <= MAX_AUTO_APPLY_DISTANCE_KM
    }

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, COARSE_PERMISSION) == PackageManager.PERMISSION_GRANTED

    /**
     * Returns the nearest district, or null when the permission is missing, the
     * location services are off, or no fix arrives before [timeoutMillis].
     * Call from a background/main dispatcher — it suspends, it does not block.
     */
    @SuppressLint("MissingPermission")
    suspend fun detect(
        context: Context,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
    ): DistrictDetection? {
        if (!hasPermission(context)) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        val lastKnown = bestLastKnown(manager)
        val location = when {
            lastKnown != null && ageMillis(lastKnown) <= FRESH_FIX_MILLIS -> lastKnown
            else -> requestCurrentLocation(context, manager, timeoutMillis) ?: lastKnown
        } ?: return null

        return nearestDistrict(location.latitude, location.longitude)
    }

    /** Nearest district to a coordinate, by great-circle distance. */
    fun nearestDistrict(latitude: Double, longitude: Double): DistrictDetection? =
        RuhaniContentCatalog.bdDistricts
            .map { district ->
                DistrictDetection(
                    district = district,
                    distanceKm = haversineKm(latitude, longitude, district.latitude, district.longitude)
                )
            }
            .minByOrNull { it.distanceKm }

    /** Great-circle distance in kilometres. */
    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = (sin(dLat / 2) * sin(dLat / 2)) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * (sin(dLon / 2) * sin(dLon / 2))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    // ---------------------------------------------------------------------
    // Platform location plumbing
    // ---------------------------------------------------------------------

    /**
     * Coarse permission cannot use the GPS provider, so network/passive are the
     * only sources. Both are best-effort: a device with location services off
     * returns an empty list and the caller falls back to the manual picker.
     */
    private fun availableProviders(manager: LocationManager): List<String> =
        listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .filter { provider ->
                runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)
            }

    @SuppressLint("MissingPermission")
    private fun bestLastKnown(manager: LocationManager): Location? =
        availableProviders(manager)
            .mapNotNull { provider ->
                runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            }
            .minByOrNull { ageMillis(it) }

    private fun ageMillis(location: Location): Long {
        val ageNanos = SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos
        return if (ageNanos <= 0L) Long.MAX_VALUE else ageNanos / 1_000_000L
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestCurrentLocation(
        context: Context,
        manager: LocationManager,
        timeoutMillis: Long
    ): Location? {
        for (provider in availableProviders(manager)) {
            val location = withTimeoutOrNull(timeoutMillis) {
                requestSingleUpdate(context, manager, provider)
            }
            if (location != null) return location
        }
        return null
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleUpdate(
        context: Context,
        manager: LocationManager,
        provider: String
    ): Location? = suspendCancellableCoroutine { continuation ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val cancellationSignal = CancellationSignal()
            continuation.invokeOnCancellation { cancellationSignal.cancel() }
            runCatching {
                manager.getCurrentLocation(
                    provider,
                    cancellationSignal,
                    ContextCompat.getMainExecutor(context)
                ) { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
            }.onFailure {
                if (continuation.isActive) continuation.resume(null)
            }
        } else {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    runCatching { manager.removeUpdates(this) }
                    if (continuation.isActive) continuation.resume(location)
                }

                @Deprecated("Deprecated in API 29")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                override fun onProviderEnabled(provider: String) = Unit

                override fun onProviderDisabled(provider: String) {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
            continuation.invokeOnCancellation {
                runCatching { manager.removeUpdates(listener) }
            }
            runCatching {
                manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            }.onFailure {
                if (continuation.isActive) continuation.resume(null)
            }
        }
    }
}
