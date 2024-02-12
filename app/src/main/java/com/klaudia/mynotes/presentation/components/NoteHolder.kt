package com.klaudia.mynotes.presentation.components

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.util.toColor
import io.realm.kotlin.types.RealmInstant
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteHolder(
    note: Note,
    onClick: (String) -> Unit,
    categoryName: String,
    color: String
) {
    Row(modifier = Modifier.clickable(indication = null,
        interactionSource = remember {
            MutableInteractionSource()
        }) { onClick(note._id.toHexString()) }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))

                .background(MaterialTheme.colorScheme.surface)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.inversePrimary,
                    shape = RoundedCornerShape(8.dp)
                )

            //shadowElevation = 4.dp
        ) {
            Column {
                //title
                Text(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 8.dp,
                        end = 8.dp,
                        bottom = 4.dp
                    ),
                    text = note.title,
                    style = TextStyle(fontSize = MaterialTheme.typography.titleSmall.fontSize),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatDate(dateString = note.dateCreated),
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.labelSmall.fontSize
                    ), modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                //content preview
                Text(
                    text = note.content,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                    style = TextStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                //category, if exists
                if (note.categoryId != null && categoryName != "") {
                    Log.d("color", color)
                    var bgColor = if(!color.isNullOrEmpty()){
                        color.toColor()
                    }
                    else MaterialTheme.colorScheme.background

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgColor)
                            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = categoryName,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                            style = TextStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun NoteHolderPreview() {
    val note: Note = Note().apply {
        content = "Some content"
        title = "Example title"
    }
    NoteHolder(note = note, onClick = {}, categoryName = "Category Name", color = "#FF3700B3")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun formatDate(dateString: RealmInstant): String {
    val instant =
        Instant.ofEpochSecond(dateString.epochSeconds, dateString.nanosecondsOfSecond.toLong())

    // Format the Instant as needed
    val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return instant.atZone(ZoneOffset.UTC).format(outputFormatter)
}