# Consolidated Islamic App Feature List

This document combines the feature lists found in the folder. Repeated features have been merged into shared entries; app-specific features are retained where they add distinct functionality.

## Quran and Islamic Learning

- **Quran reading, audio, and study:** Full Quran text with Bangla and other translations, including word-by-word Bangla translation where available; tafsir; selectable Mushaf layouts and prints (including Noorani, Hifzi, Kolkata, and 15-line editions); PDF editions; audio recitations from multiple Qaris, including word-by-word playback; Tajweed options; Hifz and memorization support; and topic-based verse browsing.
- **Hadith collections:** Major collections such as Sahih Bukhari and Sahih Muslim, with Bengali translations and Hadith excerpts for study and reminders.
- **Islamic books and references:** Digital libraries of Islamic books in Bangla, Urdu, and English, covering worship, belief, Ramadan, Zakat, family matters, and madrasa curricula.
- **Fatwa and religious guidance:** Answers to everyday religious questions, masail and fatwas, including guidance on purification and transactions; some apps offer scholar Q&A or direct access to a Mufti.

## Prayer and Worship

- **Prayer times and Azan alerts:** Location-based schedules for the five daily prayers, with configurable calculation methods, location settings, notifications, alarms, and widgets.
- **Additional prayer support:** Timings and reminders for Duha, Awabin, and Tahajjud; prayer-time silence settings; and daily Salat tracking.
- **Missed prayer tracking:** Log regular prayers and track or plan the make-up of Qaza prayers.
- **Ramadan and fasting:** Ramadan schedules and countdowns for Suhoor/Sahri, Imsak, and Iftar, plus seasonal worship guidance.
- **Duas, dhikr, and Ruqyah:** Daily and occasion-based Masnoon supplications, Dua-Durud, Dhikr, Manzil, and Quranic/Sunnah-based Ruqyah in text and audio.
- **Tasbih counter:** Digital zikr counter with configurable counting and, in some versions, vibration feedback.
- **Zakat calculator:** Calculate yearly Zakat obligations using precious-metal rates where supported.
- **Zakat and charity support:** Guidance and tools for Zakat and Sadaka, including distribution support where offered.
- **Names of Allah:** The 99 Beautiful Names (Al-Asmaul Husna) with translations and audio recitation.
- **Islamic names:** Name references including meanings of Islamic baby names.
- **Hajj and Umrah guide:** Step-by-step guidance and interactive maps for pilgrimage rites.

## Islamic Dates and Direction

- **Qibla compass:** Compass guidance to the Kaaba.
- **Calendars:** Hijri calendar and date conversion, with Bengali and Gregorian calendar support; includes important Islamic dates and worship schedules.
- **Nearby mosques:** Location-based discovery of nearby prayer spaces.
- **Multi-calendar support:** Some apps include Bangla calendar dates alongside Hijri and Gregorian dates.

## Audio and Live Services

- **Islamic audio library:** Quran recitations, duas, and Ruqyah, with offline playback where available.
- **Haramain live streams:** Video broadcasts from Makkah and Madinah.

## Customization, Access, and Privacy

- **Offline access:** Core text, databases, and selected audio can be stored on-device for use without an internet connection.
- **Personalization:** Light and dark themes, location preferences, prayer calculation settings, and Fiqh school options where available.

## Source Notes

- The folder contained feature lists for **Islami Jindegi**, **Kahf Guard**, **Muslim Day**, **Muslim Nasiha**, and **Muslim Bangla**. The Muslim Nasiha list repeated the same feature set; those duplicates are included only once.
- App-specific or optional capabilities are described as “where available” because they may not exist in every app.


---

## Technical Feature Matrix & Phase Mapping

| ID | Feature Module | Category | Phase | Offline Requirement | Technical Engine / Data Binding |
|---|---|---|---|---|---|
| **F-01** | Welcome, Language & Location Onboarding | Core / Auth | **MVP (P1)** | 100% Local | `preferences` (Dexie) + 64 BD District static JSON + Geolocation API |
| **F-02** | Email & Mobile OTP Authentication | Core / Auth | **MVP (P1)** | Online Required | Supabase Auth (`signInWithOtp`, `verifyOtp`) + Guest-to-Cloud LWW Sync |
| **F-03** | Live Prayer Times & Waqt Countdown | Prayer | **MVP (P1)** | 100% Offline | `adhan-js` engine + `requestAnimationFrame` / 1s ticker + Calculation Profiles |
| **F-04** | Azan Alerts, Reminders & Silent Hint | Prayer | **MVP (P1)** | 100% Offline | `@capacitor/local-notifications` (`allowWhileIdle`) + Custom Azan Audio |
| **F-05** | Nawafil Timings (Duha, Awabin, Tahajjud) | Prayer | **MVP (P1)** | 100% Offline | Derived astronomical offsets from Sunrise, Maghrib, and Fajr |
| **F-06** | Daily Salat & Qaza Prayer Tracker | Prayer | **MVP (P1)** | Offline-First + Sync | `salat_logs` & `qaza_ledger` (Dexie ↔ Supabase `salat_tracker_logs`) |
| **F-07** | Tri-Calendar (Hijri, Bangla, Gregorian) | Dates | **MVP (P1)** | 100% Offline | `Intl.DateTimeFormat` (`islamic-umalqura` + offset) + Bongabdo Engine |
| **F-08** | Qibla Compass & Geodesic Bearing | Direction | **MVP (P1)** | 100% Offline | Great-Circle formula to Kaaba (`21.4225° N, 39.8262° E`) + Magnetometer API |
| **F-09** | Quran Reader (Surah, Juz, Page, Bangla) | Quran | **MVP (P1)** | Offline-First | Bundled Surah/Ayah Bangla DB + Quran.com v4 API (`tafsirs`, `words`) |
| **F-10** | Word-by-Word Bangla & Tafsir View | Quran | **MVP (P1)** | On-Demand Offline | IndexedDB `quran_words` & `tafsirs` store + Download Manager |
| **F-11** | Multi-Qari Audio Player & Offline Audio | Audio | **MVP (P1)** | Streaming + Cache | HTML5 Audio + MediaSession API + OPFS / Capacitor Filesystem |
| **F-12** | Duas, Morning/Evening Dhikr & Ruqyah | Worship | **MVP (P1)** | 100% Offline Text | Bundled Hisnul Muslim & Ruqyah JSON + Audio Playlist support |
| **F-13** | Digital Tasbih Counter | Worship | **MVP (P1)** | 100% Offline | Local state + `@capacitor/haptics` + Reset confirmation modal |
| **F-14** | 99 Names of Allah (Asmaul Husna) | Learning | **MVP (P1)** | 100% Offline | Bundled JSON (Arabic, Bangla meaning, transliteration, audio) |
| **F-15** | Ramadan Dashboard (Sahri, Imsak, Iftar) | Seasonal | **MVP (P1)** | 100% Offline | Dynamic Hijri month detection + Fasting countdown engine |
| **F-16** | Zakat Calculator & Nisab Engine | Worship | **MVP (P1)** | 100% Offline | Asset/Liability calculator + Gold/Silver Nisab (Vori/Gram/Tola) |
| **F-17** | Profile, Theme (Light/Dark) & Settings | Core | **MVP (P1)** | Offline-First + Sync | `next-themes` + Dexie `preferences` ↔ Supabase `profiles` |
| **F-18** | Hadith Collections (Bukhari, Muslim, etc.) | Learning | **Phase 2** | On-Demand Offline | Static Hadith Book Packs (JSON/SQLite) + Full-text search |
| **F-19** | Hifz Mode & Mushaf Print Layouts (15-line/PDF) | Quran | **Phase 2** | On-Demand Offline | Verse-masking UI + Range repeat audio + PDF.js / Mushaf renderer |
| **F-20** | Nearby Mosque Finder | Direction | **Phase 2** | Online + Cache | OpenStreetMap Overpass API (`amenity=place_of_worship`) + Leaflet |
| **F-21** | Haramain 24/7 Live Stream (Makkah/Madinah) | Live | **Phase 2** | Online Only | `hls.js` adaptive stream player + Network error/retry boundaries |
| **F-22** | Islamic Baby Names Dictionary | Learning | **Phase 2** | 100% Offline | Bundled searchable Bangla/Arabic names dataset |
| **F-23** | Islamic Books Library, Fatwa & Hajj Guide | Learning | **Phase 3** | On-Demand Offline | Categorized eBook/Article Packs + Scholar Q&A / Masail index |
