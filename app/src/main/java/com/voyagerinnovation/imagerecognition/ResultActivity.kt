package com.voyagerinnovation.imagerecognition

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        initializeViews()
    }

    private fun initializeViews() {
        val resultType = intent.getIntExtra(KEY_RESULT_TYPE, 0)
        val result = intent.getStringExtra(KEY_RESULT)

        when (resultType) {
            TYPE_IMAGE_RECOGNITION -> textview_label_image_recognition.visibility = View.VISIBLE
            TYPE_QR_CODE -> textview_label_qr_code.visibility = View.VISIBLE
        }

        textview_result.text = result
    }

    companion object {
        const val KEY_RESULT_TYPE = "Key Result Type"
        const val KEY_RESULT = "Key Result"

        const val TYPE_IMAGE_RECOGNITION = 1
        const val TYPE_QR_CODE = 2
    }
}
