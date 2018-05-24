package com.voyagerinnovation.imagerecognition

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class ApiHelper {
    private val imageRecognitionApi: ImageRecognitionApi

    interface Listener<T> {
        fun onSuccess(response: T)

        fun onError(throwable: Throwable)
    }

    init {
        val httpLoggingInterceptor = HttpLoggingInterceptor().setLevel(
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.BASIC
        )

        val okHttpClient = OkHttpClient().newBuilder().addInterceptor(httpLoggingInterceptor).build()

        val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

        imageRecognitionApi = retrofit.create(ImageRecognitionApi::class.java)
    }

    fun doImageRecognition(file: File, listener: Listener<DoImageRecognitionResponse>) {
        val part = MultipartBody.Part.createFormData("file", file.name, RequestBody.create(MediaType.parse("image/*"), file))
        val call = imageRecognitionApi.doImageRecognition(part)
        call.enqueue(object : Callback<DoImageRecognitionResponse> {
            override fun onResponse(call: Call<DoImageRecognitionResponse>?, response: Response<DoImageRecognitionResponse>?) {
                if (response != null && response.isSuccessful && response.body() != null) {
                    listener.onSuccess(response.body()!!)
                } else {
                    listener.onError(NullPointerException())
                }
            }

            override fun onFailure(call: Call<DoImageRecognitionResponse>?, t: Throwable?) {
                if (t != null) {
                    listener.onError(t)
                } else {
                    listener.onError(NullPointerException())
                }
            }
        })
    }
}