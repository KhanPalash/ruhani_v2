package com.example.data.remote

import com.example.domain.QuranAyah
import com.example.domain.QuranSurah
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class QuranApiReciterAudio(
    val reciterKey: String,
    val reciterName: String,
    val url: String,
    val originalUrl: String
)

data class QuranApiAyahResponse(
    val surahNo: Int,
    val ayahNo: Int,
    val surahName: String,
    val surahNameArabic: String,
    val surahNameTranslation: String,
    val revelationPlace: String,
    val totalAyah: Int,
    val arabic1: String,
    val arabic2: String,
    val bengali: String,
    val english: String,
    val endpointUrl: String,
    val audioByReciter: Map<String, QuranApiReciterAudio>
)

/**
 * Client for The-Quran-Project/Quran-API (https://github.com/The-Quran-Project/Quran-API.git)
 * Base endpoint: https://quranapi.pages.dev/api
 * Ayah endpoint: GET https://quranapi.pages.dev/api/{surahNo}/{ayahNo}.json
 * Surah endpoint: GET https://quranapi.pages.dev/api/{surahNo}.json
 */
object QuranApiService {

    const val BASE_API_URL = "https://quranapi.pages.dev/api"
    private const val MIRROR_API_URL = "https://the-quran-project.github.io/Quran-API/api"
    private const val AUDIO_CDN_BASE = "https://the-quran-project.github.io/Quran-Audio/Data"

    private val ayahCache = ConcurrentHashMap<String, QuranApiAyahResponse>()
    private val surahCache = ConcurrentHashMap<Int, List<QuranAyah>>()

    fun buildAyahEndpointUrl(surahNo: Int, ayahNo: Int): String =
        "$BASE_API_URL/$surahNo/$ayahNo.json"

    fun buildSurahEndpointUrl(surahNo: Int): String =
        "$BASE_API_URL/$surahNo.json"

    /**
     * Resolves the standardized Reciter key ("1".."5") used by The-Quran-Project/Quran-API.
     */
    fun normalizeReciterKey(qariId: String): String = when (qariId.lowercase(Locale.US)) {
        "1", "alafasy", "mishary" -> "1"
        "2", "shatri", "abubakr" -> "2"
        "3", "qatami", "nasser" -> "3"
        "4", "dosari", "yasser", "sudais" -> "4"
        "5", "rifai", "hani", "husary", "abdulbasit" -> "5"
        else -> "1"
    }

    /**
     * Builds the exact audio map matching The-Quran-Project/Quran-API schema for any (surahNo, ayahNo).
     */
    fun buildStandardAudioMap(surahNo: Int, ayahNo: Int): Map<String, QuranApiReciterAudio> {
        val code = String.format(Locale.US, "%03d%03d", surahNo, ayahNo)
        return mapOf(
            "1" to QuranApiReciterAudio(
                reciterKey = "1",
                reciterName = "Mishary Rashid Al Afasy",
                url = "$AUDIO_CDN_BASE/1/${surahNo}_${ayahNo}.mp3",
                originalUrl = "https://everyayah.com/data/Alafasy_128kbps/$code.mp3"
            ),
            "2" to QuranApiReciterAudio(
                reciterKey = "2",
                reciterName = "Abu Bakr Al Shatri",
                url = "$AUDIO_CDN_BASE/2/${surahNo}_${ayahNo}.mp3",
                originalUrl = "https://everyayah.com/data/Abu_Bakr_Ash-Shaatree_128kbps/$code.mp3"
            ),
            "3" to QuranApiReciterAudio(
                reciterKey = "3",
                reciterName = "Nasser Al Qatami",
                url = "$AUDIO_CDN_BASE/3/${surahNo}_${ayahNo}.mp3",
                originalUrl = "https://everyayah.com/data/Nasser_Alqatami_128kbps/$code.mp3"
            ),
            "4" to QuranApiReciterAudio(
                reciterKey = "4",
                reciterName = "Yasser Al Dosari",
                url = "$AUDIO_CDN_BASE/4/${surahNo}_${ayahNo}.mp3",
                originalUrl = "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/$code.mp3"
            ),
            "5" to QuranApiReciterAudio(
                reciterKey = "5",
                reciterName = "Hani Ar Rifai",
                url = "$AUDIO_CDN_BASE/5/${surahNo}_${ayahNo}.mp3",
                originalUrl = "https://everyayah.com/data/Hani_Rifai_192kbps/$code.mp3"
            )
        )
    }

    /**
     * Fetches Ayah-by-Ayah JSON from GET https://quranapi.pages.dev/api/{surahNo}/{ayahNo}.json
     * including multi-reciter audio links ("1".."5"), Arabic text, Bengali & English translations.
     */
    suspend fun fetchAyah(
        surahNo: Int,
        ayahNo: Int,
        forceRefresh: Boolean = false
    ): Result<QuranApiAyahResponse> = withContext(Dispatchers.IO) {
        val cacheKey = "${surahNo}_$ayahNo"
        if (!forceRefresh) {
            ayahCache[cacheKey]?.let { return@withContext Result.success(it) }
        }

        val primaryUrl = buildAyahEndpointUrl(surahNo, ayahNo)
        val mirrorUrl = "$MIRROR_API_URL/$surahNo/$ayahNo.json"

        runCatching {
            val rawJson = try {
                httpGetJson(primaryUrl)
            } catch (_: Exception) {
                httpGetJson(mirrorUrl)
            }
            val parsed = parseAyahJson(rawJson, surahNo, ayahNo, primaryUrl)
            ayahCache[cacheKey] = parsed
            parsed
        }
    }

    /**
     * Fetches the full Surah chapter JSON from GET https://quranapi.pages.dev/api/{surahNo}.json
     * and merges it with any local word-by-word / tafsir entries from [baseSurah].
     */
    suspend fun fetchFullSurahAyahs(
        baseSurah: QuranSurah
    ): Result<List<QuranAyah>> = withContext(Dispatchers.IO) {
        val surahNo = baseSurah.number
        surahCache[surahNo]?.let { return@withContext Result.success(it) }

        val url = buildSurahEndpointUrl(surahNo)
        runCatching {
            val rawJson = httpGetJson(url)
            val root = JSONObject(rawJson)
            val arabic1Array = root.optJSONArray("arabic1")
            val bengaliArray = root.optJSONArray("bengali")
            val englishArray = root.optJSONArray("english")
            val totalAyah = root.optInt("totalAyah", arabic1Array?.length() ?: baseSurah.totalAyahs)

            if (arabic1Array == null || arabic1Array.length() == 0) {
                return@runCatching baseSurah.ayahs
            }

            val existingByNum = baseSurah.ayahs.associateBy { it.ayahNumber }
            val merged = ArrayList<QuranAyah>(arabic1Array.length())

            for (i in 0 until arabic1Array.length()) {
                val ayahNo = i + 1
                val localAyah = existingByNum[ayahNo]
                val apiArabic = arabic1Array.optString(i).takeIf { it.isNotBlank() }
                val apiBengali = bengaliArray?.optString(i)?.takeIf { it.isNotBlank() }
                val apiEnglish = englishArray?.optString(i)?.takeIf { it.isNotBlank() }

                if (localAyah != null) {
                    merged.add(
                        localAyah.copy(
                            arabicText = localAyah.arabicText.ifBlank { apiArabic ?: "" },
                            translationBn = localAyah.translationBn.ifBlank { apiBengali ?: "" },
                            translationEn = localAyah.translationEn.ifBlank { apiEnglish ?: "" }
                        )
                    )
                } else {
                    merged.add(
                        QuranAyah(
                            surahNumber = surahNo,
                            ayahNumber = ayahNo,
                            juzNumber = baseSurah.juzStart,
                            pageNumber = baseSurah.ayahs.firstOrNull()?.pageNumber ?: 1,
                            arabicText = apiArabic ?: "",
                            transliteration = "Ayah $surahNo:$ayahNo",
                            translationBn = apiBengali ?: "",
                            translationEn = apiEnglish ?: "",
                            tafsirBn = "সূরা ${baseSurah.nameBn}-এর আয়াত $ayahNo (The-Quran-Project API থেকে সংগৃহীত)।"
                        )
                    )
                }
            }

            if (merged.isNotEmpty()) {
                surahCache[surahNo] = merged
                merged
            } else {
                baseSurah.ayahs
            }
        }
    }

    private fun parseAyahJson(
        body: String,
        surahNo: Int,
        ayahNo: Int,
        endpointUrl: String
    ): QuranApiAyahResponse {
        val root = JSONObject(body)
        val defaultAudioMap = buildStandardAudioMap(surahNo, ayahNo).toMutableMap()
        val audioObj = root.optJSONObject("audio")
        if (audioObj != null) {
            val keys = audioObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = audioObj.optJSONObject(key) ?: continue
                val reciter = item.optString("reciter", defaultAudioMap[key]?.reciterName ?: "Reciter $key")
                val url = item.optString("url", defaultAudioMap[key]?.url ?: "")
                val originalUrl = item.optString("originalUrl", defaultAudioMap[key]?.originalUrl ?: url)
                if (url.isNotBlank() || originalUrl.isNotBlank()) {
                    defaultAudioMap[key] = QuranApiReciterAudio(
                        reciterKey = key,
                        reciterName = reciter,
                        url = url.ifBlank { originalUrl },
                        originalUrl = originalUrl.ifBlank { url }
                    )
                }
            }
        }

        return QuranApiAyahResponse(
            surahNo = root.optInt("surahNo", surahNo),
            ayahNo = root.optInt("ayahNo", ayahNo),
            surahName = root.optString("surahName", "Surah $surahNo"),
            surahNameArabic = root.optString("surahNameArabicLong", root.optString("surahNameArabic", "")),
            surahNameTranslation = root.optString("surahNameTranslation", ""),
            revelationPlace = root.optString("revelationPlace", ""),
            totalAyah = root.optInt("totalAyah", 0),
            arabic1 = root.optString("arabic1", ""),
            arabic2 = root.optString("arabic2", ""),
            bengali = root.optString("bengali", ""),
            english = root.optString("english", ""),
            endpointUrl = endpointUrl,
            audioByReciter = defaultAudioMap
        )
    }

    private fun httpGetJson(urlString: String): String {
        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Ruhani-Android/1.0")
        }
        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code from $urlString")
            }
            responseText
        } finally {
            connection.disconnect()
        }
    }
}
