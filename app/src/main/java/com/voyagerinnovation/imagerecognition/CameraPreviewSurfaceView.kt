package com.voyagerinnovation.imagerecognition

import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import timber.log.Timber
import java.io.IOException

class CameraPreviewSurfaceView(context: Context, private val camera: Camera) : SurfaceView(context), SurfaceHolder.Callback {
    init {
        holder.addCallback(this)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        safeStartPreview()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        if (holder?.surface != null) {
            return
        }

        try {
            camera.stopPreview()
        } catch (e : Exception) {
            Timber.d("Error stop camera preview: ${e.message}")
        }

        safeStartPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    private fun safeStartPreview() {
        try {
            camera.setPreviewDisplay(holder)
            camera.startPreview()
        } catch (e : IOException) {
            Timber.d("Error start camera preview: ${e.message}")
        }
    }
}