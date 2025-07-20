package io.bashpsk.imagekolor.filter

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.round

fun ImageBitmap.getKolorFilterBitmap(filter: KolorFilter): ImageBitmap {

    return getKolorFilterBitmap(filter = filter.colorFilter)
}

fun ImageBitmap.getKolorFilterBitmap(filter: ColorFilter): ImageBitmap {

    val sourceImage=this@getKolorFilterBitmap

    val imageBitmap = ImageBitmap(
        width = sourceImage.width,
        height = sourceImage.height,
        config = sourceImage.config,
        colorSpace = sourceImage.colorSpace
    )

    val paint = Paint().apply {

        colorFilter = filter
    }

    Canvas(image = imageBitmap).drawImageRect(
        image = sourceImage,
        dstOffset = Offset.Zero.round(),
        paint = paint
    )

    return imageBitmap
}