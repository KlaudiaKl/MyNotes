package com.klaudia.mynotes.presentation.screens.manage_categories

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klaudia.mynotes.data.Categories
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.presentation.components.CategoryHolder

@Composable
fun ManageCategoriesScreenContent(
    categories: Categories,
    onClick: (String) -> Unit,
    onLongClick: (String) -> Unit,
    paddingValues: PaddingValues,
) {
    when (categories) {
        is RequestState.Success -> {
            val categoriesList = categories.data ?: emptyList()
            Log.d("Categories:", categoriesList.toString())
            if (categoriesList.isNotEmpty()) {
                LazyColumn(modifier = Modifier.padding(12.dp)) {
                    categoriesList.forEach { cat ->
                        item(key = cat._id.toString()) {
                            CategoryHolder(
                                category = cat,
                                onClick = onClick,
                                onLongClick = onLongClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
        // Handle other states (Loading, Error) if needed
        else -> {}
    }
}