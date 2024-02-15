package com.klaudia.mynotes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klaudia.mynotes.data.HomeRepositoryImpl
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.presentation.screens.home.HomeViewModel
import com.klaudia.mynotes.util.toRealmInstant

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every

import io.mockk.mockk
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mongodb.kbson.ObjectId
import java.time.LocalDate
import java.time.ZoneId

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel
    private lateinit var homeRepositoryImpl: HomeRepositoryImpl

    private val testDispatcher = StandardTestDispatcher()


    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        homeRepositoryImpl =
            mockk(relaxed = true) //`relaxed = true` allows for unmocked calls to return default values

        //every { homeRepositoryImpl.getCategories() } returns flowOf(RequestState.Success(emptyList<Category>()))

        val mockNoteList = listOf(
            Note().apply {
                _id = ObjectId() // Generates a new ObjectId
                ownerId = "testOwnerId"
                title = "Test Note 1"
                content = "This is the content of test note 1."
                dateCreated = java.time.Instant.now().atZone(ZoneId.systemDefault()).toInstant()
                    .toRealmInstant()
                categoryId = null
                fontSize = 16.0
            },
            Note().apply {
                _id = ObjectId()
                ownerId = "testOwnerId"
                title = "Test Note 2"
                content = "This is the content of test note 2."
                dateCreated = java.time.Instant.now().atZone(ZoneId.systemDefault()).toInstant()
                    .toRealmInstant()
                categoryId = null
                fontSize = 16.0
            }
        )
        val mockNotesDate = LocalDate.now()
        val mockNotes = RequestState.Success(mapOf(mockNotesDate to mockNoteList))
        coEvery { homeRepositoryImpl.getNotesWithCategoryDetails(Sort.DESCENDING) } returns flowOf(mockNotes)
        viewModel = HomeViewModel(homeRepositoryImpl)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // Resetting the main dispatcher to the original Main dispatcher
        Dispatchers.resetMain()

    }

    @Test
    fun `fetchNotesAndCategories updates notes state on success`() = runTest {
        viewModel.fetchNotesAndCategories()
        advanceUntilIdle()
        val result = viewModel.notesWithCategories.value
        assertTrue("Expectednotes.value to be RequestState.Success", result is RequestState.Success)

        val resultState = viewModel.notesWithCategories.value as RequestState.Success
        assertEquals("Verify the expected data is present", 1,
            resultState.data?.size)
    }

    @Test
    fun testGetCategories() = runTest {
        val expectedCategories = listOf(
            Category().apply {
                _id = ObjectId()
                ownerId = "Test owner id"
                categoryName = "Test Category 1"
                color = "Red"
            },
            Category().apply {
                _id = ObjectId()
                ownerId = "Test Owner id 2"
                categoryName = "Test Category 2"
                color = "Blue"
            }
        )

        // Setup the mock behavior for this test case
        every { homeRepositoryImpl.getCategories() } returns flowOf(
            RequestState.Success(
                expectedCategories
            )
        )

        // Executing the function under test
        viewModel.getCategories()

        // Waiting  for all coroutines launched by viewModel.getCategories() to complete
        advanceUntilIdle()

        // Asserting the expected outcome
        val result = viewModel.categories.value
        assertTrue(
            "Expected categories.value to be RequestState.Success with expected data",
            result is RequestState.Success && result.data == expectedCategories
        )

        val actualCategories = (result as RequestState.Success).data
        assertEquals(expectedCategories, actualCategories)
    }


    //testing category insertion
    @Test
    fun `insertCategory calls onSuccess in insertion`() = runTest {
        val mockCategory = Category()
        val name = "Test Category"
        val color = "Red"

        coEvery { homeRepositoryImpl.insertCategory(any(), any(), any(), name, color) } coAnswers {
            // Simulating calling onSuccess
            secondArg<() -> Unit>().invoke()
        }
        val onSuccessMock = mockk<() -> Unit>(relaxed = true)
        val onErrorMock = mockk<(String) -> Unit>(relaxed = true)

        viewModel.insertCategory(
            mockCategory,
            onSuccess = onSuccessMock,
            onError = onErrorMock,
            name,
            color
        )
        advanceUntilIdle()
        coVerify(exactly = 1) { onSuccessMock() }
        coVerify(exactly = 0) { onErrorMock(any()) }

    }

    //testing category insertion - failure
    @Test
    fun `insertCategory calls onError in insertion failure`() = runTest {
        val mockCategory = Category()
        val name = "Test Category"
        val color = "Red"
        val errorMessage = "Error inserting category"

        coEvery { homeRepositoryImpl.insertCategory(any(), any(), any(), name, color) } coAnswers {
            thirdArg<(String) -> Unit>().invoke(errorMessage)
        }

        val onSuccessMock = mockk<() -> Unit>(relaxed = true)
        val onErrorMock = mockk<(String) -> Unit>(relaxed = true)


        viewModel.insertCategory(
            mockCategory,
            onSuccess = onSuccessMock,
            onError = onErrorMock,
            name = name,
            color = color
        )
        advanceUntilIdle() //waiting for all the coroutines
        // Assert
        coVerify(exactly = 0) { onSuccessMock() }
        coVerify(exactly = 1) { onErrorMock(errorMessage) }
    }

    //testing error handling in getNotes()
    @Test
    fun `get Notes updates notes state on error`() = runTest {

        val errorMsg = "Network error"
        coEvery { homeRepositoryImpl.getNotes(any())} returns flowOf(RequestState.Error(Exception(errorMsg)))

        viewModel.fetchNotesAndCategories()
        advanceUntilIdle()
        assertTrue(viewModel.notes.value is RequestState.Error)
        assertEquals(errorMsg, (viewModel.notes.value as RequestState.Error).error.message)

    }

    //testing error handling in getCatgories
    @Test
    fun `get Categories updates state on error` () = runTest {
        val errorMsg = "Network error"
        coEvery { homeRepositoryImpl.getCategories()} returns flowOf(RequestState.Error(Exception(errorMsg)))

        viewModel.getCategories()
        advanceUntilIdle()
        assertTrue(viewModel.categories.value is RequestState.Error)
        assertEquals(errorMsg, (viewModel.categories.value as RequestState.Error).error.message)
    }

    @Test
    fun `observes categories state flow updates`() = runTest {
        TODO()
    }
}