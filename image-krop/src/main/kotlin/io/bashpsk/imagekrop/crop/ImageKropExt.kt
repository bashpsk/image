package io.bashpsk.imagekrop.crop

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

internal fun DrawScope.drawHandle(corner: KropCorner, center: Offset) {

    val handleLength = 48.0F

    when (corner) {

        KropCorner.TOP_LEFT -> {

            drawKropLine(
                start = center,
                end = Offset(center.x + handleLength, center.y)
            )

            drawKropLine(
                start = center,
                end = Offset(center.x, center.y + handleLength)
            )
        }

        KropCorner.TOP_RIGHT -> {

            drawKropLine(
                start = center,
                end = Offset(center.x - handleLength, center.y)
            )

            drawKropLine(
                start = center,
                end = Offset(center.x, center.y + handleLength)
            )
        }

        KropCorner.BOTTOM_LEFT -> {

            drawKropLine(
                start = center,
                end = Offset(center.x + handleLength, center.y)
            )

            drawKropLine(start = center, end = Offset(center.x, center.y - handleLength))
        }

        KropCorner.BOTTOM_RIGHT -> {

            drawKropLine(
                start = center,
                end = Offset(center.x - handleLength, center.y)
            )

            drawKropLine(
                start = center,
                end = Offset(center.x, center.y - handleLength)
            )
        }

        KropCorner.TOP_CENTRE, KropCorner.BOTTOM_CENTRE -> {

            drawKropLine(
                start = Offset(center.x - handleLength / 2, center.y),
                end = Offset(center.x + handleLength / 2, center.y)
            )
        }

        KropCorner.LEFT_CENTRE, KropCorner.RIGHT_CENTRE -> {

            drawKropLine(
                start = Offset(center.x, center.y - handleLength / 2),
                end = Offset(center.x, center.y + handleLength / 2)
            )
        }
    }
}

internal fun DrawScope.drawPlus(topLeft: Offset, rectSize: Size) {

    val centerX = topLeft.x + rectSize.width / 2
    val centerY = topLeft.y + rectSize.height / 2
    val plusSize = 40.0F
    val strokeWidth = 6.0F

    drawLine(
        color = Color.Red,
        start = Offset(centerX - plusSize / 2, centerY),
        end = Offset(centerX + plusSize / 2, centerY),
        strokeWidth = strokeWidth
    )

    drawLine(
        color = Color.Red,
        start = Offset(centerX, centerY - plusSize / 2),
        end = Offset(centerX, centerY + plusSize / 2),
        strokeWidth = strokeWidth
    )
}

private fun DrawScope.drawKropLine(start: Offset, end: Offset) {

    val strokeWidth = 8.0F
    val color = Color.Yellow

    drawLine(start = start, end = end, color = color, strokeWidth = strokeWidth)
}