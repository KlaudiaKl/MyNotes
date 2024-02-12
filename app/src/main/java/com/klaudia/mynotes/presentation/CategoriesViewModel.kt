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
import com.klaudia.mynotes.data.MongoDbRepositoryImpl
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class CategoriesViewModel@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sharedRepository: SharedRepository): ViewModel() {

    private var categoryUiState by mutableStateOf(CategoryUiState())
    val categoryId: String? by derivedStateOf {
        categoryUiState.selectedCategoryId
    }
    var categories: MutableState<Categories> = mutableStateOf(RequestState.Idle)

    init {
        getCategoryIdArg()
        getSelectedCategory()
    }


    private fun getSelectedCategory() {
        if (categoryUiState.selectedCategoryId != null) {
            viewModelScope.launch {
                MongoDbRepositoryImpl.getSelectedCategory(
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


    private fun getCategoryIdArg() {
        categoryUiState = categoryUiState.copy(
            selectedCategoryId = savedStateHandle.get<String>(
                key = Constants.MANAGE_CATEGORIES_SCREEN_ARG_KEY
            )
        )
    }

    fun getSelectedCategoryId(id: String){
        categoryUiState = categoryUiState.copy(
            selectedCategoryId = id
        )
    }
    fun setName(categoryName: String) {
        categoryUiState = categoryUiState.copy(categoryName = categoryName)
    }
    fun deleteCategory(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (categoryUiState.selectedCategoryId != null) {
                Log.d("DELETION", "delete cat function called")
                deleteAllNotesOfCategory(ObjectId(categoryUiState.selectedCategoryId.toString()) )
                val result = MongoDbRepositoryImpl.deleteCategory(id = ObjectId.invoke(categoryUiState.selectedCategoryId!!))
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
            MongoDbRepositoryImpl.deleteAllNotesOfCategory(categoryId)
        }
    }

    fun upsertCategory(
        category: Category,
        name: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        color: String

    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("catViewModel id", categoryId.toString())
                sharedRepository.upsertCategory(categoryId, category, onSuccess, onError, name, color)
        }
    }
}
