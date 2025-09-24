package com.cc.creatorcircle.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun createFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.let { stream ->
            // Get the file name from URI with proper MIME type detection
            val fileName = getFileName(context, uri) ?: getDefaultFileName(context, uri)

            // Create a temporary file
            val tempFile = File(context.cacheDir, fileName)

            // Copy the content to the temp file
            val outputStream = FileOutputStream(tempFile)
            stream.copyTo(outputStream)

            outputStream.close()
            stream.close()

            tempFile
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    var fileName: String? = null

    if (uri.scheme == "content") {
        // Try to get the display name using OpenableColumns (more reliable)
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = it.getString(displayNameIndex)
                }
            }
        }

        // Fallback to MediaStore columns if OpenableColumns didn't work
        if (fileName == null) {
            val cursor2 = context.contentResolver.query(uri, null, null, null, null)
            cursor2?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = it.getString(displayNameIndex)
                    }
                }
            }
        }
    }

    if (fileName == null) {
        fileName = uri.path
        val cut = fileName?.lastIndexOf('/')
        if (cut != -1 && cut != null) {
            fileName = fileName.substring(cut + 1)
        }
    }

    return fileName
}

fun getDefaultFileName(context: Context, uri: Uri): String {
    // Get MIME type to determine appropriate extension
    val mimeType = context.contentResolver.getType(uri)
    val timestamp = System.currentTimeMillis()

    return when {
        mimeType?.startsWith("image/") == true -> {
            val extension = when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/gif" -> "gif"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            "image_$timestamp.$extension"
        }
        mimeType?.startsWith("video/") == true -> {
            val extension = when (mimeType) {
                "video/mp4" -> "mp4"
                "video/quicktime" -> "mov"
                "video/x-msvideo" -> "avi"
                "video/3gpp" -> "3gp"
                "video/webm" -> "webm"
                else -> "mp4"
            }
            "video_$timestamp.$extension"
        }
        else -> "file_$timestamp"
    }
}