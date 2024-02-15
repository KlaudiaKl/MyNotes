package com.klaudia.mynotes.data

import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import java.time.LocalDate

interface HomeRepository {
    fun getNotes(sort: Sort): Flow<Notes>
    fun getCategories(): Flow<Categories>
    fun getSelectedCategory(categoryId: ObjectId): Flow<RequestState<Category>>
    suspend fun insertCategory(category: Category, onSuccess: () -> Unit, onError: (String) -> Unit, name: String, color: String)

    fun getNotesWithCategoryDetails(sort: Sort): Flow<RequestState<Map<LocalDate, List<Note>>>>
}