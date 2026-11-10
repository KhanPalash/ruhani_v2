package com.example.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object GoogleAuthHelper {

    suspend fun attemptAutoSignIn(context: Context): Boolean {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            handleSignInResult(result.credential)
            true
        } catch (e: NoCredentialException) {
            false
        } catch (e: GetCredentialException) {
            Log.w("GoogleAuth", "Auto sign-in failed", e)
            false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("GoogleAuth", "Unexpected error during auto sign-in", e)
            false
        }
    }

    suspend fun onGoogleSignInClicked(
        context: Context,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val credentialManager = CredentialManager.create(context)
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
            serverClientId = context.getString(R.string.default_web_client_id)
        ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            handleSignInResult(result.credential)
            onSuccess()
        } catch (e: GetCredentialCancellationException) {
            Log.d("GoogleAuth", "User cancelled Google Sign-In")
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuth", "Credential Manager error", e)
            onError(e.localizedMessage ?: "Google Sign-In failed. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Firebase Auth error", e)
            onError(e.localizedMessage ?: "Authentication failed.")
        }
    }

    private suspend fun handleSignInResult(credential: Credential) {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(
                googleIdTokenCredential.idToken,
                null
            )
            FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
        } else {
            throw IllegalStateException("Unexpected credential type: ${credential.type}")
        }
    }

    suspend fun signOut(context: Context) {
        withContext(NonCancellable) {
            FirebaseAuth.getInstance().signOut()
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: ClearCredentialException) {
                Log.w("GoogleAuth", "Failed to clear credential state", e)
            }
        }
    }
}
