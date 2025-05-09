package com.dergoogler.mmrl.wx.ui.screens.modules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.wx.R
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.ui.component.TopAppBar
import com.dergoogler.mmrl.ui.component.TopAppBarIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {

            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        TopAppBarIcon(
                            icon = R.drawable.launcher_outline,
                        )

                        Text(stringResource(R.string.app_name))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        CheckProvider {
            Box(
                modifier = Modifier.padding(innerPadding)
            ) {
                ModulesList(
                    list = Platform.moduleManager.modules,
                    state = listState,
                )
            }
        }
    }
}