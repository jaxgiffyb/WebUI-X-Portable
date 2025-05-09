package com.dergoogler.mmrl.webuix.ui.screens.modules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.TIMEOUT_MILLIS
import com.dergoogler.mmrl.ui.component.Failed
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.webuix.R
import kotlinx.coroutines.delay

@Composable
fun CheckProvider(
    content: @Composable (Boolean) -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var timeoutReached by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (!Platform.isAlive && System.currentTimeMillis() - startTime < TIMEOUT_MILLIS) {
            delay(500)
        }

        isLoading = false
        timeoutReached = !Platform.isAlive
    }

    when {
        isLoading -> {
            Loading()
        }
        timeoutReached -> {
            Failed(stringResource(R.string.mh_seems_like_root_isn_t_authorized))
        }
        else -> {
            content(false)
        }
    }
}