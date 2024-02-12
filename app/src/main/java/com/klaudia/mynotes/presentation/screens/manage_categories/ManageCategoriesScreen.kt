package com.klaudia.mynotes.presentation.screens.manage_categories



import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.klaudia.mynotes.R
import com.klaudia.mynotes.data.Categories
import com.klaudia.mynotes.model.Category

@Composable
fun ManageCategoriesScreen(
    onAddCategoryClicked: (Category) -> Unit,
    navigateToCategoryScreenWithArgs: (String) -> Unit,
    navigateBack: () -> Unit,
    deleteCategory: () -> Unit,
    onLongClick: (String) -> Unit,
    usersCategories: Categories
) {
    var padding by remember { mutableStateOf(PaddingValues()) }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(
                    end = padding.calculateEndPadding(LayoutDirection.Ltr)
                ),
                onClick = {
                    onAddCategoryClicked(
                        Category().apply {
                            this.categoryName = ""
                        }
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CreateNewFolder,
                    contentDescription = "New Note Icon"
                )
            }
    }) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            //verticalArrangement = Arrangement.Center, // Center the content vertically
            // horizontalAlignment = Alignment.CenterHorizontally, // Center the content horizontally
        ) {

            Text(
                text = stringResource(R.string.your_categories),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.padding(12.dp))
            ManageCategoriesScreenContent(
                categories = usersCategories,
                onClick = { categoryId -> navigateToCategoryScreenWithArgs(categoryId) },
                paddingValues = paddingValues,
                onLongClick = onLongClick
            )
        }
    }
}
