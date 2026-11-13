package com.khanscreations.ruhani.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.khanscreations.ruhani.domain.BdDistrict
import com.khanscreations.ruhani.domain.LocationDistrictResolver
import kotlinx.coroutines.launch

/**
 * "Detect my district" control used by Settings and the first-run screen.
 *
 * It asks for the approximate-location permission (ACCESS_COARSE_LOCATION) only
 * when the user taps it, converts the fix into the nearest district, and hands
 * that district back through [onDetected]. Nothing is stored beyond the chosen
 * district id, so the app keeps no location history.
 */
@Composable
fun AutoDetectDistrictButton(
    isBn: Boolean,
    onDetected: (BdDistrict) -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDetecting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    fun detectFromLocation() {
        if (isDetecting) return
        scope.launch {
            isDetecting = true
            isError = false
            statusText = sourceText(isBn, "লোকেশন থেকে জেলা খোঁজা হচ্ছে…", "Detecting your district…")

            val detection = runCatching { LocationDistrictResolver.detect(context) }.getOrNull()
            isDetecting = false

            when {
                detection == null -> {
                    isError = true
                    statusText = sourceText(
                        isBn,
                        "লোকেশন পাওয়া যায়নি। লোকেশন সার্ভিস চালু আছে কি না দেখুন, অথবা জেলা ম্যানুয়ালি বেছে নিন।",
                        "No location fix. Check that location services are on, or pick a district manually."
                    )
                }

                !detection.isWithinBangladesh -> {
                    isError = true
                    statusText = sourceText(
                        isBn,
                        "আপনি বাংলাদেশের সীমার বাইরে আছেন বলে মনে হচ্ছে। জেলা ম্যানুয়ালি বেছে নিন।",
                        "You appear to be outside Bangladesh. Please pick a district manually."
                    )
                }

                else -> {
                    onDetected(detection.district)
                    statusText = sourceText(
                        isBn,
                        "শনাক্ত হয়েছে: ${detection.district.nameBn} (${detection.district.divisionBn})",
                        "Detected: ${detection.district.nameEn}"
                    )
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            detectFromLocation()
        } else {
            isError = true
            statusText = sourceText(
                isBn,
                "লোকেশন অনুমতি ছাড়া জেলা শনাক্ত করা যাবে না।",
                "District detection needs the location permission."
            )
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = containerColor,
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isDetecting) {
                    if (LocationDistrictResolver.hasPermission(context)) {
                        detectFromLocation()
                    } else {
                        permissionLauncher.launch(LocationDistrictResolver.COARSE_PERMISSION)
                    }
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = sourceText(
                        isBn,
                        "লোকেশন দিয়ে জেলা শনাক্ত করুন",
                        "Detect district from location"
                    ),
                    color = contentColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isDetecting) {
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = contentColor
                    )
                }
            }
        }

        statusText?.let { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = if (isError) MaterialTheme.colorScheme.error else contentColor.copy(alpha = 0.85f)
            )
        }
    }
}

private fun sourceText(isBn: Boolean, bn: String, en: String): String = if (isBn) bn else en
