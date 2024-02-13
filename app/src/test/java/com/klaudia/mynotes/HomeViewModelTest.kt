package com.klaudia.mynotes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.klaudia.mynotes.data.HomeRepositoryImpl
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.presentation.screens.home.HomeViewModel
import com.klaudia.mynotes.util.toRealmInstant
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.Dispatchers


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mongodb.kbson.ObjectId

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel
    private lateinit var  homeRepositoryImpl: HomeRepositoryImpl

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp(){
        homeRepositoryImpl = mockk(relaxed = true) //`relaxed = true` allows for unmocked calls to return default values

        every { homeRepositoryImpl.getCategories() } returns flowOf(RequestState.Success(emptyList<Category>()))

        val mockNoteList = listOf(
            Note().apply {
                _id = ObjectId() // Generates a new ObjectId
                ownerId = "testOwnerId"
                title = "Test Note 1"
                content = "This is the content of test note 1."
                dateCreated = java.time.Instant.now().atZone(ZoneId.systemDefault()).toInstant().toRealmInstant()
                categoryId = null
                fontSize = 16.0
            },
            Note().apply {
                _id = ObjectId()
                ownerId = "testOwnerId"
                title = "Test Note 2"
                content = "This is the content of test note 2."
                dateCreated = java.time.Instant.now().atZone(ZoneId.systemDefault()).toInstant().toRealmInstant()
                categoryId = null
                fontSize = 16.0
            }
        )
        val mockNotesDate = LocalDate.now()
        val mockNotes = flowOf(RequestState.Success(mapOf(mockNotesDate to mockNoteList)))
        coEvery {homeRepositoryImpl.getNotes(Sort.DESCENDING)} returns mockNotes
        viewModel = HomeViewModel(homeRepositoryImpl)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // Resetting the main dispatcher to the original Main dispatcher
        Dispatchers.resetMain()

    }

    @Test
    fun `getNotes updates notes state on success`() = runTest{
        viewModel.getNotes()
        val result = viewModel.notes.value
        assertTrue("Expectednotes.value to be RequestState.Success", result is RequestState.Success)
    }

    @Test
    fun testGetCategories() = runTest {

        viewModel.getCategories()
        TODO()
        // Add assertions here
    }

}