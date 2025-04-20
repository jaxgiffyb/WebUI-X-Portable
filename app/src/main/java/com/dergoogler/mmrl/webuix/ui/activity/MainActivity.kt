package com.dergoogler.mmrl.webuix.ui.activity

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.webuix.ui.screens.MainScreen
import com.dergoogler.mmrl.webuix.util.BaseActivity
import com.dergoogler.mmrl.webuix.util.setBaseContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private var isLoading by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { isLoading }

        setBaseContent {


            val userPreferences by userPreferencesRepository.data
                .collectAsStateWithLifecycle(initialValue = null)

            val preferences = if (userPreferences == null) {
                return@setBaseContent
            } else {
                isLoading = false
                checkNotNull(userPreferences)
            }


            LaunchedEffect(userPreferences) {

                Platform.init {
                    context = baseContext
                    platform = Platform.KsuNext
                }


                // setActivityEnabled<WebUIActivity>(preferences.workingMode.isRoot)
            }

//            Crossfade(
//                targetState = preferences.workingMode.isSetup,
//                label = "MainActivity"
//            ) { isSetup ->
//                if (isSetup) {
//                    SetupScreen(
//                        setMode = ::setWorkingMode
//                    )
//                } else {
//                    MainScreen(windowSizeClass)
//                }
//            }

            MainScreen()

        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}