package com.dergoogler.mmrl.webuix.util

import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.datastore.providable.LocalUserPreferences
import com.dergoogler.mmrl.ext.toFormattedDateSafely
import com.dergoogler.mmrl.platform.content.LocalModule
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils

val LocalModule.versionDisplay get(): String {
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