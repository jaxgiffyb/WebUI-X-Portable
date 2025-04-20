package com.dergoogler.mmrl.webuix.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.datastore.model.WorkingMode
import com.dergoogler.mmrl.datastore.providable.LocalUserPreferences
import com.dergoogler.mmrl.ext.isLocalWifiUrl
import com.dergoogler.mmrl.ext.navigateSingleTopTo
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ext.takeTrue
import com.dergoogler.mmrl.ui.component.TopAppBar
import com.dergoogler.mmrl.ui.component.TopAppBarTitle
import com.dergoogler.mmrl.ui.component.listItem.ListButtonItem
import com.dergoogler.mmrl.ui.component.listItem.ListEditTextSwitchItem
import com.dergoogler.mmrl.ui.component.listItem.ListHeader
import com.dergoogler.mmrl.ui.component.listItem.ListSwitchItem
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.webuix.R
import com.dergoogler.mmrl.webuix.ui.navigation.graphs.SettingsScreen
import com.dergoogler.mmrl.webuix.viewmodel.LocalSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val userPreferences = LocalUserPreferences.current
    val viewModel = LocalSettings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val navController = LocalNavController.current
    val browser = LocalUriHandler.current

//    var workingModeBottomSheet by remember { mutableStateOf(false) }
//    if (workingModeBottomSheet) WorkingModeBottomSheet(
//        onClose = {
//            workingModeBottomSheet = false
//        }
//    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    TopAppBarTitle(text = stringResource(id = R.string.settings))
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            ListButtonItem(
//                icon = R.drawable.color_swatch,
                title = stringResource(id = R.string.settings_app_theme),
                desc = stringResource(id = R.string.settings_app_theme_desc),
                onClick = {
                    navController.navigateSingleTopTo(SettingsScreen.AppTheme.route)
                }
            )

//            val workingModeIcon = when (userPreferences.workingMode) {
//                WorkingMode.MODE_MAGISK -> R.drawable.magisk_logo
//                WorkingMode.MODE_KERNEL_SU -> R.drawable.kernelsu_logo
//                WorkingMode.MODE_KERNEL_SU_NEXT -> R.drawable.kernelsu_next_logo
//                WorkingMode.MODE_APATCH -> R.drawable.brand_android
//                WorkingMode.MODE_NON_ROOT -> R.drawable.shield_lock
//                else -> R.drawable.components
//            }
//
//            val workingModeText = when (userPreferences.workingMode) {
//                WorkingMode.MODE_MAGISK -> R.string.working_mode_magisk_title
//                WorkingMode.MODE_KERNEL_SU -> R.string.working_mode_kernelsu_title
//                WorkingMode.MODE_KERNEL_SU_NEXT -> R.string.working_mode_kernelsu_next_title
//                WorkingMode.MODE_APATCH -> R.string.working_mode_apatch_title
//                WorkingMode.MODE_NON_ROOT -> R.string.setup_non_root_title
//                else -> R.string.settings_root_none
//            }
//
//            ListButtonItem(
//                icon = workingModeIcon,
//                title = stringResource(id = R.string.setup_mode),
//                desc = stringResource(
//                    id = workingModeText
//                ),
//                onClick = {
//                    workingModeBottomSheet = true
//                }
//            )

            ListHeader(title = stringResource(id = R.string.webui))

            ListSwitchItem(
                title = stringResource(id = R.string.settings_developer_mode),
                desc = stringResource(id = R.string.settings_developer_mode_desc),
                checked = userPreferences.developerMode,
                onChange = viewModel::setDeveloperMode,
            )

            var webuiRemoteUrlInfo by remember { mutableStateOf(false) }
            if (webuiRemoteUrlInfo) AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.settings_webui_remote_url))
                },
                text = {
                    Text(text = stringResource(id = R.string.settings_webui_remote_url_alert_desc))
                },
                onDismissRequest = {
                    webuiRemoteUrlInfo = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            webuiRemoteUrlInfo = false
                        }
                    ) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
            )

            ListEditTextSwitchItem(
                enabled = userPreferences.developerMode,
                title = stringResource(id = R.string.settings_webui_remote_url),
                desc = stringResource(id = R.string.settings_webui_remote_url_desc),
                value = userPreferences.webUiDevUrl,
                checked = userPreferences.useWebUiDevUrl,
                onChange = viewModel::setUseWebUiDevUrl,
                onConfirm = {
                    viewModel.setWebUiDevUrl(it)
                },
                onValid = { !it.isLocalWifiUrl() },
                base = {
                    learnMore = {
                        webuiRemoteUrlInfo = true
                    }
                },
                dialog = {
                    supportingText = { isError ->
                        isError.takeTrue {
                            Text(
                                text = stringResource(R.string.invalid_ip),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
            )

            ListSwitchItem(
                enabled = userPreferences.developerMode,
                title = stringResource(R.string.settings_security_inject_eruda),
                checked = userPreferences.enableErudaConsole,
                onChange = viewModel::setEnableEruda
            )
        }
    }
}