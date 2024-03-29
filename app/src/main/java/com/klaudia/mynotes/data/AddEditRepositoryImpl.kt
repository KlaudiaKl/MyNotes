package com.klaudia.mynotes.data

import com.klaudia.mynotes.data.db.entity.ImageToDeleteDao
import com.klaudia.mynotes.data.db.entity.ImageToUploadDao
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class AddEditRepositoryImpl @Inject constructor(
    mongoDbRepository: MongoDbRepository,
    imageToDelete: ImageToDeleteDao,
    imageToUploadDao: ImageToUploadDao
) : BaseRepository(mongoDbRepository), AddEditRepository {

    override fun getSelectedNote(noteId: ObjectId): Flow<RequestState<Note>> {
        return mongoDbRepository.getSelectedNote(noteId)
    }

    override suspend fun insertNote(note :Note): RequestState<Note> {
       return mongoDbRepository.insertNote(note)
    }

    override suspend fun updateNote(note: Note): RequestState<Note> {
        return mongoDbRepository.updateNote(note)
    }

    override suspend fun deleteNote(noteId : ObjectId): RequestState<Boolean> {
        return mongoDbRepository.deleteNote(noteId)
    }

    override fun getCategories(): Flow<Categories> {
        return mongoDbRepository.getAllCategories()
    }
}