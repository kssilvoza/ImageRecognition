package com.voyagerinnovation.imagerecognition

class ImageRecognitionUtility {
    companion object {
        const val HUMAN_NATURE = 1
        const val DOGS = 2
        const val QR_CODE = 3
        const val OTHERS = 4
        const val NONE = 5

        private const val MINIMUM_VALUE = 70

        fun checkImageRecognitionResponse(doImageRecognitionResponse: DoImageRecognitionResponse) : Int {
            when {
                doImageRecognitionResponse.humanNature > MINIMUM_VALUE -> return HUMAN_NATURE
                doImageRecognitionResponse.dog > MINIMUM_VALUE -> return DOGS
                doImageRecognitionResponse.qrCode > MINIMUM_VALUE -> return QR_CODE
                doImageRecognitionResponse.others > MINIMUM_VALUE -> return OTHERS
                else -> return NONE
            }
        }
    }
}