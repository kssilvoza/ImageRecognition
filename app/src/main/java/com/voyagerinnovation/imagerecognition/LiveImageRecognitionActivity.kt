package com.voyagerinnovation.imagerecognition

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.*
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_image_recognition.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream


class LiveImageRecognitionActivity : AppCompatActivity() {
    private lateinit var cameraPreview: CameraPreview

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private var cameraId = 0

    private val apiHelper = ApiHelper()

    private val accelerometerListener = object : SensorEventListener {
        private val UPDATE_TIME = 100
        private val MOVEMENT_LIMIT = 0.20
        private val STEADY_COUNT_LIMIT = 7

        private var prevX = 0f
        private var prevY = 0f
        private var prevZ = 0f

        private var prevTime : Long = 0

        private var steadyCount = 0

        var isListening = true

        override fun onSensorChanged(event: SensorEvent?) {
            if (steadyCount >= STEADY_COUNT_LIMIT) {
                // TODO - Add Snackbar Prompt
                Timber.d("Phone is steady")
                cameraPreview.takePicture(null, null, jpegCallback)
                steadyCount = 0
                isListening = false
            }

            if (isListening && event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val currTime = System.currentTimeMillis()

                if ((currTime - prevTime) > UPDATE_TIME) {
                    val changeX = Math.abs(x - prevX)
                    val changeY = Math.abs(y - prevY)
                    val changeZ = Math.abs(z - prevZ)

                    Timber.d("X = $changeX Y = $changeY Z = $changeZ")

                    if (changeX < MOVEMENT_LIMIT && changeY < MOVEMENT_LIMIT && changeZ < MOVEMENT_LIMIT) {
                        steadyCount += 1
                    } else {
                        steadyCount = 0
                    }

                    prevX = x
                    prevY = y
                    prevZ = z

                    prevTime = currTime
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    private val jpegCallback = Camera.PictureCallback { data, camera ->
        ShowCapturedImageAsync().execute(data)
    }

    private val previewCallback = Camera.PreviewCallback { data, camera ->
        val orientation = DisplayUtility.getScreenOrientation(this)
        val rotationCount = cameraPreview.getRotationCount()
        val result = QRCodeUtility.getQRCodeResult(data, camera, orientation, rotationCount)
        if (result != null) {
            startResultActivity(ResultActivity.TYPE_QR_CODE, result.toString())
        }
    }

    private val doImageRecognitionListener = object : ApiHelper.Listener<DoImageRecognitionResponse> {
        override fun onSuccess(response: DoImageRecognitionResponse) {
            Timber.d(response.toString())
            val result = ImageRecognitionUtility.checkImageRecognitionResponse(response)
            checkImageRecognitionResult(result, response)
        }

        override fun onError(throwable: Throwable) {
            accelerometerListener.isListening = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recognition)
        initializeCamera()
        initializeAccelerometer()
        initializeViews()
    }

    override fun onResume() {
        super.onResume()
        listenToAccelerometer()
    }

    override fun onPause() {
        super.onPause()
        unlistenToAccelerometer()
    }

    private fun initializeCamera() {
        if (CameraUtility.checkCameraHardware(this)) {
            cameraId = CameraUtility.getDefaultCameraId()
            val camera = CameraUtility.getCameraInstance(cameraId)
            if (camera != null) {
                camera.setPreviewCallback(previewCallback)
                val smallestPictureSize = CameraUtility.getSmallestPictureSize(camera)
                val cameraWrapper = CameraWrapper(camera, cameraId)
                cameraPreview = CameraPreview(this, cameraWrapper, smallestPictureSize)
                layout.addView(cameraPreview)
            } else {
                finish()
            }
        }
    }

    private fun initializeAccelerometer() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun initializeViews() {
    }

    private fun listenToAccelerometer() {
        sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun unlistenToAccelerometer() {
        sensorManager.unregisterListener(accelerometerListener)
    }

    private fun checkImageRecognitionResult(result: Int, doImageRecognitionResponse: DoImageRecognitionResponse) {
        when(result) {
            ImageRecognitionUtility.HUMAN_NATURE, ImageRecognitionUtility.DOGS, ImageRecognitionUtility.OTHERS -> {
                Timber.d(doImageRecognitionResponse.toString())
                startResultActivity(ResultActivity.TYPE_IMAGE_RECOGNITION, doImageRecognitionResponse.toString())
            }
            else -> accelerometerListener.isListening = true
        }
    }

    private inner class ShowCapturedImageAsync : AsyncTask<ByteArray, Void, Pair<Bitmap, File>?>() {
        override fun doInBackground(vararg args: ByteArray): Pair<Bitmap, File>? {
            val data = args[0]

            val degree = CameraUtility.getRotationAngle(cameraId, this@LiveImageRecognitionActivity)
            val originalBitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            val rotatedBitmap = ImageUtility.rotate(originalBitmap, degree)

            originalBitmap.recycle()

            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + FOLDER_NAME

            val pictureFileDir = File(directory)
            pictureFileDir.mkdirs()
            if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
                cancel(true)
            }

            val filePath = pictureFileDir.path + File.separator + "Test.jpg"
            val file = File(filePath)

            try {
                val out = FileOutputStream(file)
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                out.close()
                return Pair(rotatedBitmap, file)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(pair: Pair<Bitmap, File>?) {
            if (pair != null) {
                showImage(pair.first)
                apiHelper.doImageRecognition(pair.second, doImageRecognitionListener)
            }
        }
    }

    private fun startResultActivity(type: Int, result: String) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.KEY_RESULT_TYPE, type)
        intent.putExtra(ResultActivity.KEY_RESULT, result)
        startActivity(intent)
        finish()
    }

    private fun showImage(bitmap: Bitmap) {
        imageview.setImageBitmap(bitmap)
        imageview.visibility = View.VISIBLE
    }

    companion object {
        private const val FOLDER_NAME = "ImageRecognition"
    }
}