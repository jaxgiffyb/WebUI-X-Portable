package com.dergoogler.mmrl.wx.util

import com.dergoogler.mmrl.ext.exception.BrickException
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.service.ServiceManager
import com.dergoogler.mmrl.platform.stub.IModuleManager

class NonServiceManager(
    platform: Platform,
) : ServiceManager(platform) {

    private val context by lazy {
        Platform.context
    }

    private val xModuleManager by lazy {
        when (platform) {
            Platform.NonRoot -> {
                NonRootModuleManager(context)
            }

            else -> throw BrickException("Unsupported Platform $platform")
        }
    }

    override fun getModuleManager(): IModuleManager = xModuleManager
}
