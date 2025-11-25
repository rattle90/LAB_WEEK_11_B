package com.example.lab_week_11_b

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class MainActivity: AppCompatActivity() {

    // Request code for permission request to external storage
    private companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 3
    }

    private lateinit var providerFileManager: ProviderFileManager
    private var photoInfo: FileInfo? = null
    private var videoInfo: FileInfo? = null
    private var isCapturingVideo = false

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var takeVideoLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the ProviderFileManager
        providerFileManager = ProviderFileManager(
            applicationContext,
            FileHelper(applicationContext),
            contentResolver,
            Executors.newSingleThreadExecutor(), // Used for background file copying
            MediaContentHelper()
        )

        // Initialize the activity result launcher for taking a picture
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            // If the picture was taken successfully, insert it into MediaStore
            if (success) {
                providerFileManager.insertImageToStore(photoInfo)
            }
        }

        // Initialize the activity result launcher for capturing a video
        takeVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
            // If the video was captured successfully, insert it into MediaStore
            if (success) {
                providerFileManager.insertVideoToStore(videoInfo)
            }
        }

        // Set click listener for the Photo button
        findViewById<Button>(R.id.photo_button).setOnClickListener {
            isCapturingVideo = false
            // Check storage permission before opening camera
            checkStoragePermission {
                openImageCapture()
            }
        }

        // Set click listener for the Video button
        findViewById<Button>(R.id.video_button).setOnClickListener {
            isCapturingVideo = true
            // Check storage permission before opening camera
            checkStoragePermission {
                openVideoCapture()
            }
        }
    }

    // --- Camera Launch Functions ---

    private fun openImageCapture() {
        // 1. Generate FileInfo (which includes FileProvider URI)
        photoInfo = providerFileManager.generatePhotoUri(System.currentTimeMillis())
        // 2. Launch the camera activity, storing the result in the generated URI
        takePictureLauncher.launch(photoInfo?.uri)
    }

    private fun openVideoCapture() {
        // 1. Generate FileInfo (which includes FileProvider URI)
        videoInfo = providerFileManager.generateVideoUri(System.currentTimeMillis())
        // 2. Launch the video recorder activity, storing the result in the generated URI
        takeVideoLauncher.launch(videoInfo?.uri)
    }

    // --- Permission Handling ---

    // Check storage permission (only required for Android 9/API 28 and below)
    private fun checkStoragePermission (onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            when (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted() // Permission already granted
                }
                else -> {
                    // Request the permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_EXTERNAL_STORAGE
                    )
                }
            }
        } else {
            onPermissionGranted() // No permission needed for Q (Android 10) and above
        }
    }

    // Handle the permission request result for Android 9 and below
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If granted, reopen the appropriate capture function
                    if (isCapturingVideo) {
                        openVideoCapture()
                    } else {
                        openImageCapture()
                    }
                }
                return
            }
            else -> {
                // For other request codes, do nothing
            }
        }
    }
}