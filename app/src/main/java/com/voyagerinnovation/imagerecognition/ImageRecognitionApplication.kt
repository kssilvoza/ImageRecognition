package com.voyagerinnovation.imagerecognition

import android.app.Application
import timber.log.Timber

class ImageRecognitionApplication() : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeLogging()
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}