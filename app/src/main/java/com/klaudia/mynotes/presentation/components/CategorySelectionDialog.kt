package com.klaudia.mynotes.presentation.components


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.klaudia.mynotes.R
import com.klaudia.mynotes.data.Categories
import com.klaudia.mynotes.model.RequestState

@Composable
fun CategorySelectionDialog(
    noteCatId: String?,
    categories: Categories,
    onDialogOpen: Boolean,
    onDialogClosed: ()->Unit,
    onConfirm:(String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String>(noteCatId?:"") }

    if(onDialogOpen) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { onDialogClosed() },
            title = {},
            text = {
                //list all categories
                //check the category where note.categoryId == category.categoryId
                LazyColumn {
                    if (categories is RequestState.Success && !categories.data.isNullOrEmpty()) {
                        items(categories.data) { category ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedCategory == category._id.toString(),
                                    onClick = {
                                        selectedCategory = category._id.toString()
                                    }
                                   
                                )
                                Text(text = category.categoryName)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm(selectedCategory)
                        onDialogClosed()
                    })
                {
                    Text(text = stringResource(R.string.ok))
                }
                },
            dismissButton = {Button(onClick = { onDialogClosed() }) {
                Text(text = stringResource(R.string.cancel))
            }}

        )

    }
}