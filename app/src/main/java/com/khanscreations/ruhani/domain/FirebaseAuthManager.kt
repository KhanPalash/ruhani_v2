npm install -g firebase-toolspackage com.khanscreations.ruhani.domain

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

/**
 * Firebase Authentication Manager for Ruhani.
 * Handles Email/Password registration, login, password reset, and profile updates.
 * Google Sign-in is handled separately by [com.khanscreations.ruhani.ui.auth.GoogleAuthHelper].
 *
 * This replaces the old local PBKDF2-based [SecureAuthAndPrivacyManager] for auth operations.
 * SecureAuthAndPrivacyManager is still used for privacy preferences and offline session caching.
 */
object FirebaseAuthManager {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    val isSignedIn: Boolean get() = auth.currentUser != null

    // ─────────────────────────── Email / Password ───────────────────────────

    /**
     * Register a new user with email and password, then update their display name.
     * Returns [Result.success] with the [FirebaseUser] on success.
     */
    suspend fun registerWithEmailPassword(
        displayName: String,
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val trimmedEmail = email.trim().lowercase()
            val trimmedName = displayName.trim().ifBlank { "মুসলিম ব্যবহারকারী" }

            val result = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
            val user = result.user
                ?: return Result.failure(IllegalStateException("ব্যবহারকারী তৈরি হয়নি।"))

            // Update display name in Firebase Auth profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(trimmedName)
                .build()
            user.updateProfile(profileUpdates).await()

            Log.d("FirebaseAuthManager", "Email registration success: ${user.uid}")
            Result.success(user)
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(IllegalArgumentException(
                "পাসওয়ার্ড খুব দুর্বল। কমপক্ষে ৬ অক্ষরের পাসওয়ার্ড ব্যবহার করুন।"
            ))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(IllegalStateException(
                "এই ইমেইলে ইতোমধ্যে একটি অ্যাকাউন্ট রয়েছে। অনুগ্রহ করে লগইন করুন।"
            ))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(IllegalArgumentException(
                "সঠিক ইমেইল ঠিকানা প্রদান করুন।"
            ))
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Registration error", e)
            Result.failure(e)
        }
    }

    /**
     * Sign in an existing user with email and password.
     * Returns [Result.success] with the [FirebaseUser] on success.
     */
    suspend fun loginWithEmailPassword(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val trimmedEmail = email.trim().lowercase()
            val result = auth.signInWithEmailAndPassword(trimmedEmail, password).await()
            val user = result.user
                ?: return Result.failure(IllegalStateException("লগইন ব্যর্থ হয়েছে।"))

            Log.d("FirebaseAuthManager", "Email login success: ${user.uid}")
            Result.success(user)
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(IllegalArgumentException(
                "এই ইমেইলে কোনো অ্যাকাউন্ট পাওয়া যায়নি। প্রথমে নিবন্ধন (Register) করুন।"
            ))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(IllegalArgumentException(
                "ইমেইল বা পাসওয়ার্ড ভুল হয়েছে। আবার চেষ্টা করুন।"
            ))
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Login error", e)
            Result.failure(e)
        }
    }

    /**
     * Send a password reset email to the given address.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim().lowercase()).await()
            Log.d("FirebaseAuthManager", "Password reset email sent to $email")
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(IllegalArgumentException(
                "এই ইমেইলে কোনো অ্যাকাউন্ট পাওয়া যায়নি।"
            ))
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Password reset error", e)
            Result.failure(e)
        }
    }

    /**
     * Update the display name of the currently signed-in user.
     */
    suspend fun updateDisplayName(newName: String): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("কোনো ব্যবহারকারী সাইন ইন করেননি।"))
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName.trim().ifBlank { "মুসলিম ব্যবহারকারী" })
                .build()
            user.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Display name update error", e)
            Result.failure(e)
        }
    }

    /**
     * Delete the current user account from Firebase Auth.
     * Note: Firestore data deletion must be handled separately before calling this.
     */
    suspend fun deleteCurrentAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("কোনো ব্যবহারকারী সাইন ইন করেননি।"))
            user.delete().await()
            Log.d("FirebaseAuthManager", "Account deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Account deletion error", e)
            Result.failure(e)
        }
    }

    /**
     * Sign out the current Firebase user.
     */
    fun signOut() {
        auth.signOut()
        Log.d("FirebaseAuthManager", "User signed out")
    }

    /**
     * Returns a [LocalAuthSession] from the current Firebase user, or null if not signed in.
     * This bridges the existing [LocalAuthSession] data class used throughout the app.
     */
    fun buildLocalSessionFromFirebaseUser(user: FirebaseUser): LocalAuthSession {
        val provider = when {
            user.providerData.any { it.providerId == "google.com" } -> "GOOGLE"
            else -> "EMAIL_PASSWORD"
        }
        return LocalAuthSession(
            userId = user.uid,
            email = user.email ?: "",
            displayName = user.displayName?.takeIf { it.isNotBlank() } ?: "মুসলিম ব্যবহারকারী",
            authProvider = provider,
            createdAtEpochMs = user.metadata?.creationTimestamp ?: System.currentTimeMillis()
        )
    }
}

