package com.klaudia.mynotes.data


import android.util.Log
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.RequestState
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(mongoDbRepository: MongoDbRepository, private val categoryService: CategoryService) : BaseRepository(mongoDbRepository), HomeRepository {

    override fun getNotes(sort: Sort): Flow<Notes> {
        return mongoDbRepository.getAllNotes(sort)
    }

    override fun getCategories(): Flow<Categories> {
        return categoryService.getCategories()
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

    override fun getNotesWithCategoryDetails(sort: Sort): Flow<Notes> {
        val notesFlow = mongoDbRepository.getAllNotes(sort)
        val categoriesFlow = mongoDbRepository.getAllCategories()

        return combine(notesFlow, categoriesFlow) { notes, categories ->
            // Handle different combinations of RequestState for notes and categories
            when {
                notes is RequestState.Success && categories is RequestState.Success -> {

                    val categoriesMap = categories.data?.associateBy { it._id }
                    val updatedNotes = notes.data?.mapValues { entry ->
                        entry.value.map { note ->
                            note.apply {
                                categoryId?.let { id ->
                                    categoriesMap?.get(id)?.also { category ->
                                        categoryName = category.categoryName
                                        categoryColor = category.color
                                    }
                                }
                            }
                        }
                    }
                    Log.d("updated", updatedNotes?.entries.toString())
                    RequestState.Success(updatedNotes)
                }

                notes is RequestState.Error -> notes
                categories is RequestState.Error -> RequestState.Error(categories.error)
                else -> {Log.d("CombineFlow", "One of the flows is not in Success state")
                    RequestState.Loading}
            }
        }.onStart {
            emit(RequestState.Loading)
        }.catch { e ->
            emit(RequestState.Error(e))
        }
    }
}