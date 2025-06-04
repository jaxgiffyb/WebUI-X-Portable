package com.dergoogler.mmrl.wx.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.dergoogler.mmrl.wx.R

object NotificationUtils {
    const val CHANNEL_ID_PLATFORM = "WX_PLATFORM"
    const val GROUP_KEY_PLATFORM = "WX_PLATFORM_SERVICE_GROUP_KEY"
    const val NOTIFICATION_ID_PLATFORM = 1024

    fun init(context: Context) {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_ID_PLATFORM,
                context.getString(R.string.notification_platform_service),
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        NotificationManagerCompat.from(context).apply {
            createNotificationChannels(channels)
            deleteUnlistedNotificationChannels(channels.map { it.id })
        }
    }
}