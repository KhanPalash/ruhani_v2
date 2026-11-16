package com.khanscreations.ruhani.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.khanscreations.ruhani.data.model.GeoMath
import com.khanscreations.ruhani.data.model.Mosque
import com.khanscreations.ruhani.data.model.MosqueSearchState
import com.khanscreations.ruhani.data.model.OsmMapLayer
import com.khanscreations.ruhani.data.model.QiblaCompassState
import com.khanscreations.ruhani.data.model.SearchRadius
import com.khanscreations.ruhani.data.model.UserLocation
import com.khanscreations.ruhani.data.repository.CompassRepository
import com.khanscreations.ruhani.data.repository.LocationRepository
import com.khanscreations.ruhani.data.repository.MosqueRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MosqueFinderUiState(
    val hasLocationPermission: Boolean = false,
    val permissionRequestedOnce: Boolean = false,
    val liveLocation: UserLocation? = null,
    val qiblaCompass: QiblaCompassState = QiblaCompassState(),
    val selectedRadius: SearchRadius = SearchRadius.THREE_KM,
    val searchCenterLat: Double? = null,
    val searchCenterLng: Double? = null,
    val searchState: MosqueSearchState = MosqueSearchState.Idle,
    val selectedMosque: Mosque? = null,
    val followUserLocation: Boolean = true,
    val mapLayer: OsmMapLayer = OsmMapLayer.MINIMAL_LIGHT,
    val isSheetExpanded: Boolean = false,
    val isQiblaDialOpen: Boolean = false,
    val showSearchThisArea: Boolean = false
)

class MosqueFinderViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepository = LocationRepository(application.applicationContext)
    private val compassRepository = CompassRepository(application.applicationContext)
    private val mosqueRepository = MosqueRepository()

    private val _uiState = MutableStateFlow(
        MosqueFinderUiState(
            hasLocationPermission = locationRepository.hasAnyLocationPermission()
        )
    )
    val uiState: StateFlow<MosqueFinderUiState> = _uiState.asStateFlow()

    private var locationCollectionJob: Job? = null
    private var compassCollectionJob: Job? = null
    private var searchJob: Job? = null
    private var isScreenActive = false
    private var hasPerformedInitialSearch = false
    private var lastAutoSearchedLat: Double? = null
    private var lastAutoSearchedLng: Double? = null

    fun setScreenActive(active: Boolean) {
        if (isScreenActive == active) return
        isScreenActive = active
        if (active) {
            val granted = locationRepository.hasAnyLocationPermission()
            _uiState.update { it.copy(hasLocationPermission = granted) }
            if (granted) {
                startLiveLocationUpdates()
                startCompassUpdates()
            }
        } else {
            locationCollectionJob?.cancel()
            locationCollectionJob = null
            compassCollectionJob?.cancel()
            compassCollectionJob = null
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                hasLocationPermission = granted,
                permissionRequestedOnce = true
            )
        }
        if (granted && isScreenActive) {
            startLiveLocationUpdates()
            startCompassUpdates()
        }
    }

    fun refreshPermissionState() {
        val granted = locationRepository.hasAnyLocationPermission()
        if (granted != _uiState.value.hasLocationPermission) {
            _uiState.update { it.copy(hasLocationPermission = granted) }
            if (granted && isScreenActive) {
                startLiveLocationUpdates()
                startCompassUpdates()
            } else {
                locationCollectionJob?.cancel()
                locationCollectionJob = null
                compassCollectionJob?.cancel()
                compassCollectionJob = null
            }
        }
    }

    private fun startLiveLocationUpdates() {
        if (locationCollectionJob?.isActive == true) return
        locationCollectionJob = viewModelScope.launch {
            locationRepository.observeLiveLocation().collect { userLoc ->
                val qibla = GeoMath.calculateQiblaBearingDegrees(
                    fromLat = userLoc.latitude,
                    fromLng = userLoc.longitude
                )

                _uiState.update { current ->
                    val updatedSearchState = when (val state = current.searchState) {
                        is MosqueSearchState.Success -> {
                            val recalculated = state.mosques.map { mosque ->
                                mosque.copy(
                                    distanceMeters = GeoMath.calculateDistanceMeters(
                                        fromLat = userLoc.latitude,
                                        fromLng = userLoc.longitude,
                                        toLat = mosque.latitude,
                                        toLng = mosque.longitude
                                    )
                                )
                            }.sortedBy { it.distanceMeters }
                            MosqueSearchState.Success(recalculated)
                        }
                        else -> state
                    }

                    val updatedSelected = current.selectedMosque?.let { sel ->
                        sel.copy(
                            distanceMeters = GeoMath.calculateDistanceMeters(
                                fromLat = userLoc.latitude,
                                fromLng = userLoc.longitude,
                                toLat = sel.latitude,
                                toLng = sel.longitude
                            )
                        )
                    }

                    current.copy(
                        liveLocation = userLoc,
                        qiblaCompass = current.qiblaCompass.copy(qiblaBearingDegrees = qibla),
                        searchState = updatedSearchState,
                        selectedMosque = updatedSelected
                    )
                }

                val shouldSearchAtUpdatedLocation = !hasPerformedInitialSearch || (
                    _uiState.value.followUserLocation &&
                        lastAutoSearchedLat != null &&
                        lastAutoSearchedLng != null &&
                        GeoMath.calculateDistanceMeters(
                            lastAutoSearchedLat!!,
                            lastAutoSearchedLng!!,
                            userLoc.latitude,
                            userLoc.longitude
                        ) > 350f
                    )

                if (shouldSearchAtUpdatedLocation) {
                    hasPerformedInitialSearch = true
                    lastAutoSearchedLat = userLoc.latitude
                    lastAutoSearchedLng = userLoc.longitude
                    searchMosquesAt(userLoc.latitude, userLoc.longitude)
                }
            }
        }
    }

    private fun startCompassUpdates() {
        if (compassCollectionJob?.isActive == true) return
        compassCollectionJob = viewModelScope.launch {
            compassRepository.observeDeviceAzimuth().collect { reading ->
                _uiState.update { current ->
                    current.copy(
                        qiblaCompass = current.qiblaCompass.copy(
                            deviceAzimuthDegrees = reading.azimuthDegrees,
                            hasCompassSensor = reading.hasSensor
                        )
                    )
                }
            }
        }
    }

    fun onRadiusSelected(radius: SearchRadius) {
        if (_uiState.value.selectedRadius == radius) return
        _uiState.update { it.copy(selectedRadius = radius) }
        val centerLat = _uiState.value.searchCenterLat ?: _uiState.value.liveLocation?.latitude
        val centerLng = _uiState.value.searchCenterLng ?: _uiState.value.liveLocation?.longitude
        if (centerLat != null && centerLng != null) {
            searchMosquesAt(centerLat, centerLng)
        }
    }

    fun expandToMaxRadius() {
        onRadiusSelected(SearchRadius.TWENTY_KM)
    }

    fun refreshMosquesNearLiveLocation() {
        val loc = _uiState.value.liveLocation ?: return
        _uiState.update {
            it.copy(
                followUserLocation = true,
                showSearchThisArea = false
            )
        }
        searchMosquesAt(loc.latitude, loc.longitude, forceRefresh = true)
    }

    fun searchThisMapArea(centerLat: Double, centerLng: Double) {
        _uiState.update {
            it.copy(
                followUserLocation = false,
                showSearchThisArea = false
            )
        }
        searchMosquesAt(centerLat, centerLng, forceRefresh = true)
    }

    fun onMapCameraMovedByUser(centerLat: Double, centerLng: Double) {
        val refLat = _uiState.value.searchCenterLat ?: _uiState.value.liveLocation?.latitude
        val refLng = _uiState.value.searchCenterLng ?: _uiState.value.liveLocation?.longitude
        val distanceMoved = if (refLat != null && refLng != null) {
            GeoMath.calculateDistanceMeters(refLat, refLng, centerLat, centerLng)
        } else {
            0f
        }
        // Ignore single taps or small touch jitter (< 180m) so auto-follow and auto-zoom stay active
        if (distanceMoved < 180f) return

        _uiState.update {
            it.copy(
                followUserLocation = false,
                showSearchThisArea = distanceMoved > 650f
            )
        }
    }

    fun onRecenterToLiveLocation() {
        _uiState.update {
            it.copy(
                followUserLocation = true,
                selectedMosque = null
            )
        }
    }

    fun onSelectMosque(mosque: Mosque?) {
        _uiState.update {
            it.copy(
                selectedMosque = mosque,
                followUserLocation = if (mosque != null) false else it.followUserLocation
            )
        }
    }

    fun onToggleSheetExpanded(expanded: Boolean? = null) {
        _uiState.update {
            it.copy(isSheetExpanded = expanded ?: !it.isSheetExpanded)
        }
    }

    fun onToggleQiblaDial(open: Boolean? = null) {
        _uiState.update {
            it.copy(isQiblaDialOpen = open ?: !it.isQiblaDialOpen)
        }
    }

    fun onCycleMapLayer() {
        _uiState.update { current ->
            val next = when (current.mapLayer) {
                OsmMapLayer.MINIMAL_LIGHT -> OsmMapLayer.STANDARD_OSM
                OsmMapLayer.STANDARD_OSM -> OsmMapLayer.TERRAIN
                OsmMapLayer.TERRAIN -> OsmMapLayer.MINIMAL_LIGHT
            }
            current.copy(mapLayer = next)
        }
    }

    private fun searchMosquesAt(
        centerLat: Double,
        centerLng: Double,
        forceRefresh: Boolean = false
    ) {
        val userLat = _uiState.value.liveLocation?.latitude ?: centerLat
        val userLng = _uiState.value.liveLocation?.longitude ?: centerLng
        val radiusMeters = _uiState.value.selectedRadius.meters

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    searchCenterLat = centerLat,
                    searchCenterLng = centerLng,
                    searchState = MosqueSearchState.Loading
                )
            }
            val result = mosqueRepository.searchNearbyMosques(
                searchCenterLat = centerLat,
                searchCenterLng = centerLng,
                userLat = userLat,
                userLng = userLng,
                radiusMeters = radiusMeters,
                forceRefresh = forceRefresh
            )
            _uiState.update { current ->
                val keptSelection = if (result is MosqueSearchState.Success) {
                    current.selectedMosque?.let { sel ->
                        result.mosques.find { it.id == sel.id }
                    }
                } else {
                    null
                }
                current.copy(
                    searchState = result,
                    selectedMosque = keptSelection
                )
            }
        }
    }
}
