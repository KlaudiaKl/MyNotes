package com.klaudia.mynotes.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.klaudia.mynotes.model.Category

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryHolder(
    category: Category,
    onClick: (String) -> Unit,
    onLongClick: (String) -> Unit,
    modifier:  Modifier?
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
            .padding(8.dp)
            .combinedClickable (
                onClick = {onClick(category._id.toHexString())},
                onLongClick = {
                    onLongClick(category._id.toHexString())
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = category.categoryName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview
@Composable
fun CategoryHolderPrev() {
    val cat  = Category().apply {
        categoryName = "Random Name"
    }
    CategoryHolder(category = cat, onClick = {}, onLongClick = {}, modifier = null)
}