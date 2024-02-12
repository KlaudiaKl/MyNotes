package com.klaudia.mynotes.presentation.screens.authentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.klaudia.mynotes.data.GoogleSignInRepository
import com.klaudia.mynotes.data.OneTapSignInResponse
import com.klaudia.mynotes.data.SignInWithGoogleResponse
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.util.Constants.APP_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val repo: GoogleSignInRepository,
    val oneTapClient: SignInClient
): ViewModel() {


    val auth = FirebaseAuth.getInstance()
    var loadingState = mutableStateOf(false)
        private set

    fun setLoading(loading: Boolean) {
        loadingState.value = loading
    }

    var oneTapSignInResponse by mutableStateOf<OneTapSignInResponse>(RequestState.Success(null))
        private set
    var signInWithGoogleResponse by mutableStateOf<SignInWithGoogleResponse>(RequestState.Success(false))
        private set

    fun signInWithGoogle(credential: AuthCredential, isSuccessful: () -> Unit) = viewModelScope.launch{
        oneTapSignInResponse = RequestState.Loading
        signInWithGoogleResponse = repo.googleSignIn(credential = credential, isSuccessfull = isSuccessful)

    }
    fun oneTapSignIn() = viewModelScope.launch {
        oneTapSignInResponse = RequestState.Loading
        oneTapSignInResponse = repo.oneTapSignIn()
    }


    //MongoDB

    private var authenticated = mutableStateOf(false)
        private set

 fun signInWithMongoDB(
        tokenId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ){
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO){
                    App.create(APP_ID).login(
                        Credentials.jwt(tokenId)
                    //Credentials.google(tokenId, GoogleAuthType.ID_TOKEN)
                    ).loggedIn
                }
                withContext(Dispatchers.Main){
                    if(result){
                        onSuccess()
                        delay(600)
                        authenticated.value = true
                    }
                    else{
                        onError(Exception("User not logged in"))
                    }
                }
            }
            catch(e: Exception){
                withContext(Dispatchers.Main){
                    onError(e)
                }
            }
        }
    }
}
