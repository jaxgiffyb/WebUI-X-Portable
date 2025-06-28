package com.dergoogler.mmrl.wx.util

import android.content.Context
import android.content.ServiceConnection
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.Platform.Companion.createPlatformIntent
import com.dergoogler.mmrl.platform.model.IProvider
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class RootProvider(
    private val context: Context,
    private val platform: Platform,
) : IProvider {
    override val name = "RootProvider"

    override fun isAvailable() = true

    override suspend fun isAuthorized() = suspendCancellableCoroutine { continuation ->
        Shell.EXECUTOR.execute {
            runCatching {
                Shell.getShell()
            }.onSuccess {
                continuation.resume(true)
            }.onFailure {
                continuation.resume(false)
            }
        }
    }

    private val intent by lazy {
        context.createPlatformIntent<SuService>(platform)
    }

    override fun bind(connection: ServiceConnection) {
        RootService.bind(intent, connection)
    }

    override fun unbind(connection: ServiceConnection) {
        RootService.stop(intent)
    }
}
