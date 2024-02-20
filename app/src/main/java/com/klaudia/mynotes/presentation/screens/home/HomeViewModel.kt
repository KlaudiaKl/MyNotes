package com.klaudia.mynotes.presentation.screens.home

import android.os.Build
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    private val _notesWithCategories = MutableStateFlow<RequestState<Map<LocalDate, List<Note>>>>(RequestState.Loading)
    val notesWithCategories: StateFlow<RequestState<Map<LocalDate, List<Note>>>> = _notesWithCategories.asStateFlow()

    private val _isSortAscending = MutableStateFlow(false)
    private val isSortAscending: StateFlow<Boolean> = _isSortAscending.asStateFlow()

    init {
        viewModelScope.launch {
            isSortAscending.collect {
                    sortAscending ->
                fetchNotesAndCategories(if (sortAscending) Sort.ASCENDING else Sort.DESCENDING)
            }
        }
        getCategories()
    }


    private fun populateNoteCategoryNames() {
        viewModelScope.launch {
            // Check if notes are in the Success state and if categories are also loaded successfully
            val notesState = notes.value
            val categoriesState = categories.value

            if (notesState is RequestState.Success && categoriesState is RequestState.Success) {
                val notesMap = notesState.data ?: emptyMap()
                val categoriesList = categoriesState.data ?: emptyList()
                val categoryMap = categoriesList.associateBy { it._id }

                val updatedNotesMap = notesMap.mapValues { entry ->
                    entry.value.map { note ->
                        note.apply {
                            categoryId?.let { id ->
                                categoryMap[id]?.let { category ->
                                    categoryName = category.categoryName
                                    categoryColor = category.color
                                }
                            }
                        }
                    }
                }

                notes.value = RequestState.Success(updatedNotesMap)
            }
        }
    }

    // sorting
    fun toggleSortOrder() {
        viewModelScope.launch {
            _isSortAscending.value = !_isSortAscending.value // Toggle the sort order
        }
    }

    fun fetchNotesAndCategories(sort: Sort) {
        viewModelScope.launch {
            //var sort = if (_isSortAscending.value){
               // Sort.ASCENDING
           // }
            //else Sort.DESCENDING
            homeRepository.getNotesWithCategoryDetails(sort) // or your desired sort
                .collect { state ->
                    _notesWithCategories.value = state
                }
        }
    }

   /* fun getNotes() {
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
    }*/


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