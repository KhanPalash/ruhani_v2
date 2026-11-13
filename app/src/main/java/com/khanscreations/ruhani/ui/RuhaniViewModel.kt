package com.khanscreations.ruhani.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanscreations.ruhani.data.model.QuranBookmark
import com.khanscreations.ruhani.data.model.SalatLog
import com.khanscreations.ruhani.data.model.UserProfile
import com.khanscreations.ruhani.data.repository.RuhaniFirestoreRepository
import com.khanscreations.ruhani.domain.AstronomicalEngine
import com.khanscreations.ruhani.domain.BdDistrict
import com.khanscreations.ruhani.domain.CalculationMethod
import com.khanscreations.ruhani.domain.DailyPrayerSchedule
import com.khanscreations.ruhani.domain.Madhhab
import com.khanscreations.ruhani.domain.MushafMode
import com.khanscreations.ruhani.domain.OfflineArticleItem
import com.khanscreations.ruhani.domain.PendingSyncRecord
import com.khanscreations.ruhani.domain.QariInfo
import com.khanscreations.ruhani.domain.QuranAyah
import com.khanscreations.ruhani.domain.QuranCatalog
import com.khanscreations.ruhani.domain.QuranSurah
import com.khanscreations.ruhani.domain.RuhaniContentCatalog
import com.khanscreations.ruhani.domain.RuhaniNotificationItem
import com.khanscreations.ruhani.domain.TasbihPreset
import com.khanscreations.ruhani.domain.UpdateInfo
import com.khanscreations.ruhani.domain.WaqtType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val language: String = "bn",
    val useBanglaDigits: Boolean = false,
    val themeMode: String = "DARK",
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
    val azanAlerts: Map<WaqtType, Boolean> = mapOf(
        WaqtType.FAJR to true,
        WaqtType.DHUHR to true,
        WaqtType.ASR to true,
        WaqtType.MAGHRIB to true,
        WaqtType.ISHA to true
    ),
    val autoSilenceDuringSalat: Boolean = true,
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
    val duaCountsById: Map<String, Int> = emptyMap(),
    val favoriteDuaIds: Set<String> = emptySet(),
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
    val authProvider: String = "GUEST",
    val privacyCloudSyncConsent: Boolean = true,
    val privacyPersonalizedAlertsConsent: Boolean = true,
    val privacyDataMinimization: Boolean = true,
    val exportedUserDataJson: String? = null,
    val isDeviceOnline: Boolean = true,
    val downloadedArticleIds: Set<String> = emptySet(),
    val articleReadProgress: Map<String, Int> = emptyMap(),
    val offlineStorageUsedKb: Int = 0,
    val pendingSyncQueue: List<PendingSyncRecord> = emptyList(),
    val lastSyncedLabel: String = "আজ সংরক্ষিত (Just now)",
    val notifyNewContentAlerts: Boolean = true,
    val notifyImportantUpdates: Boolean = true,
    val notifyPersonalizedActivity: Boolean = true,
    val notificationFeed: List<RuhaniNotificationItem> = emptyList(),
    val salatLogsByDate: Map<String, SalatLog> = emptyMap(),
    val qazaFajr: Long = 0L,
    val qazaDhuhr: Long = 0L,
    val qazaAsr: Long = 0L,
    val qazaMaghrib: Long = 0L,
    val qazaIsha: Long = 0L,
    val qazaWitr: Long = 0L,
    val bookmarks: List<QuranBookmark> = emptyList(),
    val zakatUseSilverNisab: Boolean = true,
    val zakatGoldRatePerVori: Double = 138000.0,
    val zakatSilverRatePerVori: Double = 2100.0,
    val zakatCashBdt: String = "",
    val zakatGoldValueBdt: String = "",
    val zakatBusinessBdt: String = "",
    val zakatDebtsBdt: String = "",
    val haramainChannel: String = "MAKKAH",
    val isHaramainPlaying: Boolean = true,
    val syncStatusMessage: String? = null,
    val errorMessage: String? = null,
    val updateInfo: UpdateInfo? = null
)

class RuhaniViewModel(
    private val appContext: Context,
    private val firestoreRepository: RuhaniFirestoreRepository?,
    private val authenticatedUserId: String?,
    private val authenticatedEmail: String?,
    private val authenticatedName: String?
) : ViewModel() {

    private val prefs = appContext.getSharedPreferences("ruhani_local_prefs", Context.MODE_PRIVATE)

    val prayerViewModel: PrayerViewModel = PrayerViewModel(
        appContext = appContext,
        prefs = prefs,
        externalScope = viewModelScope
    )

    val tasbihViewModel: TasbihViewModel = TasbihViewModel(
        appContext = appContext,
        prefs = prefs,
        externalScope = viewModelScope
    )

    val quranAudioViewModel: QuranAudioViewModel = QuranAudioViewModel(
        appContext = appContext,
        prefs = prefs,
        externalScope = viewModelScope,
        firestoreRepository = firestoreRepository,
        userIdProvider = { authenticatedUserId ?: syncAndAccountViewModel.syncAndAccountState.value.currentUserId }
    )

    val syncAndAccountViewModel: SyncAndAccountViewModel = SyncAndAccountViewModel(
        appContext = appContext,
        prefs = prefs,
        externalScope = viewModelScope,
        firestoreRepository = firestoreRepository,
        authenticatedUserId = authenticatedUserId,
        authenticatedEmail = authenticatedEmail,
        authenticatedName = authenticatedName
    )

    val worshipToolsViewModel: WorshipToolsViewModel = WorshipToolsViewModel(
        appContext = appContext,
        prefs = prefs,
        externalScope = viewModelScope,
        firestoreRepository = firestoreRepository,
        userIdProvider = { authenticatedUserId ?: syncAndAccountViewModel.syncAndAccountState.value.currentUserId },
        isOnlineProvider = { syncAndAccountViewModel.syncAndAccountState.value.isDeviceOnline }
    )

    private val _uiState = MutableStateFlow(
        mergeStates(
            prayer = prayerViewModel.prayerState.value,
            tasbih = tasbihViewModel.tasbihState.value,
            quran = quranAudioViewModel.quranAudioState.value,
            account = syncAndAccountViewModel.syncAndAccountState.value,
            worship = worshipToolsViewModel.worshipToolsState.value
        )
    )
    val uiState: StateFlow<RuhaniUiState> = _uiState.asStateFlow()

    init {
        wireCallbacks()
        observeSubViewModels()
    }

    private fun wireCallbacks() {
        prayerViewModel.onProfileChanged = { syncProfileToCloud() }
        prayerViewModel.onNotificationTriggered = { syncAndAccountViewModel.refreshNotificationFeed() }

        tasbihViewModel.onTasbihMilestone = { syncProfileToCloud() }

        quranAudioViewModel.onSyncStatusMessage = { syncAndAccountViewModel.setSyncStatusMessage(it) }
        quranAudioViewModel.onErrorMessage = { syncAndAccountViewModel.setErrorMessage(it) }
        quranAudioViewModel.onStorageSizeChanged = { syncAndAccountViewModel.updateOfflineStorageSize(it) }

        syncAndAccountViewModel.onUserProfileLoaded = { profile ->
            prayerViewModel.applyProfileSettings(profile.district, profile.calculationMethod, profile.madhhab)
            tasbihViewModel.applyCloudTasbihTotal(profile.tasbihAllTimeTotal)
            worshipToolsViewModel.applyCloudQazaCounts(
                profile.qazaFajr, profile.qazaDhuhr, profile.qazaAsr,
                profile.qazaMaghrib, profile.qazaIsha, profile.qazaWitr
            )
        }
        syncAndAccountViewModel.onSalatLogsLoaded = { logs ->
            worshipToolsViewModel.applyCloudSalatLogs(logs)
        }
        syncAndAccountViewModel.onBookmarksLoaded = { bookmarks ->
            quranAudioViewModel.applyCloudBookmarks(bookmarks)
        }
        syncAndAccountViewModel.profileDataProvider = { buildUserProfile() }
        syncAndAccountViewModel.salatLogsProvider = {
            worshipToolsViewModel.worshipToolsState.value.salatLogsByDate.values.toList()
        }

        worshipToolsViewModel.onQazaChanged = { syncProfileToCloud() }
        worshipToolsViewModel.onSalatQueueUpdated = { queue -> syncAndAccountViewModel.setPendingSyncQueue(queue) }
        worshipToolsViewModel.onSalatSynced = { syncedTime ->
            syncAndAccountViewModel.setPendingSyncQueue(emptyList())
            syncAndAccountViewModel.setLastSyncedLabel(syncedTime)
        }
    }

    private fun observeSubViewModels() {
        viewModelScope.launch {
            prayerViewModel.prayerState.collect { p ->
                _uiState.update { current ->
                    current.copy(
                        selectedDistrict = p.selectedDistrict,
                        calculationMethod = p.calculationMethod,
                        madhhab = p.madhhab,
                        hijriOffsetDays = p.hijriOffsetDays,
                        prayerDayOffset = p.prayerDayOffset,
                        schedule = p.schedule,
                        azanAlerts = p.azanAlerts,
                        autoSilenceDuringSalat = p.autoSilenceDuringSalat
                    )
                }
            }
        }

        viewModelScope.launch {
            tasbihViewModel.tasbihState.collect { t ->
                _uiState.update { current ->
                    current.copy(
                        selectedTasbihPreset = t.selectedTasbihPreset,
                        tasbihCurrentCount = t.tasbihCurrentCount,
                        tasbihTarget = t.tasbihTarget,
                        tasbihCycle = t.tasbihCycle,
                        tasbihTotalCycles = t.tasbihTotalCycles,
                        tasbihTodayTotal = t.tasbihTodayTotal,
                        tasbihAllTimeTotal = t.tasbihAllTimeTotal,
                        tasbihCountsByPreset = t.tasbihCountsByPreset,
                        tasbihVibrationEnabled = t.tasbihVibrationEnabled,
                        tasbihSoundEnabled = t.tasbihSoundEnabled,
                        showTasbihResetDialog = t.showTasbihResetDialog,
                        duaCountsById = t.duaCountsById,
                        favoriteDuaIds = t.favoriteDuaIds
                    )
                }
            }
        }

        viewModelScope.launch {
            quranAudioViewModel.quranAudioState.collect { q ->
                _uiState.update { current ->
                    current.copy(
                        selectedSurah = q.selectedSurah,
                        lastReadSurahNumber = q.lastReadSurahNumber,
                        lastReadAyahNumber = q.lastReadAyahNumber,
                        mushafMode = q.mushafMode,
                        quranArabicFontSizeSp = q.quranArabicFontSizeSp,
                        showTransliteration = q.showTransliteration,
                        showWordByWord = q.showWordByWord,
                        hifzHideArabic = q.hifzHideArabic,
                        hifzHideTranslation = q.hifzHideTranslation,
                        selectedQari = q.selectedQari,
                        playingSurahNumber = q.playingSurahNumber,
                        playingAyahNumber = q.playingAyahNumber,
                        isAudioPlaying = q.isAudioPlaying,
                        isAudioBuffering = q.isAudioBuffering,
                        audioAutoAdvance = q.audioAutoAdvance,
                        audioProgressFraction = q.audioProgressFraction,
                        audioCurrentPositionMs = q.audioCurrentPositionMs,
                        audioDurationMs = q.audioDurationMs,
                        audioRepeat3x = q.audioRepeat3x,
                        ayahRepeatIteration = q.ayahRepeatIteration,
                        currentAyahAudioUrl = q.currentAyahAudioUrl,
                        currentAyahOriginalUrl = q.currentAyahOriginalUrl,
                        currentAyahApiEndpoint = q.currentAyahApiEndpoint,
                        isSurahSyncingFromApi = q.isSurahSyncingFromApi,
                        expandedTafsirAyahKey = q.expandedTafsirAyahKey,
                        downloadedSurahIds = q.downloadedSurahIds,
                        downloadingSurahId = q.downloadingSurahId,
                        downloadedAyahKeys = q.downloadedAyahKeys,
                        downloadingAyahKey = q.downloadingAyahKey,
                        isPlayingFromOfflineCache = q.isPlayingFromOfflineCache,
                        bookmarks = q.bookmarks
                    )
                }
            }
        }

        viewModelScope.launch {
            syncAndAccountViewModel.syncAndAccountState.collect { s ->
                _uiState.update { current ->
                    current.copy(
                        hasCompletedOnboarding = s.hasCompletedOnboarding,
                        isGuestMode = s.isGuestMode,
                        currentUserId = s.currentUserId,
                        userEmail = s.userEmail,
                        displayName = s.displayName,
                        phone = s.phone,
                        language = s.language,
                        useBanglaDigits = s.useBanglaDigits,
                        themeMode = s.themeMode,
                        authProvider = s.authProvider,
                        privacyCloudSyncConsent = s.privacyCloudSyncConsent,
                        privacyPersonalizedAlertsConsent = s.privacyPersonalizedAlertsConsent,
                        privacyDataMinimization = s.privacyDataMinimization,
                        exportedUserDataJson = s.exportedUserDataJson,
                        isDeviceOnline = s.isDeviceOnline,
                        downloadedArticleIds = s.downloadedArticleIds,
                        articleReadProgress = s.articleReadProgress,
                        offlineStorageUsedKb = s.offlineStorageUsedKb,
                        pendingSyncQueue = s.pendingSyncQueue,
                        lastSyncedLabel = s.lastSyncedLabel,
                        notifyNewContentAlerts = s.notifyNewContentAlerts,
                        notifyImportantUpdates = s.notifyImportantUpdates,
                        notifyPersonalizedActivity = s.notifyPersonalizedActivity,
                        notificationFeed = s.notificationFeed,
                        syncStatusMessage = s.syncStatusMessage,
                        errorMessage = s.errorMessage,
                        updateInfo = s.updateInfo
                    )
                }
            }
        }

        viewModelScope.launch {
            worshipToolsViewModel.worshipToolsState.collect { w ->
                _uiState.update { current ->
                    current.copy(
                        currentTab = w.currentTab,
                        activeSubScreen = w.activeSubScreen,
                        salatLogsByDate = w.salatLogsByDate,
                        qazaFajr = w.qazaFajr,
                        qazaDhuhr = w.qazaDhuhr,
                        qazaAsr = w.qazaAsr,
                        qazaMaghrib = w.qazaMaghrib,
                        qazaIsha = w.qazaIsha,
                        qazaWitr = w.qazaWitr,
                        zakatUseSilverNisab = w.zakatUseSilverNisab,
                        zakatGoldRatePerVori = w.zakatGoldRatePerVori,
                        zakatSilverRatePerVori = w.zakatSilverRatePerVori,
                        zakatCashBdt = w.zakatCashBdt,
                        zakatGoldValueBdt = w.zakatGoldValueBdt,
                        zakatBusinessBdt = w.zakatBusinessBdt,
                        zakatDebtsBdt = w.zakatDebtsBdt,
                        haramainChannel = w.haramainChannel,
                        isHaramainPlaying = w.isHaramainPlaying
                    )
                }
            }
        }
    }

    private fun mergeStates(
        prayer: PrayerUiState,
        tasbih: TasbihUiState,
        quran: QuranAudioUiState,
        account: SyncAndAccountUiState,
        worship: WorshipToolsUiState
    ): RuhaniUiState {
        return RuhaniUiState(
            hasCompletedOnboarding = account.hasCompletedOnboarding,
            isGuestMode = account.isGuestMode,
            currentUserId = account.currentUserId,
            userEmail = account.userEmail,
            displayName = account.displayName,
            phone = account.phone,
            language = account.language,
            useBanglaDigits = account.useBanglaDigits,
            themeMode = account.themeMode,
            authProvider = account.authProvider,
            privacyCloudSyncConsent = account.privacyCloudSyncConsent,
            privacyPersonalizedAlertsConsent = account.privacyPersonalizedAlertsConsent,
            privacyDataMinimization = account.privacyDataMinimization,
            exportedUserDataJson = account.exportedUserDataJson,
            isDeviceOnline = account.isDeviceOnline,
            downloadedArticleIds = account.downloadedArticleIds,
            articleReadProgress = account.articleReadProgress,
            offlineStorageUsedKb = account.offlineStorageUsedKb,
            pendingSyncQueue = account.pendingSyncQueue,
            lastSyncedLabel = account.lastSyncedLabel,
            notifyNewContentAlerts = account.notifyNewContentAlerts,
            notifyImportantUpdates = account.notifyImportantUpdates,
            notifyPersonalizedActivity = account.notifyPersonalizedActivity,
            notificationFeed = account.notificationFeed,
            syncStatusMessage = account.syncStatusMessage,
            errorMessage = account.errorMessage,
            updateInfo = account.updateInfo,
            selectedDistrict = prayer.selectedDistrict,
            calculationMethod = prayer.calculationMethod,
            madhhab = prayer.madhhab,
            hijriOffsetDays = prayer.hijriOffsetDays,
            prayerDayOffset = prayer.prayerDayOffset,
            schedule = prayer.schedule,
            azanAlerts = prayer.azanAlerts,
            autoSilenceDuringSalat = prayer.autoSilenceDuringSalat,
            selectedTasbihPreset = tasbih.selectedTasbihPreset,
            tasbihCurrentCount = tasbih.tasbihCurrentCount,
            tasbihTarget = tasbih.tasbihTarget,
            tasbihCycle = tasbih.tasbihCycle,
            tasbihTotalCycles = tasbih.tasbihTotalCycles,
            tasbihTodayTotal = tasbih.tasbihTodayTotal,
            tasbihAllTimeTotal = tasbih.tasbihAllTimeTotal,
            tasbihCountsByPreset = tasbih.tasbihCountsByPreset,
            tasbihVibrationEnabled = tasbih.tasbihVibrationEnabled,
            tasbihSoundEnabled = tasbih.tasbihSoundEnabled,
            showTasbihResetDialog = tasbih.showTasbihResetDialog,
            duaCountsById = tasbih.duaCountsById,
            favoriteDuaIds = tasbih.favoriteDuaIds,
            selectedSurah = quran.selectedSurah,
            lastReadSurahNumber = quran.lastReadSurahNumber,
            lastReadAyahNumber = quran.lastReadAyahNumber,
            mushafMode = quran.mushafMode,
            quranArabicFontSizeSp = quran.quranArabicFontSizeSp,
            showTransliteration = quran.showTransliteration,
            showWordByWord = quran.showWordByWord,
            hifzHideArabic = quran.hifzHideArabic,
            hifzHideTranslation = quran.hifzHideTranslation,
            selectedQari = quran.selectedQari,
            playingSurahNumber = quran.playingSurahNumber,
            playingAyahNumber = quran.playingAyahNumber,
            isAudioPlaying = quran.isAudioPlaying,
            isAudioBuffering = quran.isAudioBuffering,
            audioAutoAdvance = quran.audioAutoAdvance,
            audioProgressFraction = quran.audioProgressFraction,
            audioCurrentPositionMs = quran.audioCurrentPositionMs,
            audioDurationMs = quran.audioDurationMs,
            audioRepeat3x = quran.audioRepeat3x,
            ayahRepeatIteration = quran.ayahRepeatIteration,
            currentAyahAudioUrl = quran.currentAyahAudioUrl,
            currentAyahOriginalUrl = quran.currentAyahOriginalUrl,
            currentAyahApiEndpoint = quran.currentAyahApiEndpoint,
            isSurahSyncingFromApi = quran.isSurahSyncingFromApi,
            expandedTafsirAyahKey = quran.expandedTafsirAyahKey,
            downloadedSurahIds = quran.downloadedSurahIds,
            downloadingSurahId = quran.downloadingSurahId,
            downloadedAyahKeys = quran.downloadedAyahKeys,
            downloadingAyahKey = quran.downloadingAyahKey,
            isPlayingFromOfflineCache = quran.isPlayingFromOfflineCache,
            bookmarks = quran.bookmarks,
            currentTab = worship.currentTab,
            activeSubScreen = worship.activeSubScreen,
            salatLogsByDate = worship.salatLogsByDate,
            qazaFajr = worship.qazaFajr,
            qazaDhuhr = worship.qazaDhuhr,
            qazaAsr = worship.qazaAsr,
            qazaMaghrib = worship.qazaMaghrib,
            qazaIsha = worship.qazaIsha,
            qazaWitr = worship.qazaWitr,
            zakatUseSilverNisab = worship.zakatUseSilverNisab,
            zakatGoldRatePerVori = worship.zakatGoldRatePerVori,
            zakatSilverRatePerVori = worship.zakatSilverRatePerVori,
            zakatCashBdt = worship.zakatCashBdt,
            zakatGoldValueBdt = worship.zakatGoldValueBdt,
            zakatBusinessBdt = worship.zakatBusinessBdt,
            zakatDebtsBdt = worship.zakatDebtsBdt,
            haramainChannel = worship.haramainChannel,
            isHaramainPlaying = worship.isHaramainPlaying
        )
    }

    private fun buildUserProfile(): UserProfile {
        val s = _uiState.value
        val uid = authenticatedUserId ?: s.currentUserId ?: "local"
        return UserProfile(
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
    }

    // --- DELEGATE METHODS (PRESERVING 100% PUBLIC API COMPATIBILITY) ---

    fun dismissUpdateDialog() = syncAndAccountViewModel.dismissUpdateDialog()

    fun refreshPrayerSchedule() = prayerViewModel.refreshPrayerSchedule()

    fun syncProfileToCloud() = syncAndAccountViewModel.syncProfileToCloud()

    fun completeOnboarding(asGuest: Boolean) = syncAndAccountViewModel.completeOnboarding(asGuest)

    fun selectTab(tab: MainTab) = worshipToolsViewModel.selectTab(tab)

    fun openSubScreen(screen: SubScreen) = worshipToolsViewModel.openSubScreen(screen)

    fun navigateBack(): Boolean = worshipToolsViewModel.navigateBack()

    fun updateDistrict(district: BdDistrict) = prayerViewModel.updateDistrict(district)

    fun updateDistrictFromLocation(district: BdDistrict) = prayerViewModel.updateDistrictFromLocation(district)

    fun updateCalculationMethod(method: CalculationMethod) = prayerViewModel.updateCalculationMethod(method)

    fun updateMadhhab(madhhab: Madhhab) = prayerViewModel.updateMadhhab(madhhab)

    fun updateThemeMode(mode: String) = syncAndAccountViewModel.updateThemeMode(mode)

    fun updateLanguage(lang: String) = syncAndAccountViewModel.updateLanguage(lang)

    fun toggleBanglaDigits() = syncAndAccountViewModel.toggleBanglaDigits()

    fun updateHijriOffset(offsetDays: Int) = prayerViewModel.updateHijriOffset(offsetDays)

    fun shiftPrayerScheduleDay(delta: Int) = prayerViewModel.shiftPrayerScheduleDay(delta)

    fun toggleAzanAlert(waqt: WaqtType) = prayerViewModel.toggleAzanAlert(waqt)

    fun updateProfileDetails(name: String, phone: String) = syncAndAccountViewModel.updateProfileDetails(name, phone)

    fun selectTasbihPreset(preset: TasbihPreset) = tasbihViewModel.selectTasbihPreset(preset)

    fun updateTasbihTarget(target: Int) = tasbihViewModel.updateTasbihTarget(target)

    fun incrementTasbih() = tasbihViewModel.incrementTasbih()

    fun requestTasbihReset() = tasbihViewModel.requestTasbihReset()

    fun dismissTasbihReset() = tasbihViewModel.dismissTasbihReset()

    fun confirmTasbihReset() = tasbihViewModel.confirmTasbihReset()

    fun startNewTasbihCycle() = tasbihViewModel.startNewTasbihCycle()

    fun toggleTasbihVibration() = tasbihViewModel.toggleTasbihVibration()

    fun toggleTasbihSound() = tasbihViewModel.toggleTasbihSound()

    fun incrementDuaCount(duaId: String, target: Int) = tasbihViewModel.incrementDuaCount(duaId, target)

    fun resetDuaCount(duaId: String) = tasbihViewModel.resetDuaCount(duaId)

    fun toggleFavoriteDua(duaId: String) = tasbihViewModel.toggleFavoriteDua(duaId)

    fun openSurah(surah: QuranSurah) {
        quranAudioViewModel.openSurah(surah)
        worshipToolsViewModel.openSubScreen(SubScreen.SURAH_READER)
    }

    fun setMushafMode(mode: MushafMode) = quranAudioViewModel.setMushafMode(mode)

    fun toggleWordByWord() = quranAudioViewModel.toggleWordByWord()

    fun toggleHifzHideArabic() = quranAudioViewModel.toggleHifzHideArabic()

    fun toggleHifzHideTranslation() = quranAudioViewModel.toggleHifzHideTranslation()

    fun adjustQuranFontSize(delta: Int) = quranAudioViewModel.adjustQuranFontSize(delta)

    fun selectQari(qari: QariInfo) = quranAudioViewModel.selectQari(qari)

    fun toggleTafsirForAyah(surahNum: Int, ayahNum: Int) = quranAudioViewModel.toggleTafsirForAyah(surahNum, ayahNum)

    fun playOrPauseAyah(surah: QuranSurah, ayah: QuranAyah) = quranAudioViewModel.playOrPauseAyah(surah, ayah)

    fun toggleGlobalAudioPlayPause() = quranAudioViewModel.toggleGlobalAudioPlayPause()

    fun stepAyahAudio(delta: Int) = quranAudioViewModel.stepAyahAudio(delta)

    fun toggleAudioRepeat3x() = quranAudioViewModel.toggleAudioRepeat3x()

    fun toggleAudioAutoAdvance() = quranAudioViewModel.toggleAudioAutoAdvance()

    fun pauseAudio() = quranAudioViewModel.pauseAudio()

    fun stopAndDismissAudio() = quranAudioViewModel.stopAndDismissAudio()

    fun speakArabicText(arabicText: String) = tasbihViewModel.speakArabicText(arabicText)

    fun toggleBookmark(surah: QuranSurah, ayah: QuranAyah) = quranAudioViewModel.toggleBookmark(surah, ayah)

    fun downloadAyahAudio(surahNumber: Int, ayahNumber: Int) = quranAudioViewModel.downloadAyahAudio(surahNumber, ayahNumber)

    fun toggleOfflineSurahDownload(surahNumber: Int) = quranAudioViewModel.toggleOfflineSurahDownload(surahNumber)

    fun toggleOfflineArticleDownload(article: OfflineArticleItem) = syncAndAccountViewModel.toggleOfflineArticleDownload(article)

    fun updateArticleReadingProgress(article: OfflineArticleItem, progressPercent: Int) =
        syncAndAccountViewModel.updateArticleReadingProgress(article, progressPercent)

    fun synchronizeOfflineChangesNow() = syncAndAccountViewModel.synchronizeOfflineChangesNow()

    fun registerWithEmailPassword(
        displayName: String,
        email: String,
        password: String,
        privacyConsent: Boolean
    ) = syncAndAccountViewModel.registerWithEmailPassword(displayName, email, password, privacyConsent)

    fun loginWithEmailPassword(email: String, password: String) =
        syncAndAccountViewModel.loginWithEmailPassword(email, password)

    fun signOutUserSession() = syncAndAccountViewModel.signOutUserSession()

    fun updatePrivacySettings(
        cloudSyncConsent: Boolean,
        personalizedAlertsConsent: Boolean,
        dataMinimization: Boolean
    ) = syncAndAccountViewModel.updatePrivacySettings(cloudSyncConsent, personalizedAlertsConsent, dataMinimization)

    fun exportMyPrivacyData() {
        val s = _uiState.value
        syncAndAccountViewModel.exportMyPrivacyData(
            districtNameEn = s.selectedDistrict.nameEn,
            tasbihTotal = s.tasbihAllTimeTotal,
            bookmarksCount = s.bookmarks.size,
            downloadedSurahs = s.downloadedSurahIds
        )
    }

    fun dismissExportedPrivacyData() = syncAndAccountViewModel.dismissExportedPrivacyData()

    fun deleteAccountAndLocalData() {
        syncAndAccountViewModel.deleteAccountAndLocalData()
        _uiState.update {
            it.copy(
                bookmarks = emptyList(),
                exportedUserDataJson = null
            )
        }
    }

    fun toggleNotificationPreference(prefKey: String) = syncAndAccountViewModel.toggleNotificationPreference(prefKey)

    fun triggerPersonalizedActivityNotification() {
        val s = _uiState.value
        syncAndAccountViewModel.triggerPersonalizedActivityNotification(
            lastReadSurahNumber = s.lastReadSurahNumber,
            lastReadAyahNumber = s.lastReadAyahNumber,
            tasbihTodayTotal = s.tasbihTodayTotal
        )
    }

    fun triggerNewContentNotification() = syncAndAccountViewModel.triggerNewContentNotification()

    fun triggerWaqtTestNotification() {
        val s = _uiState.value
        syncAndAccountViewModel.triggerWaqtTestNotification(
            nextWaqtNameBn = s.schedule.nextWaqt.nameBn,
            remainingHhMmSs = s.schedule.remainingHhMmSs,
            districtNameBn = s.selectedDistrict.nameBn
        )
    }

    fun markAllNotificationsRead() = syncAndAccountViewModel.markAllNotificationsRead()

    fun toggleDailySalat(dateKey: String, prayerField: String) =
        worshipToolsViewModel.toggleDailySalat(dateKey, prayerField)

    fun adjustQazaCount(prayer: String, delta: Long) = worshipToolsViewModel.adjustQazaCount(prayer, delta)

    fun setZakatNisabStandard(useSilver: Boolean) = worshipToolsViewModel.setZakatNisabStandard(useSilver)

    fun updateZakatInputs(cash: String, gold: String, business: String, debts: String) =
        worshipToolsViewModel.updateZakatInputs(cash, gold, business, debts)

    fun selectHaramainChannel(channel: String) = worshipToolsViewModel.selectHaramainChannel(channel)

    fun toggleHaramainPlayback() = worshipToolsViewModel.toggleHaramainPlayback()

    fun clearStatusMessage() = syncAndAccountViewModel.clearStatusMessage()

    override fun onCleared() {
        super.onCleared()
        prayerViewModel.cleanup()
        tasbihViewModel.cleanup()
        quranAudioViewModel.cleanup()
        syncAndAccountViewModel.cleanup()
        worshipToolsViewModel.cleanup()
    }
}
