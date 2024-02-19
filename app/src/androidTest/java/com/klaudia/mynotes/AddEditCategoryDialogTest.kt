package com.klaudia.mynotes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.klaudia.mynotes.presentation.components.AddNewCategoryDialog
import org.junit.Rule
import org.junit.Test


class AddEditCategoryDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dialog_ShouldBeDisplayed_When_onDialogOpen_IsTrue(){
        val title = "Add Category"
        val text = "Category name"

        composeTestRule.setContent {
            AddNewCategoryDialog(
                onDialogOpen = true,
                onDialogClosed = {},
                onCategoryAdded = { _, _ -> },
                title = title,
                text = text
            )
        }

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }
    @Test
    fun dialog_ShouldClose_And_Call_onDialogClosed_When_DismissButton_IsClicked() {
        var dialogClosed = false

        composeTestRule.setContent {
            AddNewCategoryDialog(
                onDialogOpen = true,
                onDialogClosed = { dialogClosed = true },
                onCategoryAdded = { _, _ -> },
                title = "Add Category",
                text = "Category Name"
            )
        }

        composeTestRule
            .onNodeWithText("CANCEL")
            .performClick()

        assert(dialogClosed)
    }
}