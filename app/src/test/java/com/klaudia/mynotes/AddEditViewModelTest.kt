package com.klaudia.mynotes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.klaudia.mynotes.data.AddEditRepository
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.presentation.screens.add_edit.AddEditViewModel
import com.klaudia.mynotes.util.Constants
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mongodb.kbson.ObjectId


@ExperimentalCoroutinesApi
class AddEditViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AddEditViewModel
    private lateinit var addEditRepository: AddEditRepository
    private lateinit var  savedStateHandle : SavedStateHandle

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp(){
        addEditRepository = mockk(relaxed = true)
        //coEvery { savedStateHandle.get<String>(any()) } returns "testNoteId"
        savedStateHandle = mockk(relaxed = true)

        every { savedStateHandle.get<String>(Constants.ADD_EDIT_SCREEN_ARG_KEY.toString()) } returns "123456789876543212345678"
        every { savedStateHandle.get<String>(Constants.ADD_EDIT_CATEGORY_ARG_KEY) } returns "123456789876543212345678"
        viewModel = AddEditViewModel(savedStateHandle, addEditRepository)
    }

    @Test
    fun `when ViewModel is initialized, selected note is loaded`() = runTest {
        val expectedNoteId = ObjectId("123456789876543212345678")
        coEvery { addEditRepository.getSelectedNote(expectedNoteId.asObjectId()) } returns mockk(relaxed = true)
        viewModel
        verify {
            addEditRepository.getSelectedNote(expectedNoteId.asObjectId())
        }
    }

    @Test
    fun `when setTitle is called, Ui state is updated`() = runTest {
        val testTitle = "Test title"
        viewModel.setTitle(testTitle,)
        assert(viewModel.uiState.title == testTitle)
    }

    @Test
    fun `when setContent is called, Ui state is updated`() = runTest {
        val testContent = "Test content"
        viewModel.setContent(testContent)
        assert(viewModel.uiState.content == testContent)
    }

    @Test
    fun `verify getNoteIdArg retrieves and stores note ID correctly`() {
        // The ID should be retrieved in the init block, so no action needed here
        // asserting the ID was correctly stored.
        assertEquals("123456789876543212345678", viewModel.uiState.selectedNoteId)
    }
    @Test
    fun `verify getCategoryIdArg retrieves and stores note ID correctly`() {
        // The ID should be retrieved in the init block, so no action needed here
        // asserting the ID was correctly stored.
        assertEquals("123456789876543212345678", viewModel.uiState.categoryId)
    }

    @Test
    fun `verify getSelectedNote fetches note and updates UI state`() = runTest {
        val testNoteId = "123456789876543212345678"
        val testNote = Note().apply {
            _id = ObjectId(testNoteId)
            title = "Test Title"
            content = "Test Content"
            fontSize = 12.0
            categoryId = ObjectId()
        }

        coEvery { addEditRepository.getSelectedNote(ObjectId(testNoteId)) } returns flowOf(RequestState.Success(testNote))
        viewModel.getSelectedNote()
        advanceUntilIdle()

        assertEquals(testNote.title, viewModel.uiState.title)
        assertEquals(testNote.content, viewModel.uiState.content)
        //assertEquals(testNote.fontSize, viewModel.uiState.fontSize)
    }

}