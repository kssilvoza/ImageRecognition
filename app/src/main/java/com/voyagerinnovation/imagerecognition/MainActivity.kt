package com.voyagerinnovation.imagerecognition

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.io.File


class MainActivity : AppCompatActivity() {
    private val apiHelper = ApiHelper()

    private val doImageRecognitionListener = object : ApiHelper.Listener<DoImageRecognitionResponse> {
        override fun onSuccess(response: DoImageRecognitionResponse) {
            Timber.d(response.toString())
        }

        override fun onError(throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeButtons()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val path = FileUtility.getRealPathFromUri(this, data.data)
                val file = File(path)
                apiHelper.doImageRecognition(file, doImageRecognitionListener)
            }
        }
    }

    private fun initializeButtons() {
        button_image_recognition.setOnClickListener({ doImageRecognition() })
        button_live_image_recognition.setOnClickListener({ startLiveImageRecognition() })
    }

    private fun doImageRecognition() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    private fun startLiveImageRecognition() {
        val intent = Intent(this, LiveImageRecognitionActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_CODE_GALLERY = 1
    }
}
