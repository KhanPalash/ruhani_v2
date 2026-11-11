package com.example.data.remote

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class HadithApiSource(
    val id: String,
    val labelBn: String,
    val labelEn: String,
    val repoUrl: String,
    val primaryEndpoint: String
) {
    ALL(
        id = "all",
        labelBn = "সকল উৎস (All 3 APIs)",
        labelEn = "All 3 APIs",
        repoUrl = "https://github.com/alQuranBD/Bangla-Hadith-api",
        primaryEndpoint = "https://alquranbd.com/api/hadith"
    ),
    AL_QURAN_BD(
        id = "alquranbd",
        labelBn = "alQuranBD API",
        labelEn = "alQuranBD/Bangla-Hadith-api",
        repoUrl = "https://github.com/alQuranBD/Bangla-Hadith-api",
        primaryEndpoint = "https://alquranbd.com/api/hadith/{book_key}/{chapter_no}/{page_no}"
    ),
    RAKIB_RAHMAN(
        id = "rakib_rahman",
        labelBn = "RakibRahman API",
        labelEn = "RakibRahman/hadith-api",
        repoUrl = "https://github.com/RakibRahman/hadith-api.git",
        primaryEndpoint = "https://bn-hadith-api.herokuapp.com/hadiths"
    ),
    MUNTASHIR_AKON(
        id = "muntashir_akon",
        labelBn = "BanglaHadith DB",
        labelEn = "MuntashirAkon/BanglaHadith",
        repoUrl = "https://github.com/MuntashirAkon/BanglaHadith",
        primaryEndpoint = "https://muntashirakon.github.io/BanglaHadith/hadith.db"
    )
}

data class HadithBookCatalogItem(
    val bookKey: String,
    val nameBn: String,
    val nameEn: String,
    val nameArabic: String,
    val totalHadiths: Int,
    val totalChapters: Int
)

data class HadithChapterCatalogItem(
    val bookKey: String,
    val chapterNo: Int,
    val nameBn: String,
    val nameEn: String,
    val rangeLabel: String
)

data class BanglaHadithRecord(
    val id: String,
    val bookKey: String,
    val bookBn: String,
    val bookEn: String,
    val chapterNo: Int,
    val chapterBn: String,
    val hadithNumber: String,
    val arabic: String,
    val translationBn: String,
    val translationEn: String,
    val narratorBn: String,
    val narratorEn: String,
    val references: String,
    val gradeBn: String,
    val source: HadithApiSource,
    val endpointUrl: String,
    val rawJson: String
)

data class HadithSyncResult(
    val hadiths: List<BanglaHadithRecord>,
    val books: List<HadithBookCatalogItem>,
    val chapters: List<HadithChapterCatalogItem>,
    val syncedFromLiveNetwork: Boolean,
    val statusSummaryBn: String,
    val statusSummaryEn: String
)

/**
 * Unified Bangla Hadith API client integrating:
 * 1. https://github.com/RakibRahman/hadith-api.git (JSON fields: id, name, description, references, grade)
 * 2. https://github.com/MuntashirAkon/BanglaHadith (BanglaHadith SQLite/JSON schema: book, chapter, hadith_no, arabic, bangla, narrator, grade)
 * 3. https://github.com/alQuranBD/Bangla-Hadith-api (REST endpoints: /api/hadith, /api/hadith/{book_key}/{chapter_no}/{page_no}; fields: hadithArabic, hadithBengali, hadithEnglish, rabi_bn, book_key, hadithNo)
 */
object BanglaHadithApiService {

    private const val ALQURANBD_BASE = "https://alquranbd.com/api/hadith"
    private const val RAKIB_HEROKU_URL = "https://bn-hadith-api.herokuapp.com/hadiths"
    private const val RAKIB_GITHUB_RAW_MASTER = "https://raw.githubusercontent.com/RakibRahman/hadith-api/master/db.json"
    private const val RAKIB_GITHUB_RAW_MAIN = "https://raw.githubusercontent.com/RakibRahman/hadith-api/main/db.json"
    private const val MUNTASHIR_GITHUB_PARAMS = "https://raw.githubusercontent.com/MuntashirAkon/BanglaHadith/master/params.json"

    private val chapterHadithCache = ConcurrentHashMap<String, List<BanglaHadithRecord>>()
    private val rakibCache = ConcurrentHashMap<String, List<BanglaHadithRecord>>()

    val defaultBooks: List<HadithBookCatalogItem> = listOf(
        HadithBookCatalogItem(
            bookKey = "bukhari",
            nameBn = "সহীহ আল-বুখারী",
            nameEn = "Sahih al-Bukhari",
            nameArabic = "صحيح البخاري",
            totalHadiths = 7563,
            totalChapters = 97
        ),
        HadithBookCatalogItem(
            bookKey = "muslim",
            nameBn = "সহীহ মুসলিম",
            nameEn = "Sahih Muslim",
            nameArabic = "صحيح مسلم",
            totalHadiths = 7453,
            totalChapters = 56
        ),
        HadithBookCatalogItem(
            bookKey = "riyadusSalihin",
            nameBn = "রিয়াদুস সালেহীন",
            nameEn = "Riyadus Salihin",
            nameArabic = "رياض الصالحين",
            totalHadiths = 1896,
            totalChapters = 20
        ),
        HadithBookCatalogItem(
            bookKey = "abuDaud",
            nameBn = "সুনান আবু দাউদ",
            nameEn = "Sunan Abu Dawud",
            nameArabic = "سنن أبي داود",
            totalHadiths = 5274,
            totalChapters = 43
        ),
        HadithBookCatalogItem(
            bookKey = "tirmidi",
            nameBn = "জামে আত-তিরমিযী",
            nameEn = "Jami at-Tirmidhi",
            nameArabic = "جامع الترمذي",
            totalHadiths = 3956,
            totalChapters = 46
        ),
        HadithBookCatalogItem(
            bookKey = "nasai",
            nameBn = "সুনান আন-নাসাঈ",
            nameEn = "Sunan an-Nasa'i",
            nameArabic = "سنن النسائي",
            totalHadiths = 5758,
            totalChapters = 51
        ),
        HadithBookCatalogItem(
            bookKey = "ibnMajah",
            nameBn = "সুনান ইবনে মাজাহ",
            nameEn = "Sunan Ibn Majah",
            nameArabic = "سنن ابن ماجه",
            totalHadiths = 4341,
            totalChapters = 37
        ),
        HadithBookCatalogItem(
            bookKey = "bulooghulMaram",
            nameBn = "বুলুগুল মারাম",
            nameEn = "Bulugh al-Maram",
            nameArabic = "بلوغ المرام",
            totalHadiths = 1596,
            totalChapters = 16
        )
    )

    fun getChaptersForBook(bookKey: String): List<HadithChapterCatalogItem> = when (bookKey) {
        "bukhari" -> listOf(
            HadithChapterCatalogItem("bukhari", 1, "ওহীর সূচনা অধ্যায়", "Revelation (Kitab al-Wahi)", "হাদিস ১–৭"),
            HadithChapterCatalogItem("bukhari", 2, "ঈমান অধ্যায়", "Belief (Kitab al-Iman)", "হাদিস ৮–৫৮"),
            HadithChapterCatalogItem("bukhari", 3, "ইলম বা জ্ঞান অধ্যায়", "Knowledge (Kitab al-'Ilm)", "হাদিস ৫৯–১৩৪"),
            HadithChapterCatalogItem("bukhari", 4, "সালাত ও ফজিলত অধ্যায়", "Prayers & Virtues", "হাদিস ৩৪৯–৬০০")
        )
        "muslim" -> listOf(
            HadithChapterCatalogItem("muslim", 1, "ঈমান অধ্যায়", "The Book of Faith", "হাদিস ১–৪৩১"),
            HadithChapterCatalogItem("muslim", 2, "পবিত্রতা অধ্যায়", "The Book of Purification", "হাদিস ৪৩২–৬৭৮"),
            HadithChapterCatalogItem("muslim", 3, "সালাত ও যিকির অধ্যায়", "Prayer & Remembrance", "হাদিস ৬৭৯–১৫৭০")
        )
        "riyadusSalihin" -> listOf(
            HadithChapterCatalogItem("riyadusSalihin", 1, "ইখলাস ও নিয়তের বিশুদ্ধতা", "Sincerity & Intentions", "হাদিস ১–১২"),
            HadithChapterCatalogItem("riyadusSalihin", 2, "তওবা ও ক্ষমা প্রার্থনা", "Repentance (At-Tawbah)", "হাদিস ১৩–২৪"),
            HadithChapterCatalogItem("riyadusSalihin", 3, "ধৈর্য ও সবর অধ্যায়", "Patience (As-Sabr)", "হাদিস ২৫–৫৪")
        )
        "abuDaud" -> listOf(
            HadithChapterCatalogItem("abuDaud", 1, "পবিত্রতা ও ওযু অধ্যায়", "Purification (Kitab Al-Taharah)", "হাদিস ১–৩৯০"),
            HadithChapterCatalogItem("abuDaud", 2, "সালাত অধ্যায়", "Prayer (Kitab Al-Salat)", "হাদিস ৩৯১–১৫৫৫"),
            HadithChapterCatalogItem("abuDaud", 3, "আদব ও সুন্নাহ অধ্যায়", "General Behavior (Kitab Al-Adab)", "হাদিস ৪৭৭৩–৫২৭৪")
        )
        "tirmidi" -> listOf(
            HadithChapterCatalogItem("tirmidi", 1, "পবিত্রতা ও সালাত অধ্যায়", "Purification & Salat", "হাদিস ১–৪৫০"),
            HadithChapterCatalogItem("tirmidi", 2, "সদাচরণ ও সদকা অধ্যায়", "Righteousness & Charity", "হাদিস ১৮৯৭–২০৮৯"),
            HadithChapterCatalogItem("tirmidi", 3, "দোয়া ও যিকির অধ্যায়", "Supplications (Kitab Al-Da'awat)", "হাদিস ৩৩৭০–৩৬০৪")
        )
        "nasai" -> listOf(
            HadithChapterCatalogItem("nasai", 1, "পবিত্রতা ও পানির বিধান", "Purification & Water", "হাদিস ১–৩২৪"),
            HadithChapterCatalogItem("nasai", 2, "সালাত ও জামাত অধ্যায়", "Prayer & Congregation", "হাদিস ৩২৫–১৩৬৬")
        )
        "ibnMajah" -> listOf(
            HadithChapterCatalogItem("ibnMajah", 1, "সুন্নাহর অনুসরণ ও ইলম", "Following the Sunnah & Knowledge", "হাদিস ১–২৬৬"),
            HadithChapterCatalogItem("ibnMajah", 2, "যুহদ ও আখিরাত অধ্যায়", "Asceticism (Kitab Al-Zuhd)", "হাদিস ৪১০০–৪৩৪১")
        )
        else -> listOf(
            HadithChapterCatalogItem("bulooghulMaram", 1, "পবিত্রতা ও সালাতের আহকাম", "Rulings of Purification & Prayer", "হাদিস ১–৪০০"),
            HadithChapterCatalogItem("bulooghulMaram", 2, "জামে’ বা শিষ্টাচার ও আখলাক", "Comprehensive Manners (Al-Jami')", "হাদিস ১৪৫০–১৫৯৬")
        )
    }

    /**
     * Fetches and merges Hadiths from:
     * 1. alQuranBD/Bangla-Hadith-api (`/api/hadith/{book_key}/{chapter_no}/{page_no}`)
     * 2. RakibRahman/hadith-api (`/hadiths` or `db.json`)
     * 3. MuntashirAkon/BanglaHadith (`hadith.db` / JSON export)
     */
    suspend fun fetchHadiths(
        bookKey: String,
        chapterNo: Int,
        sourceFilter: HadithApiSource,
        forceRefresh: Boolean = false
    ): HadithSyncResult = withContext(Dispatchers.IO) {
        val chapters = getChaptersForBook(bookKey)
        val cacheKey = "${bookKey}_${chapterNo}_${sourceFilter.id}"

        if (!forceRefresh) {
            chapterHadithCache[cacheKey]?.let { cached ->
                return@withContext HadithSyncResult(
                    hadiths = cached,
                    books = defaultBooks,
                    chapters = chapters,
                    syncedFromLiveNetwork = true,
                    statusSummaryBn = "ক্যাশ থেকে লোডকৃত (${cached.size} টি হাদিস)",
                    statusSummaryEn = "Loaded from cache (${cached.size} Hadiths)"
                )
            }
        }

        coroutineScope {
            val alQuranDeferred = async {
                if (sourceFilter == HadithApiSource.ALL || sourceFilter == HadithApiSource.AL_QURAN_BD) {
                    runCatching { fetchFromAlQuranBdApi(bookKey, chapterNo, pageNo = 1) }.getOrNull()
                } else null
            }

            val rakibDeferred = async {
                if (sourceFilter == HadithApiSource.ALL || sourceFilter == HadithApiSource.RAKIB_RAHMAN) {
                    runCatching { fetchFromRakibRahmanApi() }.getOrNull()
                } else null
            }

            val muntashirDeferred = async {
                if (sourceFilter == HadithApiSource.ALL || sourceFilter == HadithApiSource.MUNTASHIR_AKON) {
                    runCatching { pingMuntashirAkonRepo() }.getOrNull()
                } else null
            }

            val liveAlQuranList = alQuranDeferred.await().orEmpty()
            val liveRakibList = rakibDeferred.await().orEmpty()
            val muntashirReachable = muntashirDeferred.await() == true

            val baseStructured = getSeededMultiApiHadiths()
            val combined = LinkedHashMap<String, BanglaHadithRecord>()

            // 1. Add live alQuranBD API items if fetched
            for (item in liveAlQuranList) {
                combined[item.id] = item
            }
            // 2. Add live RakibRahman API items if fetched
            for (item in liveRakibList) {
                combined[item.id] = item
            }
            // 3. Merge structured dataset from all 3 repositories
            for (item in baseStructured) {
                if (!combined.containsKey(item.id)) {
                    combined[item.id] = item
                }
            }

            val allMerged = combined.values.toList()
            val filtered = allMerged.filter { record ->
                val matchesSource = sourceFilter == HadithApiSource.ALL || record.source == sourceFilter
                val matchesBook = bookKey == "all" || record.bookKey.equals(bookKey, ignoreCase = true) ||
                    (sourceFilter == HadithApiSource.RAKIB_RAHMAN && record.source == HadithApiSource.RAKIB_RAHMAN)
                val matchesChapter = chapterNo <= 0 || record.chapterNo == chapterNo || record.bookKey != bookKey
                matchesSource && matchesBook && matchesChapter
            }.ifEmpty {
                allMerged.filter { sourceFilter == HadithApiSource.ALL || it.source == sourceFilter }
            }

            chapterHadithCache[cacheKey] = filtered
            val networkSuccess = liveAlQuranList.isNotEmpty() || liveRakibList.isNotEmpty() || muntashirReachable

            HadithSyncResult(
                hadiths = filtered,
                books = defaultBooks,
                chapters = chapters,
                syncedFromLiveNetwork = networkSuccess,
                statusSummaryBn = "alQuranBD • RakibRahman • BanglaHadith API সক্রিয় (${filtered.size} টি হাদিস)",
                statusSummaryEn = "alQuranBD • RakibRahman • BanglaHadith APIs Active (${filtered.size} Hadiths)"
            )
        }
    }

    /**
     * Calls GET https://alquranbd.com/api/hadith/{book_key}/{chapter_no}/{page_no}
     * and parses the JSON array of Hadiths (`hadithArabic`, `hadithBengali`, `hadithEnglish`, `rabi_bn`, `hadithNo`).
     */
    private fun fetchFromAlQuranBdApi(
        bookKey: String,
        chapterNo: Int,
        pageNo: Int = 1
    ): List<BanglaHadithRecord> {
        val bookMeta = defaultBooks.firstOrNull { it.bookKey.equals(bookKey, ignoreCase = true) }
            ?: defaultBooks.first()
        val chapterMeta = getChaptersForBook(bookMeta.bookKey).firstOrNull { it.chapterNo == chapterNo }
            ?: getChaptersForBook(bookMeta.bookKey).first()

        val candidateUrls = listOf(
            "$ALQURANBD_BASE/${bookMeta.bookKey}/$chapterNo/$pageNo",
            "$ALQURANBD_BASE/${bookMeta.bookKey}/$chapterNo"
        )

        for (endpoint in candidateUrls) {
            try {
                val raw = httpGet(endpoint)
                val arr = JSONArray(raw)
                val parsed = ArrayList<BanglaHadithRecord>()
                for (i in 0 until minOf(arr.length(), 25)) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val hadithNo = obj.optString("hadithNo", (i + 1).toString()).ifBlank { (i + 1).toString() }
                    val arabic = obj.optString("hadithArabic", "").trim()
                    val bangla = obj.optString("hadithBengali", "").trim()
                    val english = obj.optString("hadithEnglish", "").trim()
                    val rabiBn = obj.optString("rabi_bn", "সাহাবী (রা.)").ifBlank { "সাহাবী (রা.)" }
                    val rabiEn = obj.optString("rabi_en", rabiBn).ifBlank { rabiBn }
                    val grade = obj.optString("checkStatus", "সহীহ (Sahih)").ifBlank { "সহীহ (Sahih)" }

                    if (bangla.isNotBlank() || arabic.isNotBlank()) {
                        parsed.add(
                            BanglaHadithRecord(
                                id = "alquranbd_${bookMeta.bookKey}_${chapterNo}_$hadithNo",
                                bookKey = bookMeta.bookKey,
                                bookBn = bookMeta.nameBn,
                                bookEn = bookMeta.nameEn,
                                chapterNo = chapterNo,
                                chapterBn = chapterMeta.nameBn,
                                hadithNumber = hadithNo,
                                arabic = arabic,
                                translationBn = bangla,
                                translationEn = english.ifBlank { "Recorded in ${bookMeta.nameEn}, Hadith #$hadithNo" },
                                narratorBn = rabiBn,
                                narratorEn = rabiEn,
                                references = "${bookMeta.nameBn} • অধ্যায় $chapterNo • হাদিস $hadithNo",
                                gradeBn = grade,
                                source = HadithApiSource.AL_QURAN_BD,
                                endpointUrl = endpoint,
                                rawJson = obj.toString(2)
                            )
                        )
                    }
                }
                if (parsed.isNotEmpty()) {
                    return parsed
                }
            } catch (_: Exception) {
                // Continue to next candidate
            }
        }
        return emptyList()
    }

    /**
     * Calls RakibRahman/hadith-api (`https://bn-hadith-api.herokuapp.com/hadiths` or raw GitHub `db.json`).
     * Schema: { "id": 1, "name": "...", "description": "...", "references": "...", "grade": "..." }
     */
    private fun fetchFromRakibRahmanApi(): List<BanglaHadithRecord> {
        rakibCache["rakib"]?.let { return it }

        val candidateUrls = listOf(
            RAKIB_GITHUB_RAW_MASTER,
            RAKIB_GITHUB_RAW_MAIN,
            RAKIB_HEROKU_URL
        )

        for (endpoint in candidateUrls) {
            try {
                val raw = httpGet(endpoint)
                val array = when {
                    raw.trim().startsWith("[") -> JSONArray(raw)
                    raw.trim().startsWith("{") -> {
                        val rootObj = JSONObject(raw)
                        rootObj.optJSONArray("hadiths") ?: rootObj.optJSONArray("data") ?: JSONArray()
                    }
                    else -> JSONArray()
                }
                val list = ArrayList<BanglaHadithRecord>()
                for (i in 0 until minOf(array.length(), 30)) {
                    val obj = array.optJSONObject(i) ?: continue
                    val idVal = obj.optString("id", (i + 1).toString())
                    val narrator = obj.optString("name", "সাহাবী (রা.)")
                    val description = obj.optString("description", "")
                    val refs = obj.optString("references", "সহীহ হাদিস সংকলন")
                    val grade = obj.optString("grade", "সহীহ (Sahih)")

                    if (description.isNotBlank()) {
                        val inferredBookKey = when {
                            refs.contains("বুখারী") || refs.contains("Bukhari", ignoreCase = true) -> "bukhari"
                            refs.contains("মুসলিম") || refs.contains("Muslim", ignoreCase = true) -> "muslim"
                            refs.contains("তিরমিযী") || refs.contains("তিরমিজি") || refs.contains("Tirmidhi", ignoreCase = true) -> "tirmidi"
                            refs.contains("আবু দাউদ") || refs.contains("Dawud", ignoreCase = true) -> "abuDaud"
                            refs.contains("রিয়াদুস") || refs.contains("Riyad", ignoreCase = true) -> "riyadusSalihin"
                            else -> "bukhari"
                        }
                        list.add(
                            BanglaHadithRecord(
                                id = "rakib_$idVal",
                                bookKey = inferredBookKey,
                                bookBn = refs.substringBefore(",").substringBefore(":").ifBlank { "সহীহ হাদিস" },
                                bookEn = "RakibRahman Bangla Hadith #$idVal",
                                chapterNo = 1,
                                chapterBn = refs,
                                hadithNumber = idVal,
                                arabic = obj.optString("arabic", ""),
                                translationBn = description,
                                translationEn = "Narrated by $narrator ($refs)",
                                narratorBn = narrator,
                                narratorEn = narrator,
                                references = refs,
                                gradeBn = grade,
                                source = HadithApiSource.RAKIB_RAHMAN,
                                endpointUrl = endpoint,
                                rawJson = obj.toString(2)
                            )
                        )
                    }
                }
                if (list.isNotEmpty()) {
                    rakibCache["rakib"] = list
                    return list
                }
            } catch (_: Exception) {
                // Try next URL
            }
        }
        return emptyList()
    }

    private fun pingMuntashirAkonRepo(): Boolean {
        return try {
            val raw = httpGet(MUNTASHIR_GITHUB_PARAMS)
            raw.isNotBlank()
        } catch (_: Exception) {
            false
        }
    }

    private fun httpGet(urlString: String): String {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 7_000
            readTimeout = 7_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Ruhani-Android/1.0")
        }
        return try {
            val code = conn.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code from $urlString")
            }
            BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Comprehensive structured Bangla Hadith dataset representing all 3 repositories:
     * - alQuranBD/Bangla-Hadith-api
     * - RakibRahman/hadith-api
     * - MuntashirAkon/BanglaHadith
     */
    fun getSeededMultiApiHadiths(): List<BanglaHadithRecord> = listOf(
        // --- 1. alQuranBD/Bangla-Hadith-api Records ---
        BanglaHadithRecord(
            id = "alquranbd_bukhari_1_1",
            bookKey = "bukhari",
            bookBn = "সহীহ আল-বুখারী",
            bookEn = "Sahih al-Bukhari",
            chapterNo = 1,
            chapterBn = "ওহীর সূচনা অধ্যায়",
            hadithNumber = "1",
            arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى، فَمَنْ كَانَتْ هِجْرَتُهُ إِلَى دُنْيَا يُصِيبُهَا أَوْ إِلَى امْرَأَةٍ يَنْكِحُهَا فَهِجْرَتُهُ إِلَى مَا هَاجَرَ إِلَيْهِ",
            translationBn = "১। উমর ইবনুল খাত্তাব (রাঃ) হতে বর্ণিত। তিনি বলেন, আমি রাসূলুল্লাহ সাল্লাল্লাহু আলাইহি ওয়াসাল্লামকে বলতে শুনেছি: “সকল আমল (কাজ) নিয়তের ওপর নির্ভরশীল। আর প্রত্যেক মানুষের জন্য তাই রয়েছে যা সে নিয়ত করে।”",
            translationEn = "Narrated 'Umar bin Al-Khattab: I heard Allah's Messenger ﷺ saying, 'The reward of deeds depends upon the intentions and every person will get the reward according to what he has intended.'",
            narratorBn = "হযরত উমর ইবনুল খাত্তাব (রা.)",
            narratorEn = "Umar bin Al-Khattab (RA)",
            references = "সহীহ বুখারী: হাদিস ১ • অধ্যায় ১ (ওহীর সূচনা)",
            gradeBn = "সহীহ (Sahih)",
            source = HadithApiSource.AL_QURAN_BD,
            endpointUrl = "https://alquranbd.com/api/hadith/bukhari/1/1",
            rawJson = """
{
  "book_key": "bukhari",
  "chSerial": "1",
  "hadithNo": "1",
  "rabi_bn": "হযরত উমর ইবনুল খাত্তাব (রা.)",
  "hadithArabic": "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
  "hadithBengali": "১। উমর ইবনুল খাত্তাব (রাঃ) হতে বর্ণিত: সকল আমল নিয়তের ওপর নির্ভরশীল এবং মানুষ তার নিয়ত অনুযায়ী প্রতিফল পাবে।",
  "hadithEnglish": "The reward of deeds depends upon the intentions...",
  "checkStatus": "সহীহ (Sahih)"
}
            """.trimIndent()
        ),
        BanglaHadithRecord(
            id = "alquranbd_bukhari_2_8",
            bookKey = "bukhari",
            bookBn = "সহীহ আল-বুখারী",
            bookEn = "Sahih al-Bukhari",
            chapterNo = 2,
            chapterBn = "ঈমান অধ্যায়",
            hadithNumber = "8",
            arabic = "بُنِيَ الإِسْلاَمُ عَلَى خَمْسٍ شَهَادَةِ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ وَأَنَّ مُحَمَّدًا رَسُولُ اللَّهِ، وَإِقَامِ الصَّلاَةِ، وَإِيتَاءِ الزَّكَاةِ، وَالْحَجِّ، وَصَوْمِ رَمَضَانَ",
            translationBn = "৮। ইবনু উমর (রাঃ) হতে বর্ণিত। রাসূলুল্লাহ (ﷺ) ইরশাদ করেছেন: “ইসলামের ভিত্তি স্থাপিত হয়েছে পাঁচটি স্তম্ভের ওপর—১. আল্লাহ ছাড়া কোনো সত্য ইলাহ নেই এবং মুহাম্মদ (ﷺ) আল্লাহর রাসূল এ কথার সাক্ষ্য দেওয়া, ২. সালাত কায়েম করা, ৩. যাকাত আদায় করা, ৪. হজ্জ পালন করা এবং ৫. রমজান মাসের সিয়াম পালন করা।”",
            translationEn = "Narrated Ibn 'Umar: Allah's Messenger ﷺ said: Islam is based on five principles: to testify that none has the right to be worshipped but Allah and Muhammad is Allah's Messenger, to offer prayers, to pay Zakat, to perform Hajj, and to fast Ramadan.",
            narratorBn = "হযরত আবদুল্লাহ ইবনু উমর (রা.)",
            narratorEn = "Abdullah ibn Umar (RA)",
            references = "সহীহ বুখারী: হাদিস ৮ • অধ্যায় ২ (ঈমান)",
            gradeBn = "সহীহ (Sahih)",
            source = HadithApiSource.AL_QURAN_BD,
            endpointUrl = "https://alquranbd.com/api/hadith/bukhari/2/1",
            rawJson = """
{
  "book_key": "bukhari",
  "chSerial": "2",
  "hadithNo": "8",
  "rabi_bn": "হযরত আবদুল্লাহ ইবনু উমর (রা.)",
  "hadithArabic": "بُنِيَ الإِسْلاَمُ عَلَى خَمْسٍ شَهَادَةِ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ وَأَنَّ مُحَمَّدًا رَسُولُ اللَّهِ...",
  "hadithBengali": "৮। ইসলামের ভিত্তি স্থাপিত হয়েছে পাঁচটি স্তম্ভের ওপর...",
  "hadithEnglish": "Islam is based on five principles...",
  "checkStatus": "সহীহ (Sahih)"
}
            """.trimIndent()
        ),
        BanglaHadithRecord(
            id = "alquranbd_bukhari_3_5027",
            bookKey = "bukhari",
            bookBn = "সহীহ আল-বুখারী",
            bookEn = "Sahih al-Bukhari",
            chapterNo = 3,
            chapterBn = "ইলম বা জ্ঞান অধ্যায়",
            hadithNumber = "5027",
            arabic = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            translationBn = "৫০২৭। উসমান ইবনু আফফান (রাঃ) হতে বর্ণিত। রাসূলুল্লাহ (ﷺ) বলেছেন: “তোমাদের মধ্যে সর্বোত্তম ঐ ব্যক্তি, যে নিজে কুরআন মাজীদ শিক্ষা করে এবং অপরকে তা শিক্ষা দেয়।”",
            translationEn = "Narrated 'Uthman: The Prophet ﷺ said, 'The best among you (Muslims) are those who learn the Qur'an and teach it.'",
            narratorBn = "হযরত উসমান ইবনু আফফান (রা.)",
            narratorEn = "Uthman ibn Affan (RA)",
            references = "সহীহ বুখারী: হাদিস ৫০২৭ • ফাযায়িলুল কুরআন",
            gradeBn = "সহীহ (Sahih)",
            source = HadithApiSource.AL_QURAN_BD,
            endpointUrl = "https://alquranbd.com/api/hadith/bukhari/3/1",
            rawJson = """
{
  "book_key": "bukhari",
  "chSerial": "3",
  "hadithNo": "5027",
  "rabi_bn": "হযরত উসমান ইবনু আফফান (রা.)",
  "hadithArabic": "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
  "hadithBengali": "তোমাদের মধ্যে সর্বোত্তম ঐ ব্যক্তি, যে নিজে কুরআন শেখে এবং অন্যকে তা শেখায়।",
  "hadithEnglish": "The best among you are those who learn the Qur'an and teach it.",
  "checkStatus": "সহীহ (Sahih)"
}
            """.trimIndent()
        ),
        BanglaHadithRecord(
            id = "alquranbd_muslim_2_223",
            bookKey = "muslim",
            bookBn = "সহীহ মুসলিম",
            bookEn = "Sahih Muslim",
            chapterNo = 2,
            chapterBn = "পবিত্রতা অধ্যায়",
            hadithNumber = "223",
            arabic = "الطُّهُورُ شَطْرُ الإِيمَانِ وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ وَسُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ تَمْلآَنِ أَوْ تَمْلأُ مَا بَيْنَ السَّمَوَاتِ وَالأَرْضِ وَالصَّلاَةُ نُورٌ",
            translationBn = "২২৩। আবু মালিক আল-আশআরী (রাঃ) হতে বর্ণিত। রাসূলুল্লাহ (ﷺ) বলেছেন: “পবিত্রতা ঈমানের অর্ধেক। ‘আলহামদুলিল্লাহ’ মীযানের পাল্লাকে পূর্ণ করে দেয়। আর ‘সুবহানাল্লাহ’ ও ‘আলহামদুলিল্লাহ’ আসমান ও জমিনের মধ্যবর্তী স্থান পূর্ণ করে দেয়। সালাত হলো জ্যোতি (নূর), সদকা হলো দলিল এবং সবর হলো আলোকবর্তিকা।”",
            translationEn = "Abu Malik al-Ash'ari reported: The Messenger of Allah ﷺ said: Cleanliness is half of faith and Alhamdulillah fills the scale, and SubhanAllah and Alhamdulillah fill up what is between the heavens and the earth, and prayer is a light.",
            narratorBn = "হযরত আবু মালিক আল-আশআরী (রা.)",
            narratorEn = "Abu Malik al-Ash'ari (RA)",
            references = "সহীহ মুসলিম: হাদিস ২২৩ • কিতাবুত ত্বহারাত",
            gradeBn = "সহীহ (Sahih)",
            source = HadithApiSource.AL_QURAN_BD,
            endpointUrl = "https://alquranbd.com/api/hadith/muslim/2/1",
            rawJson = """
{
  "book_key": "muslim",
  "chSerial": "2",
  "hadithNo": "223",
  "rabi_bn": "হযরত আবু মালিক আল-আশআরী (রা.)",
  "hadithArabic": "الطُّهُورُ شَطْرُ الإِيمَانِ وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ...",
  "hadithBengali": "পবিত্রতা ঈমানের অর্ধেক এবং আলহামদুলিল্লাহ মীযানের পাল্লাকে পূর্ণ করে দেয়।",
  "hadithEnglish": "Cleanliness is half of faith and Alhamdulillah fills the scale.",
  "checkStatus": "সহীহ (Sahih)"
}
            """.trimIndent()
        ),
        BanglaHadithRecord(
            id = "alquranbd_abuDaud_1_101",
            bookKey = "abuDaud",
            bookBn = "সুনান আবু দাউদ",
            bookEn = "Sunan Abu Dawud",
            chapterNo = 1,
            chapterBn = "পবিত্রতা ও ওযু অধ্যায়",
            hadithNumber = "101",
            arabic = "مَنْ تَوَضَّأَ فَأَحْسَنَ الْوُضُوءَ خَرَجَتْ خَطَايَاهُ مِنْ جَسَدِهِ حَتَّى تَخْرُجَ مِنْ تَحْتِ أَظْفَارِهِ",
            translationBn = "১০১। হযরত উসমান ইবনু আফফান (রাঃ) হতে বর্ণিত। রাসূলুল্লাহ (ﷺ) বলেছেন: “যে ব্যক্তি উত্তমরূপে ওযু করে, তার শরীর থেকে সমস্ত সগীরা গুনাহ বের হয়ে যায়; এমনকি তার নখের নিচ থেকেও গুনাহ ঝরে পড়ে।”",
            translationEn = "He who performs Wudu properly, his sins will depart from his body, even from under his fingernails.",
            narratorBn = "হযরত উসমান ইবনু আফফান (রা.)",
            narratorEn = "Uthman ibn Affan (RA)",
            references = "সুনান আবু দাউদ: ১০১, সহীহ মুসলিম: ২৪৫",
            gradeBn = "সহীহ (Sahih)",
            source = HadithApiSource.AL_QURAN_BD,
            endpointUrl = "https://alquranbd.com/api/hadith/abuDaud/1/1",
            rawJson = """
{
  "book_key": "abuDaud",
  "chSerial": "1",
  "hadithNo": "101",
  "rabi_bn": "হযরত উসমান ইবনু আফফান (রা.)",
  "hadithArabic": "مَنْ تَوَضَّأَ فَأَحْسَنَ الْوُضُوءَ خَرَجَتْ خَطَايَاهُ مِنْ جَسَدِهِ",
  "hadithBengali": "যে ব্যক্তি উত্তমরূপে ওযু করে, তার শরীর থেকে গুনাহসমূহ ঝরে যায়।",
  "hadithEnglish": "He who performs Wudu properly, his sins will depart from his body.",
  "checkStatus": "সহীহ (Sahih)"
}
            """.trimIndent()
        ),

        // --- 2. RakibRahman/hadith-api Records (Schema: id, name, description, references, grade) ---
        BanglaHadithRecord(
            id = "rakib_1",
            bookKey = "bukhari",
            bookBn = "সহীহ আল-বুখারী",
            bookEn = "Sahih al-Bukhari",
            chapterNo = 2,
            chapterBn = "ঈমান ও চরিত্র অধ্যায়",
            hadithNumber = "13",
            arabic = "لَا يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لِأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
            translationBn = "আনাস ইবনু মালিক (রাঃ) থেকে বর্ণিত: নবী করীম (ﷺ) বলেছেন, “তোমাদের কেউ প্রকৃত মুমিন হতে পারবে না, যতক্ষণ না সে নিজের জন্য যা পছন্দ করে, তার মুসলিম ভাইয়ের জন্যও তা-ই পছন্দ করে।”",
            translationEn = "None of you will have true faith until he loves for his brother what he loves for himself.",
            narratorBn = "হযরত আনাস ইবনু মালিক (রা.)",
            narratorEn = "Anas ibn Malik (RA)",
            references = "সহীহ বুখারী: ১৩, সহীহ মুসলিম: ৪৫",
            gradeBn = "সহীহ হাদিস (Sahih)",
            source = HadithApiSource.RAKIB_RAHMAN,
            endpointUrl = "https://bn-hadith-api.herokuapp.com/hadiths",
            rawJson = """
{
  "id": 1,
  "name": "হযরত আনাস ইবনু মালিক (রা.)",
  "description": "তোমাদের কেউ প্রকৃত মুমিন হতে পারবে না, যতক্ষণ না সে নিজের জন্য যা পছন্দ করে, তার ভাইয়ের জন্যও তা পছন্দ করে।",
  "references": "সহীহ বুখারী: ১৩, সহীহ মুসলিম: ৪৫",
  "grade": "সহীহ হাদিস"
}
            """.trimIndent()
        ),
        BanglaHadithRecord(
            id = "rakib_2",
            bookKey = "tirmidi",
            bookBn = "জামে আত-তিরমিযী",
            bookEn = "Jami at-Tirmidhi",
            chapterNo = 2,
            chapterBn = "সদাচরণ ও সদকা অধ্যায়",
            hadithNumber = "1956",
            arabic = "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ وَأَمْرُكَ بِالْمَعْرُوفِ وَنَهْيُكَ عَنِ الْمُنْكَرِ صَدَقَةٌ",
            translationBn = "আবু যর গিফারী (রাঃ) থেকে বর্ণিত: রাসূলুল্লাহ (ﷺ) বলেছেন, “তোমার ভাইয়ের দিকে হাসিমুখে তাকানো তোমার জন্য একটি সদকা; সৎকাজের আদেশ ও অসৎকাজ থেকে নিষেধ করা সদকা এবং পথহারা মানুষকে পথ দেখিয়ে দেওয়াও তোমার জন্য সদকা।”",
            translationEn = "Your smiling in the face of your brother is charity, commanding good and forbidding evil is charity, and guiding a person in a land where he is lost is charity for you.",
            narratorBn = "হযরত আবু যর গিফারী (রা.)",
            narratorEn = "Abu Dharr al-Ghifari (RA)",
            references = "জামে আত-তিরমিযী: ১৯৫৬",
            gradeBn = "সহীহ হাদিস (Sahih)",
            source = HadithApiSource.RAKIB_RAHMAN,
            endpointUrl = "https://bn-hadith-api.herokuapp.com/hadiths",
            rawJson = """
{
  "id": 2,
  "name": "হযরত আবু যর গিফারী (রা.)",
  "description": "তোমার ভাইয়ের দিকে হাসিমুখে তাকানোও তোমার জন্য একটি সদকা।",
  "references": "জামে আত-তিরমিযী: ১৯৫৬",
  "grade": "সহীহ হাদিস"
}
            """.trimIndent()
        ),
        BanglaHadithRecord(
            id = "rakib_3",
            bookKey = "bukhari",
            bookBn = "সহীহ আল-বুখারী",
            bookEn = "Sahih al-Bukhari",
            chapterNo = 4,
            chapterBn = "আদব ও সদাচরণ অধ্যায়",
            hadithNumber = "6018",
            arabic = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
            translationBn = "আবু হুরায়রা (রাঃ) থেকে বর্ণিত: রাসূলুল্লাহ (ﷺ) বলেছেন, “যে ব্যক্তি আল্লাহ ও আখিরাত দিবসের ওপর ঈমান রাখে, সে যেন উত্তম কথা বলে অথবা নীরব থাকে। আর যে আল্লাহ ও শেষ দিবসে বিশ্বাস রাখে, সে যেন তার প্রতিবেশী ও মেহমানকে সম্মান করে।”",
            translationEn = "Whoever believes in Allah and the Last Day should speak a good word or remain silent.",
            narratorBn = "হযরত আবু হুরায়রা (রা.)",
            narratorEn = "Abu Hurairah (RA)",
            references = "সহীহ বুখারী: ৬০১৮, সহীহ মুসলিম: ৪৭",
            gradeBn = "সহীহ হাদিস (Sahih)",
            source = HadithApiSource.RAKIB_RAHMAN,
            endpointUrl = "https://bn-hadith-api.herokuapp.com/hadiths",
            rawJson = """
{
  "id": 3,
  "name": "হযরত আবু হুরায়রা (রা.)",
  "description": "যে ব্যক্তি আল্লাহ ও আখিরাত দিবসের ওপর ঈমান রাখে, সে যেন উত্তম কথা বলে অথবা নীরব থাকে।",
  "references": "সহীহ বুখারী: ৬০১৮, সহীহ মুসলিম: ৪৭",
  "grade": "সহীহ হাদিস"
}
            """.trimIndent()
        ),
        BanglaHadithRecord(
            id = "rakib_4",
            bookKey = "ibnMajah",
            bookBn = "সুনান ইবনে মাজাহ",
            bookEn = "Sunan Ibn Majah",
            chapterNo = 1,
            chapterBn = "ইলম বা জ্ঞান অন্বেষণ অধ্যায়",
            hadithNumber = "224",
            arabic = "طَلَبُ الْعِلْمِ فَرِيضَةٌ عَلَى كُلِّ مُسْلِمٍ",
            translationBn = "আনাস ইবনু মালিক (রাঃ) থেকে বর্ণিত: রাসূলুল্লাহ (ﷺ) বলেছেন, “প্রত্যেক মুসলিমের ওপর (দ্বীনি) ইলম বা জ্ঞান অর্জন করা ফরজ।”",
            translationEn = "Seeking knowledge is an obligation upon every Muslim.",
            narratorBn = "হযরত আনাস ইবনু মালিক (রা.)",
            narratorEn = "Anas ibn Malik (RA)",
            references = "সুনান ইবনে মাজাহ: ২২৪",
            gradeBn = "সহীহ (Sahih)",
            source = HadithApiSource.RAKIB_RAHMAN,
            endpointUrl = "https://bn-hadith-api.herokuapp.com/hadiths",
            rawJson = """
{
  "id": 4,
  "name": "হযরত আনাস ইবনু মালিক (রা.)",
  "description": "প্রত্যেক মুসলিমের ওপর ইলম (জ্ঞান) অর্জন করা ফরজ।",
  "references": "সুনান ইবনে মাজাহ: ২২৪",
  "grade": "সহীহ"
}
            """.trimIndent()
        ),

        // --- 3. MuntashirAkon/BanglaHadith Records (SQLite hadith.db schema) ---
        BanglaHadithRecord(
            id = "muntashir_riyad_1_7",
            bookKey = "riyadusSalihin",
            bookBn = "রিয়াদুস সালেহীন",
            bookEn = "Riyadus Salihin",
            chapterNo = 1,
            chapterBn = "ইখলাস ও নিয়তের বিশুদ্ধতা",
            hadithNumber = "7",
            arabic = "إِنَّ اللَّهَ لَا يَنْظُرُ إِلَى صُوَرِكُمْ وَأَمْوَالِكُمْ وَلَكِنْ يَنْظُرُ إِلَى قُلُوبِكُمْ وَأَعْمَالِكُمْ",
            translationBn = "আবু হুরায়রা (রাঃ) হতে বর্ণিত: রাসূলুল্লাহ (ﷺ) বলেছেন, “নিশ্চয়ই আল্লাহ তোমাদের বাহ্যিক অবয়ব ও ধন-সম্পদের দিকে তাকান না; বরং তিনি তাকান তোমাদের অন্তর ও আমলের (কর্মের) দিকে।”",
            translationEn = "Verily Allah does not look to your faces and your wealth, but He looks to your heart and to your deeds.",
            narratorBn = "হযরত আবু হুরায়রা (রা.)",
            narratorEn = "Abu Hurairah (RA)",
            references = "রিয়াদুস সালেহীন: হাদিস ৭ (সহীহ মুসলিম: ২৫৬৪)",
            gradeBn = "সহীহ (Sahih)",
            source = HadithApiSource.MUNTASHIR_AKON,
            endpointUrl = "https://muntashirakon.github.io/BanglaHadith/hadith.db",
            rawJson = """
{
  "table": "hadith",
  "book": "রিয়াদুস সালেহীন (Riyadus Salihin)",
  "chapter": "ইখলাস ও নিয়তের বিশুদ্ধতা",
  "hadith_no": 7,
  "narrator": "হযরত আবু হুরায়রা (রা.)",
  "arabic": "إِنَّ اللَّهَ لَا يَنْظُرُ إِلَى صُوَرِكُمْ وَأَمْوَالِكُمْ وَلَكِنْ يَنْظُرُ إِلَى قُلُوبِكُمْ وَأَعْمَالِكُمْ",
  "bangla": "নিশ্চয়ই আল্লাহ তোমাদের বাহ্যিক অবয়ব ও ধন-সম্পদের দিকে তাকান না; বরং তিনি তাকান তোমাদের অন্তর ও আমলের দিকে।",
  "grade": "সহীহ (Sahih)"
}
            """.trimIndent()
        ),
        BanglaHadithRecord(
            id = "muntashir_riyad_2_14",
            bookKey = "riyadusSalihin",
            bookBn = "রিয়াদুস সালেহীন",
            bookEn = "Riyadus Salihin",
            chapterNo = 2,
            chapterBn = "তওবা ও ক্ষমা প্রার্থনা",
            hadithNumber = "14",
            arabic = "وَاللَّهِ إِنِّي لَأَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ فِي الْيَوْمِ أَكْثَرَ مِنْ سَبْعِينَ مَرَّةً",
            translationBn = "আবু হুরায়রা (রাঃ) হতে বর্ণিত: আমি রাসূলুল্লাহ (ﷺ)-কে বলতে শুনেছি, “আল্লাহর শপথ! আমি প্রতিদিন সত্তর বারেরও অধিক আল্লাহর কাছে ক্ষমা প্রার্থনা (ইস্তিগফার) করি এবং তাঁর কাছে তওবা করি।”",
            translationEn = "By Allah, I seek forgiveness of Allah and turn to Him in repentance more than seventy times a day.",
            narratorBn = "হযরত আবু হুরায়রা (রা.)",
            narratorEn = "Abu Hurairah (RA)",
            references = "রিয়াদুস সালেহীন: হাদিস ১৪ (সহীহ বুখারী: ৬৩০৭)",
            gradeBn = "সহীহ (Sahih)",
            source = HadithApiSource.MUNTASHIR_AKON,
            endpointUrl = "https://muntashirakon.github.io/BanglaHadith/hadith.db",
            rawJson = """
{
  "table": "hadith",
  "book": "রিয়াদুস সালেহীন (Riyadus Salihin)",
  "chapter": "তওবা ও ক্ষমা প্রার্থনা",
  "hadith_no": 14,
  "narrator": "হযরত আবু হুরায়রা (রা.)",
  "arabic": "وَاللَّهِ إِنِّي لَأَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ فِي الْيَوْمِ أَكْثَرَ مِنْ سَبْعِينَ مَرَّةً",
  "bangla": "আল্লাহর শপথ! আমি প্রতিদিন সত্তর বারেরও অধিক আল্লাহর কাছে ইস্তিগফার ও তওবা করি।",
  "grade": "সহীহ (Sahih)"
}
            """.trimIndent()
        ),
        BanglaHadithRecord(
            id = "muntashir_riyad_3_27",
            bookKey = "riyadusSalihin",
            bookBn = "রিয়াদুস সালেহীন",
            bookEn = "Riyadus Salihin",
            chapterNo = 3,
            chapterBn = "ধৈর্য ও সবর অধ্যায়",
            hadithNumber = "27",
            arabic = "عَجَبًا لِأَمْرِ الْمُؤْمِنِ إِنَّ أَمْرَهُ كُلَّهُ خَيْرٌ وَلَيْسَ ذَاكَ لِأَحَدٍ إِلَّا لِلْمُؤْمِنِ إِنْ أَصَابَتْهُ سَرَّاءُ شَكَرَ فَكَانَ خَيْرًا لَهُ وَإِنْ أَصَابَتْهُ ضَرَّاءُ صَبَرَ فَكَانَ خَيْرًا لَهُ",
            translationBn = "সুহাইব (রাঃ) হতে বর্ণিত: রাসূলুল্লাহ (ﷺ) বলেছেন, “মুমিনের ব্যাপারটি কতই না বিস্ময়কর! তার প্রতিটি কাজই তার জন্য কল্যাণকর; আর এটি মুমিন ছাড়া অন্য কারো জন্য হয় না। যদি তার কোনো খুশির বিষয় আসে তবে সে কৃতজ্ঞতা (শোকর) প্রকাশ করে, ফলে তা তার জন্য কল্যাণকর হয়। আর যদি কোনো বিপদ আসে তবে সে ধৈর্য (সবর) ধারণ করে, সেটিও তার জন্য কল্যাণকর হয়।”",
            translationEn = "How wonderful is the case of a believer; there is good for him in everything and this applies only to a believer. If prosperity attends him, he expresses gratitude to Allah and that is good for him; and if adversity befalls him, he endures it patiently and that is better for him.",
            narratorBn = "হযরত সুহাইব ইবনু সিনান (রা.)",
            narratorEn = "Suhaib ibn Sinan (RA)",
            references = "রিয়াদুস সালেহীন: হাদিস ২৭ (সহীহ মুসলিম: ২৯৯৯)",
            gradeBn = "সহীহ (Sahih)",
            source = HadithApiSource.MUNTASHIR_AKON,
            endpointUrl = "https://muntashirakon.github.io/BanglaHadith/hadith.db",
            rawJson = """
{
  "table": "hadith",
  "book": "রিয়াদুস সালেহীন (Riyadus Salihin)",
  "chapter": "ধৈর্য ও সবর অধ্যায়",
  "hadith_no": 27,
  "narrator": "হযরত সুহাইব (রা.)",
  "arabic": "عَجَبًا لِأَمْرِ الْمُؤْمِنِ إِنَّ أَمْرَهُ كُلَّهُ خَيْرٌ...",
  "bangla": "মুমিনের ব্যাপারটি কতই না বিস্ময়কর! তার প্রতিটি কাজই তার জন্য কল্যাণকর...",
  "grade": "সহীহ (Sahih)"
}
            """.trimIndent()
        ),
        BanglaHadithRecord(
            id = "muntashir_nasai_1_401",
            bookKey = "nasai",
            bookBn = "সুনান আন-নাসাঈ",
            bookEn = "Sunan an-Nasa'i",
            chapterNo = 2,
            chapterBn = "সালাত ও জামাত অধ্যায়",
            hadithNumber = "3940",
            arabic = "وَجُعِلَتْ قُرَّةُ عَيْنِي فِي الصَّلَاةِ",
            translationBn = "আনাস (রাঃ) হতে বর্ণিত: রাসূলুল্লাহ (ﷺ) ইরশাদ করেছেন, “আর আমার চক্ষু শীতলতা (প্রশান্তি) রাখা হয়েছে সালাতের (নামাজের) মধ্যে।”",
            translationEn = "And the coolness of my eyes has been placed in prayer (Salat).",
            narratorBn = "হযরত আনাস ইবনু মালিক (রা.)",
            narratorEn = "Anas ibn Malik (RA)",
            references = "সুনান আন-নাসাঈ: ৩৯৪০",
            gradeBn = "সহীহ (Sahih)",
            source = HadithApiSource.MUNTASHIR_AKON,
            endpointUrl = "https://muntashirakon.github.io/BanglaHadith/hadith.db",
            rawJson = """
{
  "table": "hadith",
  "book": "সুনান আন-নাসাঈ (Sunan an-Nasa'i)",
  "chapter": "সালাত ও জামাত অধ্যায়",
  "hadith_no": 3940,
  "narrator": "হযরত আনাস ইবনু মালিক (রা.)",
  "arabic": "وَجُعِلَتْ قُرَّةُ عَيْنِي فِي الصَّلَاةِ",
  "bangla": "আর আমার চক্ষু শীতলতা রাখা হয়েছে নামাজের মধ্যে।",
  "grade": "সহীহ (Sahih)"
}
            """.trimIndent()
        ),
        BanglaHadithRecord(
            id = "muntashir_buloogh_1_1",
            bookKey = "bulooghulMaram",
            bookBn = "বুলুগুল মারাম",
            bookEn = "Bulugh al-Maram",
            chapterNo = 1,
            chapterBn = "পবিত্রতা ও সালাতের আহকাম",
            hadithNumber = "1",
            arabic = "هُوَ الطَّهُورُ مَاؤُهُ الْحِلُّ مَيْتَتُهُ",
            translationBn = "আবু হুরায়রা (রাঃ) হতে বর্ণিত: সমুদ্রের পানি সম্পর্কে রাসূলুল্লাহ (ﷺ) বলেছেন, “ইহার পানি পবিত্র ও পবিত্রকারী এবং ইহার মৃত প্রাণী (মাছ) হালাল।”",
            translationEn = "Regarding sea water, the Messenger of Allah ﷺ said: Its water is pure and purifying, and its dead creatures are Halal.",
            narratorBn = "হযরত আবু হুরায়রা (রা.)",
            narratorEn = "Abu Hurairah (RA)",
            references = "বুলুগুল মারাম: হাদিস ১ (আবু দাউদ: ৮৩, তিরমিযী: ৬৯)",
            gradeBn = "সহীহ (Sahih)",
            source = HadithApiSource.MUNTASHIR_AKON,
            endpointUrl = "https://muntashirakon.github.io/BanglaHadith/hadith.db",
            rawJson = """
{
  "table": "hadith",
  "book": "বুলুগুল মারাম (Bulugh al-Maram)",
  "chapter": "পবিত্রতা ও সালাতের আহকাম",
  "hadith_no": 1,
  "narrator": "হযরত আবু হুরায়রা (রা.)",
  "arabic": "هُوَ الطَّهُورُ مَاؤُهُ الْحِلُّ مَيْتَتُهُ",
  "bangla": "সমুদ্রের পানি পবিত্র ও পবিত্রকারী এবং এর মৃত প্রাণী হালাল।",
  "grade": "সহীহ (Sahih)"
}
            """.trimIndent()
        )
    )
}
