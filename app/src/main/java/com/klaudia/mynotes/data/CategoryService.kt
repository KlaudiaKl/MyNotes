package com.klaudia.mynotes.data

import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.RequestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class CategoryService @Inject constructor(private val mongoDbRepository: MongoDbRepository) {
    fun getSelectedCategory(categoryId: ObjectId): Flow<RequestState<Category>> {
        return mongoDbRepository.getSelectedCategory(categoryId)
    }

    fun getCategories(): Flow<Categories> {
        return mongoDbRepository.getAllCategories()
    }


    suspend fun insertCategory(
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String, color: String
    ) {
        val result = mongoDbRepository.addCategory(category = category.apply {}, name = name, color)
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

    suspend fun updateCategory(
        category: Category,
        categoryId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = mongoDbRepository.updateCategory(category = category.apply {
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

    suspend fun upsertCategory(
        categoryId: String?,
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String?, color: String
    ) {
       /* if (categoryId != null) {
            updateCategory(
                category = category,
                onSuccess = onSuccess,
                onError = onError,
                categoryId = categoryId
            )
        } else {
            if (name != null) {
                insertCategory(
                    category = category,
                    onSuccess = onSuccess,
                    onError = onError,
                    name,
                    color
                )
            }
        }*/

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