package com.voyagerinnovation.imagerecognition

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera

class CameraUtility {
    companion object {
        fun checkCameraHardware(context: Context): Boolean {
            if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                return true
            }
            return false
        }

        fun getCameraInstance(cameraId: Int): Camera? {
            var camera: Camera? = null
            try {
                if (cameraId == -1) {
                    camera = Camera.open()
                } else {
                    camera = Camera.open(cameraId)
                }
            } catch (e: Exception) {
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
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    return i
                }
            }

            return defaultCameraId
        }
    }
}