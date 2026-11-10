package com.example.ui

import android.content.Context
import android.media.AudioManager
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
import com.example.data.repository.RuhaniFirestoreRepository
import com.example.domain.AstronomicalEngine
import com.example.domain.BdDistrict
import com.example.domain.CalculationMethod
import com.example.domain.DailyPrayerSchedule
import com.example.domain.Madhhab
import com.example.domain.MushafMode
import com.example.domain.QariInfo
import com.example.domain.QuranAyah
import com.example.domain.QuranCatalog
import com.example.domain.QuranSurah
import com.example.domain.RuhaniContentCatalog
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
        WaqtType.DHUHR to false,
        WaqtType.ASR to true,
        WaqtType.MAGHRIB to true,
        WaqtType.ISHA to false
    ),
    val autoSilenceDuringSalat: Boolean = true,
    // Tasbih State
    val selectedTasbihPreset: TasbihPreset = RuhaniContentCatalog.tasbihPresets.first(),
    val tasbihCurrentCount: Int = 23,
    val tasbihTarget: Int = 33,
    val tasbihCycle: Int = 1,
    val tasbihTotalCycles: Int = 3,
    val tasbihTodayTotal: Int = 347,
    val tasbihAllTimeTotal: Long = 4220L,
    val tasbihVibrationEnabled: Boolean = true,
    val tasbihSoundEnabled: Boolean = false,
    val showTasbihResetDialog: Boolean = false,
    // Quran Reader & Audio State
    val selectedSurah: QuranSurah = QuranCatalog.surahs.first(),
    val lastReadSurahNumber: Int = 1,
    val lastReadAyahNumber: Int = 2,
    val mushafMode: MushafMode = MushafMode.AYAH_BY_AYAH,
    val quranArabicFontSizeSp: Int = 28,
    val showTransliteration: Boolean = true,
    val showWordByWord: Boolean = false,
    val hifzHideArabic: Boolean = false,
    val hifzHideTranslation: Boolean = false,
    val selectedQari: QariInfo = QuranCatalog.qariList.first(),
    val playingSurahNumber: Int? = 1,
    val playingAyahNumber: Int? = 2,
    val isAudioPlaying: Boolean = false,
    val audioProgressFraction: Float = 0.45f,
    val audioRepeat3x: Boolean = false,
    val expandedTafsirAyahKey: String? = null,
    val downloadedSurahIds: Set<Int> = setOf(1, 36, 67, 112, 113, 114),
    // Salat & Qaza Tracker
    val salatLogsByDate: Map<String, SalatLog> = emptyMap(),
    val qazaFajr: Long = 2L,
    val qazaDhuhr: Long = 1L,
    val qazaAsr: Long = 0L,
    val qazaMaghrib: Long = 0L,
    val qazaIsha: Long = 1L,
    val qazaWitr: Long = 1L,
    // Bookmarks
    val bookmarks: List<QuranBookmark> = emptyList(),
    // Zakat Calculator
    val zakatUseSilverNisab: Boolean = true,
    val zakatGoldRatePerVori: Double = 138000.0,
    val zakatSilverRatePerVori: Double = 2100.0,
    val zakatCashBdt: String = "250000",
    val zakatGoldValueBdt: String = "0",
    val zakatBusinessBdt: String = "50000",
    val zakatDebtsBdt: String = "15000",
    // Haramain Live Stream State
    val haramainChannel: String = "MAKKAH", // "MAKKAH" or "MADINAH"
    val isHaramainPlaying: Boolean = true,
    // Status / Sync message
    val syncStatusMessage: String? = null,
    val errorMessage: String? = null
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
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        initTts()
        startLivePrayerClock()
        if (authenticatedUserId != null && firestoreRepository != null) {
            observeCloudData(authenticatedUserId, firestoreRepository)
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
        val displayName = authenticatedName?.takeIf { it.isNotBlank() }
            ?: prefs.getString("display_name", "মুসলিম ব্যবহারকারী")
            ?: "মুসলিম ব্যবহারকারী"

        val todaySchedule = AstronomicalEngine.calculateSchedule(
            district = district,
            method = method,
            madhhab = madhhab,
            hijriOffsetDays = hijriOffset
        )

        val defaultSalatLog = SalatLog(
            id = todaySchedule.dateKey,
            userId = authenticatedUserId ?: "local",
            dateKey = todaySchedule.dateKey,
            fajr = prefs.getBoolean("salat_${todaySchedule.dateKey}_fajr", true),
            dhuhr = prefs.getBoolean("salat_${todaySchedule.dateKey}_dhuhr", true),
            asr = prefs.getBoolean("salat_${todaySchedule.dateKey}_asr", false),
            maghrib = prefs.getBoolean("salat_${todaySchedule.dateKey}_maghrib", false),
            isha = prefs.getBoolean("salat_${todaySchedule.dateKey}_isha", false),
            witr = prefs.getBoolean("salat_${todaySchedule.dateKey}_witr", false),
            duha = prefs.getBoolean("salat_${todaySchedule.dateKey}_duha", true),
            awwabin = prefs.getBoolean("salat_${todaySchedule.dateKey}_awwabin", false),
            tahajjud = prefs.getBoolean("salat_${todaySchedule.dateKey}_tahajjud", false),
            fasting = prefs.getBoolean("salat_${todaySchedule.dateKey}_fasting", false)
        )

        return RuhaniUiState(
            hasCompletedOnboarding = onboarded || authenticatedUserId != null,
            isGuestMode = guest,
            currentUserId = authenticatedUserId,
            userEmail = authenticatedEmail,
            displayName = displayName,
            language = lang,
            themeMode = theme,
            selectedDistrict = district,
            calculationMethod = method,
            madhhab = madhhab,
            hijriOffsetDays = hijriOffset,
            schedule = todaySchedule,
            tasbihCurrentCount = prefs.getInt("tasbih_current", 23),
            tasbihTodayTotal = prefs.getInt("tasbih_today", 347),
            tasbihAllTimeTotal = prefs.getLong("tasbih_all_time", 4220L),
            qazaFajr = prefs.getLong("qaza_fajr", 2L),
            qazaDhuhr = prefs.getLong("qaza_dhuhr", 1L),
            qazaAsr = prefs.getLong("qaza_asr", 0L),
            qazaMaghrib = prefs.getLong("qaza_maghrib", 0L),
            qazaIsha = prefs.getLong("qaza_isha", 1L),
            qazaWitr = prefs.getLong("qaza_witr", 1L),
            salatLogsByDate = mapOf(todaySchedule.dateKey to defaultSalatLog)
        )
    }

    private fun startLivePrayerClock() {
        viewModelScope.launch {
            while (isActive) {
                refreshPrayerSchedule()
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
        syncProfileToCloud()
    }

    fun updateCalculationMethod(method: CalculationMethod) {
        prefs.edit().putString("calc_method", method.id).apply()
        _uiState.update { it.copy(calculationMethod = method) }
        refreshPrayerSchedule()
        syncProfileToCloud()
    }

    fun updateMadhhab(madhhab: Madhhab) {
        prefs.edit().putString("madhhab", madhhab.id).apply()
        _uiState.update { it.copy(madhhab = madhhab) }
        refreshPrayerSchedule()
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
    }

    fun shiftPrayerScheduleDay(delta: Int) {
        val nextOffset = if (delta == 0) 0 else (_uiState.value.prayerDayOffset + delta).coerceIn(-30, 30)
        _uiState.update { it.copy(prayerDayOffset = nextOffset) }
        refreshPrayerSchedule()
    }

    fun toggleAzanAlert(waqt: WaqtType) {
        _uiState.update { state ->
            val current = state.azanAlerts[waqt] ?: false
            state.copy(azanAlerts = state.azanAlerts + (waqt to !current))
        }
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

    // --- DIGITAL TASBIH ACTIONS ---
    fun selectTasbihPreset(preset: TasbihPreset) {
        _uiState.update {
            it.copy(
                selectedTasbihPreset = preset,
                tasbihTarget = preset.defaultTarget,
                tasbihCurrentCount = 0,
                tasbihCycle = 1
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

        prefs.edit()
            .putInt("tasbih_current", nextCount)
            .putInt("tasbih_today", nextToday)
            .putLong("tasbih_all_time", nextAllTime)
            .apply()

        _uiState.update {
            it.copy(
                tasbihCurrentCount = nextCount,
                tasbihCycle = nextCycle,
                tasbihTodayTotal = nextToday,
                tasbihAllTimeTotal = nextAllTime
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
        prefs.edit().putInt("tasbih_current", 0).apply()
        _uiState.update {
            it.copy(
                tasbihCurrentCount = 0,
                tasbihCycle = 1,
                showTasbihResetDialog = false
            )
        }
    }

    fun startNewTasbihCycle() {
        _uiState.update {
            it.copy(
                tasbihCurrentCount = 0,
                tasbihCycle = (it.tasbihCycle % it.tasbihTotalCycles) + 1
            )
        }
    }

    fun toggleTasbihVibration() {
        _uiState.update { it.copy(tasbihVibrationEnabled = !it.tasbihVibrationEnabled) }
    }

    fun toggleTasbihSound() {
        _uiState.update { it.copy(tasbihSoundEnabled = !it.tasbihSoundEnabled) }
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

    // --- QURAN READER & AUDIO ACTIONS ---
    fun openSurah(surah: QuranSurah) {
        _uiState.update {
            it.copy(
                selectedSurah = surah,
                lastReadSurahNumber = surah.number,
                lastReadAyahNumber = 1,
                activeSubScreen = SubScreen.SURAH_READER
            )
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
        _uiState.update { it.copy(selectedQari = qari) }
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
        startAyahPlayback(surah, ayah)
    }

    fun toggleGlobalAudioPlayPause() {
        val s = _uiState.value
        if (s.isAudioPlaying) {
            pauseAudio()
        } else {
            val surah = QuranCatalog.surahs.firstOrNull { it.number == s.playingSurahNumber } ?: s.selectedSurah
            val ayah = surah.ayahs.firstOrNull { it.ayahNumber == s.playingAyahNumber } ?: surah.ayahs.first()
            startAyahPlayback(surah, ayah)
        }
    }

    fun stepAyahAudio(delta: Int) {
        val s = _uiState.value
        val surah = QuranCatalog.surahs.firstOrNull { it.number == s.playingSurahNumber } ?: s.selectedSurah
        val idx = surah.ayahs.indexOfFirst { it.ayahNumber == s.playingAyahNumber }.coerceAtLeast(0)
        val nextIdx = (idx + delta).coerceIn(0, surah.ayahs.lastIndex)
        startAyahPlayback(surah, surah.ayahs[nextIdx])
    }

    fun toggleAudioRepeat3x() {
        _uiState.update { it.copy(audioRepeat3x = !it.audioRepeat3x) }
    }

    private fun startAyahPlayback(surah: QuranSurah, ayah: QuranAyah) {
        audioPlayJob?.cancel()
        _uiState.update {
            it.copy(
                playingSurahNumber = surah.number,
                playingAyahNumber = ayah.ayahNumber,
                lastReadSurahNumber = surah.number,
                lastReadAyahNumber = ayah.ayahNumber,
                isAudioPlaying = true,
                audioProgressFraction = 0.05f
            )
        }
        if (ttsReady) {
            try {
                tts?.speak(ayah.arabicText, TextToSpeech.QUEUE_FLUSH, null, "ayah_${surah.number}_${ayah.ayahNumber}")
            } catch (e: Exception) {
                Log.d("RuhaniVM", "TTS speak fallback: ${e.message}")
            }
        }
        audioPlayJob = viewModelScope.launch {
            val steps = 30
            for (i in 1..steps) {
                delay(250L)
                _uiState.update { it.copy(audioProgressFraction = i.toFloat() / steps.toFloat()) }
            }
            val current = _uiState.value
            val currentIdx = surah.ayahs.indexOfFirst { it.ayahNumber == ayah.ayahNumber }
            if (current.audioRepeat3x) {
                startAyahPlayback(surah, ayah)
            } else if (currentIdx in 0 until surah.ayahs.lastIndex) {
                startAyahPlayback(surah, surah.ayahs[currentIdx + 1])
            } else {
                _uiState.update { it.copy(isAudioPlaying = false, audioProgressFraction = 1f) }
            }
        }
    }

    private fun pauseAudio() {
        audioPlayJob?.cancel()
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        _uiState.update { it.copy(isAudioPlaying = false) }
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

    fun toggleOfflineSurahDownload(surahNumber: Int) {
        _uiState.update { state ->
            val next = if (surahNumber in state.downloadedSurahIds) {
                state.downloadedSurahIds - surahNumber
            } else {
                state.downloadedSurahIds + surahNumber
            }
            state.copy(downloadedSurahIds = next)
        }
    }

    // --- SALAT & QAZA TRACKER ACTIONS ---
    fun toggleDailySalat(dateKey: String, prayerField: String) {
        val currentMap = _uiState.value.salatLogsByDate
        val existing = currentMap[dateKey] ?: SalatLog(
            id = dateKey,
            userId = authenticatedUserId ?: "local",
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

        _uiState.update { state ->
            state.copy(salatLogsByDate = state.salatLogsByDate + (dateKey to updated))
        }

        if (authenticatedUserId != null && firestoreRepository != null) {
            viewModelScope.launch {
                firestoreRepository.saveSalatLog(updated)
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
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
    }
}
