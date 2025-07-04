package com.example.aimodelbench.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

object Utils {
    fun openFolderPicker(activity: Activity, requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
        activity.startActivityForResult(intent, requestCode)
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


}