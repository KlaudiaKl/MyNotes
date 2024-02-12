package com.klaudia.mynotes.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.klaudia.mynotes.R

@Composable
fun FontSizeSlider(
    currentSize: Double,
    onSizeChange: (Double) -> Unit,
) {
    var sliderPosition by remember { mutableStateOf(currentSize.toFloat()) }
    Column {
        Text("${stringResource(R.string.font_size)} ${currentSize.toInt()}sp")
        Slider(
            value = sliderPosition,
            onValueChange = { newVal ->
                onSizeChange(newVal.toDouble())
                sliderPosition = newVal
            },
            valueRange = 12f..24f, // Define your range of font sizes
            onValueChangeFinished = {
                // This block is optional, depending on whether you want to take an action after user finishes dragging the slider
            }
        )
    }
}