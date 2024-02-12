package com.klaudia.mynotes.presentation.screens.add_edit


import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable

import com.klaudia.mynotes.model.Note


@Composable
fun AddEditEntryScreen(
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onDeleteConfirmed: () -> Unit,
    onBackPressed: () -> Unit,
    onSaveClicked: (Note) -> Unit,
    uiState: UiState,
    onDeleteClicked: () -> Unit,
    onManageCategoriesClicked: () -> Unit,
    onFontSizeChange: (Double) -> Unit
) {
    Scaffold(
        topBar = {
            AddEditScreenTopBar(
                selectedNote = uiState.selectedNote,
                onBackPressed = onBackPressed,
                onDeleteClicked = onDeleteClicked,
                onManageCategoriesClicked = onManageCategoriesClicked
            )
        },

        content = { paddingValues ->
            AddEditScreenContent(
                uiState = uiState,
                title = uiState.title,
                onTitleChanged = onTitleChanged,
                content = uiState.content,
                onContentChanged = onContentChanged,
                paddingValues = paddingValues,
                onSaveClicked = onSaveClicked,
                fontSize = uiState.fontSize.toFloat(),
                onFontSizeChange = onFontSizeChange
            )
        }
    )
}