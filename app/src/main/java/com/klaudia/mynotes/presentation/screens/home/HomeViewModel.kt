package com.klaudia.mynotes.presentation.screens.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klaudia.mynotes.data.Categories
import com.klaudia.mynotes.data.HomeRepositoryImpl
import com.klaudia.mynotes.data.Notes
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepositoryImpl
) : ViewModel() {

    var notes: MutableState<Notes> = mutableStateOf(RequestState.Idle)
    var categories: MutableState<Categories> = mutableStateOf(RequestState.Idle)
    private val _categoryName = MutableStateFlow("No Category")
    val categoryName: StateFlow<String> = _categoryName.asStateFlow()

    private val _isSortAscending = MutableStateFlow(false)
    private val isSortAscending: StateFlow<Boolean> = _isSortAscending.asStateFlow()

    init {
        viewModelScope.launch {
            isSortAscending.collect {
                getNotes()
            }
        }
        getCategories()
    }

    private fun populateNoteCategoryNames() {
        viewModelScope.launch {

            val updatedNotes = notes.value.let { currentState ->
                when (currentState) {
                    is RequestState.Success -> {
                        val notesMap = currentState.data ?: return@let currentState
                        val updatedNotesMap = mutableMapOf<LocalDate, List<Note>>()

                        notesMap.forEach { (date, noteList) ->
                            val updatedNoteList = noteList.map { note ->
                                val categoryDetails = note.categoryId?.let { categoryId ->
                                    getCategoryDetails(categoryId).firstOrNull() ?: Pair("", "")
                                }
                                note.apply {
                                    if (categoryDetails != null) {
                                        this.categoryName = categoryDetails.first
                                    }
                                    if (categoryDetails != null) {
                                        this.categoryColor = categoryDetails.second
                                    }
                                    // Assigning the fetched category name to the note
                                }
                            }
                            updatedNotesMap[date] = updatedNoteList
                        }
                        RequestState.Success(updatedNotesMap)
                    }

                    else -> currentState // if not Success state do nothing
                }
            }
            notes.value = updatedNotes // Update the notes state with category names included
            Log.d("populateNotes called", "true")
        }
    }

    private fun getCategoryDetails(categoryId: ObjectId): Flow<Pair<String, String>> {
        return homeRepository.getSelectedCategory(categoryId).map { state ->
            when (state) {
                is RequestState.Success -> {
                    val categoryName = state.data?.categoryName ?: ""
                    val categoryColor =
                        state.data?.color ?: ""
                    Pair(categoryName, categoryColor)
                }

                else -> Pair("", "")
            }
        }
    }

    // sorting
    fun toggleSortOrder() {
        viewModelScope.launch {
            _isSortAscending.value = !_isSortAscending.value // Toggle the sort order
        }
    }



    fun getNotes() {
        var sort = if (_isSortAscending.value){
            Sort.ASCENDING
        }
        else Sort.DESCENDING
        viewModelScope.launch {
            homeRepository.getNotes(sort).collect { result ->
                when (result) {
                    is RequestState.Success -> {
                        notes.value = result
                        populateNoteCategoryNames()
                    }

                    else -> notes.value = result // Handle other states (Error, Loading)
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getCategories() {
        viewModelScope.launch {
            homeRepository.getCategories().collect { result ->
                categories.value = result
                //Log.d("Categories:", categories.value.toString())
            }
        }
    }

    fun insertCategory(
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String, color: String
    ) {
        viewModelScope.launch {
            homeRepository.insertCategory(category, onSuccess, onError,name, color)
        }

    }
}