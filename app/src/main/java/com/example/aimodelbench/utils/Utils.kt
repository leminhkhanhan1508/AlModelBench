package com.example.aimodelbench.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
object Utils {
    fun openFolderPicker(activity: Activity, requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
        activity.startActivityForResult(intent, requestCode)
    }

    fun loadBitmapsFromAssets(context: Context, folderName: String): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        try {
            val assetManager = context.assets
            val files = assetManager.list(folderName) ?: return emptyList()

            for (fileName in files) {
                if (fileName.endsWith(".jpg", true) || fileName.endsWith(".png", true)) {
                    try {
                        assetManager.open("$folderName/$fileName").use { inputStream ->
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            if (bitmap != null) bitmaps.add(bitmap)
                        }
                    } catch (e: Exception) {
                        Log.e("LoadBitmap", "Failed to load $fileName", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LoadBitmap", "Failed to list assets in $folderName", e)
        }

        return bitmaps
    }

    fun loadBitmapsFromFolder(folderPath: String): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        val folder = File(folderPath)

        if (folder.exists() && folder.isDirectory) {
            val imageFiles = folder.listFiles { file ->
                file.extension.lowercase() in listOf("jpg", "jpeg", "png")
            } ?: emptyArray()

            for (file in imageFiles) {
                try {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        bitmaps.add(bitmap)
                    }
                } catch (e: Exception) {
                    Log.e("LoadBitmap", "Failed to load ${file.name}", e)
                }
            }
        } else {
            Log.e("LoadBitmap", "Folder doesn't exist or not accessible")
        }

        return bitmaps
    }

    fun shareData(
        context: Context,
        uri: String
    ){
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share JSON File"))
    }

    fun copyToDownloads(context: Context, fileName: String): File? {
        val inputFile = File(context.filesDir, fileName)
        val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputFile = File(outputDir, fileName)

        return try {
            inputFile.copyTo(outputFile, overwrite = true)
            outputFile
        } catch (e: Exception) {
            Log.e("CopyFile", "Failed: ${e.message}")
            null
        }
    }

    fun readAppCpu(pid: Int): Long {
        return try {
            val stat = File("/proc/$pid/stat").readText().split(" ")

            val utime = stat[13].toLong()   // thời gian user
            val stime = stat[14].toLong()   // thời gian kernel
            val cutime = stat[15].toLong()  // user time của con
            val cstime = stat[16].toLong()  // kernel time của con

            utime + stime + cutime + cstime
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    fun getTotalRam(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.totalMem
    }


    fun bitmapToTensorBuffer(
        bitmap: Bitmap,
        imageSize: Int = 224,
        dataType: DataType = DataType.FLOAT32
    ): TensorBuffer {
        // 1. Build preprocessing pipeline
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(imageSize, imageSize, ResizeOp.ResizeMethod.BILINEAR)) // resize
            .add(NormalizeOp(0f, 255f)) // scale [0..255] → [0..1]; adjust if needed
            .build()

        // 2. Load bitmap into TensorImage
        val tensorImage = TensorImage(dataType)
        tensorImage.load(bitmap)

        // 3. Apply preprocessing
        val processedImage = imageProcessor.process(tensorImage)

        // 4. Convert to TensorBuffer
        return TensorBuffer.createFixedSize(
            intArrayOf(1, imageSize, imageSize, 3), dataType
        ).apply {
            loadBuffer(processedImage.buffer)
        }
    }



}