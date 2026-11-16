package com.khanscreations.ruhani.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khanscreations.ruhani.R
import com.khanscreations.ruhani.data.model.MosqueSearchState
import com.khanscreations.ruhani.ui.MosqueFinderViewModel
import com.khanscreations.ruhani.ui.components.LiveLocationHeaderOverlay
import com.khanscreations.ruhani.ui.components.MapControlsColumn
import com.khanscreations.ruhani.ui.components.NearbyMosquesPanel
import com.khanscreations.ruhani.ui.components.OsmMosqueMapCanvas
import com.khanscreations.ruhani.ui.components.QiblaCompassDialog

@Composable
fun MosqueFinderScreen(
    onBack: (() -> Unit)? = null,
    applySystemBarInsets: Boolean = false,
    viewModel: MosqueFinderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fineGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(fineGranted || coarseGranted)
    }

    LaunchedEffect(Unit) {
        if (!uiState.hasLocationPermission && !uiState.permissionRequestedOnce) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        viewModel.setScreenActive(true)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.setScreenActive(false)
        }
    }

    val topSystemPadding = if (applySystemBarInsets) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }
    val bottomSystemPadding = if (applySystemBarInsets) {
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    } else {
        0.dp
    }

    if (!uiState.hasLocationPermission) {
        LocationPermissionScreen(
            permissionRequestedOnce = uiState.permissionRequestedOnce,
            topSystemPadding = topSystemPadding,
            bottomSystemPadding = bottomSystemPadding,
            onBack = onBack,
            onRequestPermission = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onOpenSettings = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            }
        )
        return
    }

    BackHandler(
        enabled = uiState.isQiblaDialOpen || uiState.selectedMosque != null || uiState.isSheetExpanded
    ) {
        when {
            uiState.isQiblaDialOpen -> viewModel.onToggleQiblaDial(false)
            uiState.selectedMosque != null -> viewModel.onSelectMosque(null)
            uiState.isSheetExpanded -> viewModel.onToggleSheetExpanded(false)
        }
    }

    if (uiState.isQiblaDialOpen) {
        QiblaCompassDialog(
            qiblaCompass = uiState.qiblaCompass,
            onDismiss = { viewModel.onToggleQiblaDial(false) }
        )
    }

    var currentMapCenterLat by remember { mutableDoubleStateOf(0.0) }
    var currentMapCenterLng by remember { mutableDoubleStateOf(0.0) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 700.dp

        OsmMosqueMapCanvas(
            uiState = uiState,
            onMapMovedByUser = viewModel::onMapCameraMovedByUser,
            onMapCenterChanged = { lat, lng ->
                currentMapCenterLat = lat
                currentMapCenterLng = lng
            },
            onSelectMosque = viewModel::onSelectMosque
        )

        if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        top = topSystemPadding + 12.dp,
                        end = 16.dp,
                        bottom = bottomSystemPadding + 16.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .width(380.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiveLocationHeaderOverlay(
                        liveLocation = uiState.liveLocation,
                        qiblaCompass = uiState.qiblaCompass,
                        selectedRadius = uiState.selectedRadius,
                        showSearchThisArea = uiState.showSearchThisArea,
                        isLoading = uiState.searchState is MosqueSearchState.Loading,
                        onRadiusSelected = viewModel::onRadiusSelected,
                        onRefreshClick = viewModel::refreshMosquesNearLiveLocation,
                        onQiblaClick = { viewModel.onToggleQiblaDial(true) },
                        onSearchThisAreaClick = {
                            viewModel.searchThisMapArea(currentMapCenterLat, currentMapCenterLng)
                        },
                        onBack = onBack
                    )

                    NearbyMosquesPanel(
                        searchState = uiState.searchState,
                        selectedRadius = uiState.selectedRadius,
                        selectedMosque = uiState.selectedMosque,
                        isExpanded = true,
                        isSideRail = true,
                        maxListHeight = this@BoxWithConstraints.maxHeight - 240.dp,
                        onToggleExpand = {},
                        onSelectMosque = viewModel::onSelectMosque,
                        onRetry = viewModel::refreshMosquesNearLiveLocation,
                        onExpandToMaxRadius = viewModel::expandToMaxRadius,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    OsmAttributionBadge(
                        modifier = Modifier.align(Alignment.BottomStart)
                    )

                    MapControlsColumn(
                        followUserLocation = uiState.followUserLocation,
                        mapLayer = uiState.mapLayer,
                        onRecenterClick = viewModel::onRecenterToLiveLocation,
                        onCycleMapLayer = viewModel::onCycleMapLayer,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        } else {
            LiveLocationHeaderOverlay(
                liveLocation = uiState.liveLocation,
                qiblaCompass = uiState.qiblaCompass,
                selectedRadius = uiState.selectedRadius,
                showSearchThisArea = uiState.showSearchThisArea,
                isLoading = uiState.searchState is MosqueSearchState.Loading,
                onRadiusSelected = viewModel::onRadiusSelected,
                onRefreshClick = viewModel::refreshMosquesNearLiveLocation,
                onQiblaClick = { viewModel.onToggleQiblaDial(true) },
                onSearchThisAreaClick = {
                    viewModel.searchThisMapArea(currentMapCenterLat, currentMapCenterLng)
                },
                onBack = onBack,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        top = topSystemPadding + 10.dp
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    OsmAttributionBadge(
                        modifier = Modifier.align(Alignment.BottomStart)
                    )

                    MapControlsColumn(
                        followUserLocation = uiState.followUserLocation,
                        mapLayer = uiState.mapLayer,
                        onRecenterClick = viewModel::onRecenterToLiveLocation,
                        onCycleMapLayer = viewModel::onCycleMapLayer,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }

                NearbyMosquesPanel(
                    searchState = uiState.searchState,
                    selectedRadius = uiState.selectedRadius,
                    selectedMosque = uiState.selectedMosque,
                    isExpanded = uiState.isSheetExpanded,
                    isSideRail = false,
                    maxListHeight = (this@BoxWithConstraints.maxHeight * 0.44f).coerceAtLeast(240.dp),
                    onToggleExpand = { viewModel.onToggleSheetExpanded() },
                    onSelectMosque = viewModel::onSelectMosque,
                    onRetry = viewModel::refreshMosquesNearLiveLocation,
                    onExpandToMaxRadius = viewModel::expandToMaxRadius,
                    modifier = Modifier.padding(bottom = bottomSystemPadding)
                )
            }
        }
    }
}

@Composable
private fun OsmAttributionBadge(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.osm_attribution),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun LocationPermissionScreen(
    permissionRequestedOnce: Boolean,
    topSystemPadding: androidx.compose.ui.unit.Dp,
    bottomSystemPadding: androidx.compose.ui.unit.Dp,
    onBack: (() -> Unit)?,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = topSystemPadding + 24.dp,
                bottom = bottomSystemPadding + 24.dp
            )
    ) {
        if (onBack != null) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("permission_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 8.dp,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_mosque_locator_art_1794751991038),
                    contentDescription = stringResource(R.string.permission_title),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.permission_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (permissionRequestedOnce) {
                        stringResource(R.string.permission_denied_rationale)
                    } else {
                        stringResource(R.string.permission_subtitle)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("enable_location_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.MyLocation,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.permission_grant_cta),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (permissionRequestedOnce) {
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_settings_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.permission_settings_cta))
                }
            }
        }
    }
}
