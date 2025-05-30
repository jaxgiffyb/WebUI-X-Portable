package com.dergoogler.mmrl.wx.util

import android.app.Service
import android.content.Context
import android.content.ServiceConnection
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.model.IProvider
import com.dergoogler.mmrl.platform.model.createPlatformIntent

class NonRootProvider(
    private val context: Context,
    private val platform: Platform,
) : IProvider {
    override val name = "NonRootProvider"

    override fun isAvailable() = true

    override suspend fun isAuthorized() = true

    private val intent by lazy {
        context.createPlatformIntent<NonRootService>(platform)
    }

    override fun bind(connection: ServiceConnection) {
        context.bindService(intent, connection, Service.BIND_AUTO_CREATE)
    }

    override fun unbind(connection: ServiceConnection) {
        context.unbindService(connection)
    }
}
