package com.khanscreations.ruhani.data.remote

import com.squareup.moshi.JsonClass
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface OverpassApiService {
    @FormUrlEncoded
    @POST
    suspend fun queryNearbyMosques(
        @Url url: String,
        @Field("data") query: String,
        @Header("User-Agent") userAgent: String = USER_AGENT
    ): OverpassResponse

    @GET
    suspend fun searchNominatim(
        @Url url: String = NOMINATIM_URL,
        @Query("q") query: String,
        @Query("viewbox") viewbox: String,
        @Query("bounded") bounded: Int = 1,
        @Query("format") format: String = "jsonv2",
        @Query("extratags") extratags: Int = 1,
        @Query("addressdetails") addressdetails: Int = 1,
        @Query("limit") limit: Int = 50,
        @Header("User-Agent") userAgent: String = USER_AGENT
    ): List<NominatimPlace>

    @GET
    suspend fun searchPhotonMosques(
        @Url url: String = PHOTON_URL,
        @Query("q") query: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int = 20,
        @Query("osm_tag") osmTag: String? = null,
        @Header("User-Agent") userAgent: String = USER_AGENT
    ): PhotonResponse

    @GET
    suspend fun searchArcGisMosques(
        @Url url: String = ARCGIS_GEOCODE_URL,
        @Query("f") format: String = "json",
        @Query("category") category: String = "Mosque",
        @Query("location") locationLngLat: String,
        @Query("maxLocations") maxLocations: Int = 25,
        @Query("outFields") outFields: String = "PlaceName,Place_addr,Type",
        @Header("User-Agent") userAgent: String = USER_AGENT
    ): ArcGisGeocodeResponse = ArcGisGeocodeResponse(emptyList())

    companion object {
        const val PRIMARY_URL = "https://overpass-api.de/api/interpreter"
        const val FALLBACK_URL = "https://overpass.private.coffee/api/interpreter"
        const val NOMINATIM_URL = "https://nominatim.openstreetmap.org/search"
        const val PHOTON_URL = "https://photon.komoot.io/api"
        const val ARCGIS_GEOCODE_URL =
            "https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer/findAddressCandidates"
        const val USER_AGENT = "MasjidNear-Android/1.0 (OpenStreetMap Mosque Finder)"
    }
}

@JsonClass(generateAdapter = true)
data class OverpassResponse(
    val elements: List<OverpassElement>? = null,
    val remark: String? = null
)

@JsonClass(generateAdapter = true)
data class OverpassElement(
    val type: String = "",
    val id: Long = 0L,
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OverpassCenter? = null,
    val tags: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class OverpassCenter(
    val lat: Double,
    val lon: Double
)

@JsonClass(generateAdapter = true)
data class NominatimPlace(
    val place_id: Long = 0L,
    val osm_type: String? = null,
    val osm_id: Long? = null,
    val lat: String? = null,
    val lon: String? = null,
    val name: String? = null,
    val display_name: String? = null,
    val category: String? = null,
    val type: String? = null,
    val extratags: Map<String, String>? = null,
    val address: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class PhotonResponse(
    val features: List<PhotonFeature>? = null
)

@JsonClass(generateAdapter = true)
data class PhotonFeature(
    val geometry: PhotonGeometry? = null,
    val properties: PhotonProperties? = null
)

@JsonClass(generateAdapter = true)
data class PhotonGeometry(
    val coordinates: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class PhotonProperties(
    val osm_id: Long? = null,
    val osm_type: String? = null,
    val name: String? = null,
    val street: String? = null,
    val housenumber: String? = null,
    val district: String? = null,
    val city: String? = null,
    val state: String? = null,
    val osm_key: String? = null,
    val osm_value: String? = null
)

@JsonClass(generateAdapter = true)
data class ArcGisGeocodeResponse(
    val candidates: List<ArcGisCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class ArcGisCandidate(
    val address: String? = null,
    val location: ArcGisLocation? = null,
    val attributes: Map<String, Any?>? = null
)

@JsonClass(generateAdapter = true)
data class ArcGisLocation(
    val x: Double? = null,
    val y: Double? = null
)
