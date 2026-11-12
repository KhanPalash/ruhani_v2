package com.khanscreations.ruhani.data.repository

import android.content.Context
import com.khanscreations.ruhani.R
import com.khanscreations.ruhani.data.model.QuranBookmark
import com.khanscreations.ruhani.data.model.SalatLog
import com.khanscreations.ruhani.data.model.UserProfile
import com.khanscreations.ruhani.data.remote.OperationType
import com.khanscreations.ruhani.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class RuhaniFirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth
        get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun getUserProfile(userId: String = requireUserId()): Result<UserProfile?> = runCatching {
        val docRef = db.collection("users").document(userId)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(
                    UserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                val createPayload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "displayName" to profile.displayName.ifBlank { "মুসলিম ব্যবহারকারী" },
                    "district" to profile.district.ifBlank { "Dhaka" },
                    "calculationMethod" to profile.calculationMethod.ifBlank { "ISLAMIC_FOUNDATION_BD" },
                    "madhhab" to profile.madhhab.ifBlank { "HANAFI" },
                    "language" to profile.language.ifBlank { "bn" },
                    "themeMode" to profile.themeMode.ifBlank { "DARK" },
                    "tasbihAllTimeTotal" to profile.tasbihAllTimeTotal,
                    "qazaFajr" to profile.qazaFajr,
                    "qazaDhuhr" to profile.qazaDhuhr,
                    "qazaAsr" to profile.qazaAsr,
                    "qazaMaghrib" to profile.qazaMaghrib,
                    "qazaIsha" to profile.qazaIsha,
                    "qazaWitr" to profile.qazaWitr,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                profile.email?.takeIf { it.isNotBlank() }?.let { createPayload["email"] = it }
                profile.phone?.takeIf { it.isNotBlank() }?.let { createPayload["phone"] = it }
                profile.photoUrl?.takeIf { it.isNotBlank() }?.let { createPayload["photoUrl"] = it }
                docRef.set(createPayload).await()
            } else {
                val updatePayload = mutableMapOf<String, Any>(
                    "displayName" to profile.displayName.ifBlank { "মুসলিম ব্যবহারকারী" },
                    "district" to profile.district.ifBlank { "Dhaka" },
                    "calculationMethod" to profile.calculationMethod.ifBlank { "ISLAMIC_FOUNDATION_BD" },
                    "madhhab" to profile.madhhab.ifBlank { "HANAFI" },
                    "language" to profile.language.ifBlank { "bn" },
                    "themeMode" to profile.themeMode.ifBlank { "DARK" },
                    "tasbihAllTimeTotal" to profile.tasbihAllTimeTotal,
                    "qazaFajr" to profile.qazaFajr,
                    "qazaDhuhr" to profile.qazaDhuhr,
                    "qazaAsr" to profile.qazaAsr,
                    "qazaMaghrib" to profile.qazaMaghrib,
                    "qazaIsha" to profile.qazaIsha,
                    "qazaWitr" to profile.qazaWitr,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                profile.email?.takeIf { it.isNotBlank() }?.let { updatePayload["email"] = it }
                profile.phone?.takeIf { it.isNotBlank() }?.let { updatePayload["phone"] = it }
                profile.photoUrl?.takeIf { it.isNotBlank() }?.let { updatePayload["photoUrl"] = it }
                docRef.update(updatePayload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    fun observeSalatLogs(): Flow<List<SalatLog>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/salat_logs"
        emitAll(
            db.collection("users").document(uid).collection("salat_logs")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SalatLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveSalatLog(log: SalatLog): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = log.dateKey.ifBlank { log.id.ifBlank { "2026-11-10" } }
        val docRef = db.collection("users").document(uid).collection("salat_logs").document(docId)
        try {
            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                val createPayload = mapOf(
                    "userId" to uid,
                    "dateKey" to docId,
                    "fajr" to log.fajr,
                    "dhuhr" to log.dhuhr,
                    "asr" to log.asr,
                    "maghrib" to log.maghrib,
                    "isha" to log.isha,
                    "witr" to log.witr,
                    "duha" to log.duha,
                    "awwabin" to log.awwabin,
                    "tahajjud" to log.tahajjud,
                    "fasting" to log.fasting,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            } else {
                val updatePayload = mapOf(
                    "fajr" to log.fajr,
                    "dhuhr" to log.dhuhr,
                    "asr" to log.asr,
                    "maghrib" to log.maghrib,
                    "isha" to log.isha,
                    "witr" to log.witr,
                    "duha" to log.duha,
                    "awwabin" to log.awwabin,
                    "tahajjud" to log.tahajjud,
                    "fasting" to log.fasting,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun getSalatLogByUserAndDate(targetUserId: String, dateKey: String): Result<SalatLog?> = runCatching {
        val docRef = db.collection("users").document(targetUserId).collection("salat_logs").document(dateKey)
        try {
            val snap = docRef.get().await()
            snap.toObject(SalatLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    fun observeBookmarks(): Flow<List<QuranBookmark>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/bookmarks"
        emitAll(
            db.collection("users").document(uid).collection("bookmarks")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        QuranBookmark::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addBookmark(bookmark: QuranBookmark): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "s${bookmark.surahNumber}_a${bookmark.ayahNumber}"
        val docRef = db.collection("users").document(uid).collection("bookmarks").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "surahNumber" to bookmark.surahNumber,
            "ayahNumber" to bookmark.ayahNumber,
            "surahNameBn" to bookmark.surahNameBn,
            "surahNameEn" to bookmark.surahNameEn,
            "arabicPreview" to bookmark.arabicPreview,
            "translationPreview" to bookmark.translationPreview,
            "note" to bookmark.note,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun removeBookmark(bookmarkId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bookmarks").document(bookmarkId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
