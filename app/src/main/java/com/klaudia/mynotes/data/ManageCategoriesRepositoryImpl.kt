package com.klaudia.mynotes.data

import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class ManageCategoriesRepositoryImpl @Inject constructor(
    mongoDbRepository: MongoDbRepository,
    private val categoryService: CategoryService
) : BaseRepository(mongoDbRepository), ManageCategoriesRepository {

    override suspend fun deleteAllNotesOfCategory(categoryId: ObjectId) {
        mongoDbRepository.deleteAllNotesOfCategory(categoryId)
    }

    override fun getSelectedCategory(categoryId: ObjectId): Flow<RequestState<Category>> {
        return categoryService.getSelectedCategory(categoryId)
    }

    override suspend fun insertCategory(
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String, color: String
    ) {
        categoryService.insertCategory(category, onSuccess, onError, name, color)
    }

    override suspend fun updateCategory(
        category: Category,
        categoryId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        categoryService.updateCategory(category, categoryId, onSuccess, onError)
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

    override suspend fun deleteCategory(catId: ObjectId): RequestState<Boolean> {
        return mongoDbRepository.deleteCategory(catId)
    }

    override fun getCategories(): Flow<Categories> {
        return categoryService.getCategories()
    }
}