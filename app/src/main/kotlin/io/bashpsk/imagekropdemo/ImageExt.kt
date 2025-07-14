package io.bashpsk.imagekropdemo

import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

internal suspend fun ImageBitmap.saveAsFile(name: String): File? = withContext(Dispatchers.IO) {

    val bitmap = this@saveAsFile.asAndroidBitmap()

    val PictureDirectory = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "Image Krop"
    )

    if (PictureDirectory.exists().not()) PictureDirectory.mkdirs()

    val file = File(PictureDirectory, "$name.png")

    return@withContext try {

        val outputStream = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        file
    } catch (exception: Exception) {

        Log.e("ImageKrop", "Saving image failed: ${exception.message}")
        null
    }
}