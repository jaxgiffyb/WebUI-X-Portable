package com.dergoogler.mmrl.webuix.ui.screens.modules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.ui.component.Loading
import kotlinx.coroutines.delay

@Composable
fun CheckProvider(
    content: @Composable (Boolean) -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Platform.isAlive) {
        while (!Platform.isAlive) {
            delay(1000)
        }

        isLoading = false
    }

    AnimatedVisibility(
        visible = isLoading, enter = fadeIn(), exit = fadeOut()
    ) {
        Loading()
    }

    AnimatedVisibility(
        visible = !isLoading, enter = fadeIn(), exit = fadeOut()
    ) {
        // passing isLoading makes no sense
        content(isLoading)
    }
}