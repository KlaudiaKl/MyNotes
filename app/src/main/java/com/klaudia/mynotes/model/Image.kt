package com.klaudia.mynotes.model

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

data class Image(
    val image: Uri,
    val remoteImgPath: String = ""
)

@Composable
fun rememberImagesState(): ImagesState {
    return remember { ImagesState()}
}
class ImagesState{
    val images = mutableStateListOf<Image>()
    val imagesToDelete = mutableStateListOf<Image>()

     fun addImage(image: Image){
         images.add(image)
     }

    fun deleteImage(image: Image){
        images.remove(image)
        imagesToDelete.add(image)
    }

    fun clearImagesToDelete(){
        imagesToDelete.clear()
    }


}
