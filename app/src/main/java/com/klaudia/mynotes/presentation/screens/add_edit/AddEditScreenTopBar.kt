package com.klaudia.mynotes.presentation.screens.add_edit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.klaudia.mynotes.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreenTopBar(
    selectedNoteTitle: String?,
    onBackPressed: () -> Unit,
    onDeleteClicked: () -> Unit,
    onManageCategoriesClicked: () -> Unit,
    onShareClicked: () -> Unit
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back_arrow_icon)
                )
            }
        },
        title = {
            Text(
                text = selectedNoteTitle?.take(5) ?: stringResource(R.string.new_note),
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        },
        actions = {

            IconButton(onClick = {
                onDeleteClicked()
            }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete_entry))
            }

            IconButton(onClick = {
                onManageCategoriesClicked()
            }) {
                Icon(imageVector = Icons.Default.Folder, contentDescription = stringResource(R.string.add_category))
            }
            IconButton(onClick = {
                onShareClicked()
            }) {
                Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(R.string.share))
            }
        }
    )
}



    
