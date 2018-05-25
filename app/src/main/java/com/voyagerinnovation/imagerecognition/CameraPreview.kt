package com.voyagerinnovation.imagerecognition

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.hardware.Camera
import android.os.Handler
import android.view.*
import timber.log.Timber

class CameraPreview(context: Context, private val cameraWrapper: CameraWrapper, private val previewCallback: Camera.PreviewCallback) : SurfaceView(context), SurfaceHolder.Callback {
    private val autoFocusHandler = Handler()

    private var previewing = true
    private var autoFocus = true
    private var surfaceCreated = false

    init {
        holder.addCallback(this)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        surfaceCreated = true
        showCameraPreview()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Timber.d("On Surface Changed")
        if (holder?.surface != null) {
            return
        }
        stopCameraPreview()
        showCameraPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        surfaceCreated = false
        stopCameraPreview()
    }

    fun showCameraPreview() {
        try {
            holder.addCallback(this)
            previewing = true
            setupCameraParameters()
            cameraWrapper.camera.setPreviewDisplay(holder)
            cameraWrapper.camera.setDisplayOrientation(getDisplayOrientation())
            cameraWrapper.camera.setPreviewCallback(previewCallback)
            cameraWrapper.camera.startPreview()
            if (autoFocus) {
                if (surfaceCreated) { // check if surface created before using autofocus
                    safeAutoFocus()
                } else {
                    scheduleAutoFocus() // wait 1 sec and then do check again
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopCameraPreview() {
        try {
            previewing = false
            holder.removeCallback(this)
            cameraWrapper.camera.cancelAutoFocus()
            cameraWrapper.camera.setPreviewCallback(null)
            cameraWrapper.camera.stopPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupCameraParameters() {
        val optimalSize = getOptimalPreviewSize()
        val parameters = cameraWrapper.camera.parameters
        parameters.setPreviewSize(optimalSize!!.width, optimalSize.height)
        cameraWrapper.camera.parameters = parameters
        adjustViewSize(optimalSize)
    }

    private fun getOptimalPreviewSize(): Camera.Size? {
        val sizes = cameraWrapper.camera.parameters.supportedPreviewSizes
        var w = width
        var h = height
        if (getScreenOrientation(context) == Configuration.ORIENTATION_PORTRAIT) {
            val portraitWidth = h
            h = w
            w = portraitWidth
        }

        val targetRatio = w.toDouble() / h
        if (sizes == null) return null

        var optimalSize: Camera.Size? = null
        var minDiff = java.lang.Double.MAX_VALUE

        val targetHeight = h

        // Try to find an size match aspect ratio and size
        for (size in sizes!!) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE_DEFAULT) continue
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - targetHeight).toDouble()
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = java.lang.Double.MAX_VALUE
            for (size in sizes!!) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - targetHeight).toDouble()
                }
            }
        }
        return optimalSize
    }

    private fun adjustViewSize(cameraSize: Camera.Size) {
        val previewSize = convertSizeToLandscapeOrientation(Point(width, height))
        val cameraRatio = cameraSize.width.toFloat() / cameraSize.height
        val screenRatio = previewSize.x.toFloat() / previewSize.y

        if (screenRatio > cameraRatio) {
            setViewSize((previewSize.y * cameraRatio).toInt(), previewSize.y)
        } else {
            setViewSize(previewSize.x, (previewSize.x / cameraRatio).toInt())
        }
    }

    private fun convertSizeToLandscapeOrientation(size: Point): Point {
        return if (getDisplayOrientation() % 180 == 0) {
            size
        } else {
            Point(size.y, size.x)
        }
    }

    private fun setViewSize(width: Int, height: Int) {
        val layoutParams = layoutParams
        var tmpWidth: Int
        var tmpHeight: Int
        if (getDisplayOrientation() % 180 == 0) {
            tmpWidth = width
            tmpHeight = height
        } else {
            tmpWidth = height
            tmpHeight = width
        }

        if (SHOULD_SCALE_TO_FIT) {
            val parentWidth = (parent as View).width
            val parentHeight = (parent as View).height
            val ratioWidth = parentWidth.toFloat() / tmpWidth.toFloat()
            val ratioHeight = parentHeight.toFloat() / tmpHeight.toFloat()

            val compensation: Float

            if (ratioWidth > ratioHeight) {
                compensation = ratioWidth
            } else {
                compensation = ratioHeight
            }

            tmpWidth = Math.round(tmpWidth * compensation)
            tmpHeight = Math.round(tmpHeight * compensation)
        }

        layoutParams.width = tmpWidth
        layoutParams.height = tmpHeight
        setLayoutParams(layoutParams)
    }

    fun getDisplayOrientation(): Int {
        val info = Camera.CameraInfo()
        if (cameraWrapper.cameraId == -1) {
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info)
        } else {
            Camera.getCameraInfo(cameraWrapper.cameraId, info)
        }

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay

        val rotation = display.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        return result
    }

    private fun getScreenOrientation(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay

        val orientation : Int
        if (display.width == display.height) {
            orientation = Configuration.ORIENTATION_SQUARE
        } else {
            if (display.width < display.height) {
                orientation = Configuration.ORIENTATION_PORTRAIT
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE
            }
        }
        return orientation
    }

    fun safeAutoFocus() {
        try {
            cameraWrapper.camera.autoFocus(autoFocusCB)
        } catch (re: RuntimeException) {
            // Horrible hack to deal with autofocus errors on Sony devices
            // See https://github.com/dm77/barcodescanner/issues/7 for example
            scheduleAutoFocus() // wait 1 sec and then do check again
        }

    }

    fun setAutoFocus(state: Boolean) {
        if (previewing) {
            if (state == autoFocus) {
                return
            }
            autoFocus = state
            if (autoFocus) {
                if (surfaceCreated) { // check if surface created before using autofocus
                    Timber.v("Starting autofocus")
                    safeAutoFocus()
                } else {
                    scheduleAutoFocus() // wait 1 sec and then do check again
                }
            } else {
                Timber.v("Cancelling autofocus")
                cameraWrapper.camera.cancelAutoFocus()
            }
        }
    }

    private val doAutoFocus = Runnable {
        if (previewing && autoFocus && surfaceCreated) {
            safeAutoFocus()
        }
    }

    // Mimic continuous auto-focusing
    private var autoFocusCB: Camera.AutoFocusCallback = Camera.AutoFocusCallback { success, camera -> scheduleAutoFocus() }

    private fun scheduleAutoFocus() {
        autoFocusHandler.postDelayed(doAutoFocus, 1000)
    }

    companion object {
        private const val ASPECT_TOLERANCE_DEFAULT = 0.1f
        private const val SHOULD_SCALE_TO_FIT = true
    }
}