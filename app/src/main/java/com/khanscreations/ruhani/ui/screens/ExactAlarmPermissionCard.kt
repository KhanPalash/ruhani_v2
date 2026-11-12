package com.khanscreations.ruhani.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.khanscreations.ruhani.domain.PrayerAlarmScheduler
import com.khanscreations.ruhani.ui.theme.NoorMintLuminous
import com.khanscreations.ruhani.ui.theme.NoorRedAlert

/**
 * Whether the OS will grant this app exact alarms, plus a way to ask for them.
 *
 * Prayer alerts are only punctual when the exact-alarm permission is held. On
 * Android 12+ the permission is not granted on install, so without an explicit
 * prompt the app degrades to inexact alarms that can fire several minutes late.
 */
class ExactAlarmPermission(
    val granted: Boolean,
    val requestPermission: () -> Unit
)

@Composable
fun rememberExactAlarmPermission(): ExactAlarmPermission {
    val context = LocalContext.current
    // A plain state holder (not a delegated local) so every reader always sees the
    // latest value, even from a callback captured by an earlier composition.
    val grantedState = remember { mutableStateOf(PrayerAlarmScheduler.canScheduleExactAlarms(context)) }

    fun refresh() {
        val nowGranted = PrayerAlarmScheduler.canScheduleExactAlarms(context)
        val wasGranted = grantedState.value
        grantedState.value = nowGranted
        if (nowGranted && !wasGranted) {
            // The grant just landed: re-arm the prayer alarms so they use exact timing.
            PrayerAlarmScheduler.schedulePrayerAlarms(context)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refresh()
    }

    // Re-read the permission whenever the user comes back from system settings.
    LifecycleResumeEffect(context) {
        refresh()
        onPauseOrDispose { }
    }

    return ExactAlarmPermission(
        granted = grantedState.value,
        requestPermission = {
            try {
                launcher.launch(PrayerAlarmScheduler.exactAlarmSettingsIntent(context))
            } catch (_: ActivityNotFoundException) {
                // Some OEM builds omit the exact-alarm screen; fall back to app details.
                launcher.launch(appDetailsIntent(context))
            }
        }
    )
}

/**
 * Full settings card explaining the exact-alarm permission and offering a grant
 * button when the permission is missing.
 */
@Composable
fun ExactAlarmPermissionCard(
    isBn: Boolean,
    modifier: Modifier = Modifier
) {
    val permission = rememberExactAlarmPermission()
    val granted = permission.granted

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("exact_alarm_card")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (granted) NoorMintLuminous else NoorRedAlert,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isBn) "নামাজের আযান (Prayer Alerts)" else "Prayer Time Alerts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (granted) {
                    if (isBn) "সঠিক সময়ে আযান নোটিফিকেশন পাঠানোর অনুমতি আছে।"
                    else "Exact alarms are permitted, so azan notifications arrive on time."
                } else {
                    if (isBn) "সঠিক সময়ে আযান নোটিফিকেশন পেতে 'Exact Alarm' অনুমতি দিন। না দিলে বিজ্ঞপ্তি কয়েক মিনিট দেরিতে আসতে পারে।"
                    else "Allow 'Exact Alarm' so azan notifications arrive on time. Without it, alerts can be delayed by several minutes."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (granted) MaterialTheme.colorScheme.onSurface else NoorRedAlert
            )

            if (!granted) {
                OutlinedButton(
                    onClick = permission.requestPermission,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exact_alarm_grant_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = if (isBn) "Exact Alarm অনুমতি দিন" else "Grant exact alarm permission",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = if (isBn) {
                    "Android ১২+ এ নির্দিষ্ট সময়ে বিজ্ঞপ্তি দেওয়ার জন্য অনুমতি আলাদা করে দিতে হয়।"
                } else {
                    "Android 12+ requires a separate grant before the system can fire alarms at an exact time."
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Compact warning strip shown on the prayer schedule when azan alerts are on but
 * the app cannot yet schedule exact alarms.
 */
@Composable
fun ExactAlarmWarningBanner(
    isBn: Boolean,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = NoorRedAlert.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, NoorRedAlert.copy(alpha = 0.45f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("exact_alarm_banner")
            .clickable(onClick = onGrant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = NoorRedAlert,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBn) "আযান বিজ্ঞপ্তি সঠিক সময়ে আসবে না" else "Azan alerts may be delayed",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NoorRedAlert
                )
                Text(
                    text = if (isBn) "সঠিক সময়ের জন্য Exact Alarm অনুমতি দিন"
                    else "Tap to grant the Exact Alarm permission",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun appDetailsIntent(context: Context) =
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        "package:${context.packageName}".toUri()
    )
