package com.klaudia.mynotes.presentation.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.klaudia.mynotes.R

@Composable
fun AddNewCategoryDialog(
    onDialogOpen: Boolean,
    onDialogClosed: () -> Unit,
    onCategoryAdded: (String, String) -> Unit,
    currentName: String = "",
    currentColor: String = "",
    title: String,
    text: String
) {
    val colorOptions = listOf("#FF5733", "#FFC300", "#3EC73C", "#654DFD", "#900C3F")
    var categoryName by remember { mutableStateOf(currentName) }
    var selectedColor by remember { mutableStateOf(currentColor) }
    var isError by remember { mutableStateOf(false) }



    if (onDialogOpen) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDialogClosed,
            title = { Text(text = title) },
            text = {
                Column {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = { Text(text = text) },
                        isError = isError,
                        supportingText = {
                            if (isError) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(R.string.name_cannot_be_empty),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Choose a color:")
                    Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        colorOptions.forEach { color ->
                            val colorInt = Color(android.graphics.Color.parseColor(color))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colorInt)
                                    .border(
                                        border = if (selectedColor == color) BorderStroke(2.dp, Color.Green) else BorderStroke(0.dp, Color.Transparent),
                                        shape = CircleShape
                                    )
                                    .padding(10.dp)
                                    .clickable { selectedColor = color },
                                content = {}
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = {
                        if (categoryName.isNotBlank()) {
                            onCategoryAdded(categoryName, selectedColor)
                            //categoryName = ""
                            onDialogClosed()
                        } else {
                            isError = true
                        }
                    }
                ) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDialogClosed)
                {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}