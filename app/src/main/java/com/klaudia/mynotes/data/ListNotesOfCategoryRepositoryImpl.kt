package com.klaudia.mynotes.data

import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class ListNotesOfCategoryRepositoryImpl @Inject constructor(
    mongoDbRepository: MongoDbRepository,
    private val categoryService: CategoryService
) : BaseRepository(mongoDbRepository), ListNotesOfCategoryRepository {

    override suspend fun updateCategory(
        category: Category,
        categoryId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        categoryService.updateCategory(category, categoryId, onSuccess, onError)
    }

    override suspend fun insertCategory(
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String,
        color: String
    ) {
        categoryService.insertCategory(category, onSuccess, onError, name, color)
    }

    override suspend fun upsertCategory(
        categoryId: String?,
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String?, color: String
    ) {
        categoryService.upsertCategory(categoryId, category, onSuccess, onError, name, color)
    }

    override fun getAllNotesOfCategory(categoryId: ObjectId): Flow<Notes> {
        return mongoDbRepository.getAllNotesOfCategory(categoryId)
    }

    override fun getSelectedCategory(categoryId: ObjectId): Flow<RequestState<Category>> {
        return categoryService.getSelectedCategory(categoryId)
    }
}