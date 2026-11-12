package com.khanscreations.ruhani.data.repository

import com.khanscreations.ruhani.base.FirestoreEmulatorTestBase
import com.khanscreations.ruhani.data.model.QuranBookmark
import com.khanscreations.ruhani.data.model.SalatLog
import com.khanscreations.ruhani.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [36], manifest = Config.NONE)
class RuhaniRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndObserveUserProfile_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = RuhaniFirestoreRepository(firestore)

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveUserProfile(
                UserProfile(
                    userId = uid,
                    displayName = "আবদুল্লাহ রহমান",
                    district = "Dhaka",
                    tasbihAllTimeTotal = 99L
                )
            )
        }
        assertTrue(saveResult.isSuccess)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProfile().first { it != null && it.tasbihAllTimeTotal == 99L }
        }
        assertNotNull(observed)
        assertEquals("আবদুল্লাহ রহমান", observed?.displayName)
    }

    @Test
    fun saveAndObserveSalatLog_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = RuhaniFirestoreRepository(firestore)
        val dateKey = "2026-11-${UUID.randomUUID().toString().take(6)}"

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveSalatLog(
                SalatLog(
                    userId = uid,
                    dateKey = dateKey,
                    fajr = true,
                    dhuhr = true,
                    asr = true
                )
            )
        }
        assertTrue(createResult.isSuccess)

        val logs = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSalatLogs().first { list -> list.any { it.dateKey == dateKey } }
        }
        assertTrue(logs.any { it.dateKey == dateKey && it.fajr && it.dhuhr && it.asr })
    }

    @Test
    fun addAndObserveBookmark_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = RuhaniFirestoreRepository(firestore)

        val addResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addBookmark(
                QuranBookmark(
                    userId = uid,
                    surahNumber = 36L,
                    ayahNumber = 1L,
                    surahNameBn = "ইয়াসিন",
                    surahNameEn = "Ya-Sin",
                    arabicPreview = "يس",
                    translationPreview = "ইয়াসিন",
                    note = "Heart of the Quran"
                )
            )
        }
        assertTrue(addResult.isSuccess)

        val bookmarks = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeBookmarks().first { list -> list.any { it.surahNumber == 36L } }
        }
        assertTrue(bookmarks.any { it.surahNameEn == "Ya-Sin" })
    }

    @Test
    fun getSalatLog_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = RuhaniFirestoreRepository(firestore)
        val dateKey = "2026-11-${UUID.randomUUID().toString().take(6)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveSalatLog(SalatLog(userId = aliceUid, dateKey = dateKey, fajr = true)).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = RuhaniFirestoreRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getSalatLogByUserAndDate(aliceUid, dateKey)
        }
        assertTrue(result.isFailure)
        val firestoreEx = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun getUserProfile_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = RuhaniFirestoreRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getUserProfile(aliceUid)
        }
        assertTrue(result.isFailure)
        val firestoreEx = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@ruhani.test"
        const val BOB_EMAIL = "bob@ruhani.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
