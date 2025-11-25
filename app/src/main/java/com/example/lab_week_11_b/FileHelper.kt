package com.example.lab_week_11_b

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

class FileHelper (private val context: Context) {
    // Generate a URI to access the file (temporary access for other apps)
    fun getUriFromFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "com.example.lab_week_11_b.camera", // Harus sama dengan authorities di Manifest
            file
        )
    }

    // Get the folder name for pictures (defined in file_provider_paths.xml)
    fun getPicturesFolder(): String = Environment.DIRECTORY_PICTURES

    // Get the folder name for videos (defined in file_provider_paths.xml)
    fun getVideosFolder(): String = Environment.DIRECTORY_MOVIES
}