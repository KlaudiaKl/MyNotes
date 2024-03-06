package com.klaudia.mynotes.presentation.screens.add_edit

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.klaudia.mynotes.model.Image
import com.klaudia.mynotes.model.ImagesState
import com.klaudia.mynotes.model.Note
@Composable
fun AddEditEntryScreen(
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    //onDeleteConfirmed: () -> Unit,
    onBackPressed: () -> Unit,
    onSaveClicked: (Note) -> Unit,
    uiState: UiState,
    onDeleteClicked: () -> Unit,
    onManageCategoriesClicked: () -> Unit,
    onFontSizeChange: (Double) -> Unit,
    onShareClicked: () -> Unit,
    imagesState: ImagesState,
    onImageSelect: (Uri) -> Unit,
    onImageDeleteClicked: (Image) -> Unit
) {

    var selectedGalleryImage by remember { mutableStateOf<Image?>(null) }
    Scaffold(
        topBar = {
            AddEditScreenTopBar(
                selectedNoteTitle = uiState.title,
                onBackPressed = onBackPressed,
                onDeleteClicked = onDeleteClicked,
                onManageCategoriesClicked = onManageCategoriesClicked,
                onShareClicked = onShareClicked
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
                onFontSizeChange = onFontSizeChange,
                imagesState = imagesState,
                onImageSelect = onImageSelect,
                onImageClicked = { selectedGalleryImage = it}
            )
            AnimatedVisibility(visible = selectedGalleryImage != null) {
                Dialog(onDismissRequest = { selectedGalleryImage = null }) {
                    if (selectedGalleryImage != null) {
                        ZoomableImage(
                            selectedGalleryImage = selectedGalleryImage!!,
                            onCloseClicked = { selectedGalleryImage = null },
                            onDeleteClicked = {
                                if (selectedGalleryImage != null) {
                                    onImageDeleteClicked(selectedGalleryImage!!)
                                    selectedGalleryImage = null
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ZoomableImage(
    selectedGalleryImage: Image,
    onCloseClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = maxOf(1f, minOf(scale * zoom, 5f))
                    val maxX = (size.width * (scale - 1)) / 2
                    val minX = -maxX
                    offsetX = maxOf(minX, minOf(maxX, offsetX + pan.x))
                    val maxY = (size.height * (scale - 1)) / 2
                    val minY = -maxY
                    offsetY = maxOf(minY, minOf(maxY, offsetY + pan.y))
                }
            }
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = maxOf(.5f, minOf(3f, scale)),
                    scaleY = maxOf(.5f, minOf(3f, scale)),
                    translationX = offsetX,
                    translationY = offsetY
                ),
            model = ImageRequest.Builder(LocalContext.current)
                .data(selectedGalleryImage.image.toString())
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Fit,
            contentDescription = "Gallery Image"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onCloseClicked) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Icon")
                Text(text = "Close")
            }
            Button(onClick = onDeleteClicked) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                Text(text = "Delete")
            }
        }
    }
}