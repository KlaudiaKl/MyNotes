package com.klaudia.mynotes

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.klaudia.mynotes.presentation.components.LoginWithGoogleButton
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginWithGoogleButtonTest() {

    @get:Rule
    val composeTestRule = createComposeRule()


    @Test
    fun loginButton_displays_CorrectText_whenNotLoading() {
        val buttonText = "Sign in with Google"

        composeTestRule.setContent {
            LoginWithGoogleButton(
                text = buttonText,
                loadingState = false,
                onClick = {}
            )
        }

        composeTestRule.onNodeWithText(buttonText).assertIsDisplayed()
    }

    @Test
    fun loginButton_displaysLoadingText_whenLoading(){
         val loadingText = "Just a second"

        composeTestRule.setContent {
            LoginWithGoogleButton (
                loadingState = true,
                loadingText = loadingText,
                onClick = {}

            )

        }
        composeTestRule.onNodeWithText(loadingText).assertIsDisplayed()
    }

    @Test
    fun loginButton_triggersOnClick_whenClicked(){
        var clicked = false
        composeTestRule.setContent {
            LoginWithGoogleButton (
                text = "Sign in with Google",
                loadingState = false,
                onClick = {clicked = true}
            )
        }

        composeTestRule.onNodeWithText("Sign in with Google").performClick()

        assert(clicked)
    }

    @Test
    fun button_showsLoadingIndicator_whenLoading(){
        val loadingState = true
        composeTestRule.setContent {
            LoginWithGoogleButton (
                loadingState = loadingState,
                onClick = {}
            )
        }

        composeTestRule.onNodeWithTag("CircularProgressIndicator").assertIsDisplayed()
    }

    @Test
    fun button_hidesLoadingIndicator_whenNotLoading(){
        val loadingState = false
        composeTestRule.setContent {
            LoginWithGoogleButton (
                onClick = {},
                loadingState = loadingState
            )

        }

        composeTestRule.onNodeWithTag("CircularProgressIndicator").assertDoesNotExist()
    }

    @Test
    fun button_isDisabled_whenLoading(){
        val loadingState = true

        composeTestRule.setContent {
            LoginWithGoogleButton (
                text = "Just a second",
                onClick = {},
                loadingState = loadingState
            )
        }

        composeTestRule.onNodeWithText("Just a second...").assertIsNotEnabled()
    }

    @Test
    fun loginButton_displaysIconCorrectly() {
        // Arrange
        composeTestRule.setContent {
            LoginWithGoogleButton(onClick = {})
        }

        // Assert
        composeTestRule.onNodeWithContentDescription("Google Logo").assertIsDisplayed()
    }
}