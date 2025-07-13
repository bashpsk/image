package io.bashpsk.imagekrop.crop

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

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

internal fun DrawScope.drawKropShapePreview(kropShape: KropShape, shapeColor: Color) {

    val shapePath = findKropShapePath(
        kropShape = kropShape,
        width = size.width,
        height = size.height,
        radiusSize = 0.15F
    )

    drawPath(
        path = shapePath,
        color = shapeColor,
        style = Stroke(width = 2.dp.toPx())
    )
}

/**
 * Calculates the new top-left and bottom-right points of a rectangle
 * after a corner drag, maintaining a given aspect ratio.
 *
 * @param draggedCornerCurrent The current position of the corner being dragged.
 * @param anchorCorner The position of the corner opposite to the dragged corner (this corner stays fixed).
 * @param dragDelta The amount by which the draggedCornerCurrent has been moved.
 * @param cornerType The specific corner being dragged (TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT).
 * @param aspectRatio The desired aspect ratio (width / height).
 * @param minSize The minimum allowed size (width or height) for the rectangle.
 * @param canvasWidth The maximum width of the canvas.
 * @param canvasHeight The maximum height of the canvas.
 * @return A Pair of new topLeft and bottomRight Offsets, or null if the drag is invalid.
 */
internal fun calculateNewRect(
    draggedCornerCurrent: Offset,
    anchorCorner: Offset,
    dragDelta: Offset,
    cornerType: KropCorner,
    aspectRatio: Float,
    minSize: Float,
    canvasWidth: Float,
    canvasHeight: Float
): Pair<Offset, Offset>? {

    var newDraggedCorner = draggedCornerCurrent + dragDelta

    // Calculate proposed new width and height based on the drag
    // The sign of width/height depends on which corner is the anchor
    var proposedWidth = when (cornerType) {
        KropCorner.TOP_LEFT, KropCorner.BOTTOM_LEFT -> anchorCorner.x - newDraggedCorner.x
        KropCorner.TOP_RIGHT, KropCorner.BOTTOM_RIGHT -> newDraggedCorner.x - anchorCorner.x
        else -> return null // Should not happen for this function
    }

    var proposedHeight = when (cornerType) {
        KropCorner.TOP_LEFT, KropCorner.TOP_RIGHT -> anchorCorner.y - newDraggedCorner.y
        KropCorner.BOTTOM_LEFT, KropCorner.BOTTOM_RIGHT -> newDraggedCorner.y - anchorCorner.y
        else -> return null // Should not happen for this function
    }

    // Adjust one dimension to maintain aspect ratio
    // Prioritize the dimension that changed more significantly, or based on drag direction
    // For simplicity here, let's assume we adjust height based on width first,
    // then check if width needs adjustment based on height.

    if (proposedWidth < minSize) proposedWidth = minSize
    if (proposedHeight < minSize) proposedHeight = minSize


    // Adjust height based on width and aspect ratio
    var adjustedHeight = proposedWidth / aspectRatio
    var adjustedWidth = proposedWidth

    // If adjusted height makes the rectangle too small or too large,
    // recalculate width based on height.
    if (adjustedHeight < minSize) {
        adjustedHeight = minSize
        adjustedWidth = adjustedHeight * aspectRatio
    }

    // Now, let's ensure the adjusted dimensions don't make the rectangle go out of canvas bounds
    // when placed relative to the anchor. This part is tricky because the new top-left
    // depends on which corner was dragged.

    val newTopLeft: Offset
    val newBottomRight: Offset

    when (cornerType) {
        KropCorner.TOP_LEFT -> {
            // Anchor is bottom-right
            // New top-left is determined by new width and height relative to anchor
            newTopLeft = Offset(anchorCorner.x - adjustedWidth, anchorCorner.y - adjustedHeight)
            newBottomRight = anchorCorner
        }
        KropCorner.TOP_RIGHT -> {
            // Anchor is bottom-left
            newTopLeft = Offset(anchorCorner.x, anchorCorner.y - adjustedHeight)
            newBottomRight = Offset(anchorCorner.x + adjustedWidth, anchorCorner.y)
        }
        KropCorner.BOTTOM_LEFT -> {
            // Anchor is top-right
            newTopLeft = Offset(anchorCorner.x - adjustedWidth, anchorCorner.y)
            newBottomRight = Offset(anchorCorner.x, anchorCorner.y + adjustedHeight)
        }
        KropCorner.BOTTOM_RIGHT -> {
            // Anchor is top-left
            newTopLeft = anchorCorner
            newBottomRight = Offset(anchorCorner.x + adjustedWidth, anchorCorner.y + adjustedHeight)
        }
        else -> return null // Should not reach here
    }

    // Final boundary checks and size clamping
    val finalTopLeftX = newTopLeft.x.coerceIn(0f, canvasWidth - minSize)
    val finalTopLeftY = newTopLeft.y.coerceIn(0f, canvasHeight - minSize)
    var finalBottomRightX = newBottomRight.x.coerceIn(minSize, canvasWidth)
    var finalBottomRightY = newBottomRight.y.coerceIn(minSize, canvasHeight)

    // Ensure minSize after coercion
    if (finalBottomRightX - finalTopLeftX < minSize) {
        if (cornerType == KropCorner.TOP_LEFT || cornerType == KropCorner.BOTTOM_LEFT) {
            // Dragging left edge, anchor is right
            finalBottomRightX = (finalTopLeftX + minSize).coerceAtMost(canvasWidth)
        } else {
            // Dragging right edge, anchor is left
            // This case should be handled by the width adjustment before, but as a safeguard:
            finalBottomRightX = (finalTopLeftX + minSize).coerceAtMost(canvasWidth)
        }
    }
    if (finalBottomRightY - finalTopLeftY < minSize) {
        if (cornerType == KropCorner.TOP_LEFT || cornerType == KropCorner.TOP_RIGHT) {
            // Dragging top edge, anchor is bottom
            finalBottomRightY = (finalTopLeftY + minSize).coerceAtMost(canvasHeight)
        } else {
            // Dragging bottom edge, anchor is top
            finalBottomRightY = (finalTopLeftY + minSize).coerceAtMost(canvasHeight)
        }
    }


    // Re-apply aspect ratio based on the coerced and size-limited rectangle
    var finalWidth = finalBottomRightX - finalTopLeftX
    var finalHeight = finalBottomRightY - finalTopLeftY

    if (finalWidth / aspectRatio > finalHeight) { // Width is too large for aspect ratio
        finalWidth = finalHeight * aspectRatio
    } else { // Height is too large for aspect ratio (or perfectly fine)
        finalHeight = finalWidth / aspectRatio
    }

    // Ensure final dimensions are at least minSize
    finalWidth = finalWidth.coerceAtLeast(minSize)
    finalHeight = finalHeight.coerceAtLeast(minSize)


    // Determine final topLeft and bottomRight based on the corner type and new dimensions
    val resultTopLeft: Offset
    val resultBottomRight: Offset

    when (cornerType) {
        KropCorner.TOP_LEFT -> {
            resultBottomRight = anchorCorner
            resultTopLeft = Offset(anchorCorner.x - finalWidth, anchorCorner.y - finalHeight)
        }
        KropCorner.TOP_RIGHT -> {
            resultBottomRight = Offset(anchorCorner.x + finalWidth, anchorCorner.y)
            resultTopLeft = Offset(anchorCorner.x, anchorCorner.y - finalHeight)
        }
        KropCorner.BOTTOM_LEFT -> {
            resultBottomRight = Offset(anchorCorner.x, anchorCorner.y + finalHeight)
            resultTopLeft = Offset(anchorCorner.x - finalWidth, anchorCorner.y)
        }
        KropCorner.BOTTOM_RIGHT -> {
            resultTopLeft = anchorCorner
            resultBottomRight = Offset(anchorCorner.x + finalWidth, anchorCorner.y + finalHeight)
        }
        else -> return null
    }

    // One last check to ensure the calculated rect is within canvas bounds
    // This is important because aspect ratio adjustments might push it out
    val clampedTopLeftX = resultTopLeft.x.coerceIn(0f, canvasWidth - minSize)
    val clampedTopLeftY = resultTopLeft.y.coerceIn(0f, canvasHeight - minSize)
    val clampedBottomRightX = resultBottomRight.x.coerceIn(clampedTopLeftX + minSize, canvasWidth)
    val clampedBottomRightY = resultBottomRight.y.coerceIn(clampedTopLeftY + minSize, canvasHeight)


    if (clampedBottomRightX - clampedTopLeftX < minSize || clampedBottomRightY - clampedTopLeftY < minSize) {
        // If after all adjustments, it's still too small, it might be an impossible scenario
        // with current constraints, or return null / previous state.
        // For now, let's try to return a valid smallest rect from the anchor if possible.
        // This part can be refined based on desired behavior for edge cases.
        return null // Or handle more gracefully
    }


    return Pair(
        Offset(clampedTopLeftX, clampedTopLeftY),
        Offset(clampedBottomRightX, clampedBottomRightY)
    )
}
