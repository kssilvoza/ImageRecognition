package com.voyagerinnovation.imagerecognition

import android.content.res.Configuration
import android.hardware.Camera
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import timber.log.Timber

class QRCodeUtility {
    companion object {
        fun getQRCodeResult(data: ByteArray, camera: Camera, orientation: Int, rotationCount: Int) : Result? {
            Timber.d("Byte array size: ${data.size}")
            Timber.d("Preview size:  ${camera.parameters.previewSize.width} ${camera.parameters.previewSize.height}")
            Timber.d("Picture size: ${camera.parameters.pictureSize.width} ${camera.parameters.pictureSize.height}")
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

            val source = QRCodeUtility.buildLuminanceSource(newData, width, height)
            if (source != null) {
                val multiFormatReader = MultiFormatReader()

                var bitmap = BinaryBitmap(HybridBinarizer(source))

                var result: Result? = null
                try {
                    result = multiFormatReader.decodeWithState(bitmap)
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
                    bitmap = BinaryBitmap(HybridBinarizer(invertedSource))

                    try {
                        result = multiFormatReader.decodeWithState(bitmap)
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

        private fun buildLuminanceSource(data: ByteArray, width: Int, height: Int): PlanarYUVLuminanceSource? {
            var source: PlanarYUVLuminanceSource? = null

            try {
                source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
            } catch (var7: Exception) {
                var7.printStackTrace()
            }

            return source
        }
    }
}