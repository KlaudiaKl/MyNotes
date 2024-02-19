package com.klaudia.mynotes

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.klaudia.mynotes.navigation.Screen
import com.klaudia.mynotes.navigation.SetupNavGraph
import com.klaudia.mynotes.ui.theme.MyNotesTheme
import com.klaudia.mynotes.util.Constants.APP_ID
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            MyNotesTheme(dynamicColor = false) {
                val navController = rememberNavController()
                SetupNavGraph(startDestination = getStartDestination(), navController = navController )

            }
        }
    }

    private fun getStartDestination(): String {
        val user = App.create(APP_ID).currentUser
        return if (user != null && user.loggedIn) Screen.HomeScreen.route
        else Screen.AuthenticationScreen.route
    }
}


