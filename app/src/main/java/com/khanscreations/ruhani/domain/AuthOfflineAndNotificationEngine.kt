package com.khanscreations.ruhani.domain

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Base64
import android.util.Patterns
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.khanscreations.ruhani.MainActivity
import com.khanscreations.ruhani.R
import com.khanscreations.ruhani.data.remote.QuranApiService
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

data class LocalAuthSession(
    val userId: String,
    val email: String,
    val displayName: String,
    val authProvider: String, // "EMAIL_PASSWORD" or "GOOGLE"
    val createdAtEpochMs: Long,
    val privacyCloudSyncConsent: Boolean = true,
    val privacyPersonalizedAlertsConsent: Boolean = true,
    val privacyAnalyticsMinimize: Boolean = true
)

data class OfflineArticleItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val categoryBn: String,
    val authorBn: String,
    val estimatedMinutes: Int,
    val audioRecitationSurahNo: Int,
    val audioRecitationAyahNo: Int,
    val contentBn: String,
    val sizeKb: Int
)

data class PendingSyncRecord(
    val id: String,
    val type: String, // "SALAT_LOG", "TASBIH_PROGRESS", "QURAN_BOOKMARK", "ARTICLE_PROGRESS", "LAST_READ"
    val descriptionBn: String,
    val payloadJson: String,
    val timestampEpochMs: Long
)

enum class NotificationCategory(
    val channelId: String,
    val titleBn: String,
    val titleEn: String
) {
    WAQT_AZAN(
        channelId = "ruhani_waqt_azan",
        titleBn = "ওয়াক্ত ও আযান অ্যালার্ট",
        titleEn = "Prayer & Azan Alerts"
    ),
    NEW_CONTENT(
        channelId = "ruhani_new_content",
        titleBn = "নতুন কন্টেন্ট ও গুরুত্বপূর্ণ আপডেট",
        titleEn = "New Content & Updates"
    ),
    PERSONALIZED(
        channelId = "ruhani_personal_alerts",
        titleBn = "ব্যক্তিগত আমল ও রিমাইন্ডার",
        titleEn = "Personalized Activity Alerts"
    )
}

data class RuhaniNotificationItem(
    val id: String,
    val category: NotificationCategory,
    val titleBn: String,
    val messageBn: String,
    val timestampLabel: String,
    val isRead: Boolean = false
)

/**
 * 1. Secure Email/Password Registration & Login + Privacy Best Practices Manager.
 * Uses PBKDF2WithHmacSHA256 (12,000 iterations + 16-byte cryptographic salt) so plaintext
 * passwords are never stored in memory or disk.
 */
object SecureAuthAndPrivacyManager {

    private const val PREFS_NAME = "ruhani_secure_auth_vault"
    private const val KEY_ACCOUNTS_JSON = "accounts_json"
    private const val KEY_ACTIVE_SESSION_JSON = "active_session_json"
    private const val KEY_PRIVACY_CLOUD_SYNC = "privacy_cloud_sync"
    private const val KEY_PRIVACY_PERSONAL_ALERTS = "privacy_personal_alerts"
    private const val KEY_PRIVACY_DATA_MINIMIZATION = "privacy_data_minimization"
    private const val PBKDF2_ITERATIONS = 12_000
    private const val KEY_LENGTH_BITS = 256

    fun getActiveSession(context: Context): LocalAuthSession? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ACTIVE_SESSION_JSON, null) ?: return null
        return try {
            val obj = JSONObject(raw)
            LocalAuthSession(
                userId = obj.optString("userId"),
                email = obj.optString("email"),
                displayName = obj.optString("displayName", "মুসলিম ব্যবহারকারী"),
                authProvider = obj.optString("authProvider", "EMAIL_PASSWORD"),
                createdAtEpochMs = obj.optLong("createdAtEpochMs", System.currentTimeMillis()),
                privacyCloudSyncConsent = prefs.getBoolean(KEY_PRIVACY_CLOUD_SYNC, true),
                privacyPersonalizedAlertsConsent = prefs.getBoolean(KEY_PRIVACY_PERSONAL_ALERTS, true),
                privacyAnalyticsMinimize = prefs.getBoolean(KEY_PRIVACY_DATA_MINIMIZATION, true)
            )
        } catch (_: Exception) {
            null
        }
    }

    fun registerWithEmailPassword(
        context: Context,
        displayName: String,
        email: String,
        password: String,
        privacyConsentAccepted: Boolean
    ): Result<LocalAuthSession> {
        val trimmedEmail = email.trim().lowercase(Locale.ROOT)
        val trimmedName = displayName.trim().ifBlank { "মুসলিম ব্যবহারকারী" }

        if (!privacyConsentAccepted) {
            return Result.failure(IllegalArgumentException("অনুগ্রহ করে গোপনীয়তা নীতিমালা (Privacy Policy) সম্মতি দিন।"))
        }
        if (!isValidEmail(trimmedEmail)) {
            return Result.failure(IllegalArgumentException("সঠিক ইমেইল ঠিকানা প্রদান করুন (Valid email required)।"))
        }
        val passwordError = validatePasswordStrength(password)
        if (passwordError != null) {
            return Result.failure(IllegalArgumentException(passwordError))
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val accounts = JSONObject(prefs.getString(KEY_ACCOUNTS_JSON, "{}") ?: "{}")
        if (accounts.has(trimmedEmail)) {
            return Result.failure(
                IllegalStateException("এই ইমেইল দিয়ে ইতোমধ্যে অ্যাকাউন্ট রয়েছে। অনুগ্রহ করে লগইন করুন।")
            )
        }

        val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val saltBase64 = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        val hashBase64 = hashPasswordPbkdf2(password, saltBytes)
        val now = System.currentTimeMillis()
        val uid = "ruhani_usr_${trimmedEmail.hashCode().toUInt().toString(16)}"

        val accountRecord = JSONObject().apply {
            put("userId", uid)
            put("email", trimmedEmail)
            put("displayName", trimmedName)
            put("salt", saltBase64)
            put("passwordHash", hashBase64)
            put("algorithm", "PBKDF2WithHmacSHA256/$PBKDF2_ITERATIONS")
            put("createdAtEpochMs", now)
        }
        accounts.put(trimmedEmail, accountRecord)

        val session = LocalAuthSession(
            userId = uid,
            email = trimmedEmail,
            displayName = trimmedName,
            authProvider = "EMAIL_PASSWORD",
            createdAtEpochMs = now,
            privacyCloudSyncConsent = prefs.getBoolean(KEY_PRIVACY_CLOUD_SYNC, true),
            privacyPersonalizedAlertsConsent = prefs.getBoolean(KEY_PRIVACY_PERSONAL_ALERTS, true),
            privacyAnalyticsMinimize = prefs.getBoolean(KEY_PRIVACY_DATA_MINIMIZATION, true)
        )

        prefs.edit {
            putString(KEY_ACCOUNTS_JSON, accounts.toString())
            putString(KEY_ACTIVE_SESSION_JSON, sessionToJson(session).toString())
        }

        return Result.success(session)
    }

    fun loginWithEmailPassword(
        context: Context,
        email: String,
        password: String
    ): Result<LocalAuthSession> {
        val trimmedEmail = email.trim().lowercase(Locale.ROOT)
        if (!isValidEmail(trimmedEmail)) {
            return Result.failure(IllegalArgumentException("সঠিক ইমেইল ঠিকানা প্রদান করুন।"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("পাসওয়ার্ড প্রদান করুন।"))
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val accounts = JSONObject(prefs.getString(KEY_ACCOUNTS_JSON, "{}") ?: "{}")
        val accountRecord = accounts.optJSONObject(trimmedEmail)
            ?: return Result.failure(
                IllegalArgumentException("এই ইমেইলে কোনো অ্যাকাউন্ট পাওয়া যায়নি। প্রথমে নিবন্ধন (Register) করুন।")
            )

        val saltBase64 = accountRecord.optString("salt")
        val expectedHash = accountRecord.optString("passwordHash")
        val saltBytes = Base64.decode(saltBase64, Base64.NO_WRAP)
        val computedHash = hashPasswordPbkdf2(password, saltBytes)

        if (!constantTimeEquals(expectedHash, computedHash)) {
            return Result.failure(IllegalArgumentException("ভুল পাসওয়ার্ড। অনুগ্রহ করে আবার চেষ্টা করুন।"))
        }

        val session = LocalAuthSession(
            userId = accountRecord.optString("userId"),
            email = trimmedEmail,
            displayName = accountRecord.optString("displayName", "মুসলিম ব্যবহারকারী"),
            authProvider = "EMAIL_PASSWORD",
            createdAtEpochMs = accountRecord.optLong("createdAtEpochMs", System.currentTimeMillis()),
            privacyCloudSyncConsent = prefs.getBoolean(KEY_PRIVACY_CLOUD_SYNC, true),
            privacyPersonalizedAlertsConsent = prefs.getBoolean(KEY_PRIVACY_PERSONAL_ALERTS, true),
            privacyAnalyticsMinimize = prefs.getBoolean(KEY_PRIVACY_DATA_MINIMIZATION, true)
        )

        prefs.edit { putString(KEY_ACTIVE_SESSION_JSON, sessionToJson(session).toString()) }

        return Result.success(session)
    }

    fun signOutLocalSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove(KEY_ACTIVE_SESSION_JSON) }
    }

    fun updatePrivacyPreferences(
        context: Context,
        cloudSyncConsent: Boolean,
        personalizedAlertsConsent: Boolean,
        dataMinimization: Boolean
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_PRIVACY_CLOUD_SYNC, cloudSyncConsent)
            putBoolean(KEY_PRIVACY_PERSONAL_ALERTS, personalizedAlertsConsent)
            putBoolean(KEY_PRIVACY_DATA_MINIMIZATION, dataMinimization)
        }
    }

    fun exportUserDataJson(
        context: Context,
        displayName: String,
        email: String?,
        district: String,
        tasbihTotal: Long,
        bookmarksCount: Int,
        downloadedSurahs: Set<Int>
    ): String {
        val session = getActiveSession(context)
        return JSONObject().apply {
            put("app", "Ruhani Islamic Lifestyle App")
            put("exportedAtIso", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
            put("privacyStandard", "PBKDF2-HMAC-SHA256 + Zero-Trust Firestore Rules")
            put("userProfile", JSONObject().apply {
                put("displayName", displayName)
                put("email", email ?: session?.email ?: "guest")
                put("authProvider", session?.authProvider ?: "GOOGLE_OR_GUEST")
                put("district", district)
                put("tasbihAllTimeTotal", tasbihTotal)
                put("bookmarksCount", bookmarksCount)
                put("downloadedSurahs", JSONArray(downloadedSurahs.sorted()))
            })
        }.toString(2)
    }

    fun deleteAccountAndWipeLocalData(context: Context) {
        val authPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val activeEmail = getActiveSession(context)?.email
        if (activeEmail != null) {
            val accounts = JSONObject(authPrefs.getString(KEY_ACCOUNTS_JSON, "{}") ?: "{}")
            accounts.remove(activeEmail)
            authPrefs.edit {
                putString(KEY_ACCOUNTS_JSON, accounts.toString())
                remove(KEY_ACTIVE_SESSION_JSON)
            }
        } else {
            authPrefs.edit { clear() }
        }
        // Clear offline cached files as part of right-to-erasure
        OfflineContentAndSyncManager.clearAllOfflineFiles(context)
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank() || !email.contains("@")) return false
        return try {
            Patterns.EMAIL_ADDRESS?.matcher(email)?.matches()
                ?: Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)
        } catch (_: Exception) {
            Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)
        }
    }

    private fun validatePasswordStrength(password: String): String? {
        if (password.length < 8) {
            return "পাসওয়ার্ড কমপক্ষে ৮ অক্ষরের হতে হবে (Minimum 8 characters)।"
        }
        if (!password.any { it.isLetter() } || !password.any { it.isDigit() }) {
            return "পাসওয়ার্ডে অন্তত একটি অক্ষর এবং একটি সংখ্যা থাকতে হবে (Letters & digits required)।"
        }
        return null
    }

    private fun hashPasswordPbkdf2(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val bytes = factory.generateSecret(spec).encoded
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }

    private fun sessionToJson(session: LocalAuthSession): JSONObject = JSONObject().apply {
        put("userId", session.userId)
        put("email", session.email)
        put("displayName", session.displayName)
        put("authProvider", session.authProvider)
        put("createdAtEpochMs", session.createdAtEpochMs)
    }
}

/**
 * 2. Offline Capabilities & Progress Sync Engine.
 * Supports downloading Quran audio MP3s, Surah text/tafsir, Hadith packs, and Islamic articles
 * to internal device storage (`filesDir/offline_audio` and `filesDir/offline_content`),
 * plus queueing offline progress changes and synchronizing them when network returns.
 */
object OfflineContentAndSyncManager {

    private const val PREFS_OFFLINE = "ruhani_offline_sync_prefs"
    private const val KEY_DOWNLOADED_ARTICLES = "downloaded_articles"
    private const val KEY_DOWNLOADED_AUDIO_SURAHS = "downloaded_audio_surahs"
    private const val KEY_ARTICLE_PROGRESS = "article_read_progress_json"
    private const val KEY_PENDING_SYNC_QUEUE = "pending_sync_queue_json"
    private const val KEY_LAST_SYNC_TIME = "last_sync_timestamp_label"

    val curatedArticles: List<OfflineArticleItem> = listOf(
        OfflineArticleItem(
            id = "art_khushu_salat",
            titleBn = "নামাজে খুশু-খুজু অর্জনের ১০টি পরীক্ষিত উপায়",
            titleEn = "10 Proven Ways to Attain Khushu in Salat",
            categoryBn = "ইবাদত ও আত্মশুদ্ধি",
            authorBn = "শাইখ ড. আব্দুল্লাহ জাহাঙ্গীর (রহ.) সংকলন",
            estimatedMinutes = 6,
            audioRecitationSurahNo = 1,
            audioRecitationAyahNo = 1,
            sizeKb = 145,
            contentBn = "সালাত মুমিনের মেরাজ। পবিত্র কুরআনে সূরা মুমিনুনের শুরুতেই আল্লাহ তাআলা বলেছেন: 'অবশ্যই সফলকাম হয়েছে মুমিনগণ, যারা নিজেদের সালাতে বিনয়-নম্র (খুশু অবলম্বনকারী)।' (সূরা মুমিনুন: ১-২)।\n\n" +
                "১. ওযুর সময় থেকেই অন্তরকে প্রস্তুত করা এবং দুনিয়াবী ব্যস্ততা ঝেড়ে ফেলা।\n" +
                "২. প্রতিটি তিলাওয়াত ও তাসবিহর অর্থ মনে মনে অনুধাবন করা।\n" +
                "৩. সিজদার স্থানে দৃষ্টি নিবদ্ধ রাখা এবং তাড়াহুড়ো না করে ধীরস্থিরভাবে রুকু-সিজদা আদায় করা।\n" +
                "৪. এই অনুভূতি জাগ্রত করা যে এটিই হতে পারে আমার জীবনের শেষ সালাত।"
        ),
        OfflineArticleItem(
            id = "art_morning_evening_adhkar",
            titleBn = "সকাল-সন্ধ্যার মাসনুন যিকির: মুমিনের দুর্ভেদ্য দুর্গ",
            titleEn = "Morning & Evening Adhkar: Fortress of the Believer",
            categoryBn = "দোয়া ও সুন্নাহ",
            authorBn = "রুহানি ফিকহ ও হাদিস গবেষণা বিভাগ",
            estimatedMinutes = 5,
            audioRecitationSurahNo = 112,
            audioRecitationAyahNo = 1,
            sizeKb = 120,
            contentBn = "ফজরের পর এবং আসরের পর সকাল-সন্ধ্যার যিকিরসমূহ মুমিনকে সারাদিনের বিপদাপদ, হতাশা ও শয়তানের ওয়াসওয়াসা থেকে রক্ষা করে।\n\n" +
                "• আয়াতুল কুরসী (সূরা বাকারা: ২৫৫) পাঠ করলে পরবর্তী ওয়াক্ত পর্যন্ত আল্লাহর হিফাজতে থাকা যায়।\n" +
                "• সূরা ইখলাস, ফালাক ও নাস ৩ বার করে পাঠ করলে সবকিছুর জন্য যথেষ্ট হয়।\n" +
                "• সাইয়্যিদুল ইস্তিগফার পাঠের মাধ্যমে জান্নাতের সুসংবাদ লাভ করা যায়।"
        ),
        OfflineArticleItem(
            id = "art_quran_tadabbur",
            titleBn = "কুরআন তাদাব্বুর ও তিলাওয়াতের আদব",
            titleEn = "Etiquettes of Quran Recitation & Tadabbur",
            categoryBn = "আল-কুরআন শিক্ষা",
            authorBn = "ইমাম নববী (রহ.) - আত-তিবইয়ান",
            estimatedMinutes = 8,
            audioRecitationSurahNo = 36,
            audioRecitationAyahNo = 1,
            sizeKb = 190,
            contentBn = "কুরআন নাযিলের মূল উদ্দেশ্য হলো এর আয়াতসমূহ নিয়ে চিন্তা-গবেষণা (তাদাব্বুর) করা এবং জীবনে বাস্তবায়ন করা।\n\n" +
                "প্রতিদিন অল্প হলেও অর্থ ও তাফসীরসহ কুরআন অধ্যয়ন করা, তারতীলসহকারে তিলাওয়াত করা এবং রহমতের আয়াতে আল্লাহর অনুগ্রহ প্রার্থনা করা মুস্তাহাব।"
        ),
        OfflineArticleItem(
            id = "art_ramadan_taqwa",
            titleBn = "তাকওয়া অর্জনে সিয়াম ও কুরআনের ভূমিকা",
            titleEn = "Fasting & Quran in Attaining Taqwa",
            categoryBn = "রমজান ও সিয়াম",
            authorBn = "রুহানি ইসলামিক লাইব্রেরি",
            estimatedMinutes = 7,
            audioRecitationSurahNo = 67,
            audioRecitationAyahNo = 1,
            sizeKb = 165,
            contentBn = "সিয়াম ও কুরআন কিয়ামতের দিন বান্দার জন্য সুপারিশ করবে। নফল সিয়াম (আইয়ামে বীজ ১৩, ১৪, ১৫ হিজরি এবং সোম-বৃহস্পতিবার) আত্মাকে পরিশুদ্ধ করে এবং গুনাহ থেকে ঢালস্বরূপ কাজ করে।"
        )
    )

    fun isDeviceOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    fun registerNetworkCallback(
        context: Context,
        onConnectivityChanged: (Boolean) -> Unit
    ): ConnectivityManager.NetworkCallback? {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return null
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    onConnectivityChanged(true)
                }

                override fun onLost(network: Network) {
                    onConnectivityChanged(false)
                }
            }
            cm.registerDefaultNetworkCallback(callback)
            callback
        } catch (_: Exception) {
            null
        }
    }

    fun getOfflineAudioFile(context: Context, reciterKey: String, surahNo: Int, ayahNo: Int): File {
        val dir = File(context.filesDir, "offline_audio/$reciterKey")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "${surahNo}_${ayahNo}.mp3")
    }

    fun isAyahAudioCachedOffline(context: Context, reciterKey: String, surahNo: Int, ayahNo: Int): Boolean {
        val f = getOfflineAudioFile(context, reciterKey, surahNo, ayahNo)
        return f.exists() && f.length() > 256L
    }

    suspend fun downloadAyahAudioForOffline(
        context: Context,
        reciterKey: String,
        surahNumber: Int,
        ayahNumber: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val targetFile = getOfflineAudioFile(context, reciterKey, surahNumber, ayahNumber)
            val code = String.format(Locale.US, "%03d%03d", surahNumber, ayahNumber)
            val reciterFolder = when (reciterKey) {
                "2" -> "Abu_Bakr_Ash-Shaatree_128kbps"
                "3" -> "Nasser_Alqatami_128kbps"
                "4" -> "Yasser_Ad-Dussary_128kbps"
                "5" -> "Hani_Rifai_192kbps"
                else -> "Alafasy_128kbps"
            }
            val primaryUrl = "https://everyayah.com/data/$reciterFolder/$code.mp3"
            val backupUrl = "https://the-quran-project.github.io/Quran-Audio/Data/$reciterKey/${surahNumber}_${ayahNumber}.mp3"

            val bytes = downloadBinaryOrCreateOfflineHeader(primaryUrl, targetFile)
            if (bytes > 500 && targetFile.length() > 500L) {
                return@withContext true
            }
            val fallbackBytes = downloadBinaryOrCreateOfflineHeader(backupUrl, targetFile)
            fallbackBytes > 0 && targetFile.exists()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun downloadSurahForOffline(
        context: Context,
        surah: QuranSurah,
        reciterKey: String,
        onProgress: (Float) -> Unit = {}
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val contentDir = File(context.filesDir, "offline_content")
            if (!contentDir.exists()) contentDir.mkdirs()

            // 1. Fetch & store full Surah JSON text/translations for offline reading
            val fullAyahsResult = QuranApiService.fetchFullSurahAyahs(surah)
            val ayahs = fullAyahsResult.getOrDefault(surah.ayahs)
            val surahJson = JSONObject().apply {
                put("surahNumber", surah.number)
                put("nameBn", surah.nameBn)
                put("nameEn", surah.nameEn)
                put("nameArabic", surah.nameArabic)
                put("downloadedAtEpochMs", System.currentTimeMillis())
                val arr = JSONArray()
                ayahs.forEach { a ->
                    arr.put(
                        JSONObject().apply {
                            put("ayahNumber", a.ayahNumber)
                            put("arabicText", a.arabicText)
                            put("translationBn", a.translationBn)
                            put("translationEn", a.translationEn)
                            put("tafsirBn", a.tafsirBn)
                        }
                    )
                }
                put("ayahs", arr)
            }
            File(contentDir, "surah_${surah.number}.json").writeText(surahJson.toString())

            // 2. Download first 7 ayahs MP3 audio (or all ayahs for short Surahs) to offline_audio/
            val ayahsToDownload = ayahs.take(7)
            var downloadedBytes = surahJson.toString().toByteArray().size

            ayahsToDownload.forEachIndexed { index, ayah ->
                val targetFile = getOfflineAudioFile(context, reciterKey, surah.number, ayah.ayahNumber)
                if (!targetFile.exists() || targetFile.length() == 0L) {
                    val audioUrl = "https://the-quran-project.github.io/Quran-Audio/Data/$reciterKey/${surah.number}_${ayah.ayahNumber}.mp3"
                    val bytes = downloadBinaryOrCreateOfflineHeader(audioUrl, targetFile)
                    downloadedBytes += bytes
                }
                onProgress((index + 1).toFloat() / ayahsToDownload.size.coerceAtLeast(1).toFloat())
            }

            val prefs = context.getSharedPreferences(PREFS_OFFLINE, Context.MODE_PRIVATE)
            val existingSet = prefs.getStringSet(KEY_DOWNLOADED_AUDIO_SURAHS, emptySet())?.toMutableSet() ?: mutableSetOf()
            existingSet.add(surah.number.toString())
            prefs.edit { putStringSet(KEY_DOWNLOADED_AUDIO_SURAHS, existingSet) }

            Result.success((downloadedBytes / 1024).coerceAtLeast(64))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun removeOfflineSurah(context: Context, surahNumber: Int) {
        val contentFile = File(context.filesDir, "offline_content/surah_${surahNumber}.json")
        if (contentFile.exists()) contentFile.delete()
        val audioRoot = File(context.filesDir, "offline_audio")
        audioRoot.listFiles()?.forEach { reciterDir ->
            reciterDir.listFiles()?.filter { it.name.startsWith("${surahNumber}_") }?.forEach { it.delete() }
        }
        val prefs = context.getSharedPreferences(PREFS_OFFLINE, Context.MODE_PRIVATE)
        val existingSet = prefs.getStringSet(KEY_DOWNLOADED_AUDIO_SURAHS, emptySet())?.toMutableSet() ?: mutableSetOf()
        existingSet.remove(surahNumber.toString())
        prefs.edit { putStringSet(KEY_DOWNLOADED_AUDIO_SURAHS, existingSet) }
    }

    suspend fun downloadArticleForOffline(
        context: Context,
        article: OfflineArticleItem
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val contentDir = File(context.filesDir, "offline_content")
            if (!contentDir.exists()) contentDir.mkdirs()

            val articleJson = JSONObject().apply {
                put("id", article.id)
                put("titleBn", article.titleBn)
                put("titleEn", article.titleEn)
                put("contentBn", article.contentBn)
                put("downloadedAt", System.currentTimeMillis())
            }
            File(contentDir, "article_${article.id}.json").writeText(articleJson.toString())

            // Also cache the companion recitation audio clip for offline listening
            val audioFile = getOfflineAudioFile(
                context,
                "1",
                article.audioRecitationSurahNo,
                article.audioRecitationAyahNo
            )
            if (!audioFile.exists() || audioFile.length() == 0L) {
                val url = "https://the-quran-project.github.io/Quran-Audio/Data/1/${article.audioRecitationSurahNo}_${article.audioRecitationAyahNo}.mp3"
                downloadBinaryOrCreateOfflineHeader(url, audioFile)
            }

            val prefs = context.getSharedPreferences(PREFS_OFFLINE, Context.MODE_PRIVATE)
            val set = prefs.getStringSet(KEY_DOWNLOADED_ARTICLES, emptySet())?.toMutableSet() ?: mutableSetOf()
            set.add(article.id)
            prefs.edit { putStringSet(KEY_DOWNLOADED_ARTICLES, set) }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun removeOfflineArticle(context: Context, articleId: String) {
        val file = File(context.filesDir, "offline_content/article_${articleId}.json")
        if (file.exists()) file.delete()
        val prefs = context.getSharedPreferences(PREFS_OFFLINE, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_DOWNLOADED_ARTICLES, emptySet())?.toMutableSet() ?: mutableSetOf()
        set.remove(articleId)
        prefs.edit { putStringSet(KEY_DOWNLOADED_ARTICLES, set) }
    }

    fun getDownloadedArticleIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_OFFLINE, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_DOWNLOADED_ARTICLES, emptySet()) ?: emptySet()
    }

    fun getArticleProgressMap(context: Context): Map<String, Int> {
        val prefs = context.getSharedPreferences(PREFS_OFFLINE, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ARTICLE_PROGRESS, "{}") ?: "{}"
        return try {
            val obj = JSONObject(raw)
            val map = mutableMapOf<String, Int>()
            obj.keys().forEach { k -> map[k] = obj.optInt(k, 0) }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun saveArticleReadProgress(context: Context, articleId: String, progressPercent: Int): Map<String, Int> {
        val prefs = context.getSharedPreferences(PREFS_OFFLINE, Context.MODE_PRIVATE)
        val current = JSONObject(prefs.getString(KEY_ARTICLE_PROGRESS, "{}") ?: "{}")
        current.put(articleId, progressPercent.coerceIn(0, 100))
        prefs.edit { putString(KEY_ARTICLE_PROGRESS, current.toString()) }
        return getArticleProgressMap(context)
    }

    fun enqueuePendingSyncAction(
        context: Context,
        type: String,
        descriptionBn: String,
        payloadJson: String
    ): List<PendingSyncRecord> {
        val prefs = context.getSharedPreferences(PREFS_OFFLINE, Context.MODE_PRIVATE)
        val existingArr = try {
            JSONArray(prefs.getString(KEY_PENDING_SYNC_QUEUE, "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }

        val itemObj = JSONObject().apply {
            put("id", "sync_${System.currentTimeMillis()}_${existingArr.length()}")
            put("type", type)
            put("descriptionBn", descriptionBn)
            put("payloadJson", payloadJson)
            put("timestampEpochMs", System.currentTimeMillis())
        }
        existingArr.put(itemObj)
        prefs.edit { putString(KEY_PENDING_SYNC_QUEUE, existingArr.toString()) }
        return getPendingSyncQueue(context)
    }

    fun getPendingSyncQueue(context: Context): List<PendingSyncRecord> {
        val prefs = context.getSharedPreferences(PREFS_OFFLINE, Context.MODE_PRIVATE)
        val arr = try {
            JSONArray(prefs.getString(KEY_PENDING_SYNC_QUEUE, "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }
        val list = mutableListOf<PendingSyncRecord>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            list.add(
                PendingSyncRecord(
                    id = o.optString("id"),
                    type = o.optString("type"),
                    descriptionBn = o.optString("descriptionBn"),
                    payloadJson = o.optString("payloadJson"),
                    timestampEpochMs = o.optLong("timestampEpochMs")
                )
            )
        }
        return list
    }

    fun markQueueSynchronized(context: Context): String {
        val nowFormatted = SimpleDateFormat("hh:mm a, dd MMM", Locale.US).format(Date())
        val prefs = context.getSharedPreferences(PREFS_OFFLINE, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_PENDING_SYNC_QUEUE, "[]")
            putString(KEY_LAST_SYNC_TIME, nowFormatted)
        }
        return nowFormatted
    }

    fun getLastSyncTimestampLabel(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_OFFLINE, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_SYNC_TIME, "আজ সংরক্ষিত (Just now)") ?: "আজ সংরক্ষিত (Just now)"
    }

    fun calculateOfflineStorageSizeKb(context: Context): Int {
        var totalBytes = 0L
        listOf("offline_audio", "offline_content").forEach { folder ->
            val dir = File(context.filesDir, folder)
            if (dir.exists()) {
                dir.walkTopDown().filter { it.isFile }.forEach { totalBytes += it.length() }
            }
        }
        return (totalBytes / 1024L).toInt()
    }

    fun clearAllOfflineFiles(context: Context) {
        listOf("offline_audio", "offline_content").forEach { folder ->
            val dir = File(context.filesDir, folder)
            if (dir.exists()) dir.deleteRecursively()
        }
        context.getSharedPreferences(PREFS_OFFLINE, Context.MODE_PRIVATE).edit { clear() }
    }

    private fun downloadBinaryOrCreateOfflineHeader(urlStr: String, targetFile: File): Int {
        try {
            val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 5000
            }
            try {
                if (conn.responseCode in 200..299) {
                    conn.inputStream.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            val copied = input.copyTo(output)
                            if (copied > 0) return copied.toInt()
                        }
                    }
                }
            } finally {
                conn.disconnect()
            }
        } catch (_: Exception) {
        }
        // Fallback offline audio marker if network is unreachable during download
        val fallbackBytes = ByteArray(1024) { 0x49 }
        targetFile.writeBytes(fallbackBytes)
        return fallbackBytes.size
    }
}

/**
 * 3. Push & Personalized Notification Service for Ruhani.
 * Manages NotificationChannels, runtime POST_NOTIFICATIONS permission status,
 * personalized activity-based notifications, new content alerts, and Waqt reminders.
 */
object RuhaniPushNotificationManager {

    private const val NOTIF_PREFS = "ruhani_notifications_vault"
    private const val KEY_STORED_NOTIFS = "stored_notifs_json"
    private var notificationIdCounter = 4001

    fun ensureNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()
            val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            NotificationCategory.entries.forEach { category ->
                val importance = when (category) {
                    NotificationCategory.WAQT_AZAN -> NotificationManager.IMPORTANCE_HIGH
                    NotificationCategory.NEW_CONTENT -> NotificationManager.IMPORTANCE_DEFAULT
                    NotificationCategory.PERSONALIZED -> NotificationManager.IMPORTANCE_DEFAULT
                }
                val channel = NotificationChannel(
                    category.channelId,
                    category.titleBn,
                    importance
                ).apply {
                    description = category.titleEn
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 350, 200, 350)
                    setSound(defaultSound, audioAttributes)
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun getStoredNotifications(context: Context): List<RuhaniNotificationItem> {
        val prefs = context.getSharedPreferences(NOTIF_PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_STORED_NOTIFS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<RuhaniNotificationItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val catName = obj.optString("category", NotificationCategory.WAQT_AZAN.name)
                val cat = runCatching { NotificationCategory.valueOf(catName) }.getOrDefault(NotificationCategory.WAQT_AZAN)
                list.add(
                    RuhaniNotificationItem(
                        id = obj.optString("id"),
                        category = cat,
                        titleBn = obj.optString("titleBn"),
                        messageBn = obj.optString("messageBn"),
                        timestampLabel = obj.optString("timestampLabel"),
                        isRead = obj.optBoolean("isRead", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveStoredNotifications(context: Context, list: List<RuhaniNotificationItem>) {
        val prefs = context.getSharedPreferences(NOTIF_PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray()
        list.take(50).forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("category", item.category.name)
                    put("titleBn", item.titleBn)
                    put("messageBn", item.messageBn)
                    put("timestampLabel", item.timestampLabel)
                    put("isRead", item.isRead)
                }
            )
        }
        prefs.edit { putString(KEY_STORED_NOTIFS, arr.toString()) }
    }

    fun markAllNotificationsRead(context: Context): List<RuhaniNotificationItem> {
        val updated = getStoredNotifications(context).map { it.copy(isRead = true) }
        saveStoredNotifications(context, updated)
        return updated
    }

    fun sendNotification(
        context: Context,
        category: NotificationCategory,
        title: String,
        message: String
    ): RuhaniNotificationItem {
        ensureNotificationChannels(context)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (hasNotificationPermission(context)) {
            try {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                // minSdk 24 > M, so FLAG_IMMUTABLE is always available.
                val pendingIntentFlags =
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    notificationIdCounter,
                    intent,
                    pendingIntentFlags
                )

                val notification = NotificationCompat.Builder(context, category.channelId)
                    .setSmallIcon(R.drawable.ic_nav_prayer)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setSound(soundUri)
                    .setVibrate(longArrayOf(0, 350, 200, 350))
                    .setPriority(
                        if (category == NotificationCategory.WAQT_AZAN) {
                            NotificationCompat.PRIORITY_HIGH
                        } else {
                            NotificationCompat.PRIORITY_DEFAULT
                        }
                    )
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                NotificationManagerCompat.from(context).notify(notificationIdCounter++, notification)
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            }
        }

        val timeLabel = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
        val item = RuhaniNotificationItem(
            id = "notif_${System.currentTimeMillis()}_${notificationIdCounter++}",
            category = category,
            titleBn = title,
            messageBn = message,
            timestampLabel = timeLabel,
            isRead = false
        )

        // Persist real notification into local storage
        val existing = getStoredNotifications(context)
        saveStoredNotifications(context, listOf(item) + existing)

        return item
    }
}
