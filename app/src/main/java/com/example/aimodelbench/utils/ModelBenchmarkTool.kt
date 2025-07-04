package com.example.aimodelbench.utils

import android.app.ActivityManager
import android.content.Context
import android.os.*
import android.util.Log
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureNanoTime

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
    private val numThreads: Int = 1
) {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)

    fun evaluate(
        outputFile: File,
        runInference: () -> Unit
    ) {
        val startTime = System.currentTimeMillis()
        val startTimeStr = simpleDateFormat.format(Date(startTime))

        val startCpuTicks = getCpuTime()
        val memBefore = getMemoryInfo()

        val inferenceTimeNs = measureNanoTime {
            runInference()
        }

        val endTime = System.currentTimeMillis()
        val endTimeStr = simpleDateFormat.format(Date(endTime))

        val endCpuTicks = getCpuTime()
        val memAfter = getMemoryInfo()

        val json = JSONObject().apply {
            put("model_name", modelName)
            put("delegate_used", delegateUsed)
            put("num_threads", numThreads)
            put("start_time", startTimeStr)
            put("end_time", endTimeStr)
            put("inference_time_ms", inferenceTimeNs / 1_000_000)
            put("cpu_time_ticks", endCpuTicks - startCpuTicks)

            put("device_info", JSONObject().apply {
                put("device_model", Build.MODEL)
                put("manufacturer", Build.MANUFACTURER)
                put("sdk_version", Build.VERSION.SDK_INT)
            })

            put("memory_kb", JSONObject().apply {
                put("dalvik_pss", memAfter.dalvikPss - memBefore.dalvikPss)
                put("native_pss", memAfter.nativePss - memBefore.nativePss)
                put("total_pss", memAfter.totalPss - memBefore.totalPss)
            })
        }

        FileWriter(outputFile).use {
            it.write(json.toString(4))
        }

        Log.d("BenchmarkTool", "Saved benchmark to: ${json}")
    }

    private fun getCpuTime(): Long {
        val pid = Process.myPid()
        return try {
            val stat = File("/proc/$pid/stat").readText().split(" ")
            stat[13].toLong() + stat[14].toLong()
        } catch (e: Exception) {
            Log.e("BenchmarkTool", "CPU stat read failed", e)
            -1
        }
    }

    private fun getMemoryInfo(): Debug.MemoryInfo {
        return Debug.MemoryInfo().apply { Debug.getMemoryInfo(this) }
    }
}

