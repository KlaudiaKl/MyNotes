package com.klaudia.mynotes.presentation.screens.authentication

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.klaudia.mynotes.model.RequestState

@Composable
fun OneTapSignIn(
    viewModel: AuthenticationViewModel = hiltViewModel(),
    launch: (result: BeginSignInResult) -> Unit

) {
    when(val oneTapSignInResponse = viewModel.oneTapSignInResponse) {
        is RequestState.Loading -> {

        }
        is RequestState.Success -> oneTapSignInResponse.data?.let {
            LaunchedEffect(it) {
                launch(it)
            }
        }
        is RequestState.Error -> LaunchedEffect(Unit) {
            Log.d("ONE TAP ERROR", oneTapSignInResponse.error.message.toString())
        }
        RequestState.Idle -> {

        }
    }
}