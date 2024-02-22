package com.klaudia.mynotes

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.klaudia.mynotes.presentation.components.formatDate
import com.klaudia.mynotes.util.toRealmInstant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class FormatDateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    fun TestFormatDate(){
        val testRealmInstant = Instant.now().toRealmInstant()
        BasicText(text = formatDate(dateString = testRealmInstant))
    }

    @Test
    fun formatDate_displaysCorrectly(){
        composeTestRule.setContent {
            TestFormatDate()
        }
        composeTestRule.onNodeWithText("22.02.2024").assertExists()
        //note: because the date we're testing is Instant.now, change the text above to current date when running this test.
    }
}