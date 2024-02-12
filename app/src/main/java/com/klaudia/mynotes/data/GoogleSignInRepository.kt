package com.klaudia.mynotes.data

import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.firebase.auth.AuthCredential
import com.klaudia.mynotes.model.RequestState

typealias OneTapSignInResponse = RequestState<BeginSignInResult>
typealias SignInWithGoogleResponse = RequestState<Boolean>

interface GoogleSignInRepository {
    val isUserAuthenticatedInFirebase: Boolean
    suspend fun oneTapSignIn(): OneTapSignInResponse
    suspend fun googleSignIn(credential: AuthCredential, isSuccessfull: () -> Unit): SignInWithGoogleResponse
}