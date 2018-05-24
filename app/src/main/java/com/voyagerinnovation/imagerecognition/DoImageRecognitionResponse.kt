package com.voyagerinnovation.imagerecognition

import com.google.gson.annotations.SerializedName

data class DoImageRecognitionResponse(@SerializedName("dog") val dog: Float,
                                      @SerializedName("human_nature") val humanNature: Float,
                                      @SerializedName("others") val others: Float,
                                      @SerializedName("qr_code") val qrCode: Float) {
}