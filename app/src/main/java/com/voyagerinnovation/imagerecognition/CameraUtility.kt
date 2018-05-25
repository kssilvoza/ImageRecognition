package com.voyagerinnovation.imagerecognition

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
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

        fun getSmallestPictureSize(camera: Camera) : Camera.Size? {
            var smallestPictureSize : Camera.Size? = null
            var smallestArea = Int.MAX_VALUE
            for (currPictureSize in camera.parameters.supportedPictureSizes) {
                val currArea = currPictureSize.width * currPictureSize.height
                if (currArea < smallestArea) {
                    smallestPictureSize = currPictureSize
                    smallestArea = currArea
                }
            }

            return smallestPictureSize
        }

//        fun getRotatedData(data: ByteArray, camera: Camera, orientation: Int, rotationCount: Int): ImageWrapper {
//            val parameters = camera.parameters
//            val size = parameters.previewSize
//            var width = size.width
//            var height = size.height
//
//            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//                var newData = data
//
//                if (rotationCount == 1 || rotationCount == 3) {
//                    val tmp = width
//                    width = height
//                    height = tmp
//
//                    for (i in 0 until rotationCount) {
//                        val rotatedData = ByteArray(data.size)
//                        for (y in 0 until height) {
//                            for (x in 0 until width)
//                                rotatedData[x * height + height - y - 1] = data[x + y * width]
//                        }
//                        newData = rotatedData
//                        val tmp1 = width
//                        width = height
//                        height = tmp1
//                    }
//                }
//
//                return ImageWrapper(newData, width, height)
//            } else {
//                return ImageWrapper(data, width, height)
//            }
//        }

        fun getRotatedData(data: ByteArray, camera: Camera, rotationCount: Int): ByteArray {
            var newData = data
            val parameters = camera.parameters
            val size = parameters.previewSize
            var width = size.width
            var height = size.height

            if (rotationCount == 1 || rotationCount == 3) {
                for (i in 0 until rotationCount) {
                    val rotatedData = ByteArray(newData.size)
                    for (y in 0 until height) {
                        for (x in 0 until width)
                            rotatedData[x * height + height - y - 1] = newData[x + y * width]
                    }
                    newData = rotatedData
                    val tmp = width
                    width = height
                    height = tmp
                }
            }

            return newData
        }

        fun getRotationCount(cameraPreview: CameraPreview): Int {
            val displayOrientation = cameraPreview.getDisplayOrientation()
            return displayOrientation / 90
        }
    }
}