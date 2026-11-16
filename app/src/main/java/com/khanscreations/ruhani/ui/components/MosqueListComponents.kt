package com.khanscreations.ruhani.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.khanscreations.ruhani.R
import com.khanscreations.ruhani.data.model.GeoMath
import com.khanscreations.ruhani.data.model.Mosque
import com.khanscreations.ruhani.data.model.MosqueSearchState
import com.khanscreations.ruhani.data.model.SearchRadius

@Composable
fun NearbyMosquesPanel(
    searchState: MosqueSearchState,
    selectedRadius: SearchRadius,
    selectedMosque: Mosque?,
    isExpanded: Boolean,
    isSideRail: Boolean,
    maxListHeight: Dp,
    onToggleExpand: () -> Unit,
    onSelectMosque: (Mosque?) -> Unit,
    onRetry: () -> Unit,
    onExpandToMaxRadius: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = if (isSideRail) {
            RoundedCornerShape(24.dp)
        } else {
            RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        },
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 3.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!isSideRail) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }

            AnimatedContent(
                targetState = selectedMosque,
                label = "mosque_panel_content"
            ) { focusedMosque ->
                if (focusedMosque != null) {
                    MosqueDetailSection(
                        mosque = focusedMosque,
                        onClose = { onSelectMosque(null) },
                        onDirections = { openMosqueDirections(context, focusedMosque) },
                        onOpenMaps = { openMosqueInMaps(context, focusedMosque) },
                        onShare = { shareMosqueDetails(context, focusedMosque) }
                    )
                } else {
                    MosqueListSection(
                        searchState = searchState,
                        selectedRadius = selectedRadius,
                        isExpanded = isExpanded || isSideRail,
                        isSideRail = isSideRail,
                        maxListHeight = maxListHeight,
                        onToggleExpand = onToggleExpand,
                        onSelectMosque = onSelectMosque,
                        onDirections = { mosque -> openMosqueDirections(context, mosque) },
                        onRetry = onRetry,
                        onExpandToMaxRadius = onExpandToMaxRadius
                    )
                }
            }
        }
    }
}

@Composable
private fun MosqueListSection(
    searchState: MosqueSearchState,
    selectedRadius: SearchRadius,
    isExpanded: Boolean,
    isSideRail: Boolean,
    maxListHeight: Dp,
    onToggleExpand: () -> Unit,
    onSelectMosque: (Mosque) -> Unit,
    onDirections: (Mosque) -> Unit,
    onRetry: () -> Unit,
    onExpandToMaxRadius: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Mosque,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.nearby_mosques_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val subtitle = when (searchState) {
                    MosqueSearchState.Idle -> stringResource(R.string.locating_you)
                    MosqueSearchState.Loading -> "Searching OpenStreetMap nearby…"
                    is MosqueSearchState.Success ->
                        stringResource(R.string.mosque_count_format, searchState.mosques.size)
                    is MosqueSearchState.Error -> "Could not refresh mosques"
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (searchState is MosqueSearchState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (!isSideRail) {
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("toggle_mosque_list_button")
                ) {
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Rounded.ExpandMore
                        } else {
                            Icons.Rounded.ExpandLess
                        },
                        contentDescription = if (isExpanded) "Collapse list" else "Expand list",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (searchState) {
            MosqueSearchState.Idle,
            MosqueSearchState.Loading -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Finding mosques closest to your live GPS…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            is MosqueSearchState.Error -> {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = searchState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FilledTonalButton(
                            onClick = onRetry,
                            modifier = Modifier.testTag("retry_mosques_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Try Again")
                        }
                    }
                }
            }

            is MosqueSearchState.Success -> {
                if (searchState.mosques.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mosque,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.no_mosques_found),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (selectedRadius != SearchRadius.TWENTY_KM) {
                                    FilledTonalButton(
                                        onClick = onExpandToMaxRadius,
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("expand_radius_button")
                                    ) {
                                        Text(stringResource(R.string.expand_radius_10km))
                                    }
                                }
                                OutlinedButton(
                                    onClick = onRetry,
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("empty_retry_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry")
                                }
                            }
                        }
                    }
                } else {
                    val visibleMosques = if (isExpanded) {
                        searchState.mosques
                    } else {
                        searchState.mosques.take(2)
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = if (isExpanded) maxListHeight else 210.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 6.dp)
                    ) {
                        items(visibleMosques, key = { it.id }) { mosque ->
                            MosqueListItemCard(
                                mosque = mosque,
                                onClick = { onSelectMosque(mosque) },
                                onDirections = { onDirections(mosque) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MosqueListItemCard(
    mosque: Mosque,
    onClick: () -> Unit,
    onDirections: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mosque_card_${mosque.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mosque.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = mosque.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = GeoMath.formatDistance(mosque.distanceMeters),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.DirectionsWalk,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(
                                R.string.walk_time_format,
                                GeoMath.estimateWalkMinutes(mosque.distanceMeters)
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (mosque.openingHours != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = mosque.openingHours,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onDirections,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Directions,
                    contentDescription = stringResource(R.string.directions_cta),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun MosqueDetailSection(
    mosque: Mosque,
    onClose: () -> Unit,
    onDirections: () -> Unit,
    onOpenMaps: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Mosque,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mosque.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${GeoMath.formatDistance(mosque.distanceMeters)} • ${
                        stringResource(
                            R.string.walk_time_format,
                            GeoMath.estimateWalkMinutes(mosque.distanceMeters)
                        )
                    }",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("close_mosque_detail_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Back to nearby list"
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = mosque.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        if (mosque.openingHours != null || mosque.phone != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mosque.openingHours != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = mosque.openingHours,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (mosque.phone != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Call,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = mosque.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onDirections,
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("mosque_directions_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Directions,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.directions_cta))
            }

            FilledTonalButton(
                onClick = onOpenMaps,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("mosque_open_maps_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Map,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.open_in_maps_cta))
            }

            OutlinedButton(
                onClick = onShare,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("mosque_share_button"),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = stringResource(R.string.share_cta),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun openMosqueDirections(context: Context, mosque: Mosque) {
    val geoUri = Uri.parse(
        "geo:${mosque.latitude},${mosque.longitude}?q=${mosque.latitude},${mosque.longitude}(${Uri.encode(mosque.name)})"
    )
    val geoIntent = Intent(Intent.ACTION_VIEW, geoUri)
    runCatching {
        context.startActivity(geoIntent)
    }.onFailure {
        val osmUrl = Uri.parse(
            "https://www.openstreetmap.org/directions?engine=fossgis_osrm_foot&route=%3B${mosque.latitude}%2C${mosque.longitude}"
        )
        context.startActivity(Intent(Intent.ACTION_VIEW, osmUrl))
    }
}

private fun openMosqueInMaps(context: Context, mosque: Mosque) {
    val targetUri = Uri.parse(
        "geo:${mosque.latitude},${mosque.longitude}?q=${mosque.latitude},${mosque.longitude}(${Uri.encode(mosque.name)})"
    )
    val intent = Intent(Intent.ACTION_VIEW, targetUri)
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        val fallback = Uri.parse(
            "https://www.openstreetmap.org/?mlat=${mosque.latitude}&mlon=${mosque.longitude}#map=17/${mosque.latitude}/${mosque.longitude}"
        )
        context.startActivity(Intent(Intent.ACTION_VIEW, fallback))
    }
}

private fun shareMosqueDetails(context: Context, mosque: Mosque) {
    val osmLink = "https://www.openstreetmap.org/?mlat=${mosque.latitude}&mlon=${mosque.longitude}#map=17/${mosque.latitude}/${mosque.longitude}"
    val shareText = "${mosque.name}\n${mosque.address}\nDistance: ${
        GeoMath.formatDistance(mosque.distanceMeters)
    }\n$osmLink"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, mosque.name)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share Mosque Location"))
}
