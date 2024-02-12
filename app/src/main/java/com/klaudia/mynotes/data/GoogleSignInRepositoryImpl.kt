package com.klaudia.mynotes.data

import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.util.Constants.CREATED_AT
import com.klaudia.mynotes.util.Constants.DISPLAY_NAME
import com.klaudia.mynotes.util.Constants.EMAIL
import com.klaudia.mynotes.util.Constants.PHOTO_URL
import com.klaudia.mynotes.util.Constants.SIGN_IN_REQUEST
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named


class GoogleSignInRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @Named(SIGN_IN_REQUEST)
    private var signInRequest: BeginSignInRequest,
    private var oneTapClient: SignInClient,
    private var signUpRequest: BeginSignInRequest,
    private val db: FirebaseFirestore
) : GoogleSignInRepository {

    override val isUserAuthenticatedInFirebase = firebaseAuth.currentUser != null

    override suspend fun oneTapSignIn(): OneTapSignInResponse {
        return try {
            val signInResult = oneTapClient.beginSignIn(signInRequest).await()
            RequestState.Success(signInResult)
        } catch (e: Exception) {
            try {
                val signUpResult = oneTapClient.beginSignIn(signUpRequest).await()
                RequestState.Success(signUpResult)
            } catch (e: Exception) {
                RequestState.Error(e)
            }
        }
    }

    override suspend fun googleSignIn(
        credential: AuthCredential,
        isSuccessfull: () -> Unit
    ): SignInWithGoogleResponse {
        return try {
            val authResult =
                firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isSuccessfull()
                    } else {
                        Log.d("GOOGLE SIGN IN", "Couldn't sign in to firebase")
                    }
                }.await()
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
            if (isNewUser) {
                addUserToFirestore()
            }
            RequestState.Success(true)
        } catch (e: Exception) {
            RequestState.Error(e)
        }
    }

    private suspend fun addUserToFirestore() {
        firebaseAuth.currentUser?.apply {
            val user = toUser()
            db.collection("users").document(uid).set(user).await()
        }
    }

    fun FirebaseUser.toUser() = mapOf(
        DISPLAY_NAME to displayName,
        EMAIL to email,
        PHOTO_URL to photoUrl?.toString(),
        CREATED_AT to FieldValue.serverTimestamp()
    )
}