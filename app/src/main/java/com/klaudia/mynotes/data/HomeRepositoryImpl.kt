package com.klaudia.mynotes.data


import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.RequestState
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(mongoDbRepository: MongoDbRepository, private val categoryService: CategoryService) : BaseRepository(mongoDbRepository), HomeRepository {

    override fun getNotes(sort: Sort): Flow<Notes> {
        return mongoDbRepository.getAllNotes(sort)
    }

    override fun getCategories(): Flow<Categories> {
        return mongoDbRepository.getAllCategories()
    }

    override fun getSelectedCategory(categoryId: ObjectId): Flow<RequestState<Category>>{
       return mongoDbRepository.getSelectedCategory(categoryId)
    }

    override suspend fun insertCategory(
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        name: String,
        color: String
    ) {
        categoryService.insertCategory(category,onSuccess,onError,name, color)
    }
}