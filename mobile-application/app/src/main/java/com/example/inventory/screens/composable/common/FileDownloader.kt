package com.example.inventory.screens.composable.common

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream

class FileDownloader(private val context: Context) {

    fun savePdfToDownloads(sourceFile: File, fileName: String): Result<String> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                savePdfUsingMediaStore(sourceFile, fileName)
            } else {
                savePdfToDownloadsLegacy(sourceFile, fileName)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Suppress("DEPRECATION")
    private fun savePdfToDownloadsLegacy(sourceFile: File, fileName: String): Result<String> {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val destFile = File(downloadsDir, fileName)

            FileInputStream(sourceFile).use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Notify system about the new file
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = Uri.fromFile(destFile)
                context.sendBroadcast(mediaScanIntent)
            }

            Result.success("PDF saved to Downloads: ${destFile.absolutePath}")
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save PDF: ${e.message}"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun savePdfUsingMediaStore(sourceFile: File, fileName: String): Result<String> {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    FileInputStream(sourceFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Result.success("PDF saved to Downloads folder")
            } else {
                Result.failure(Exception("Failed to create file in Downloads"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save PDF: ${e.message}"))
        }
    }

    fun sharePdfFile(){

    }
}