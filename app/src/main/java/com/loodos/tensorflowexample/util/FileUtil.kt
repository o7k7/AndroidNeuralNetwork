package com.loodos.tensorflowexample.util

/**
 * Created by orhunkupeli on 6.04.2020
 */
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtil {
    @JvmStatic
    @Throws(IOException::class)
    fun save(context: Context, bytes: ByteArray, file: File, callback: () -> Unit) {
        try {
            FileOutputStream(file).use { output -> output.write(bytes) }
        } finally {
            callback.invoke()
            notifyMediaStoreScanner(file, context)
        }
    }

    // > Android.Q
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @JvmStatic
    fun save(context: Context, bitmap: Bitmap, @NonNull name: String, callback: () -> Unit) {
        val resolver: ContentResolver = context.contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/signalify")
        val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri != null) {
            val fos = resolver.openOutputStream(imageUri)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos?.flush()
            fos?.close()
        }
        callback.invoke()
    }

    @JvmStatic
    fun getFileName() = System.currentTimeMillis().toString()

    @JvmStatic
    fun getImageFileFormat() = ".jpg"

    @JvmStatic
    fun getImageFileName() = "/IMG_" + System.currentTimeMillis()

    @JvmStatic
    fun getImageFileNameWithFormat() = "/IMG_" + System.currentTimeMillis() + ".jpg"

    private fun notifyMediaStoreScanner(file: File, context: Context) {
        MediaScannerConnection.scanFile(context.applicationContext, arrayOf(file.path), null)
        { path: String, _: Uri? -> Log.i("TEST:: ", "Scanned $path") }
    }
}