package io.bashpsk.imagekrop.crop

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import io.bashpsk.imagekrop.utils.LOG_TAG
import kotlin.math.min
import kotlin.math.roundToInt

fun ImageBitmap.getCroppedImageBitmap(
    cropRect: Rect,
    canvasSize: IntSize,
    imageFlip: KropImageFlip? = null
): KropResult {

    val canvasWidth = canvasSize.width.toFloat()
    val canvasHeight = canvasSize.height.toFloat()
    val bitmapWidth = this@getCroppedImageBitmap.width.toFloat()
    val bitmapHeight = this@getCroppedImageBitmap.height.toFloat()

    val widthRatio = canvasWidth / bitmapWidth
    val heightRatio = canvasHeight / bitmapHeight
    val scaleFactor = min(widthRatio, heightRatio)

    val displayedImageWidth = bitmapWidth * scaleFactor
    val displayedImageHeight = bitmapHeight * scaleFactor

    val offsetX = (canvasWidth - displayedImageWidth) / 2
    val offsetY = (canvasHeight - displayedImageHeight) / 2

    val cropLeft = ((cropRect.left - offsetX) / scaleFactor).roundToInt().coerceIn(
        0..bitmapWidth.toInt()
    )

    val cropTop = ((cropRect.top - offsetY) / scaleFactor).roundToInt().coerceIn(
        0..bitmapHeight.toInt()
    )

    val cropRight = ((cropRect.right - offsetX) / scaleFactor).roundToInt().coerceIn(
        0..bitmapWidth.toInt()
    )

    val cropBottom = ((cropRect.bottom - offsetY) / scaleFactor).roundToInt().coerceIn(
        0..bitmapHeight.toInt()
    )

    val maxCropWidth = (bitmapWidth - cropLeft).toInt().coerceAtLeast(1)
    val maxCropHeight = (bitmapHeight - cropTop).toInt().coerceAtLeast(1)

    val cropWidth = (cropRight - cropLeft).coerceIn(1, maxCropWidth)
    val cropHeight = (cropBottom - cropTop).coerceIn(1, maxCropHeight)

    return try {

        if (cropWidth <= 1 || cropHeight <= 1) {

            val message = "Image Crop Size Too Small : $cropWidth x $cropHeight"

            Log.e(LOG_TAG, message)
            KropResult.Failed(message = message, original = this)
        } else {

            val originalBitmap = this@getCroppedImageBitmap.asAndroidBitmap()

            val matrix = Matrix().apply {

                when (imageFlip) {

                    KropImageFlip.LeftToRight, KropImageFlip.RightToLeft -> preScale(-1.0F, 1.0F)
                    KropImageFlip.TopToBottom, KropImageFlip.BottomToTop -> preScale(1.0F, -1.0F)
                    else -> Unit
                }
            }

            val flippedBitmap = Bitmap.createBitmap(
                originalBitmap,
                0,
                0,
                originalBitmap.width,
                originalBitmap.height,
                matrix,
                true
            )

            val croppedBitmap = Bitmap.createBitmap(
                flippedBitmap,
                cropLeft,
                cropTop,
                cropWidth,
                cropHeight
            ).asImageBitmap()

            KropResult.Success(cropped = croppedBitmap, original = this@getCroppedImageBitmap)
        }
    } catch (exception: Exception) {

        val message = "Image Crop Failed : ${exception.message?.ifEmpty { "Unknown" }}"

        Log.e(LOG_TAG, message, exception)
        KropResult.Failed(message = message, original = this@getCroppedImageBitmap)
    }
}