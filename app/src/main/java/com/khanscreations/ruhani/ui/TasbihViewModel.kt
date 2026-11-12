package com.khanscreations.ruhani.ui

import android.content.Context
import android.content.SharedPreferences
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
import com.khanscreations.ruhani.domain.RuhaniContentCatalog
import com.khanscreations.ruhani.domain.TasbihPreset
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TasbihUiState(
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
    val favoriteDuaIds: Set<String> = emptySet()
)

class TasbihViewModel(
    private val appContext: Context,
    private val prefs: SharedPreferences = appContext.getSharedPreferences("ruhani_local_prefs", Context.MODE_PRIVATE),
    externalScope: CoroutineScope? = null,
    var onTasbihMilestone: ((Long) -> Unit)? = null
) : ViewModel() {

    @Suppress("unused")
    private val scope: CoroutineScope = externalScope ?: viewModelScope
    private val _tasbihState = MutableStateFlow(loadInitialState())
    val tasbihState: StateFlow<TasbihUiState> = _tasbihState.asStateFlow()

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ttsReady = true
                    tts?.language = Locale.forLanguageTag("ar")
                }
            }
        } catch (e: Exception) {
            Log.w("TasbihVM", "TTS initialization skipped: ${e.message}")
        }
    }

    private fun loadInitialState(): TasbihUiState {
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

        return TasbihUiState(
            selectedTasbihPreset = activePreset,
            tasbihCurrentCount = currentCountForActive,
            tasbihTarget = savedTarget,
            tasbihCycle = savedCycle,
            tasbihTodayTotal = tasbihToday,
            tasbihAllTimeTotal = prefs.getLong("tasbih_all_time", 0L),
            tasbihCountsByPreset = presetCountsMap,
            duaCountsById = duaCountsMap,
            favoriteDuaIds = favDuas
        )
    }

    fun selectTasbihPreset(preset: TasbihPreset) {
        val savedCount = _tasbihState.value.tasbihCountsByPreset[preset.id]
            ?: prefs.getInt("tasbih_preset_count_${preset.id}", 0)
        prefs.edit()
            .putString("selected_tasbih_preset_id", preset.id)
            .putInt("tasbih_target", preset.defaultTarget)
            .putInt("tasbih_current", savedCount)
            .apply()
        _tasbihState.update {
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
        _tasbihState.update { state ->
            val clampedCount = state.tasbihCurrentCount.coerceAtMost(safeTarget)
            state.copy(
                tasbihTarget = safeTarget,
                tasbihCurrentCount = clampedCount
            )
        }
    }

    fun incrementTasbih() {
        val s = _tasbihState.value
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

        _tasbihState.update {
            it.copy(
                tasbihCurrentCount = nextCount,
                tasbihCycle = nextCycle,
                tasbihTodayTotal = nextToday,
                tasbihAllTimeTotal = nextAllTime,
                tasbihCountsByPreset = updatedPresetCounts
            )
        }

        if (nextAllTime % 10L == 0L) {
            onTasbihMilestone?.invoke(nextAllTime)
        }
    }

    fun requestTasbihReset() {
        _tasbihState.update { it.copy(showTasbihResetDialog = true) }
    }

    fun dismissTasbihReset() {
        _tasbihState.update { it.copy(showTasbihResetDialog = false) }
    }

    fun confirmTasbihReset() {
        val presetId = _tasbihState.value.selectedTasbihPreset.id
        val updatedPresetCounts = _tasbihState.value.tasbihCountsByPreset + (presetId to 0)
        prefs.edit()
            .putInt("tasbih_current", 0)
            .putInt("tasbih_cycle", 1)
            .putInt("tasbih_preset_count_$presetId", 0)
            .apply()
        _tasbihState.update {
            it.copy(
                tasbihCurrentCount = 0,
                tasbihCycle = 1,
                tasbihCountsByPreset = updatedPresetCounts,
                showTasbihResetDialog = false
            )
        }
    }

    fun startNewTasbihCycle() {
        val presetId = _tasbihState.value.selectedTasbihPreset.id
        val nextCycle = (_tasbihState.value.tasbihCycle % _tasbihState.value.tasbihTotalCycles) + 1
        val updatedPresetCounts = _tasbihState.value.tasbihCountsByPreset + (presetId to 0)
        prefs.edit()
            .putInt("tasbih_current", 0)
            .putInt("tasbih_cycle", nextCycle)
            .putInt("tasbih_preset_count_$presetId", 0)
            .apply()
        _tasbihState.update {
            it.copy(
                tasbihCurrentCount = 0,
                tasbihCycle = nextCycle,
                tasbihCountsByPreset = updatedPresetCounts
            )
        }
    }

    fun toggleTasbihVibration() {
        _tasbihState.update { it.copy(tasbihVibrationEnabled = !it.tasbihVibrationEnabled) }
    }

    fun toggleTasbihSound() {
        _tasbihState.update { it.copy(tasbihSoundEnabled = !it.tasbihSoundEnabled) }
    }

    fun incrementDuaCount(duaId: String, target: Int) {
        val current = _tasbihState.value.duaCountsById[duaId] ?: 0
        val next = if (current >= target) 1 else current + 1
        if (_tasbihState.value.tasbihVibrationEnabled) {
            triggerHapticTap()
        }
        prefs.edit().putInt("dua_count_$duaId", next).apply()
        _tasbihState.update { state ->
            state.copy(duaCountsById = state.duaCountsById + (duaId to next))
        }
    }

    fun resetDuaCount(duaId: String) {
        prefs.edit().putInt("dua_count_$duaId", 0).apply()
        _tasbihState.update { state ->
            state.copy(duaCountsById = state.duaCountsById + (duaId to 0))
        }
    }

    fun toggleFavoriteDua(duaId: String) {
        val currentFavs = _tasbihState.value.favoriteDuaIds
        val nextFavs = if (duaId in currentFavs) currentFavs - duaId else currentFavs + duaId
        prefs.edit().putStringSet("favorite_duas", nextFavs).apply()
        _tasbihState.update { it.copy(favoriteDuaIds = nextFavs) }
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

    fun applyCloudTasbihTotal(total: Long) {
        _tasbihState.update { state ->
            state.copy(tasbihAllTimeTotal = maxOf(state.tasbihAllTimeTotal, total))
        }
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
            Log.d("TasbihVM", "Vibration skipped: ${e.message}")
        }
    }

    private fun triggerSoundClick() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 45)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
        } catch (e: Exception) {
            Log.d("TasbihVM", "Tone skipped: ${e.message}")
        }
    }

    fun cleanup() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
        ttsReady = false
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}
