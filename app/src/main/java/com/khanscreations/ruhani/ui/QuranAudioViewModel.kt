package com.khanscreations.ruhani.ui

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanscreations.ruhani.data.model.QuranBookmark
import com.khanscreations.ruhani.data.remote.QuranApiService
import com.khanscreations.ruhani.data.repository.RuhaniFirestoreRepository
import com.khanscreations.ruhani.domain.MushafMode
import com.khanscreations.ruhani.domain.OfflineContentAndSyncManager
import com.khanscreations.ruhani.domain.QariInfo
import com.khanscreations.ruhani.domain.QuranAyah
import com.khanscreations.ruhani.domain.QuranCatalog
import com.khanscreations.ruhani.domain.QuranSurah
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.core.content.edit

data class QuranAudioUiState(
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
    val bookmarks: List<QuranBookmark> = emptyList()
)

class QuranAudioViewModel(
    private val appContext: Context,
    private val prefs: SharedPreferences = appContext.getSharedPreferences("ruhani_local_prefs", Context.MODE_PRIVATE),
    externalScope: CoroutineScope? = null,
    private val firestoreRepository: RuhaniFirestoreRepository? = null,
    private val userIdProvider: () -> String? = { null },
    var onSyncStatusMessage: ((String) -> Unit)? = null,
    var onErrorMessage: ((String) -> Unit)? = null,
    var onStorageSizeChanged: ((Int) -> Unit)? = null
) : ViewModel() {

    private val scope: CoroutineScope = externalScope ?: viewModelScope
    private val _quranAudioState = MutableStateFlow(loadInitialState())
    val quranAudioState: StateFlow<QuranAudioUiState> = _quranAudioState.asStateFlow()

    private var audioPlayJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
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
                    tts?.language = java.util.Locale.forLanguageTag("ar")
                }
            }
        } catch (e: Exception) {
            Log.w("QuranAudioVM", "TTS initialization skipped: ${e.message}")
        }
    }

    private fun loadInitialState(): QuranAudioUiState {
        val downloadedSurahs = prefs.getStringSet("downloaded_surah_ids", emptySet())
            ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        val downloadedAyahs = prefs.getStringSet("downloaded_ayah_keys", emptySet())
            ?.toSet() ?: emptySet()

        return QuranAudioUiState(
            downloadedSurahIds = downloadedSurahs,
            downloadedAyahKeys = downloadedAyahs
        )
    }

    fun openSurah(surah: QuranSurah) {
        val defaultAyah = surah.ayahs.firstOrNull()?.ayahNumber ?: 1
        val reciterKey = QuranApiService.normalizeReciterKey(_quranAudioState.value.selectedQari.id)
        val initialAudio = QuranApiService.buildStandardAudioMap(surah.number, defaultAyah)[reciterKey]
        _quranAudioState.update {
            it.copy(
                selectedSurah = surah,
                lastReadSurahNumber = surah.number,
                lastReadAyahNumber = defaultAyah,
                playingSurahNumber = surah.number,
                playingAyahNumber = defaultAyah,
                currentAyahAudioUrl = initialAudio?.url ?: it.currentAyahAudioUrl,
                currentAyahOriginalUrl = initialAudio?.originalUrl ?: it.currentAyahOriginalUrl,
                currentAyahApiEndpoint = QuranApiService.buildAyahEndpointUrl(surah.number, defaultAyah),
                isSurahSyncingFromApi = surah.ayahs.size < surah.totalAyahs
            )
        }

        scope.launch {
            val result = QuranApiService.fetchFullSurahAyahs(surah)
            result.onSuccess { fullAyahs ->
                _quranAudioState.update { current ->
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
                _quranAudioState.update { it.copy(isSurahSyncingFromApi = false) }
            }
        }
    }

    fun setMushafMode(mode: MushafMode) {
        _quranAudioState.update {
            it.copy(
                mushafMode = mode,
                showWordByWord = mode == MushafMode.WORD_BY_WORD
            )
        }
    }

    fun toggleWordByWord() {
        _quranAudioState.update { it.copy(showWordByWord = !it.showWordByWord) }
    }

    fun toggleHifzHideArabic() {
        _quranAudioState.update { it.copy(hifzHideArabic = !it.hifzHideArabic) }
    }

    fun toggleHifzHideTranslation() {
        _quranAudioState.update { it.copy(hifzHideTranslation = !it.hifzHideTranslation) }
    }

    fun adjustQuranFontSize(delta: Int) {
        _quranAudioState.update {
            it.copy(quranArabicFontSizeSp = (it.quranArabicFontSizeSp + delta).coerceIn(20, 42))
        }
    }

    fun selectQari(qari: QariInfo) {
        val current = _quranAudioState.value
        val surahNo = current.playingSurahNumber ?: current.selectedSurah.number
        val ayahNo = current.playingAyahNumber ?: 1
        val reciterKey = QuranApiService.normalizeReciterKey(qari.id)
        val audioInfo = QuranApiService.buildStandardAudioMap(surahNo, ayahNo)[reciterKey]
        _quranAudioState.update {
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
        _quranAudioState.update {
            it.copy(expandedTafsirAyahKey = if (it.expandedTafsirAyahKey == key) null else key)
        }
    }

    fun playOrPauseAyah(surah: QuranSurah, ayah: QuranAyah) {
        val s = _quranAudioState.value
        val isSameAyah = s.playingSurahNumber == surah.number && s.playingAyahNumber == ayah.ayahNumber
        if (isSameAyah && s.isAudioPlaying) {
            pauseAudio()
            return
        }
        startAyahPlayback(surah, ayah, resetRepeatCounter = true)
    }

    fun toggleGlobalAudioPlayPause() {
        val s = _quranAudioState.value
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
        val s = _quranAudioState.value
        val surah = if (s.selectedSurah.number == s.playingSurahNumber) s.selectedSurah
        else QuranCatalog.surahs.firstOrNull { it.number == s.playingSurahNumber } ?: s.selectedSurah
        if (surah.ayahs.isEmpty()) return
        val idx = surah.ayahs.indexOfFirst { it.ayahNumber == s.playingAyahNumber }.coerceAtLeast(0)
        val nextIdx = (idx + delta).coerceIn(0, surah.ayahs.lastIndex)
        startAyahPlayback(surah, surah.ayahs[nextIdx], resetRepeatCounter = true)
    }

    fun toggleAudioRepeat3x() {
        _quranAudioState.update {
            it.copy(
                audioRepeat3x = !it.audioRepeat3x,
                ayahRepeatIteration = 1
            )
        }
    }

    fun toggleAudioAutoAdvance() {
        _quranAudioState.update { it.copy(audioAutoAdvance = !it.audioAutoAdvance) }
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

        val reciterKey = QuranApiService.normalizeReciterKey(_quranAudioState.value.selectedQari.id)
        val fallbackAudio = QuranApiService.buildStandardAudioMap(surah.number, ayah.ayahNumber)[reciterKey]
        val endpointUrl = QuranApiService.buildAyahEndpointUrl(surah.number, ayah.ayahNumber)

        _quranAudioState.update {
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

        audioPlayJob = scope.launch {
            val offlineFile = OfflineContentAndSyncManager.getOfflineAudioFile(
                appContext,
                reciterKey,
                surah.number,
                ayah.ayahNumber
            )
            val hasOfflineMp3 = offlineFile.exists() && offlineFile.length() > 1500L

            val apiResult = QuranApiService.fetchAyah(surah.number, ayah.ayahNumber)
            val resolvedAudio = apiResult.getOrNull()?.audioByReciter?.get(reciterKey) ?: fallbackAudio
            val primaryMp3Url = if (hasOfflineMp3) offlineFile.absolutePath else (resolvedAudio?.url ?: "")
            val backupMp3Url = resolvedAudio?.originalUrl ?: (resolvedAudio?.url ?: "")

            _quranAudioState.update { state ->
                state.copy(
                    currentAyahAudioUrl = resolvedAudio?.url ?: fallbackAudio?.url ?: "",
                    currentAyahOriginalUrl = backupMp3Url,
                    currentAyahApiEndpoint = endpointUrl,
                    isPlayingFromOfflineCache = hasOfflineMp3 || (surah.number in state.downloadedSurahIds)
                )
            }

            val streamedSuccess = streamMp3WithMediaPlayer(
                primaryUrl = primaryMp3Url,
                backupUrl = backupMp3Url,
                surah = surah,
                ayah = ayah
            )

            if (!streamedSuccess && isActive && _quranAudioState.value.isAudioPlaying) {
                _quranAudioState.update { it.copy(isAudioBuffering = false) }
                if (ttsReady) {
                    try {
                        tts?.speak(
                            ayah.arabicText,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "ayah_${surah.number}_${ayah.ayahNumber}"
                        )
                    } catch (e: Exception) {
                        Log.d("QuranAudioVM", "TTS speak fallback: ${e.message}")
                    }
                }
                val totalMs = 6000
                val steps = 24
                val stepMs = totalMs / steps
                for (i in 1..steps) {
                    if (!isActive || !_quranAudioState.value.isAudioPlaying) return@launch
                    delay(stepMs.toLong())
                    _quranAudioState.update {
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

                player.setOnPreparedListener { prepared = true }
                player.setOnErrorListener { _, _, _ ->
                    failed = true
                    true
                }
                player.setOnCompletionListener { completed = true }

                player.setDataSource(candidateUrl)
                player.prepareAsync()

                var waitedMs = 0
                while (!prepared && !failed && waitedMs < 6000 && _quranAudioState.value.isAudioPlaying) {
                    delay(100L)
                    waitedMs += 100
                }

                if (!prepared || failed || !_quranAudioState.value.isAudioPlaying) {
                    releaseMediaPlayer()
                    continue
                }

                player.start()
                val duration = player.duration.coerceAtLeast(1000)
                _quranAudioState.update {
                    it.copy(
                        isAudioBuffering = false,
                        audioDurationMs = duration
                    )
                }

                while (!completed && !failed && _quranAudioState.value.isAudioPlaying) {
                    val pos = try {
                        player.currentPosition.coerceAtLeast(0)
                    } catch (_: Exception) { 0 }
                    val total = try {
                        player.duration.takeIf { it > 0 } ?: duration
                    } catch (_: Exception) { duration }
                    val fraction = (pos.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    _quranAudioState.update {
                        it.copy(
                            audioProgressFraction = fraction,
                            audioCurrentPositionMs = pos,
                            audioDurationMs = total
                        )
                    }
                    delay(150L)
                }

                releaseMediaPlayer()
                if (completed && _quranAudioState.value.isAudioPlaying) {
                    _quranAudioState.update {
                        it.copy(
                            audioProgressFraction = 1f,
                            audioCurrentPositionMs = it.audioDurationMs
                        )
                    }
                    onAyahPlaybackFinished(surah, ayah)
                    return true
                }
                if (!_quranAudioState.value.isAudioPlaying) {
                    return true
                }
            } catch (e: Exception) {
                Log.d("QuranAudioVM", "MediaPlayer candidate failed ($candidateUrl): ${e.message}")
                releaseMediaPlayer()
            }
        }
        return false
    }

    private fun onAyahPlaybackFinished(surah: QuranSurah, ayah: QuranAyah) {
        val current = _quranAudioState.value
        val activeSurah = if (current.selectedSurah.number == surah.number) current.selectedSurah else surah
        val currentIdx = activeSurah.ayahs.indexOfFirst { it.ayahNumber == ayah.ayahNumber }

        if (current.audioRepeat3x && current.ayahRepeatIteration < 3) {
            _quranAudioState.update { it.copy(ayahRepeatIteration = it.ayahRepeatIteration + 1) }
            startAyahPlayback(activeSurah, ayah, resetRepeatCounter = false)
        } else if (current.audioAutoAdvance && currentIdx in 0 until activeSurah.ayahs.lastIndex) {
            _quranAudioState.update { it.copy(ayahRepeatIteration = 1) }
            startAyahPlayback(activeSurah, activeSurah.ayahs[currentIdx + 1], resetRepeatCounter = true)
        } else {
            _quranAudioState.update {
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
        _quranAudioState.update {
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
        _quranAudioState.update {
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

    fun toggleBookmark(surah: QuranSurah, ayah: QuranAyah) {
        val docId = "s${surah.number}_a${ayah.ayahNumber}"
        val existing = _quranAudioState.value.bookmarks.firstOrNull {
            it.id == docId || (it.surahNumber == surah.number.toLong() && it.ayahNumber == ayah.ayahNumber.toLong())
        }
        val uid = userIdProvider()
        if (existing != null) {
            _quranAudioState.update { state ->
                state.copy(bookmarks = state.bookmarks.filterNot { it.id == existing.id })
            }
            if (uid != null && firestoreRepository != null) {
                scope.launch {
                    firestoreRepository.removeBookmark(docId)
                }
            }
        } else {
            val bm = QuranBookmark(
                id = docId,
                userId = uid ?: "local",
                surahNumber = surah.number.toLong(),
                ayahNumber = ayah.ayahNumber.toLong(),
                surahNameBn = surah.nameBn,
                surahNameEn = surah.nameEn,
                arabicPreview = ayah.arabicText,
                translationPreview = ayah.translationBn,
                note = "${surah.nameBn} • আয়াত ${ayah.ayahNumber}"
            )
            _quranAudioState.update { state ->
                state.copy(bookmarks = state.bookmarks + bm)
            }
            if (uid != null && firestoreRepository != null) {
                scope.launch {
                    firestoreRepository.addBookmark(bm)
                }
            }
        }
    }

    fun downloadAyahAudio(surahNumber: Int, ayahNumber: Int) {
        val key = "${surahNumber}_${ayahNumber}"
        val current = _quranAudioState.value
        if (key in current.downloadedAyahKeys || current.downloadingAyahKey == key) return

        _quranAudioState.update { it.copy(downloadingAyahKey = key) }
        val reciterKey = QuranApiService.normalizeReciterKey(current.selectedQari.id)
        scope.launch {
            val success = OfflineContentAndSyncManager.downloadAyahAudioForOffline(
                context = appContext,
                reciterKey = reciterKey,
                surahNumber = surahNumber,
                ayahNumber = ayahNumber
            )
            if (success) {
                val updatedKeys = _quranAudioState.value.downloadedAyahKeys + key
                prefs.edit { putStringSet("downloaded_ayah_keys", updatedKeys) }
                val newSize = OfflineContentAndSyncManager.calculateOfflineStorageSizeKb(appContext)
                _quranAudioState.update { state ->
                    state.copy(
                        downloadedAyahKeys = updatedKeys,
                        downloadingAyahKey = null
                    )
                }
                onStorageSizeChanged?.invoke(newSize)
                onSyncStatusMessage?.invoke("আয়াত $surahNumber:$ayahNumber এর অডিও ডাউনলোড সম্পন্ন হয়েছে")
            } else {
                _quranAudioState.update { it.copy(downloadingAyahKey = null) }
                onErrorMessage?.invoke("আয়াতের অডিও ডাউনলোড ব্যর্থ হয়েছে")
            }
        }
    }

    fun toggleOfflineSurahDownload(surahNumber: Int) {
        val current = _quranAudioState.value
        if (surahNumber in current.downloadedSurahIds) {
            OfflineContentAndSyncManager.removeOfflineSurah(appContext, surahNumber)
            val newSize = OfflineContentAndSyncManager.calculateOfflineStorageSizeKb(appContext)
            val updated = current.downloadedSurahIds - surahNumber
            prefs.edit { putStringSet("downloaded_surah_ids", updated.map { it.toString() }.toSet()) }
            _quranAudioState.update { state ->
                state.copy(downloadedSurahIds = updated)
            }
            onStorageSizeChanged?.invoke(newSize)
            onSyncStatusMessage?.invoke("অফলাইন মেমরি থেকে সূরা #$surahNumber সরানো হয়েছে")
        } else {
            val surah = QuranCatalog.surahs.firstOrNull { it.number == surahNumber } ?: return
            val reciterKey = QuranApiService.normalizeReciterKey(current.selectedQari.id)
            _quranAudioState.update { it.copy(downloadingSurahId = surahNumber) }
            scope.launch {
                OfflineContentAndSyncManager.downloadSurahForOffline(appContext, surah, reciterKey)
                val newSize = OfflineContentAndSyncManager.calculateOfflineStorageSizeKb(appContext)
                val updated = _quranAudioState.value.downloadedSurahIds + surahNumber
                prefs.edit { putStringSet("downloaded_surah_ids", updated.map { it.toString() }.toSet()) }
                _quranAudioState.update { state ->
                    state.copy(
                        downloadedSurahIds = updated,
                        downloadingSurahId = null
                    )
                }
                onStorageSizeChanged?.invoke(newSize)
                onSyncStatusMessage?.invoke("${surah.nameBn} (অডিও ও তাফসীর) অফলাইনে ডাউনলোড সম্পন্ন হয়েছে")
            }
        }
    }

    fun applyCloudBookmarks(bookmarks: List<QuranBookmark>) {
        _quranAudioState.update { it.copy(bookmarks = bookmarks) }
    }

    fun cleanup() {
        audioPlayJob?.cancel()
        audioPlayJob = null
        releaseMediaPlayer()
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
