package com.klaudia.mynotes

import com.klaudia.mynotes.data.MongoDbRepository
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.presentation.screens.list_notes_of_category.ListNotesOfCatViewModel
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mongodb.kbson.ObjectId
import java.time.LocalDate

class ListNotesOfCatViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ListNotesOfCatViewModel
    private val mockMongoDbRepository = mockk<MongoDbRepository>()

    @Before
    fun setup() {
        // Mocking the getAllNotesOfCategory function to return a successful state with empty data
        coEvery { mockMongoDbRepository.getAllNotesOfCategory(any()) } returns flowOf(RequestState.Success(mapOf<LocalDate, List<Note>>()))

        // Initialize your ViewModel here with the mocked MongoDbRepository
        viewModel = ListNotesOfCatViewModel(
            savedStateHandle = mockk(relaxed = true), // Mock SavedStateHandle with 'relaxed = true' for simplicity
            sharedRepository = mockMongoDbRepository // Use the mocked repository
        )
    }

    @Test
    fun `when getNotesOfCategory is called, notesOfCategory should update`() = runBlockingTest {
        // Assuming ObjectId("someId") is a valid ID you'd like to test with
        val categoryId = ObjectId("someId")
        viewModel.getNotesOfCategory(categoryId)

        // Verify that notesOfCategory is updated
        assertTrue(viewModel.notesOfCategory.value is RequestState.Success)
    }
}
