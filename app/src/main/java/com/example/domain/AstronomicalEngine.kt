package com.example.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

data class BdDistrict(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val divisionBn: String,
    val latitude: Double,
    val longitude: Double
)

enum class CalculationMethod(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesOffset: Int = 0
) {
    ISLAMIC_FOUNDATION_BD(
        "ISLAMIC_FOUNDATION_BD",
        "ইসলামিক ফাউন্ডেশন বাংলাদেশ",
        "Islamic Foundation Bangladesh",
        18.0,
        18.0
    ),
    KARACHI(
        "KARACHI",
        "করাচি ইসলামিক ইউনিভার্সিটি",
        "University of Islamic Sciences, Karachi",
        18.0,
        18.0
    ),
    UMM_AL_QURA(
        "UMM_AL_QURA",
        "উম্মুল কুরা, মক্কা",
        "Umm al-Qura University, Makkah",
        18.5,
        0.0,
        90
    ),
    MWL(
        "MWL",
        "মুসলিম ওয়ার্ল্ড লীগ",
        "Muslim World League",
        18.0,
        17.0
    ),
    ISNA(
        "ISNA",
        "ইসলামিক সোসাইটি অব নর্থ আমেরিকা",
        "ISNA (North America)",
        15.0,
        15.0
    );

    companion object {
        fun fromId(id: String): CalculationMethod =
            entries.firstOrNull { it.id == id } ?: ISLAMIC_FOUNDATION_BD
    }
}

enum class Madhhab(val id: String, val titleBn: String, val titleEn: String, val shadowFactor: Int) {
    HANAFI("HANAFI", "হানাফি (২ গুণ ছায়া)", "Hanafi", 2),
    SHAFI("SHAFI", "শাফিঈ/মালেকি/হাম্বলি (১ গুণ ছায়া)", "Shafi'i / Standard", 1);

    companion object {
        fun fromId(id: String): Madhhab = entries.firstOrNull { it.id == id } ?: HANAFI
    }
}

enum class WaqtType(val nameBn: String, val nameEn: String, val tagBn: String, val tagEn: String) {
    FAJR("ফজর", "Fajr", "ভোর", "Dawn"),
    SUNRISE("সূর্যোদয়", "Sunrise", "নিষিদ্ধ সময়", "Sunrise"),
    DHUHR("যোহর", "Dhuhr", "দুপুর", "Noon"),
    ASR("আসর", "Asr", "বিকেল", "Afternoon"),
    MAGHRIB("মাগরিব", "Maghrib", "সন্ধ্যা", "Sunset"),
    ISHA("ইশা", "Isha", "রাত", "Night")
}

data class PrayerWindow(
    val type: WaqtType,
    val startMinutes: Int,
    val endMinutes: Int,
    val jamaatMinutes: Int,
    val startFormatted12h: String,
    val startTimeOnly: String,
    val amPm: String,
    val endFormatted12h: String,
    val jamaatFormatted12h: String,
    val isCurrent: Boolean,
    val isPast: Boolean
)

data class ProhibitedInterval(
    val titleBn: String,
    val titleEn: String,
    val rangeText: String
)

data class NawafilWindow(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val subtitleBn: String,
    val rangeText: String
)

data class DailyPrayerSchedule(
    val dateKey: String,
    val gregorianDateText: String,
    val hijriDateText: String,
    val hijriDateBn: String,
    val hijriDay: Int,
    val hijriMonthIndex: Int,
    val hijriMonthNameEn: String,
    val hijriMonthNameBn: String,
    val hijriYear: Int,
    val banglaDateText: String,
    val banglaDateBn: String,
    val currentTimeText: String,
    val currentAmPm: String,
    val sunriseText: String,
    val sunsetText: String,
    val sehriEndText: String,
    val imsakText: String,
    val iftarText: String,
    val currentWaqt: WaqtType,
    val nextWaqt: WaqtType,
    val nextWaqtStartText: String,
    val currentWaqtStartText: String,
    val currentWaqtEndText: String,
    val remainingHhMmSs: String,
    val elapsedFraction: Float,
    val elapsedPercent: Int,
    val fivePrayers: List<PrayerWindow>,
    val prohibitedIntervals: List<ProhibitedInterval>,
    val nawafilPrayers: List<NawafilWindow>,
    val qiblaBearingDegrees: Double,
    val distanceToKaabaKm: Int
)

object AstronomicalEngine {

    private const val KAABA_LAT = 21.422487
    private const val KAABA_LNG = 39.826206

    val hijriMonthsEn = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    val hijriMonthsBn = listOf(
        "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    val banglaMonthsEn = listOf(
        "Boishakh", "Jaishtha", "Asharh", "Srabon", "Bhadro", "Ashwin",
        "Kartik", "Ogrohayon", "Poush", "Magh", "Falgun", "Choitro"
    )

    val banglaMonthsBn = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )

    fun toBanglaDigits(input: String): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bnDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatDigits(input: String, useBanglaDigits: Boolean): String =
        if (useBanglaDigits) toBanglaDigits(input) else input

    fun calculateSchedule(
        district: BdDistrict,
        method: CalculationMethod,
        madhhab: Madhhab,
        hijriOffsetDays: Int = 0,
        nowMillis: Long = System.currentTimeMillis(),
        dayOffset: Int = 0
    ): DailyPrayerSchedule {
        val tz = TimeZone.getTimeZone("Asia/Dhaka")
        val cal = Calendar.getInstance(tz).apply {
            timeInMillis = nowMillis
            if (dayOffset != 0) {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
        }

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        val lat = district.latitude
        val lng = district.longitude
        val tzHours = 6.0 // Bangladesh Standard Time (UTC+6)

        // Solar position approximations
        val gamma = 2.0 * Math.PI / 365.0 * (dayOfYear - 1 + 0.5)
        val eqTime = 229.18 * (
            0.000075 +
                0.001868 * cos(gamma) -
                0.032077 * sin(gamma) -
                0.014615 * cos(2 * gamma) -
                0.040849 * sin(2 * gamma)
            )
        val decl = 0.006918 -
            0.399912 * cos(gamma) +
            0.070257 * sin(gamma) -
            0.006758 * cos(2 * gamma) +
            0.000907 * sin(2 * gamma) -
            0.002697 * cos(3 * gamma) +
            0.00148 * sin(3 * gamma)

        val solarNoonMin = (720.0 - 4.0 * lng - eqTime + tzHours * 60.0).roundToInt()

        fun hourAngleMinutes(zenithDeg: Double): Int {
            val latRad = Math.toRadians(lat)
            val zenRad = Math.toRadians(zenithDeg)
            val cosHa = (cos(zenRad) / (cos(latRad) * cos(decl))) - tan(latRad) * tan(decl)
            val clamped = cosHa.coerceIn(-0.999, 0.999)
            val haDeg = Math.toDegrees(acos(clamped))
            return (haDeg * 4.0).roundToInt()
        }

        val sunriseDiff = hourAngleMinutes(90.833)
        val fajrDiff = hourAngleMinutes(90.0 + method.fajrAngle)
        val ishaDiff = if (method.ishaMinutesOffset > 0) {
            sunriseDiff + method.ishaMinutesOffset
        } else {
            hourAngleMinutes(90.0 + method.ishaAngle)
        }

        // Asr altitude & hour angle
        val latRad = Math.toRadians(lat)
        val asrAltRad = atan(1.0 / (madhhab.shadowFactor + tan(abs(latRad - decl))))
        val asrZenithDeg = 90.0 - Math.toDegrees(asrAltRad)
        val asrDiff = hourAngleMinutes(asrZenithDeg)

        val fajrStart = (solarNoonMin - fajrDiff).coerceIn(240, 360)
        val sunriseMin = (solarNoonMin - sunriseDiff).coerceIn(330, 410)
        val dhuhrStart = (solarNoonMin + 3).coerceIn(700, 760)
        val asrStart = (solarNoonMin + asrDiff).coerceIn(900, 1040)
        val maghribStart = (solarNoonMin + sunriseDiff + 2).coerceIn(1020, 1150)
        val ishaStart = (solarNoonMin + ishaDiff).coerceIn(1100, 1240)

        val nowHour = cal.get(Calendar.HOUR_OF_DAY)
        val nowMinute = cal.get(Calendar.MINUTE)
        val nowSecond = cal.get(Calendar.SECOND)
        val nowTotalSeconds = nowHour * 3600 + nowMinute * 60 + nowSecond
        val nowTotalMinutes = nowHour * 60 + nowMinute

        data class WaqtBounds(
            val type: WaqtType,
            val startMin: Int,
            val endMin: Int,
            val jamaatMin: Int
        )

        val scheduleBounds = listOf(
            WaqtBounds(WaqtType.FAJR, fajrStart, sunriseMin - 1, fajrStart + 30),
            WaqtBounds(WaqtType.DHUHR, dhuhrStart, asrStart - 1, dhuhrStart + 65),
            WaqtBounds(WaqtType.ASR, asrStart, maghribStart - 2, asrStart + 30),
            WaqtBounds(WaqtType.MAGHRIB, maghribStart, ishaStart - 1, maghribStart + 8),
            WaqtBounds(WaqtType.ISHA, ishaStart, 24 * 60 + fajrStart - 1, ishaStart + 45)
        )

        val activeWaqt: WaqtType
        val nextWaqt: WaqtType
        val activeStartSec: Int
        val activeEndSec: Int
        val nextStartMin: Int

        when {
            nowTotalMinutes < fajrStart -> {
                activeWaqt = WaqtType.ISHA
                nextWaqt = WaqtType.FAJR
                activeStartSec = (ishaStart - 24 * 60) * 60
                activeEndSec = fajrStart * 60
                nextStartMin = fajrStart
            }
            nowTotalMinutes in fajrStart until sunriseMin -> {
                activeWaqt = WaqtType.FAJR
                nextWaqt = WaqtType.DHUHR
                activeStartSec = fajrStart * 60
                activeEndSec = sunriseMin * 60
                nextStartMin = dhuhrStart
            }
            nowTotalMinutes in sunriseMin until dhuhrStart -> {
                activeWaqt = WaqtType.DHUHR
                nextWaqt = WaqtType.DHUHR
                activeStartSec = sunriseMin * 60
                activeEndSec = dhuhrStart * 60
                nextStartMin = dhuhrStart
            }
            nowTotalMinutes in dhuhrStart until asrStart -> {
                activeWaqt = WaqtType.DHUHR
                nextWaqt = WaqtType.ASR
                activeStartSec = dhuhrStart * 60
                activeEndSec = asrStart * 60
                nextStartMin = asrStart
            }
            nowTotalMinutes in asrStart until maghribStart -> {
                activeWaqt = WaqtType.ASR
                nextWaqt = WaqtType.MAGHRIB
                activeStartSec = asrStart * 60
                activeEndSec = maghribStart * 60
                nextStartMin = maghribStart
            }
            nowTotalMinutes in maghribStart until ishaStart -> {
                activeWaqt = WaqtType.MAGHRIB
                nextWaqt = WaqtType.ISHA
                activeStartSec = maghribStart * 60
                activeEndSec = ishaStart * 60
                nextStartMin = ishaStart
            }
            else -> {
                activeWaqt = WaqtType.ISHA
                nextWaqt = WaqtType.FAJR
                activeStartSec = ishaStart * 60
                activeEndSec = (24 * 60 + fajrStart) * 60
                nextStartMin = fajrStart
            }
        }

        val remainingSec = (activeEndSec - nowTotalSeconds).coerceAtLeast(0)
        val totalWindowSec = (activeEndSec - activeStartSec).coerceAtLeast(1)
        val elapsedSec = (nowTotalSeconds - activeStartSec).coerceIn(0, totalWindowSec)
        val elapsedFraction = (elapsedSec.toFloat() / totalWindowSec.toFloat()).coerceIn(0.05f, 0.98f)
        val elapsedPercent = (elapsedFraction * 100f).roundToInt()

        val remH = remainingSec / 3600
        val remM = (remainingSec % 3600) / 60
        val remS = remainingSec % 60
        val remainingHhMmSs = String.format(Locale.US, "%02d:%02d:%02d", remH, remM, remS)

        val fivePrayers = scheduleBounds.map { b ->
            val normEnd = b.endMin % (24 * 60)
            val isCurrent = when (b.type) {
                WaqtType.ISHA -> nowTotalMinutes >= ishaStart || nowTotalMinutes < fajrStart
                WaqtType.FAJR -> nowTotalMinutes in fajrStart until sunriseMin
                WaqtType.DHUHR -> nowTotalMinutes in dhuhrStart until asrStart
                WaqtType.ASR -> nowTotalMinutes in asrStart until maghribStart
                WaqtType.MAGHRIB -> nowTotalMinutes in maghribStart until ishaStart
                else -> false
            }
            val isPast = !isCurrent && nowTotalMinutes > b.startMin && b.type != WaqtType.ISHA
            val (tOnly, amPm) = splitTime12h(b.startMin)
            PrayerWindow(
                type = b.type,
                startMinutes = b.startMin,
                endMinutes = normEnd,
                jamaatMinutes = b.jamaatMin % (24 * 60),
                startFormatted12h = formatMinutes12h(b.startMin),
                startTimeOnly = tOnly,
                amPm = amPm,
                endFormatted12h = formatMinutes12h(normEnd),
                jamaatFormatted12h = formatMinutes12h(b.jamaatMin % (24 * 60)),
                isCurrent = isCurrent,
                isPast = isPast
            )
        }

        val prohibited = listOf(
            ProhibitedInterval(
                titleBn = "সূর্যোদয় কাল",
                titleEn = "SUNRISE",
                rangeText = "${formatMinutesTimeOnly(sunriseMin)} - ${formatMinutesTimeOnly(sunriseMin + 15)}"
            ),
            ProhibitedInterval(
                titleBn = "যাওয়াল (দ্বিপ্রহর)",
                titleEn = "ZAWAL (NOON)",
                rangeText = "${formatMinutesTimeOnly(solarNoonMin - 10)} - ${formatMinutesTimeOnly(dhuhrStart - 1)}"
            ),
            ProhibitedInterval(
                titleBn = "সূর্যাস্ত কাল",
                titleEn = "SUNSET",
                rangeText = "${formatMinutesTimeOnly(maghribStart - 17)} - ${formatMinutesTimeOnly(maghribStart - 2)}"
            )
        )

        val nawafil = listOf(
            NawafilWindow(
                id = "tahajjud",
                titleBn = "তাহাজ্জুদ (রাতের শেষ তৃতীয়াংশ)",
                titleEn = "Tahajjud (Last Third of Night)",
                subtitleBn = "রাতের সর্বোত্তম নফল ইবাদত",
                rangeText = "01:30 AM - ${formatMinutes12h(fajrStart - 15)}"
            ),
            NawafilWindow(
                id = "duha",
                titleBn = "ইশরাক ও চাশত (দুহা)",
                titleEn = "Ishraq & Duha (Chasht)",
                subtitleBn = "সূর্য ওঠার ২০ মিনিট পর থেকে দ্বিপ্রহরের আগ পর্যন্ত",
                rangeText = "${formatMinutes12h(sunriseMin + 20)} - ${formatMinutes12h(solarNoonMin - 15)}"
            ),
            NawafilWindow(
                id = "awwabin",
                titleBn = "আওয়াবিন নামাজ",
                titleEn = "Awwabin",
                subtitleBn = "মাগরিবের ফরজ ও সুন্নতের পর ৬ রাকাত",
                rangeText = "${formatMinutes12h(maghribStart + 15)} - ${formatMinutes12h(ishaStart - 10)}"
            )
        )

        val hijri = calculateHijriDate(year, month, day, hijriOffsetDays)
        val bangla = calculateBanglaDate(year, month, day)

        val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
        val gregFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.US).apply { timeZone = tz }
        val gregText = gregFormat.format(Date(cal.timeInMillis))

        val (curTimeOnly, curAmPm) = splitTime12h(nowTotalMinutes)

        return DailyPrayerSchedule(
            dateKey = dateKey,
            gregorianDateText = gregText,
            hijriDateText = "${hijri.day} ${hijriMonthsEn[hijri.month - 1]}, ${hijri.year} AH",
            hijriDateBn = "${toBanglaDigits(hijri.day.toString())} ${hijriMonthsBn[hijri.month - 1]}, ${toBanglaDigits(hijri.year.toString())} হিজরি",
            hijriDay = hijri.day,
            hijriMonthIndex = hijri.month,
            hijriMonthNameEn = hijriMonthsEn[hijri.month - 1],
            hijriMonthNameBn = hijriMonthsBn[hijri.month - 1],
            hijriYear = hijri.year,
            banglaDateText = "${bangla.day} ${banglaMonthsEn[bangla.month - 1]}, ${bangla.year} BS",
            banglaDateBn = "${toBanglaDigits(bangla.day.toString())} ${banglaMonthsBn[bangla.month - 1]}, ${toBanglaDigits(bangla.year.toString())} বঙ্গাব্দ",
            currentTimeText = curTimeOnly,
            currentAmPm = curAmPm,
            sunriseText = formatMinutes12h(sunriseMin),
            sunsetText = formatMinutes12h(maghribStart - 1),
            sehriEndText = formatMinutes12h(fajrStart - 5),
            imsakText = formatMinutes12h(fajrStart - 10),
            iftarText = formatMinutes12h(maghribStart),
            currentWaqt = activeWaqt,
            nextWaqt = nextWaqt,
            nextWaqtStartText = formatMinutes12h(nextStartMin),
            currentWaqtStartText = formatMinutes12h((activeStartSec / 60 + 24 * 60) % (24 * 60)),
            currentWaqtEndText = formatMinutes12h((activeEndSec / 60) % (24 * 60)),
            remainingHhMmSs = remainingHhMmSs,
            elapsedFraction = elapsedFraction,
            elapsedPercent = elapsedPercent,
            fivePrayers = fivePrayers,
            prohibitedIntervals = prohibited,
            nawafilPrayers = nawafil,
            qiblaBearingDegrees = calculateQiblaBearing(lat, lng),
            distanceToKaabaKm = calculateDistanceToKaabaKm(lat, lng)
        )
    }

    fun calculateQiblaBearing(lat: Double, lng: Double): Double {
        val phiK = Math.toRadians(KAABA_LAT)
        val lambdaK = Math.toRadians(KAABA_LNG)
        val phi = Math.toRadians(lat)
        val lambda = Math.toRadians(lng)
        val deltaLambda = lambdaK - lambda

        val y = sin(deltaLambda)
        val x = cos(phi) * tan(phiK) - sin(phi) * cos(deltaLambda)
        val bearing = (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
        return (bearing * 10.0).roundToInt() / 10.0
    }

    fun calculateDistanceToKaabaKm(lat: Double, lng: Double): Int {
        val r = 6371.0
        val dLat = Math.toRadians(KAABA_LAT - lat)
        val dLng = Math.toRadians(KAABA_LNG - lng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat)) * cos(Math.toRadians(KAABA_LAT)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return (r * c).roundToInt()
    }

    data class SimpleDateTriplet(val day: Int, val month: Int, val year: Int)

    // Kuwaiti / Tabular Hijri conversion with moon-sighting offset
    fun calculateHijriDate(gregYear: Int, gregMonth: Int, gregDay: Int, offsetDays: Int = 0): SimpleDateTriplet {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Dhaka")).apply {
            set(gregYear, gregMonth - 1, gregDay, 12, 0, 0)
            add(Calendar.DAY_OF_YEAR, offsetDays)
        }
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)

        val jd = if (m > 2) {
            floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + d - 1524.5
        } else {
            val yy = y - 1
            val mm = m + 12
            floor(365.25 * (yy + 4716)) + floor(30.6001 * (mm + 1)) + d - 1524.5
        }.toInt()

        val l = jd - 1948440 + 10632
        val n = (l - 1) / 10631
        val l2 = l - 10631 * n + 354
        val j = ((10985 - l2) / 5316) * ((50 * l2) / 17719) + (l2 / 5670) * ((43 * l2) / 15238)
        val l3 = l2 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val hMonth = ((24 * l3) / 709).coerceIn(1, 12)
        val hDay = (l3 - (709 * hMonth) / 24).coerceIn(1, 30)
        val hYear = 30 * n + j - 30
        return SimpleDateTriplet(hDay, hMonth, hYear)
    }

    // Revised Bangla Calendar (Bongabdo) calculation
    fun calculateBanglaDate(gregYear: Int, gregMonth: Int, gregDay: Int): SimpleDateTriplet {
        val isLeap = (gregYear % 4 == 0 && gregYear % 100 != 0) || (gregYear % 400 == 0)
        val banglaYear = if (gregMonth < 4 || (gregMonth == 4 && gregDay < 14)) {
            gregYear - 594
        } else {
            gregYear - 593
        }

        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Dhaka")).apply {
            set(gregYear, gregMonth - 1, gregDay, 12, 0, 0)
        }
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val pohelaBoishakhDay = if (isLeap) 105 else 104 // April 14

        val daysSinceBoishakh = if (dayOfYear >= pohelaBoishakhDay) {
            dayOfYear - pohelaBoishakhDay
        } else {
            val prevYearDays = if (((gregYear - 1) % 4 == 0 && (gregYear - 1) % 100 != 0) || ((gregYear - 1) % 400 == 0)) 366 else 365
            val prevPohela = if (prevYearDays == 366) 105 else 104
            (prevYearDays - prevPohela) + dayOfYear
        }

        // Bangla Academy revised month lengths: first 5 months = 31 days, next 7 months = 30 days (Falgun = 31 in leap year)
        val monthLengths = intArrayOf(31, 31, 31, 31, 31, 30, 30, 30, 30, 30, if (isLeap) 31 else 30, 30)
        var rem = daysSinceBoishakh
        var bMonth = 1
        for (len in monthLengths) {
            if (rem < len) break
            rem -= len
            bMonth++
        }
        val bDay = (rem + 1).coerceIn(1, 31)
        return SimpleDateTriplet(bDay, bMonth.coerceIn(1, 12), banglaYear)
    }

    private fun splitTime12h(totalMinutes: Int): Pair<String, String> {
        val norm = ((totalMinutes % (24 * 60)) + (24 * 60)) % (24 * 60)
        val h24 = norm / 60
        val m = norm % 60
        val amPm = if (h24 >= 12) "PM" else "AM"
        val h12 = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        return String.format(Locale.US, "%02d:%02d", h12, m) to amPm
    }

    private fun formatMinutesTimeOnly(totalMinutes: Int): String = splitTime12h(totalMinutes).first

    fun formatMinutes12h(totalMinutes: Int): String {
        val (t, ap) = splitTime12h(totalMinutes)
        return "$t $ap"
    }
}
