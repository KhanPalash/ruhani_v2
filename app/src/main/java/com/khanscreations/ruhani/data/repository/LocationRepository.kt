package com.khanscreations.ruhani.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.khanscreations.ruhani.data.model.GeoMath
import com.khanscreations.ruhani.data.model.UserLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class LocationRepository(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasFineLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun hasAnyLocationPermission(): Boolean =
        hasFineLocationPermission() ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun observeLiveLocation(): Flow<UserLocation> = callbackFlow {
        if (!hasAnyLocationPermission()) {
            close()
            return@callbackFlow
        }

        val isPrecise = hasFineLocationPermission()
        var lastGeocodedLat: Double? = null
        var lastGeocodedLng: Double? = null
        var cachedAreaName: String? = null

        fun emitLocation(location: Location) {
            val shouldGeocode =
                cachedAreaName == null ||
                    lastGeocodedLat == null ||
                    lastGeocodedLng == null ||
                    GeoMath.calculateDistanceMeters(
                        lastGeocodedLat!!,
                        lastGeocodedLng!!,
                        location.latitude,
                        location.longitude
                    ) > 180f

            val initialModel = UserLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = if (location.hasAccuracy()) location.accuracy else 25f,
                isPrecise = isPrecise,
                areaName = cachedAreaName,
                updatedAtMillis = System.currentTimeMillis()
            )
            trySend(initialModel)

            if (shouldGeocode) {
                lastGeocodedLat = location.latitude
                lastGeocodedLng = location.longitude
                launch {
                    val resolvedArea = reverseGeocodeArea(location.latitude, location.longitude)
                    if (!resolvedArea.isNullOrBlank()) {
                        cachedAreaName = resolvedArea
                        trySend(initialModel.copy(areaName = resolvedArea))
                    }
                }
            }
        }

        // Immediate last known fix for fast map centering on launch
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
            if (lastLoc != null) {
                emitLocation(lastLoc)
            }
        }

        val priority = if (isPrecise) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val locationRequest = LocationRequest.Builder(priority, 4000L)
            .setMinUpdateIntervalMillis(2000L)
            .setMinUpdateDistanceMeters(3f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let(::emitLocation)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    private suspend fun reverseGeocodeArea(latitude: Double, longitude: Double): String? {
        if (!Geocoder.isPresent()) return null
        return runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            val address: Address? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        if (cont.isActive) {
                            cont.resume(addresses.firstOrNull())
                        }
                    }
                }
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                }
            }
            address?.let { addr ->
                val parts = listOfNotNull(
                    addr.subLocality?.takeIf { it.isNotBlank() },
                    addr.locality?.takeIf { it.isNotBlank() }
                        ?: addr.subAdminArea?.takeIf { it.isNotBlank() }
                ).distinct()
                parts.joinToString(", ").takeIf { it.isNotBlank() }
            }
        }.getOrNull()
    }
}
