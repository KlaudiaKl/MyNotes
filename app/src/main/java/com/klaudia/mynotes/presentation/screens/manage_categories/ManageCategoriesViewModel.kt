package com.klaudia.mynotes.presentation.screens.manage_categories

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
import com.klaudia.mynotes.data.ManageCategoriesRepository
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.presentation.CategoryUiState
import com.klaudia.mynotes.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ManageCategoriesViewModel @Inject constructor(
    private val categoriesRepository: ManageCategoriesRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var categoryUiState by mutableStateOf(CategoryUiState())
    val categoryId: String? by derivedStateOf {
        categoryUiState.selectedCategoryId
    }
    var categories: MutableState<Categories> = mutableStateOf(RequestState.Idle)

    private val _categoryName = MutableStateFlow("No Category")
    val categoryName: StateFlow<String> = _categoryName.asStateFlow()

    init {
        getCategoryIdArg()
        getSelectedCategory()
        getCategories()
    }

    private fun getCategoryIdArg() {
        categoryUiState = categoryUiState.copy(
            selectedCategoryId = savedStateHandle.get<String>(
                key = Constants.MANAGE_CATEGORIES_SCREEN_ARG_KEY
            )
        )
    }

    private fun deleteAllNotesOfCategory(categoryId: ObjectId){
        viewModelScope.launch(Dispatchers.IO) {
            categoriesRepository.deleteAllNotesOfCategory(categoryId)
        }
    }
    fun setName(categoryName: String) {
        categoryUiState = categoryUiState.copy(categoryName = categoryName)
    }
    private fun getSelectedCategory() {
        if (categoryUiState.selectedCategoryId != null) {
            viewModelScope.launch {
                categoriesRepository.getSelectedCategory(
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
        }
        else{
            Log.d("VIEWMODEL 62", "category id is null")
        }
    }

    fun getSelectedCategoryId(id: String){
        categoryUiState = categoryUiState.copy(
            selectedCategoryId = id
        )
    }

    fun upsertCategory(
        categoryId: String?,
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String?,
        color: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
           /* if (categoryId != null&& name != null) {
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
            }*/
            categoriesRepository.upsertCategory(categoryId,category,onSuccess, onError, name, color)
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
                val result = categoriesRepository.deleteCategory(catId  = ObjectId.invoke(categoryUiState.selectedCategoryId!!))
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCategories() {
        viewModelScope.launch {
            categoriesRepository.getCategories().collect { result ->
                categories.value = result
                Log.d("Categories:", categories.value.toString())
            }
        }

    }
}