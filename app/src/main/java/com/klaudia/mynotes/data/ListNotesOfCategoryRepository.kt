package com.klaudia.mynotes.data

import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId

interface ListNotesOfCategoryRepository {

    suspend fun updateCategory(
        category: Category,
        categoryId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    suspend fun insertCategory(
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String,
        color: String
    )

    suspend fun upsertCategory(
        categoryId: String?,
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String?, color: String
    )

    fun getAllNotesOfCategory(categoryId: ObjectId): Flow<Notes>

    fun getSelectedCategory(categoryId: ObjectId): Flow<RequestState<Category>>
}