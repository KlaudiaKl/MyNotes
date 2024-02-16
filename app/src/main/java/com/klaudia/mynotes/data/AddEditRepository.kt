package com.klaudia.mynotes.data

import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId

interface AddEditRepository {
    fun getSelectedNote(noteId: ObjectId): Flow<RequestState<Note>>
    suspend fun insertNote(note :Note): RequestState<Note>
    suspend fun updateNote(note: Note): RequestState<Note>

    suspend fun deleteNote(noteId : ObjectId): RequestState<Boolean>
    fun getCategories(): Flow<Categories>

    }