package com.klaudia.mynotes.data

import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId

interface ManageCategoriesRepository {
    suspend fun deleteAllNotesOfCategory(categoryId: ObjectId)
    fun getSelectedCategory(categoryId: ObjectId): Flow<RequestState<Category>>
    suspend fun insertCategory(
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String, color: String
    )

    suspend fun updateCategory(
        category: Category,
        categoryId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    suspend fun upsertCategory(
        categoryId: String?,
        category:Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String?, color: String
    )

    suspend fun deleteCategory(catId: ObjectId): RequestState<Boolean>

    fun getCategories(): Flow<Categories>
}