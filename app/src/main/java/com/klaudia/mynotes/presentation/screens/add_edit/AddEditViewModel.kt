package com.klaudia.mynotes.presentation.screens.add_edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klaudia.mynotes.data.AddEditRepository
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.util.Constants.ADD_EDIT_CATEGORY_ARG_KEY
import com.klaudia.mynotes.util.Constants.ADD_EDIT_SCREEN_ARG_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val addEditRepository: AddEditRepository
    ) :
    ViewModel() {

    var uiState by mutableStateOf(UiState())

    init {
        getNoteIdArg()
        getCategoryIdArg()
        getSelectedNote()
    }

    private fun getSelectedNote() {
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

    private suspend fun insertNote(
        note: Note,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val result = addEditRepository.insertNote(note = note.apply {})
        if (result is RequestState.Success) {

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
        })
        if (result is RequestState.Success) {
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
}

data class UiState(
    val selectedNoteId: String? = null,
    val selectedNote: Note? = null,
    val title: String = "",
    val content: String = "",
    val dateCreated: RealmInstant? = null,
    val categoryId: String? = null,
    val fontSize: Double = 16.0
)