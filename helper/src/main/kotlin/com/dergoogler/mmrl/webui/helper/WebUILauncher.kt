package com.dergoogler.mmrl.webui.helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dergoogler.mmrl.platform.PLATFORM_KEY
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.putModId

data class WebUIPermissions(
    private val debugPostFix: String,
) {
    val WEBUI_X = "com.dergoogler.mmrl$debugPostFix.permission.WEBUI_X"
    val WEBUI_LEGACY = "com.dergoogler.mmrl$debugPostFix.permission.WEBUI_LEGACY"
}

class WebUILauncher(
    private val debug: Boolean = false,
) {
    fun launchWX(
        context: Context,
        modId: ModId,
        platform: Platform,
        transformer: (Intent.() -> Intent)? = null,
    ) {
        try {
            val intent = Intent().apply {
                component = ComponentName(packageName, X)

                putModId(modId)
                putExtra(PLATFORM_KEY, platform)

                if (transformer != null) {
                    transformer()
                }
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "launchWX: ${e.message}")
        }
    }

    fun launchLegacy(
        context: Context,
        modId: ModId,
        platform: Platform,
        transformer: (Intent.() -> Intent)? = null,
    ) {
        try {
            val intent = Intent().apply {
                component = ComponentName(packageName, LEGACY)

                putModId(modId)
                putExtra(PLATFORM_KEY, platform)

                if (transformer != null) {
                    transformer()
                }
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "launchWX: ${e.message}")
        }
    }

    private val debugPostFix = if (debug) ".debug" else ""
    private val packageName = "com.dergoogler.mmrl.wx$debugPostFix"

    val permissions = WebUIPermissions(debugPostFix)

    private companion object {
        const val TAG = "WebUILauncher"

        const val X = "com.dergoogler.mmrl.wx.ui.activity.webui.WebUIActivity"
        const val LEGACY = "com.dergoogler.mmrl.wx.ui.activity.webui.KsuWebUIActivity"
    }
}