package com.klaudia.mynotes.data

import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.RequestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId

abstract class BaseRepository(protected val mongoDbRepository: MongoDbRepository) {


}