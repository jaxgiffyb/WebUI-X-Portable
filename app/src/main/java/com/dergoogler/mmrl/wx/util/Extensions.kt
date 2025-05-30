package com.dergoogler.mmrl.wx.util

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.datastore.model.WorkingMode
import com.dergoogler.mmrl.datastore.providable.LocalUserPreferences
import com.dergoogler.mmrl.ext.exception.BrickException
import com.dergoogler.mmrl.ext.toFormattedDateSafely
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.content.LocalModule
import com.dergoogler.mmrl.platform.stub.IServiceManager
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope

val LocalModule.versionDisplay
    get(): String {
        val included = "\\(.*?${versionCode}.*?\\)".toRegex()
            .containsMatchIn(version)

        return if (included) {
            version
        } else {
            "$version (${versionCode})"
        }
    }

val Float.toFormattedDateSafely: String
    @Composable
    get() {
        val prefs = LocalUserPreferences.current
        return this.toFormattedDateSafely(prefs.datePattern)
    }

val Long.toFormattedDateSafely: String
    @Composable
    get() {
        val prefs = LocalUserPreferences.current
        return this.toFormattedDateSafely(prefs.datePattern)
    }

fun Platform.toWorkingMode() = when (this) {
    Platform.Magisk -> WorkingMode.MODE_MAGISK
    Platform.KernelSU -> WorkingMode.MODE_KERNEL_SU
    Platform.KsuNext -> WorkingMode.MODE_KERNEL_SU_NEXT
    Platform.APatch -> WorkingMode.MODE_APATCH
    Platform.NonRoot -> WorkingMode.MODE_NON_ROOT
    else -> throw BrickException("Unsupported Platform")
}


inline fun <T> withNewRootShell(
    globalMnt: Boolean = false,
    debug: Boolean = false,
    commands: Array<String> = arrayOf("su"),
    block: Shell.() -> T,
): T {
    return createRootShell(globalMnt, debug, commands).use(block)
}

fun createRootShell(
    globalMnt: Boolean = false,
    debug: Boolean = false,
    commands: Array<String> = arrayOf("su"),
): Shell {
    Shell.enableVerboseLogging = debug
    val builder = Shell.Builder.create()
    if (globalMnt) {
        builder.setFlags(Shell.FLAG_MOUNT_MASTER)
    }
    return builder.build(*commands)
}

private suspend fun init(
    platform: Platform,
    context: Context,
    self: PlatformManager,
): IServiceManager? {
    if (platform.isNonRoot) {
        return self.from(
            NonRootProvider(
                context,
                platform,
            )
        )
    }

    return self.from(
        RootProvider(
            context,
            platform,
        )
    )
}

suspend fun initPlatform(
    context: Context,
    platform: Platform,
) = PlatformManager.init {
    init(platform, context, this)
}

suspend fun initPlatform(
    scope: CoroutineScope,
    context: Context,
    platform: Platform,
) = PlatformManager.init(scope) {
    init(platform, context, this)
}