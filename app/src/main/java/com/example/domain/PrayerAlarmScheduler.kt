package com.example.domain

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

class PrayerNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val waqtName = intent?.getStringExtra("EXTRA_WAQT_NAME") ?: return
        val waqtNameBn = intent.getStringExtra("EXTRA_WAQT_NAME_BN") ?: waqtName

        val prefs = context.getSharedPreferences("ruhani_local_prefs", Context.MODE_PRIVATE)
        val alertEnabled = prefs.getBoolean("azan_alert_$waqtName", true)
        if (alertEnabled) {
            val title = "নামাজের ওয়াক্ত উপস্থিত: $waqtNameBn"
            val message = "সালাতের ওয়াক্ত শুরু হয়েছে। ওযু করে জামাতে সালাত আদায় করার প্রস্তুতি নিন।"
            RuhaniPushNotificationManager.sendNotification(
                context = context,
                category = NotificationCategory.WAQT_AZAN,
                title = title,
                message = message
            )
        }

        // Reschedule prayer alarms for upcoming days
        PrayerAlarmScheduler.schedulePrayerAlarms(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            PrayerAlarmScheduler.schedulePrayerAlarms(context)
        }
    }
}

object PrayerAlarmScheduler {

    private const val TAG = "PrayerAlarmScheduler"

    fun schedulePrayerAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val prefs = context.getSharedPreferences("ruhani_local_prefs", Context.MODE_PRIVATE)

        val districtId = prefs.getString("district", "Dhaka") ?: "Dhaka"
        val district = RuhaniContentCatalog.findDistrict(districtId)
        val method = CalculationMethod.fromId(
            prefs.getString("calc_method", "ISLAMIC_FOUNDATION_BD") ?: "ISLAMIC_FOUNDATION_BD"
        )
        val madhhab = Madhhab.fromId(prefs.getString("madhhab", "HANAFI") ?: "HANAFI")
        val hijriOffset = prefs.getInt("hijri_offset", 0)

        val todaySchedule = AstronomicalEngine.calculateSchedule(
            district = district,
            method = method,
            madhhab = madhhab,
            hijriOffsetDays = hijriOffset,
            dayOffset = 0
        )

        val now = System.currentTimeMillis()

        todaySchedule.fivePrayers.forEachIndexed { index, prayer ->
            val waqtType = prayer.type
            val isAlertEnabled = prefs.getBoolean("azan_alert_${waqtType.name}", true)
            val requestCode = 8000 + index

            val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                putExtra("EXTRA_WAQT_NAME", waqtType.name)
                putExtra("EXTRA_WAQT_NAME_BN", waqtType.nameBn)
            }
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                pendingIntentFlags
            )

            if (!isAlertEnabled) {
                alarmManager.cancel(pendingIntent)
                return@forEachIndexed
            }

            // Calculate trigger time
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, prayer.startMinutes / 60)
                set(Calendar.MINUTE, prayer.startMinutes % 60)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // If time has passed today, schedule for tomorrow
            if (cal.timeInMillis <= now) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            cal.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            cal.timeInMillis,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        cal.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        cal.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to schedule alarm for ${waqtType.name}: ${e.message}")
            }
        }
    }
}
