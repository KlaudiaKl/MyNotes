package com.klaudia.mynotes.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.klaudia.mynotes.R

@Composable
fun LoginWithGoogleButton(
    modifier: Modifier = Modifier,
    loadingState: Boolean = false,
    text: String = stringResource(R.string.sign_in_using_google),
    loadingText: String = stringResource(R.string.just_a_second),
    icon: Int = R.drawable.google_logo,
    shape: Shape = Shapes().extraSmall,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    loadingIndicatorColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick: () -> Unit
) {
    var signInText by remember {
        mutableStateOf(text)
    }

    LaunchedEffect(key1 = loadingState){
        signInText = if(loadingState){
            loadingText
        } else {
            text
        }
    }
    
    Surface(
        modifier = modifier.clickable(enabled = !loadingState){
            onClick()
        },
        shape = shape,
        color = backgroundColor
    ) {
        
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(12.dp)
                .animateContentSize(animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "Google Logo",
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = signInText,
                style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
            )

            if (loadingState) {
                Spacer(modifier = Modifier.width(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp)
                        .testTag("CircularProgressIndicator"),
                    strokeWidth = 2.dp,
                    color = loadingIndicatorColor
                )
            }
        }
    }
}

@Composable
@Preview
fun ButtonPreview() {
    LoginWithGoogleButton {}
}

