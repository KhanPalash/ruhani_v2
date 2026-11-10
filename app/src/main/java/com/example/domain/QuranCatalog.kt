package com.example.domain

data class WordByWordItem(
    val arabic: String,
    val transliteration: String,
    val meaningBn: String,
    val meaningEn: String
)

data class QuranAyah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val juzNumber: Int,
    val pageNumber: Int,
    val arabicText: String,
    val transliteration: String,
    val translationBn: String,
    val translationEn: String,
    val tafsirBn: String,
    val tafsirSource: String = "তাফসীর ইবনে কাসীর ও আহসানুল বায়ান",
    val words: List<WordByWordItem> = emptyList()
)

data class QuranSurah(
    val number: Int,
    val nameArabic: String,
    val nameEn: String,
    val nameBn: String,
    val meaningBn: String,
    val meaningEn: String,
    val revelationType: String, // "Makki" or "Madani"
    val revelationBn: String,   // "মাক্কী" or "মাদানী"
    val totalAyahs: Int,
    val rukuCount: Int,
    val juzStart: Int,
    val ayahs: List<QuranAyah>
)

data class QariInfo(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val style: String
)

enum class MushafMode(val titleBn: String, val titleEn: String) {
    AYAH_BY_AYAH("আয়াত ভিত্তিক (অনুবাদসহ)", "Ayah by Ayah"),
    WORD_BY_WORD("শব্দে-শব্দে অনুবাদ", "Word by Word"),
    NOORANI_MUSHAF("নূরানী মুশাফ (১৫ লাইন)", "Noorani 15-Line"),
    HIFZ_MODE("হিফজ ও মেমোরাইজেশন", "Hifz Practice")
}

object QuranCatalog {

    val qariList = listOf(
        QariInfo("alafasy", "Mishary Rashid Alafasy", "মিশারি রশিদ আল-আফাসি", "Murattal • High Clarity"),
        QariInfo("sudais", "Abdur-Rahman as-Sudais", "আব্দুর রহমান আস-সুদাইস", "Haramain • Makkah"),
        QariInfo("husary", "Mahmoud Khalil Al-Husary", "মাহমুদ খলিল আল-হুসারি", "Tajweed • Muallim"),
        QariInfo("abdulbasit", "AbdulBaset AbdulSamad", "আব্দুল বাসিত আব্দুস সামাদ", "Mujawwad • Classic")
    )

    val surahs: List<QuranSurah> = listOf(
        QuranSurah(
            number = 1,
            nameArabic = "سُورَةُ ٱلْفَاتِحَةِ",
            nameEn = "Surah Al-Fatihah",
            nameBn = "সূরা আল-ফাতিহা",
            meaningBn = "সূচনা / উন্মোচন",
            meaningEn = "The Opening",
            revelationType = "Makki",
            revelationBn = "মাক্কী",
            totalAyahs = 7,
            rukuCount = 1,
            juzStart = 1,
            ayahs = listOf(
                QuranAyah(
                    surahNumber = 1,
                    ayahNumber = 1,
                    juzNumber = 1,
                    pageNumber = 1,
                    arabicText = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                    transliteration = "Bismillahir-Rahmanir-Rahim",
                    translationBn = "পরম করুণাময়, অসীম দয়ালু আল্লাহর নামে (শুরু করছি)।",
                    translationEn = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
                    tafsirBn = "আল্লাহর পবিত্র নাম স্মরণ করে প্রতিটি উত্তম কাজ শুরু করার শিক্ষা দেওয়া হয়েছে। ‘রহমান’ ও ‘রহীম’ আল্লাহর দুটি গুণবাচক নাম যা তাঁর অসীম দয়ার পরিচয় বহন করে।",
                    words = listOf(
                        WordByWordItem("بِسْمِ", "Bismi", "নামে", "In the name"),
                        WordByWordItem("ٱللَّهِ", "Allahi", "আল্লাহর", "of Allah"),
                        WordByWordItem("ٱلرَّحْمَـٰنِ", "Ar-Rahmani", "পরম করুণাময়", "the Entirely Merciful"),
                        WordByWordItem("ٱلرَّحِيمِ", "Ar-Rahim", "অসীম দয়ালু", "the Especially Merciful")
                    )
                ),
                QuranAyah(
                    surahNumber = 1,
                    ayahNumber = 2,
                    juzNumber = 1,
                    pageNumber = 1,
                    arabicText = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ",
                    transliteration = "Al-hamdu lillahi rabbil-'alamin",
                    translationBn = "সকল প্রশংসা ও কৃতজ্ঞতা একমাত্র আল্লাহর জন্য, যিনি বিশ্বজগতের রব (প্রতিপালক)।",
                    translationEn = "All praise is due to Allah, Lord of the worlds.",
                    tafsirBn = "সমস্ত সৃষ্টিজগতের সৃষ্টি, লালন-পালন ও পরিচালনার একচ্ছত্র অধিপতি মহান আল্লাহ। তাই সকল প্রশংসা ও কৃতজ্ঞতা কেবল তাঁরই প্রাপ্য।",
                    words = listOf(
                        WordByWordItem("ٱلْحَمْدُ", "Al-hamdu", "সকল প্রশংসা", "All praise"),
                        WordByWordItem("لِلَّهِ", "lillahi", "আল্লাহর জন্য", "is for Allah"),
                        WordByWordItem("رَبِّ", "Rabbi", "প্রতিপালক", "Lord"),
                        WordByWordItem("ٱلْعَـٰلَمِينَ", "al-'alamin", "বিশ্বজগতের", "of the worlds")
                    )
                ),
                QuranAyah(
                    surahNumber = 1,
                    ayahNumber = 3,
                    juzNumber = 1,
                    pageNumber = 1,
                    arabicText = "ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                    transliteration = "Ar-Rahmanir-Rahim",
                    translationBn = "যিনি পরম করুণাময়, অতি দয়ালু।",
                    translationEn = "The Entirely Merciful, the Especially Merciful.",
                    tafsirBn = "আল্লাহর প্রতিপালন ভীতিপ্রদ নয় বরং দয়া ও করুণায় পরিপূর্ণ—এই আয়াতে তা পুনরায় স্মরণ করিয়ে দেওয়া হয়েছে।",
                    words = listOf(
                        WordByWordItem("ٱلرَّحْمَـٰنِ", "Ar-Rahmani", "পরম করুণাময়", "The Entirely Merciful"),
                        WordByWordItem("ٱلرَّحِيمِ", "Ar-Rahim", "অতি দয়ালু", "the Especially Merciful")
                    )
                ),
                QuranAyah(
                    surahNumber = 1,
                    ayahNumber = 4,
                    juzNumber = 1,
                    pageNumber = 1,
                    arabicText = "مَـٰلِكِ يَوْمِ ٱلدِّينِ",
                    transliteration = "Maliki yawmid-din",
                    translationBn = "যিনি বিচার দিনের (প্রতিফল দিবসের) মালিক।",
                    translationEn = "Sovereign of the Day of Recompense.",
                    tafsirBn = "কিয়ামতের দিন কারো কোনো ক্ষমতা থাকবে না; সেদিন চূড়ান্ত বিচার ও প্রতিফল দানের মালিক একমাত্র আল্লাহ।",
                    words = listOf(
                        WordByWordItem("مَـٰلِكِ", "Maliki", "মালিক / অধিপতি", "Master / Sovereign"),
                        WordByWordItem("يَوْمِ", "yawmi", "দিনের", "of the Day"),
                        WordByWordItem("ٱلدِّينِ", "ad-din", "বিচারের / প্রতিফলের", "of Judgment")
                    )
                ),
                QuranAyah(
                    surahNumber = 1,
                    ayahNumber = 5,
                    juzNumber = 1,
                    pageNumber = 1,
                    arabicText = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
                    transliteration = "Iyyaka na'budu wa iyyaka nasta'in",
                    translationBn = "আমরা কেবল আপনারই ইবাদত করি এবং কেবল আপনারই কাছে সাহায্য চাই।",
                    translationEn = "It is You we worship and You we ask for help.",
                    tafsirBn = "তাওহীদের মূল ভিত্তি হলো ইবাদত ও সাহায্য প্রার্থনা শুধুমাত্র আল্লাহর জন্য একনিষ্ঠ করা।",
                    words = listOf(
                        WordByWordItem("إِيَّاكَ", "Iyyaka", "কেবল আপনারই", "You alone"),
                        WordByWordItem("نَعْبُدُ", "na'budu", "আমরা ইবাদত করি", "we worship"),
                        WordByWordItem("وَإِيَّاكَ", "wa iyyaka", "এবং কেবল আপনারই", "and You alone"),
                        WordByWordItem("نَسْتَعِينُ", "nasta'in", "আমরা সাহায্য চাই", "we ask for help")
                    )
                ),
                QuranAyah(
                    surahNumber = 1,
                    ayahNumber = 6,
                    juzNumber = 1,
                    pageNumber = 1,
                    arabicText = "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ",
                    transliteration = "Ihdinas-siratal-mustaqim",
                    translationBn = "আমাদেরকে সরল ও সঠিক পথ প্রদর্শন করুন।",
                    translationEn = "Guide us to the straight path.",
                    tafsirBn = "মানুষের সবচেয়ে বড় প্রয়োজন হলো হিদায়াত বা সরল পথের দিশা পাওয়া এবং তার ওপর অবিচল থাকা।",
                    words = listOf(
                        WordByWordItem("ٱهْدِنَا", "Ihdina", "আমাদের পথ দেখান", "Guide us"),
                        WordByWordItem("ٱلصِّرَٰطَ", "as-sirata", "পথ", "to the path"),
                        WordByWordItem("ٱلْمُسْتَقِيمَ", "al-mustaqim", "সরল / সঠিক", "the straight")
                    )
                ),
                QuranAyah(
                    surahNumber = 1,
                    ayahNumber = 7,
                    juzNumber = 1,
                    pageNumber = 1,
                    arabicText = "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ",
                    transliteration = "Siratal-ladhina an'amta 'alayhim ghayril-maghdubi 'alayhim wa lad-dallin",
                    translationBn = "তাদের পথ, যাদেরকে আপনি নিআমত দান করেছেন; তাদের পথ নয়, যাদের ওপর আপনার গজব নিপতিত হয়েছে এবং যারা পথভ্রষ্ট হয়েছে।",
                    translationEn = "The path of those upon whom You have bestowed favor, not of those who have evoked anger or of those who are astray.",
                    tafsirBn = "নবী, সিদ্দিক, শহীদ ও নেককার বান্দাদের পথই হলো নিআমতপ্রাপ্তদের পথ। সত্য জেনেও তা বর্জনকারী এবং না জেনে গোমরাহীতে লিপ্তদের পথ থেকে বাঁচার দোয়া এটি।",
                    words = listOf(
                        WordByWordItem("صِرَٰطَ", "Sirata", "পথ", "The path"),
                        WordByWordItem("ٱلَّذِينَ", "alladhina", "তাদের যাদের", "of those"),
                        WordByWordItem("أَنْعَمْتَ", "an'amta", "আপনি অনুগ্রহ করেছেন", "You have favored"),
                        WordByWordItem("عَلَيْهِمْ", "'alayhim", "তাদের ওপর", "upon them"),
                        WordByWordItem("غَيْرِ", "ghayri", "ভিন্ন / ছাড়া", "not of"),
                        WordByWordItem("ٱلْمَغْضُوبِ", "al-maghdubi", "অভিশপ্ত / ক্রোধভাজন", "those who earned wrath"),
                        WordByWordItem("وَلَا ٱلضَّآلِّينَ", "wa lad-dallin", "এবং পথভ্রষ্টদেরও নয়", "nor those who went astray")
                    )
                )
            )
        ),
        QuranSurah(
            number = 2,
            nameArabic = "سُورَةُ ٱلْبَقَرَةِ",
            nameEn = "Surah Al-Baqarah",
            nameBn = "সূরা আল-বাকারাহ",
            meaningBn = "বকনা-বাছুর",
            meaningEn = "The Cow",
            revelationType = "Madani",
            revelationBn = "মাদানী",
            totalAyahs = 286,
            rukuCount = 40,
            juzStart = 1,
            ayahs = listOf(
                QuranAyah(
                    surahNumber = 2,
                    ayahNumber = 1,
                    juzNumber = 1,
                    pageNumber = 2,
                    arabicText = "الٓمٓ",
                    transliteration = "Alif-Lam-Mim",
                    translationBn = "আলিফ-লাম-মীম।",
                    translationEn = "Alif, Lam, Meem.",
                    tafsirBn = "এগুলো হুরুফে মুকাত্তাআত বা বিচ্ছিন্ন বর্ণমালা। এর প্রকৃত অর্থ আল্লাহই সবচেয়ে ভালো জানেন।",
                    words = listOf(
                        WordByWordItem("الٓمٓ", "Alif-Lam-Mim", "আলিফ-লাম-মীম", "Alif Lam Meem")
                    )
                ),
                QuranAyah(
                    surahNumber = 2,
                    ayahNumber = 2,
                    juzNumber = 1,
                    pageNumber = 2,
                    arabicText = "ذَٰلِكَ ٱلْكِتَـٰبُ لَا رَيْبَ  فِيهِ  هُدًى لِّلْمُتَّقِينَ",
                    transliteration = "Dhalikal-kitabu la rayba fihi hudan lil-muttaqin",
                    translationBn = "এই সেই কিতাব, যাতে কোনো সন্দেহ নেই; মুত্তাকীদের জন্য এটি পথনির্দেশ।",
                    translationEn = "This is the Book about which there is no doubt, a guidance for those conscious of Allah.",
                    tafsirBn = "পবিত্র কুরআন আল্লাহর পক্ষ থেকে অবতীর্ণ অভ্রান্ত গ্রন্থ। যারা আল্লাহকে ভয় করে ও পাপ থেকে বাঁচে, তারা এর মাধ্যমে পূর্ণ হিদায়াত লাভ করে।",
                    words = listOf(
                        WordByWordItem("ذَٰلِكَ", "Dhalika", "এই সেই", "That / This"),
                        WordByWordItem("ٱلْكِتَـٰبُ", "al-kitabu", "কিতাব / গ্রন্থ", "is the Book"),
                        WordByWordItem("لَا رَيْبَ", "la rayba", "কোনো সন্দেহ নেই", "no doubt"),
                        WordByWordItem("فِيهِ", "fihi", "এর মধ্যে", "in it"),
                        WordByWordItem("هُدًى", "hudan", "পথনির্দেশ", "guidance"),
                        WordByWordItem("لِّلْمُتَّقِينَ", "lil-muttaqin", "মুত্তাকীদের জন্য", "for the God-conscious")
                    )
                ),
                QuranAyah(
                    surahNumber = 2,
                    ayahNumber = 255,
                    juzNumber = 3,
                    pageNumber = 42,
                    arabicText = "ٱللَّهُ لَآ إِلَـٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ ۚ لَا تَأْخُذُهُۥ سِنَةٌ وَلَا نَوْمٌ",
                    transliteration = "Allahu la ilaha illa huwal-hayyul-qayyum, la ta'khudhuhu sinatun wa la nawm",
                    translationBn = "আল্লাহ—তিনি ছাড়া কোনো সত্য ইলাহ নেই। তিনি চিরঞ্জীব, সর্বসত্তার ধারক। তাঁকে তন্দ্রা ও নিদ্রা স্পর্শ করে না। (আয়াতুল কুরসি)",
                    translationEn = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep.",
                    tafsirBn = "এটি মহিমান্বিত আয়াতুল কুরসি—কুরআনের সর্বশ্রেষ্ঠ আয়াত। প্রত্যেক ফরজ নামাজের পর এটি পাঠ করলে জান্নাতে প্রবেশের পথে মৃত্যু ছাড়া আর কোনো বাধা থাকে না।",
                    words = listOf(
                        WordByWordItem("ٱللَّهُ", "Allahu", "আল্লাহ", "Allah"),
                        WordByWordItem("لَآ إِلَـٰهَ", "la ilaha", "কোনো ইলাহ নেই", "there is no deity"),
                        WordByWordItem("إِلَّا هُوَ", "illa huwa", "তিনি ছাড়া", "except Him"),
                        WordByWordItem("ٱلْحَىُّ", "al-Hayyu", "চিরঞ্জীব", "the Ever-Living"),
                        WordByWordItem("ٱلْقَيُّومُ", "al-Qayyum", "সর্বসত্তার ধারক", "the Sustainer")
                    )
                ),
                QuranAyah(
                    surahNumber = 2,
                    ayahNumber = 286,
                    juzNumber = 3,
                    pageNumber = 49,
                    arabicText = "لَا يُكَلِّفُ ٱللَّهُ نَفْسًا إِلَّا وُسْعَهَا ۚ لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا ٱكْتَسَبَتْ",
                    transliteration = "La yukallifullahu nafsan illa wus'aha, laha ma kasabat wa 'alayha maktasabat",
                    translationBn = "আল্লাহ কোনো ব্যক্তিকে তার সামর্থ্যের বাইরে দায়িত্ব চাপিয়ে দেন না। সে ভালো যা উপার্জন করে তা তারই কল্যাণে আসে, আর মন্দ যা করে তা তারই বিরুদ্ধে যায়।",
                    translationEn = "Allah does not charge a soul except within its capacity. It will have the consequence of what good it has gained, and it will bear the consequence of what evil it has earned.",
                    tafsirBn = "সূরা বাকারার শেষ দুই আয়াত আরশের নিচের খাজানা থেকে দান করা হয়েছে। রাতে এ দুটি আয়াত তিলাওয়াত করলে তা বান্দার জন্য যথেষ্ট হয়ে যায়।",
                    words = listOf(
                        WordByWordItem("لَا يُكَلِّفُ", "La yukallifu", "বোঝা চাপান না", "Does not burden"),
                        WordByWordItem("ٱللَّهُ", "Allahu", "আল্লাহ", "Allah"),
                        WordByWordItem("نَفْسًا", "nafsan", "কোনো প্রাণীকে", "any soul"),
                        WordByWordItem("إِلَّا وُسْعَهَا", "illa wus'aha", "তার সাধ্যের বাইরে", "beyond its capacity")
                    )
                )
            )
        ),
        QuranSurah(
            number = 36,
            nameArabic = "سُورَةُ يسٓ",
            nameEn = "Surah Ya-Sin",
            nameBn = "সূরা ইয়াসিন",
            meaningBn = "ইয়াসিন",
            meaningEn = "Ya-Sin",
            revelationType = "Makki",
            revelationBn = "মাক্কী",
            totalAyahs = 83,
            rukuCount = 5,
            juzStart = 22,
            ayahs = listOf(
                QuranAyah(
                    surahNumber = 36,
                    ayahNumber = 1,
                    juzNumber = 22,
                    pageNumber = 440,
                    arabicText = "يسٓ",
                    transliteration = "Ya-Sin",
                    translationBn = "ইয়াসিন।",
                    translationEn = "Ya, Seen.",
                    tafsirBn = "হুরুফে মুকাত্তাআত। কুরআনের অলৌকিকত্বের নিদর্শন।",
                    words = listOf(WordByWordItem("يسٓ", "Ya-Sin", "ইয়াসিন", "Ya-Sin"))
                ),
                QuranAyah(
                    surahNumber = 36,
                    ayahNumber = 2,
                    juzNumber = 22,
                    pageNumber = 440,
                    arabicText = "وَٱلْقُرْءَانِ ٱلْحَكِيمِ",
                    transliteration = "Wal-Qur'anil-Hakim",
                    translationBn = "প্রজ্ঞাময় কুরআনের শপথ।",
                    translationEn = "By the wise Qur'an.",
                    tafsirBn = "আল্লাহ প্রজ্ঞাপূর্ণ কুরআনের শপথ করে রাসূলুল্লাহ (ﷺ)-এর রিসালাতের সত্যতা ঘোষণা করেছেন।",
                    words = listOf(
                        WordByWordItem("وَٱلْقُرْءَانِ", "Wal-Qur'ani", "শপথ কুরআনের", "By the Quran"),
                        WordByWordItem("ٱلْحَكِيمِ", "al-Hakim", "প্রজ্ঞাময়", "the Wise")
                    )
                ),
                QuranAyah(
                    surahNumber = 36,
                    ayahNumber = 3,
                    juzNumber = 22,
                    pageNumber = 440,
                    arabicText = "إِنَّكَ لَمِنَ ٱلْمُرْسَلِينَ",
                    transliteration = "Innaka laminal-mursalin",
                    translationBn = "নিশ্চয় আপনি প্রেরিত রাসূলগণের একজন।",
                    translationEn = "Indeed you, [O Muhammad], are from among the messengers,",
                    tafsirBn = "মক্কার কাফিররা নবীজীর রিসালাত অস্বীকার করত; আল্লাহ কসম করে তাঁর নবুওয়াতের সাক্ষ্য দিয়েছেন।",
                    words = listOf(
                        WordByWordItem("إِنَّكَ", "Innaka", "নিশ্চয় আপনি", "Indeed you"),
                        WordByWordItem("لَمِنَ", "lamina", "অবশ্যই অন্তর্ভুক্ত", "are surely among"),
                        WordByWordItem("ٱلْمُرْسَلِينَ", "al-mursalin", "রাসূলগণের", "the messengers")
                    )
                ),
                QuranAyah(
                    surahNumber = 36,
                    ayahNumber = 58,
                    juzNumber = 23,
                    pageNumber = 444,
                    arabicText = "سَلَـٰمٌ قَوْلًا مِّن رَّبٍّ رَّحِيمٍ",
                    transliteration = "Salamun qawlam-mir-rabbir-rahim",
                    translationBn = "করুণাময় রবের পক্ষ থেকে তাদেরকে বলা হবে ‘সালাম’ (শান্তি)।",
                    translationEn = "Peace - a word from a Merciful Lord.",
                    tafsirBn = "জান্নাতবাসীদের জন্য সবচেয়ে বড় সম্মান ও আনন্দ হবে স্বয়ং রাব্বুল আলামীনের পক্ষ থেকে সালামের ঘোষণা।",
                    words = listOf(
                        WordByWordItem("سَلَـٰمٌ", "Salamun", "শান্তি / সালাম", "Peace"),
                        WordByWordItem("قَوْلًا", "qawlan", "বাণী", "a word"),
                        WordByWordItem("مِّن رَّبٍّ", "min Rabbin", "রবের পক্ষ থেকে", "from a Lord"),
                        WordByWordItem("رَّحِيمٍ", "Rahim", "পরম দয়ালু", "Merciful")
                    )
                )
            )
        ),
        QuranSurah(
            number = 55,
            nameArabic = "سُورَةُ ٱلرَّحْمَـٰنِ",
            nameEn = "Surah Ar-Rahman",
            nameBn = "সূরা আর-রহমান",
            meaningBn = "পরম করুণাময়",
            meaningEn = "The Most Merciful",
            revelationType = "Madani",
            revelationBn = "মাদানী",
            totalAyahs = 78,
            rukuCount = 3,
            juzStart = 27,
            ayahs = listOf(
                QuranAyah(
                    surahNumber = 55,
                    ayahNumber = 1,
                    juzNumber = 27,
                    pageNumber = 531,
                    arabicText = "ٱلرَّحْمَـٰنُ",
                    transliteration = "Ar-Rahman",
                    translationBn = "পরম করুণাময় (আল্লাহ)।",
                    translationEn = "The Most Merciful",
                    tafsirBn = "আল্লাহ তাঁর ‘রহমান’ নামের উল্লেখ করে মানবজাতির প্রতি তাঁর সর্বশ্রেষ্ঠ অনুগ্রহ কুরআন শিক্ষার কথা বর্ণনা করেছেন।",
                    words = listOf(WordByWordItem("ٱلرَّحْمَـٰنُ", "Ar-Rahman", "পরম করুণাময়", "The Most Merciful"))
                ),
                QuranAyah(
                    surahNumber = 55,
                    ayahNumber = 2,
                    juzNumber = 27,
                    pageNumber = 531,
                    arabicText = "عَلَّمَ ٱلْقُرْءَانَ",
                    transliteration = "'Allamal-Qur'an",
                    translationBn = "তিনিই কুরআন শিক্ষা দিয়েছেন।",
                    translationEn = "Taught the Qur'an,",
                    tafsirBn = "কুরআনের জ্ঞান দান করা আল্লাহর করুণার সবচেয়ে বড় প্রকাশ।",
                    words = listOf(
                        WordByWordItem("عَلَّمَ", "'Allama", "শিক্ষা দিয়েছেন", "Taught"),
                        WordByWordItem("ٱلْقُرْءَانَ", "al-Qur'an", "কুরআন", "the Quran")
                    )
                ),
                QuranAyah(
                    surahNumber = 55,
                    ayahNumber = 13,
                    juzNumber = 27,
                    pageNumber = 531,
                    arabicText = "فَبِأَىِّ ءَالَآءِ رَبِّكُمَا تُكَذِّبَانِ",
                    transliteration = "Fabi'ayyi ala'i rabbikuma tukadhdhiban",
                    translationBn = "অতএব, তোমরা উভয়ে (মানুষ ও জিন) তোমাদের রবের কোন কোন অনুগ্রহকে অস্বীকার করবে?",
                    translationEn = "So which of the favors of your Lord would you deny?",
                    tafsirBn = "এই সূরায় ৩১ বার এই আয়াতটি পুনরাবৃত্তি করে আল্লাহ তাঁর অগণিত নিআমতের প্রতি কৃতজ্ঞ হওয়ার আহ্বান জানিয়েছেন।",
                    words = listOf(
                        WordByWordItem("فَبِأَىِّ", "Fabi'ayyi", "তবে কোন", "So which"),
                        WordByWordItem("ءَالَآءِ", "ala'i", "অনুগ্রহসমূহ", "of the favors"),
                        WordByWordItem("رَبِّكُمَا", "rabbikuma", "তোমাদের রবের", "of your Lord"),
                        WordByWordItem("تُكَذِّبَانِ", "tukadhdhiban", "তোমরা অস্বীকার করবে", "will you deny")
                    )
                )
            )
        ),
        QuranSurah(
            number = 67,
            nameArabic = "سُورَةُ ٱلْمُلْكِ",
            nameEn = "Surah Al-Mulk",
            nameBn = "সূরা আল-মুলক",
            meaningBn = "সার্বভৌম কর্তৃত্ব",
            meaningEn = "The Sovereignty",
            revelationType = "Makki",
            revelationBn = "মাক্কী",
            totalAyahs = 30,
            rukuCount = 2,
            juzStart = 29,
            ayahs = listOf(
                QuranAyah(
                    surahNumber = 67,
                    ayahNumber = 1,
                    juzNumber = 29,
                    pageNumber = 562,
                    arabicText = "تَبَـٰرَكَ ٱلَّذِى بِيَدِهِ ٱلْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَىْءٍ قَدِيرٌ",
                    transliteration = "Tabarakalladhi biyadihil-mulku wa huwa 'ala kulli shay'in qadir",
                    translationBn = "মহিমান্বিত তিনি, যাঁর হাতে রাজত্ব ও সার্বভৌম কর্তৃত্ব; আর তিনি সব কিছুর ওপর সর্বশক্তিমান।",
                    translationEn = "Blessed is He in whose hand is dominion, and He is over all things competent.",
                    tafsirBn = "হাদীসে এসেছে, সূরা মুলক হলো কবরের আজাব থেকে রক্ষাকারী (আল-মানিআহ)। প্রতি রাতে এটি তিলাওয়াত করা সুন্নত।",
                    words = listOf(
                        WordByWordItem("تَبَـٰرَكَ", "Tabaraka", "বরকতময় / মহিমান্বিত", "Blessed is"),
                        WordByWordItem("ٱلَّذِى", "alladhi", "যিনি", "He Who"),
                        WordByWordItem("بِيَدِهِ", "biyadihi", "যাঁর হাতে", "in His hand"),
                        WordByWordItem("ٱلْمُلْكُ", "al-mulku", "সার্বভৌম রাজত্ব", "is the dominion")
                    )
                ),
                QuranAyah(
                    surahNumber = 67,
                    ayahNumber = 2,
                    juzNumber = 29,
                    pageNumber = 562,
                    arabicText = "ٱلَّذِى خَلَقَ ٱلْمَوْتَ وَٱلْحَيَوٰةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ ٱلْعَزِيزُ ٱلْغَفُورُ",
                    transliteration = "Alladhi khalaqal-mawta wal-hayata liyabluwakum ayyukum ahsanu 'amala",
                    translationBn = "যিনি সৃষ্টি করেছেন মরণ ও জীবন, যাতে তোমাদেরকে পরীক্ষা করেন—কে তোমাদের মধ্যে আমলে সর্বোত্তম? আর তিনি পরাক্রমশালী, পরম ক্ষমাশীল।",
                    translationEn = "[He] who created death and life to test you [as to] which of you is best in deed - and He is the Exalted in Might, the Forgiving.",
                    tafsirBn = "আমলের সংখ্যার চেয়ে আমলের বিশুদ্ধতা (ইখলাস ও সুন্নাহর অনুসরণ) আল্লাহর কাছে অধিক গুরুত্বপূর্ণ।",
                    words = listOf(
                        WordByWordItem("خَلَقَ", "khalaqa", "সৃষ্টি করেছেন", "created"),
                        WordByWordItem("ٱلْمَوْتَ", "al-mawta", "মৃত্যু", "death"),
                        WordByWordItem("وَٱلْحَيَوٰةَ", "wal-hayata", "এবং জীবন", "and life"),
                        WordByWordItem("لِيَبْلُوَكُمْ", "liyabluwakum", "তোমাদের পরীক্ষার জন্য", "to test you")
                    )
                )
            )
        ),
        QuranSurah(
            number = 112,
            nameArabic = "سُورَةُ ٱلْإِخْلَاصِ",
            nameEn = "Surah Al-Ikhlas",
            nameBn = "সূরা আল-ইখলাস",
            meaningBn = "একনিষ্ঠতা / তাওহীদ",
            meaningEn = "The Sincerity",
            revelationType = "Makki",
            revelationBn = "মাক্কী",
            totalAyahs = 4,
            rukuCount = 1,
            juzStart = 30,
            ayahs = listOf(
                QuranAyah(
                    surahNumber = 112,
                    ayahNumber = 1,
                    juzNumber = 30,
                    pageNumber = 604,
                    arabicText = "قُلْ هُوَ ٱللَّهُ أَحَدٌ",
                    transliteration = "Qul huwallahu ahad",
                    translationBn = "বলুন, তিনি আল্লাহ, এক ও অদ্বিতীয়।",
                    translationEn = "Say, 'He is Allah, [who is] One,'",
                    tafsirBn = "সূরা ইখলাস কুরআনের এক-তৃতীয়াংশের সমতুল্য। এতে আল্লাহর একত্ববাদের পূর্ণাঙ্গ পরিচয় তুলে ধরা হয়েছে।",
                    words = listOf(
                        WordByWordItem("قُلْ", "Qul", "বলুন", "Say"),
                        WordByWordItem("هُوَ", "Huwa", "তিনি", "He is"),
                        WordByWordItem("ٱللَّهُ", "Allahu", "আল্লাহ", "Allah"),
                        WordByWordItem("أَحَدٌ", "Ahad", "এক ও অদ্বিতীয়", "the One")
                    )
                ),
                QuranAyah(
                    surahNumber = 112,
                    ayahNumber = 2,
                    juzNumber = 30,
                    pageNumber = 604,
                    arabicText = "ٱللَّهُ ٱلصَّمَدُ",
                    transliteration = "Allahus-samad",
                    translationBn = "আল্লাহ অমুখাপেক্ষী (সকলেই তাঁর মুখাপেক্ষী)।",
                    translationEn = "Allah, the Eternal Refuge.",
                    tafsirBn = "‘আস-সামাদ’ অর্থ এমন সত্তা যাঁর কাছে সবাই প্রয়োজনে আশ্রয় চায়, কিন্তু তিনি কারো মুখাপেক্ষী নন।",
                    words = listOf(
                        WordByWordItem("ٱللَّهُ", "Allahu", "আল্লাহ", "Allah"),
                        WordByWordItem("ٱلصَّمَدُ", "as-Samad", "অমুখাপেক্ষী", "the Eternal Refuge")
                    )
                ),
                QuranAyah(
                    surahNumber = 112,
                    ayahNumber = 3,
                    juzNumber = 30,
                    pageNumber = 604,
                    arabicText = "لَمْ يَلِدْ وَلَمْ يُولَدْ",
                    transliteration = "Lam yalid wa lam yulad",
                    translationBn = "তিনি কাউকে জন্ম দেননি এবং তাঁকেও জন্ম দেওয়া হয়নি।",
                    translationEn = "He neither begets nor is born,",
                    tafsirBn = "আল্লাহ সন্তান ও পিতা-মাতা থেকে সম্পূর্ণ পবিত্র ও ঊর্ধ্বে।",
                    words = listOf(
                        WordByWordItem("لَمْ يَلِدْ", "Lam yalid", "জন্ম দেননি", "He neither begets"),
                        WordByWordItem("وَلَمْ يُولَدْ", "wa lam yulad", "জন্ম নেননি", "nor is born")
                    )
                ),
                QuranAyah(
                    surahNumber = 112,
                    ayahNumber = 4,
                    juzNumber = 30,
                    pageNumber = 604,
                    arabicText = "وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌۢ",
                    transliteration = "Wa lam yakul-lahu kufuwan ahad",
                    translationBn = "এবং তাঁর সমতুল্য কেউই নেই।",
                    translationEn = "Nor is there to Him any equivalent.",
                    tafsirBn = "সত্তা, গুণাবলি ও ক্ষমতায় আল্লাহর কোনো সমকক্ষ বা তুলনা নেই।",
                    words = listOf(
                        WordByWordItem("وَلَمْ يَكُن", "Wa lam yakun", "এবং নেই", "And there is not"),
                        WordByWordItem("لَّهُۥ", "lahu", "তাঁর জন্য", "unto Him"),
                        WordByWordItem("كُفُوًا", "kufuwan", "সমতুল্য", "equivalent"),
                        WordByWordItem("أَحَدٌۢ", "ahad", "কেউই", "anyone")
                    )
                )
            )
        ),
        QuranSurah(
            number = 113,
            nameArabic = "سُورَةُ ٱلْفَلَقِ",
            nameEn = "Surah Al-Falaq",
            nameBn = "সূরা আল-ফালাক",
            meaningBn = "ঊষা / ভোরের আলো",
            meaningEn = "The Daybreak",
            revelationType = "Makki",
            revelationBn = "মাক্কী",
            totalAyahs = 5,
            rukuCount = 1,
            juzStart = 30,
            ayahs = listOf(
                QuranAyah(
                    surahNumber = 113,
                    ayahNumber = 1,
                    juzNumber = 30,
                    pageNumber = 604,
                    arabicText = "قُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ",
                    transliteration = "Qul a'udhu birabbil-falaq",
                    translationBn = "বলুন, আমি আশ্রয় প্রার্থনা করছি ঊষার রবের কাছে।",
                    translationEn = "Say, 'I seek refuge in the Lord of daybreak'",
                    tafsirBn = "সূরা ফালাক ও সূরা নাসকে একত্রে ‘মুআওয়িযাতাইন’ বলা হয়। সকল প্রকার অনিষ্ট, জাদু ও বদনজর থেকে রক্ষার শ্রেষ্ঠ রুকইয়াহ এটি।",
                    words = listOf(
                        WordByWordItem("قُلْ", "Qul", "বলুন", "Say"),
                        WordByWordItem("أَعُوذُ", "a'udhu", "আমি আশ্রয় চাই", "I seek refuge"),
                        WordByWordItem("بِرَبِّ", "birabbi", "রবের কাছে", "in the Lord"),
                        WordByWordItem("ٱلْفَلَقِ", "al-falaq", "ঊষার", "of daybreak")
                    )
                ),
                QuranAyah(
                    surahNumber = 113,
                    ayahNumber = 2,
                    juzNumber = 30,
                    pageNumber = 604,
                    arabicText = "مِن شَرِّ مَا خَلَقَ",
                    transliteration = "Min sharri ma khalaq",
                    translationBn = "তিনি যা সৃষ্টি করেছেন তার অনিষ্ট হতে।",
                    translationEn = "From the evil of that which He created",
                    tafsirBn = "সৃষ্টিজগতের দৃশ্য ও অদৃশ্য সকল অনিষ্ট থেকে আল্লাহর আশ্রয় প্রার্থনা।",
                    words = listOf(
                        WordByWordItem("مِن شَرِّ", "Min sharri", "অনিষ্ট হতে", "From the evil"),
                        WordByWordItem("مَا خَلَقَ", "ma khalaq", "যা তিনি সৃষ্টি করেছেন", "of what He created")
                    )
                )
            )
        ),
        QuranSurah(
            number = 114,
            nameArabic = "سُورَةُ ٱلنَّاسِ",
            nameEn = "Surah An-Nas",
            nameBn = "সূরা আন-নাস",
            meaningBn = "মানবজাতি",
            meaningEn = "Mankind",
            revelationType = "Makki",
            revelationBn = "মাক্কী",
            totalAyahs = 6,
            rukuCount = 1,
            juzStart = 30,
            ayahs = listOf(
                QuranAyah(
                    surahNumber = 114,
                    ayahNumber = 1,
                    juzNumber = 30,
                    pageNumber = 604,
                    arabicText = "قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ",
                    transliteration = "Qul a'udhu birabbin-nas",
                    translationBn = "বলুন, আমি আশ্রয় প্রার্থনা করছি মানুষের রবের কাছে,",
                    translationEn = "Say, 'I seek refuge in the Lord of mankind,'",
                    tafsirBn = "মানুষ ও জিন শয়তানের কুমন্ত্রণা (ওয়াসওয়াসা) থেকে বাঁচতে আল্লাহর তিনটি গুণের উসিলায় আশ্রয় চাওয়া হয়েছে।",
                    words = listOf(
                        WordByWordItem("قُلْ", "Qul", "বলুন", "Say"),
                        WordByWordItem("أَعُوذُ", "a'udhu", "আমি আশ্রয় চাই", "I seek refuge"),
                        WordByWordItem("بِرَبِّ", "birabbi", "রবের কাছে", "in the Lord"),
                        WordByWordItem("ٱلنَّاسِ", "an-nas", "মানুষের", "of mankind")
                    )
                ),
                QuranAyah(
                    surahNumber = 114,
                    ayahNumber = 4,
                    juzNumber = 30,
                    pageNumber = 604,
                    arabicText = "مِن شَرِّ ٱلْوَسْوَاسِ ٱلْخَنَّاسِ",
                    transliteration = "Min sharril-waswasil-khannas",
                    translationBn = "আত্মগোপনকারী কুমন্ত্রণাদাতার অনিষ্ট হতে,",
                    translationEn = "From the evil of the retreating whisperer -",
                    tafsirBn = "বান্দা যখন আল্লাহর যিকির করে তখন শয়তান পিছু হটে, আর গাফেল হলে আবার কুমন্ত্রণা দেয়।",
                    words = listOf(
                        WordByWordItem("مِن شَرِّ", "Min sharri", "অনিষ্ট হতে", "From the evil"),
                        WordByWordItem("ٱلْوَسْوَاسِ", "al-waswasi", "কুমন্ত্রণাদাতার", "of the whisperer"),
                        WordByWordItem("ٱلْخَنَّاسِ", "al-khannas", "পশ্চাদপসরণকারী", "the retreating")
                    )
                )
            )
        )
    )
}
