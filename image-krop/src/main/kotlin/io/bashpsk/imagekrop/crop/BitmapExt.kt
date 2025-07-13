package io.bashpsk.imagekrop.crop

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import io.bashpsk.imagekrop.utils.LOG_TAG
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val FLIP_HORIZONTAL_SCALE = -1.0F
private const val FLIP_VERTICAL_SCALE = -1.0F
private const val IDENTITY_SCALE = 1.0F
private const val MIN_CROP_DIMENSION_PX = 1

fun ImageBitmap.getCroppedImageBitmap(
    cropRect: Rect,
    canvasSize: IntSize,
    imageFlip: KropImageFlip? = null,
    kropShape: KropShape = KropShape.SharpeCorner
): KropResult {

    val sourceImageBitmap = this@getCroppedImageBitmap

    if (canvasSize.width <= 0 || canvasSize.height <= 0) {

        val message = "Canvas size must be positive: ${canvasSize.width}x${canvasSize.height}"

        Log.e(LOG_TAG, message)
        return KropResult.Failed(message = message, original = sourceImageBitmap)
    }

    if (sourceImageBitmap.width <= 0 || sourceImageBitmap.height <= 0) {

        val message = "Source image bitmap size must be positive: ${
            sourceImageBitmap.width
        }x${
            sourceImageBitmap.height
        }"

        Log.e(LOG_TAG, message)
        return KropResult.Failed(message = message, original = sourceImageBitmap)
    }

    if (cropRect.width < 0 || cropRect.height < 0) {

        val message = "Crop rectangle dimensions cannot be negative: ${
            cropRect.width
        }x${
            cropRect.height
        }"

        Log.e(LOG_TAG, message)
        return KropResult.Failed(message = message, original = sourceImageBitmap)
    }

    val canvasWidth = canvasSize.width.toFloat()
    val canvasHeight = canvasSize.height.toFloat()
    val actualBitmapWidth = sourceImageBitmap.width.toFloat()
    val actualBitmapHeight = sourceImageBitmap.height.toFloat()

    val widthRatio = canvasWidth / actualBitmapWidth
    val heightRatio = canvasHeight / actualBitmapHeight
    val scaleFactor = min(widthRatio, heightRatio)

    val displayedImageWidth = actualBitmapWidth * scaleFactor
    val displayedImageHeight = actualBitmapHeight * scaleFactor

    val offsetX = (canvasWidth - displayedImageWidth) / 2f
    val offsetY = (canvasHeight - displayedImageHeight) / 2f

    val cropLeft = transformToBitmapCoordinate(
        canvasCoordinate = cropRect.left,
        canvasOffset = offsetX,
        scaleFactor = scaleFactor,
        maxBitmapDimension = actualBitmapWidth.toInt()
    )

    val cropTop = transformToBitmapCoordinate(
        canvasCoordinate = cropRect.top,
        canvasOffset = offsetY,
        scaleFactor = scaleFactor,
        maxBitmapDimension = actualBitmapHeight.toInt()
    )

    val cropRight = transformToBitmapCoordinate(
        canvasCoordinate = cropRect.right,
        canvasOffset = offsetX,
        scaleFactor = scaleFactor,
        maxBitmapDimension = actualBitmapWidth.toInt()
    )

    val cropBottom = transformToBitmapCoordinate(
        canvasCoordinate = cropRect.bottom,
        canvasOffset = offsetY,
        scaleFactor = scaleFactor,
        maxBitmapDimension = actualBitmapHeight.toInt()
    )

    val validatedCropLeft = min(cropLeft, cropRight)
    val validatedCropTop = min(cropTop, cropBottom)
    val validatedCropRight = max(cropLeft, cropRight)
    val validatedCropBottom = max(cropTop, cropBottom)

    val cropWidth = (validatedCropRight - validatedCropLeft).coerceAtLeast(MIN_CROP_DIMENSION_PX)
    val cropHeight = (validatedCropBottom - validatedCropTop).coerceAtLeast(MIN_CROP_DIMENSION_PX)

    if (cropWidth <= MIN_CROP_DIMENSION_PX && cropHeight <= MIN_CROP_DIMENSION_PX) {

        val message = "Calculated crop dimensions too small: ${
            cropWidth
        }x${cropHeight}. Minimum is $MIN_CROP_DIMENSION_PX."

        Log.w(LOG_TAG, message)
    }

    try {

        val androidBitmap = sourceImageBitmap.asAndroidBitmap()
        var bitmapToProcess = androidBitmap

        if (imageFlip != null) {

            val matrix = Matrix()

            when (imageFlip) {

                KropImageFlip.Horizontal -> matrix.preScale(FLIP_HORIZONTAL_SCALE, IDENTITY_SCALE)
                KropImageFlip.Vertical -> matrix.preScale(IDENTITY_SCALE, FLIP_VERTICAL_SCALE)
                else -> Unit
            }

            if (matrix.isIdentity.not()) {

                bitmapToProcess = Bitmap.createBitmap(
                    androidBitmap,
                    0,
                    0,
                    androidBitmap.width,
                    androidBitmap.height,
                    matrix,
                    true
                )
            }
        }

        val croppedBitmap = Bitmap.createBitmap(
            bitmapToProcess,
            validatedCropLeft,
            validatedCropTop,
            cropWidth,
            cropHeight
        ).asImageBitmap()

        val shapedBitmap = bitmapShapeMask(imageBitmap = croppedBitmap, kropShape = kropShape)

        return KropResult.Success(cropped = shapedBitmap, original = sourceImageBitmap)
    } catch (exception: IllegalArgumentException) {

        val message = "Image Crop Failed: Invalid dimensions for bitmap. ${exception.message}"

        Log.e(LOG_TAG, message, exception)
        return KropResult.Failed(message = message, original = sourceImageBitmap)
    } catch (exception: OutOfMemoryError) {

        val message = "Image Crop Failed: Out of memory. ${exception.message}"

        Log.e(LOG_TAG, message, exception)
        return KropResult.Failed(message = message, original = sourceImageBitmap)
    } catch (exception: Exception) {

        val message = "Image Crop Failed: An unexpected error occurred. ${exception.message}"

        Log.e(LOG_TAG, message, exception)
        return KropResult.Failed(message = message, original = sourceImageBitmap)
    }
}

private fun transformToBitmapCoordinate(
    canvasCoordinate: Float,
    canvasOffset: Float,
    scaleFactor: Float,
    maxBitmapDimension: Int
): Int {

    if (scaleFactor == 0f) return 0

    return ((canvasCoordinate - canvasOffset) / scaleFactor).roundToInt().coerceIn(
        range = 0..maxBitmapDimension
    )
}

fun bitmapShapeMask(
    imageBitmap: ImageBitmap,
    kropShape: KropShape
): ImageBitmap {

    val width = imageBitmap.width
    val height = imageBitmap.height

    val outputImageBitmap = ImageBitmap(
        width = width,
        height = height,
        config = ImageBitmapConfig.Argb8888
    )

    Canvas(image = outputImageBitmap).apply {

        val shapePath = findKropShapePath(
            kropShape = kropShape,
            width = width.toFloat(),
            height = height.toFloat()
        )

        val paint = Paint().apply {

            isAntiAlias = true
            shader = ImageShader(imageBitmap)
        }

        save()
        clipPath(path = shapePath)
        drawImageRect(image = imageBitmap, paint = paint)
        restore()
    }

    return outputImageBitmap
}

internal fun findKropShapePath(
    kropShape: KropShape,
    width: Float,
    height: Float,
    radiusSize: Float = 0.05F
): Path {

    val rect = Rect(left = 0.0F, top = 0.0F, right = width, bottom = height)

    return Path().apply {

        when (kropShape) {

            KropShape.Circle -> addOval(oval = rect)

            KropShape.RoundedCorner -> {

                val cornerRadius = CornerRadius(
                    x = width * radiusSize,
                    y = height * radiusSize
                )

                addRoundRect(RoundRect(rect = rect, cornerRadius = cornerRadius))
            }

            KropShape.CutCorner -> {

                val cut = minOf(width, height) * radiusSize

                moveTo(rect.left + cut, rect.top)
                lineTo(rect.right - cut, rect.top)
                lineTo(rect.right, rect.top + cut)
                lineTo(rect.right, rect.bottom - cut)
                lineTo(rect.right - cut, rect.bottom)
                lineTo(rect.left + cut, rect.bottom)
                lineTo(rect.left, rect.bottom - cut)
                lineTo(rect.left, rect.top + cut)
                close()
            }

            KropShape.Triangle -> {

                moveTo(rect.center.x, rect.top)
                lineTo(rect.right, rect.bottom)
                lineTo(rect.left, rect.bottom)
                close()
            }

            KropShape.Star -> addPath(createStarPath(rect))
            KropShape.Pentagon -> addPath(createPolygonPath(rect, sides = 5))
            KropShape.Hexagon -> addPath(createPolygonPath(rect, sides = 6))
            KropShape.Heptagon -> addPath(createPolygonPath(rect, sides = 7))
            KropShape.Octagon -> addPath(createPolygonPath(rect, sides = 8))
            KropShape.Nonagon -> addPath(createPolygonPath(rect, sides = 9))
            KropShape.Decagon -> addPath(createPolygonPath(rect, sides = 10))
            KropShape.SharpeCorner -> addRect(rect)
        }
    }
}

internal fun createPolygonPath(rect: Rect, sides: Int): Path {

    val path = Path()
    val radius = min(rect.width, rect.height) / 2
    val centerX = rect.center.x
    val centerY = rect.center.y
    val angle = (2 * PI / sides)
    val startAngle = if (sides % 2 != 0) -PI / 2 else 0.0

    for (i in 0 until sides) {

        val theta = startAngle + i * angle
        val x = centerX + radius * cos(theta).toFloat()
        val y = centerY + radius * sin(theta).toFloat()

        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    path.close()

    return path
}

internal fun createStarPath(rect: Rect): Path {

    val path = Path()
    val centerX = rect.center.x
    val centerY = rect.center.y
    val outerRadius = min(rect.width, rect.height) / 2
    val innerRadius = outerRadius / 2.5F
    val points = 10

    for (i in 0 until points) {

        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = Math.toRadians((i * 360.0 / points) - 90)
        val x = centerX + radius * cos(angle).toFloat()
        val y = centerY + radius * sin(angle).toFloat()

        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    path.close()

    return path
}