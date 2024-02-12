package com.klaudia.mynotes.presentation

import android.util.Log
import com.klaudia.mynotes.data.MongoDbRepositoryImpl
import com.klaudia.mynotes.data.MongoDbRepositoryImpl.deleteAllNotesOfCategory
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.RequestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class SharedRepository @Inject constructor() {


    suspend fun updateCategory(
        category: Category,
        categoryId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = MongoDbRepositoryImpl.updateCategory(category = category.apply {
            _id = ObjectId.invoke(categoryId!!)
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

    suspend fun insertCategory(
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String, color: String
    ) {
        val result = MongoDbRepositoryImpl.addCategory(category = category.apply {}, name = name, color)
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
        category:Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String?, color: String
    ) {
            if (categoryId != null) {
                updateCategory(category = category, onSuccess = onSuccess, onError = onError, categoryId = categoryId)
            } else {
                if (name != null) {
                    insertCategory(category = category, onSuccess = onSuccess, onError = onError, name, color)
                }
            }
    }
}

