package com.dergoogler.mmrl.wx.util

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.dergoogler.mmrl.platform.Platform.Companion.getPlatform

class NonRootService : Service() {
    override fun onBind(intent: Intent): IBinder {
        val mode = intent.getPlatform() ?: throw Exception("Platform not found")
        return NonServiceManager(mode)
    }
}