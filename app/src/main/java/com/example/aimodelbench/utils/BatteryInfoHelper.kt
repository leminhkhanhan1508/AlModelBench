package com.example.aimodelbench.utils

import android.content.Context
import android.os.BatteryManager
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object BatteryInfoHelper {
    private const val TAG = "BatteryInfoHelper"

    fun getEnergyInfo(context: Context) {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        // Lấy thông tin tổng năng lượng còn lại (nWh)
        val energyNWh = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
        if (energyNWh != Long.MIN_VALUE) {
            Log.i(TAG, "Battery Energy Counter: $energyNWh nWh")
        } else {
            Log.w(TAG, "Energy counter not supported on this device")
        }

        // Lấy thông tin chi tiết qua dumpsys batterystats
        val batteryStatsOutput = runShellCommand("dumpsys batterystats --charged")
        Log.i(TAG, "Battery Stats:\n$batteryStatsOutput")
    }

    private fun runShellCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            reader.close()
            process.waitFor()
            output.toString()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
