package com.klaudia.mynotes.presentation.screens.add_edit

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.klaudia.mynotes.data.AddEditRepository
import com.klaudia.mynotes.data.Categories
import com.klaudia.mynotes.data.db.entity.ImageToDelete
import com.klaudia.mynotes.data.db.entity.ImageToDeleteDao
import com.klaudia.mynotes.data.db.entity.ImageToUpload
import com.klaudia.mynotes.data.db.entity.ImageToUploadDao
import com.klaudia.mynotes.model.Image
import com.klaudia.mynotes.model.ImagesState
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.util.Constants.ADD_EDIT_CATEGORY_ARG_KEY
import com.klaudia.mynotes.util.Constants.ADD_EDIT_SCREEN_ARG_KEY
import com.klaudia.mynotes.util.fetchImagesFromFirebase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val addEditRepository: AddEditRepository,
   private val imageToUploadDao: ImageToUploadDao,
    private val imageToDeleteDao: ImageToDeleteDao
    ) :
    ViewModel() {

    var uiState by mutableStateOf(UiState())
    var categories: MutableState<Categories> = mutableStateOf(RequestState.Idle)

    private val _shareNoteEvent = Channel<Intent>(Channel.BUFFERED)
    val shareNoteEvent = _shareNoteEvent.receiveAsFlow()

    val imagesState = ImagesState()


    init {
        getNoteIdArg()
        getCategoryIdArg()
        getSelectedNote()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCategories() {
        viewModelScope.launch {
            addEditRepository.getCategories().collect { result ->
                categories.value = result
                //Log.d("Categories:", categories.value.toString())
            }
        }
    }

fun getSelectedNote() {
        if (uiState.selectedNoteId != null) {
            viewModelScope.launch {
                addEditRepository.getSelectedNote(
                    noteId = ObjectId.invoke(
                        uiState.selectedNoteId!!
                    )
                )
                    .catch {
                        emit(RequestState.Error(Exception("Note already deleted")))
                    }
                    .collect { note ->
                        if (note is RequestState.Success) {
                            note.data?.let { setTitle(it.title) }
                            note.data?.let { setContent(it.content) }
                            note.data?.let { setFontSize(it.fontSize) }
                            note.data?.let {setCategoryId(it.categoryId.toString())}
                            note.data?.let {
                                fetchImagesFromFirebase(
                                    remoteImagePaths = it.images,
                                    onImageDownload = { downloadedImg ->
                                        imagesState.addImage(
                                        Image(
                                            image = downloadedImg,
                                            remoteImgPath = extractImagePath(
                                                fullImageUrl = downloadedImg.toString()
                                            )
                                        )
                                        )
                                    },
                                    onReadyToDisplay = { }
                                )
                            }
                        }
                    }
            }
        }
    }

    private fun getCategoryIdArg() {
        uiState = uiState.copy(
            categoryId = savedStateHandle.get<String>(
                key = ADD_EDIT_CATEGORY_ARG_KEY
            )
        )
    }

    private fun getNoteIdArg() {
        uiState = uiState.copy(
            selectedNoteId = savedStateHandle.get<String>(
                key = ADD_EDIT_SCREEN_ARG_KEY
            )
        )
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setContent(content: String) {
        uiState = uiState.copy(content = content)
    }
    fun setCategoryId(catId: String){
        uiState = uiState.copy(categoryId = catId)
    }

    @VisibleForTesting
    private suspend fun insertNote(
        note: Note,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val result = addEditRepository.insertNote(note = note.apply {
            if (!uiState.categoryId.isNullOrEmpty()) {categoryId=ObjectId.invoke(uiState.categoryId!!) }
        })

        if (result is RequestState.Success) {
            uploadImagesToFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    private suspend fun updateNote(
        note: Note,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = addEditRepository.updateNote(note = note.apply {
            _id = ObjectId.invoke(uiState.selectedNoteId!!)
            if (!uiState.categoryId.isNullOrEmpty()) {
                val hexString = uiState.categoryId!!.removePrefix("BsonObjectId(").removeSuffix(")")
                categoryId=ObjectId.invoke(hexString)
            }

        })
        if (result is RequestState.Success) {
            uploadImagesToFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }
    fun upsertNote(
        note: Note,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.selectedNoteId != null) {
                updateNote(note = note, onSuccess = onSuccess, onError = onError)
            } else {
                insertNote(note = note, onSuccess = onSuccess, onError = onError)
            }
        }
    }

    fun deleteNote(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.selectedNoteId != null) {
                val result = addEditRepository.deleteNote(noteId = ObjectId.invoke(uiState.selectedNoteId!!))
                if (result is RequestState.Success) {
                    withContext(Dispatchers.Main) {
                        uiState.selectedNote?.let { deleteImagesFromFirebase(it.images) }

                        onSuccess()
                    }
                } else if (result is RequestState.Error) {
                    withContext(Dispatchers.Main) {
                        onError(result.error.message.toString())
                    }
                }
            }
        }
    }

    fun setFontSize(size: Double) {
        uiState = uiState.copy(fontSize = size)
    }

    fun prepareShareNoteIntent(noteTitle: String, noteContent: String){
        val sendIntent: Intent = Intent().apply{
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, noteTitle)
            putExtra(Intent.EXTRA_TEXT, noteContent)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        viewModelScope.launch {
            _shareNoteEvent.send(shareIntent)
        }
    }


    fun addImage(image: Uri, imageType: String) {
        val remoteImagePath = "images/${FirebaseAuth.getInstance().currentUser?.uid}/" +
                "${image.lastPathSegment}-${System.currentTimeMillis()}.$imageType"
        imagesState.addImage(
            Image(
                image = image,
                remoteImgPath = remoteImagePath
            )
        )
    }

    private fun uploadImagesToFirebase() {
        val storage = FirebaseStorage.getInstance().reference
        imagesState.images.forEach { galleryImage ->
            val imagePath = storage.child(galleryImage.remoteImgPath)
            imagePath.putFile(galleryImage.image)
                .addOnProgressListener {
                    val sessionUri = it.uploadSessionUri
                    if (sessionUri != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToUploadDao.addImageToUpload(
                                ImageToUpload(
                                    remoteImagePath = galleryImage.remoteImgPath,
                                    imageUri = galleryImage.image.toString(),
                                    sessionUri = sessionUri.toString()
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun deleteImagesFromFirebase(images: List<String>? = null) {
        val storage = FirebaseStorage.getInstance().reference
        if (images != null) {
            images.forEach { remotePath ->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDao.addImageToDelete(
                                ImageToDelete(remoteImagePath = remotePath)
                            )
                        }
                    }
            }
        } else {
            imagesState.imagesToDelete.map { it.remoteImgPath }.forEach { remotePath ->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDao.addImageToDelete(
                                ImageToDelete(remoteImagePath = remotePath)
                            )
                        }
                    }
            }
        }
    }

    private fun extractImagePath(fullImageUrl: String): String {
        val chunks = fullImageUrl.split("%2F")
        val imageName = chunks[2].split("?").first()
        return "images/${Firebase.auth.currentUser?.uid}/$imageName"
    }
}

data class UiState(
    val selectedNoteId: String? = null,
    val selectedNote: Note? = null,
    val title: String = "",
    val content: String = "",
    val dateCreated: RealmInstant? = null,
    val categoryId: String? = null,
    val fontSize: Double = 16.0,

)