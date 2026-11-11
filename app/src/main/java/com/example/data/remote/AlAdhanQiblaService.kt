package com.example.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class AlAdhanQiblaResult(
    val latitude: Double,
    val longitude: Double,
    val directionDegrees: Double,
    val status: String,
    val httpCode: Int,
    val compassBitmap: ImageBitmap?,
    val compassEndpointUrl: String,
    val directionEndpointUrl: String,
    val imageSizePx: Int
)

object AlAdhanQiblaService {

    private const val BASE_URL = "https://api.aladhan.com/v1/qibla"
    private val memoryCache = ConcurrentHashMap<String, AlAdhanQiblaResult>()

    fun buildDirectionUrl(latitude: Double, longitude: Double): String {
        val latStr = String.format(Locale.US, "%.6f", latitude)
        val lngStr = String.format(Locale.US, "%.6f", longitude)
        return "$BASE_URL/$latStr/$lngStr"
    }

    fun buildCompassImageUrl(latitude: Double, longitude: Double, size: Int = 600): String {
        val clampedSize = size.coerceIn(200, 2000)
        val latStr = String.format(Locale.US, "%.6f", latitude)
        val lngStr = String.format(Locale.US, "%.6f", longitude)
        return "$BASE_URL/$latStr/$lngStr/compass/$clampedSize"
    }

    suspend fun fetchAlAdhanQibla(
        latitude: Double,
        longitude: Double,
        size: Int = 600,
        forceRefresh: Boolean = false
    ): Result<AlAdhanQiblaResult> = withContext(Dispatchers.IO) {
        val clampedSize = size.coerceIn(200, 2000)
        val cacheKey = String.format(Locale.US, "%.4f_%.4f_%d", latitude, longitude, clampedSize)
        if (!forceRefresh) {
            memoryCache[cacheKey]?.let { return@withContext Result.success(it) }
        }

        val directionUrl = buildDirectionUrl(latitude, longitude)
        val compassUrl = buildCompassImageUrl(latitude, longitude, clampedSize)

        runCatching {
            coroutineScope {
                val directionDeferred = async { fetchDirectionJson(directionUrl) }
                val bitmapDeferred = async { fetchCompassBitmap(compassUrl) }

                val (code, status, directionDeg, resolvedLat, resolvedLng) = directionDeferred.await()
                val bitmap = bitmapDeferred.await()

                val result = AlAdhanQiblaResult(
                    latitude = resolvedLat,
                    longitude = resolvedLng,
                    directionDegrees = directionDeg,
                    status = status,
                    httpCode = code,
                    compassBitmap = bitmap?.asImageBitmap(),
                    compassEndpointUrl = compassUrl,
                    directionEndpointUrl = directionUrl,
                    imageSizePx = clampedSize
                )
                if (bitmap != null) {
                    memoryCache[cacheKey] = result
                }
                result
            }
        }
    }

    private data class DirectionJsonPayload(
        val code: Int,
        val status: String,
        val direction: Double,
        val latitude: Double,
        val longitude: Double
    )

    private fun fetchDirectionJson(urlString: String): DirectionJsonPayload {
        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Ruhani-Android/1.0")
        }
        return try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            val root = JSONObject(body)
            val code = root.optInt("code", responseCode)
            val status = root.optString("status", "OK")
            val data = root.optJSONObject("data")
            val direction = data?.optDouble("direction", 277.51) ?: 277.51
            val lat = data?.optDouble("latitude", 23.8103) ?: 23.8103
            val lng = data?.optDouble("longitude", 90.4125) ?: 90.4125
            DirectionJsonPayload(code, status, direction, lat, lng)
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchCompassBitmap(urlString: String): Bitmap? {
        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 12_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "image/png,image/*;q=0.8")
            setRequestProperty("User-Agent", "Ruhani-Android/1.0")
        }
        return try {
            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                connection.inputStream.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }
}
