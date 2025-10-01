package com.example.aimodelbench.utils

import android.os.Build
import android.os.Debug
import android.util.Log
import com.example.aimodelbench.utils.Utils.readAppCpu
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

/**
 * model_name	text	Tên model đang benchmark
 * start_time / end_time	time	Thời gian chạy, xác định duration
 * inference_time_ms	ms	Tổng thời gian chạy mô hình
 * cpu_time_ticks	ticks	Mức tiêu thụ CPU Tick CPU (1 tick ≈ 10ms hoặc khác tùy hệ thống)
 * memory_kb	KB	RAM sử dụng trong quá trình inference
 * dalvik_pss	Dung lượng RAM cấp phát cho Dalvik/ART VM (Java heap) (KB)
 * native_pss	Dung lượng RAM cấp phát cho phần native (C/C++) của app (KB)
 * total_pss	Tổng lượng RAM mà app sử dụng (dalvik + native + khác) (KB)
 */
class ModelBenchmarkEvaluator(
    private val modelName: String,
    private val delegateUsed: String = "CPU",
    private val numThreads: Int = 1,
    private val totalRam : Long
) {
    private val TAG = "LMKTAG"
    private val FINISH_MESSAGE = "myFunction: execution finished"
    fun evaluate(
        outputFile: File,
        runInference: () -> Unit
    ) {
        val pid = android.os.Process.myPid()
        val memBefore = getMemoryInfo()
        val startAppCpu = readAppCpu(pid)
        val startTotal = System.currentTimeMillis()
        // main function
        runInference()
        val endTotal = System.currentTimeMillis()
        val endAppCpu = readAppCpu(pid)
        val memAfter = getMemoryInfo()
        val cpuTicks = endAppCpu - startAppCpu
        val cpuMs = cpuTicks * 1000.0 / 100.0 // nếu kernel HZ=100
        val wallMs = endTotal - startTotal
        val cpuPercent = (cpuMs / wallMs) * 100.0 / Runtime.getRuntime().availableProcessors()
        val memoryProcess = (memAfter.totalPss - memBefore.totalPss).coerceAtLeast(0)
        val memoryPercent = getProcessMemoryUsagePercent(memoryProcess.toFloat(),totalRam.toFloat())
        /**
         * cpu_time_ticks: tổng ticks CPU.
         * cpu_time_ms: tổng ms CPU sử dụng.
         * total_time_ms: thời gian thực tế trôi qua.
         * avg_cpu_percent: mức sử dụng CPU trung bình (theo %).
         */
        val json = JSONObject().apply {
            put("device_model", Build.MODEL)
            put("manufacturer", Build.MANUFACTURER)
            put("sdk_version", Build.VERSION.SDK_INT)
            put("model_name", modelName)
            put("delegate_used", delegateUsed)
            put("num_threads", numThreads)
            put("cpu_time_ticks", cpuTicks)
            put("cpu_time_ms", cpuMs)
            put("total_time_ms", wallMs)
            put("avg_cpu_percent", cpuPercent)
            put("memory_process", memoryProcess)
            put("memory_percent", memoryPercent)
        }

        FileWriter(outputFile).use {
            it.write(json.toString(4))
        }
        Log.i(TAG, "$json")
        Log.i(TAG, FINISH_MESSAGE)
    }

    private fun getMemoryInfo(): Debug.MemoryInfo {
        return Debug.MemoryInfo().apply { Debug.getMemoryInfo(this) }
    }

    private fun getProcessMemoryUsagePercent(memoryProcess: Float, totalMemory: Float): Float {
        return (memoryProcess / totalMemory) * 100f
    }
}

