package com.dergoogler.mmrl.wx.ui.activity.webui.interfaces

import android.text.TextUtils
import android.view.Window
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import com.topjohnwu.superuser.internal.WaitRunnable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.CompletableFuture

class KernelSUInterface(
    wxOptions: WXOptions,
) : WXInterface(wxOptions) {
    // Defined a name for the interface (window.ksu)
    override var name: String = "ksu"

    // Defined a logging tag for debugging
    override var tag: String = "KernelSUInterface"

    private val commands = if (options.platform.isNonRoot) arrayOf("sh") else arrayOf("su")

    private var shell: Shell by mutableStateOf(Shell.getShell())

    private inline fun <T> withNewRootShell(
        globalMnt: Boolean = false,
        block: Shell.() -> T,
    ): T {
        return createRootShell(globalMnt).use(block)
    }

    private fun createRootShell(
        globalMnt: Boolean = false,
    ): Shell {
        Shell.enableVerboseLogging = options.debug
        val builder = Shell.Builder.create()
        if (globalMnt) {
            builder.setFlags(Shell.FLAG_MOUNT_MASTER)
        }
        shell = builder.build(*commands)
        return shell
    }


    @JavascriptInterface
    fun mmrl(): Boolean {
        return true
    }

    @JavascriptInterface
    fun toast(msg: String) {
        post {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun fullScreen(enable: Boolean) {
        runMainLooperPost {
            if (enable) {
                hideSystemUI(window)
            } else {
                showSystemUI(window)
            }
        }
    }

    @JavascriptInterface
    fun moduleInfo(): String {
        console.warn("$name.moduleInfo() have been removed due to security reasons.")
        val currentModuleInfo = JSONObject()
        currentModuleInfo.put("moduleDir", null)
        currentModuleInfo.put("id", null)
        return currentModuleInfo.toString()
    }

    @JavascriptInterface
    fun exec(cmd: String): String {
        return withNewRootShell { ShellUtils.fastCmd(this, cmd) }
    }

    @JavascriptInterface
    fun exec(cmd: String, callbackFunc: String) {
        exec(cmd, null, callbackFunc)
    }

    private fun processOptions(sb: StringBuilder, options: String?) {
        val opts = if (options == null) JSONObject() else {
            JSONObject(options)
        }

        val cwd = opts.optString("cwd")
        if (!TextUtils.isEmpty(cwd)) {
            sb.append("cd ${cwd};")
        }

        opts.optJSONObject("env")?.let { env ->
            env.keys().forEach { key ->
                sb.append("export ${key}=${env.getString(key)};")
            }
        }
    }

    @JavascriptInterface
    fun exec(
        cmd: String,
        options: String?,
        callbackFunc: String,
    ) {
        val finalCommand = StringBuilder()
        processOptions(finalCommand, options)
        finalCommand.append(cmd)

        scope.launch(Dispatchers.IO) {
            val result = withNewRootShell(
                globalMnt = true,
            ) {
                newJob().add(finalCommand.toString()).to(ArrayList(), ArrayList()).exec()
            }

            val stdout = result.out.joinToString(separator = "\n")
            val stderr = result.err.joinToString(separator = "\n")

            val jsCode =
                "(function() { try { ${callbackFunc}(${result.code}, ${
                    JSONObject.quote(
                        stdout
                    )
                }, ${JSONObject.quote(stderr)}); } catch(e) { console.error(e); } })();"

            runJs(jsCode)
        }
    }

    // ensure it really runs on the ui thread
    private fun runAndWait(r: Runnable) {
        if (ShellUtils.onMainThread()) {
            r.run()
        } else {
            val wr = WaitRunnable(r)
            runMainLooperPost(wr)
            wr.waitUntilDone()
        }
    }

    @JavascriptInterface
    fun spawn(command: String, args: String, options: String?, callbackFunc: String) {
        val finalCommand = StringBuilder()

        processOptions(finalCommand, options)

        if (!TextUtils.isEmpty(args)) {
            finalCommand.append(command).append(" ")
            JSONArray(args).let { argsArray ->
                for (i in 0 until argsArray.length()) {
                    finalCommand.append(argsArray.getString(i))
                    finalCommand.append(" ")
                }
            }
        } else {
            finalCommand.append(command)
        }

        val shell = createRootShell(
            globalMnt = true,
        )

        val emitData = fun(name: String, data: String) {
            val jsCode =
                "(function() { try { ${callbackFunc}.${name}.emit('data', ${
                    JSONObject.quote(
                        data
                    )
                }); } catch(e) { console.error('emitData', e); } })();"

            runJs(jsCode)
        }

        val stdout = object : CallbackList<String>(::runAndWait) {
            override fun onAddElement(s: String) {
                emitData("stdout", s)
            }
        }

        val stderr = object : CallbackList<String>(::runAndWait) {
            override fun onAddElement(s: String) {
                emitData("stderr", s)
            }
        }

        scope.launch(Dispatchers.IO) {
            val future = shell.newJob().add(finalCommand.toString()).to(stdout, stderr).enqueue()
            val completableFuture = CompletableFuture.supplyAsync {
                future.get()
            }

            completableFuture.thenAccept { result ->
                val emitExitCode =
                    "(function() { try { ${callbackFunc}.emit('exit', ${result.code}); } catch(e) { console.error(`emitExit error: \${e}`); } })();"
                runJs(emitExitCode)


                if (result.code != 0) {
                    val emitErrCode =
                        "(function() { try { var err = new Error(); err.exitCode = ${result.code}; err.message = ${
                            JSONObject.quote(
                                result.err.joinToString(
                                    "\n"
                                )
                            )
                        };${callbackFunc}.emit('error', err); } catch(e) { console.error('emitErr', e); } })();"
                    runJs(emitErrCode)
                }
            }.whenComplete { _, _ ->
                runJsCatching { shell.close() }
            }
        }
    }

    override fun onActivityStop() {
        super.onActivityStop()

        if (config.killShellWhenBackground) {
            shell.close()
        }
    }

    override fun onActivityDestroy() {
        super.onActivityDestroy()
        shell.close()
    }

    override fun onActivityResume() {
        super.onActivityResume()

        if (config.killShellWhenBackground) {
            shell = createRootShell(true)
        }
    }
}

fun hideSystemUI(window: Window) =
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

fun showSystemUI(window: Window) =
    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).show(WindowInsetsCompat.Type.systemBars())