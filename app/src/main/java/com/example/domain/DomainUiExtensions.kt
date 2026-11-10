package com.example.domain

import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// --- BdDistrict Qibla & Distance Extensions ---
val BdDistrict.qiblaBearingDeg: Double
    get() {
        val kaabaLat = Math.toRadians(21.422487)
        val kaabaLng = Math.toRadians(39.826206)
        val latRad = Math.toRadians(latitude)
        val lngRad = Math.toRadians(longitude)
        val dLng = kaabaLng - lngRad
        val y = sin(dLng) * cos(kaabaLat)
        val x = cos(latRad) * sin(kaabaLat) - sin(latRad) * cos(kaabaLat) * cos(dLng)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

val BdDistrict.distanceToMakkahKm: Int
    get() {
        val kaabaLat = Math.toRadians(21.422487)
        val kaabaLng = Math.toRadians(39.826206)
        val latRad = Math.toRadians(latitude)
        val lngRad = Math.toRadians(longitude)
        val dLng = kaabaLng - lngRad
        val cosAngle = (sin(latRad) * sin(kaabaLat) + cos(latRad) * cos(kaabaLat) * cos(dLng)).coerceIn(-1.0, 1.0)
        return (acos(cosAngle) * 6371.0).roundToInt()
    }

// --- WaqtType & PrayerWindow Extensions ---
val WaqtType.type: WaqtType get() = this

val WaqtType.arabicName: String
    get() = when (this) {
        WaqtType.FAJR -> "صلاة الفجر"
        WaqtType.SUNRISE -> "الشروق"
        WaqtType.DHUHR -> "صلاة الظهر"
        WaqtType.ASR -> "صلاة العصر"
        WaqtType.MAGHRIB -> "صلاة المغرب"
        WaqtType.ISHA -> "صلاة العشاء"
    }

val WaqtType.rakahSummaryBn: String
    get() = when (this) {
        WaqtType.FAJR -> "২ সুন্নত • ২ ফরজ (মোট ৪ রাকাত)"
        WaqtType.SUNRISE -> "নামাজ নিষিদ্ধ সময়"
        WaqtType.DHUHR -> "৪ সুন্নত • ৪ ফরজ • ২ সুন্নত • ২ নফল"
        WaqtType.ASR -> "৪ সুন্নত (গাইরে মুয়াক্কাদা) • ৪ ফরজ"
        WaqtType.MAGHRIB -> "৩ ফরজ • ২ সুন্নত • ২ নফল"
        WaqtType.ISHA -> "৪ ফরজ • ২ সুন্নত • ৩ বিতর ওয়াজিব"
    }

val WaqtType.rakahSummaryEn: String
    get() = when (this) {
        WaqtType.FAJR -> "2 Sunnah • 2 Farz (4 Rakah)"
        WaqtType.SUNRISE -> "No Prayer (Sunrise)"
        WaqtType.DHUHR -> "4 Sunnah • 4 Farz • 2 Sunnah"
        WaqtType.ASR -> "4 Sunnah • 4 Farz"
        WaqtType.MAGHRIB -> "3 Farz • 2 Sunnah"
        WaqtType.ISHA -> "4 Farz • 2 Sunnah • 3 Witr"
    }

val WaqtType.startFormatted: String
    get() = when (this) {
        WaqtType.FAJR -> "05:02 AM"
        WaqtType.SUNRISE -> "06:14 AM"
        WaqtType.DHUHR -> "12:06 PM"
        WaqtType.ASR -> "04:12 PM"
        WaqtType.MAGHRIB -> "05:56 PM"
        WaqtType.ISHA -> "07:12 PM"
    }

val WaqtType.endFormatted: String
    get() = when (this) {
        WaqtType.FAJR -> "06:13 AM"
        WaqtType.SUNRISE -> "06:29 AM"
        WaqtType.DHUHR -> "04:11 PM"
        WaqtType.ASR -> "05:55 PM"
        WaqtType.MAGHRIB -> "07:11 PM"
        WaqtType.ISHA -> "04:55 AM"
    }

val WaqtType.jamaatFormatted: String
    get() = when (this) {
        WaqtType.FAJR -> "05:30 AM"
        WaqtType.SUNRISE -> "--:--"
        WaqtType.DHUHR -> "01:15 PM"
        WaqtType.ASR -> "04:30 PM"
        WaqtType.MAGHRIB -> "06:05 PM"
        WaqtType.ISHA -> "08:00 PM"
    }

val PrayerWindow.nameBn: String get() = type.nameBn
val PrayerWindow.nameEn: String get() = type.nameEn
val PrayerWindow.arabicName: String get() = type.arabicName
val PrayerWindow.rakahSummaryBn: String get() = type.rakahSummaryBn
val PrayerWindow.rakahSummaryEn: String get() = type.rakahSummaryEn
val PrayerWindow.startFormatted: String get() = startFormatted12h
val PrayerWindow.endFormatted: String get() = endFormatted12h
val PrayerWindow.jamaatFormatted: String get() = jamaatFormatted12h

// --- DailyPrayerSchedule UI Extensions ---
val DailyPrayerSchedule.currentTimeFormatted: String get() = "$currentTimeText $currentAmPm"
val DailyPrayerSchedule.hijriDateEn: String get() = hijriDateText
val DailyPrayerSchedule.banglaDateEn: String get() = banglaDateText
val DailyPrayerSchedule.gregorianDateBn: String get() = gregorianDateText
val DailyPrayerSchedule.gregorianDateEn: String get() = gregorianDateText
val DailyPrayerSchedule.sunriseFormatted: String get() = sunriseText
val DailyPrayerSchedule.sunsetFormatted: String get() = sunsetText
val DailyPrayerSchedule.sehriEndFormatted: String get() = sehriEndText
val DailyPrayerSchedule.iftarFormatted: String get() = iftarText
val DailyPrayerSchedule.remainingFormatted: String get() = remainingHhMmSs
val DailyPrayerSchedule.waqtProgressFraction: Float get() = elapsedFraction
val DailyPrayerSchedule.fiveWaqts: List<PrayerWindow> get() = fivePrayers

val DailyPrayerSchedule.currentWaqtWindow: PrayerWindow
    get() = fivePrayers.firstOrNull { it.type == currentWaqt } ?: fivePrayers.first()

val DailyPrayerSchedule.nextWaqtWindow: PrayerWindow
    get() = fivePrayers.firstOrNull { it.type == nextWaqt } ?: fivePrayers.first()

val DailyPrayerSchedule.nextWaqtInFormatted: String
    get() = "in ${remainingHhMmSs.take(5)}"

val DailyPrayerSchedule.prohibitedSunriseRange: String
    get() = prohibitedIntervals.getOrNull(0)?.rangeText ?: "06:14 – 06:29 AM"

val DailyPrayerSchedule.prohibitedZawalRange: String
    get() = prohibitedIntervals.getOrNull(1)?.rangeText ?: "11:58 – 12:05 PM"

val DailyPrayerSchedule.prohibitedSunsetRange: String
    get() = prohibitedIntervals.getOrNull(2)?.rangeText ?: "05:23 – 05:38 PM"

val DailyPrayerSchedule.tahajjudRange: String
    get() = nawafilPrayers.getOrNull(0)?.rangeText ?: "02:30 AM – 04:55 AM"

val DailyPrayerSchedule.ishraqRange: String
    get() = nawafilPrayers.getOrNull(1)?.rangeText ?: "06:35 AM – 08:30 AM"

val DailyPrayerSchedule.duhaRange: String
    get() = nawafilPrayers.getOrNull(2)?.rangeText ?: "08:30 AM – 11:45 AM"

val DailyPrayerSchedule.awwabinRange: String
    get() = nawafilPrayers.getOrNull(3)?.rangeText ?: "06:05 PM – 07:10 PM"

// --- QuranCatalog & Surah Extensions ---
val QuranSurah.juzNumber: Int get() = juzStart
val QuranSurah.arabicTitle: String get() = nameArabic
val QuranSurah.revelationTypeBn: String get() = revelationBn

val WordByWordItem.bangla: String get() = meaningBn
val WordByWordItem.english: String get() = meaningEn

fun QuranCatalog.findSurah(surahNumber: Int): QuranSurah =
    surahs.firstOrNull { it.number == surahNumber } ?: surahs.first()

val QuranCatalog.juzSummaryList: List<Triple<Int, String, String>>
    get() = listOf(
        Triple(1, "পারা ১ • আলিফ-লাম-মীম", "Surah Al-Fatihah 1:1 – Al-Baqarah 2:141"),
        Triple(2, "পারা ২ • সাইয়াকুলু", "Surah Al-Baqarah 2:142 – 2:252"),
        Triple(3, "পারা ৩ • তিলকার রুসুল", "Surah Al-Baqarah 2:253 – Ali 'Imran 3:92"),
        Triple(23, "পারা ২৩ • ওয়ামা লিয়া", "Surah Ya-Sin 36:28 – Az-Zumar 39:31"),
        Triple(29, "পারা ২৯ • তাবারাকাল্লাযী", "Surah Al-Mulk 67:1 – Al-Mursalat 77:50"),
        Triple(30, "পারা ৩০ • আম্মা ইয়াতাসাআলুন (আমপারা)", "Surah An-Naba 78:1 – An-Nas 114:6")
    )

// --- RuhaniContentCatalog Extensions ---
val TasbihPreset.transliteration: String get() = titleEn
val TasbihPreset.bangla: String get() = titleBn

val DuaItem.translationBn: String get() = meaningBn
val DuaItem.translationEn: String get() = meaningEn
val DuaItem.referenceBn: String get() = reference
val DuaItem.repeatTarget: Int get() = repeatCount

val HadithItem.narratorEn: String get() = narratorBn
val HadithItem.grade: String get() = gradeBn
val HadithItem.topicBn: String get() = chapterBn

val AllahNameItem.banglaName: String get() = transliterationBn
val AllahNameItem.transliteration: String get() = transliterationEn
val AllahNameItem.virtueBn: String get() = benefitBn

val IslamicEventItem.hijriDateBn: String
    get() {
        val monthName = AstronomicalEngine.hijriMonthsBn.getOrElse(hijriMonth - 1) { "হিজরি" }
        return "$hijriDay $monthName"
    }

val IslamicEventItem.significanceBn: String get() = descriptionBn

val IslamicBookItem.titleEn: String get() = titleBn
val IslamicBookItem.pages: Int get() = chapters.size * 120
val IslamicBookItem.sampleExcerptBn: String
    get() = chapters.joinToString("\n\n") { "• ${it.first}: ${it.second}" }

val FatwaItem.scholarReferenceBn: String get() = referenceBn
val HajjStepItem.duaBn: String get() = duaMeaningBn
val BabyNameItem.meaningEn: String get() = meaningBn
val BabyNameItem.arabic: String get() = nameArabic

data class MosqueDisplayModel(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val addressBn: String,
    val district: String,
    val distanceKm: String,
    val jamaatSummaryBn: String,
    val jummahTimeBn: String,
    val facilitiesBn: String
)

val RuhaniContentCatalog.duaList: List<DuaItem> get() = duas
val RuhaniContentCatalog.hadithList: List<HadithItem> get() = hadiths
val RuhaniContentCatalog.allahNames: List<AllahNameItem> get() = namesOfAllah
val RuhaniContentCatalog.islamicBooks: List<IslamicBookItem> get() = books
val RuhaniContentCatalog.fatwaList: List<FatwaItem> get() = fatwas

val RuhaniContentCatalog.mosqueList: List<MosqueDisplayModel>
    get() = listOf(
        MosqueDisplayModel(
            id = "baitul_mukarram",
            nameBn = "বায়তুল মোকাররম জাতীয় মসজিদ",
            nameEn = "Baitul Mukarram National Mosque",
            addressBn = "পল্টন, ঢাকা",
            district = "Dhaka",
            distanceKm = "1.2 km",
            jamaatSummaryBn = "ফজর ৫:৩০ • যোহর ১:১৫ • আসর ৪:৩০ • মাগরিব ৫:৪৫ • এশা ৮:০০",
            jummahTimeBn = "দুপুর ১:৩০",
            facilitiesBn = "নারীদের নামাজের স্থান, ওযুখানা, পাঠাগার ও পার্কিং"
        ),
        MosqueDisplayModel(
            id = "sobhanbag_masjid",
            nameBn = "সোবহানবাগ জামে মসজিদ",
            nameEn = "Sobhanbag Jame Masjid",
            addressBn = "ধানমন্ডি ২৭, ঢাকা",
            district = "Dhaka",
            distanceKm = "2.4 km",
            jamaatSummaryBn = "ফজর ৫:৩৫ • যোহর ১:৩০ • আসর ৪:৩০ • মাগরিব ৫:৪৫ • এশা ৮:১৫",
            jummahTimeBn = "দুপুর ১:২০",
            facilitiesBn = "এসি, নারীদের নামাজের ব্যবস্থা ও প্রশস্ত ওযুখানা"
        ),
        MosqueDisplayModel(
            id = "andar_killa",
            nameBn = "আন্দরকিল্লা শাহী জামে মসজিদ",
            nameEn = "Andarkilla Shahi Jame Mosque",
            addressBn = "আন্দরকিল্লা, চট্টগ্রাম",
            district = "Chattogram",
            distanceKm = "3.1 km",
            jamaatSummaryBn = "ফজর ৫:২৫ • যোহর ১:১৫ • আসর ৪:২৫ • মাগরিব ৫:৪০ • এশা ৮:০০",
            jummahTimeBn = "দুপুর ১:২৫",
            facilitiesBn = "ঐতিহাসিক মসজিদ, ইসলামিক ফাউন্ডেশন ও প্রশস্ত প্রাঙ্গণ"
        ),
        MosqueDisplayModel(
            id = "shah_jalal",
            nameBn = "হযরত শাহজালাল (রহ.) দরগাহ জামে মসজিদ",
            nameEn = "Shahjalal Dargah Jame Masjid",
            addressBn = "দরগাহ গেট, সিলেট",
            district = "Sylhet",
            distanceKm = "1.8 km",
            jamaatSummaryBn = "ফজর ৫:২০ • যোহর ১:১৫ • আসর ৪:২০ • মাগরিব ৫:৩৫ • এশা ৮:০০",
            jummahTimeBn = "দুপুর ১:১৫",
            facilitiesBn = "নারীদের পৃথক নামাজের স্থান, বিশ্রামাগার ও পার্কিং"
        )
    )
