package com.dergoogler.mmrl.webui.model

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Immutable
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.content.LocalModule
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.hiddenApi.HiddenPackageManager
import com.dergoogler.mmrl.platform.hiddenApi.HiddenUserManager
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.putModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.webrootDir
import com.dergoogler.mmrl.webui.R
import com.dergoogler.mmrl.webui.activity.WXActivity
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.moshi
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dalvik.system.BaseDexClassLoader
import dalvik.system.DexClassLoader
import dalvik.system.InMemoryDexClassLoader
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap


object WebUIPermissions {
    const val PLUGIN_DEX_LOADER = "webui.permission.PLUGIN_DEX_LOADER"
    const val DSL_DEX_LOADING = "webui.permission.DSL_DEX_LOADING"
    const val WX_ROOT_PATH = "wx.permission.ROOT_PATH"
}

/**
 * Represents the required version information for interacting with the Web UI.
 *
 * This data class specifies the minimum version of the Web UI that the client must be using,
 * along with optional supporting text and a link for the user to get help or updates.
 *
 * @property required The minimum required version number (an integer). Defaults to 1.
 *                    Clients with a Web UI version lower than this value should be prompted to upgrade.
 * @property supportText Optional text providing additional context or instructions to the user.
 *                       For example: "Please update to the latest version for the best experience."
 * @property supportLink Optional URL link where the user can find more information about the
 *                       required version, such as download instructions or release notes.
 *                       For example: "https://example.com/webui-update"
 */
@JsonClass(generateAdapter = true)
data class WebUIConfigRequireVersion(
    val required: Int = 1,
    val supportText: String? = null,
    val supportLink: String? = null,
)

@JsonClass(generateAdapter = true)
data class WebUIConfigRequireVersionPackages(
    val code: Int = -1,
    val packageName: Any,
    val supportText: String? = null,
    val supportLink: String? = null,
) {
    val packageNames
        get(): List<String> {
            return when (packageName) {
                is String -> listOf(packageName)
                is List<*> -> packageName.filterIsInstance<String>()
                else -> emptyList()
            }
        }
}

/**
 * Represents the required configuration for the Web UI.
 *
 * This data class defines the minimum required configuration settings needed for the Web UI to function correctly.
 * Currently, it only includes the required version information.
 *
 * @property version The required version details for the Web UI. Defaults to a new [WebUIConfigRequireVersion] instance.
 */
@JsonClass(generateAdapter = true)
data class WebUIConfigRequire(
    val packages: List<WebUIConfigRequireVersionPackages> = emptyList(),
    val version: WebUIConfigRequireVersion = WebUIConfigRequireVersion(),
)

@JsonClass(generateAdapter = false)
enum class DexSourceType {
    @Json(name = "dex") DEX,
    @Json(name = "apk") APK
}

private val interfaceCache = ConcurrentHashMap<String, JavaScriptInterface<out WXInterface>>()

@JsonClass(generateAdapter = true)
data class WebUIConfigDexFile(
    val type: DexSourceType = DexSourceType.DEX,
    val path: String? = null,
    val className: String? = null,
) {
    private companion object {
        const val TAG = "WebUIConfigDexFile"
    }

    /**
     * Loads and instantiates a JavaScript interface from a DEX or APK file.
     *
     * @param context The Android Context.
     * @param modId The ID of the mod providing the web root.
     * @param interfaceCache A thread-safe cache to store and retrieve loaded interfaces,
     * preventing redundant and expensive file operations.
     * @return The instantiated JavaScriptInterface, or null if loading fails.
     */
    fun getInterface(
        context: Context,
        modId: ModId,
    ): JavaScriptInterface<out WXInterface>? {
        // Use guard clauses for cleaner validation at the start.
        val currentClassName = className ?: return null
        val currentPath = path ?: return null

        // 1. Check cache first for immediate retrieval.
        interfaceCache[currentClassName]?.let { return it }

        return try {
            // 2. Create the appropriate class loader.
            val loader = when (type) {
                DexSourceType.DEX -> createDexLoader(context, modId, currentPath)
                DexSourceType.APK -> createApkLoader(context, currentPath)
            } ?: return null // Return null if loader creation failed.

            // 3. Load the class and create an instance.
            val rawClass = loader.loadClass(currentClassName)
            if (!WXInterface::class.java.isAssignableFrom(rawClass)) {
                Log.e(TAG, "Loaded class $currentClassName does not implement WXInterface")
                return null
            }

            @Suppress("UNCHECKED_CAST")
            val clazz = rawClass as Class<out WXInterface>
            val instance = JavaScriptInterface(clazz)

            // 4. Cache the new instance and return it.
            interfaceCache.putIfAbsent(currentClassName, instance)
            instance
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "Class $currentClassName not found in path: $currentPath", e)
            null
        } catch (e: Exception) {
            // Generic catch for any other instantiation or loading errors.
            Log.e(TAG, "Error loading class $currentClassName from path: $currentPath", e)
            null
        }
    }

    /**
     * Creates a ClassLoader for a standalone .dex file.
     */
    private fun createDexLoader(context: Context, modId: ModId, dexPath: String): BaseDexClassLoader? {
        val file = SuFile(modId.webrootDir, dexPath)

        if (!file.isFile || file.extension != "dex") {
            Log.e(TAG, "Provided path is not a valid .dex file: ${file.path}")
            return null
        }

        // Using InMemoryDexClassLoader is efficient if DEX files are not excessively large.
        val dexFileBytes = file.readBytes()
        return InMemoryDexClassLoader(ByteBuffer.wrap(dexFileBytes), context.classLoader)
    }

    /**
     * Creates a ClassLoader for a class within an installed APK.
     */
    private fun createApkLoader(context: Context, packageName: String): BaseDexClassLoader? {
        return try {
            val pm: HiddenPackageManager = PlatformManager.packageManager
            val um: HiddenUserManager = PlatformManager.userManager
            val appInfo = pm.getApplicationInfo(packageName, um.myUserId, 0)
            val apkPath = appInfo.sourceDir

            val optimizedDir = context.getDir("dex_opt", Context.MODE_PRIVATE).absolutePath

            DexClassLoader(apkPath, optimizedDir, null, context.classLoader)
        } catch (e: PackageManager.NameNotFoundException) {
            // Use consistent logging instead of printStackTrace.
            Log.e(TAG, "Could not find package: $packageName", e)
            null
        }
    }
}

@Immutable
@JsonClass(generateAdapter = true)
data class WebUIConfig(
    val modId: ModId = ModId.EMPTY,
    val require: WebUIConfigRequire = WebUIConfigRequire(),
    val permissions: List<String> = emptyList(),
    val historyFallback: Boolean = false,
    val title: String? = null,
    val icon: String? = null,
    val windowResize: Boolean = true,
    val backHandler: Boolean? = true,
    val backInterceptor: Any? = null,
    val refreshInterceptor: String? = null,
    val exitConfirm: Boolean = true,
    val pullToRefresh: Boolean = false,
    val historyFallbackFile: String = "index.html",
    val autoStatusBarsStyle: Boolean = true,
    val dexFiles: List<WebUIConfigDexFile> = emptyList(),
    val killShellWhenBackground: Boolean = true,
) {
    val hasRootPathPermission get() = WebUIPermissions.WX_ROOT_PATH in permissions

    val useJavaScriptRefreshInterceptor get() = refreshInterceptor == "javascript"
    val useNativeRefreshInterceptor get() = refreshInterceptor == "native"

    private fun getIconFile() = if (icon != null) SuFile(modId.webrootDir, icon) else null
    private fun getShortcutId() = "shortcut_$modId"

    fun canAddWebUIShortcut(): Boolean {
        val iconFile = getIconFile()
        return title != null && iconFile != null && iconFile.exists() && iconFile.isFile
    }

    fun hasWebUIShortcut(context: Context): Boolean {
        val shortcutId = getShortcutId()
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        return shortcutManager.pinnedShortcuts.any { it.id == shortcutId }
    }

    fun createShortcut(
        context: Context,
        cls: Class<out WXActivity>,
    ) {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        val shortcutId = getShortcutId()

        if (!canAddWebUIShortcut()) {
            return
        }

        val iconFile = getIconFile()

        // Paranoia check
        if (iconFile == null) {
            return
        }

        if (shortcutManager.isRequestPinShortcutSupported) {
            if (shortcutManager.pinnedShortcuts.any { it.id == shortcutId }) {
                Toast.makeText(
                    context, context.getString(R.string.shortcut_already_exists), Toast.LENGTH_SHORT
                ).show()
                return
            }

            val shortcutIntent = Intent(context, cls).apply {
                putModId(modId.toString())
            }

            shortcutIntent.action = Intent.ACTION_VIEW

            val bitmap =
                iconFile.newInputStream().buffered().use { BitmapFactory.decodeStream(it) }

            val shortcut =
                ShortcutInfo.Builder(context, shortcutId).setShortLabel(title!!).setLongLabel(title)
                    .setIcon(Icon.createWithAdaptiveBitmap(bitmap)).setIntent(shortcutIntent)
                    .build()

            shortcutManager.requestPinShortcut(shortcut, null)
        }
    }

    companion object {
        const val TAG = "WebUIConfig"

        /**
         * Extension property for [ModId] to retrieve its associated [WebUIConfig].
         *
         * This property attempts to load and parse a `config.json` or `config.mmrl.json`
         * file from the module's `webroot` directory.
         * - It first constructs the path to the module's directory (`/data/adb/modules/<module_id>`).
         * - Then, it looks for `config.json` or `config.mmrl.json` within the `webroot` subdirectory.
         * - If a configuration file is found, its content is read and parsed into a [WebUIConfig] object.
         * - If no configuration file is found or if parsing fails, a default [WebUIConfig]
         *   instance is returned, initialized with the current [ModId].
         *
         * @return The parsed [WebUIConfig] if a configuration file is found and valid,
         *         otherwise a default [WebUIConfig] for the given [ModId].
         */
        val ModId.asWebUIConfig: WebUIConfig
            get() {
                val config = WebUIConfig(this)

                val configFile =
                    webrootDir.fromPaths("config.json", "config.mmrl.json") ?: return config

                val jsonString = configFile.readText()
                val jsonAdapter = moshi.adapter(WebUIConfig::class.java)

                val json = jsonAdapter.fromJson(jsonString)

                return (json ?: config).copy(modId = this)
            }

        val LocalModule.webUiConfig get() = id.asWebUIConfig
    }
}
