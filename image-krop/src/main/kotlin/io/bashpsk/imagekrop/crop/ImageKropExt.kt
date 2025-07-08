package io.bashpsk.imagekrop.crop

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntSize

internal fun DrawScope.drawHandle(
    corner: KropCorner,
    center: Offset,
    kropConfig: KropConfig
) {

    val handleLength = kropConfig.handleLength

    when (corner) {

        KropCorner.TOP_LEFT -> {

            drawKropLine(
                start = center,
                end = Offset(center.x + handleLength, center.y),
                kropConfig = kropConfig
            )

            drawKropLine(
                start = center,
                end = Offset(center.x, center.y + handleLength),
                kropConfig = kropConfig
            )
        }

        KropCorner.TOP_RIGHT -> {

            drawKropLine(
                start = center,
                end = Offset(center.x - handleLength, center.y),
                kropConfig = kropConfig
            )

            drawKropLine(
                start = center,
                end = Offset(center.x, center.y + handleLength),
                kropConfig = kropConfig
            )
        }

        KropCorner.BOTTOM_LEFT -> {

            drawKropLine(
                start = center,
                end = Offset(center.x + handleLength, center.y),
                kropConfig = kropConfig
            )

            drawKropLine(
                start = center,
                end = Offset(center.x, center.y - handleLength),
                kropConfig = kropConfig
            )
        }

        KropCorner.BOTTOM_RIGHT -> {

            drawKropLine(
                start = center,
                end = Offset(center.x - handleLength, center.y),
                kropConfig = kropConfig
            )

            drawKropLine(
                start = center,
                end = Offset(center.x, center.y - handleLength),
                kropConfig = kropConfig
            )
        }

        KropCorner.TOP_CENTRE, KropCorner.BOTTOM_CENTRE -> {

            drawKropLine(
                start = Offset(center.x - handleLength / 2, center.y),
                end = Offset(center.x + handleLength / 2, center.y),
                kropConfig = kropConfig
            )
        }

        KropCorner.LEFT_CENTRE, KropCorner.RIGHT_CENTRE -> {

            drawKropLine(
                start = Offset(center.x, center.y - handleLength / 2),
                end = Offset(center.x, center.y + handleLength / 2),
                kropConfig = kropConfig
            )
        }
    }
}

internal fun DrawScope.drawPlus(
    topLeft: Offset,
    rectSize: Size,
    kropConfig: KropConfig
) {

    val centerX = topLeft.x + rectSize.width / 2
    val centerY = topLeft.y + rectSize.height / 2

    drawLine(
        start = Offset(centerX - kropConfig.targetSize / 2, centerY),
        end = Offset(centerX + kropConfig.targetSize / 2, centerY),
        color = kropConfig.targetColor,
        strokeWidth = kropConfig.targetStroke
    )

    drawLine(
        start = Offset(centerX, centerY - kropConfig.targetSize / 2),
        end = Offset(centerX, centerY + kropConfig.targetSize / 2),
        color = kropConfig.targetColor,
        strokeWidth = kropConfig.targetStroke
    )
}

private fun DrawScope.drawKropLine(
    start: Offset,
    end: Offset,
    kropConfig: KropConfig
) {

    drawLine(
        start = start,
        end = end,
        color = kropConfig.handleColor,
        strokeWidth = kropConfig.handleStroke
    )
}

internal fun getCropRectWithAspect(
    canvasSize: IntSize,
    aspectRatio: KropAspectRatio,
    paddingFraction: Float = 0.05F
): Rect {

    val canvasWidth = canvasSize.width.toFloat()
    val canvasHeight = canvasSize.height.toFloat()

    val availableWidth = canvasWidth * (1.0F - 2.0F * paddingFraction)
    val availableHeight = canvasHeight * (1.0F - 2.0F * paddingFraction)

    val desiredRatio = aspectRatio.ratio ?: (canvasWidth / canvasHeight)

    val targetWidth: Float
    val targetHeight: Float

    if (availableWidth / desiredRatio <= availableHeight) {

        targetWidth = availableWidth
        targetHeight = targetWidth / desiredRatio
    } else {

        targetHeight = availableHeight
        targetWidth = targetHeight * desiredRatio
    }

    val centerX = canvasWidth / 2
    val centerY = canvasHeight / 2

    val left = centerX - targetWidth / 2
    val top = centerY - targetHeight / 2
    val right = centerX + targetWidth / 2
    val bottom = centerY + targetHeight / 2

    return Rect(topLeft = Offset(x = left, y = top), bottomRight = Offset(x = right, y = bottom))
}