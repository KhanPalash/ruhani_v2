package com.example.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.QuranBookmark
import com.example.data.model.SalatLog
import com.example.data.model.UserProfile
import com.example.data.remote.QuranApiService
import com.example.data.repository.RuhaniFirestoreRepository
import com.example.domain.AstronomicalEngine
import com.example.domain.BdDistrict
import com.example.domain.CalculationMethod
import com.example.domain.DailyPrayerSchedule
import com.example.domain.Madhhab
import com.example.domain.MushafMode
import com.example.domain.NotificationCategory
import com.example.domain.OfflineArticleItem
import com.example.domain.OfflineContentAndSyncManager
import com.example.domain.PendingSyncRecord
import com.example.domain.QariInfo
import com.example.domain.QuranAyah
import com.example.domain.QuranCatalog
import com.example.domain.QuranSurah
import com.example.domain.RuhaniContentCatalog
import com.example.domain.RuhaniNotificationItem
import com.example.domain.RuhaniPushNotificationManager
import com.example.domain.SecureAuthAndPrivacyManager
import com.example.domain.TasbihPreset
import com.example.domain.WaqtType
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class MainTab {
    HOME, QURAN, PRAYER, MORE
}

enum class SubScreen {
    NONE,
    SURAH_READER,
    TASBIH,
    DUA_DHIKR,
    SALAT_TRACKER,
    QIBLA,
    CALENDAR,
    ZAKAT,
    NAMES_OF_ALLAH,
    HADITH,
    RAMADAN,
    NAWAFIL,
    MOSQUES,
    BOOKS,
    FATWA,
    HAJJ_GUIDE,
    BABY_NAMES,
    HARAMAIN_LIVE,
    PROFILE,
    SETTINGS
}

data class RuhaniUiState(
    val hasCompletedOnboarding: Boolean = false,
    val isGuestMode: Boolean = false,
    val currentUserId: String? = null,
    val userEmail: String? = null,
    val displayName: String = "মুসলিম ব্যবহারকারী",
    val phone: String = "",
    val language: String = "bn", // "bn" or "en"
    val useBanglaDigits: Boolean = false,
    val themeMode: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
    val selectedDistrict: BdDistrict = RuhaniContentCatalog.bdDistricts.first(),
    val calculationMethod: CalculationMethod = CalculationMethod.ISLAMIC_FOUNDATION_BD,
    val madhhab: Madhhab = Madhhab.HANAFI,
    val hijriOffsetDays: Int = 0,
    val prayerDayOffset: Int = 0,
    val schedule: DailyPrayerSchedule = AstronomicalEngine.calculateSchedule(
        district = RuhaniContentCatalog.bdDistricts.first(),
        method = CalculationMethod.ISLAMIC_FOUNDATION_BD,
        madhhab = Madhhab.HANAFI
    ),
    val currentTab: MainTab = MainTab.HOME,
    val activeSubScreen: SubScreen = SubScreen.NONE,
    // Azan & Notification toggles per Waqt
    val azanAlerts: Map<WaqtType, Boolean> = mapOf(
        WaqtType.FAJR to true,
        WaqtType.DHUHR to true,
        WaqtType.ASR to true,
        WaqtType.MAGHRIB to true,
        WaqtType.ISHA to true
    ),
    val autoSilenceDuringSalat: Boolean = true,
    // Tasbih State
    val selectedTasbihPreset: TasbihPreset = RuhaniContentCatalog.tasbihPresets.first(),
    val tasbihCurrentCount: Int = 0,
    val tasbihTarget: Int = 33,
    val tasbihCycle: Int = 1,
    val tasbihTotalCycles: Int = 3,
    val tasbihTodayTotal: Int = 0,
    val tasbihAllTimeTotal: Long = 0L,
    val tasbihCountsByPreset: Map<String, Int> = emptyMap(),
    val tasbihVibrationEnabled: Boolean = true,
    val tasbihSoundEnabled: Boolean = false,
    val showTasbihResetDialog: Boolean = false,
    // Dua Local State
    val duaCountsById: Map<String, Int> = emptyMap(),
    val favoriteDuaIds: Set<String> = emptySet(),
    // Quran Reader & Audio State
    val selectedSurah: QuranSurah = QuranCatalog.surahs.first(),
    val lastReadSurahNumber: Int = 1,
    val lastReadAyahNumber: Int = 1,
    val mushafMode: MushafMode = MushafMode.AYAH_BY_AYAH,
    val quranArabicFontSizeSp: Int = 28,
    val showTransliteration: Boolean = true,
    val showWordByWord: Boolean = false,
    val hifzHideArabic: Boolean = false,
    val hifzHideTranslation: Boolean = false,
    val selectedQari: QariInfo = QuranCatalog.qariList.first(),
    val playingSurahNumber: Int? = null,
    val playingAyahNumber: Int? = null,
    val isAudioPlaying: Boolean = false,
    val isAudioBuffering: Boolean = false,
    val audioAutoAdvance: Boolean = true,
    val audioProgressFraction: Float = 0.0f,
    val audioCurrentPositionMs: Int = 0,
    val audioDurationMs: Int = 0,
    val audioRepeat3x: Boolean = false,
    val ayahRepeatIteration: Int = 1,
    val currentAyahAudioUrl: String = "https://the-quran-project.github.io/Quran-Audio/Data/1/1_1.mp3",
    val currentAyahOriginalUrl: String = "https://everyayah.com/data/Alafasy_128kbps/001001.mp3",
    val currentAyahApiEndpoint: String = "https://quranapi.pages.dev/api/1/1.json",
    val isSurahSyncingFromApi: Boolean = false,
    val expandedTafsirAyahKey: String? = null,
    val downloadedSurahIds: Set<Int> = emptySet(),
    val downloadingSurahId: Int? = null,
    val downloadedAyahKeys: Set<String> = emptySet(),
    val downloadingAyahKey: String? = null,
    val isPlayingFromOfflineCache: Boolean = false,
    // Auth & Privacy State
    val authProvider: String = "GUEST", // "GOOGLE", "EMAIL_PASSWORD", "GUEST"
    val privacyCloudSyncConsent: Boolean = true,
    val privacyPersonalizedAlertsConsent: Boolean = true,
    val privacyDataMinimization: Boolean = true,
    val exportedUserDataJson: String? = null,
    // Offline Content & Progress Synchronization State
    val isDeviceOnline: Boolean = true,
    val downloadedArticleIds: Set<String> = emptySet(),
    val articleReadProgress: Map<String, Int> = emptyMap(),
    val offlineStorageUsedKb: Int = 0,
    val pendingSyncQueue: List<PendingSyncRecord> = emptyList(),
    val lastSyncedLabel: String = "আজ সংরক্ষিত (Just now)",
    // Push & Personalized Notifications State
    val notifyNewContentAlerts: Boolean = true,
    val notifyImportantUpdates: Boolean = true,
    val notifyPersonalizedActivity: Boolean = true,
    val notificationFeed: List<RuhaniNotificationItem> = emptyList(),
    // Salat & Qaza Tracker
    val salatLogsByDate: Map<String, SalatLog> = emptyMap(),
    val qazaFajr: Long = 0L,
    val qazaDhuhr: Long = 0L,
    val qazaAsr: Long = 0L,
    val qazaMaghrib: Long = 0L,
    val qazaIsha: Long = 0L,
    val qazaWitr: Long = 0L,
    // Bookmarks
    val bookmarks: List<QuranBookmark> = emptyList(),
    // Zakat Calculator
    val zakatUseSilverNisab: Boolean = true,
    val zakatGoldRatePerVori: Double = 138000.0,
    val zakatSilverRatePerVori: Double = 2100.0,
    val zakatCashBdt: String = "",
    val zakatGoldValueBdt: String = "",
    val zakatBusinessBdt: String = "",
    val zakatDebtsBdt: String = "",
    // Haramain Live Stream State
    val haramainChannel: String = "MAKKAH", // "MAKKAH" or "MADINAH"
    val isHaramainPlaying: Boolean = true,
    // Status / Sync message
    val syncStatusMessage: String? = null,
    val errorMessage: String? = null,
    // In-app update prompt
    val updateInfo: com.example.domain.UpdateInfo? = null
)

class RuhaniViewModel(
    private val appContext: Context,
    private val firestoreRepository: RuhaniFirestoreRepository?,
    private val authenticatedUserId: String?,
    private val authenticatedEmail: String?,
    private val authenticatedName: String?
) : ViewModel() {

    private val prefs = appContext.getSharedPreferences("ruhani_local_prefs", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<RuhaniUiState> = _uiState.asStateFlow()

    private var audioPlayJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var networkCallback: android.net.ConnectivityManager.NetworkCallback? = null

    init {
        initTts()
        RuhaniPushNotificationManager.ensureNotificationChannels(appContext)
        com.example.domain.PrayerAlarmScheduler.schedulePrayerAlarms(appContext)
        startLivePrayerClock()
        observeNetworkConnectivity()
        if (authenticatedUserId != null && firestoreRepository != null) {
            observeCloudData(authenticatedUserId, firestoreRepository)
        }
        checkForUpdate()
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(updateInfo = null) }
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            val currentVersion = try {
                appContext.packageManager
                    .getPackageInfo(appContext.packageName, 0)
                    .versionName ?: "1.0"
            } catch (_: Exception) { "1.0" }
            val info = com.example.domain.UpdateChecker.checkForUpdate(currentVersion)
            if (info != null) {
                _uiState.update { it.copy(updateInfo = info) }
            }
        }
    }

    private fun observeNetworkConnectivity() {
        networkCallback = OfflineContentAndSyncManager.registerNetworkCallback(appContext) { isOnline ->
            val wasOffline = !_uiState.value.isDeviceOnline
            _uiState.update { it.copy(isDeviceOnline = isOnline) }
            if (isOnline && wasOffline && _uiState.value.pendingSyncQueue.isNotEmpty()) {
                synchronizeOfflineChangesNow()
            }
        }
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ttsReady = true
                    tts?.language = Locale("ar")
                }
            }
        } catch (e: Exception) {
            Log.w("RuhaniVM", "TTS initialization skipped: ${e.message}")
        }
    }

    private fun loadInitialState(): RuhaniUiState {
        val districtId = prefs.getString("district", "Dhaka") ?: "Dhaka"
        val district = RuhaniContentCatalog.findDistrict(districtId)
        val method = CalculationMethod.fromId(
            prefs.getString("calc_method", "ISLAMIC_FOUNDATION_BD") ?: "ISLAMIC_FOUNDATION_BD"
        )
        val madhhab = Madhhab.fromId(prefs.getString("madhhab", "HANAFI") ?: "HANAFI")
        val hijriOffset = prefs.getInt("hijri_offset", 0)
        val lang = prefs.getString("language", "bn") ?: "bn"
        val theme = prefs.getString("theme_mode", "DARK") ?: "DARK"
        val onboarded = prefs.getBoolean("onboarded", false)
        val guest = prefs.getBoolean("guest_mode", false)
        val localSession = SecureAuthAndPrivacyManager.getActiveSession(appContext)
        val resolvedUserId = authenticatedUserId ?: localSession?.userId
        val resolvedEmail = authenticatedEmail ?: localSession?.email
        val resolvedProvider = when {
            authenticatedUserId != null -> "GOOGLE"
            localSession != null -> localSession.authProvider
            else -> "GUEST"
        }
        val displayName = authenticatedName?.takeIf { it.isNotBlank() }
            ?: localSession?.displayName?.takeIf { it.isNotBlank() }
            ?: prefs.getString("display_name", "মুসলিম ব্যবহারকারী")
            ?: "মুসলিম ব্যবহারকারী"

        val todaySchedule = AstronomicalEngine.calculateSchedule(
            district = district,
            method = method,
            madhhab = madhhab,
            hijriOffsetDays = hijriOffset
        )

        val azanAlertsMap = mapOf(
            WaqtType.FAJR to prefs.getBoolean("azan_alert_${WaqtType.FAJR.name}", true),
            WaqtType.DHUHR to prefs.getBoolean("azan_alert_${WaqtType.DHUHR.name}", true),
            WaqtType.ASR to prefs.getBoolean("azan_alert_${WaqtType.ASR.name}", true),
            WaqtType.MAGHRIB to prefs.getBoolean("azan_alert_${WaqtType.MAGHRIB.name}", true),
            WaqtType.ISHA to prefs.getBoolean("azan_alert_${WaqtType.ISHA.name}", true)
        )

        val defaultSalatLog = SalatLog(
            id = todaySchedule.dateKey,
            userId = resolvedUserId ?: "local",
            dateKey = todaySchedule.dateKey,
            fajr = prefs.getBoolean("salat_${todaySchedule.dateKey}_fajr", false),
            dhuhr = prefs.getBoolean("salat_${todaySchedule.dateKey}_dhuhr", false),
            asr = prefs.getBoolean("salat_${todaySchedule.dateKey}_asr", false),
            maghrib = prefs.getBoolean("salat_${todaySchedule.dateKey}_maghrib", false),
            isha = prefs.getBoolean("salat_${todaySchedule.dateKey}_isha", false),
            witr = prefs.getBoolean("salat_${todaySchedule.dateKey}_witr", false),
            duha = prefs.getBoolean("salat_${todaySchedule.dateKey}_duha", false),
            awwabin = prefs.getBoolean("salat_${todaySchedule.dateKey}_awwabin", false),
            tahajjud = prefs.getBoolean("salat_${todaySchedule.dateKey}_tahajjud", false),
            fasting = prefs.getBoolean("salat_${todaySchedule.dateKey}_fasting", false)
        )

        val tasbihToday = prefs.getInt("tasbih_today", 0)
        val savedPresetId = prefs.getString("selected_tasbih_preset_id", RuhaniContentCatalog.tasbihPresets.first().id)
            ?: RuhaniContentCatalog.tasbihPresets.first().id
        val activePreset = RuhaniContentCatalog.tasbihPresets.firstOrNull { it.id == savedPresetId }
            ?: RuhaniContentCatalog.tasbihPresets.first()
        val presetCountsMap = RuhaniContentCatalog.tasbihPresets.associate { preset ->
            val defaultVal = if (preset.id == activePreset.id) prefs.getInt("tasbih_current", 0) else 0
            preset.id to prefs.getInt("tasbih_preset_count_${preset.id}", defaultVal)
        }
        val currentCountForActive = presetCountsMap[activePreset.id] ?: prefs.getInt("tasbih_current", 0)
        val savedTarget = prefs.getInt("tasbih_target", activePreset.defaultTarget)
        val savedCycle = prefs.getInt("tasbih_cycle", 1)

        val duaCountsMap = RuhaniContentCatalog.duas.associate { dua ->
            dua.id to prefs.getInt("dua_count_${dua.id}", 0)
        }
        val favDuas = prefs.getStringSet("favorite_duas", emptySet())
            ?.toSet() ?: emptySet()

        val realNotifs = RuhaniPushNotificationManager.getStoredNotifications(appContext)

        return RuhaniUiState(
            hasCompletedOnboarding = onboarded || resolvedUserId != null,
            isGuestMode = guest && resolvedUserId == null,
            currentUserId = resolvedUserId,
            userEmail = resolvedEmail,
            displayName = displayName,
            authProvider = resolvedProvider,
            privacyCloudSyncConsent = localSession?.privacyCloudSyncConsent ?: true,
            privacyPersonalizedAlertsConsent = localSession?.privacyPersonalizedAlertsConsent ?: true,
            privacyDataMinimization = localSession?.privacyAnalyticsMinimize ?: true,
            isDeviceOnline = OfflineContentAndSyncManager.isDeviceOnline(appContext),
            downloadedArticleIds = OfflineContentAndSyncManager.getDownloadedArticleIds(appContext),
            downloadedSurahIds = prefs.getStringSet("downloaded_surah_ids", emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
            downloadedAyahKeys = prefs.getStringSet("downloaded_ayah_keys", emptySet())?.toSet() ?: emptySet(),
            articleReadProgress = OfflineContentAndSyncManager.getArticleProgressMap(appContext),
            offlineStorageUsedKb = OfflineContentAndSyncManager.calculateOfflineStorageSizeKb(appContext),
            pendingSyncQueue = OfflineContentAndSyncManager.getPendingSyncQueue(appContext),
            lastSyncedLabel = OfflineContentAndSyncManager.getLastSyncTimestampLabel(appContext),
            notifyNewContentAlerts = prefs.getBoolean("notify_new_content", true),
            notifyImportantUpdates = prefs.getBoolean("notify_important_updates", true),
            notifyPersonalizedActivity = prefs.getBoolean("notify_personalized_activity", true),
            notificationFeed = realNotifs,
            azanAlerts = azanAlertsMap,
            language = lang,
            themeMode = theme,
            selectedDistrict = district,
            calculationMethod = method,
            madhhab = madhhab,
            hijriOffsetDays = hijriOffset,
            schedule = todaySchedule,
            selectedTasbihPreset = activePreset,
            tasbihCurrentCount = currentCountForActive,
            tasbihTarget = savedTarget,
            tasbihCycle = savedCycle,
            tasbihTodayTotal = tasbihToday,
            tasbihAllTimeTotal = prefs.getLong("tasbih_all_time", 0L),
            tasbihCountsByPreset = presetCountsMap,
            duaCountsById = duaCountsMap,
            favoriteDuaIds = favDuas,
            qazaFajr = prefs.getLong("qaza_fajr", 0L),
            qazaDhuhr = prefs.getLong("qaza_dhuhr", 0L),
            qazaAsr = prefs.getLong("qaza_asr", 0L),
            qazaMaghrib = prefs.getLong("qaza_maghrib", 0L),
            qazaIsha = prefs.getLong("qaza_isha", 0L),
            qazaWitr = prefs.getLong("qaza_witr", 0L),
            salatLogsByDate = mapOf(todaySchedule.dateKey to defaultSalatLog)
        )
    }

    private var lastObservedWaqt: WaqtType? = null

    private fun startLivePrayerClock() {
        viewModelScope.launch {
            while (isActive) {
                refreshPrayerSchedule()
                val currentWaqt = _uiState.value.schedule.currentWaqt
                if (lastObservedWaqt != null && lastObservedWaqt != currentWaqt) {
                    val s = _uiState.value
                    if (s.azanAlerts[currentWaqt] == true) {
                        RuhaniPushNotificationManager.sendNotification(
                            context = appContext,
                            category = NotificationCategory.WAQT_AZAN,
                            title = "নামাজের ওয়াক্ত উপস্থিত: ${currentWaqt.nameBn}",
                            message = "${s.selectedDistrict.nameBn} জেলায় ${currentWaqt.nameBn} সালাতের ওয়াক্ত শুরু হয়েছে। ওযু করে জামাতে নামাজ আদায়ের প্রস্তুতি নিন।"
                        )
                        _uiState.update { it.copy(notificationFeed = RuhaniPushNotificationManager.getStoredNotifications(appContext)) }
                    }
                }
                lastObservedWaqt = currentWaqt
                delay(1000L)
            }
        }
    }

    fun refreshPrayerSchedule() {
        val s = _uiState.value
        val updated = AstronomicalEngine.calculateSchedule(
            district = s.selectedDistrict,
            method = s.calculationMethod,
            madhhab = s.madhhab,
            hijriOffsetDays = s.hijriOffsetDays,
            dayOffset = s.prayerDayOffset
        )
        _uiState.update { it.copy(schedule = updated) }
    }

    private fun observeCloudData(uid: String, repo: RuhaniFirestoreRepository) {
        // 1. Observe UserProfile
        viewModelScope.launch {
            repo.observeUserProfile()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val root = firestoreError ?: error
                    Log.w("RuhaniVM", "Profile listener error: ${root.message}")
                    _uiState.update { it.copy(errorMessage = root.localizedMessage) }
                }
                .collect { profile ->
                    if (profile != null) {
                        val dist = RuhaniContentCatalog.findDistrict(profile.district)
                        val method = CalculationMethod.fromId(profile.calculationMethod)
                        val madhhab = Madhhab.fromId(profile.madhhab)
                        _uiState.update { state ->
                            state.copy(
                                displayName = profile.displayName,
                                phone = profile.phone ?: state.phone,
                                selectedDistrict = dist,
                                calculationMethod = method,
                                madhhab = madhhab,
                                language = profile.language,
                                themeMode = profile.themeMode,
                                tasbihAllTimeTotal = maxOf(state.tasbihAllTimeTotal, profile.tasbihAllTimeTotal),
                                qazaFajr = profile.qazaFajr,
                                qazaDhuhr = profile.qazaDhuhr,
                                qazaAsr = profile.qazaAsr,
                                qazaMaghrib = profile.qazaMaghrib,
                                qazaIsha = profile.qazaIsha,
                                qazaWitr = profile.qazaWitr
                            )
                        }
                        refreshPrayerSchedule()
                    } else {
                        // Seed initial profile in Firestore from current local preferences
                        syncProfileToCloud()
                    }
                }
        }

        // 2. Observe SalatLogs
        viewModelScope.launch {
            repo.observeSalatLogs()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w("RuhaniVM", "SalatLogs listener error: ${error.message}")
                }
                .collect { logs ->
                    if (logs.isNotEmpty()) {
                        val map = logs.associateBy { it.dateKey }
                        _uiState.update { state ->
                            state.copy(salatLogsByDate = state.salatLogsByDate + map)
                        }
                    }
                }
        }

        // 3. Observe Bookmarks
        viewModelScope.launch {
            repo.observeBookmarks()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w("RuhaniVM", "Bookmarks listener error: ${error.message}")
                }
                .collect { bookmarks ->
                    _uiState.update { it.copy(bookmarks = bookmarks) }
                }
        }
    }

    fun syncProfileToCloud() {
        val uid = authenticatedUserId ?: return
        val repo = firestoreRepository ?: return
        val s = _uiState.value
        viewModelScope.launch {
            val profile = UserProfile(
                userId = uid,
                displayName = s.displayName.ifBlank { "মুসলিম ব্যবহারকারী" },
                email = s.userEmail,
                phone = s.phone.takeIf { it.isNotBlank() },
                district = s.selectedDistrict.id,
                calculationMethod = s.calculationMethod.id,
                madhhab = s.madhhab.id,
                language = s.language,
                themeMode = s.themeMode,
                tasbihAllTimeTotal = s.tasbihAllTimeTotal,
                qazaFajr = s.qazaFajr,
                qazaDhuhr = s.qazaDhuhr,
                qazaAsr = s.qazaAsr,
                qazaMaghrib = s.qazaMaghrib,
                qazaIsha = s.qazaIsha,
                qazaWitr = s.qazaWitr
            )
            val result = repo.saveUserProfile(profile)
            if (result.isSuccess) {
                _uiState.update { it.copy(syncStatusMessage = "ক্লাউডে সংরক্ষিত হয়েছে (Synced)") }
            }
        }
    }

    fun completeOnboarding(asGuest: Boolean) {
        prefs.edit()
            .putBoolean("onboarded", true)
            .putBoolean("guest_mode", asGuest)
            .apply()
        _uiState.update {
            it.copy(
                hasCompletedOnboarding = true,
                isGuestMode = asGuest
            )
        }
    }

    fun selectTab(tab: MainTab) {
        _uiState.update {
            it.copy(
                currentTab = tab,
                activeSubScreen = SubScreen.NONE
            )
        }
    }

    fun openSubScreen(screen: SubScreen) {
        _uiState.update { it.copy(activeSubScreen = screen) }
    }

    fun navigateBack(): Boolean {
        val current = _uiState.value
        if (current.activeSubScreen != SubScreen.NONE) {
            _uiState.update { it.copy(activeSubScreen = SubScreen.NONE) }
            return true
        }
        if (current.currentTab != MainTab.HOME) {
            _uiState.update { it.copy(currentTab = MainTab.HOME) }
            return true
        }
        return false
    }

    fun updateDistrict(district: BdDistrict) {
        prefs.edit().putString("district", district.id).apply()
        _uiState.update { it.copy(selectedDistrict = district) }
        refreshPrayerSchedule()
        com.example.domain.PrayerAlarmScheduler.schedulePrayerAlarms(appContext)
        syncProfileToCloud()
    }

    fun updateCalculationMethod(method: CalculationMethod) {
        prefs.edit().putString("calc_method", method.id).apply()
        _uiState.update { it.copy(calculationMethod = method) }
        refreshPrayerSchedule()
        com.example.domain.PrayerAlarmScheduler.schedulePrayerAlarms(appContext)
        syncProfileToCloud()
    }

    fun updateMadhhab(madhhab: Madhhab) {
        prefs.edit().putString("madhhab", madhhab.id).apply()
        _uiState.update { it.copy(madhhab = madhhab) }
        refreshPrayerSchedule()
        com.example.domain.PrayerAlarmScheduler.schedulePrayerAlarms(appContext)
        syncProfileToCloud()
    }

    fun updateThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _uiState.update { it.copy(themeMode = mode) }
        syncProfileToCloud()
    }

    fun updateLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
        _uiState.update { it.copy(language = lang) }
        syncProfileToCloud()
    }

    fun toggleBanglaDigits() {
        _uiState.update { it.copy(useBanglaDigits = !it.useBanglaDigits) }
    }

    fun updateHijriOffset(offsetDays: Int) {
        val clamped = offsetDays.coerceIn(-2, 2)
        prefs.edit().putInt("hijri_offset", clamped).apply()
        _uiState.update { it.copy(hijriOffsetDays = clamped) }
        refreshPrayerSchedule()
        com.example.domain.PrayerAlarmScheduler.schedulePrayerAlarms(appContext)
    }

    fun shiftPrayerScheduleDay(delta: Int) {
        val nextOffset = if (delta == 0) 0 else (_uiState.value.prayerDayOffset + delta).coerceIn(-30, 30)
        _uiState.update { it.copy(prayerDayOffset = nextOffset) }
        refreshPrayerSchedule()
    }

    fun toggleAzanAlert(waqt: WaqtType) {
        val current = _uiState.value.azanAlerts[waqt] ?: true
        val updated = !current
        prefs.edit().putBoolean("azan_alert_${waqt.name}", updated).apply()
        _uiState.update { state ->
            state.copy(azanAlerts = state.azanAlerts + (waqt to updated))
        }
        com.example.domain.PrayerAlarmScheduler.schedulePrayerAlarms(appContext)
    }

    fun updateProfileDetails(name: String, phone: String) {
        prefs.edit().putString("display_name", name).apply()
        _uiState.update {
            it.copy(
                displayName = name.ifBlank { "মুসলিম ব্যবহারকারী" },
                phone = phone
            )
        }
        syncProfileToCloud()
    }

    // --- DIGITAL TASBIH & LOCAL PERSISTENCE ACTIONS ---
    fun selectTasbihPreset(preset: TasbihPreset) {
        val savedCount = _uiState.value.tasbihCountsByPreset[preset.id]
            ?: prefs.getInt("tasbih_preset_count_${preset.id}", 0)
        prefs.edit()
            .putString("selected_tasbih_preset_id", preset.id)
            .putInt("tasbih_target", preset.defaultTarget)
            .putInt("tasbih_current", savedCount)
            .apply()
        _uiState.update {
            it.copy(
                selectedTasbihPreset = preset,
                tasbihTarget = preset.defaultTarget,
                tasbihCurrentCount = savedCount,
                tasbihCycle = 1
            )
        }
    }

    fun updateTasbihTarget(target: Int) {
        val safeTarget = target.coerceAtLeast(1)
        prefs.edit().putInt("tasbih_target", safeTarget).apply()
        _uiState.update { state ->
            val clampedCount = state.tasbihCurrentCount.coerceAtMost(safeTarget)
            state.copy(
                tasbihTarget = safeTarget,
                tasbihCurrentCount = clampedCount
            )
        }
    }

    fun incrementTasbih() {
        val s = _uiState.value
        if (s.tasbihVibrationEnabled) {
            triggerHapticTap()
        }
        if (s.tasbihSoundEnabled) {
            triggerSoundClick()
        }

        val nextCount: Int
        val nextCycle: Int
        if (s.tasbihCurrentCount + 1 > s.tasbihTarget) {
            nextCount = 1
            nextCycle = if (s.tasbihCycle >= s.tasbihTotalCycles) 1 else s.tasbihCycle + 1
        } else {
            nextCount = s.tasbihCurrentCount + 1
            nextCycle = s.tasbihCycle
        }

        val nextToday = s.tasbihTodayTotal + 1
        val nextAllTime = s.tasbihAllTimeTotal + 1L
        val presetId = s.selectedTasbihPreset.id
        val updatedPresetCounts = s.tasbihCountsByPreset + (presetId to nextCount)

        prefs.edit()
            .putInt("tasbih_current", nextCount)
            .putInt("tasbih_cycle", nextCycle)
            .putInt("tasbih_preset_count_$presetId", nextCount)
            .putInt("tasbih_today", nextToday)
            .putLong("tasbih_all_time", nextAllTime)
            .apply()

        _uiState.update {
            it.copy(
                tasbihCurrentCount = nextCount,
                tasbihCycle = nextCycle,
                tasbihTodayTotal = nextToday,
                tasbihAllTimeTotal = nextAllTime,
                tasbihCountsByPreset = updatedPresetCounts
            )
        }

        if (nextAllTime % 10L == 0L) {
            syncProfileToCloud()
        }
    }

    fun requestTasbihReset() {
        _uiState.update { it.copy(showTasbihResetDialog = true) }
    }

    fun dismissTasbihReset() {
        _uiState.update { it.copy(showTasbihResetDialog = false) }
    }

    fun confirmTasbihReset() {
        val presetId = _uiState.value.selectedTasbihPreset.id
        val updatedPresetCounts = _uiState.value.tasbihCountsByPreset + (presetId to 0)
        prefs.edit()
            .putInt("tasbih_current", 0)
            .putInt("tasbih_cycle", 1)
            .putInt("tasbih_preset_count_$presetId", 0)
            .apply()
        _uiState.update {
            it.copy(
                tasbihCurrentCount = 0,
                tasbihCycle = 1,
                tasbihCountsByPreset = updatedPresetCounts,
                showTasbihResetDialog = false
            )
        }
    }

    fun startNewTasbihCycle() {
        val presetId = _uiState.value.selectedTasbihPreset.id
        val nextCycle = (_uiState.value.tasbihCycle % _uiState.value.tasbihTotalCycles) + 1
        val updatedPresetCounts = _uiState.value.tasbihCountsByPreset + (presetId to 0)
        prefs.edit()
            .putInt("tasbih_current", 0)
            .putInt("tasbih_cycle", nextCycle)
            .putInt("tasbih_preset_count_$presetId", 0)
            .apply()
        _uiState.update {
            it.copy(
                tasbihCurrentCount = 0,
                tasbihCycle = nextCycle,
                tasbihCountsByPreset = updatedPresetCounts
            )
        }
    }

    fun toggleTasbihVibration() {
        _uiState.update { it.copy(tasbihVibrationEnabled = !it.tasbihVibrationEnabled) }
    }

    fun toggleTasbihSound() {
        _uiState.update { it.copy(tasbihSoundEnabled = !it.tasbihSoundEnabled) }
    }

    // --- DUA LOCAL COUNTER & FAVORITES ---
    fun incrementDuaCount(duaId: String, target: Int) {
        val current = _uiState.value.duaCountsById[duaId] ?: 0
        val next = if (current >= target) 1 else current + 1
        if (_uiState.value.tasbihVibrationEnabled) {
            triggerHapticTap()
        }
        prefs.edit().putInt("dua_count_$duaId", next).apply()
        _uiState.update { state ->
            state.copy(duaCountsById = state.duaCountsById + (duaId to next))
        }
    }

    fun resetDuaCount(duaId: String) {
        prefs.edit().putInt("dua_count_$duaId", 0).apply()
        _uiState.update { state ->
            state.copy(duaCountsById = state.duaCountsById + (duaId to 0))
        }
    }

    fun toggleFavoriteDua(duaId: String) {
        val currentFavs = _uiState.value.favoriteDuaIds
        val nextFavs = if (duaId in currentFavs) currentFavs - duaId else currentFavs + duaId
        prefs.edit().putStringSet("favorite_duas", nextFavs).apply()
        _uiState.update { it.copy(favoriteDuaIds = nextFavs) }
    }

    private fun triggerHapticTap() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(25L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25L)
            }
        } catch (e: Exception) {
            Log.d("RuhaniVM", "Vibration skipped: ${e.message}")
        }
    }

    private fun triggerSoundClick() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 45)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
        } catch (e: Exception) {
            Log.d("RuhaniVM", "Tone skipped: ${e.message}")
        }
    }

    // --- QURAN READER & AUDIO ACTIONS (The-Quran-Project/Quran-API) ---
    fun openSurah(surah: QuranSurah) {
        val defaultAyah = surah.ayahs.firstOrNull()?.ayahNumber ?: 1
        val reciterKey = QuranApiService.normalizeReciterKey(_uiState.value.selectedQari.id)
        val initialAudio = QuranApiService.buildStandardAudioMap(surah.number, defaultAyah)[reciterKey]
        _uiState.update {
            it.copy(
                selectedSurah = surah,
                lastReadSurahNumber = surah.number,
                lastReadAyahNumber = defaultAyah,
                playingSurahNumber = surah.number,
                playingAyahNumber = defaultAyah,
                currentAyahAudioUrl = initialAudio?.url ?: it.currentAyahAudioUrl,
                currentAyahOriginalUrl = initialAudio?.originalUrl ?: it.currentAyahOriginalUrl,
                currentAyahApiEndpoint = QuranApiService.buildAyahEndpointUrl(surah.number, defaultAyah),
                activeSubScreen = SubScreen.SURAH_READER,
                isSurahSyncingFromApi = surah.ayahs.size < surah.totalAyahs
            )
        }
        // Fetch full Surah verses from The-Quran-Project/Quran-API (https://quranapi.pages.dev/api/{surahNo}.json)
        viewModelScope.launch {
            val result = QuranApiService.fetchFullSurahAyahs(surah)
            result.onSuccess { fullAyahs ->
                _uiState.update { current ->
                    if (current.selectedSurah.number == surah.number) {
                        current.copy(
                            selectedSurah = current.selectedSurah.copy(ayahs = fullAyahs),
                            isSurahSyncingFromApi = false
                        )
                    } else {
                        current.copy(isSurahSyncingFromApi = false)
                    }
                }
            }.onFailure {
                _uiState.update { it.copy(isSurahSyncingFromApi = false) }
            }
        }
    }

    fun setMushafMode(mode: MushafMode) {
        _uiState.update {
            it.copy(
                mushafMode = mode,
                showWordByWord = mode == MushafMode.WORD_BY_WORD
            )
        }
    }

    fun toggleWordByWord() {
        _uiState.update { it.copy(showWordByWord = !it.showWordByWord) }
    }

    fun toggleHifzHideArabic() {
        _uiState.update { it.copy(hifzHideArabic = !it.hifzHideArabic) }
    }

    fun toggleHifzHideTranslation() {
        _uiState.update { it.copy(hifzHideTranslation = !it.hifzHideTranslation) }
    }

    fun adjustQuranFontSize(delta: Int) {
        _uiState.update {
            it.copy(quranArabicFontSizeSp = (it.quranArabicFontSizeSp + delta).coerceIn(20, 42))
        }
    }

    fun selectQari(qari: QariInfo) {
        val current = _uiState.value
        val surahNo = current.playingSurahNumber ?: current.selectedSurah.number
        val ayahNo = current.playingAyahNumber ?: 1
        val reciterKey = QuranApiService.normalizeReciterKey(qari.id)
        val audioInfo = QuranApiService.buildStandardAudioMap(surahNo, ayahNo)[reciterKey]
        _uiState.update {
            it.copy(
                selectedQari = qari,
                currentAyahAudioUrl = audioInfo?.url ?: it.currentAyahAudioUrl,
                currentAyahOriginalUrl = audioInfo?.originalUrl ?: it.currentAyahOriginalUrl
            )
        }
        if (current.isAudioPlaying) {
            val activeSurah = if (current.selectedSurah.number == surahNo) current.selectedSurah
            else QuranCatalog.surahs.firstOrNull { it.number == surahNo } ?: QuranCatalog.surahs.first()
            val activeAyah = activeSurah.ayahs.firstOrNull { it.ayahNumber == ayahNo }
                ?: activeSurah.ayahs.first()
            startAyahPlayback(activeSurah, activeAyah, resetRepeatCounter = true)
        }
    }

    fun toggleTafsirForAyah(surahNum: Int, ayahNum: Int) {
        val key = "${surahNum}_$ayahNum"
        _uiState.update {
            it.copy(expandedTafsirAyahKey = if (it.expandedTafsirAyahKey == key) null else key)
        }
    }

    fun playOrPauseAyah(surah: QuranSurah, ayah: QuranAyah) {
        val s = _uiState.value
        val isSameAyah = s.playingSurahNumber == surah.number && s.playingAyahNumber == ayah.ayahNumber
        if (isSameAyah && s.isAudioPlaying) {
            pauseAudio()
            return
        }
        startAyahPlayback(surah, ayah, resetRepeatCounter = true)
    }

    fun toggleGlobalAudioPlayPause() {
        val s = _uiState.value
        if (s.isAudioPlaying) {
            pauseAudio()
        } else {
            val surah = if (s.selectedSurah.number == s.playingSurahNumber) s.selectedSurah
            else QuranCatalog.surahs.firstOrNull { it.number == s.playingSurahNumber } ?: s.selectedSurah
            val ayah = surah.ayahs.firstOrNull { it.ayahNumber == s.playingAyahNumber } ?: surah.ayahs.firstOrNull() ?: return
            startAyahPlayback(surah, ayah, resetRepeatCounter = false)
        }
    }

    fun stepAyahAudio(delta: Int) {
        val s = _uiState.value
        val surah = if (s.selectedSurah.number == s.playingSurahNumber) s.selectedSurah
        else QuranCatalog.surahs.firstOrNull { it.number == s.playingSurahNumber } ?: s.selectedSurah
        if (surah.ayahs.isEmpty()) return
        val idx = surah.ayahs.indexOfFirst { it.ayahNumber == s.playingAyahNumber }.coerceAtLeast(0)
        val nextIdx = (idx + delta).coerceIn(0, surah.ayahs.lastIndex)
        startAyahPlayback(surah, surah.ayahs[nextIdx], resetRepeatCounter = true)
    }

    fun toggleAudioRepeat3x() {
        _uiState.update {
            it.copy(
                audioRepeat3x = !it.audioRepeat3x,
                ayahRepeatIteration = 1
            )
        }
    }

    fun toggleAudioAutoAdvance() {
        _uiState.update { it.copy(audioAutoAdvance = !it.audioAutoAdvance) }
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.setOnPreparedListener(null)
            mediaPlayer?.setOnCompletionListener(null)
            mediaPlayer?.setOnErrorListener(null)
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
        }
    }

    private fun startAyahPlayback(
        surah: QuranSurah,
        ayah: QuranAyah,
        resetRepeatCounter: Boolean = true
    ) {
        audioPlayJob?.cancel()
        releaseMediaPlayer()

        val reciterKey = QuranApiService.normalizeReciterKey(_uiState.value.selectedQari.id)
        val fallbackAudio = QuranApiService.buildStandardAudioMap(surah.number, ayah.ayahNumber)[reciterKey]
        val endpointUrl = QuranApiService.buildAyahEndpointUrl(surah.number, ayah.ayahNumber)

        _uiState.update {
            it.copy(
                playingSurahNumber = surah.number,
                playingAyahNumber = ayah.ayahNumber,
                lastReadSurahNumber = surah.number,
                lastReadAyahNumber = ayah.ayahNumber,
                isAudioPlaying = true,
                isAudioBuffering = true,
                audioProgressFraction = 0.02f,
                audioCurrentPositionMs = 0,
                audioDurationMs = 0,
                ayahRepeatIteration = if (resetRepeatCounter) 1 else it.ayahRepeatIteration,
                currentAyahAudioUrl = fallbackAudio?.url ?: "",
                currentAyahOriginalUrl = fallbackAudio?.originalUrl ?: "",
                currentAyahApiEndpoint = endpointUrl
            )
        }

        audioPlayJob = viewModelScope.launch {
            // 0. Check if Ayah audio is cached offline first
            val offlineFile = OfflineContentAndSyncManager.getOfflineAudioFile(
                appContext,
                reciterKey,
                surah.number,
                ayah.ayahNumber
            )
            val hasOfflineMp3 = offlineFile.exists() && offlineFile.length() > 1500L

            // 1. Fetch Ayah metadata & reciter audio URLs from GET https://quranapi.pages.dev/api/{surahNo}/{ayahNo}.json
            val apiResult = QuranApiService.fetchAyah(surah.number, ayah.ayahNumber)
            val resolvedAudio = apiResult.getOrNull()?.audioByReciter?.get(reciterKey) ?: fallbackAudio
            val primaryMp3Url = if (hasOfflineMp3) offlineFile.absolutePath else (resolvedAudio?.url ?: "")
            val backupMp3Url = resolvedAudio?.originalUrl ?: (resolvedAudio?.url ?: "")

            _uiState.update { state ->
                state.copy(
                    currentAyahAudioUrl = resolvedAudio?.url ?: fallbackAudio?.url ?: "",
                    currentAyahOriginalUrl = backupMp3Url,
                    currentAyahApiEndpoint = endpointUrl,
                    isPlayingFromOfflineCache = hasOfflineMp3 || (surah.number in state.downloadedSurahIds)
                )
            }

            // 2. Try streaming real MP3 audio via Android MediaPlayer (or local offline MP3)
            val streamedSuccess = streamMp3WithMediaPlayer(
                primaryUrl = primaryMp3Url,
                backupUrl = backupMp3Url,
                surah = surah,
                ayah = ayah
            )

            // 3. Fallback for offline / unit test environments without MediaPlayer network access
            if (!streamedSuccess && isActive && _uiState.value.isAudioPlaying) {
                _uiState.update { it.copy(isAudioBuffering = false) }
                if (ttsReady) {
                    try {
                        tts?.speak(
                            ayah.arabicText,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "ayah_${surah.number}_${ayah.ayahNumber}"
                        )
                    } catch (e: Exception) {
                        Log.d("RuhaniVM", "TTS speak fallback: ${e.message}")
                    }
                }
                val totalMs = 6000
                val steps = 24
                val stepMs = totalMs / steps
                for (i in 1..steps) {
                    if (!isActive || !_uiState.value.isAudioPlaying) return@launch
                    delay(stepMs.toLong())
                    _uiState.update {
                        it.copy(
                            audioProgressFraction = i.toFloat() / steps.toFloat(),
                            audioCurrentPositionMs = i * stepMs,
                            audioDurationMs = totalMs
                        )
                    }
                }
                onAyahPlaybackFinished(surah, ayah)
            }
        }
    }

    private suspend fun streamMp3WithMediaPlayer(
        primaryUrl: String,
        backupUrl: String,
        surah: QuranSurah,
        ayah: QuranAyah
    ): Boolean {
        if (primaryUrl.isBlank() && backupUrl.isBlank()) return false
        val urlsToTry = listOfNotNull(
            primaryUrl.takeIf { it.isNotBlank() },
            backupUrl.takeIf { it.isNotBlank() && it != primaryUrl }
        )

        for (candidateUrl in urlsToTry) {
            try {
                val player = MediaPlayer()
                mediaPlayer = player
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                var prepared = false
                var failed = false
                var completed = false

                player.setOnPreparedListener {
                    prepared = true
                }
                player.setOnErrorListener { _, _, _ ->
                    failed = true
                    true
                }
                player.setOnCompletionListener {
                    completed = true
                }

                player.setDataSource(candidateUrl)
                player.prepareAsync()

                // Wait up to 6 seconds for prepareAsync
                var waitedMs = 0
                while (!prepared && !failed && waitedMs < 6000 && _uiState.value.isAudioPlaying) {
                    delay(100L)
                    waitedMs += 100
                }

                if (!prepared || failed || !_uiState.value.isAudioPlaying) {
                    releaseMediaPlayer()
                    continue
                }

                player.start()
                val duration = player.duration.coerceAtLeast(1000)
                _uiState.update {
                    it.copy(
                        isAudioBuffering = false,
                        audioDurationMs = duration
                    )
                }

                while (!completed && !failed && _uiState.value.isAudioPlaying) {
                    val pos = try {
                        player.currentPosition.coerceAtLeast(0)
                    } catch (_: Exception) {
                        0
                    }
                    val total = try {
                        player.duration.takeIf { it > 0 } ?: duration
                    } catch (_: Exception) {
                        duration
                    }
                    val fraction = (pos.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    _uiState.update {
                        it.copy(
                            audioProgressFraction = fraction,
                            audioCurrentPositionMs = pos,
                            audioDurationMs = total
                        )
                    }
                    delay(150L)
                }

                releaseMediaPlayer()
                if (completed && _uiState.value.isAudioPlaying) {
                    _uiState.update {
                        it.copy(
                            audioProgressFraction = 1f,
                            audioCurrentPositionMs = it.audioDurationMs
                        )
                    }
                    onAyahPlaybackFinished(surah, ayah)
                    return true
                }
                if (!_uiState.value.isAudioPlaying) {
                    return true
                }
            } catch (e: Exception) {
                Log.d("RuhaniVM", "MediaPlayer candidate failed ($candidateUrl): ${e.message}")
                releaseMediaPlayer()
            }
        }
        return false
    }

    private fun onAyahPlaybackFinished(surah: QuranSurah, ayah: QuranAyah) {
        val current = _uiState.value
        val activeSurah = if (current.selectedSurah.number == surah.number) current.selectedSurah else surah
        val currentIdx = activeSurah.ayahs.indexOfFirst { it.ayahNumber == ayah.ayahNumber }

        if (current.audioRepeat3x && current.ayahRepeatIteration < 3) {
            _uiState.update { it.copy(ayahRepeatIteration = it.ayahRepeatIteration + 1) }
            startAyahPlayback(activeSurah, ayah, resetRepeatCounter = false)
        } else if (current.audioAutoAdvance && currentIdx in 0 until activeSurah.ayahs.lastIndex) {
            _uiState.update { it.copy(ayahRepeatIteration = 1) }
            startAyahPlayback(activeSurah, activeSurah.ayahs[currentIdx + 1], resetRepeatCounter = true)
        } else {
            _uiState.update {
                it.copy(
                    isAudioPlaying = false,
                    isAudioBuffering = false,
                    audioProgressFraction = 1f,
                    ayahRepeatIteration = 1
                )
            }
        }
    }

    fun pauseAudio() {
        audioPlayJob?.cancel()
        releaseMediaPlayer()
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        _uiState.update {
            it.copy(
                isAudioPlaying = false,
                isAudioBuffering = false
            )
        }
    }

    fun stopAndDismissAudio() {
        audioPlayJob?.cancel()
        releaseMediaPlayer()
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        _uiState.update {
            it.copy(
                isAudioPlaying = false,
                isAudioBuffering = false,
                playingSurahNumber = null,
                playingAyahNumber = null,
                audioProgressFraction = 0f,
                audioCurrentPositionMs = 0
            )
        }
    }

    fun speakArabicText(arabicText: String) {
        if (ttsReady) {
            try {
                tts?.speak(arabicText, TextToSpeech.QUEUE_FLUSH, null, "speak_${arabicText.hashCode()}")
            } catch (_: Exception) {
            }
        } else {
            triggerSoundClick()
        }
    }

    fun toggleBookmark(surah: QuranSurah, ayah: QuranAyah) {
        val docId = "s${surah.number}_a${ayah.ayahNumber}"
        val existing = _uiState.value.bookmarks.firstOrNull {
            it.id == docId || (it.surahNumber == surah.number.toLong() && it.ayahNumber == ayah.ayahNumber.toLong())
        }
        if (existing != null) {
            _uiState.update { state ->
                state.copy(bookmarks = state.bookmarks.filterNot { it.id == existing.id })
            }
            if (authenticatedUserId != null && firestoreRepository != null) {
                viewModelScope.launch {
                    firestoreRepository.removeBookmark(docId)
                }
            }
        } else {
            val bm = QuranBookmark(
                id = docId,
                userId = authenticatedUserId ?: "local",
                surahNumber = surah.number.toLong(),
                ayahNumber = ayah.ayahNumber.toLong(),
                surahNameBn = surah.nameBn,
                surahNameEn = surah.nameEn,
                arabicPreview = ayah.arabicText,
                translationPreview = ayah.translationBn,
                note = "${surah.nameBn} • আয়াত ${ayah.ayahNumber}"
            )
            _uiState.update { state ->
                state.copy(bookmarks = state.bookmarks + bm)
            }
            if (authenticatedUserId != null && firestoreRepository != null) {
                viewModelScope.launch {
                    firestoreRepository.addBookmark(bm)
                }
            }
        }
    }

    fun downloadAyahAudio(surahNumber: Int, ayahNumber: Int) {
        val key = "${surahNumber}_${ayahNumber}"
        val current = _uiState.value
        if (key in current.downloadedAyahKeys || current.downloadingAyahKey == key) return

        _uiState.update { it.copy(downloadingAyahKey = key) }
        val reciterKey = QuranApiService.normalizeReciterKey(current.selectedQari.id)
        viewModelScope.launch {
            val success = OfflineContentAndSyncManager.downloadAyahAudioForOffline(
                context = appContext,
                reciterKey = reciterKey,
                surahNumber = surahNumber,
                ayahNumber = ayahNumber
            )
            if (success) {
                val updatedKeys = _uiState.value.downloadedAyahKeys + key
                prefs.edit().putStringSet("downloaded_ayah_keys", updatedKeys).apply()
                _uiState.update { state ->
                    state.copy(
                        downloadedAyahKeys = updatedKeys,
                        downloadingAyahKey = null,
                        offlineStorageUsedKb = OfflineContentAndSyncManager.calculateOfflineStorageSizeKb(appContext),
                        syncStatusMessage = "আয়াত $surahNumber:$ayahNumber এর অডিও ডাউনলোড সম্পন্ন হয়েছে"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        downloadingAyahKey = null,
                        errorMessage = "আয়াতের অডিও ডাউনলোড ব্যর্থ হয়েছে"
                    )
                }
            }
        }
    }

    fun toggleOfflineSurahDownload(surahNumber: Int) {
        val current = _uiState.value
        if (surahNumber in current.downloadedSurahIds) {
            OfflineContentAndSyncManager.removeOfflineSurah(appContext, surahNumber)
            _uiState.update { state ->
                state.copy(
                    downloadedSurahIds = state.downloadedSurahIds - surahNumber,
                    offlineStorageUsedKb = OfflineContentAndSyncManager.calculateOfflineStorageSizeKb(appContext),
                    syncStatusMessage = "অফলাইন মেমরি থেকে সূরা #$surahNumber সরানো হয়েছে"
                )
            }
        } else {
            val surah = QuranCatalog.surahs.firstOrNull { it.number == surahNumber } ?: return
            val reciterKey = QuranApiService.normalizeReciterKey(current.selectedQari.id)
            _uiState.update { it.copy(downloadingSurahId = surahNumber) }
            viewModelScope.launch {
                OfflineContentAndSyncManager.downloadSurahForOffline(appContext, surah, reciterKey)
                _uiState.update { state ->
                    state.copy(
                        downloadedSurahIds = state.downloadedSurahIds + surahNumber,
                        downloadingSurahId = null,
                        offlineStorageUsedKb = OfflineContentAndSyncManager.calculateOfflineStorageSizeKb(appContext),
                        syncStatusMessage = "${surah.nameBn} (অডিও ও তাফসীর) অফলাইনে ডাউনলোড সম্পন্ন হয়েছে"
                    )
                }
            }
        }
    }

    fun toggleOfflineArticleDownload(article: OfflineArticleItem) {
        val current = _uiState.value
        if (article.id in current.downloadedArticleIds) {
            OfflineContentAndSyncManager.removeOfflineArticle(appContext, article.id)
            _uiState.update { state ->
                state.copy(
                    downloadedArticleIds = state.downloadedArticleIds - article.id,
                    offlineStorageUsedKb = OfflineContentAndSyncManager.calculateOfflineStorageSizeKb(appContext),
                    syncStatusMessage = "অফলাইন তালিকা থেকে প্রবন্ধটি সরানো হয়েছে"
                )
            }
        } else {
            viewModelScope.launch {
                OfflineContentAndSyncManager.downloadArticleForOffline(appContext, article)
                _uiState.update { state ->
                    state.copy(
                        downloadedArticleIds = state.downloadedArticleIds + article.id,
                        offlineStorageUsedKb = OfflineContentAndSyncManager.calculateOfflineStorageSizeKb(appContext),
                        syncStatusMessage = "‘${article.titleBn}’ অফলাইনে পড়ার ও শোনার জন্য সংরক্ষিত হয়েছে"
                    )
                }
            }
        }
    }

    fun updateArticleReadingProgress(article: OfflineArticleItem, progressPercent: Int) {
        val updatedMap = OfflineContentAndSyncManager.saveArticleReadProgress(
            appContext,
            article.id,
            progressPercent
        )
        val queue = OfflineContentAndSyncManager.enqueuePendingSyncAction(
            context = appContext,
            type = "ARTICLE_PROGRESS",
            descriptionBn = "প্রবন্ধ পাঠের অগ্রগতি: ${article.titleBn} ($progressPercent%)",
            payloadJson = "{\"articleId\":\"${article.id}\",\"progress\":$progressPercent}"
        )
        _uiState.update {
            it.copy(
                articleReadProgress = updatedMap,
                pendingSyncQueue = queue,
                syncStatusMessage = "পাঠের অগ্রগতি ($progressPercent%) সংরক্ষিত হয়েছে"
            )
        }
        if (_uiState.value.isDeviceOnline && authenticatedUserId != null) {
            synchronizeOfflineChangesNow()
        }
    }

    fun synchronizeOfflineChangesNow() {
        viewModelScope.launch {
            val s = _uiState.value
            if (authenticatedUserId != null && firestoreRepository != null && s.privacyCloudSyncConsent) {
                syncProfileToCloud()
                s.salatLogsByDate.values.forEach { log ->
                    firestoreRepository.saveSalatLog(log)
                }
            }
            val syncedTime = OfflineContentAndSyncManager.markQueueSynchronized(appContext)
            _uiState.update {
                it.copy(
                    pendingSyncQueue = emptyList(),
                    lastSyncedLabel = syncedTime,
                    syncStatusMessage = "সকল অফলাইন পরিবর্তন ও অগ্রগতি সফলভাবে সিঙ্ক হয়েছে ($syncedTime)"
                )
            }
        }
    }

    // --- SECURE EMAIL/PASSWORD AUTH & PRIVACY ACTIONS ---
    fun registerWithEmailPassword(
        displayName: String,
        email: String,
        password: String,
        privacyConsent: Boolean
    ) {
        val result = SecureAuthAndPrivacyManager.registerWithEmailPassword(
            context = appContext,
            displayName = displayName,
            email = email,
            password = password,
            privacyConsentAccepted = privacyConsent
        )
        result.onSuccess { session ->
            prefs.edit()
                .putBoolean("onboarded", true)
                .putBoolean("guest_mode", false)
                .putString("display_name", session.displayName)
                .apply()
            _uiState.update {
                it.copy(
                    hasCompletedOnboarding = true,
                    isGuestMode = false,
                    currentUserId = session.userId,
                    userEmail = session.email,
                    displayName = session.displayName,
                    authProvider = session.authProvider,
                    errorMessage = null,
                    syncStatusMessage = "ইমেইল অ্যাকাউন্ট নিবন্ধন সফল হয়েছে (${session.email})"
                )
            }
        }.onFailure { err ->
            _uiState.update { it.copy(errorMessage = err.message ?: "নিবন্ধন ব্যর্থ হয়েছে") }
        }
    }

    fun loginWithEmailPassword(email: String, password: String) {
        val result = SecureAuthAndPrivacyManager.loginWithEmailPassword(
            context = appContext,
            email = email,
            password = password
        )
        result.onSuccess { session ->
            prefs.edit()
                .putBoolean("onboarded", true)
                .putBoolean("guest_mode", false)
                .putString("display_name", session.displayName)
                .apply()
            _uiState.update {
                it.copy(
                    hasCompletedOnboarding = true,
                    isGuestMode = false,
                    currentUserId = session.userId,
                    userEmail = session.email,
                    displayName = session.displayName,
                    authProvider = session.authProvider,
                    errorMessage = null,
                    syncStatusMessage = "সফলভাবে লগইন সম্পন্ন হয়েছে (${session.email})"
                )
            }
        }.onFailure { err ->
            _uiState.update { it.copy(errorMessage = err.message ?: "লগইন ব্যর্থ হয়েছে") }
        }
    }

    fun signOutUserSession() {
        SecureAuthAndPrivacyManager.signOutLocalSession(appContext)
        _uiState.update {
            it.copy(
                currentUserId = null,
                userEmail = null,
                authProvider = "GUEST",
                isGuestMode = true,
                syncStatusMessage = "সাইন আউট সম্পন্ন হয়েছে"
            )
        }
    }

    fun updatePrivacySettings(
        cloudSyncConsent: Boolean,
        personalizedAlertsConsent: Boolean,
        dataMinimization: Boolean
    ) {
        SecureAuthAndPrivacyManager.updatePrivacyPreferences(
            context = appContext,
            cloudSyncConsent = cloudSyncConsent,
            personalizedAlertsConsent = personalizedAlertsConsent,
            dataMinimization = dataMinimization
        )
        _uiState.update {
            it.copy(
                privacyCloudSyncConsent = cloudSyncConsent,
                privacyPersonalizedAlertsConsent = personalizedAlertsConsent,
                privacyDataMinimization = dataMinimization,
                syncStatusMessage = "গোপনীয়তা ও ডেটা সুরক্ষা সেটিংস আপডেট হয়েছে"
            )
        }
    }

    fun exportMyPrivacyData() {
        val s = _uiState.value
        val json = SecureAuthAndPrivacyManager.exportUserDataJson(
            context = appContext,
            displayName = s.displayName,
            email = s.userEmail,
            district = s.selectedDistrict.nameEn,
            tasbihTotal = s.tasbihAllTimeTotal,
            bookmarksCount = s.bookmarks.size,
            downloadedSurahs = s.downloadedSurahIds
        )
        _uiState.update {
            it.copy(
                exportedUserDataJson = json,
                syncStatusMessage = "আপনার ব্যক্তিগত ডেটা JSON রিপোর্ট প্রস্তুত করা হয়েছে"
            )
        }
    }

    fun dismissExportedPrivacyData() {
        _uiState.update { it.copy(exportedUserDataJson = null) }
    }

    fun deleteAccountAndLocalData() {
        SecureAuthAndPrivacyManager.deleteAccountAndWipeLocalData(appContext)
        prefs.edit().clear().apply()
        _uiState.update {
            it.copy(
                currentUserId = null,
                userEmail = null,
                displayName = "মুসলিম ব্যবহারকারী",
                authProvider = "GUEST",
                isGuestMode = true,
                bookmarks = emptyList(),
                pendingSyncQueue = emptyList(),
                exportedUserDataJson = null,
                syncStatusMessage = "আপনার অ্যাকাউন্ট ও লোকাল ডেটা সম্পূর্ণ মুছে ফেলা হয়েছে"
            )
        }
    }

    // --- PUSH & PERSONALIZED NOTIFICATION ACTIONS ---
    fun toggleNotificationPreference(prefKey: String) {
        val s = _uiState.value
        when (prefKey) {
            "new_content" -> {
                val next = !s.notifyNewContentAlerts
                prefs.edit().putBoolean("notify_new_content", next).apply()
                _uiState.update { it.copy(notifyNewContentAlerts = next) }
            }
            "important_updates" -> {
                val next = !s.notifyImportantUpdates
                prefs.edit().putBoolean("notify_important_updates", next).apply()
                _uiState.update { it.copy(notifyImportantUpdates = next) }
            }
            "personalized_activity" -> {
                val next = !s.notifyPersonalizedActivity
                prefs.edit().putBoolean("notify_personalized_activity", next).apply()
                _uiState.update { it.copy(notifyPersonalizedActivity = next) }
            }
        }
    }

    fun triggerPersonalizedActivityNotification() {
        val s = _uiState.value
        val surahName = QuranCatalog.surahs.firstOrNull { it.number == s.lastReadSurahNumber }?.nameBn ?: "আল-ফাতিহা"
        RuhaniPushNotificationManager.sendNotification(
            context = appContext,
            category = NotificationCategory.PERSONALIZED,
            title = "ব্যক্তিগত আমল রিমাইন্ডার • ${s.displayName}",
            message = "সর্বশেষ তিলাওয়াত: সূরা $surahName (আয়াত ${s.lastReadAyahNumber})। আজকের তাসবিহ: ${s.tasbihTodayTotal} বার। আপনার দৈনিক আমল অব্যাহত রাখুন!"
        )
        _uiState.update {
            it.copy(
                notificationFeed = RuhaniPushNotificationManager.getStoredNotifications(appContext),
                syncStatusMessage = "ব্যক্তিগত আমল নোটিফিকেশন পাঠানো হয়েছে"
            )
        }
    }

    fun triggerNewContentNotification() {
        RuhaniPushNotificationManager.sendNotification(
            context = appContext,
            category = NotificationCategory.NEW_CONTENT,
            title = "নতুন কন্টেন্ট ও হিজরি আপডেট • রুহানি",
            message = "নতুন বাংলা হাদিস অধ্যায়, সূরা ইয়াসিন অফলাইন অডিও এবং আল-আযান হিজরি ক্যালেন্ডার আপডেট যুক্ত হয়েছে।"
        )
        _uiState.update {
            it.copy(
                notificationFeed = RuhaniPushNotificationManager.getStoredNotifications(appContext),
                syncStatusMessage = "নতুন কন্টেন্ট পুশ অ্যালার্ট পাঠানো হয়েছে"
            )
        }
    }

    fun triggerWaqtTestNotification() {
        val s = _uiState.value
        RuhaniPushNotificationManager.sendNotification(
            context = appContext,
            category = NotificationCategory.WAQT_AZAN,
            title = "ওয়াক্ত ও আযান অ্যালার্ট: ${s.schedule.nextWaqt.nameBn}",
            message = "${s.selectedDistrict.nameBn} জেলায় পরবর্তী ওয়াক্ত ${s.schedule.nextWaqt.nameBn} (${s.schedule.remainingHhMmSs} বাকি)।"
        )
        _uiState.update {
            it.copy(
                notificationFeed = RuhaniPushNotificationManager.getStoredNotifications(appContext),
                syncStatusMessage = "ওয়াক্ত ও আযান টেস্ট নোটিফিকেশন পাঠানো হয়েছে"
            )
        }
    }

    fun markAllNotificationsRead() {
        val updated = RuhaniPushNotificationManager.markAllNotificationsRead(appContext)
        _uiState.update { state ->
            state.copy(notificationFeed = updated)
        }
    }

    // --- SALAT & QAZA TRACKER ACTIONS ---
    fun toggleDailySalat(dateKey: String, prayerField: String) {
        val currentMap = _uiState.value.salatLogsByDate
        val existing = currentMap[dateKey] ?: SalatLog(
            id = dateKey,
            userId = authenticatedUserId ?: _uiState.value.currentUserId ?: "local",
            dateKey = dateKey
        )
        val updated = when (prayerField) {
            "fajr" -> existing.copy(fajr = !existing.fajr)
            "dhuhr" -> existing.copy(dhuhr = !existing.dhuhr)
            "asr" -> existing.copy(asr = !existing.asr)
            "maghrib" -> existing.copy(maghrib = !existing.maghrib)
            "isha" -> existing.copy(isha = !existing.isha)
            "witr" -> existing.copy(witr = !existing.witr)
            "duha" -> existing.copy(duha = !existing.duha)
            "awwabin" -> existing.copy(awwabin = !existing.awwabin)
            "tahajjud" -> existing.copy(tahajjud = !existing.tahajjud)
            "fasting" -> existing.copy(fasting = !existing.fasting)
            else -> existing
        }

        prefs.edit()
            .putBoolean("salat_${dateKey}_fajr", updated.fajr)
            .putBoolean("salat_${dateKey}_dhuhr", updated.dhuhr)
            .putBoolean("salat_${dateKey}_asr", updated.asr)
            .putBoolean("salat_${dateKey}_maghrib", updated.maghrib)
            .putBoolean("salat_${dateKey}_isha", updated.isha)
            .putBoolean("salat_${dateKey}_witr", updated.witr)
            .putBoolean("salat_${dateKey}_duha", updated.duha)
            .putBoolean("salat_${dateKey}_awwabin", updated.awwabin)
            .putBoolean("salat_${dateKey}_tahajjud", updated.tahajjud)
            .putBoolean("salat_${dateKey}_fasting", updated.fasting)
            .apply()

        val queue = OfflineContentAndSyncManager.enqueuePendingSyncAction(
            context = appContext,
            type = "SALAT_LOG",
            descriptionBn = "সালাত ট্র্যাকার আপডেট ($dateKey • ${prayerField.uppercase()})",
            payloadJson = "{\"dateKey\":\"$dateKey\",\"field\":\"$prayerField\"}"
        )

        _uiState.update { state ->
            state.copy(
                salatLogsByDate = state.salatLogsByDate + (dateKey to updated),
                pendingSyncQueue = queue
            )
        }

        if (authenticatedUserId != null && firestoreRepository != null && _uiState.value.isDeviceOnline) {
            viewModelScope.launch {
                firestoreRepository.saveSalatLog(updated)
                val syncedTime = OfflineContentAndSyncManager.markQueueSynchronized(appContext)
                _uiState.update { it.copy(pendingSyncQueue = emptyList(), lastSyncedLabel = syncedTime) }
            }
        }
    }

    fun adjustQazaCount(prayer: String, delta: Long) {
        val s = _uiState.value
        val nextState = when (prayer) {
            "fajr" -> s.copy(qazaFajr = (s.qazaFajr + delta).coerceAtLeast(0L))
            "dhuhr" -> s.copy(qazaDhuhr = (s.qazaDhuhr + delta).coerceAtLeast(0L))
            "asr" -> s.copy(qazaAsr = (s.qazaAsr + delta).coerceAtLeast(0L))
            "maghrib" -> s.copy(qazaMaghrib = (s.qazaMaghrib + delta).coerceAtLeast(0L))
            "isha" -> s.copy(qazaIsha = (s.qazaIsha + delta).coerceAtLeast(0L))
            "witr" -> s.copy(qazaWitr = (s.qazaWitr + delta).coerceAtLeast(0L))
            else -> s
        }
        prefs.edit()
            .putLong("qaza_fajr", nextState.qazaFajr)
            .putLong("qaza_dhuhr", nextState.qazaDhuhr)
            .putLong("qaza_asr", nextState.qazaAsr)
            .putLong("qaza_maghrib", nextState.qazaMaghrib)
            .putLong("qaza_isha", nextState.qazaIsha)
            .putLong("qaza_witr", nextState.qazaWitr)
            .apply()
        _uiState.value = nextState
        syncProfileToCloud()
    }

    // --- ZAKAT CALCULATOR ACTIONS ---
    fun setZakatNisabStandard(useSilver: Boolean) {
        _uiState.update { it.copy(zakatUseSilverNisab = useSilver) }
    }

    fun updateZakatInputs(cash: String, gold: String, business: String, debts: String) {
        _uiState.update {
            it.copy(
                zakatCashBdt = cash,
                zakatGoldValueBdt = gold,
                zakatBusinessBdt = business,
                zakatDebtsBdt = debts
            )
        }
    }

    fun selectHaramainChannel(channel: String) {
        _uiState.update { it.copy(haramainChannel = channel, isHaramainPlaying = true) }
    }

    fun toggleHaramainPlayback() {
        _uiState.update { it.copy(isHaramainPlaying = !it.isHaramainPlaying) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(syncStatusMessage = null, errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayJob?.cancel()
        releaseMediaPlayer()
        try {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            networkCallback?.let { cm?.unregisterNetworkCallback(it) }
        } catch (_: Exception) {
        }
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
    }
}
