package com.klaudia.mynotes.data

import android.os.Build
import android.security.keystore.UserNotAuthenticatedException
import android.util.Log
import androidx.annotation.RequiresApi
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.util.Constants.APP_ID
import com.klaudia.mynotes.util.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MongoDbRepositoryImpl @Inject constructor() : MongoDbRepository {

    private val app = App.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm

    init {
        realmConfiguration()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getAllNotes(sort: Sort): Flow<Notes> {
        return if (user != null) {
            //Log.d("USER ID", user.id.toString())
            try {
                realm.query<Note>(query = "ownerId == $0", user.id)
                    .sort(property = "dateCreated", sortOrder = sort)
                    .asFlow()
                    .map { result ->
                        RequestState.Success(
                            data = result.list.groupBy {
                                it.dateCreated.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )
                    }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override fun getSelectedNote(noteId: ObjectId): Flow<RequestState<Note>> {
        return if (user != null) {
            try {
                realm.query<Note>(query = "_id == $0", noteId).asFlow().map {
                    RequestState.Success(data = it.list.first())
                }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getAllNotesOfCategory(categoryId: ObjectId): Flow<Notes> {
        return if (user != null) {
            //Log.d("USER ID", user.id.toString())
            try {
                realm.query<Note>(
                    query = "ownerId == $0  AND categoryId == $1",
                    user.id,
                    categoryId
                )
                    .sort(property = "dateCreated", sortOrder = Sort.DESCENDING)
                    .asFlow()
                    .map { result ->
                        RequestState.Success(
                            data = result.list.groupBy {
                                it.dateCreated.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )
                    }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override fun getSelectedCategory(categoryId: ObjectId): Flow<RequestState<Category>> {
        return if (user != null) {
            try {
                realm.query<Category>(query = "_id == $0", categoryId).asFlow().map {
                    RequestState.Success(data = it.list.firstOrNull())
                }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override suspend fun insertNote(note: Note): RequestState<Note> {
        return if (user != null) {
            realm.write {
                try {
                    val addedNote = copyToRealm(note.apply { ownerId = user.id })
                    RequestState.Success(data = addedNote)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun updateNote(note: Note): RequestState<Note> {
        return if (user != null) {
            realm.write {
                val queriedNote = query<Note>(query = "_id == $0", note._id).first().find()
                if (queriedNote != null) {
                    queriedNote.title = note.title
                    queriedNote.content = note.content
                    queriedNote.fontSize = note.fontSize
                    queriedNote.categoryId = note.categoryId
                    queriedNote.images= note.images

                    RequestState.Success(data = queriedNote)
                } else {
                    RequestState.Error(error = Exception("Queried Diary does not exist."))
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun updateCategory(category: Category): RequestState<Category> {
        return if (user != null) {
            realm.write {
                val queriedCategory =
                    query<Category>(query = "_id == $0", category._id).first().find()
                if (queriedCategory != null) {
                    queriedCategory.categoryName = category.categoryName
                    queriedCategory.color = category.color
                    RequestState.Success(data = queriedCategory)
                } else {
                    RequestState.Error(error = Exception("Queried Category does not exist."))
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteNote(id: ObjectId): RequestState<Boolean> {
        return if (user != null) {
            realm.write {
                val note =
                    query<Note>(query = "_id == $0 AND ownerId == $1", id, user.id)
                        .first().find()
                if (note != null) {
                    try {
                        delete(note)
                        RequestState.Success(data = true)
                    } catch (e: Exception) {
                        RequestState.Error(e)
                    }
                } else {
                    RequestState.Error(Exception("Entry does not exist."))
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun addCategory(category: Category, name: String, catColor:String): RequestState<Category> {
        return if (user != null) {
            realm.write {
                try {
                    val addedCategory = copyToRealm(category.apply {
                        ownerId = user.id
                        categoryName = name
                        color = catColor
                    })
                    RequestState.Success(data = addedCategory)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override fun getAllCategories(): Flow<Categories> {
        return if (user != null) {
            //Log.d("USER ID", user.id.toString())
            try {
                realm.query<Category>(query = "ownerId == $0", user.id)
                    .asFlow()
                    .map { result ->
                        RequestState.Success(
                            data = result.list
                        )
                    }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override suspend fun deleteCategory(id: ObjectId): RequestState<Boolean> {
        return if (user != null) {
            realm.write {
                val category =
                    query<Category>(query = "_id == $0 AND ownerId == $1", id, user.id)
                        .first().find()
                if (category != null) {
                    try {
                        delete(category)
                        RequestState.Success(data = true)
                    } catch (e: Exception) {
                        RequestState.Error(e)
                    }
                } else {
                    RequestState.Error(Exception("Entry does not exist."))
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteAllNotesOfCategory(categoryId: ObjectId): RequestState<Boolean> {
        return if (user != null) {
            realm.write {
                val notesOfCategory =
                    this.query<Note>("ownerId == $0 and categoryId == $1", user.id, categoryId)
                        .find()
                try {
                    delete(notesOfCategory)
                    RequestState.Success(data = true)
                } catch (e: Exception) {
                    Log.d("DELETE NOTESofCategory", e.toString())
                    RequestState.Error(e)

                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override fun realmConfiguration() {
        if (user != null) {
            val config = SyncConfiguration.Builder(user, setOf(Note::class, Category::class))
                .initialSubscriptions { sub ->
                    add(
                        query = sub.query<Note>(query = "ownerId == $0", user.id),
                        name = "User's Notes"
                    )
                    add(
                        query = sub.query<Category>(query = "ownerId == $0", user.id),
                        name = "User's Categories"
                    )

                }
                .log(LogLevel.ALL)
                .build()
            realm = Realm.open(config)
        }
    }
}