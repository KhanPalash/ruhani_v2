package com.khanscreations.ruhani.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.khanscreations.ruhani.R
import com.khanscreations.ruhani.data.model.GeoMath
import com.khanscreations.ruhani.data.model.QiblaCompassState
import com.khanscreations.ruhani.ui.theme.MasjidBrassSecondary
import com.khanscreations.ruhani.ui.theme.MasjidEmeraldPrimary
import com.khanscreations.ruhani.ui.theme.MasjidLivePulseGreen
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QiblaCompassDialog(
    qiblaCompass: QiblaCompassState,
    onDismiss: () -> Unit
) {
    val bearing = qiblaCompass.qiblaBearingDegrees ?: 0f
    val targetNeedleAngle = qiblaCompass.relativeQiblaDegrees ?: bearing

    // Continuous shortest-path rotation to avoid 359° -> 0° wrap snap
    var continuousNeedleAngle by remember { mutableFloatStateOf(targetNeedleAngle) }
    LaunchedEffect(targetNeedleAngle) {
        val delta = GeoMath.shortestSignedAngle(
            GeoMath.normalizeDegrees(continuousNeedleAngle),
            targetNeedleAngle
        )
        continuousNeedleAngle += delta
    }

    val animatedNeedle by animateFloatAsState(
        targetValue = continuousNeedleAngle,
        animationSpec = spring(stiffness = 220f, dampingRatio = 0.78f),
        label = "qibla_dial_needle"
    )

    val heading = qiblaCompass.deviceAzimuthDegrees
    var continuousHeading by remember { mutableFloatStateOf(-(heading ?: 0f)) }
    LaunchedEffect(heading) {
        if (heading != null) {
            val desired = -heading
            val delta = GeoMath.shortestSignedAngle(
                GeoMath.normalizeDegrees(continuousHeading),
                GeoMath.normalizeDegrees(desired)
            )
            continuousHeading += delta
        }
    }
    val animatedDialRotation by animateFloatAsState(
        targetValue = continuousHeading,
        animationSpec = spring(stiffness = 220f, dampingRatio = 0.80f),
        label = "qibla_dial_rose"
    )

    val isAligned = qiblaCompass.isAlignedToQibla
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 16.dp,
            border = BorderStroke(
                1.5.dp,
                if (isAligned) MasjidLivePulseGreen else MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.qibla_compass_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_qibla_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close Qibla compass"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.size(230.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.minDimension / 2f - 10.dp.toPx()

                        // Outer ring
                        drawCircle(
                            color = if (isAligned) MasjidLivePulseGreen else outlineColor,
                            radius = radius,
                            center = center,
                            style = Stroke(width = if (isAligned) 3.dp.toPx() else 2.dp.toPx())
                        )

                        // Rotating compass tick ring
                        rotate(degrees = animatedDialRotation, pivot = center) {
                            for (deg in 0 until 360 step 15) {
                                val rad = Math.toRadians((deg - 90).toDouble())
                                val isMajor = deg % 90 == 0
                                val innerR = radius - (if (isMajor) 14.dp.toPx() else 7.dp.toPx())
                                val start = Offset(
                                    center.x + (innerR * cos(rad)).toFloat(),
                                    center.y + (innerR * sin(rad)).toFloat()
                                )
                                val end = Offset(
                                    center.x + (radius * cos(rad)).toFloat(),
                                    center.y + (radius * sin(rad)).toFloat()
                                )
                                drawLine(
                                    color = if (deg == 0) MasjidBrassSecondary else onSurface.copy(alpha = 0.35f),
                                    start = start,
                                    end = end,
                                    strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        // Qibla Kaaba pointer needle
                        rotate(degrees = animatedNeedle, pivot = center) {
                            val tipY = center.y - radius + 20.dp.toPx()
                            val tailY = center.y + radius * 0.42f
                            val needleHalfW = 11.dp.toPx()

                            val forwardPath = Path().apply {
                                moveTo(center.x, tipY)
                                lineTo(center.x - needleHalfW, center.y)
                                lineTo(center.x + needleHalfW, center.y)
                                close()
                            }
                            drawPath(
                                path = forwardPath,
                                color = if (isAligned) MasjidLivePulseGreen else MasjidEmeraldPrimary
                            )

                            val tailPath = Path().apply {
                                moveTo(center.x, tailY)
                                lineTo(center.x - needleHalfW * 0.7f, center.y)
                                lineTo(center.x + needleHalfW * 0.7f, center.y)
                                close()
                            }
                            drawPath(
                                path = tailPath,
                                color = outlineColor.copy(alpha = 0.65f)
                            )

                            // Kaaba golden emblem at needle tip
                            drawCircle(
                                color = MasjidBrassSecondary,
                                radius = 7.dp.toPx(),
                                center = Offset(center.x, tipY + 14.dp.toPx())
                            )
                        }

                        drawCircle(
                            color = Color.White,
                            radius = 7.dp.toPx(),
                            center = center
                        )
                        drawCircle(
                            color = MasjidEmeraldPrimary,
                            radius = 4.dp.toPx(),
                            center = center
                        )
                    }

                    // Center N indicator pill at top of compass frame
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Text(
                            text = "${bearing.roundToInt()}° ${GeoMath.compassCardinal(bearing)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val statusText = when {
                    !qiblaCompass.hasCompassSensor ->
                        stringResource(R.string.compass_sensor_unavailable)
                    isAligned ->
                        stringResource(R.string.qibla_aligned)
                    (qiblaCompass.signedOffsetDegrees ?: 0f) > 0f ->
                        stringResource(R.string.qibla_turn_right)
                    else ->
                        stringResource(R.string.qibla_turn_left)
                }

                Surface(
                    color = if (isAligned) {
                        MasjidLivePulseGreen.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isAligned) MasjidEmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isAligned) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }

                if (heading != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Device heading: ${heading.roundToInt()}° • Kaaba bearing: ${bearing.roundToInt()}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
