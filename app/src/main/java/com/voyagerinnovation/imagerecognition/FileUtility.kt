package com.voyagerinnovation.imagerecognition

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

class FileUtility {
    companion object {
        fun getRealPathFromUri(context : Context, contentUri : Uri) : String {
            val result : String
            val cursor = context.contentResolver.query(contentUri, null, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                result = contentUri.getPath();
            } else {
                cursor.moveToFirst();
                result = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                cursor.close();
            }
            return result;
        }
    }
}