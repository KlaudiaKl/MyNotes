package com.klaudia.mynotes.presentation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klaudia.mynotes.data.Categories
import com.klaudia.mynotes.data.MongoDbRepository
import com.klaudia.mynotes.data.Notes
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject
//this file is to be deleted later, not used anymore
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class SharedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: MongoDbRepository
): ViewModel() {
    var notes: MutableState<Notes> = mutableStateOf(RequestState.Idle)
    var categories: MutableState<Categories> = mutableStateOf(RequestState.Idle)
    private val _categoryName = MutableStateFlow("No Category")
    val categoryName: StateFlow<String> = _categoryName.asStateFlow()

    var categoryUiState by mutableStateOf(CategoryUiState())
    val categoryId: String? by derivedStateOf {
        categoryUiState.selectedCategoryId
    }

    private val _isSortAscending = MutableStateFlow(false)
    private val isSortAscending: StateFlow<Boolean> = _isSortAscending.asStateFlow()


    init {
        viewModelScope.launch {
            isSortAscending.collect{
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
                                    getCategoryDetails(categoryId).firstOrNull() ?: Pair("","")
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
        }
    }

    private fun getCategoryDetails(categoryId: ObjectId): Flow<Pair<String, String>> {
        return repository.getSelectedCategory(categoryId).map { state ->
            when (state) {
                is RequestState.Success -> {
                    val categoryName = state.data?.categoryName ?: ""
                    val categoryColor = state.data?.color ?: "" // Assuming the new color field is named 'color'
                    Pair(categoryName, categoryColor)
                }
                else -> Pair("", "")
            }
        }
    }

    private fun getSelectedCategoryName(categoryId: ObjectId): Flow<String> {
        return repository.getSelectedCategory(categoryId).map { state ->
            when (state) {
                is RequestState.Success -> state.data?.categoryName ?: ""
                else -> ""
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotes() {

        var sort = if (_isSortAscending.value){
            Sort.ASCENDING
        }
        else Sort.DESCENDING
        viewModelScope.launch {
            repository.getAllNotes(sort).collect { result ->
                when (result) {
                    is RequestState.Success -> {
                        notes.value = result
                        populateNoteCategoryNames() // Populate category names after fetching notes
                    }
                    else -> notes.value = result
                }
            }
        }

    }
    // sorting
    fun toggleSortOrder() {
        viewModelScope.launch {
            _isSortAscending.value = !_isSortAscending.value // Toggle the sort order
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { result ->
                categories.value = result
                Log.d("Categories:", categories.value.toString())
            }
        }

    }

    suspend fun insertCategory(
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String,
        color: String
    ) {
        val result = repository.addCategory(category = category.apply {}, name = name, catColor = color)
        if (result is RequestState.Success) {

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    //////////////


    fun getSelectedCategoryId(id: String){
        categoryUiState = categoryUiState.copy(
            selectedCategoryId = id
        )
    }
    fun setName(categoryName: String) {
        categoryUiState = categoryUiState.copy(categoryName = categoryName)
    }

    private suspend fun updateCategory(
        category: Category,
        categoryId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = repository.updateCategory(category = category.apply {
            _id = ObjectId.invoke(categoryId)
        })
        if (result is RequestState.Success) {
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    fun upsertCategory(
        categoryId: String?,
        category:Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String?,
        color: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
        if (categoryId != null&& name != null) {
            val updatedCategory = category.apply {
                this.categoryName = name
                this.color = color
                _id = ObjectId.invoke(categoryId)
            }
            updateCategory(category = updatedCategory, onSuccess = onSuccess, onError = onError, categoryId = categoryId)
        }
        else {
            if (name != null) {
                val newCategory = Category().apply {
                    this.categoryName = name
                }
                insertCategory(category = newCategory, onSuccess = onSuccess, onError = onError, name, color)
            }
        }
        }
    }

    fun deleteCategory(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (categoryUiState.selectedCategoryId != null) {
                Log.d("DELETION", "delete cat function called")
                deleteAllNotesOfCategory(ObjectId(categoryUiState.selectedCategoryId.toString()) )
                val result = repository.deleteCategory(id = ObjectId.invoke(categoryUiState.selectedCategoryId!!))
                if (result is RequestState.Success) {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else if (result is RequestState.Error) {
                    withContext(Dispatchers.Main) {
                        onError(result.error.message.toString())
                    }
                }
            }
            else{
                Log.d("DELETION", "delete cat function called but ID is null")
            }
        }
    }

    private fun deleteAllNotesOfCategory(categoryId: ObjectId){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllNotesOfCategory(categoryId)
        }
    }

}

data class CategoryUiState(
    val selectedCategoryId: String? = null,
    val selectedCategory: Category? = null,
    val categoryName: String = "none"
)

