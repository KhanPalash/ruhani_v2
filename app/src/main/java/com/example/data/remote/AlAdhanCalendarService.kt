package com.example.data.remote

import com.example.domain.AstronomicalEngine
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

enum class HijriCalendarMethod(
    val apiCode: String,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String
) {
    HJCOSA(
        apiCode = "HJCoSA",
        titleBn = "হাই জুডিশিয়াল কাউন্সিল (HJCoSA)",
        titleEn = "High Judicial Council of Saudi Arabia",
        descriptionBn = "সৌদি আরবের উচ্চ বিচারিক পরিষদের চাঁদ দেখার স্ট্যান্ডার্ড"
    ),
    UAQ(
        apiCode = "UAQ",
        titleBn = "উম্মুল কুরা (Umm al-Qura)",
        titleEn = "Umm al-Qura Calendar (Makkah)",
        descriptionBn = "মক্কার উম্মুল কুরা জ্যোতির্বৈজ্ঞানিক বর্ষপঞ্জি"
    ),
    DIYANET(
        apiCode = "DIYANET",
        titleBn = "দিয়ানাত (Diyanet)",
        titleEn = "Diyanet İşleri Başkanlığı",
        descriptionBn = "তুরস্কের ধর্মীয় বিষয়ক অধিদপ্তরের হিসাব পদ্ধতি"
    ),
    MATHEMATICAL(
        apiCode = "MATHEMATICAL",
        titleBn = "গাণিতিক হিজরি (Mathematical)",
        titleEn = "Tabular Mathematical Hijri",
        descriptionBn = "ধ্রুপদী গাণিতিক হিজরি বর্ষপঞ্জি"
    );

    companion object {
        fun fromCode(code: String): HijriCalendarMethod =
            entries.firstOrNull { it.apiCode.equals(code, ignoreCase = true) } ?: HJCOSA
    }
}

data class AlAdhanCalendarDay(
    val gregorianDate: String, // DD-MM-YYYY
    val gregorianDay: Int,
    val gregorianWeekdayEn: String,
    val gregorianWeekdayBn: String,
    val gregorianMonthNumber: Int,
    val gregorianMonthEn: String,
    val gregorianYear: Int,
    val hijriDate: String, // DD-MM-YYYY
    val hijriDay: Int,
    val hijriWeekdayEn: String,
    val hijriWeekdayAr: String,
    val hijriMonthNumber: Int,
    val hijriMonthEn: String,
    val hijriMonthAr: String,
    val hijriMonthBn: String,
    val hijriYear: Int,
    val banglaDateFormatted: String,
    val holidays: List<String>,
    val isWhiteDayFasting: Boolean
)

data class AlAdhanMonthlyCalendar(
    val month: Int,
    val year: Int,
    val method: HijriCalendarMethod,
    val adjustment: Int,
    val endpointUrl: String,
    val days: List<AlAdhanCalendarDay>,
    val isFromLiveApi: Boolean
)

/**
 * Client for the AlAdhan Islamic / Hijri Calendar API:
 * Documentation: https://aladhan.com/islamic-calendar-api
 *
 * Primary Endpoints:
 * - GET https://api.aladhan.com/v1/gToHCalendar/{month}/{year}?calendarMethod={method}&adjustment={offset}
 * - GET https://api.aladhan.com/v1/gToH/{DD-MM-YYYY}?calendarMethod={method}&adjustment={offset}
 */
object AlAdhanCalendarService {

    private const val BASE_URL = "https://api.aladhan.com/v1"
    private val monthCache = ConcurrentHashMap<String, AlAdhanMonthlyCalendar>()

    private val hijriMonthNamesBn = mapOf(
        1 to "মুহাররম",
        2 to "সফর",
        3 to "রবিউল আউয়াল",
        4 to "রবিউস সানি",
        5 to "জমাদিউল আউয়াল",
        6 to "জমাদিউস সানি",
        7 to "রজব",
        8 to "শাবান",
        9 to "রমজান",
        10 to "শাওয়াল",
        11 to "জিলকদ",
        12 to "জিলহজ"
    )

    private val weekdayBnMap = mapOf(
        "Sunday" to "রবিবার",
        "Monday" to "সোমবার",
        "Tuesday" to "মঙ্গলবার",
        "Wednesday" to "বুধবার",
        "Thursday" to "বৃহস্পতিবার",
        "Friday" to "শুক্রবার",
        "Saturday" to "শনিবার"
    )

    fun buildMonthlyEndpointUrl(
        month: Int,
        year: Int,
        method: HijriCalendarMethod,
        adjustment: Int
    ): String = "$BASE_URL/gToHCalendar/$month/$year?calendarMethod=${method.apiCode}&adjustment=$adjustment"

    fun buildDateConversionEndpointUrl(
        dateDdMmYyyy: String,
        method: HijriCalendarMethod,
        adjustment: Int
    ): String = "$BASE_URL/gToH/$dateDdMmYyyy?calendarMethod=${method.apiCode}&adjustment=$adjustment"

    suspend fun fetchMonthlyHijriCalendar(
        month: Int,
        year: Int,
        method: HijriCalendarMethod = HijriCalendarMethod.HJCOSA,
        adjustment: Int = 0
    ): Result<AlAdhanMonthlyCalendar> = withContext(Dispatchers.IO) {
        val safeMonth = month.coerceIn(1, 12)
        val safeYear = year.coerceIn(1970, 2100)
        val cacheKey = "${safeYear}_${safeMonth}_${method.apiCode}_$adjustment"
        monthCache[cacheKey]?.let { cached ->
            if (cached.isFromLiveApi) return@withContext Result.success(cached)
        }

        val endpoint = buildMonthlyEndpointUrl(safeMonth, safeYear, method, adjustment)
        try {
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("Accept", "application/json")
            }

            try {
                if (conn.responseCode in 200..299) {
                    val body = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    val root = JSONObject(body)
                    val dataArray = root.optJSONArray("data")
                    if (dataArray != null && dataArray.length() > 0) {
                        val parsedDays = mutableListOf<AlAdhanCalendarDay>()
                        for (i in 0 until dataArray.length()) {
                            val item = dataArray.optJSONObject(i) ?: continue
                            parsedDays.add(parseDayObject(item, adjustment))
                        }
                        if (parsedDays.isNotEmpty()) {
                            val calendar = AlAdhanMonthlyCalendar(
                                month = safeMonth,
                                year = safeYear,
                                method = method,
                                adjustment = adjustment,
                                endpointUrl = endpoint,
                                days = parsedDays,
                                isFromLiveApi = true
                            )
                            monthCache[cacheKey] = calendar
                            return@withContext Result.success(calendar)
                        }
                    }
                }
            } finally {
                conn.disconnect()
            }
        } catch (_: Exception) {
            // Fall through to deterministic calculation when offline
        }

        val fallback = buildFallbackMonthlyCalendar(safeMonth, safeYear, method, adjustment, endpoint)
        monthCache[cacheKey] = fallback
        Result.success(fallback)
    }

    suspend fun convertGregorianToHijri(
        dateDdMmYyyy: String,
        method: HijriCalendarMethod = HijriCalendarMethod.HJCOSA,
        adjustment: Int = 0
    ): Result<AlAdhanCalendarDay> = withContext(Dispatchers.IO) {
        val endpoint = buildDateConversionEndpointUrl(dateDdMmYyyy, method, adjustment)
        try {
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("Accept", "application/json")
            }
            try {
                if (conn.responseCode in 200..299) {
                    val body = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    val root = JSONObject(body)
                    val dataObj = root.optJSONObject("data")
                    if (dataObj != null) {
                        return@withContext Result.success(parseDayObject(dataObj, adjustment))
                    }
                }
            } finally {
                conn.disconnect()
            }
        } catch (_: Exception) {
        }

        // Offline fallback conversion for DD-MM-YYYY
        val parsedLocalDate = try {
            LocalDate.parse(dateDdMmYyyy, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        } catch (_: Exception) {
            LocalDate.now()
        }
        Result.success(buildFallbackDay(parsedLocalDate, adjustment))
    }

    private fun parseDayObject(item: JSONObject, adjustment: Int): AlAdhanCalendarDay {
        val gregObj = item.optJSONObject("gregorian") ?: JSONObject()
        val hijriObj = item.optJSONObject("hijri") ?: JSONObject()

        val gregDateStr = gregObj.optString("date", "01-01-2026")
        val gregDay = gregObj.optString("day", "1").toIntOrNull() ?: 1
        val gregWeekdayEn = gregObj.optJSONObject("weekday")?.optString("en", "Monday") ?: "Monday"
        val gregMonthObj = gregObj.optJSONObject("month")
        val gregMonthNum = gregMonthObj?.optInt("number", 1) ?: 1
        val gregMonthEn = gregMonthObj?.optString("en", "January") ?: "January"
        val gregYear = gregObj.optString("year", "2026").toIntOrNull() ?: 2026

        val hijriDateStr = hijriObj.optString("date", "01-01-1447")
        val rawHijriDay = hijriObj.optString("day", "1").toIntOrNull() ?: 1
        val hijriWeekdayObj = hijriObj.optJSONObject("weekday")
        val hijriWeekdayEn = hijriWeekdayObj?.optString("en", "Al Ithnayn") ?: ""
        val hijriWeekdayAr = hijriWeekdayObj?.optString("ar", "الإثنين") ?: ""
        val hijriMonthObj = hijriObj.optJSONObject("month")
        val hijriMonthNum = hijriMonthObj?.optInt("number", 1) ?: 1
        val hijriMonthEn = hijriMonthObj?.optString("en", "Muḥarram") ?: "Muḥarram"
        val hijriMonthAr = hijriMonthObj?.optString("ar", "مُحَرَّم") ?: "مُحَرَّم"
        val hijriYear = hijriObj.optString("year", "1447").toIntOrNull() ?: 1447

        val holidaysList = mutableListOf<String>()
        val holidaysArr = hijriObj.optJSONArray("holidays")
        if (holidaysArr != null) {
            for (h in 0 until holidaysArr.length()) {
                val holiday = holidaysArr.optString(h).trim()
                if (holiday.isNotEmpty()) holidaysList.add(holiday)
            }
        }

        val localDate = try {
            LocalDate.of(gregYear, gregMonthNum.coerceIn(1, 12), gregDay.coerceIn(1, 28))
        } catch (_: Exception) {
            LocalDate.now()
        }
        val banglaDate = computeBanglaDateFormatted(localDate)

        // Add notable Islamic observances if API holidays list is empty
        if (holidaysList.isEmpty()) {
            when {
                hijriMonthNum == 1 && rawHijriDay == 1 -> holidaysList.add("ইসলামী নববর্ষ (1st Muharram)")
                hijriMonthNum == 1 && rawHijriDay == 10 -> holidaysList.add("পবিত্র আশুরা (Day of Ashura)")
                hijriMonthNum == 3 && rawHijriDay == 12 -> holidaysList.add("ঈদে মিলাদুন্নবী (স.)")
                hijriMonthNum == 7 && rawHijriDay == 27 -> holidaysList.add("শবে মেরাজ (Lailatul Miraj)")
                hijriMonthNum == 8 && rawHijriDay == 15 -> holidaysList.add("শবে বরাত (Nisf Sha'ban)")
                hijriMonthNum == 9 && rawHijriDay == 1 -> holidaysList.add("পবিত্র রমজান শুরু (1st Ramadan)")
                hijriMonthNum == 9 && rawHijriDay == 27 -> holidaysList.add("শবে কদর (Lailatul Qadr)")
                hijriMonthNum == 10 && rawHijriDay == 1 -> holidaysList.add("পবিত্র ঈদুল ফিতর (Eid-ul-Fitr)")
                hijriMonthNum == 12 && rawHijriDay == 9 -> holidaysList.add("পবিত্র আরাফার দিন (Day of Arafah)")
                hijriMonthNum == 12 && rawHijriDay == 10 -> holidaysList.add("পবিত্র ঈদুল আযহা (Eid-ul-Adha)")
            }
        }

        return AlAdhanCalendarDay(
            gregorianDate = gregDateStr,
            gregorianDay = gregDay,
            gregorianWeekdayEn = gregWeekdayEn,
            gregorianWeekdayBn = weekdayBnMap[gregWeekdayEn] ?: gregWeekdayEn,
            gregorianMonthNumber = gregMonthNum,
            gregorianMonthEn = gregMonthEn,
            gregorianYear = gregYear,
            hijriDate = hijriDateStr,
            hijriDay = rawHijriDay,
            hijriWeekdayEn = hijriWeekdayEn,
            hijriWeekdayAr = hijriWeekdayAr,
            hijriMonthNumber = hijriMonthNum,
            hijriMonthEn = hijriMonthEn,
            hijriMonthAr = hijriMonthAr,
            hijriMonthBn = hijriMonthNamesBn[hijriMonthNum] ?: hijriMonthEn,
            hijriYear = hijriYear,
            banglaDateFormatted = banglaDate,
            holidays = holidaysList,
            isWhiteDayFasting = rawHijriDay in listOf(13, 14, 15)
        )
    }

    private fun buildFallbackMonthlyCalendar(
        month: Int,
        year: Int,
        method: HijriCalendarMethod,
        adjustment: Int,
        endpointUrl: String
    ): AlAdhanMonthlyCalendar {
        val yearMonth = YearMonth.of(year, month)
        val days = (1..yearMonth.lengthOfMonth()).map { dayOfMonth ->
            val date = LocalDate.of(year, month, dayOfMonth)
            buildFallbackDay(date, adjustment)
        }
        return AlAdhanMonthlyCalendar(
            month = month,
            year = year,
            method = method,
            adjustment = adjustment,
            endpointUrl = endpointUrl,
            days = days,
            isFromLiveApi = false
        )
    }

    private fun buildFallbackDay(date: LocalDate, adjustment: Int): AlAdhanCalendarDay {
        val adjustedDate = date.plusDays(adjustment.toLong())
        val jd = adjustedDate.toEpochDay() + 2440588L
        var l = jd - 1948440L + 10632L
        val n = (l - 1L) / 10631L
        l = l - 10631L * n + 354L
        val j = ((10985L - l) / 5316L) * ((50L * l) / 17719L) + (l / 5670L) * ((43L * l) / 15238L)
        l = l - ((30L - j) / 15L) * ((17719L * j) / 50L) - (j / 16L) * ((15238L * j) / 43L) + 29L
        val hMonth = ((24L * l) / 709L).toInt().coerceIn(1, 12)
        val hDay = (l - (709L * hMonth) / 24L).toInt().coerceIn(1, 30)
        val hYear = (30L * n + j - 30L).toInt()

        val hijriMonthsEn = listOf(
            "Muḥarram", "Ṣafar", "Rabīʿ al-awwal", "Rabīʿ al-thānī",
            "Jumādá al-ūlá", "Jumādá al-ākhirah", "Rajab", "Shaʿbān",
            "Ramaḍān", "Shawwāl", "Dhū al-Qaʿdah", "Dhū al-Ḥijjah"
        )
        val hijriMonthsAr = listOf(
            "مُحَرَّم", "صَفَر", "رَبِيع الأَوَّل", "رَبِيع الثَّانِي",
            "جُمَادَىٰ الأُولَىٰ", "جُمَادَىٰ الآخِرَة", "رَجَب", "شَعْبَان",
            "رَمَضَان", "شَوَّال", "ذُو القَعْدَة", "ذُو الحِجَّة"
        )
        val weekdayEn = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val holidays = mutableListOf<String>()
        when {
            hMonth == 1 && hDay == 1 -> holidays.add("ইসলামী নববর্ষ (1st Muharram)")
            hMonth == 1 && hDay == 10 -> holidays.add("পবিত্র আশুরা (Day of Ashura)")
            hMonth == 3 && hDay == 12 -> holidays.add("ঈদে মিলাদুন্নবী (স.)")
            hMonth == 7 && hDay == 27 -> holidays.add("শবে মেরাজ (Lailatul Miraj)")
            hMonth == 8 && hDay == 15 -> holidays.add("শবে বরাত (Nisf Sha'ban)")
            hMonth == 9 && hDay == 1 -> holidays.add("পবিত্র রমজান শুরু (1st Ramadan)")
            hMonth == 9 && hDay == 27 -> holidays.add("শবে কদর (Lailatul Qadr)")
            hMonth == 10 && hDay == 1 -> holidays.add("পবিত্র ঈদুল ফিতর (Eid-ul-Fitr)")
            hMonth == 12 && hDay == 9 -> holidays.add("পবিত্র আরাফার দিন (Day of Arafah)")
            hMonth == 12 && hDay == 10 -> holidays.add("পবিত্র ঈদুল আযহা (Eid-ul-Adha)")
        }

        return AlAdhanCalendarDay(
            gregorianDate = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
            gregorianDay = date.dayOfMonth,
            gregorianWeekdayEn = weekdayEn,
            gregorianWeekdayBn = weekdayBnMap[weekdayEn] ?: weekdayEn,
            gregorianMonthNumber = date.monthValue,
            gregorianMonthEn = date.month.name.lowercase().replaceFirstChar { it.uppercase() },
            gregorianYear = date.year,
            hijriDate = "%02d-%02d-%04d".format(hDay, hMonth, hYear),
            hijriDay = hDay,
            hijriWeekdayEn = weekdayEn,
            hijriWeekdayAr = "",
            hijriMonthNumber = hMonth,
            hijriMonthEn = hijriMonthsEn[hMonth - 1],
            hijriMonthAr = hijriMonthsAr[hMonth - 1],
            hijriMonthBn = hijriMonthNamesBn[hMonth] ?: hijriMonthsEn[hMonth - 1],
            hijriYear = hYear,
            banglaDateFormatted = computeBanglaDateFormatted(date),
            holidays = holidays,
            isWhiteDayFasting = hDay in listOf(13, 14, 15)
        )
    }

    private fun computeBanglaDateFormatted(date: LocalDate): String {
        val banglaMonthsBn = listOf(
            "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
            "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
        )
        val banglaMonthsEn = listOf(
            "Boishakh", "Joishtho", "Asharh", "Shrabon", "Bhadro", "Ashwin",
            "Kartik", "Ogrohayon", "Poush", "Magh", "Falgun", "Choitro"
        )
        val dayOfYear = date.dayOfYear
        val bYear = if (date.monthValue < 4 || (date.monthValue == 4 && date.dayOfMonth < 14)) {
            date.year - 594
        } else {
            date.year - 593
        }
        val shifted = (dayOfYear - 104 + 365) % 365
        val mIdx = (shifted / 30).coerceIn(0, 11)
        val bDay = (shifted % 30) + 1
        return "$bDay ${banglaMonthsBn[mIdx]} $bYear বঙ্গাব্দ ($bDay ${banglaMonthsEn[mIdx]})"
    }
}
