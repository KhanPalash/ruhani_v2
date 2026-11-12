package com.khanscreations.ruhani

import android.app.AlarmManager
import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import com.khanscreations.ruhani.domain.PrayerAlarmScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager
import org.robolectric.Shadows.shadowOf

/**
 * Covers the Android 12+ exact-alarm permission contract used for prayer alerts.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExactAlarmPermissionTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("ruhani_local_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
    }

    @Test
    fun canScheduleExactAlarms_reflectsSystemGrant() {
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        assertTrue(PrayerAlarmScheduler.canScheduleExactAlarms(context))

        ShadowAlarmManager.setCanScheduleExactAlarms(false)
        assertFalse(PrayerAlarmScheduler.canScheduleExactAlarms(context))
    }

    @Test
    fun exactAlarmSettingsIntent_opensSystemGrantScreenForThisPackage() {
        val intent = PrayerAlarmScheduler.exactAlarmSettingsIntent(context)
        assertEquals(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, intent.action)
        assertEquals("package:${context.packageName}", intent.data.toString())
    }

    @Test
    fun schedulePrayerAlarms_usesExactAlarmsWhenGranted() {
        ShadowAlarmManager.setCanScheduleExactAlarms(true)

        PrayerAlarmScheduler.schedulePrayerAlarms(context)

        val alarms = shadowOf(alarmManager()).scheduledAlarms
        assertEquals(5, alarms.size)
        assertTrue(
            "All prayer alarms should be exact when the permission is granted",
            alarms.all { it.windowLengthMs == ShadowAlarmManager.WINDOW_EXACT }
        )
    }

    @Test
    fun schedulePrayerAlarms_fallsBackToInexactAlarmsWhenDenied() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)

        // Must not throw and must still arm the five prayer alerts.
        PrayerAlarmScheduler.schedulePrayerAlarms(context)

        val alarms = shadowOf(alarmManager()).scheduledAlarms
        assertEquals(5, alarms.size)
        assertTrue(
            "Prayer alerts should degrade to inexact (idle-aware) alarms, not be dropped",
            alarms.all { it.windowLengthMs == ShadowAlarmManager.WINDOW_HEURISTIC }
        )
    }

    private fun alarmManager(): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}
