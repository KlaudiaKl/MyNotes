package com.klaudia.mynotes.presentation.screens.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klaudia.mynotes.data.Categories
import com.klaudia.mynotes.data.MongoDbRepositoryImpl
import com.klaudia.mynotes.data.Notes
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.time.LocalDate

//@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel : ViewModel() {

    var notes: MutableState<Notes> = mutableStateOf(RequestState.Idle)
    var categories: MutableState<Categories> = mutableStateOf(RequestState.Idle)
    private val _categoryName = MutableStateFlow("No Category")
    val categoryName: StateFlow<String> = _categoryName.asStateFlow()

    init {
        getNotes()
        getCategories()
    }

    private fun populateNoteCategoryNames() {
        viewModelScope.launch {
            // Assuming notes.value is already populated with notes and their categories
            val updatedNotes = notes.value.let { currentState ->
                when (currentState) {
                    is RequestState.Success -> {
                        val notesMap = currentState.data ?: return@let currentState
                        val updatedNotesMap = mutableMapOf<LocalDate, List<Note>>()

                        notesMap.forEach { (date, noteList) ->
                            val updatedNoteList = noteList.map { note ->
                                val category = note.categoryId?.let { categoryId ->
                                    getSelectedCategoryName(categoryId).firstOrNull() ?: ""
                                }
                                note.apply {
                                    this.categoryName = category // Assign the fetched category name to the note
                                }
                            }
                            updatedNotesMap[date] = updatedNoteList
                        }
                        RequestState.Success(updatedNotesMap)
                    }
                    else -> currentState // No operation if not Success state
                }
            }

            notes.value = updatedNotes // Update the notes state with category names included
        }
    }

    private suspend fun getSelectedCategoryName(categoryId: ObjectId): Flow<String> {
        return MongoDbRepositoryImpl.getSelectedCategory(categoryId).map { state ->
            when (state) {
                is RequestState.Success -> state.data?.categoryName ?: "No Category"
                else -> "No Category"
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotes() {

        viewModelScope.launch {
            MongoDbRepositoryImpl.getAllNotes(Sort.DESCENDING).collect { result ->
                when (result) {
                    is RequestState.Success -> {
                        notes.value = result
                        populateNoteCategoryNames() // Populate category names after fetching notes
                    }
                    else -> notes.value = result // Handle other states (Error, Loading)
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getCategories() {
        viewModelScope.launch {
            MongoDbRepositoryImpl.getAllCategories().collect { result ->
                categories.value = result
                Log.d("Categories:", categories.value.toString())
            }
        }
    }


}