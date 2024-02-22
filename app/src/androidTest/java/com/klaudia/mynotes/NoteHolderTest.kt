package com.klaudia.mynotes

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.presentation.components.NoteHolder
import io.realm.kotlin.types.RealmInstant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mongodb.kbson.ObjectId


@RunWith(AndroidJUnit4::class)
class NoteHolderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun noteHolder_DisplaysNote(){
        val note = Note().apply {
            _id = ObjectId()
            title = "Test Note"
            content = "This is a test note content."
            dateCreated = RealmInstant.now()
            categoryId = ObjectId()
            categoryName = "Test Category Name"
            categoryColor = "#FFFFF"

        }

        var clickedNoteId = ObjectId()

        //assuming categoryName and categoryColor are present for this Note
        composeTestRule.setContent {
            NoteHolder(
                note = note,
                onClick = { clickedNoteId = ObjectId(it)},
                categoryName = note.categoryName?:"Name not found",
                color = note.categoryColor!!)
        }

        composeTestRule.onNodeWithText(note.title).assertExists()
        composeTestRule.onNodeWithText(note.content).assertExists()
        composeTestRule.onNodeWithText(note.categoryName!!).assertExists()

        composeTestRule.onNodeWithText(note.title).performClick()
        assert(clickedNoteId == note._id)
    }
}