@file:Suppress("HardCodedStringLiteral")

package io.github.domi04151309.alwayson.helpers

import android.util.Log
import java.io.DataOutputStream
import java.io.IOException

internal object Root {
    fun request(): Boolean {
        val process: Process
        return try {
            process = Runtime.getRuntime().exec("su")
            val output = DataOutputStream(process.outputStream)
            output.writeBytes("echo access granted\n")
            output.writeBytes("exit\n")
            output.flush()
            true
        } catch (exception: IOException) {
            Log.w(Global.LOG_TAG, exception.toString())
            false
        }
    }

    fun shell(command: String) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            process.waitFor()
        } catch (exception: IOException) {
            Log.w("Superuser", exception.toString())
        }
    }
}
