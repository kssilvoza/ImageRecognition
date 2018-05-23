package com.voyagerinnovation.imagerecognition

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_image_recognition.*
import timber.log.Timber

class ImageRecognitionActivity : AppCompatActivity() {
    private lateinit var cameraPreviewSurfaceView: CameraPreviewSurfaceView

    private var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recognition)
        initializeCamera()
        initializeViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCamera()
    }

    private fun initializeCamera() {
        if (checkCameraHardware(this)) {
            camera = getCameraInstance()
        }
    }

    private fun initializeViews() {
        if (camera != null) {
            cameraPreviewSurfaceView = CameraPreviewSurfaceView(this, camera!!)
            layout.addView(cameraPreviewSurfaceView)
        } else {
            Timber.d("Camera is null")
        }
    }

    private fun releaseCamera() {
        camera?.release()
    }

    private fun checkCameraHardware(context: Context) : Boolean {
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true
        }
        return false
    }

    private fun getCameraInstance() : Camera? {
        var camera : Camera? = null
        try {
            Timber.d("Before Open Camera")
            camera = Camera.open(getDefaultCameraId())
            Timber.d("After Open Camera")
        } catch (e :  Exception) {
            e.printStackTrace()
        }
        return camera
    }

    fun getDefaultCameraId(): Int {
        val numberOfCameras = Camera.getNumberOfCameras()
        val cameraInfo = Camera.CameraInfo()
        var defaultCameraId = -1

        for (i in 0 until numberOfCameras) {
            defaultCameraId = i
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == 0) {
                return i
            }
        }

        return defaultCameraId
    }
}