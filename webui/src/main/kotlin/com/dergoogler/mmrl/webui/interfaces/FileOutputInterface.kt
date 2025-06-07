package com.dergoogler.mmrl.webui.interfaces

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.model.ModId
import java.io.BufferedOutputStream
import java.io.OutputStream

@Keep
class FileOutputInterface(
    wxOptions: WXOptions,
) : WXInterface(wxOptions) {
    private val ModId.sanitizedIdWithFileOutputStream get(): String = "${sanitizedIdWithFile}OutputStream"

    override var name: String = modId.sanitizedIdWithFileOutputStream
    override var tag: String = "FileOutputInterface"

    @JavascriptInterface
    fun open(path: String, append: Boolean): FileOutputInterfaceStream? =
        try {
            val file = SuFile(path)
            val outputStream = file.newOutputStream(append)

            FileOutputInterfaceStream(outputStream, wxOptions)
        } catch (e: Exception) {
            console.error(e)
            null
        }

    @JavascriptInterface
    fun open(path: String): FileOutputInterfaceStream? = open(path, false)
}

class FileOutputInterfaceStream(
    outputStream: OutputStream,
    wxOptions: WXOptions,
) : WXInterface(wxOptions) {
    private val bufferedOutputStream = BufferedOutputStream(outputStream)

    fun getStream(): OutputStream = bufferedOutputStream

    @JavascriptInterface
    fun write(b: Int) {
        runTry("Error while writing to stream", -1) {
            bufferedOutputStream.write(b)
        }
    }

    @JavascriptInterface
    fun flush() = runTry("Error while flushing stream") {
        bufferedOutputStream.flush()
    }

    @JavascriptInterface
    fun close() = runTry("Error while closing stream") {
        bufferedOutputStream.close()
    }
}