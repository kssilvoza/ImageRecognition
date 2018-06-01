package com.voyagerinnovation.imagerecognition

import android.content.res.Configuration
import android.graphics.Bitmap
import android.hardware.Camera
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer


class QRCodeUtility {
    companion object {
        fun getQRCodeResult(bitmap: Bitmap) : Result? {
            val intArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
            return readSource(source)
        }

        fun getQRCodeResult(data: ByteArray, camera: Camera, orientation: Int, rotationCount: Int) : Result? {
            val parameters = camera.parameters
            val size = parameters.pictureSize
            var width = size.width
            var height = size.height

            var newData = data
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (rotationCount == 1 || rotationCount == 3) {
                    val tmp = width
                    width = height
                    height = tmp
                }
                newData = CameraUtility.getRotatedData(data, camera, rotationCount)
            }

            val source = QRCodeUtility.buildPlanarYUVLuminanceSource(newData, width, height)
            return readSource(source)
        }

        private fun readSource(source: LuminanceSource?) : Result? {
            if (source != null) {
                var binaryBitmap = BinaryBitmap(HybridBinarizer(source))

                val multiFormatReader = MultiFormatReader()

                var result: Result? = null
                try {
                    result = multiFormatReader.decodeWithState(binaryBitmap)
                } catch (re: ReaderException) {
                    re.printStackTrace()
                } catch (npe: NullPointerException) {
                    npe.printStackTrace()
                } catch (ae: ArrayIndexOutOfBoundsException) {
                    ae.printStackTrace()
                } finally {
                    multiFormatReader.reset()
                }

                if (result == null) {
                    val invertedSource = source.invert()
                    binaryBitmap = BinaryBitmap(HybridBinarizer(invertedSource))

                    try {
                        result = multiFormatReader.decodeWithState(binaryBitmap)
                    } catch (nfe: NotFoundException) {
                        nfe.printStackTrace()
                    } finally {
                        multiFormatReader.reset()
                    }
                }

                return result
            }

            return null
        }

        private fun buildPlanarYUVLuminanceSource(data: ByteArray, width: Int, height: Int): PlanarYUVLuminanceSource? {
            var source: PlanarYUVLuminanceSource? = null

            try {
                source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return source
        }
    }
}