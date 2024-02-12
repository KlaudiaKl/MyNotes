package com.klaudia.mynotes.presentation.screens.list_notes_of_category

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.klaudia.mynotes.R
import com.klaudia.mynotes.data.Notes
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.RequestState


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ListNotesOfCategoryScreen(
    categoryUiState: CategoryUiState,
    navigateToAddEditScreenWithCategoryArg: (String) -> Unit,
    noteEntries: Notes,
    navigateToAddEditScreenWithArgs: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    //category: Category,
    categoryName: String,
    onSaveNameButtonClick: (Category) -> Unit
) {
    val padding by remember { mutableStateOf(PaddingValues()) }
    var isSaveButtonVisible by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current



    Scaffold(
       // modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {

        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(
                    end = padding.calculateEndPadding(LayoutDirection.Ltr)
                ),
                onClick = { navigateToAddEditScreenWithCategoryArg(categoryUiState.selectedCategoryId!!) }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "New Note Icon"
                )
            }
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues), verticalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TextField(
                    value = categoryName,
                    onValueChange = {
                        onNameChanged(it)
                        isSaveButtonVisible = true
                    },
                    modifier = Modifier
                        .padding(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Unspecified,
                        disabledIndicatorColor = Color.Unspecified,
                        unfocusedIndicatorColor = Color.Unspecified,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    ),
                    textStyle = MaterialTheme.typography.titleLarge,
                    trailingIcon = {
                        if (!isSaveButtonVisible) Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit name"
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done // Set the "Done" action
                    ),
                    keyboardActions = KeyboardActions(
                        onAny =  {focusManager.clearFocus()}

                    ),
                )
                SaveIconButton(
                    onClick = {
                        onSaveNameButtonClick(
                            Category().apply {
                                this.categoryName = categoryUiState.categoryName
                            }
                        )
                        keyboardController?.hide() // Hide the keyboard
                        focusManager.clearFocus()
                        isSaveButtonVisible = false
                    },
                    isValueChanged = isSaveButtonVisible
                )

            }

            when (noteEntries) {
                is RequestState.Success -> {
                    if (noteEntries.data != null) {
                        ListNotesOfCategoryScreenContent(
                            notes = noteEntries.data,
                            onClick = navigateToAddEditScreenWithArgs,
                            categoryName = ""
                        )
                        Log.d("ENTRIES", noteEntries.data.toString())
                    } else {
                        Log.d("EMPTY LIST", "noteEntries == null")
                    }
                }

                is RequestState.Error -> {
                    Log.d("ERROR home screen", "${noteEntries.error.message}")
                }

                RequestState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun SaveIconButton(
    onClick: () -> Unit,
    isValueChanged: Boolean
) {
    AnimatedVisibility(visible = isValueChanged) {
        Button(
            onClick = onClick,
            modifier = Modifier.padding(8.dp)
        ) {

            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "Save name",
                tint = MaterialTheme.colorScheme.onPrimary, // Change the color as needed
                modifier = Modifier.padding(4.dp) // Optional padding
            )
            Text(text = stringResource(R.string.save), style = MaterialTheme.typography.bodySmall)

        }
    }
}