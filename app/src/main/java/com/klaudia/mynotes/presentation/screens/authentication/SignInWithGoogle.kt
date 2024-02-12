package com.klaudia.mynotes.presentation.screens.authentication

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.klaudia.mynotes.model.RequestState

@Composable
fun SignInWithGoogle(
    viewModel: AuthenticationViewModel = hiltViewModel(),
    navigateToHomeScreen: (signedIn: Boolean) -> Unit
) {
    when(val signInResponse = viewModel.signInWithGoogleResponse) {
        is RequestState.Success -> signInResponse.data?.let{
            signedIn ->
            LaunchedEffect(signedIn ){
                navigateToHomeScreen(signedIn)
            }
        }
        is RequestState.Error -> LaunchedEffect(Unit){ Log.d("SIGN IN ERROR", signInResponse.error.message.toString())}
        RequestState.Idle -> {
            TODO()
        }
        RequestState.Loading -> {
            TODO()
        }
    }
}