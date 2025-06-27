package com.dergoogler.mmrl.webui.interfaces

import android.content.Intent
import android.webkit.JavascriptInterface
import androidx.core.net.toUri

class IntentInterface(wxOptions: WXOptions) : WXInterface(wxOptions) {
    override var name: String = "\$intent"

    @JavascriptInterface
    fun create(action: String): IntentData {
        return IntentData(Intent(action), wxOptions)
    }
}

class IntentData(
    val intent: Intent,
    wxOptions: WXOptions,
) : WXInterface(wxOptions) {
    @JavascriptInterface
    fun setPackage(packageName: String) {
        intent.setPackage(packageName)
    }

    @JavascriptInterface
    fun setData(data: String) {
        intent.setData(data.toUri())
    }

    @JavascriptInterface
    fun addCategory(category: String) {
        intent.addCategory(category)
    }

    @JavascriptInterface
    fun setType(type: String) {
        intent.setType(type)
    }

    @JavascriptInterface
    fun putExtra(name: String, value: String) {
        intent.putExtra(name, value)
    }

    @JavascriptInterface
    fun putExtra(name: String, value: Int) {
        intent.putExtra(name, value)
    }

    @JavascriptInterface
    fun putExtra(name: String, value: Boolean) {
        intent.putExtra(name, value)
    }

    @JavascriptInterface
    fun getStringExtra(name: String): String? = intent.getStringExtra(name)

    @JavascriptInterface
    fun getIntExtra(name: String): Int = intent.getIntExtra(name, 0x0)

    @JavascriptInterface
    fun getBooleanExtra(name: String): Boolean = intent.getBooleanExtra(name, false)
}