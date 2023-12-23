@file:Suppress("HardCodedStringLiteral")

package io.github.domi04151309.alwayson.helpers

import android.util.Log
import java.io.DataOutputStream
import java.io.IOException

internal object Root {
    fun request(): Boolean {
        val p: Process
        return try {
            p = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(p.outputStream)
            os.writeBytes("echo access granted\n")
            os.writeBytes("exit\n")
            os.flush()
            true
        } catch (e: IOException) {
            Log.w(Global.LOG_TAG, e.toString())
            false
        }
    }

    fun shell(command: String) {
        try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            p.waitFor()
        } catch (e: IOException) {
            Log.w("Superuser", e.toString())
        }
    }
}
