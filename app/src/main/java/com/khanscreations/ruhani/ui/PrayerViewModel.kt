package com.khanscreations.ruhani.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanscreations.ruhani.domain.AstronomicalEngine
import com.khanscreations.ruhani.domain.BdDistrict
import com.khanscreations.ruhani.domain.CalculationMethod
import com.khanscreations.ruhani.domain.DailyPrayerSchedule
import com.khanscreations.ruhani.domain.Madhhab
import com.khanscreations.ruhani.domain.NotificationCategory
import com.khanscreations.ruhani.domain.PrayerAlarmScheduler
import com.khanscreations.ruhani.domain.RuhaniContentCatalog
import com.khanscreations.ruhani.domain.RuhaniPushNotificationManager
import com.khanscreations.ruhani.domain.WaqtType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PrayerUiState(
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
    val azanAlerts: Map<WaqtType, Boolean> = mapOf(
        WaqtType.FAJR to true,
        WaqtType.DHUHR to true,
        WaqtType.ASR to true,
        WaqtType.MAGHRIB to true,
        WaqtType.ISHA to true
    ),
    val autoSilenceDuringSalat: Boolean = true
)

class PrayerViewModel(
    private val appContext: Context,
    private val prefs: SharedPreferences = appContext.getSharedPreferences("ruhani_local_prefs", Context.MODE_PRIVATE),
    externalScope: CoroutineScope? = null,
    var onProfileChanged: (() -> Unit)? = null,
    var onNotificationTriggered: (() -> Unit)? = null
) : ViewModel() {

    private val scope: CoroutineScope = externalScope ?: viewModelScope
    private val _prayerState = MutableStateFlow(loadInitialState())
    val prayerState: StateFlow<PrayerUiState> = _prayerState.asStateFlow()

    private var clockJob: Job? = null
    private var lastObservedWaqt: WaqtType? = null

    init {
        PrayerAlarmScheduler.schedulePrayerAlarms(appContext)
        startLivePrayerClock()
    }

    private fun loadInitialState(): PrayerUiState {
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
            hijriOffsetDays = hijriOffset
        )

        val azanAlertsMap = mapOf(
            WaqtType.FAJR to prefs.getBoolean("azan_alert_${WaqtType.FAJR.name}", true),
            WaqtType.DHUHR to prefs.getBoolean("azan_alert_${WaqtType.DHUHR.name}", true),
            WaqtType.ASR to prefs.getBoolean("azan_alert_${WaqtType.ASR.name}", true),
            WaqtType.MAGHRIB to prefs.getBoolean("azan_alert_${WaqtType.MAGHRIB.name}", true),
            WaqtType.ISHA to prefs.getBoolean("azan_alert_${WaqtType.ISHA.name}", true)
        )

        return PrayerUiState(
            selectedDistrict = district,
            calculationMethod = method,
            madhhab = madhhab,
            hijriOffsetDays = hijriOffset,
            schedule = todaySchedule,
            azanAlerts = azanAlertsMap
        )
    }

    private fun startLivePrayerClock() {
        clockJob?.cancel()
        clockJob = scope.launch {
            while (isActive) {
                refreshPrayerSchedule()
                val currentWaqt = _prayerState.value.schedule.currentWaqt
                if (lastObservedWaqt != null && lastObservedWaqt != currentWaqt) {
                    val s = _prayerState.value
                    if (s.azanAlerts[currentWaqt] == true) {
                        RuhaniPushNotificationManager.sendNotification(
                            context = appContext,
                            category = NotificationCategory.WAQT_AZAN,
                            title = "নামাজের ওয়াক্ত উপস্থিত: ${currentWaqt.nameBn}",
                            message = "${s.selectedDistrict.nameBn} জেলায় ${currentWaqt.nameBn} সালাতের ওয়াক্ত শুরু হয়েছে। ওযু করে জামাতে নামাজ আদায়ের প্রস্তুতি নিন।"
                        )
                        onNotificationTriggered?.invoke()
                    }
                }
                lastObservedWaqt = currentWaqt
                delay(1000L)
            }
        }
    }

    fun refreshPrayerSchedule() {
        val s = _prayerState.value
        val updated = AstronomicalEngine.calculateSchedule(
            district = s.selectedDistrict,
            method = s.calculationMethod,
            madhhab = s.madhhab,
            hijriOffsetDays = s.hijriOffsetDays,
            dayOffset = s.prayerDayOffset
        )
        _prayerState.update { it.copy(schedule = updated) }
    }

    fun updateDistrict(district: BdDistrict) {
        prefs.edit().putString("district", district.id).apply()
        _prayerState.update { it.copy(selectedDistrict = district) }
        refreshPrayerSchedule()
        PrayerAlarmScheduler.schedulePrayerAlarms(appContext)
        onProfileChanged?.invoke()
    }

    fun updateCalculationMethod(method: CalculationMethod) {
        prefs.edit().putString("calc_method", method.id).apply()
        _prayerState.update { it.copy(calculationMethod = method) }
        refreshPrayerSchedule()
        PrayerAlarmScheduler.schedulePrayerAlarms(appContext)
        onProfileChanged?.invoke()
    }

    fun updateMadhhab(madhhab: Madhhab) {
        prefs.edit().putString("madhhab", madhhab.id).apply()
        _prayerState.update { it.copy(madhhab = madhhab) }
        refreshPrayerSchedule()
        PrayerAlarmScheduler.schedulePrayerAlarms(appContext)
        onProfileChanged?.invoke()
    }

    fun updateHijriOffset(offsetDays: Int) {
        val clamped = offsetDays.coerceIn(-2, 2)
        prefs.edit().putInt("hijri_offset", clamped).apply()
        _prayerState.update { it.copy(hijriOffsetDays = clamped) }
        refreshPrayerSchedule()
        PrayerAlarmScheduler.schedulePrayerAlarms(appContext)
    }

    fun shiftPrayerScheduleDay(delta: Int) {
        val nextOffset = if (delta == 0) 0 else (_prayerState.value.prayerDayOffset + delta).coerceIn(-30, 30)
        _prayerState.update { it.copy(prayerDayOffset = nextOffset) }
        refreshPrayerSchedule()
    }

    fun toggleAzanAlert(waqt: WaqtType) {
        val current = _prayerState.value.azanAlerts[waqt] ?: true
        val updated = !current
        prefs.edit().putBoolean("azan_alert_${waqt.name}", updated).apply()
        _prayerState.update { state ->
            state.copy(azanAlerts = state.azanAlerts + (waqt to updated))
        }
        PrayerAlarmScheduler.schedulePrayerAlarms(appContext)
    }

    fun applyProfileSettings(districtId: String, methodId: String, madhhabId: String) {
        val dist = RuhaniContentCatalog.findDistrict(districtId)
        val method = CalculationMethod.fromId(methodId)
        val madhhab = Madhhab.fromId(madhhabId)
        _prayerState.update {
            it.copy(
                selectedDistrict = dist,
                calculationMethod = method,
                madhhab = madhhab
            )
        }
        refreshPrayerSchedule()
    }

    fun cleanup() {
        clockJob?.cancel()
        clockJob = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}
