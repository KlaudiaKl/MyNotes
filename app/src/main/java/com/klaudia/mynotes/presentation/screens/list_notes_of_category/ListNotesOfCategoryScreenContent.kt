package com.klaudia.mynotes.presentation.screens.list_notes_of_category

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.klaudia.mynotes.R
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.presentation.components.NoteHolder
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListNotesOfCategoryScreenContent(
    notes: Map<LocalDate, List<Note>>,
    onClick: (String) -> Unit,
    categoryName : String
) {
    if (notes.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding()

        ) {
            notes.forEach { (localDate, entries) ->
                items(
                    items = entries,
                    key = { it._id.toString() }
                ) {
                    NoteHolder(note = it, onClick = onClick, categoryName = categoryName, color = "")
                }
            }
        }
    } else {
        Column (modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.there_are_no_notes_of_this_category_yet),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(12.dp)
            )

            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Icon", tint = MaterialTheme.colorScheme.inversePrimary)
            Text(
                text = stringResource(R.string.use_the_button_below_to_add_a_note_to_this_category),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp)
            )

        }
        
    }
}