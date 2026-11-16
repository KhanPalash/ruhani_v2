package com.khanscreations.ruhani.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.khanscreations.ruhani.R
import com.khanscreations.ruhani.data.model.GeoMath
import com.khanscreations.ruhani.data.model.Mosque
import com.khanscreations.ruhani.data.model.MosqueSearchState
import com.khanscreations.ruhani.data.model.OsmMapLayer
import com.khanscreations.ruhani.data.model.QiblaCompassState
import com.khanscreations.ruhani.data.model.SearchRadius
import com.khanscreations.ruhani.data.model.UserLocation
import com.khanscreations.ruhani.ui.MosqueFinderUiState
import com.khanscreations.ruhani.ui.theme.MasjidBrassSecondary
import com.khanscreations.ruhani.ui.theme.MasjidEmeraldPrimary
import com.khanscreations.ruhani.ui.theme.MasjidLivePulseGreen
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

private val OpenTopoTileSource = XYTileSource(
    "OpenTopoMap",
    0,
    17,
    256,
    ".png",
    arrayOf(
        "https://a.tile.opentopomap.org/",
        "https://b.tile.opentopomap.org/",
        "https://c.tile.opentopomap.org/"
    ),
    "© OpenStreetMap contributors | © OpenTopoMap"
)

private fun createMinimalLightColorFilter(): ColorMatrixColorFilter {
    val saturationMatrix = ColorMatrix().apply {
        setSaturation(0.20f)
    }
    val softWarmTint = ColorMatrix(
        floatArrayOf(
            0.96f, 0f, 0f, 0f, 12f,
            0f, 0.96f, 0f, 0f, 11f,
            0f, 0f, 0.93f, 0f, 8f,
            0f, 0f, 0f, 1f, 0f
        )
    )
    saturationMatrix.postConcat(softWarmTint)
    return ColorMatrixColorFilter(saturationMatrix)
}

@SuppressLint("ClickableViewAccessibility")
@Composable
fun OsmMosqueMapCanvas(
    uiState: MosqueFinderUiState,
    onMapMovedByUser: (Double, Double) -> Unit,
    onMapCenterChanged: (Double, Double) -> Unit,
    onSelectMosque: (Mosque?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    remember {
        val osmBaseDir = File(context.cacheDir, "osmdroid").apply { mkdirs() }
        val osmTileDir = File(osmBaseDir, "tiles").apply { mkdirs() }
        // Clean up any stale CARTO watermarked tile cache from earlier builds
        File(osmTileDir, "CartoDBPositron").takeIf { it.exists() }?.deleteRecursively()

        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE))
            osmdroidBasePath = osmBaseDir
            osmdroidTileCache = osmTileDir
            userAgentValue = "MasjidNear/1.0 (${context.packageName})"
        }
        true
    }

    val minimalLightFilter = remember { createMinimalLightColorFilter() }
    val loadingBgColor = MaterialTheme.colorScheme.surfaceContainerLow.toArgb()
    val loadingGridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setUseDataConnection(true)
            overlayManager.tilesOverlay.apply {
                setColorFilter(minimalLightFilter)
                loadingBackgroundColor = loadingBgColor
                loadingLineColor = loadingGridColor
            }
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
            minZoomLevel = 3.0
            maxZoomLevel = 19.0
            controller.setZoom(14.5)
        }
    }

    var isUserInteracting by remember { mutableStateOf(false) }
    var hasInitializedCamera by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        mapView.setOnTouchListener { _, motionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    isUserInteracting = true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isUserInteracting) {
                        val center = mapView.mapCenter
                        onMapCenterChanged(center.latitude, center.longitude)
                        onMapMovedByUser(center.latitude, center.longitude)
                    }
                    isUserInteracting = false
                }
            }
            false
        }

        val mapListener = object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                val center = mapView.mapCenter
                onMapCenterChanged(center.latitude, center.longitude)
                if (isUserInteracting) {
                    onMapMovedByUser(center.latitude, center.longitude)
                }
                return false
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                val center = mapView.mapCenter
                onMapCenterChanged(center.latitude, center.longitude)
                return false
            }
        }
        mapView.addMapListener(mapListener)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.removeMapListener(mapListener)
            mapView.onDetach()
        }
    }

    // Switch tile source and minimal light ColorFilter when map layer changes
    LaunchedEffect(uiState.mapLayer) {
        when (uiState.mapLayer) {
            OsmMapLayer.MINIMAL_LIGHT -> {
                mapView.setTileSource(TileSourceFactory.MAPNIK)
                mapView.overlayManager.tilesOverlay.setColorFilter(minimalLightFilter)
            }
            OsmMapLayer.STANDARD_OSM -> {
                mapView.setTileSource(TileSourceFactory.MAPNIK)
                mapView.overlayManager.tilesOverlay.setColorFilter(null)
            }
            OsmMapLayer.TERRAIN -> {
                mapView.setTileSource(OpenTopoTileSource)
                mapView.overlayManager.tilesOverlay.setColorFilter(null)
            }
        }
        mapView.invalidate()
    }

    val liveLoc = uiState.liveLocation
    val mosques = (uiState.searchState as? MosqueSearchState.Success)?.mosques.orEmpty()

    // Center smoothly on live GPS / search center and fit nearest discovered mosques
    LaunchedEffect(
        liveLoc?.latitude,
        liveLoc?.longitude,
        uiState.searchCenterLat,
        uiState.searchCenterLng,
        uiState.followUserLocation,
        uiState.selectedRadius,
        mosques.firstOrNull()?.id,
        mosques.size
    ) {
        val targetLat = if (uiState.followUserLocation) {
            liveLoc?.latitude ?: uiState.searchCenterLat
        } else {
            uiState.searchCenterLat ?: liveLoc?.latitude
        }
        val targetLng = if (uiState.followUserLocation) {
            liveLoc?.longitude ?: uiState.searchCenterLng
        } else {
            uiState.searchCenterLng ?: liveLoc?.longitude
        }

        if (targetLat != null && targetLng != null && uiState.selectedMosque == null) {
            val targetPoint = GeoPoint(targetLat, targetLng)
            val targetZoom = zoomForRadiusAndMosques(uiState.selectedRadius, mosques)
            if (!hasInitializedCamera) {
                hasInitializedCamera = true
                mapView.controller.setZoom(targetZoom)
                mapView.controller.setCenter(targetPoint)
            } else if (uiState.followUserLocation || mosques.isNotEmpty()) {
                mapView.controller.setZoom(targetZoom)
                mapView.controller.animateTo(targetPoint)
            }
        }
    }

    // Focus camera on selected mosque
    LaunchedEffect(uiState.selectedMosque?.id) {
        val mosque = uiState.selectedMosque ?: return@LaunchedEffect
        mapView.controller.animateTo(
            GeoPoint(mosque.latitude, mosque.longitude),
            16.2,
            500L
        )
    }

    // Redraw overlays through OverlayManager whenever location, search center, radius, mosques, or selection changes
    LaunchedEffect(
        liveLoc?.latitude,
        liveLoc?.longitude,
        uiState.searchCenterLat,
        uiState.searchCenterLng,
        uiState.selectedRadius,
        mosques,
        uiState.selectedMosque?.id
    ) {
        syncMosqueOverlaysToOverlayManager(
            mapView = mapView,
            context = context,
            liveLocation = liveLoc,
            searchCenterLat = uiState.searchCenterLat,
            searchCenterLng = uiState.searchCenterLng,
            radiusMeters = uiState.selectedRadius.meters,
            mosques = mosques,
            selectedMosqueId = uiState.selectedMosque?.id,
            onSelectMosque = onSelectMosque
        )
    }

    AndroidView(
        factory = { mapView },
        update = { view ->
            syncMosqueOverlaysToOverlayManager(
                mapView = view,
                context = context,
                liveLocation = liveLoc,
                searchCenterLat = uiState.searchCenterLat,
                searchCenterLng = uiState.searchCenterLng,
                radiusMeters = uiState.selectedRadius.meters,
                mosques = mosques,
                selectedMosqueId = uiState.selectedMosque?.id,
                onSelectMosque = onSelectMosque
            )
        },
        modifier = modifier.fillMaxSize()
    )
}

@Composable
fun LiveLocationHeaderOverlay(
    liveLocation: UserLocation?,
    qiblaCompass: QiblaCompassState,
    selectedRadius: SearchRadius,
    showSearchThisArea: Boolean,
    isLoading: Boolean,
    onRadiusSelected: (SearchRadius) -> Unit,
    onRefreshClick: () -> Unit,
    onQiblaClick: () -> Unit,
    onSearchThisAreaClick: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 6.dp,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("nearby_mosques_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                LivePulseDot()
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (liveLocation != null) {
                                stringResource(R.string.live_location_active)
                            } else {
                                stringResource(R.string.locating_you)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (liveLocation != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.accuracy_format,
                                        liveLocation.accuracyMeters.roundToInt()
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    val locationLine = when {
                        liveLocation?.areaName != null -> liveLocation.areaName
                        liveLocation != null -> String.format(
                            Locale.US,
                            "%.5f, %.5f",
                            liveLocation.latitude,
                            liveLocation.longitude
                        )
                        else -> "Waiting for GPS signal…"
                    }
                    Text(
                        text = locationLine,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Interactive Live Qibla Compass Pill
                val qiblaBearing = qiblaCompass.qiblaBearingDegrees
                if (qiblaBearing != null) {
                    val targetAngle = qiblaCompass.relativeQiblaDegrees ?: qiblaBearing
                    val animatedNeedleAngle by animateFloatAsState(
                        targetValue = targetAngle,
                        animationSpec = tween(durationMillis = 180),
                        label = "qibla_pill_needle"
                    )
                    val isAligned = qiblaCompass.isAlignedToQibla
                    Surface(
                        onClick = onQiblaClick,
                        color = if (isAligned) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("qibla_compass_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Navigation,
                                contentDescription = stringResource(R.string.qibla_label),
                                tint = if (isAligned) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                },
                                modifier = Modifier
                                    .size(16.dp)
                                    .rotate(animatedNeedleAngle)
                            )
                            Text(
                                text = "${qiblaBearing.roundToInt()}° ${
                                    GeoMath.compassCardinal(qiblaBearing)
                                }",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAligned) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))
                IconButton(
                    onClick = onRefreshClick,
                    enabled = !isLoading && liveLocation != null,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("refresh_mosques_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.refresh_mosques),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchRadius.entries.forEach { radius ->
                val selected = radius == selectedRadius
                FilterChip(
                    selected = selected,
                    onClick = { onRadiusSelected(radius) },
                    label = {
                        Text(
                            text = radius.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.testTag("radius_chip_${radius.meters}")
                )
            }
        }

        AnimatedVisibility(
            visible = showSearchThisArea,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Surface(
                onClick = onSearchThisAreaClick,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 6.dp,
                modifier = Modifier.testTag("search_this_area_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.search_this_area),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun MapControlsColumn(
    followUserLocation: Boolean,
    mapLayer: OsmMapLayer,
    onRecenterClick: () -> Unit,
    onCycleMapLayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End
    ) {
        SmallFloatingActionButton(
            onClick = onCycleMapLayer,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(48.dp)
                .testTag("map_layer_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.Layers,
                contentDescription = stringResource(R.string.toggle_map_type)
            )
        }

        SmallFloatingActionButton(
            onClick = onRecenterClick,
            containerColor = if (followUserLocation) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            },
            contentColor = if (followUserLocation) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier
                .size(48.dp)
                .testTag("recenter_location_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.MyLocation,
                contentDescription = stringResource(R.string.recenter_location)
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 2.dp
        ) {
            Text(
                text = when (mapLayer) {
                    OsmMapLayer.MINIMAL_LIGHT -> stringResource(R.string.map_layer_light)
                    OsmMapLayer.STANDARD_OSM -> stringResource(R.string.map_layer_streets)
                    OsmMapLayer.TERRAIN -> stringResource(R.string.map_layer_terrain)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun LivePulseDot() {
    val transition = rememberInfiniteTransition(label = "live_gps_pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(18.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(MasjidLivePulseGreen.copy(alpha = pulseAlpha))
        )
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(MasjidLivePulseGreen)
        )
    }
}

private fun createLiveLocationDrawable(context: Context): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (36 * density).roundToInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val cx = sizePx / 2f
    val cy = sizePx / 2f

    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MasjidEmeraldPrimary.copy(alpha = 0.24f).toArgb()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, sizePx * 0.46f, haloPaint)

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, sizePx * 0.28f, borderPaint)

    val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MasjidLivePulseGreen.toArgb()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, sizePx * 0.20f, corePaint)

    return BitmapDrawable(context.resources, bitmap)
}

internal fun syncMosqueOverlaysToOverlayManager(
    mapView: MapView,
    context: Context,
    liveLocation: UserLocation?,
    searchCenterLat: Double?,
    searchCenterLng: Double?,
    radiusMeters: Int,
    mosques: List<Mosque>,
    selectedMosqueId: String?,
    onSelectMosque: (Mosque?) -> Unit
) {
    val overlayManager = mapView.overlayManager
    overlayManager.clear()

    // 1. Search radius circle overlay (expands visually if nearest mosque was auto-discovered beyond chip radius)
    val circleLat = searchCenterLat ?: liveLocation?.latitude
    val circleLng = searchCenterLng ?: liveLocation?.longitude
    if (circleLat != null && circleLng != null) {
        val nearestMosqueMeters = mosques.firstOrNull()?.distanceMeters?.toDouble() ?: 0.0
        val effectiveCircleMeters = maxOf(radiusMeters.toDouble(), nearestMosqueMeters * 1.08)
        val radiusCircle = Polygon(mapView).apply {
            id = "search_radius_circle"
            points = Polygon.pointsAsCircle(
                GeoPoint(circleLat, circleLng),
                effectiveCircleMeters
            )
            fillPaint.color = MasjidEmeraldPrimary.copy(alpha = 0.08f).toArgb()
            outlinePaint.color = MasjidEmeraldPrimary.copy(alpha = 0.45f).toArgb()
            outlinePaint.strokeWidth = 3f
            setOnClickListener { _, _, _ ->
                if (selectedMosqueId != null) {
                    onSelectMosque(null)
                }
                false
            }
        }
        overlayManager.add(radiusCircle)
    }

    // 2. Live User Location marker overlay
    if (liveLocation != null) {
        val userMarker = Marker(mapView).apply {
            id = "user_live_location"
            title = "Your Live Location"
            position = GeoPoint(liveLocation.latitude, liveLocation.longitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            icon = createLiveLocationDrawable(context)
            setInfoWindow(null)
            setOnMarkerClickListener { _, _ -> false }
        }
        overlayManager.add(userMarker)
    }

    // 3. Individual Mosque Marker objects (selected drawn last so it sits on top)
    val orderedMosques = mosques.sortedBy { it.id == selectedMosqueId }
    orderedMosques.forEach { mosque ->
        val isSelected = selectedMosqueId == mosque.id
        val mosqueMarker = Marker(mapView).apply {
            id = mosque.id
            title = mosque.name
            snippet = "${GeoMath.formatDistance(mosque.distanceMeters)} • ${mosque.address}"
            relatedObject = mosque
            position = GeoPoint(mosque.latitude, mosque.longitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = createMosquePinDrawable(
                context = context,
                distanceText = GeoMath.formatDistance(mosque.distanceMeters),
                isSelected = isSelected
            )
            setInfoWindow(null)
            setOnMarkerClickListener { _, _ ->
                onSelectMosque(mosque)
                true
            }
        }
        overlayManager.add(mosqueMarker)
    }

    mapView.invalidate()
    mapView.postInvalidate()
}

private fun createMosquePinDrawable(
    context: Context,
    distanceText: String,
    isSelected: Boolean
): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val textSizePx = (if (isSelected) 12.5f else 11f) * density
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = textSizePx
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val iconSize = (if (isSelected) 16f else 14f) * density
    val hPad = (if (isSelected) 10f else 8f) * density
    val gap = 5f * density
    val textWidth = textPaint.measureText(distanceText)
    val pillWidth = hPad + iconSize + gap + textWidth + hPad
    val pillHeight = (if (isSelected) 32f else 28f) * density
    val pointerHeight = 7f * density
    val strokeWidth = (if (isSelected) 2.2f else 1.5f) * density

    val totalW = (pillWidth + strokeWidth * 2).roundToInt().coerceAtLeast(1)
    val totalH = (pillHeight + pointerHeight + strokeWidth * 2).roundToInt().coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgColor = if (isSelected) MasjidBrassSecondary.toArgb() else MasjidEmeraldPrimary.toArgb()
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bgColor
        style = Paint.Style.FILL
    }
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
    }

    val rect = RectF(
        strokeWidth,
        strokeWidth,
        strokeWidth + pillWidth,
        strokeWidth + pillHeight
    )
    val corner = pillHeight / 2f
    canvas.drawRoundRect(rect, corner, corner, fillPaint)
    canvas.drawRoundRect(rect, corner, corner, borderPaint)

    // Pointer triangle
    val cx = totalW / 2f
    val tipY = strokeWidth + pillHeight + pointerHeight
    val pointerPath = Path().apply {
        moveTo(cx - 5f * density, strokeWidth + pillHeight - 1f)
        lineTo(cx, tipY)
        lineTo(cx + 5f * density, strokeWidth + pillHeight - 1f)
        close()
    }
    canvas.drawPath(pointerPath, fillPaint)

    // Custom vector mosque icon inside the pin pill
    val iconLeft = (strokeWidth + hPad).roundToInt()
    val iconTop = (strokeWidth + (pillHeight - iconSize) / 2f).roundToInt()
    val iconSizePx = iconSize.roundToInt().coerceAtLeast(1)
    val mosqueDrawable = ContextCompat.getDrawable(context, R.drawable.ic_mosque_marker)?.mutate()
    if (mosqueDrawable != null) {
        mosqueDrawable.setBounds(
            iconLeft,
            iconTop,
            iconLeft + iconSizePx,
            iconTop + iconSizePx
        )
        mosqueDrawable.draw(canvas)
    }

    // Distance text
    val textX = strokeWidth + hPad + iconSize + gap
    val fontMetrics = textPaint.fontMetrics
    val textY = strokeWidth + pillHeight / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f
    canvas.drawText(distanceText, textX, textY, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}

private fun zoomForRadiusAndMosques(
    radius: SearchRadius,
    mosques: List<Mosque>
): Double {
    val thirdClosestMeters = mosques.take(3).lastOrNull()?.distanceMeters ?: 0f
    val effectiveMeters = maxOf(radius.meters.toFloat(), thirdClosestMeters * 1.15f)
    return when {
        effectiveMeters <= 1200f -> 15.2
        effectiveMeters <= 3500f -> 13.8
        effectiveMeters <= 6000f -> 12.9
        effectiveMeters <= 11500f -> 11.9
        effectiveMeters <= 16500f -> 11.3
        else -> 10.8
    }
}
