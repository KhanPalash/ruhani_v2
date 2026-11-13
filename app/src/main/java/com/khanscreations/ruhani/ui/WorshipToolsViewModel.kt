package com.khanscreations.ruhani.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanscreations.ruhani.data.model.SalatLog
import com.khanscreations.ruhani.data.repository.RuhaniFirestoreRepository
import com.khanscreations.ruhani.domain.AstronomicalEngine
import com.khanscreations.ruhani.domain.CalculationMethod
import com.khanscreations.ruhani.domain.Madhhab
import com.khanscreations.ruhani.domain.OfflineContentAndSyncManager
import com.khanscreations.ruhani.domain.PendingSyncRecord
import com.khanscreations.ruhani.domain.RuhaniContentCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.core.content.edit

data class WorshipToolsUiState(
    val currentTab: MainTab = MainTab.HOME,
    val activeSubScreen: SubScreen = SubScreen.NONE,
    val salatLogsByDate: Map<String, SalatLog> = emptyMap(),
    val qazaFajr: Long = 0L,
    val qazaDhuhr: Long = 0L,
    val qazaAsr: Long = 0L,
    val qazaMaghrib: Long = 0L,
    val qazaIsha: Long = 0L,
    val qazaWitr: Long = 0L,
    val zakatUseSilverNisab: Boolean = true,
    val zakatGoldRatePerVori: Double = 138000.0,
    val zakatSilverRatePerVori: Double = 2100.0,
    val zakatCashBdt: String = "",
    val zakatGoldValueBdt: String = "",
    val zakatBusinessBdt: String = "",
    val zakatDebtsBdt: String = "",
    val haramainChannel: String = "MAKKAH",
    val isHaramainPlaying: Boolean = true
)

class WorshipToolsViewModel(
    private val appContext: Context,
    private val prefs: SharedPreferences = appContext.getSharedPreferences("ruhani_local_prefs", Context.MODE_PRIVATE),
    externalScope: CoroutineScope? = null,
    private val firestoreRepository: RuhaniFirestoreRepository? = null,
    private val userIdProvider: () -> String? = { null },
    private val isOnlineProvider: () -> Boolean = { true },
    var onQazaChanged: (() -> Unit)? = null,
    var onSalatQueueUpdated: ((List<PendingSyncRecord>) -> Unit)? = null,
    var onSalatSynced: ((String) -> Unit)? = null
) : ViewModel() {

    private val scope: CoroutineScope = externalScope ?: viewModelScope
    private val _worshipToolsState = MutableStateFlow(loadInitialState())
    val worshipToolsState: StateFlow<WorshipToolsUiState> = _worshipToolsState.asStateFlow()

    private fun loadInitialState(): WorshipToolsUiState {
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

        val resolvedUserId = userIdProvider() ?: "local"
        val defaultSalatLog = SalatLog(
            id = todaySchedule.dateKey,
            userId = resolvedUserId,
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

        return WorshipToolsUiState(
            salatLogsByDate = mapOf(todaySchedule.dateKey to defaultSalatLog),
            qazaFajr = prefs.getLong("qaza_fajr", 0L),
            qazaDhuhr = prefs.getLong("qaza_dhuhr", 0L),
            qazaAsr = prefs.getLong("qaza_asr", 0L),
            qazaMaghrib = prefs.getLong("qaza_maghrib", 0L),
            qazaIsha = prefs.getLong("qaza_isha", 0L),
            qazaWitr = prefs.getLong("qaza_witr", 0L)
        )
    }

    fun selectTab(tab: MainTab) {
        _worshipToolsState.update {
            it.copy(
                currentTab = tab,
                activeSubScreen = SubScreen.NONE
            )
        }
    }

    fun openSubScreen(screen: SubScreen) {
        _worshipToolsState.update { it.copy(activeSubScreen = screen) }
    }

    fun navigateBack(): Boolean {
        val current = _worshipToolsState.value
        if (current.activeSubScreen != SubScreen.NONE) {
            _worshipToolsState.update { it.copy(activeSubScreen = SubScreen.NONE) }
            return true
        }
        if (current.currentTab != MainTab.HOME) {
            _worshipToolsState.update { it.copy(currentTab = MainTab.HOME) }
            return true
        }
        return false
    }

    fun toggleDailySalat(dateKey: String, prayerField: String) {
        val currentMap = _worshipToolsState.value.salatLogsByDate
        val uid = userIdProvider() ?: "local"
        val existing = currentMap[dateKey] ?: SalatLog(
            id = dateKey,
            userId = uid,
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

        prefs.edit {
            putBoolean("salat_${dateKey}_fajr", updated.fajr)
            putBoolean("salat_${dateKey}_dhuhr", updated.dhuhr)
            putBoolean("salat_${dateKey}_asr", updated.asr)
            putBoolean("salat_${dateKey}_maghrib", updated.maghrib)
            putBoolean("salat_${dateKey}_isha", updated.isha)
            putBoolean("salat_${dateKey}_witr", updated.witr)
            putBoolean("salat_${dateKey}_duha", updated.duha)
            putBoolean("salat_${dateKey}_awwabin", updated.awwabin)
            putBoolean("salat_${dateKey}_tahajjud", updated.tahajjud)
            putBoolean("salat_${dateKey}_fasting", updated.fasting)
        }

        val queue = OfflineContentAndSyncManager.enqueuePendingSyncAction(
            context = appContext,
            type = "SALAT_LOG",
            descriptionBn = "সালাত ট্র্যাকার আপডেট ($dateKey • ${prayerField.uppercase()})",
            payloadJson = "{\"dateKey\":\"$dateKey\",\"field\":\"$prayerField\"}"
        )

        _worshipToolsState.update { state ->
            state.copy(salatLogsByDate = state.salatLogsByDate + (dateKey to updated))
        }
        onSalatQueueUpdated?.invoke(queue)

        if (firestoreRepository != null && userIdProvider() != null && isOnlineProvider()) {
            scope.launch {
                firestoreRepository.saveSalatLog(updated)
                val syncedTime = OfflineContentAndSyncManager.markQueueSynchronized(appContext)
                onSalatSynced?.invoke(syncedTime)
            }
        }
    }

    fun adjustQazaCount(prayer: String, delta: Long) {
        val s = _worshipToolsState.value
        val nextState = when (prayer) {
            "fajr" -> s.copy(qazaFajr = (s.qazaFajr + delta).coerceAtLeast(0L))
            "dhuhr" -> s.copy(qazaDhuhr = (s.qazaDhuhr + delta).coerceAtLeast(0L))
            "asr" -> s.copy(qazaAsr = (s.qazaAsr + delta).coerceAtLeast(0L))
            "maghrib" -> s.copy(qazaMaghrib = (s.qazaMaghrib + delta).coerceAtLeast(0L))
            "isha" -> s.copy(qazaIsha = (s.qazaIsha + delta).coerceAtLeast(0L))
            "witr" -> s.copy(qazaWitr = (s.qazaWitr + delta).coerceAtLeast(0L))
            else -> s
        }
        prefs.edit {
            putLong("qaza_fajr", nextState.qazaFajr)
            putLong("qaza_dhuhr", nextState.qazaDhuhr)
            putLong("qaza_asr", nextState.qazaAsr)
            putLong("qaza_maghrib", nextState.qazaMaghrib)
            putLong("qaza_isha", nextState.qazaIsha)
            putLong("qaza_witr", nextState.qazaWitr)
        }
        _worshipToolsState.value = nextState
        onQazaChanged?.invoke()
    }

    fun setZakatNisabStandard(useSilver: Boolean) {
        _worshipToolsState.update { it.copy(zakatUseSilverNisab = useSilver) }
    }

    fun updateZakatInputs(cash: String, gold: String, business: String, debts: String) {
        _worshipToolsState.update {
            it.copy(
                zakatCashBdt = cash,
                zakatGoldValueBdt = gold,
                zakatBusinessBdt = business,
                zakatDebtsBdt = debts
            )
        }
    }

    fun selectHaramainChannel(channel: String) {
        _worshipToolsState.update { it.copy(haramainChannel = channel, isHaramainPlaying = true) }
    }

    fun toggleHaramainPlayback() {
        _worshipToolsState.update { it.copy(isHaramainPlaying = !it.isHaramainPlaying) }
    }

    fun applyCloudSalatLogs(logs: List<SalatLog>) {
        if (logs.isNotEmpty()) {
            val map = logs.associateBy { it.dateKey }
            _worshipToolsState.update { state ->
                state.copy(salatLogsByDate = state.salatLogsByDate + map)
            }
        }
    }

    fun applyCloudQazaCounts(
        fajr: Long, dhuhr: Long, asr: Long, maghrib: Long, isha: Long, witr: Long
    ) {
        _worshipToolsState.update {
            it.copy(
                qazaFajr = fajr,
                qazaDhuhr = dhuhr,
                qazaAsr = asr,
                qazaMaghrib = maghrib,
                qazaIsha = isha,
                qazaWitr = witr
            )
        }
    }

    fun cleanup() {
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}
