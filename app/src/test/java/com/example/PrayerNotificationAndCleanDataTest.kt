package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.NotificationCategory
import com.example.domain.OfflineContentAndSyncManager
import com.example.domain.PrayerAlarmScheduler
import com.example.domain.RuhaniPushNotificationManager
import com.example.ui.RuhaniUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrayerNotificationAndCleanDataTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("ruhani_local_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("ruhani_notifications_vault", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("ruhani_offline_sync_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    @Test
    fun initialState_hasNoMockData() {
        val state = RuhaniUiState()
        assertEquals(0, state.tasbihCurrentCount)
        assertEquals(0, state.tasbihTodayTotal)
        assertEquals(0L, state.tasbihAllTimeTotal)
        assertEquals(0L, state.qazaFajr)
        assertEquals(0L, state.qazaDhuhr)
        assertEquals(0L, state.qazaAsr)
        assertEquals(0L, state.qazaMaghrib)
        assertEquals(0L, state.qazaIsha)
        assertEquals(0L, state.qazaWitr)
        assertEquals("", state.zakatCashBdt)
        assertEquals("", state.zakatBusinessBdt)
        assertEquals("", state.zakatDebtsBdt)
        assertTrue(state.downloadedSurahIds.isEmpty())
        assertTrue(state.downloadedArticleIds.isEmpty())
        assertTrue(state.articleReadProgress.isEmpty())
        assertTrue(state.notificationFeed.isEmpty())
    }

    @Test
    fun offlineContent_startsEmptyWithoutMockSeeding() {
        val articles = OfflineContentAndSyncManager.getDownloadedArticleIds(context)
        assertTrue(articles.isEmpty())
        val sizeKb = OfflineContentAndSyncManager.calculateOfflineStorageSizeKb(context)
        assertEquals(0, sizeKb)
    }

    @Test
    fun notificationChannels_ensureChannelsCreated() {
        RuhaniPushNotificationManager.ensureNotificationChannels(context)
        // Verify channels do not throw and can be called safely
        assertTrue(true)
    }

    @Test
    fun sendNotification_persistsInLocalVault() {
        val notif = RuhaniPushNotificationManager.sendNotification(
            context = context,
            category = NotificationCategory.WAQT_AZAN,
            title = "ফজর নামাজের ওয়াক্ত",
            message = "ফজর সালাতের সময় শুরু হয়েছে।"
        )
        assertNotNull(notif.id)
        assertEquals("ফজর নামাজের ওয়াক্ত", notif.titleBn)

        val stored = RuhaniPushNotificationManager.getStoredNotifications(context)
        assertEquals(1, stored.size)
        assertEquals(notif.id, stored.first().id)
        assertEquals(false, stored.first().isRead)

        val updated = RuhaniPushNotificationManager.markAllNotificationsRead(context)
        assertEquals(1, updated.size)
        assertTrue(updated.first().isRead)
    }

    @Test
    fun prayerAlarmScheduler_schedulesWithoutCrashing() {
        PrayerAlarmScheduler.schedulePrayerAlarms(context)
        assertTrue(true)
    }
}
