package com.example.domain

data class DuaItem(
    val id: String,
    val categoryBn: String,
    val categoryEn: String,
    val titleBn: String,
    val titleEn: String,
    val arabic: String,
    val transliterationBn: String,
    val meaningBn: String,
    val meaningEn: String,
    val reference: String,
    val repeatCount: Int = 1,
    val occasionBn: String = "দৈনন্দিন আমল",
    val occasionEn: String = "Daily Supplication"
)

data class TasbihPreset(
    val id: String,
    val arabic: String,
    val titleEn: String,
    val titleBn: String,
    val meaningEn: String,
    val meaningBn: String,
    val defaultTarget: Int
)

data class HadithItem(
    val id: String,
    val bookBn: String,
    val bookEn: String,
    val hadithNumber: String,
    val chapterBn: String,
    val arabic: String,
    val translationBn: String,
    val translationEn: String,
    val narratorBn: String,
    val gradeBn: String = "সহীহ (Sahih)"
)

data class AllahNameItem(
    val number: Int,
    val arabic: String,
    val transliterationEn: String,
    val transliterationBn: String,
    val meaningBn: String,
    val meaningEn: String,
    val benefitBn: String
)

data class FatwaItem(
    val id: String,
    val categoryBn: String,
    val questionBn: String,
    val answerBn: String,
    val referenceBn: String
)

data class IslamicBookItem(
    val id: String,
    val titleBn: String,
    val authorBn: String,
    val categoryBn: String,
    val languageBn: String,
    val summaryBn: String,
    val chapters: List<Pair<String, String>>
)

data class HajjStepItem(
    val stepNumber: Int,
    val titleBn: String,
    val dateHijriBn: String,
    val locationBn: String,
    val descriptionBn: String,
    val duaArabic: String,
    val duaMeaningBn: String
)

data class BabyNameItem(
    val nameBn: String,
    val nameEn: String,
    val nameArabic: String,
    val genderBn: String, // "ছেলে" or "মেয়ে"
    val meaningBn: String,
    val origin: String = "আরবি"
)

data class IslamicEventItem(
    val hijriMonth: Int,
    val hijriDay: Int,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val isFastingRecommended: Boolean = false
)

data class NearbyMosqueItem(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val areaBn: String,
    val distanceKm: Double,
    val nextJamaatLabel: String,
    val hasWomenPrayerSpace: Boolean,
    val hasParking: Boolean
)

object RuhaniContentCatalog {

    // All 64 Districts of Bangladesh across 8 Divisions
    val bdDistricts: List<BdDistrict> = listOf(
        // Dhaka Division (13)
        BdDistrict("Dhaka", "Dhaka", "ঢাকা", "ঢাকা বিভাগ", 23.8103, 90.4125),
        BdDistrict("Gazipur", "Gazipur", "গাজীপুর", "ঢাকা বিভাগ", 24.0023, 90.4264),
        BdDistrict("Narayanganj", "Narayanganj", "নারায়ণগঞ্জ", "ঢাকা বিভাগ", 23.6238, 90.5000),
        BdDistrict("Tangail", "Tangail", "টাঙ্গাইল", "ঢাকা বিভাগ", 24.2513, 89.9167),
        BdDistrict("Narsingdi", "Narsingdi", "নরসিংদী", "ঢাকা বিভাগ", 23.9322, 90.7154),
        BdDistrict("Manikganj", "Manikganj", "মানিকগঞ্জ", "ঢাকা বিভাগ", 23.8644, 90.0047),
        BdDistrict("Munshiganj", "Munshiganj", "মুন্সীগঞ্জ", "ঢাকা বিভাগ", 23.5422, 90.5305),
        BdDistrict("Kishoreganj", "Kishoreganj", "কিশোরগঞ্জ", "ঢাকা বিভাগ", 24.4449, 90.7766),
        BdDistrict("Faridpur", "Faridpur", "ফরিদপুর", "ঢাকা বিভাগ", 23.6070, 89.8429),
        BdDistrict("Gopalganj", "Gopalganj", "গোপালগঞ্জ", "ঢাকা বিভাগ", 23.0051, 89.8266),
        BdDistrict("Madaripur", "Madaripur", "মাদারীপুর", "ঢাকা বিভাগ", 23.1641, 90.1897),
        BdDistrict("Rajbari", "Rajbari", "রাজবাড়ী", "ঢাকা বিভাগ", 23.7574, 89.6445),
        BdDistrict("Shariatpur", "Shariatpur", "শরীয়তপুর", "ঢাকা বিভাগ", 23.2423, 90.4348),
        // Chattogram Division (11)
        BdDistrict("Chattogram", "Chattogram", "চট্টগ্রাম", "চট্টগ্রাম বিভাগ", 22.3569, 91.7832),
        BdDistrict("CoxsBazar", "Cox's Bazar", "কক্সবাজার", "চট্টগ্রাম বিভাগ", 21.4272, 92.0058),
        BdDistrict("Cumilla", "Cumilla", "কুমিল্লা", "চট্টগ্রাম বিভাগ", 23.4607, 91.1809),
        BdDistrict("Brahmanbaria", "Brahmanbaria", "ব্রাহ্মণবাড়িয়া", "চট্টগ্রাম বিভাগ", 23.9571, 91.1119),
        BdDistrict("Chandpur", "Chandpur", "চাঁদপুর", "চট্টগ্রাম বিভাগ", 23.2332, 90.6712),
        BdDistrict("Noakhali", "Noakhali", "নোয়াখালী", "চট্টগ্রাম বিভাগ", 22.8696, 91.0994),
        BdDistrict("Feni", "Feni", "ফেনী", "চট্টগ্রাম বিভাগ", 23.0159, 91.3976),
        BdDistrict("Lakshmipur", "Lakshmipur", "লক্ষ্মীপুর", "চট্টগ্রাম বিভাগ", 22.9425, 90.8412),
        BdDistrict("Rangamati", "Rangamati", "রাঙ্গামাটি", "চট্টগ্রাম বিভাগ", 22.6533, 92.1789),
        BdDistrict("Bandarban", "Bandarban", "বান্দরবান", "চট্টগ্রাম বিভাগ", 22.1953, 92.2184),
        BdDistrict("Khagrachhari", "Khagrachhari", "খাগড়াছড়ি", "চট্টগ্রাম বিভাগ", 23.1193, 91.9847),
        // Sylhet Division (4)
        BdDistrict("Sylhet", "Sylhet", "সিলেট", "সিলেট বিভাগ", 24.8949, 91.8687),
        BdDistrict("Moulvibazar", "Moulvibazar", "মৌলভীবাজার", "সিলেট বিভাগ", 24.4829, 91.7774),
        BdDistrict("Habiganj", "Habiganj", "হবিগঞ্জ", "সিলেট বিভাগ", 24.3749, 91.4155),
        BdDistrict("Sunamganj", "Sunamganj", "সুনামগঞ্জ", "সিলেট বিভাগ", 25.0658, 91.3950),
        // Rajshahi Division (8)
        BdDistrict("Rajshahi", "Rajshahi", "রাজশাহী", "রাজশাহী বিভাগ", 24.3745, 88.6042),
        BdDistrict("Bogura", "Bogura", "বগুড়া", "রাজশাহী বিভাগ", 24.8465, 89.3773),
        BdDistrict("Pabna", "Pabna", "পাবনা", "রাজশাহী বিভাগ", 24.0064, 89.2372),
        BdDistrict("Sirajganj", "Sirajganj", "সিরাজগঞ্জ", "রাজশাহী বিভাগ", 24.4534, 89.7007),
        BdDistrict("Natore", "Natore", "নাটোর", "রাজশাহী বিভাগ", 24.4206, 89.0006),
        BdDistrict("Naogaon", "Naogaon", "নওগাঁ", "রাজশাহী বিভাগ", 24.7936, 88.9318),
        BdDistrict("Chapainawabganj", "Chapainawabganj", "চাঁপাইনবাবগঞ্জ", "রাজশাহী বিভাগ", 24.5965, 88.2775),
        BdDistrict("Joypurhat", "Joypurhat", "জয়পুরহাট", "রাজশাহী বিভাগ", 25.0968, 89.0227),
        // Khulna Division (10)
        BdDistrict("Khulna", "Khulna", "খুলনা", "খুলনা বিভাগ", 22.8456, 89.5403),
        BdDistrict("Jashore", "Jashore", "যশোর", "খুলনা বিভাগ", 23.1664, 89.2081),
        BdDistrict("Kushtia", "Kushtia", "কুষ্টিয়া", "খুলনা বিভাগ", 23.9013, 89.1204),
        BdDistrict("Satkhira", "Satkhira", "সাতক্ষীরা", "খুলনা বিভাগ", 22.7185, 89.0705),
        BdDistrict("Bagerhat", "Bagerhat", "বাগেরহাট", "খুলনা বিভাগ", 22.6516, 89.7859),
        BdDistrict("Jhenaidah", "Jhenaidah", "ঝিনাইদহ", "খুলনা বিভাগ", 23.5450, 89.1726),
        BdDistrict("Magura", "Magura", "মাগুরা", "খুলনা বিভাগ", 23.4873, 89.4199),
        BdDistrict("Narail", "Narail", "নড়াইল", "খুলনা বিভাগ", 23.1725, 89.5127),
        BdDistrict("Chuadanga", "Chuadanga", "চুয়াডাঙ্গা", "খুলনা বিভাগ", 23.6402, 88.8418),
        BdDistrict("Meherpur", "Meherpur", "মেহেরপুর", "খুলনা বিভাগ", 23.7622, 88.6318),
        // Barishal Division (6)
        BdDistrict("Barishal", "Barishal", "বরিশাল", "বরিশাল বিভাগ", 22.7010, 90.3535),
        BdDistrict("Patuakhali", "Patuakhali", "পটুয়াখালী", "বরিশাল বিভাগ", 22.3596, 90.3298),
        BdDistrict("Bhola", "Bhola", "ভোলা", "বরিশাল বিভাগ", 22.6859, 90.6482),
        BdDistrict("Pirojpur", "Pirojpur", "পিরোজপুর", "বরিশাল বিভাগ", 22.5841, 89.9720),
        BdDistrict("Barguna", "Barguna", "বরগুনা", "বরিশাল বিভাগ", 22.1592, 90.1255),
        BdDistrict("Jhalokati", "Jhalokati", "ঝালকাঠি", "বরিশাল বিভাগ", 22.6406, 90.1987),
        // Rangpur Division (8)
        BdDistrict("Rangpur", "Rangpur", "রংপুর", "রংপুর বিভাগ", 25.7439, 89.2752),
        BdDistrict("Dinajpur", "Dinajpur", "দিনাজপুর", "রংপুর বিভাগ", 25.6217, 88.6354),
        BdDistrict("Kurigram", "Kurigram", "কুড়িগ্রাম", "রংপুর বিভাগ", 25.8054, 89.6362),
        BdDistrict("Gaibandha", "Gaibandha", "গাইবান্ধা", "রংপুর বিভাগ", 25.3287, 89.5281),
        BdDistrict("Nilphamari", "Nilphamari", "নীলফামারী", "রংপুর বিভাগ", 25.9317, 88.8560),
        BdDistrict("Panchagarh", "Panchagarh", "পঞ্চগড়", "রংপুর বিভাগ", 26.3411, 88.5542),
        BdDistrict("Thakurgaon", "Thakurgaon", "ঠাকুরগাঁও", "রংপুর বিভাগ", 26.0337, 88.4617),
        BdDistrict("Lalmonirhat", "Lalmonirhat", "লালমনিরহাট", "রংপুর বিভাগ", 25.9923, 89.2847),
        // Mymensingh Division (4)
        BdDistrict("Mymensingh", "Mymensingh", "ময়মনসিংহ", "ময়মনসিংহ বিভাগ", 24.7471, 90.4203),
        BdDistrict("Jamalpur", "Jamalpur", "জামালপুর", "ময়মনসিংহ বিভাগ", 24.9375, 89.9378),
        BdDistrict("Netrokona", "Netrokona", "নেত্রকোনা", "ময়মনসিংহ বিভাগ", 24.8709, 90.7279),
        BdDistrict("Sherpur", "Sherpur", "শেরপুর", "ময়মনসিংহ বিভাগ", 25.0205, 90.0153)
    )

    fun findDistrict(idOrName: String): BdDistrict =
        bdDistricts.firstOrNull {
            it.id.equals(idOrName, ignoreCase = true) ||
                it.nameEn.equals(idOrName, ignoreCase = true) ||
                it.nameBn == idOrName
        } ?: bdDistricts.first()

    val tasbihPresets = listOf(
        TasbihPreset(
            id = "subhanallah",
            arabic = "سُبْحَانَ اللَّهِ",
            titleEn = "SubhanAllah",
            titleBn = "সুবহানাল্লাহ",
            meaningEn = "Glory be to Allah",
            meaningBn = "আল্লাহ পবিত্র ও মহিমাময়",
            defaultTarget = 33
        ),
        TasbihPreset(
            id = "alhamdulillah",
            arabic = "الْحَمْدُ لِلَّهِ",
            titleEn = "Alhamdulillah",
            titleBn = "আলহামদুলিল্লাহ",
            meaningEn = "All praise is due to Allah",
            meaningBn = "সকল প্রশংসা আল্লাহর জন্য",
            defaultTarget = 33
        ),
        TasbihPreset(
            id = "allahuakbar",
            arabic = "اللَّهُ أَكْبَرُ",
            titleEn = "Allahu Akbar",
            titleBn = "আল্লাহু আকবার",
            meaningEn = "Allah is the Greatest",
            meaningBn = "আল্লাহ সর্বশ্রেষ্ঠ",
            defaultTarget = 34
        ),
        TasbihPreset(
            id = "tahlil",
            arabic = "لَا إِلَٰهَ إِلَّا اللَّهُ",
            titleEn = "La ilaha illallah",
            titleBn = "লা ইলাহা ইল্লাল্লাহ",
            meaningEn = "There is no deity except Allah",
            meaningBn = "আল্লাহ ছাড়া কোনো সত্য ইলাহ নেই",
            defaultTarget = 100
        ),
        TasbihPreset(
            id = "istighfar",
            arabic = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            titleEn = "Astaghfirullah",
            titleBn = "আস্তাগফিরুল্লাহ",
            meaningEn = "I seek forgiveness from Allah",
            meaningBn = "আমি আল্লাহর কাছে ক্ষমা প্রার্থনা করছি",
            defaultTarget = 100
        ),
        TasbihPreset(
            id = "salawat",
            arabic = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
            titleEn = "Salawat / Durud",
            titleBn = "দরুদ শরীফ",
            meaningEn = "Peace and blessings upon Prophet Muhammad ﷺ",
            meaningBn = "হে আল্লাহ! আমাদের নবী মুহাম্মদের ওপর রহমত ও শান্তি বর্ষণ করুন",
            defaultTarget = 100
        )
    )

    val duas: List<DuaItem> = listOf(
        DuaItem(
            id = "sayyidul_istighfar",
            categoryBn = "সকাল-সন্ধ্যা",
            categoryEn = "Morning & Evening",
            titleBn = "সাইয়্যিদুল ইস্তিগফার (ক্ষমা প্রার্থনার শ্রেষ্ঠ দোয়া)",
            titleEn = "Sayyidul Istighfar (Master Supplication for Forgiveness)",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliterationBn = "আল্লাহুম্মা আনতা রাব্বী লা ইলাহা ইল্লা আনতা, খালাকতানী ওয়া আনা আবদুকা, ওয়া আনা আলা আহদিকা ওয়া ওয়াদিকা মাসতাতা'তু...",
            meaningBn = "হে আল্লাহ! আপনি আমার রব, আপনি ছাড়া কোনো সত্য উপাস্য নেই। আপনি আমাকে সৃষ্টি করেছেন এবং আমি আপনার বান্দা। আমি আমার সাধ্যমতো আপনার অঙ্গীকার ও প্রতিশ্রুতির ওপর আছি...",
            meaningEn = "O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant...",
            reference = "সহীহ আল-বুখারী: ৬৩০৬",
            repeatCount = 1,
            occasionBn = "ফজর ও মাগরিবের পর (সকাল-সন্ধ্যা)",
            occasionEn = "After Fajr & Maghrib (Morning & Evening)"
        ),
        DuaItem(
            id = "morning_protection",
            categoryBn = "সকাল-সন্ধ্যা",
            categoryEn = "Morning & Evening",
            titleBn = "সকল অনিষ্ট ও আকস্মিক বিপদ থেকে রক্ষার দোয়া",
            titleEn = "Protection from Sudden Harm & Calamity",
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliterationBn = "বিসমিল্লাহিল্লাযী লা ইয়াদুররু মা'আসমিহী শাইউন ফিল আরদি ওয়ালা ফিস সামা-ই ওয়া হুওয়াস সামী'উল আলীম।",
            meaningBn = "আল্লাহর নামে, যাঁর নামের বরকতে আসমান ও জমিনের কোনো কিছুই কোনো ক্ষতি করতে পারে না; আর তিনি সর্বশ্রোতা, সর্বজ্ঞ।",
            meaningEn = "In the Name of Allah with Whose Name nothing on earth or in heaven can cause harm, and He is the All-Hearing, All-Knowing.",
            reference = "সুনান আবু দাউদ: ৫০৮৮, তিরমিযী: ৩৩৮৮",
            repeatCount = 3,
            occasionBn = "প্রতিদিন সকাল ও সন্ধ্যায় ৩ বার",
            occasionEn = "Every Morning & Evening (3x)"
        ),
        DuaItem(
            id = "morning_hasbiyallah",
            categoryBn = "সকাল-সন্ধ্যা",
            categoryEn = "Morning & Evening",
            titleBn = "দুশ্চিন্তা ও পেরেশানি মুক্তির সকাল-সন্ধ্যার দোয়া",
            titleEn = "Sufficiency of Allah Against All Worries",
            arabic = "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ ۖ عَلَيْهِ تَوَكَّلْتُ ۖ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            transliterationBn = "হাসবিয়াল্লাহু লা ইলাহা ইল্লা হুয়া আলাইহি তাওয়াক্কালতু ওয়া হুয়া রাব্বুল আরশিল আযীম।",
            meaningBn = "আল্লাহই আমার জন্য যথেষ্ট, তিনি ছাড়া কোনো সত্য উপাস্য নেই। আমি তাঁরই ওপর ভরসা করেছি এবং তিনি মহান আরশের রব।",
            meaningEn = "Allah is sufficient for me. There is no deity except Him. On Him I have relied, and He is the Lord of the Great Throne.",
            reference = "সূরা আত-তাওবাহ: ১২৯; আবু দাউদ: ৫০৮১",
            repeatCount = 7,
            occasionBn = "সকাল ও সন্ধ্যায় মানসিক প্রশান্তির জন্য",
            occasionEn = "Morning & Evening for Relief from Anxiety"
        ),
        DuaItem(
            id = "after_salat_salam",
            categoryBn = "নামাজের পর",
            categoryEn = "After Prayer",
            titleBn = "সালাম ফেরানোর পর মাসনূন যিকির",
            titleEn = "Dhikr Immediately After Obligatory Prayer",
            arabic = "أَسْتَغْفِرُ اللَّهَ (٣) اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
            transliterationBn = "আস্তাগফিরুল্লাহ (৩ বার)। আল্লাহুম্মা আনতাস সালাম ওয়া মিনকাস সালাম, তাবারাকতা ইয়া যাল জালালি ওয়াল ইকরাম।",
            meaningBn = "আমি আল্লাহর কাছে ক্ষমা চাই (৩ বার)। হে আল্লাহ! আপনিই শান্তিদাতা এবং আপনার কাছ থেকেই শান্তি আসে। হে মহিমাময় ও মহানুভব! আপনি বরকতময়।",
            meaningEn = "I seek the forgiveness of Allah (3x). O Allah, You are Peace and from You comes peace. Blessed are You, O Owner of majesty and honor.",
            reference = "সহীহ মুসলিম: ৫৯১",
            repeatCount = 1,
            occasionBn = "ফরজ নামাজের সালাম ফেরানোর সাথে সাথে",
            occasionEn = "Immediately After Concluding Fard Salat"
        ),
        DuaItem(
            id = "after_salat_muadh",
            categoryBn = "নামাজের পর",
            categoryEn = "After Prayer",
            titleBn = "ইবাদত ও শুকরিয়া আদায়ে আল্লাহর সাহায্য চাওয়ার দোয়া",
            titleEn = "Help in Remembering, Thanking & Worshipping Allah",
            arabic = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ، وَشُكْرِكَ، وَحُسْنِ عِبَادَتِكَ",
            transliterationBn = "আল্লাহুম্মা আ'ইন্নী আলা যিকরিকা ওয়া শুকরিকা ওয়া হুসনি ইবাদাতিক।",
            meaningBn = "হে আল্লাহ! আপনার যিকির করার, আপনার কৃতজ্ঞতা প্রকাশ করার এবং উত্তমরূপে আপনার ইবাদত করার ব্যাপারে আমাকে সাহায্য করুন।",
            meaningEn = "O Allah, help me to remember You, to give thanks to You, and to worship You in the best manner.",
            reference = "সুনান আবু দাউদ: ১৫২২",
            repeatCount = 1,
            occasionBn = "প্রত্যেক নামাজের শেষ বৈঠকে বা সালামের পর",
            occasionEn = "After Every Prayer (Recommended to Muadh ra.)"
        ),
        DuaItem(
            id = "rabbana_atina",
            categoryBn = "দৈনন্দিন জীবন",
            categoryEn = "Daily Masnoon",
            titleBn = "দুনিয়া ও আখিরাতের কল্যাণের দোয়া (রাব্বানা আতিনা)",
            titleEn = "Goodness in This World and the Hereafter",
            arabic = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            transliterationBn = "রাব্বানা আতিনা ফিদ-দুনইয়া হাসানাতাও ওয়া ফিল আখিরাতি হাসানাতাও ওয়া কিনা আযাবান-নার।",
            meaningBn = "হে আমাদের রব! আমাদেরকে দুনিয়াতে কল্যাণ দিন এবং আখিরাতেও কল্যাণ দিন এবং আমাদেরকে জাহান্নামের আগুনের শাস্তি থেকে রক্ষা করুন।",
            meaningEn = "Our Lord, give us in this world that which is good and in the Hereafter that which is good and protect us from the punishment of the Fire.",
            reference = "সূরা আল-বাকারাহ: ২০১",
            repeatCount = 3,
            occasionBn = "যেকোনো মোনাজাত ও তাওয়াফের সময়",
            occasionEn = "General Supplication & Tawaf"
        ),
        DuaItem(
            id = "meal_before_after",
            categoryBn = "দৈনন্দিন জীবন",
            categoryEn = "Daily Masnoon",
            titleBn = "খাবার শেষ করার পর শুকরিয়ার দোয়া",
            titleEn = "Gratitude After Finishing a Meal",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            transliterationBn = "আলহামদুলিল্লাহিল্লাযী আত'আমানী হাযা ওয়া রাযাকানীহি মিন গাইরি হাওলিম মিন্নী ওয়ালা কুওয়্যাহ।",
            meaningBn = "সকল প্রশংসা আল্লাহর জন্য, যিনি আমার নিজস্ব কোনো চেষ্টা ও শক্তি ছাড়াই আমাকে এই খাবার খাইয়েছেন এবং রিজিক দান করেছেন।",
            meaningEn = "Praise be to Allah Who has fed me this food and provided it for me without any might or power on my part.",
            reference = "জামে আত-তিরমিযী: ৩৪৫৮",
            repeatCount = 1,
            occasionBn = "খাবার ও পানাহার শেষ করার পর",
            occasionEn = "After Eating Food or Drinking"
        ),
        DuaItem(
            id = "mosque_entry_dua",
            categoryBn = "দৈনন্দিন জীবন",
            categoryEn = "Daily Masnoon",
            titleBn = "মসজিদে প্রবেশের দোয়া",
            titleEn = "Entering the Mosque",
            arabic = "بِسْمِ اللَّهِ، وَالصَّلَاةُ وَالسَّلَامُ عَلَى رَسُولِ اللَّهِ، اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            transliterationBn = "বিসমিল্লাহি ওয়াস-সালাতু ওয়াস-সালামু আলা রাসূলিল্লাহ, আল্লাহুম্মাফতাহ লী আবওয়াবা রাহমাতিক।",
            meaningBn = "আল্লাহর নামে শুরু করছি এবং রাসূলুল্লাহ (ﷺ)-এর ওপর দরুদ ও সালাম। হে আল্লাহ! আমার জন্য আপনার রহমতের দরজাসমূহ খুলে দিন।",
            meaningEn = "In the name of Allah, and prayers and peace be upon the Messenger of Allah. O Allah, open the gates of Your mercy for me.",
            reference = "সহীহ মুসলিম: ৭১৩",
            repeatCount = 1,
            occasionBn = "ডান পা দিয়ে মসজিদে প্রবেশের সময়",
            occasionEn = "When Stepping Into the Masjid"
        ),
        DuaItem(
            id = "knowledge_increase",
            categoryBn = "দৈনন্দিন জীবন",
            categoryEn = "Daily Masnoon",
            titleBn = "উপকারী জ্ঞান ও প্রজ্ঞা বৃদ্ধির দোয়া",
            titleEn = "Seeking Beneficial Knowledge & Provision",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا",
            transliterationBn = "আল্লাহুম্মা ইন্নী আসআলুকা ইলমান নাফি'আন, ওয়া রিযকান তাইয়্যিবান, ওয়া আমালান মুতাকাব্বালা।",
            meaningBn = "হে আল্লাহ! আমি আপনার কাছে উপকারী জ্ঞান, পবিত্র রিজিক এবং কবুলযোগ্য আমল প্রার্থনা করছি।",
            meaningEn = "O Allah, I ask You for beneficial knowledge, goodly provision, and acceptable deeds.",
            reference = "সুনান ইবনু মাজাহ: ৯২৫",
            repeatCount = 1,
            occasionBn = "পড়াশোনার শুরুতে ও ফজরের নামাজের পর",
            occasionEn = "Before Studying & After Fajr Prayer"
        ),
        DuaItem(
            id = "sleep_dua",
            categoryBn = "ঘুম ও জাগরণ",
            categoryEn = "Sleep & Waking",
            titleBn = "ঘুমানোর আগের মাসনূন দোয়া",
            titleEn = "Before Going to Sleep",
            arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliterationBn = "বিসমিকাল্লাহুম্মা আমূতু ওয়া আহইয়া।",
            meaningBn = "হে আল্লাহ! আপনারই নামে আমি মৃত্যুবরণ করি (ঘুমাই) এবং আপনারই নামে জীবিত হই (জাগ্রত হই)।",
            meaningEn = "In Your name, O Allah, I die and I live.",
            reference = "সহীহ আল-বুখারী: ৬৩২৪",
            repeatCount = 1,
            occasionBn = "রাতে বিছানায় ঘুমানোর পূর্বে",
            occasionEn = "At Bedtime Before Sleeping"
        ),
        DuaItem(
            id = "waking_up_dua",
            categoryBn = "ঘুম ও জাগরণ",
            categoryEn = "Sleep & Waking",
            titleBn = "ঘুম থেকে জেগে ওঠার দোয়া",
            titleEn = "Upon Waking Up from Sleep",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliterationBn = "আলহামদুলিল্লাহিল্লাযী আহইয়ানা বা'দা মা আমাতানা ওয়া ইলাইহিন নুশূর।",
            meaningBn = "সকল প্রশংসা আল্লাহর জন্য, যিনি আমাদেরকে নিদ্রারূপ মৃত্যুর পর পুনরায় জীবিত করেছেন, আর তাঁরই কাছে সকলের পুনরুত্থান।",
            meaningEn = "All praise is for Allah who gave us life after having taken it from us and unto Him is the resurrection.",
            reference = "সহীহ আল-বুখারী: ৬৩১২",
            repeatCount = 1,
            occasionBn = "সকালে ঘুম ভাঙার সাথে সাথে",
            occasionEn = "Immediately Upon Waking Up"
        ),
        DuaItem(
            id = "leaving_home_dua",
            categoryBn = "সফর ও নিরাপত্তা",
            categoryEn = "Travel & Safety",
            titleBn = "ঘর থেকে বের হওয়ার দোয়া",
            titleEn = "Leaving the House for Work or Travel",
            arabic = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliterationBn = "বিসমিল্লাহি তাওয়াক্কালতু আলাল্লাহ, ওয়ালা হাওলা ওয়ালা কুওয়্যাতা ইল্লা বিল্লাহ।",
            meaningBn = "আল্লাহর নামে বের হচ্ছি, আল্লাহর ওপরই ভরসা করলাম। আর আল্লাহর সাহায্য ছাড়া কোনো শক্তি বা ক্ষমতা নেই।",
            meaningEn = "In the name of Allah, I place my trust in Allah, and there is no might nor power except with Allah.",
            reference = "সুনান আবু দাউদ: ৫০৯৫",
            repeatCount = 1,
            occasionBn = "বাসা বা ঘর থেকে বাইরে বের হওয়ার সময়",
            occasionEn = "When Stepping Out of Home"
        ),
        DuaItem(
            id = "travel_vehicle_dua",
            categoryBn = "সফর ও নিরাপত্তা",
            categoryEn = "Travel & Safety",
            titleBn = "যানবাহনে আরোহণ ও সফরের দোয়া",
            titleEn = "Boarding a Vehicle & Starting a Journey",
            arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنْقَلِبُونَ",
            transliterationBn = "সুবহানাল্লাযী সাখখারা লানা হাযা ওয়ামা কুন্না লাহু মুকরিনীন, ওয়া ইন্না ইলা রাব্বিনা লামুনকালিবূন।",
            meaningBn = "পবিত্র সেই মহান সত্তা, যিনি এই বাহনকে আমাদের অধীন করে দিয়েছেন, অথচ আমরা একে বশীভূত করতে সক্ষম ছিলাম না। আর আমরা আমাদের রবের কাছেই ফিরে যাব।",
            meaningEn = "Glory to Him Who has subjected this to us, and we could never have it by our efforts. And assuredly, to our Lord we shall return.",
            reference = "সূরা আয-যুখরুফ: ১৩-১৪; মুসলিম: ১৩৪২",
            repeatCount = 1,
            occasionBn = "গাড়ি, বাস, ট্রেন বা বিমানে ভ্রমণের শুরুতে",
            occasionEn = "When Boarding Any Transport or Journey"
        ),
        DuaItem(
            id = "ruqyah_shifa",
            categoryBn = "রুকইয়াহ ও শিফা",
            categoryEn = "Ruqyah & Healing",
            titleBn = "অসুস্থ ব্যক্তির আরোগ্য লাভের নববী রুকইয়াহ",
            titleEn = "Prophetic Ruqyah for Complete Healing",
            arabic = "اللَّهُمَّ رَبَّ النَّاسِ أَذْهِبِ الْبَأْسَ، اشْفِهِ وَأَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا",
            transliterationBn = "আল্লাহুম্মা রাব্বান-নাস, আযহিবিল বা'স, ইশফিহি ওয়া আনতাশ-শাফী, লা শিফাআ ইল্লা শিফাউকা, শিফাআন লা ইউগাদিরু সাকামা।",
            meaningBn = "হে আল্লাহ! মানুষের প্রতিপালক, কষ্ট দূর করে দিন এবং আরোগ্য দান করুন—আপনিই তো আরোগ্যদাতা। আপনার আরোগ্য ছাড়া কোনো আরোগ্য নেই; এমন আরোগ্য দিন যা কোনো রোগ অবশিষ্ট রাখে না।",
            meaningEn = "O Allah, Lord of mankind, remove the affliction and heal, for You are the Healer. There is no healing except Your healing, a healing that leaves no illness behind.",
            reference = "সহীহ আল-বুখারী: ৫৬৭৫",
            repeatCount = 3,
            occasionBn = "অসুস্থতা ও শারীরিক ব্যথা নিরাময়ে",
            occasionEn = "For Sickness, Pain & Visiting the Sick"
        ),
        DuaItem(
            id = "distress_yunus",
            categoryBn = "রুকইয়াহ ও শিফা",
            categoryEn = "Ruqyah & Healing",
            titleBn = "কঠিন বিপদ ও সংকট মুক্তির দোয়া (দোয়ায়ে ইউনুস)",
            titleEn = "Relief from Severe Distress (Dua of Prophet Yunus)",
            arabic = "لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
            transliterationBn = "লা ইলাহা ইল্লা আনতা সুবহানাকা ইন্নী কুনতু মিনায-যালিমীন।",
            meaningBn = "হে আল্লাহ! আপনি ছাড়া কোনো সত্য উপাস্য নেই; আপনি পবিত্র ও মহান। নিশ্চয়ই আমি জালিমদের অন্তর্ভুক্ত হয়ে গেছি।",
            meaningEn = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
            reference = "সূরা আল-আম্বিয়া: ৮৭; তিরমিযী: ৩৫০৫",
            repeatCount = 7,
            occasionBn = "কঠিন বিপদ, মানসিক চাপ ও সংকটের মুহূর্তে",
            occasionEn = "During Severe Hardship, Grief or Crisis"
        ),
        DuaItem(
            id = "iftar_dua",
            categoryBn = "রমজান ও রোজা",
            categoryEn = "Ramadan & Fasting",
            titleBn = "ইফতারের সুন্নাহ দোয়া",
            titleEn = "Supplication Upon Breaking Fast (Iftar)",
            arabic = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
            transliterationBn = "যাহাবায-যামাউ ওয়াবতাল্লাতিল উরূকু ওয়া সাবাতাল আজরু ইন শা আল্লাহ।",
            meaningBn = "পিপাসা দূর হলো, শিরা-উপশিরা সিক্ত হলো এবং আল্লাহ চান তো প্রতিদানও নির্ধারিত হলো।",
            meaningEn = "The thirst is gone, the veins are moistened, and the reward is confirmed, if Allah wills.",
            reference = "সুনান আবু দাউদ: ২৩৫৭",
            repeatCount = 1,
            occasionBn = "ইফতার করার পরপরই",
            occasionEn = "At the Time of Breaking Fast (Iftar)"
        ),
        DuaItem(
            id = "lailatul_qadr",
            categoryBn = "রমজান ও রোজা",
            categoryEn = "Ramadan & Fasting",
            titleBn = "লাইলাতুল কদরের বিশেষ দোয়া",
            titleEn = "Supplication for Laylatul Qadr",
            arabic = "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
            transliterationBn = "আল্লাহুম্মা ইন্নাকা আফুউন তুহিব্বুল আফওয়া ফা'ফু আন্নী।",
            meaningBn = "হে আল্লাহ! নিশ্চয়ই আপনি পরম ক্ষমাশীল, আপনি ক্ষমা করতে ভালোবাসেন; অতএব আমাকে ক্ষমা করে দিন।",
            meaningEn = "O Allah, indeed You are Pardoning and love pardon, so pardon me.",
            reference = "জামে আত-তিরমিযী: ৩৫১৩",
            repeatCount = 7,
            occasionBn = "রমজানের শেষ দশক ও কদরের রাতে",
            occasionEn = "Last 10 Nights of Ramadan & Laylatul Qadr"
        ),
        DuaItem(
            id = "parents_mercy_dua",
            categoryBn = "ক্ষমা ও পরিবার",
            categoryEn = "Forgiveness & Family",
            titleBn = "পিতা-মাতার মাগফিরাত ও রহমতের দোয়া",
            titleEn = "Mercy and Forgiveness for Parents",
            arabic = "رَّبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
            transliterationBn = "রাব্বির হামহুমা কামা রাব্বায়ানী সাগীরা।",
            meaningBn = "হে আমার রব! আমার পিতা-মাতার প্রতি সদয় হোন, যেভাবে শৈশবে তাঁরা আমাকে স্নেহ-মমতা দিয়ে লালন-পালন করেছেন।",
            meaningEn = "My Lord, have mercy upon them as they brought me up when I was small.",
            reference = "সূরা বনী ইসরাঈল: ২৪",
            repeatCount = 3,
            occasionBn = "মা-বাবার জন্য প্রতি নামাজের পর দোয়া",
            occasionEn = "Praying for Parents Daily"
        )
    )

    val hadiths: List<HadithItem> = listOf(
        HadithItem(
            id = "bukhari_5027",
            bookBn = "সহীহ আল-বুখারী",
            bookEn = "Sahih al-Bukhari",
            hadithNumber = "5027",
            chapterBn = "কুরআনের ফজিলত অধ্যায়",
            arabic = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            translationBn = "রাসূলুল্লাহ (ﷺ) বলেছেন: “তোমাদের মধ্যে সর্বোত্তম ঐ ব্যক্তি, যে নিজে কুরআন শেখে এবং অন্যকে তা শেখায়।”",
            translationEn = "The Messenger of Allah ﷺ said: “The best among you are those who learn the Quran and teach it to others.”",
            narratorBn = "হযরত উসমান ইবনু আফফান (রা.)"
        ),
        HadithItem(
            id = "bukhari_1",
            bookBn = "সহীহ আল-বুখারী",
            bookEn = "Sahih al-Bukhari",
            hadithNumber = "1",
            chapterBn = "ওহীর সূচনা অধ্যায়",
            arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
            translationBn = "রাসূলুল্লাহ (ﷺ) বলেছেন: “সকল কাজ নিয়তের ওপর নির্ভরশীল; আর প্রতিটি মানুষ তার নিয়ত অনুযায়ীই প্রতিফল পাবে।”",
            translationEn = "Actions are judged by intentions, and every person will have what they intended.",
            narratorBn = "হযরত উমর ইবনুল খাত্তাব (রা.)"
        ),
        HadithItem(
            id = "muslim_223",
            bookBn = "সহীহ মুসলিম",
            bookEn = "Sahih Muslim",
            hadithNumber = "223",
            chapterBn = "পবিত্রতার ফজিলত অধ্যায়",
            arabic = "الطُّهُورُ شَطْرُ الإِيمَانِ وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ",
            translationBn = "রাসূলুল্লাহ (ﷺ) বলেছেন: “পবিত্রতা ঈমানের অঙ্গ (অর্ধেক), আর ‘আলহামদুলিল্লাহ’ মীযানের পাল্লাকে পূর্ণ করে দেয়।”",
            translationEn = "Cleanliness is half of faith and Alhamdulillah fills the scale.",
            narratorBn = "হযরত আবু মালিক আল-আশআরী (রা.)"
        ),
        HadithItem(
            id = "musnad_ahmad_salat",
            bookBn = "মুসনাদে আহমাদ",
            bookEn = "Musnad Ahmad",
            hadithNumber = "6576",
            chapterBn = "নামাজ সংরক্ষণের ফজিলত",
            arabic = "مَنْ حَافَظَ عَلَيْهَا كَانَتْ لَهُ نُورًا وَبُرْهَانًا وَنَجَاةً يَوْمَ الْقِيَامَةِ",
            translationBn = "“যে ব্যক্তি পাঁচ ওয়াক্ত নামাজের যথাযথ সংরক্ষণ করবে, কিয়ামতের দিন তা তার জন্য জ্যোতি (নূর), দলিল এবং মুক্তির উপায় হবে।”",
            translationEn = "Whoever regularly guards the five daily prayers at their prescribed times, they will be a light, an indisputable proof, and a source of salvation on the Day of Resurrection.",
            narratorBn = "হযরত আবদুল্লাহ ইবনু আমর (রা.)"
        ),
        HadithItem(
            id = "tirmidhi_1956",
            bookBn = "জামে আত-তিরমিযী",
            bookEn = "Jami at-Tirmidhi",
            hadithNumber = "1956",
            chapterBn = "সদাচরণ ও সদকা অধ্যায়",
            arabic = "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ",
            translationBn = "রাসূলুল্লাহ (ﷺ) বলেছেন: “তোমার ভাইয়ের দিকে হাসিমুখে তাকানোও তোমার জন্য একটি সদকা।”",
            translationEn = "Your smiling in the face of your brother is charity.",
            narratorBn = "হযরত আবু যর গিফারী (রা.)"
        )
    )

    val namesOfAllah: List<AllahNameItem> = listOf(
        AllahNameItem(1, "ٱللَّٰهُ", "Allah", "আল্লাহ", "সকল গুণের আধার একক ইলাহ", "The Greatest Name", "হৃদয়ে ঈমানের নূর ও প্রশান্তি বৃদ্ধি পায়।"),
        AllahNameItem(2, "ٱلرَّحْمَـٰنُ", "Ar-Rahman", "আর-রহমান", "পরম করুণাময়", "The Entirely Merciful", "আল্লাহর অসীম রহমত ও গাফিলতি দূর হওয়ার মাধ্যম।"),
        AllahNameItem(3, "ٱلرَّحِيمُ", "Ar-Rahim", "আর-রহীম", "অতি দয়ালু", "The Especially Merciful", "দুনিয়া ও আখিরাতে বিশেষ অনুগ্রহ লাভ।"),
        AllahNameItem(4, "ٱلْمَلِكُ", "Al-Malik", "আল-মালিক", "সার্বভৌম অধিপতি", "The King and Sovereign", "আত্মিক সম্মান ও অমুখাপেক্ষিতা অর্জন।"),
        AllahNameItem(5, "ٱلْقُدُّوسُ", "Al-Quddus", "আল-কুদ্দুস", "নিষ্কলুষ ও অতি পবিত্র", "The Absolutely Pure", "অন্তরের কলুষতা ও কুমন্ত্রণা থেকে মুক্তি।"),
        AllahNameItem(6, "ٱلسَّلَامُ", "As-Salam", "আস-সালাম", "শান্তি ও নিরাপত্তার উৎস", "The Source of Peace", "অসুস্থতা ও বিপদাপদ থেকে নিরাপত্তা লাভ।"),
        AllahNameItem(7, "ٱلْمُؤْمِنُ", "Al-Mu'min", "আল-মু'মিন", "নিরাপত্তা ও ঈমান দানকারী", "The Giver of Faith & Security", "ভয়ভীতি দূর হয়ে অন্তরে প্রশান্তি আসে।"),
        AllahNameItem(8, "ٱلْمُهَيْمِنُ", "Al-Muhaymin", "আল-মুহাইমিন", "সর্বরক্ষক ও তত্ত্বাবধায়ক", "The Guardian and Overseer", "আল্লাহর হিফাজত ও স্মরণশক্তি বৃদ্ধি।"),
        AllahNameItem(9, "ٱلْعَزِيزُ", "Al-Aziz", "আল-আযীয", "মহাপরাক্রমশালী", "The Almighty", "অপমান থেকে মুক্তি ও মর্যাদা লাভ।"),
        AllahNameItem(10, "ٱلْجَبَّارُ", "Al-Jabbar", "আল-জাব্বার", "দুর্নিবার ও ভাঙা হৃদয় জোড়া লাগানো সত্তা", "The Compeller & Restorer", "জুলুম থেকে রক্ষা ও বিপদে সান্ত্বনা লাভ।"),
        AllahNameItem(11, "ٱلْمُتَكَبِّرُ", "Al-Mutakabbir", "আল-মুতাকাব্বির", "শ্রেষ্ঠত্বের অধিকারী", "The Supreme in Greatness", "অহংকার থেকে মুক্তি ও কাজে বরকত।"),
        AllahNameItem(12, "ٱلْخَالِقُ", "Al-Khaliq", "আল-খালিক", "সৃষ্টিকর্তা", "The Creator", "সৃষ্টির মহিমা উপলব্ধি ও ইবাদতে একনিষ্ঠতা।"),
        AllahNameItem(13, "ٱلْبَارِئُ", "Al-Bari'", "আল-বারী", "শূন্য থেকে অস্তিত্ব দানকারী", "The Evolver", "রোগমুক্তি ও কঠিন কাজ সহজ হওয়া।"),
        AllahNameItem(14, "ٱلْمُصَوِّرُ", "Al-Musawwir", "আল-মুসাওয়ির", "রূপদানকারী", "The Fashioner", "উত্তম চরিত্র ও সুন্দর চিন্তাশক্তি লাভ।"),
        AllahNameItem(15, "ٱلْغَفَّارُ", "Al-Ghaffar", "আল-গাফফার", "বারবার ক্ষমাকারী", "The Constant Forgiver", "গুনাহ মাফ ও রিজিকে বরকত লাভ।"),
        AllahNameItem(16, "ٱلْقَهَّارُ", "Al-Qahhar", "আল-কাহহার", "সর্বজয়ী পরাক্রমশালী", "The Subduer", "নফসের কুমন্ত্রণা ও দুনিয়ার মোহ জয় করা।"),
        AllahNameItem(17, "ٱلْوَهَّابُ", "Al-Wahhab", "আল-ওয়াহহাব", "বিনা স্বার্থে মহান দাতা", "The Bestower", "অভাব মোচন ও উত্তম দান লাভের দোয়া।"),
        AllahNameItem(18, "ٱلرَّزَّاقُ", "Ar-Razzaq", "আর-রাযযাক", "রিজিকদাতা", "The Provider", "হালাল ও প্রশস্ত রিজিক লাভের শ্রেষ্ঠ ওসিলা।"),
        AllahNameItem(19, "ٱلْفَتَّاحُ", "Al-Fattah", "আল-ফাত্তাহ", "বিজয় ও রহমতের দ্বার উন্মোচনকারী", "The Opener", "কঠিন সংকট সমাধান ও জ্ঞানের দুয়ার খোলা।"),
        AllahNameItem(20, "ٱلْعَلِيمُ", "Al-Alim", "আল-আলীম", "সর্বজ্ঞানী", "The All-Knowing", "উপকারী ইলম ও প্রজ্ঞা বৃদ্ধি।")
    )

    val fatwas: List<FatwaItem> = listOf(
        FatwaItem(
            id = "fatwa_qaza",
            categoryBn = "নামাজ ও কাজা",
            questionBn = "ফরজ নামাজ কাজা হয়ে গেলে তা আদায়ের নিয়ম কী?",
            answerBn = "কোনো ফরজ বা ওয়াজিব (বিতর) নামাজ সময়মতো আদায় করতে না পারলে যত দ্রুত সম্ভব কাজা আদায় করা ওয়াজিব। নিষিদ্ধ তিন সময় (সূর্যোদয়, দ্বিপ্রহর ও সূর্যাস্ত) ছাড়া যেকোনো সময় কাজা নামাজ পড়া যায়। শুধুমাত্র ফরজ ও বিতর নামাজের কাজা পড়তে হয়।",
            referenceBn = "সহীহ মুসলিম: ৬৮৪; ফাতাওয়া শামী: ২/৬২"
        ),
        FatwaItem(
            id = "fatwa_zakat_nisab",
            categoryBn = "যাকাত ও সদকা",
            questionBn = "যাকাতের নিসাব কীভাবে হিসাব করতে হবে—স্বর্ণ নাকি রুপার ভিত্তিতে?",
            answerBn = "সাড়ে সাত তোলা (৮৭.৪৮ গ্রাম) স্বর্ণ অথবা সাড়ে বায়ান্ন তোলা (৬১২.৩৬ গ্রাম) রুপা বা এর সমমূল্যের নগদ অর্থ ও ব্যবসায়িক পণ্য এক চান্দ্র বছর স্থায়ী হলে ২.৫% হারে যাকাত ফরজ হয়। দরিদ্রদের কল্যাণের দিকে লক্ষ্য রেখে রুপার নিসাব অনুযায়ী হিসাব করা আলেমগণের নিকট অধিক সতর্ক ও উত্তম।",
            referenceBn = "সুনান আবু দাউদ: ১৫৭৩; আল-হিদায়া: ১/১৮৬"
        ),
        FatwaItem(
            id = "fatwa_wudu_doubt",
            categoryBn = "পবিত্রতা ও ওযু",
            questionBn = "ওযু করার পর ওযু ভেঙেছে কি না সন্দেহ হলে করণীয় কী?",
            answerBn = "নিশ্চিতভাবে ওযু করার পর কেবল সন্দেহের কারণে ওযু ভাঙে না। যতক্ষণ না ওযু ভঙ্গের ব্যাপারে নিশ্চিত হওয়া যায়, ততক্ষণ আগের ওযু বহাল থাকবে।",
            referenceBn = "সহীহ বুখারী: ১৩৭; সহীহ মুসলিম: ৩৬১"
        ),
        FatwaItem(
            id = "fatwa_fasting_inhaler",
            categoryBn = "রমজান ও রোজা",
            questionBn = "রোজা অবস্থায় ভুলবশত কিছু খেয়ে ফেললে কি রোজা ভেঙে যাবে?",
            answerBn = "রোজা অবস্থায় সম্পূর্ণ ভুলবশত কিছু খেয়ে বা পান করে ফেললে রোজা ভাঙে না। মনে পড়ার সাথে সাথে খাওয়া বন্ধ করতে হবে এবং বাকি দিন রোজা পূর্ণ করতে হবে।",
            referenceBn = "সহীহ বুখারী: ১৯৩৩; সহীহ মুসলিম: ১১৫৫"
        )
    )

    val books: List<IslamicBookItem> = listOf(
        IslamicBookItem(
            id = "riyadus_salihin",
            titleBn = "রিয়াদুস সালেহীন (নেককারদের বাগান)",
            authorBn = "ইমাম মুহিউদ্দিন আন-নববী (রহ.)",
            categoryBn = "হাদীস ও আত্মশুদ্ধি",
            languageBn = "বাংলা ও আরবি",
            summaryBn = "ইখলাস, তাওবা, সবর, শুকরিয়া ও দৈনন্দিন আমল বিষয়ে কুরআন ও সহীহ হাদীসের এক অনন্য সংকলন।",
            chapters = listOf(
                "অধ্যায় ১: ইখলাস ও নিয়তের বিশুদ্ধতা" to "প্রতিটি ইবাদত ও ভালো কাজে একমাত্র আল্লাহর সন্তুষ্টি অর্জনই মুমিনের মূল লক্ষ্য। নিয়ত বিশুদ্ধ হলে ক্ষুদ্র আমলও পাহাড়সম সওয়াবে পরিণত হয়।",
                "অধ্যায় ২: তাওবা ও ক্ষমা প্রার্থনা" to "বান্দা গুনাহ করার পর অনুতপ্ত হৃদয়ে আল্লাহর দিকে ফিরে এলে আল্লাহ অত্যন্ত আনন্দিত হন এবং তাঁর ক্ষমার দুয়ার সর্বদা খোলা থাকে।",
                "অধ্যায় ৩: সবর ও ধৈর্য ধারণ" to "বিপদাপদে ধৈর্য ধারণ করা এবং আল্লাহর ফয়সালায় সন্তুষ্ট থাকা ঈমানের অন্যতম শ্রেষ্ঠ নিদর্শন।"
            )
        ),
        IslamicBookItem(
            id = "hisnul_muslim",
            titleBn = "হিসনুল মুসলিম (মুমিনের দুর্গ)",
            authorBn = "শাইখ সাঈদ ইবন আলী আল-কাহতানী",
            categoryBn = "দোয়া ও যিকির",
            languageBn = "বাংলা ও আরবি",
            summaryBn = "সকাল-সন্ধ্যা, ঘুম, নামাজ, সফর ও বিপদাপদসহ জীবনের প্রতিটি মুহূর্তের প্রামাণ্য মাসনূন দোয়ার পকেট গাইড।",
            chapters = listOf(
                "অধ্যায় ১: সকাল ও সন্ধ্যার যিকির" to "ফজরের পর থেকে সূর্যোদয় এবং আসরের পর থেকে সূর্যাস্ত পর্যন্ত পঠিতব্য যিকিরসমূহ মুমিনকে সারাদিন নিরাপদ দুর্গে রাখে।",
                "অধ্যায় ২: রুকইয়াহ শারইয়াহ" to "কুরআনের আয়াত ও রাসূল (ﷺ)-এর শেখানো দোয়ার মাধ্যমে আত্মিক ও শারীরিক আরোগ্য লাভের পদ্ধতি।"
            )
        ),
        IslamicBookItem(
            id = "rahe_belayet",
            titleBn = "রাহে বেলায়েত ও মাসনূন আমল",
            authorBn = "ড. খোন্দকার আব্দুল্লাহ জাহাঙ্গীর (রহ.)",
            categoryBn = "ইবাদত ও সুন্নাহ",
            languageBn = "বাংলা",
            summaryBn = "সুন্নাতের আলোকে আল্লাহর নৈকট্য লাভ, প্রতিদিনের ওযিফা এবং বিদআতমুক্ত আমলের নির্ভরযোগ্য নির্দেশিকা।",
            chapters = listOf(
                "অধ্যায় ১: সুন্নাহর অনুসরণের গুরুত্ব" to "রাসূলুল্লাহ (ﷺ)-এর সুন্নাহর হুবহু অনুসরণই আল্লাহর ভালোবাসা ও বেলায়েত লাভের একমাত্র রাজপথ।",
                "অধ্যায় ২: পাঁচ ওয়াক্ত নামাজের খুশু-খুযু" to "নামাজে একাগ্রতা অর্জনের জন্য নামাজের অর্থ বোঝা এবং ধীরস্থিরভাবে রুকু-সিজদা আদায় করা জরুরি।"
            )
        )
    )

    val hajjSteps: List<HajjStepItem> = listOf(
        HajjStepItem(
            stepNumber = 1,
            titleBn = "ইহরাম ও তালবিয়াহ (মীকাত)",
            dateHijriBn = "৮ জিলহজ বা উমরাহর শুরুতে",
            locationBn = "মীকাত ও মসজিদুল হারাম",
            descriptionBn = "পরিষ্কার-পরিচ্ছন্ন হয়ে ইহরামের কাপড় পরিধান করে হজ বা উমরাহর নিয়ত করা এবং বেশি বেশি তালবিয়াহ পাঠ করা।",
            duaArabic = "لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لَا شَرِيكَ لَكَ",
            duaMeaningBn = "আমি হাজির হে আল্লাহ! আমি হাজির। আপনার কোনো শরীক নেই, নিশ্চয়ই সমস্ত প্রশংসা, নিআমত ও রাজত্ব আপনারই।"
        ),
        HajjStepItem(
            stepNumber = 2,
            titleBn = "তাওয়াফ ও মাকামে ইবরাহীম",
            dateHijriBn = "মক্কায় আগমন / ১০ জিলহজ",
            locationBn = "পবিত্র কাবা শরীফ",
            descriptionBn = "হাজরে আসওয়াদ থেকে শুরু করে কাবাঘরের চারপাশে ৭ চক্কর তাওয়াফ সম্পন্ন করা এবং মাকামে ইবরাহীমের পেছনে ২ রাকাত নামাজ পড়া।",
            duaArabic = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            duaMeaningBn = "রুকনে ইয়ামানী ও হাজরে আসওয়াদের মাঝে এই দোয়াটি পড়া সুন্নত।"
        ),
        HajjStepItem(
            stepNumber = 3,
            titleBn = "সাফা ও মারওয়া সাঈ",
            dateHijriBn = "তাওয়াফের পর",
            locationBn = "সাফা ও মারওয়া পাহাড়",
            descriptionBn = "সাফা পাহাড় থেকে শুরু করে মারওয়া পর্যন্ত ৭ বার সাঈ করা এবং যমযমের পানি পান করা।",
            duaArabic = "إِنَّ الصَّفَا وَالْمَرْوَةَ مِن شَعَائِرِ اللَّهِ",
            duaMeaningBn = "নিশ্চয়ই সাফা ও মারওয়া আল্লাহর নিদর্শনসমূহের অন্তর্ভুক্ত।"
        ),
        HajjStepItem(
            stepNumber = 4,
            titleBn = "উকুফে আরাফাহ ও মুযদালিফা",
            dateHijriBn = "৯ জিলহজ",
            locationBn = "আরাফাতের ময়দান ও মুযদালিফা",
            descriptionBn = "হজের মূল রুকন হলো ৯ জিলহজ আরাফাতের ময়দানে অবস্থান করে দোয়া ও ইস্তিগফার করা এবং রাতে মুযদালিফায় অবস্থান করা।",
            duaArabic = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            duaMeaningBn = "আরাফাতের দিনের শ্রেষ্ঠ দোয়া।"
        ),
        HajjStepItem(
            stepNumber = 5,
            titleBn = "জামারায় কঙ্কর নিক্ষেপ, কুরবানী ও হলক",
            dateHijriBn = "১০–১২ জিলহজ",
            locationBn = "মিনা",
            descriptionBn = "জামারায়ে আকাবায় ৭টি কঙ্কর নিক্ষেপ, হাদির পশু কুরবানী, মাথা মুণ্ডন বা চুল ছোট করা এবং তাওয়াফে যিয়ারত সম্পন্ন করা।",
            duaArabic = "اللَّهُ أَكْبَرُ",
            duaMeaningBn = "প্রতিটি কঙ্কর নিক্ষেপের সময় ‘আল্লাহু আকবার’ বলা।"
        )
    )

    val babyNames: List<BabyNameItem> = listOf(
        BabyNameItem("আবদুল্লাহ", "Abdullah", "عَبْدُ اللَّهِ", "ছেলে", "আল্লাহর বান্দা (আল্লাহর সবচেয়ে প্রিয় নাম)"),
        BabyNameItem("আবদুর রহমান", "Abdur Rahman", "عَبْدُ الرَّحْمَنِ", "ছেলে", "পরম করুণাময়ের বান্দা"),
        BabyNameItem("মুহাম্মদ", "Muhammad", "مُحَمَّد", "ছেলে", "প্রশংসিত"),
        BabyNameItem("রায়হান", "Rayhan", "رَيْحَان", "ছেলে", "জান্নাতের সুগন্ধি ফুল"),
        BabyNameItem("আবরার", "Abrar", "أَبْرَار", "ছেলে", "নেককার ও সত্যবাদী"),
        BabyNameItem("তাহমিদ", "Tahmid", "تَحْمِيد", "ছেলে", "আল্লাহর প্রশংসাকারী"),
        BabyNameItem("জুবায়ের", "Zubair", "زُبَيْر", "ছেলে", "শক্তিমান ও সাহসী সাহাবীর নাম"),
        BabyNameItem("আয়েশা", "Aisha", "عَائِشَة", "মেয়ে", "জীবনযাপনকারিনী, সচ্ছল (উম্মুল মুমিনীন)"),
        BabyNameItem("ফাতিমা", "Fatima", "فَاطِمَة", "মেয়ে", "জাহান্নাম থেকে মুক্ত (নবীকন্যা)"),
        BabyNameItem("মারইয়াম", "Maryam", "مَرْيَم", "মেয়ে", "আল্লাহর ইবাদতকারিনী ও পবিত্রা"),
        BabyNameItem("তাসনিম", "Tasnim", "تَسْنِيم", "মেয়ে", "জান্নাতের সুউচ্চ ঝর্ণা"),
        BabyNameItem("হুমায়রা", "Humaira", "حُمَيْرَاء", "মেয়ে", "শুভ্র-লালিমাময়ী"),
        BabyNameItem("আফিয়া", "Afia", "عَافِيَة", "মেয়ে", "নিরাপত্তা ও সুস্বাস্থ্য"),
        BabyNameItem("সুমাইয়া", "Sumayyah", "سُمَيَّة", "মেয়ে", "উচ্চ মর্যাদাসম্পন্না (ইসলামের প্রথম শহীদা)")
    )

    val islamicEvents: List<IslamicEventItem> = listOf(
        IslamicEventItem(1, 1, "হিজরি নববর্ষের সূচনা", "Islamic New Year", "মুহাররম মাসের প্রথম দিন; হিজরি ক্যালেন্ডারের সূচনা।"),
        IslamicEventItem(1, 10, "পবিত্র আশুরা", "Day of Ashura", "মুহাররমের ১০ তারিখ; ৯ ও ১০ অথবা ১০ ও ১১ তারিখ নফল রোজা রাখা সুন্নত।", true),
        IslamicEventItem(3, 12, "সীরাতুন্নবী (ﷺ) স্মরণ", "12 Rabi' al-Awwal", "রাসূলুল্লাহ (ﷺ)-এর জীবনাদর্শ ও সুন্নাহ চর্চার বিশেষ মাস।"),
        IslamicEventItem(8, 15, "মধ্য শাবান (১৫ শাবান)", "Mid-Sha'ban", "শাবান মাসে অধিক নফল রোজা রেখে রমজানের প্রস্তুতি গ্রহণ।", true),
        IslamicEventItem(9, 1, "পবিত্র মাহে রমজান শুরু", "Start of Ramadan", "রহমত, মাগফিরাত ও নাজাতের মাস; কুরআন নাযিলের মাস।", true),
        IslamicEventItem(9, 27, "লাইলাতুল কদর অনুসন্ধান", "Laylatul Qadr (Last 10 Nights)", "হাজার মাসের চেয়েও শ্রেষ্ঠ রজনী; শেষ দশকের বিজোড় রাতগুলোতে অনুসন্ধান।"),
        IslamicEventItem(10, 1, "পবিত্র ঈদুল ফিতর", "Eid-ul-Fitr", "এক মাস সিয়াম সাধনার পর আনন্দের দিন ও সদকাতুল ফিতর আদায়।"),
        IslamicEventItem(12, 9, "ইয়াওমে আরাফাহ", "Day of Arafah", "আরাফাতের দিন রোজা রাখলে বিগত এক বছর ও আগামী এক বছরের গুনাহ মাফ হয়।", true),
        IslamicEventItem(12, 10, "পবিত্র ঈদুল আযহা", "Eid-ul-Adha", "ত্যাগ ও কুরবানীর মহান ঈদ।")
    )

    fun getNearbyMosquesForDistrict(district: BdDistrict, schedule: DailyPrayerSchedule): List<NearbyMosqueItem> {
        val nextPrayer = schedule.fivePrayers.firstOrNull { it.type == schedule.nextWaqt }
            ?: schedule.fivePrayers.first()
        return listOf(
            NearbyMosqueItem(
                id = "${district.id}_central",
                nameBn = "${district.nameBn} কেন্দ্রীয় জামে মসজিদ",
                nameEn = "${district.nameEn} Central Jame Masjid",
                areaBn = "${district.nameBn} সদর • প্রধান সড়ক",
                distanceKm = 0.4,
                nextJamaatLabel = "${nextPrayer.type.nameBn} জামাত: ${nextPrayer.jamaatFormatted12h}",
                hasWomenPrayerSpace = true,
                hasParking = true
            ),
            NearbyMosqueItem(
                id = "${district.id}_model",
                nameBn = "${district.nameBn} জেলা মডেল মসজিদ ও ইসলামিক সাংস্কৃতিক কেন্দ্র",
                nameEn = "${district.nameEn} Model Mosque & Islamic Center",
                areaBn = "${district.nameBn} পৌর এলাকা",
                distanceKm = 1.1,
                nextJamaatLabel = "${nextPrayer.type.nameBn} জামাত: ${nextPrayer.jamaatFormatted12h}",
                hasWomenPrayerSpace = true,
                hasParking = true
            ),
            NearbyMosqueItem(
                id = "${district.id}_baitul_aman",
                nameBn = "বায়তুল আমান জামে মসজিদ, ${district.nameBn}",
                nameEn = "Baitul Aman Jame Masjid, ${district.nameEn}",
                areaBn = "আবাসিক এলাকা, ${district.nameBn}",
                distanceKm = 1.6,
                nextJamaatLabel = "${nextPrayer.type.nameBn} জামাত: ${nextPrayer.jamaatFormatted12h}",
                hasWomenPrayerSpace = false,
                hasParking = true
            ),
            NearbyMosqueItem(
                id = "${district.id}_baitun_noor",
                nameBn = "মসজিদে বায়তুন নূর, ${district.nameBn}",
                nameEn = "Masjid-e-Baitun Noor, ${district.nameEn}",
                areaBn = "উত্তর পাড়া, ${district.nameBn}",
                distanceKm = 2.3,
                nextJamaatLabel = "${nextPrayer.type.nameBn} জামাত: ${nextPrayer.jamaatFormatted12h}",
                hasWomenPrayerSpace = false,
                hasParking = false
            )
        )
    }
}
