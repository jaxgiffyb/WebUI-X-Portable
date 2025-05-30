package com.dergoogler.mmrl.wx.ui.component

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.dergoogler.mmrl.ext.systemBarsPaddingEnd
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.ui.component.dialog.ConfirmData
import com.dergoogler.mmrl.ui.component.dialog.rememberConfirm
import com.dergoogler.mmrl.wx.R
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Composable
fun ModuleImporter() {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val confirm = rememberConfirm()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            val success = importZipToModules(context, uri)

            if (!success) {
                confirm(
                    ConfirmData(
                        title = "Failed!",
                        description = "Failed to import module.",
                        onConfirm = {},
                        onClose = {}
                    )
                )
                return@rememberLauncherForActivityResult
            }

            confirm(
                ConfirmData(
                    title = "Success!",
                    description = "Module imported successfully.",
                    onConfirm = {},
                    onClose = {}
                )
            )
        }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                launcher.launch("application/zip")
            }
        }
    }

    FloatingActionButton(
        modifier = Modifier.systemBarsPaddingEnd(),
        interactionSource = interactionSource,
        onClick = {},
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            painter = painterResource(id = R.drawable.package_import),
            contentDescription = null
        )
    }
}

fun importZipToModules(context: Context, zipUri: Uri): Boolean {
    return try {
        val zipFile = SuFile(context.cacheDir, "temp.zip")
        context.contentResolver.openInputStream(zipUri)?.use { input ->
            FileOutputStream(zipFile).use { output ->
                input.copyTo(output)
            }
        }

        val moduleProps = extractModuleProp(zipFile) ?: return false
        val id = moduleProps["id"]?.takeIf { it.isNotBlank() } ?: return false

        val baseDir = context.getExternalFilesDir(null)

        if (baseDir == null) return false

        val targetDir = SuFile(baseDir, "modules/$id")
        if (targetDir.exists()) targetDir.deleteRecursively()
        targetDir.mkdirs()

        unzip(zipFile, targetDir)

        zipFile.delete()

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


fun unzip(zipFile: SuFile, targetDir: SuFile) {
    ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
        var entry: ZipEntry?
        while (zis.nextEntry.also { entry = it } != null) {
            val file = SuFile(targetDir, entry!!.name)
            if (entry.isDirectory) {
                file.mkdirs()
            } else {
                file.parentFile?.mkdirs()
                FileOutputStream(file).use { out -> zis.copyTo(out) }
            }
        }
    }
}

fun extractModuleProp(zipFile: SuFile): Map<String, String>? {
    ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
        var entry: ZipEntry?
        while (zis.nextEntry.also { entry = it } != null) {
            if (entry?.name == "module.prop") {
                val content = zis.bufferedReader().use { it.readText() }
                return readProps(content)
            }
        }
    }
    return null
}

fun readProps(props: String) = props.lines()
    .associate { line ->
        val items = line.split("=", limit = 2).map { it.trim() }
        if (items.size != 2) "" to "" else items[0] to items[1]
    }
