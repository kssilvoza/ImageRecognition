package com.voyagerinnovation.imagerecognition;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class ImageUtility {
    public static Bitmap convertToBitmap(byte[] pixelArray, int width, int height) {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int byteCnt = 0;
        for(int i = 0 ; i < height; i++){
            for(int j = 0 ; j < width ; j++){
                byte pixelB = pixelArray[byteCnt];
                int pixelBlue = pixelB & 0xFF;
                int pixelGreen = pixelBlue << 8;
                int pixelRed = pixelGreen << 8;
                int pixelDefault = 0xFF000000;
                int pixelFinal = pixelBlue | pixelRed | pixelGreen | pixelDefault;
                bmp.setPixel(j, i, pixelFinal);

                byteCnt++;
            }
        }
        return bmp;
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }
}
