package com.voyagerinnovation.imagerecognition

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeButtons()
    }

    private fun initializeButtons() {
        button_image_recognition.setOnClickListener({ startImageRecognitionActivity() })
    }

    private fun startImageRecognitionActivity() {
        val intent = Intent(this, ImageRecognitionActivity::class.java)
        startActivity(intent)
    }
}
