package com.example.lab_week_11_b

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import org.apache.commons.io.IOUtils
import java.io.File
import java.util.concurrent.Executor

class ProviderFileManager(
    private val context: Context,
    private val fileHelper: FileHelper,
    private val contentResolver: ContentResolver,
    private val executor: Executor,
    private val mediaContentHelper: MediaContentHelper
) {
    // 1. Generate the data model (FileInfo) for a new Photo
    fun generatePhotoUri (time: Long): FileInfo {
        val name = "img_$time.jpg"
        // Get the file object in the External Files Dir (app-specific storage)
        val file = File(
            context.getExternalFilesDir(fileHelper.getPicturesFolder()),
            name
        )
        return FileInfo(
            fileHelper.getUriFromFile(file), // FileProvider URI
            file,
            name,
            fileHelper.getPicturesFolder(),
            "image/jpeg"
        )
    }

    // 2. Generate the data model (FileInfo) for a new Video
    fun generateVideoUri (time: Long): FileInfo {
        val name = "video_$time.mp4"
        val file = File(
            context.getExternalFilesDir(fileHelper.getVideosFolder()),
            name
        )
        return FileInfo(
            fileHelper.getUriFromFile(file), // FileProvider URI
            file,
            name,
            fileHelper.getVideosFolder(),
            "video/mp4"
        )
    }

    // 3. Insert Image to MediaStore
    fun insertImageToStore (fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                fileInfo,
                mediaContentHelper.getImageContentUri(),
                mediaContentHelper.generateImageContentValues(it)
            )
        }
    }

    // 4. Insert Video to MediaStore
    fun insertVideoToStore (fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                fileInfo,
                mediaContentHelper.getVideoContentUri(),
                mediaContentHelper.generateVideoContentValues(it)
            )
        }
    }

    // 5. Core logic: Insert the file to MediaStore (copy file data)
    private fun insertToStore (fileInfo: FileInfo, contentUri: Uri, contentValues: ContentValues) {
        // Run on a separate thread (Executor) since file operations are slow (I/O)
        executor.execute {
            // 1. Insert an entry into MediaStore and get the new URI
            val insertedUri = contentResolver.insert(contentUri, contentValues)

            insertedUri?.let {
                // 2. Open input stream from the temporary FileProvider URI
                val inputStream = contentResolver.openInputStream(fileInfo.uri)
                // 3. Open output stream to the new MediaStore URI
                val outputStream = contentResolver.openOutputStream(insertedUri)

                // 4. Copy data from input to output stream
                IOUtils.copy(inputStream, outputStream)
            }
        }
    }
}