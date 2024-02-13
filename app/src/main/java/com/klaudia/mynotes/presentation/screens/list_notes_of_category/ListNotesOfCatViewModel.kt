package com.klaudia.mynotes.presentation.screens.list_notes_of_category


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klaudia.mynotes.data.Categories
import com.klaudia.mynotes.data.ListNotesOfCategoryRepository

import com.klaudia.mynotes.data.Notes
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ListNotesOfCatViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val listNotesOfCategoryRepository: ListNotesOfCategoryRepository
) : ViewModel() {
    var categoryUiState by mutableStateOf(CategoryUiState())
    var categories: MutableState<Categories> = mutableStateOf(RequestState.Idle)
    var notesOfCategory: MutableState<Notes> = mutableStateOf(RequestState.Idle)
    init {
        getCategoryIdArg()
        getSelectedCategory()
        val catId = categoryUiState.selectedCategoryId?.let { ObjectId(it) }
        if (catId != null) {
            Log.d("getnotesofcategory", catId.toHexString())
            getNotesOfCategory(catId)
        }
    }

    val categoryId =
        if (categoryUiState.selectedCategoryId != null) categoryUiState.selectedCategoryId else null

    private fun getSelectedCategory() {
        if (categoryUiState.selectedCategoryId != null) {
            viewModelScope.launch {
                listNotesOfCategoryRepository.getSelectedCategory(
                    categoryId = org.mongodb.kbson.ObjectId.invoke(
                        categoryUiState.selectedCategoryId!!
                    )
                )
                    .catch {
                        emit(RequestState.Error(Exception("Item already deleted")))
                    }
                    .collect { category ->
                        if (category is RequestState.Success) {
                            category.data?.let { setName(it.categoryName) }
                        }
                    }
            }
        } else {
            Log.d("VIEWMODEL 62", "category id is null")
        }
    }

    private fun getCategoryIdArg() {
        categoryUiState = categoryUiState.copy(
            selectedCategoryId = savedStateHandle.get<String>(
                key = Constants.MANAGE_CATEGORIES_SCREEN_ARG_KEY
            )
        )
    }

    fun setName(categoryName: String) {
        categoryUiState = categoryUiState.copy(categoryName = categoryName)
    }

    fun upsertCategory(
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String?, color : String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (categoryId != null) {
                listNotesOfCategoryRepository.upsertCategory(categoryId, category, onSuccess, onError, name, color)
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotesOfCategory(categoryId: ObjectId) {
        viewModelScope.launch {
            listNotesOfCategoryRepository.getAllNotesOfCategory(categoryId).collect { result ->
                notesOfCategory.value = result
            }
        }
    }
}

data class CategoryUiState(
    val selectedCategoryId: String? = null,
    val selectedCategory: Category? = null,
    val categoryName: String = "none"
)