package com.voyagerinnovation.imagerecognition

import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_image_recognition.*

class LiveImageRecognitionActivity : AppCompatActivity() {
    private lateinit var cameraPreview: CameraPreview

    private lateinit var cameraWrapper: CameraWrapper

    private val previewCallback = object: Camera.PreviewCallback {
        override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recognition)
        initializeCamera()
        initializeViews()
    }

    private fun initializeCamera() {
        if (CameraUtility.checkCameraHardware(this)) {
            val camera = CameraUtility.getCameraInstance(CameraUtility.getDefaultCameraId())
            if (camera != null) {
                cameraWrapper = CameraWrapper(camera, CameraUtility.getDefaultCameraId())
            } else {
                finish()
            }
        }
    }

    private fun initializeViews() {
        cameraPreview = CameraPreview(this, cameraWrapper, previewCallback)
        layout.addView(cameraPreview)
    }
}