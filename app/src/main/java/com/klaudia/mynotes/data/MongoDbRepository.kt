package com.klaudia.mynotes.data

import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import java.time.LocalDate

typealias Notes = RequestState<Map<LocalDate, List<Note>>>
typealias Categories = RequestState<List<Category>>
interface MongoDbRepository {
    fun getAllNotes(sort: Sort) : Flow<Notes>
    fun getSelectedNote(noteId: ObjectId): Flow<RequestState<Note>>
    fun getSelectedCategory(categoryId: ObjectId): Flow<RequestState<Category>>
    fun realmConfiguration()
    suspend fun insertNote(note: Note): RequestState<Note>
    suspend fun updateNote(note: Note): RequestState<Note>
    suspend fun deleteNote(id: ObjectId): RequestState<Boolean>
    suspend fun deleteCategory(id: ObjectId): RequestState<Boolean>
    suspend fun addCategory(category: Category, name: String, catColor:String): RequestState<Category>
    fun getAllCategories(): Flow<Categories>
    fun getAllNotesOfCategory(categoryId: ObjectId): Flow<Notes>
    suspend fun deleteAllNotesOfCategory(categoryId: ObjectId): RequestState<Boolean>
    suspend fun updateCategory(category: Category): RequestState<Category>
}