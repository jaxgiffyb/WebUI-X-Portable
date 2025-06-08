package com.dergoogler.mmrl.wx.ui.navigation.graphs

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.dergoogler.mmrl.ext.panicArguments
import com.dergoogler.mmrl.ext.panicString
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.content.LocalModule
import com.dergoogler.mmrl.platform.model.ModId.Companion.toModId
import com.dergoogler.mmrl.wx.ui.navigation.MainScreen
import com.dergoogler.mmrl.wx.ui.screens.modules.ModulesScreen
import com.dergoogler.mmrl.wx.ui.screens.modules.screens.ConfigEditorScreen

enum class ModulesScreen(val route: String) {
    Home("Modules"),
    Config("Config/{id}"),
}

fun NavGraphBuilder.modulesScreen() = navigation(
    startDestination = ModulesScreen.Home.route,
    route = MainScreen.Modules.route
) {
    composable(
        route = ModulesScreen.Home.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        ModulesScreen()
    }

    composable(
        route = ModulesScreen.Config.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        val args = it.panicArguments
        val id = args.panicString("id")

        val module = PlatformManager.moduleManager.getModuleById(id.toModId())

        ConfigEditorScreen(module)
    }
}