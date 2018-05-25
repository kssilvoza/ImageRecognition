package com.voyagerinnovation.imagerecognition

import android.content.Context
import android.hardware.*
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_image_recognition.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream


class LiveImageRecognitionActivity : AppCompatActivity() {
    private lateinit var cameraPreview: CameraPreview

    private lateinit var cameraWrapper: CameraWrapper

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private val apiHelper = ApiHelper()

    private val previewCallback = Camera.PreviewCallback {
        data, camera ->
//            Timber.d("onPreviewFrame")
//
//            val parameters = camera.parameters
//            val size = parameters.previewSize
//            var width = size.width
//            var height = size.height
//
//            var newData = data
//            if (DisplayUtility.getScreenOrientation(this) == Configuration.ORIENTATION_PORTRAIT) {
//                val rotationCount = CameraUtility.getRotationCount(cameraPreview)
//                if (rotationCount == 1 || rotationCount == 3) {
//                    val tmp = width
//                    width = height
//                    height = tmp
//                }
//                newData = CameraUtility.getRotatedData(data, camera, rotationCount)
//            }
//            val imageWrapper = ImageWrapper(newData, width, height)
//
//            val orientation = DisplayUtility.getScreenOrientation(this)
//            val rotationCount = cameraPreview.getDisplayOrientation() / 90
//
//            val imageWrapper = CameraUtility.getRotatedData(data, camera, orientation, rotationCount)
//            val parameters = camera.parameters
//            val size = parameters.previewSize
//            val width = size.width
//            val height = size.height
//            val imageWrapper = ImageWrapper(data, width, height)
//
//            val yuvImage = YuvImage(imageWrapper.data, parameters.previewFormat, imageWrapper.width, imageWrapper.height, null)
//            val out = ByteArrayOutputStream()
//
//            yuvImage.compressToJpeg(Rect(0, 0, imageWrapper.width, imageWrapper.height), 50, out)
//            val imageBytes = out.toByteArray()
//            val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//
//            val image = ImageUtility.convertToBitmap(imageWrapper.data, imageWrapper.width, imageWrapper.height)
//
//            runOnUiThread {
//                Timber.d("Set Image Bitmap")
//                imageview.setImageBitmap(image)
//            }
    }

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
                cameraWrapper.camera.takePicture(null, null, jpegCallback)
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

    private val doImageRecognitionListener = object : ApiHelper.Listener<DoImageRecognitionResponse> {
        override fun onSuccess(response: DoImageRecognitionResponse) {
            Timber.d(response.toString())
        }

        override fun onError(throwable: Throwable) {
            throwable.printStackTrace()
//            accelerometerListener.isListening = true
        }
    }

    private val jpegCallback = Camera.PictureCallback { data, camera ->
//        val parameters = camera.parameters
//        val size = parameters.previewSize
//        val width = size.width
//        val height = size.height
//
//        val yuvImage = YuvImage(data, parameters.previewFormat, width, height, null)
//        val out = ByteArrayOutputStream()
//
//        yuvImage.compressToJpeg(Rect(0, 0, width, height), 50, out)
//        val imageBytes = out.toByteArray()
//        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        ShowCapturedImageAsync().execute(data)
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
            val camera = CameraUtility.getCameraInstance(CameraUtility.getDefaultCameraId())
            if (camera != null) {
                val smallestPictureSize = CameraUtility.getSmallestPictureSize(camera)
                if (smallestPictureSize != null) {
                    camera.parameters.setPictureSize(smallestPictureSize.width, smallestPictureSize.height)
                }
                cameraWrapper = CameraWrapper(camera, CameraUtility.getDefaultCameraId())
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
        cameraPreview = CameraPreview(this, cameraWrapper, null)
        layout.addView(cameraPreview)
    }

    private fun listenToAccelerometer() {
        sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun unlistenToAccelerometer() {
        sensorManager.unregisterListener(accelerometerListener)
    }

    private inner class ShowCapturedImageAsync : AsyncTask<ByteArray, Void, File?>() {
        override fun doInBackground(vararg args: ByteArray): File? {
            val data = args[0]

            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + FOLDER_NAME

            val pictureFileDir = File(directory)
            pictureFileDir.mkdirs()
            if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
                cancel(true)
            }

            val filePath = pictureFileDir.path + File.separator + "Test.jpg"
            val pictureFile = File(filePath)

            try {
                val out = FileOutputStream(pictureFile)
                out.write(data)
                out.close()
                return pictureFile
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(file: File?) {
            if (file != null) {
                apiHelper.doImageRecognition(file, doImageRecognitionListener)
            }
        }
    }

    companion object {
        private const val FOLDER_NAME = "ImageRecognition"
    }
}