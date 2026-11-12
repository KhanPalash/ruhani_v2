package com.khanscreations.ruhani.ui

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanscreations.ruhani.data.model.QuranBookmark
import com.khanscreations.ruhani.data.model.SalatLog
import com.khanscreations.ruhani.data.model.UserProfile
import com.khanscreations.ruhani.data.repository.RuhaniFirestoreRepository
import com.khanscreations.ruhani.domain.NotificationCategory
import com.khanscreations.ruhani.domain.OfflineArticleItem
import com.khanscreations.ruhani.domain.OfflineContentAndSyncManager
import com.khanscreations.ruhani.domain.PendingSyncRecord
import com.khanscreations.ruhani.domain.RuhaniNotificationItem
import com.khanscreations.ruhani.domain.RuhaniPushNotificationManager
import com.khanscreations.ruhani.domain.SecureAuthAndPrivacyManager
import com.khanscreations.ruhani.domain.UpdateChecker
import com.khanscreations.ruhani.domain.UpdateInfo
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SyncAndAccountUiState(
    val hasCompletedOnboarding: Boolean = false,
    val isGuestMode: Boolean = false,
    val currentUserId: String? = null,
    val userEmail: String? = null,
    val displayName: String = "মুসলিম ব্যবহারকারী",
    val phone: String = "",
    val language: String = "bn",
    val useBanglaDigits: Boolean = false,
    val themeMode: String = "DARK",
    val authProvider: String = "GUEST",
    val privacyCloudSyncConsent: Boolean = true,
    val privacyPersonalizedAlertsConsent: Boolean = true,
    val privacyDataMinimization: Boolean = true,
    val exportedUserDataJson: String? = null,
    val isDeviceOnline: Boolean = true,
    val downloadedArticleIds: Set<String> = emptySet(),
    val articleReadProgress: Map<String, Int> = emptyMap(),
    val offlineStorageUsedKb: Int = 0,
    val pendingSyncQueue: List<PendingSyncRecord> = emptyList(),
    val lastSyncedLabel: String = "আজ সংরক্ষিত (Just now)",
    val notifyNewContentAlerts: Boolean = true,
    val notifyImportantUpdates: Boolean = true,
    val notifyPersonalizedActivity: Boolean = true,
    val notificationFeed: List<RuhaniNotificationItem> = emptyList(),
    val syncStatusMessage: String? = null,
    val errorMessage: String? = null,
    val updateInfo: UpdateInfo? = null
)

class SyncAndAccountViewModel(
    private val appContext: Context,
    private val prefs: SharedPreferences = appContext.getSharedPreferences("ruhani_local_prefs", Context.MODE_PRIVATE),
    externalScope: CoroutineScope? = null,
    private val firestoreRepository: RuhaniFirestoreRepository? = null,
    private val authenticatedUserId: String? = null,
    private val authenticatedEmail: String? = null,
    private val authenticatedName: String? = null,
    var onUserProfileLoaded: ((UserProfile) -> Unit)? = null,
    var onSalatLogsLoaded: ((List<SalatLog>) -> Unit)? = null,
    var onBookmarksLoaded: ((List<QuranBookmark>) -> Unit)? = null,
    var profileDataProvider: (() -> UserProfile)? = null,
    var salatLogsProvider: (() -> List<SalatLog>) = { emptyList() }
) : ViewModel() {

    private val scope: CoroutineScope = externalScope ?: viewModelScope
    private val _syncAndAccountState = MutableStateFlow(loadInitialState())
    val syncAndAccountState: StateFlow<SyncAndAccountUiState> = _syncAndAccountState.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        RuhaniPushNotificationManager.ensureNotificationChannels(appContext)
        observeNetworkConnectivity()
        if (authenticatedUserId != null && firestoreRepository != null) {
            observeCloudData(authenticatedUserId, firestoreRepository)
        }
        checkForUpdate()
    }

    private fun loadInitialState(): SyncAndAccountUiState {
        val lang = prefs.getString("language", "bn") ?: "bn"
        val theme = prefs.getString("theme_mode", "DARK") ?: "DARK"
        val onboarded = prefs.getBoolean("onboarded", false)
        val guest = prefs.getBoolean("guest_mode", false)
        val localSession = SecureAuthAndPrivacyManager.getActiveSession(appContext)
        val resolvedUserId = authenticatedUserId ?: localSession?.userId
        val resolvedEmail = authenticatedEmail ?: localSession?.email
        val resolvedProvider = when {
            authenticatedUserId != null -> "GOOGLE"
            localSession != null -> localSession.authProvider
            else -> "GUEST"
        }
        val displayName = authenticatedName?.takeIf { it.isNotBlank() }
            ?: localSession?.displayName?.takeIf { it.isNotBlank() }
            ?: prefs.getString("display_name", "মুসলিম ব্যবহারকারী")
            ?: "মুসলিম ব্যবহারকারী"

        val realNotifs = RuhaniPushNotificationManager.getStoredNotifications(appContext)

        return SyncAndAccountUiState(
            hasCompletedOnboarding = onboarded || resolvedUserId != null,
            isGuestMode = guest && resolvedUserId == null,
            currentUserId = resolvedUserId,
            userEmail = resolvedEmail,
            displayName = displayName,
            authProvider = resolvedProvider,
            privacyCloudSyncConsent = localSession?.privacyCloudSyncConsent ?: true,
            privacyPersonalizedAlertsConsent = localSession?.privacyPersonalizedAlertsConsent ?: true,
            privacyDataMinimization = localSession?.privacyAnalyticsMinimize ?: true,
            isDeviceOnline = OfflineContentAndSyncManager.isDeviceOnline(appContext),
            downloadedArticleIds = OfflineContentAndSyncManager.getDownloadedArticleIds(appContext),
            articleReadProgress = OfflineContentAndSyncManager.getArticleProgressMap(appContext),
            offlineStorageUsedKb = OfflineContentAndSyncManager.calculateOfflineStorageSizeKb(appContext),
            pendingSyncQueue = OfflineContentAndSyncManager.getPendingSyncQueue(appContext),
            lastSyncedLabel = OfflineContentAndSyncManager.getLastSyncTimestampLabel(appContext),
            notifyNewContentAlerts = prefs.getBoolean("notify_new_content", true),
            notifyImportantUpdates = prefs.getBoolean("notify_important_updates", true),
            notifyPersonalizedActivity = prefs.getBoolean("notify_personalized_activity", true),
            notificationFeed = realNotifs,
            language = lang,
            themeMode = theme
        )
    }

    private fun observeNetworkConnectivity() {
        networkCallback = OfflineContentAndSyncManager.registerNetworkCallback(appContext) { isOnline ->
            val wasOffline = !_syncAndAccountState.value.isDeviceOnline
            _syncAndAccountState.update { it.copy(isDeviceOnline = isOnline) }
            if (isOnline && wasOffline && _syncAndAccountState.value.pendingSyncQueue.isNotEmpty()) {
                synchronizeOfflineChangesNow()
            }
        }
    }

    private fun checkForUpdate() {
        scope.launch {
            val currentVersion = try {
                appContext.packageManager
                    .getPackageInfo(appContext.packageName, 0)
                    .versionName ?: "1.0"
            } catch (_: Exception) { "1.0" }
            val info = UpdateChecker.checkForUpdate(currentVersion)
            if (info != null) {
                _syncAndAccountState.update { it.copy(updateInfo = info) }
            }
        }
    }

    fun dismissUpdateDialog() {
        _syncAndAccountState.update { it.copy(updateInfo = null) }
    }

    private fun observeCloudData(uid: String, repo: RuhaniFirestoreRepository) {
        scope.launch {
            repo.observeUserProfile()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val root = firestoreError ?: error
                    Log.w("SyncAccountVM", "Profile listener error: ${root.message}")
                    _syncAndAccountState.update { it.copy(errorMessage = root.localizedMessage) }
                }
                .collect { profile ->
                    if (profile != null) {
                        _syncAndAccountState.update { state ->
                            state.copy(
                                displayName = profile.displayName,
                                phone = profile.phone ?: state.phone,
                                language = profile.language,
                                themeMode = profile.themeMode
                            )
                        }
                        onUserProfileLoaded?.invoke(profile)
                    } else {
                        syncProfileToCloud()
                    }
                }
        }

        scope.launch {
            repo.observeSalatLogs()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w("SyncAccountVM", "SalatLogs listener error: ${error.message}")
                }
                .collect { logs ->
                    if (logs.isNotEmpty()) {
                        onSalatLogsLoaded?.invoke(logs)
                    }
                }
        }

        scope.launch {
            repo.observeBookmarks()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w("SyncAccountVM", "Bookmarks listener error: ${error.message}")
                }
                .collect { bookmarks ->
                    onBookmarksLoaded?.invoke(bookmarks)
                }
        }
    }

    fun syncProfileToCloud() {
        val uid = authenticatedUserId ?: _syncAndAccountState.value.currentUserId ?: return
        val repo = firestoreRepository ?: return
        val profile = profileDataProvider?.invoke() ?: return
        scope.launch {
            val result = repo.saveUserProfile(profile.copy(userId = uid))
            if (result.isSuccess) {
                _syncAndAccountState.update { it.copy(syncStatusMessage = "ক্লাউডে সংরক্ষিত হয়েছে (Synced)") }
            }
        }
    }

    fun completeOnboarding(asGuest: Boolean) {
        prefs.edit()
            .putBoolean("onboarded", true)
            .putBoolean("guest_mode", asGuest)
            .apply()
        _syncAndAccountState.update {
            it.copy(
                hasCompletedOnboarding = true,
                isGuestMode = asGuest
            )
        }
    }

    fun updateThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _syncAndAccountState.update { it.copy(themeMode = mode) }
        syncProfileToCloud()
    }

    fun updateLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
        _syncAndAccountState.update { it.copy(language = lang) }
        syncProfileToCloud()
    }

    fun toggleBanglaDigits() {
        _syncAndAccountState.update { it.copy(useBanglaDigits = !it.useBanglaDigits) }
    }

    fun updateProfileDetails(name: String, phone: String) {
        prefs.edit().putString("display_name", name).apply()
        _syncAndAccountState.update {
            it.copy(
                displayName = name.ifBlank { "মুসলিম ব্যবহারকারী" },
                phone = phone
            )
        }
        syncProfileToCloud()
    }

    fun registerWithEmailPassword(
        displayName: String,
        email: String,
        password: String,
        privacyConsent: Boolean
    ) {
        val result = SecureAuthAndPrivacyManager.registerWithEmailPassword(
            context = appContext,
            displayName = displayName,
            email = email,
            password = password,
            privacyConsentAccepted = privacyConsent
        )
        result.onSuccess { session ->
            prefs.edit()
                .putBoolean("onboarded", true)
                .putBoolean("guest_mode", false)
                .putString("display_name", session.displayName)
                .apply()
            _syncAndAccountState.update {
                it.copy(
                    hasCompletedOnboarding = true,
                    isGuestMode = false,
                    currentUserId = session.userId,
                    userEmail = session.email,
                    displayName = session.displayName,
                    authProvider = session.authProvider,
                    errorMessage = null,
                    syncStatusMessage = "ইমেইল অ্যাকাউন্ট নিবন্ধন সফল হয়েছে (${session.email})"
                )
            }
        }.onFailure { err ->
            _syncAndAccountState.update { it.copy(errorMessage = err.message ?: "নিবন্ধন ব্যর্থ হয়েছে") }
        }
    }

    fun loginWithEmailPassword(email: String, password: String) {
        val result = SecureAuthAndPrivacyManager.loginWithEmailPassword(
            context = appContext,
            email = email,
            password = password
        )
        result.onSuccess { session ->
            prefs.edit()
                .putBoolean("onboarded", true)
                .putBoolean("guest_mode", false)
                .putString("display_name", session.displayName)
                .apply()
            _syncAndAccountState.update {
                it.copy(
                    hasCompletedOnboarding = true,
                    isGuestMode = false,
                    currentUserId = session.userId,
                    userEmail = session.email,
                    displayName = session.displayName,
                    authProvider = session.authProvider,
                    errorMessage = null,
                    syncStatusMessage = "সফলভাবে লগইন সম্পন্ন হয়েছে (${session.email})"
                )
            }
        }.onFailure { err ->
            _syncAndAccountState.update { it.copy(errorMessage = err.message ?: "লগইন ব্যর্থ হয়েছে") }
        }
    }

    fun signOutUserSession() {
        SecureAuthAndPrivacyManager.signOutLocalSession(appContext)
        _syncAndAccountState.update {
            it.copy(
                currentUserId = null,
                userEmail = null,
                authProvider = "GUEST",
                isGuestMode = true,
                syncStatusMessage = "সাইন আউট সম্পন্ন হয়েছে"
            )
        }
    }

    fun updatePrivacySettings(
        cloudSyncConsent: Boolean,
        personalizedAlertsConsent: Boolean,
        dataMinimization: Boolean
    ) {
        SecureAuthAndPrivacyManager.updatePrivacyPreferences(
            context = appContext,
            cloudSyncConsent = cloudSyncConsent,
            personalizedAlertsConsent = personalizedAlertsConsent,
            dataMinimization = dataMinimization
        )
        _syncAndAccountState.update {
            it.copy(
                privacyCloudSyncConsent = cloudSyncConsent,
                privacyPersonalizedAlertsConsent = personalizedAlertsConsent,
                privacyDataMinimization = dataMinimization,
                syncStatusMessage = "গোপনীয়তা ও ডেটা সুরক্ষা সেটিংস আপডেট হয়েছে"
            )
        }
    }

    fun exportMyPrivacyData(
        districtNameEn: String,
        tasbihTotal: Long,
        bookmarksCount: Int,
        downloadedSurahs: Set<Int>
    ) {
        val s = _syncAndAccountState.value
        val json = SecureAuthAndPrivacyManager.exportUserDataJson(
            context = appContext,
            displayName = s.displayName,
            email = s.userEmail,
            district = districtNameEn,
            tasbihTotal = tasbihTotal,
            bookmarksCount = bookmarksCount,
            downloadedSurahs = downloadedSurahs
        )
        _syncAndAccountState.update {
            it.copy(
                exportedUserDataJson = json,
                syncStatusMessage = "আপনার ব্যক্তিগত ডেটা JSON রিপোর্ট প্রস্তুত করা হয়েছে"
            )
        }
    }

    fun dismissExportedPrivacyData() {
        _syncAndAccountState.update { it.copy(exportedUserDataJson = null) }
    }

    fun deleteAccountAndLocalData() {
        SecureAuthAndPrivacyManager.deleteAccountAndWipeLocalData(appContext)
        prefs.edit().clear().apply()
        _syncAndAccountState.update {
            it.copy(
                currentUserId = null,
                userEmail = null,
                displayName = "মুসলিম ব্যবহারকারী",
                authProvider = "GUEST",
                isGuestMode = true,
                pendingSyncQueue = emptyList(),
                exportedUserDataJson = null,
                syncStatusMessage = "আপনার অ্যাকাউন্ট ও লোকাল ডেটা সম্পূর্ণ মুছে ফেলা হয়েছে"
            )
        }
    }

    fun toggleNotificationPreference(prefKey: String) {
        val s = _syncAndAccountState.value
        when (prefKey) {
            "new_content" -> {
                val next = !s.notifyNewContentAlerts
                prefs.edit().putBoolean("notify_new_content", next).apply()
                _syncAndAccountState.update { it.copy(notifyNewContentAlerts = next) }
            }
            "important_updates" -> {
                val next = !s.notifyImportantUpdates
                prefs.edit().putBoolean("notify_important_updates", next).apply()
                _syncAndAccountState.update { it.copy(notifyImportantUpdates = next) }
            }
            "personalized_activity" -> {
                val next = !s.notifyPersonalizedActivity
                prefs.edit().putBoolean("notify_personalized_activity", next).apply()
                _syncAndAccountState.update { it.copy(notifyPersonalizedActivity = next) }
            }
        }
    }

    fun triggerPersonalizedActivityNotification(
        lastReadSurahNumber: Int,
        lastReadAyahNumber: Int,
        tasbihTodayTotal: Int
    ) {
        val s = _syncAndAccountState.value
        val surahName = com.khanscreations.ruhani.domain.QuranCatalog.surahs.firstOrNull { it.number == lastReadSurahNumber }?.nameBn ?: "আল-ফাতিহা"
        RuhaniPushNotificationManager.sendNotification(
            context = appContext,
            category = NotificationCategory.PERSONALIZED,
            title = "ব্যক্তিগত আমল রিমাইন্ডার • ${s.displayName}",
            message = "সর্বশেষ তিলাওয়াত: সূরা $surahName (আয়াত $lastReadAyahNumber)। আজকের তাসবিহ: $tasbihTodayTotal বার। আপনার দৈনিক আমল অব্যাহত রাখুন!"
        )
        _syncAndAccountState.update {
            it.copy(
                notificationFeed = RuhaniPushNotificationManager.getStoredNotifications(appContext),
                syncStatusMessage = "ব্যক্তিগত আমল নোটিফিকেশন পাঠানো হয়েছে"
            )
        }
    }

    fun triggerNewContentNotification() {
        RuhaniPushNotificationManager.sendNotification(
            context = appContext,
            category = NotificationCategory.NEW_CONTENT,
            title = "নতুন কন্টেন্ট ও হিজরি আপডেট • রুহানি",
            message = "নতুন বাংলা হাদিস অধ্যায়, সূরা ইয়াসিন অফলাইন অডিও এবং আল-আযান হিজরি ক্যালেন্ডার আপডেট যুক্ত হয়েছে।"
        )
        _syncAndAccountState.update {
            it.copy(
                notificationFeed = RuhaniPushNotificationManager.getStoredNotifications(appContext),
                syncStatusMessage = "নতুন কন্টেন্ট পুশ অ্যালার্ট পাঠানো হয়েছে"
            )
        }
    }

    fun triggerWaqtTestNotification(nextWaqtNameBn: String, remainingHhMmSs: String, districtNameBn: String) {
        RuhaniPushNotificationManager.sendNotification(
            context = appContext,
            category = NotificationCategory.WAQT_AZAN,
            title = "ওয়াক্ত ও আযান অ্যালার্ট: $nextWaqtNameBn",
            message = "$districtNameBn জেলায় পরবর্তী ওয়াক্ত $nextWaqtNameBn ($remainingHhMmSs বাকি)।"
        )
        _syncAndAccountState.update {
            it.copy(
                notificationFeed = RuhaniPushNotificationManager.getStoredNotifications(appContext),
                syncStatusMessage = "ওয়াক্ত ও আযান টেস্ট নোটিফিকেশন পাঠানো হয়েছে"
            )
        }
    }

    fun markAllNotificationsRead() {
        val updated = RuhaniPushNotificationManager.markAllNotificationsRead(appContext)
        _syncAndAccountState.update { state ->
            state.copy(notificationFeed = updated)
        }
    }

    fun toggleOfflineArticleDownload(article: OfflineArticleItem) {
        val current = _syncAndAccountState.value
        if (article.id in current.downloadedArticleIds) {
            OfflineContentAndSyncManager.removeOfflineArticle(appContext, article.id)
            val updated = current.downloadedArticleIds - article.id
            _syncAndAccountState.update { state ->
                state.copy(
                    downloadedArticleIds = updated,
                    offlineStorageUsedKb = OfflineContentAndSyncManager.calculateOfflineStorageSizeKb(appContext),
                    syncStatusMessage = "অফলাইন তালিকা থেকে প্রবন্ধটি সরানো হয়েছে"
                )
            }
        } else {
            scope.launch {
                OfflineContentAndSyncManager.downloadArticleForOffline(appContext, article)
                val updated = _syncAndAccountState.value.downloadedArticleIds + article.id
                _syncAndAccountState.update { state ->
                    state.copy(
                        downloadedArticleIds = updated,
                        offlineStorageUsedKb = OfflineContentAndSyncManager.calculateOfflineStorageSizeKb(appContext),
                        syncStatusMessage = "‘${article.titleBn}’ অফলাইনে পড়ার ও শোনার জন্য সংরক্ষিত হয়েছে"
                    )
                }
            }
        }
    }

    fun updateArticleReadingProgress(article: OfflineArticleItem, progressPercent: Int) {
        val updatedMap = OfflineContentAndSyncManager.saveArticleReadProgress(
            appContext,
            article.id,
            progressPercent
        )
        val queue = OfflineContentAndSyncManager.enqueuePendingSyncAction(
            context = appContext,
            type = "ARTICLE_PROGRESS",
            descriptionBn = "প্রবন্ধ পাঠের অগ্রগতি: ${article.titleBn} ($progressPercent%)",
            payloadJson = "{\"articleId\":\"${article.id}\",\"progress\":$progressPercent}"
        )
        _syncAndAccountState.update {
            it.copy(
                articleReadProgress = updatedMap,
                pendingSyncQueue = queue,
                syncStatusMessage = "পাঠের অগ্রগতি ($progressPercent%) সংরক্ষিত হয়েছে"
            )
        }
        if (_syncAndAccountState.value.isDeviceOnline && authenticatedUserId != null) {
            synchronizeOfflineChangesNow()
        }
    }

    fun synchronizeOfflineChangesNow() {
        scope.launch {
            val s = _syncAndAccountState.value
            val uid = authenticatedUserId ?: s.currentUserId
            if (uid != null && firestoreRepository != null && s.privacyCloudSyncConsent) {
                syncProfileToCloud()
                salatLogsProvider().forEach { log ->
                    firestoreRepository.saveSalatLog(log)
                }
            }
            val syncedTime = OfflineContentAndSyncManager.markQueueSynchronized(appContext)
            _syncAndAccountState.update {
                it.copy(
                    pendingSyncQueue = emptyList(),
                    lastSyncedLabel = syncedTime,
                    syncStatusMessage = "সকল অফলাইন পরিবর্তন ও অগ্রগতি সফলভাবে সিঙ্ক হয়েছে ($syncedTime)"
                )
            }
        }
    }

    fun setPendingSyncQueue(queue: List<PendingSyncRecord>) {
        _syncAndAccountState.update { it.copy(pendingSyncQueue = queue) }
    }

    fun setLastSyncedLabel(label: String) {
        _syncAndAccountState.update { it.copy(lastSyncedLabel = label) }
    }

    fun updateOfflineStorageSize(sizeKb: Int) {
        _syncAndAccountState.update { it.copy(offlineStorageUsedKb = sizeKb) }
    }

    fun setSyncStatusMessage(msg: String) {
        _syncAndAccountState.update { it.copy(syncStatusMessage = msg) }
    }

    fun setErrorMessage(msg: String) {
        _syncAndAccountState.update { it.copy(errorMessage = msg) }
    }

    fun refreshNotificationFeed() {
        val feed = RuhaniPushNotificationManager.getStoredNotifications(appContext)
        _syncAndAccountState.update { it.copy(notificationFeed = feed) }
    }

    fun clearStatusMessage() {
        _syncAndAccountState.update { it.copy(syncStatusMessage = null, errorMessage = null) }
    }

    fun cleanup() {
        try {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            networkCallback?.let { cm?.unregisterNetworkCallback(it) }
        } catch (_: Exception) {
        }
        networkCallback = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}
