package io.bashpsk.imagekrop.crop

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntSize
import io.bashpsk.imagekrop.R
import io.bashpsk.imagekrop.offset.hasNeared
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ImageKrop(
    modifier: Modifier = Modifier,
    imageBitmap: ImageBitmap?,
    kropConfig: KropConfig = KropConfig(),
    onImageKropDone: (result: KropResult) -> Unit,
    onNavigateBack: () -> Unit = {}
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarCoroutineScope = rememberCoroutineScope()
    val imagePreviewSheetState = rememberModalBottomSheetState()

    val imageBitmapBroken = ImageBitmap.imageResource(id = R.drawable.image_broken)

    val originalImageBitmap by remember(imageBitmap, imageBitmapBroken) {
        derivedStateOf { imageBitmap ?: imageBitmapBroken }
    }

    var isRefreshing by remember { mutableStateOf(false) }
    var modifiedImageBitmap by remember { mutableStateOf(originalImageBitmap) }

    val modifiedBitmapRatio by remember(modifiedImageBitmap) {
        derivedStateOf { modifiedImageBitmap.width.toFloat() / modifiedImageBitmap.height }
    }

    var topLeft by remember { mutableStateOf(Offset.Zero) }
    var topRight by remember { mutableStateOf(Offset.Zero) }
    var bottomLeft by remember { mutableStateOf(Offset.Zero) }
    var bottomRight by remember { mutableStateOf(Offset.Zero) }

    val topCenter by remember {
        derivedStateOf { Offset((topLeft.x + topRight.x) / 2, topLeft.y) }
    }

    val bottomCenter by remember {
        derivedStateOf { Offset((bottomLeft.x + bottomRight.x) / 2, bottomLeft.y) }
    }

    val leftCenter by remember {
        derivedStateOf { Offset(topLeft.x, (topLeft.y + bottomLeft.y) / 2) }
    }

    val rightCenter by remember {
        derivedStateOf { Offset(topRight.x, (topRight.y + bottomRight.y) / 2) }
    }

    var kropCorner by remember { mutableStateOf<KropCorner?>(null) }
    var kropAspectRatio by remember { mutableStateOf(KropAspectRatio.Square) }
    var isMovingCropRect by remember { mutableStateOf(false) }
    var isAspectLocked by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val rectSize by remember {
        derivedStateOf { Size(width = topRight.x - topLeft.x, height = bottomLeft.y - topLeft.y) }
    }

    val threshold by remember(kropConfig) { derivedStateOf { kropConfig.handleThreshold } }
    val cropSizeLimit by remember(kropConfig) { derivedStateOf { kropConfig.minimumCropSize } }
    val overlayColor = remember { Color.Black.copy(alpha = 0.5F) }

    val pointerInputWithoutAspect = Modifier.pointerInput(Unit) {

        detectDragGestures(
            onDragStart = { offset ->

                kropCorner = when {

                    offset.hasNeared(
                        point = topLeft,
                        threshold = threshold
                    ) -> KropCorner.TOP_LEFT

                    offset.hasNeared(
                        point = topRight,
                        threshold = threshold
                    ) -> KropCorner.TOP_RIGHT

                    offset.hasNeared(
                        point = bottomLeft,
                        threshold = threshold
                    ) -> KropCorner.BOTTOM_LEFT

                    offset.hasNeared(
                        point = bottomRight,
                        threshold = threshold
                    ) -> KropCorner.BOTTOM_RIGHT

                    offset.hasNeared(
                        point = topCenter,
                        threshold = threshold
                    ) -> KropCorner.TOP_CENTRE

                    offset.hasNeared(
                        point = bottomCenter,
                        threshold = threshold
                    ) -> KropCorner.BOTTOM_CENTRE

                    offset.hasNeared(
                        point = leftCenter,
                        threshold = threshold
                    ) -> KropCorner.LEFT_CENTRE

                    offset.hasNeared(
                        point = rightCenter,
                        threshold = threshold
                    ) -> KropCorner.RIGHT_CENTRE

                    else -> null
                }

                isMovingCropRect = kropCorner == null && Rect(
                    topLeft = topLeft,
                    bottomRight = bottomRight
                ).contains(offset)
            },
            onDragEnd = {

                kropCorner = null
                isMovingCropRect = false
            },
            onDrag = { change, dragAmount ->

                change.consume()

                val minX = 0F
                val minY = 0F
                val maxX = canvasSize.width.toFloat()
                val maxY = canvasSize.height.toFloat()

                when (kropCorner) {

                    KropCorner.TOP_LEFT -> {

                        val potentialWidth = bottomRight.x - (topLeft.x + dragAmount.x)
                        val potentialHeight = bottomRight.y - (topLeft.y + dragAmount.y)

                        if (potentialWidth >= cropSizeLimit && potentialHeight >= cropSizeLimit) {

                            val newX = (topLeft.x + dragAmount.x).coerceIn(
                                minX..bottomRight.x - cropSizeLimit
                            )

                            val newY = (topLeft.y + dragAmount.y).coerceIn(
                                minY..bottomRight.y - cropSizeLimit
                            )

                            topLeft = Offset(newX, newY)
                            topRight = topRight.copy(y = newY)
                            bottomLeft = bottomLeft.copy(x = newX)
                        }
                    }

                    KropCorner.TOP_RIGHT -> {

                        val potentialWidth = (topRight.x + dragAmount.x) - bottomLeft.x
                        val potentialHeight = bottomRight.y - (topRight.y + dragAmount.y)

                        if (potentialWidth >= cropSizeLimit && potentialHeight >= cropSizeLimit) {

                            val newX = (topRight.x + dragAmount.x).coerceIn(
                                topLeft.x + cropSizeLimit..maxX
                            )

                            val newY = (topRight.y + dragAmount.y).coerceIn(
                                minY..bottomLeft.y - cropSizeLimit
                            )

                            topRight = Offset(newX, newY)
                            topLeft = topLeft.copy(y = newY)
                            bottomRight = bottomRight.copy(x = newX)
                        }
                    }

                    KropCorner.BOTTOM_LEFT -> {

                        val potentialWidth = bottomRight.x - (bottomLeft.x + dragAmount.x)
                        val potentialHeight = (bottomLeft.y + dragAmount.y) - topRight.y

                        if (potentialWidth >= cropSizeLimit && potentialHeight >= cropSizeLimit) {

                            val newX = (bottomLeft.x + dragAmount.x).coerceIn(
                                minX..bottomRight.x - cropSizeLimit
                            )

                            val newY = (bottomLeft.y + dragAmount.y).coerceIn(
                                topLeft.y + cropSizeLimit..maxY
                            )

                            bottomLeft = Offset(newX, newY)
                            topLeft = topLeft.copy(x = newX)
                            bottomRight = bottomRight.copy(y = newY)
                        }
                    }

                    KropCorner.BOTTOM_RIGHT -> {

                        val potentialWidth = (bottomRight.x + dragAmount.x) - topLeft.x
                        val potentialHeight = (bottomRight.y + dragAmount.y) - topLeft.y

                        if (potentialWidth >= cropSizeLimit && potentialHeight >= cropSizeLimit) {

                            val newX = (bottomRight.x + dragAmount.x).coerceIn(
                                bottomLeft.x + cropSizeLimit..maxX
                            )

                            val newY = (bottomRight.y + dragAmount.y).coerceIn(
                                topRight.y + cropSizeLimit..maxY
                            )

                            bottomRight = Offset(newX, newY)
                            topRight = topRight.copy(x = newX)
                            bottomLeft = bottomLeft.copy(y = newY)
                        }
                    }

                    KropCorner.TOP_CENTRE -> {

                        val potentialHeight = bottomLeft.y - (topLeft.y + dragAmount.y)

                        if (potentialHeight >= cropSizeLimit) {

                            val newY = (topLeft.y + dragAmount.y).coerceIn(
                                minY..bottomLeft.y - cropSizeLimit
                            )

                            topLeft = topLeft.copy(y = newY)
                            topRight = topRight.copy(y = newY)
                        }
                    }

                    KropCorner.LEFT_CENTRE -> {

                        val potentialWidth = bottomRight.x - (topLeft.x + dragAmount.x)

                        if (potentialWidth >= cropSizeLimit) {

                            val newX = (topLeft.x + dragAmount.x).coerceIn(
                                minX..bottomRight.x - cropSizeLimit
                            )

                            topLeft = topLeft.copy(x = newX)
                            bottomLeft = bottomLeft.copy(x = newX)
                        }
                    }

                    KropCorner.RIGHT_CENTRE -> {

                        val potentialWidth = (topRight.x + dragAmount.x) - topLeft.x

                        if (potentialWidth >= cropSizeLimit) {

                            val newX = (topRight.x + dragAmount.x).coerceIn(
                                topLeft.x + cropSizeLimit..maxX
                            )

                            topRight = topRight.copy(x = newX)
                            bottomRight = bottomRight.copy(x = newX)
                        }
                    }

                    KropCorner.BOTTOM_CENTRE -> {

                        val potentialHeight = (bottomLeft.y + dragAmount.y) - topLeft.y

                        if (potentialHeight >= cropSizeLimit) {

                            val newY = (bottomLeft.y + dragAmount.y).coerceIn(
                                topLeft.y + cropSizeLimit..maxY
                            )

                            bottomLeft = bottomLeft.copy(y = newY)
                            bottomRight = bottomRight.copy(y = newY)
                        }

                    }

                    null -> if (isMovingCropRect) {

                        val newTopLeftX = (topLeft.x + dragAmount.x).coerceIn(
                            minX..maxX - rectSize.width
                        )

                        val newTopLeftY = (topLeft.y + dragAmount.y).coerceIn(
                            minY..maxY - rectSize.height
                        )

                        val newTopRightX = newTopLeftX + rectSize.width
                        val newBottomLeftY = newTopLeftY + rectSize.height

                        topLeft = Offset(newTopLeftX, newTopLeftY)
                        topRight = Offset(newTopRightX, newTopLeftY)
                        bottomLeft = Offset(newTopLeftX, newBottomLeftY)
                        bottomRight = Offset(newTopRightX, newBottomLeftY)
                    }
                }
            }
        )
    }

    val pointerInputWithAspect =
        Modifier.pointerInput(kropAspectRatio, canvasSize, cropSizeLimit) {
            detectDragGestures(
                onDragStart = { offset ->

                    kropCorner = when {

                        offset.hasNeared(topLeft, threshold) -> KropCorner.TOP_LEFT
                        offset.hasNeared(topRight, threshold) -> KropCorner.TOP_RIGHT
                        offset.hasNeared(bottomLeft, threshold) -> KropCorner.BOTTOM_LEFT
                        offset.hasNeared(bottomRight, threshold) -> KropCorner.BOTTOM_RIGHT
                        offset.hasNeared(topCenter, threshold) -> KropCorner.TOP_CENTRE
                        offset.hasNeared(bottomCenter, threshold) -> KropCorner.BOTTOM_CENTRE
                        offset.hasNeared(leftCenter, threshold) -> KropCorner.LEFT_CENTRE
                        offset.hasNeared(rightCenter, threshold) -> KropCorner.RIGHT_CENTRE
                        else -> null
                    }

                    isMovingCropRect =
                        kropCorner == null && Rect(topLeft, bottomRight).contains(offset)
                },
                onDragEnd = {

                    kropCorner = null
                    isMovingCropRect = false
                },
                onDrag = { change, dragAmount ->

                    change.consume()

                    if (canvasSize == IntSize.Zero) return@detectDragGestures

                    val currentAspectRatio = kropAspectRatio.ratio ?: return@detectDragGestures

                    val minX = 0.0F
                    val minY = 0.0F
                    val maxX = canvasSize.width.toFloat()
                    val maxY = canvasSize.height.toFloat()

                    fun calculateNewRect(
                        draggedCornerCurrent: Offset,
                        anchorCorner: Offset,
                        dragDelta: Offset,
                        cornerType: KropCorner
                    ): Pair<Offset, Offset>? {

                        val proposedDraggedX = draggedCornerCurrent.x + dragDelta.x
                        val proposedDraggedY = draggedCornerCurrent.y + dragDelta.y

                        var newWidth: Float
                        var newHeight: Float

                        when (cornerType) {

                            KropCorner.TOP_LEFT, KropCorner.BOTTOM_RIGHT -> {

                                newWidth = abs(anchorCorner.x - proposedDraggedX)
                                newHeight = abs(anchorCorner.y - proposedDraggedY)
                            }

                            KropCorner.TOP_RIGHT, KropCorner.BOTTOM_LEFT -> {

                                newWidth = abs(anchorCorner.x - proposedDraggedX)
                                newHeight = abs(anchorCorner.y - proposedDraggedY)
                            }

                            else -> return null
                        }

                        if (abs(dragDelta.x) > abs(dragDelta.y)) {

                            newHeight = newWidth / currentAspectRatio
                        } else {

                            newWidth = newHeight * currentAspectRatio
                        }

                        if (newWidth < cropSizeLimit) {

                            newWidth = cropSizeLimit
                            newHeight = newWidth / currentAspectRatio
                        }

                        if (newHeight < cropSizeLimit) {

                            newHeight = cropSizeLimit
                            newWidth = newHeight * currentAspectRatio
                        }

                        if (newWidth < cropSizeLimit) {

                            newWidth = cropSizeLimit
                            newHeight = newWidth / currentAspectRatio
                        }

                        if (newHeight < cropSizeLimit) {

                            newHeight = cropSizeLimit
                            newWidth = newHeight * currentAspectRatio
                        }

                        var resTopLeft: Offset
                        var resBottomRight: Offset

                        when (cornerType) {

                            KropCorner.TOP_LEFT -> {

                                resTopLeft = Offset(
                                    x = anchorCorner.x - newWidth,
                                    y = anchorCorner.y - newHeight
                                )

                                resBottomRight = anchorCorner
                            }

                            KropCorner.TOP_RIGHT -> {

                                resTopLeft = Offset(anchorCorner.x, anchorCorner.y - newHeight)
                                resBottomRight = Offset(anchorCorner.x + newWidth, anchorCorner.y)
                            }

                            KropCorner.BOTTOM_LEFT -> {

                                resTopLeft = Offset(anchorCorner.x - newWidth, anchorCorner.y)
                                resBottomRight = Offset(anchorCorner.x, anchorCorner.y + newHeight)
                            }

                            KropCorner.BOTTOM_RIGHT -> {

                                resTopLeft = anchorCorner

                                resBottomRight = Offset(
                                    x = anchorCorner.x + newWidth,
                                    y = anchorCorner.y + newHeight
                                )
                            }

                            else -> return null
                        }

                        newWidth = newWidth.coerceAtMost(maxX - minX)
                        newHeight = newHeight.coerceAtMost(maxY - minY)

                        val aspectH = newWidth / currentAspectRatio

                        if (aspectH < newHeight) {

                            newHeight = aspectH
                        } else {

                            newWidth = newHeight * currentAspectRatio
                        }

                        if (newWidth < cropSizeLimit || newHeight < cropSizeLimit) {

                            if (
                                (cornerType == KropCorner.TOP_LEFT
                                        && (dragDelta.x > 0 || dragDelta.y > 0)) ||
                                (cornerType == KropCorner.TOP_RIGHT
                                        && (dragDelta.x < 0 || dragDelta.y > 0)) ||
                                (cornerType == KropCorner.BOTTOM_LEFT
                                        && (dragDelta.x > 0 || dragDelta.y < 0)) ||
                                (cornerType == KropCorner.BOTTOM_RIGHT
                                        && (dragDelta.x < 0 || dragDelta.y < 0))
                            ) {

                                return null
                            }

                            if (newWidth < cropSizeLimit) {

                                newWidth = cropSizeLimit
                                newHeight = newWidth / currentAspectRatio
                            }

                            if (newHeight < cropSizeLimit) {

                                newHeight = cropSizeLimit
                                newWidth = newHeight * currentAspectRatio
                            }
                        }

                        when (cornerType) {

                            KropCorner.TOP_LEFT -> {

                                resBottomRight = anchorCorner

                                resTopLeft = Offset(
                                    x = resBottomRight.x - newWidth,
                                    y = resBottomRight.y - newHeight
                                )
                            }

                            KropCorner.TOP_RIGHT -> {

                                resTopLeft = Offset(anchorCorner.x, anchorCorner.y - newHeight)
                                resBottomRight = Offset(anchorCorner.x + newWidth, anchorCorner.y)
                            }

                            KropCorner.BOTTOM_LEFT -> {

                                resTopLeft = Offset(anchorCorner.x - newWidth, anchorCorner.y)
                                resBottomRight = Offset(anchorCorner.x, anchorCorner.y + newHeight)
                            }

                            KropCorner.BOTTOM_RIGHT -> {

                                resTopLeft = anchorCorner

                                resBottomRight = Offset(
                                    x = resTopLeft.x + newWidth,
                                    y = resTopLeft.y + newHeight
                                )
                            }

                            else -> return null
                        }

                        var finalTopLeftX = resTopLeft.x
                        var finalTopLeftY = resTopLeft.y

                        if (finalTopLeftX < minX) finalTopLeftX = minX
                        if (finalTopLeftY < minY) finalTopLeftY = minY

                        if (finalTopLeftX + newWidth > maxX) finalTopLeftX = maxX - newWidth
                        if (finalTopLeftY + newHeight > maxY) finalTopLeftY = maxY - newHeight

                        if (newWidth <= 0 || newHeight <= 0) return null

                        resTopLeft = Offset(finalTopLeftX, finalTopLeftY)
                        resBottomRight = Offset(finalTopLeftX + newWidth, finalTopLeftY + newHeight)

                        if (
                            resTopLeft.x.isNaN() || resTopLeft.y.isNaN() || resBottomRight.x.isNaN()
                            || resBottomRight.y.isNaN() || resTopLeft.x.isInfinite()
                            || resTopLeft.y.isInfinite() || resBottomRight.x.isInfinite()
                            || resBottomRight.y.isInfinite()
                        ) {

                            return null
                        }

                        if (resTopLeft.x > resBottomRight.x || resTopLeft.y > resBottomRight.y) {

                            return null;
                        }

                        return Pair(resTopLeft, resBottomRight)
                    }

                    var nTopLeft: Offset = topLeft
                    val nTopRight: Offset = topRight
                    val nBottomLeft: Offset = bottomLeft
                    var nBottomRight: Offset = bottomRight

                    when (kropCorner) {

                        KropCorner.TOP_LEFT -> calculateNewRect(
                            draggedCornerCurrent = topLeft,
                            anchorCorner = bottomRight,
                            dragDelta = dragAmount,
                            cornerType = KropCorner.TOP_LEFT
                        )

                        KropCorner.TOP_RIGHT -> calculateNewRect(
                            draggedCornerCurrent = topRight,
                            anchorCorner = bottomLeft,
                            dragDelta = dragAmount,
                            cornerType = KropCorner.TOP_RIGHT
                        )

                        KropCorner.BOTTOM_LEFT -> calculateNewRect(
                            draggedCornerCurrent = bottomLeft,
                            anchorCorner = topRight,
                            dragDelta = dragAmount,
                            cornerType = KropCorner.BOTTOM_LEFT
                        )

                        KropCorner.BOTTOM_RIGHT -> calculateNewRect(
                            draggedCornerCurrent = bottomRight,
                            anchorCorner = topLeft,
                            dragDelta = dragAmount,
                            cornerType = KropCorner.BOTTOM_RIGHT
                        )

                        KropCorner.TOP_CENTRE -> {

                            val originalHeight = bottomRight.y - topLeft.y
                            val originalWidth = bottomRight.x - topLeft.x

                            var newTop = (topLeft.y + dragAmount.y).coerceIn(
                                minY..bottomRight.y - cropSizeLimit
                            )

                            var newHeight = (bottomRight.y - newTop).coerceAtLeast(cropSizeLimit)

                            var newWidth = (newHeight * currentAspectRatio).coerceAtLeast(
                                cropSizeLimit
                            )

                            if (newWidth > maxX - minX) {

                                newWidth = maxX - minX

                                newHeight = (newWidth / currentAspectRatio).coerceAtLeast(
                                    cropSizeLimit
                                )

                                newTop = (bottomRight.y - newHeight).coerceIn(
                                    minY..maxY - newHeight
                                )
                            }

                            if (newHeight < cropSizeLimit) {

                                newHeight = cropSizeLimit

                                newWidth = (newHeight * currentAspectRatio).coerceAtLeast(
                                    cropSizeLimit
                                )
                            }

                            val currentCenterX = topLeft.x + originalWidth / 2

                            val newLeft = (currentCenterX - newWidth / 2).coerceIn(
                                minimumValue = minX,
                                maximumValue = maxX - newWidth
                            )

                            nTopLeft = Offset(newLeft, newTop)
                            nBottomRight = Offset(newLeft + newWidth, newTop + newHeight)
                            Pair(nTopLeft, nBottomRight)
                        }

                        KropCorner.BOTTOM_CENTRE -> {

                            val originalHeight = bottomRight.y - topLeft.y
                            val originalWidth = bottomRight.x - topLeft.x

                            var newBottom = (bottomRight.y + dragAmount.y).coerceIn(
                                range = topLeft.y + cropSizeLimit..maxY
                            )

                            var newHeight = (newBottom - topLeft.y).coerceAtLeast(cropSizeLimit)

                            var newWidth = (newHeight * currentAspectRatio).coerceAtLeast(
                                cropSizeLimit
                            )

                            if (newWidth > maxX - minX) {

                                newWidth = maxX - minX

                                newHeight = (newWidth / currentAspectRatio).coerceAtLeast(
                                    cropSizeLimit
                                )

                                newBottom = (topLeft.y + newHeight).coerceIn(minY + newHeight, maxY)
                            }

                            if (newHeight < cropSizeLimit) {

                                newHeight = cropSizeLimit

                                newWidth = (newHeight * currentAspectRatio).coerceAtLeast(
                                    cropSizeLimit
                                )
                            }

                            val currentCenterX = topLeft.x + originalWidth / 2

                            val newLeft = (currentCenterX - newWidth / 2).coerceIn(
                                minX..maxX - newWidth
                            )

                            nTopLeft = Offset(newLeft, newBottom - newHeight)
                            nBottomRight = Offset(newLeft + newWidth, newBottom)
                            Pair(nTopLeft, nBottomRight)
                        }

                        KropCorner.LEFT_CENTRE -> {

                            val originalHeight = bottomRight.y - topLeft.y
                            val originalWidth = bottomRight.x - topLeft.x

                            var newLeft = (topLeft.x + dragAmount.x).coerceIn(
                                minX..bottomRight.x - cropSizeLimit
                            )

                            var newWidth = (bottomRight.x - newLeft).coerceAtLeast(cropSizeLimit)

                            var newHeight = (newWidth / currentAspectRatio).coerceAtLeast(
                                cropSizeLimit
                            )

                            if (newHeight > maxY - minY) {

                                newHeight = maxY - minY

                                newWidth = (newHeight * currentAspectRatio).coerceAtLeast(
                                    cropSizeLimit
                                )

                                newLeft = (bottomRight.x - newWidth).coerceIn(minX, maxX - newWidth)
                            }

                            if (newWidth < cropSizeLimit) {

                                newWidth = cropSizeLimit

                                newHeight = (newWidth / currentAspectRatio).coerceAtLeast(
                                    cropSizeLimit
                                )
                            }

                            val currentCenterY = topLeft.y + originalHeight / 2

                            val newTop = (currentCenterY - newHeight / 2).coerceIn(
                                minY..maxY - newHeight
                            )

                            nTopLeft = Offset(newLeft, newTop)
                            nBottomRight = Offset(newLeft + newWidth, newTop + newHeight)
                            Pair(nTopLeft, nBottomRight)
                        }

                        KropCorner.RIGHT_CENTRE -> {

                            val originalHeight = bottomRight.y - topLeft.y

                            var newRight = (bottomRight.x + dragAmount.x).coerceIn(
                                topLeft.x + cropSizeLimit..maxX
                            )

                            var newWidth = (newRight - topLeft.x).coerceAtLeast(cropSizeLimit)
                            var newHeight =
                                (newWidth / currentAspectRatio).coerceAtLeast(cropSizeLimit)

                            if (newHeight > maxY - minY) {

                                newHeight = maxY - minY

                                newWidth = (newHeight * currentAspectRatio).coerceAtLeast(
                                    cropSizeLimit
                                )

                                newRight = (topLeft.x + newWidth).coerceIn(minX + newWidth, maxX)
                            }

                            if (newWidth < cropSizeLimit) {

                                newWidth = cropSizeLimit

                                newHeight = (newWidth / currentAspectRatio).coerceAtLeast(
                                    cropSizeLimit
                                )
                            }

                            val currentCenterY = topLeft.y + originalHeight / 2

                            val newTop = (currentCenterY - newHeight / 2).coerceIn(
                                minY..maxY - newHeight
                            )

                            nTopLeft = Offset(newRight - newWidth, newTop)
                            nBottomRight = Offset(newRight, newTop + newHeight)
                            Pair(nTopLeft, nBottomRight)
                        }

                        null -> if (isMovingCropRect) {

                            val currentRectWidth = bottomRight.x - topLeft.x
                            val currentRectHeight = bottomLeft.y - topLeft.y

                            val proposedTopLeftX = (topLeft.x + dragAmount.x)
                            val proposedTopLeftY = (topLeft.y + dragAmount.y)

                            nTopLeft = Offset(
                                x = proposedTopLeftX.coerceIn(minX..maxX - currentRectWidth),
                                y = proposedTopLeftY.coerceIn(minY..maxY - currentRectHeight)
                            )

                            nBottomRight = Offset(
                                x = nTopLeft.x + currentRectWidth,
                                y = nTopLeft.y + currentRectHeight
                            )

                            Pair(nTopLeft, nBottomRight)
                        } else null
                    }?.let { (newCalculatedTopLeft, newCalculatedBottomRight) ->

                        topLeft = newCalculatedTopLeft
                        bottomRight = newCalculatedBottomRight
                        topRight = topRight.copy(x = bottomRight.x, y = topLeft.y)
                        bottomLeft = bottomLeft.copy(x = topLeft.x, y = bottomRight.y)
                    }
                }
            )
        }

    val pointerInputModifier = when (isAspectLocked) {

        true -> pointerInputWithAspect
        false -> pointerInputWithoutAspect
    }

    LaunchedEffect(canvasSize, originalImageBitmap, kropAspectRatio, isAspectLocked) {

        if (
            canvasSize != IntSize.Zero && originalImageBitmap.width > 0
            && originalImageBitmap.height > 0
        ) {

            val canvasWidth = canvasSize.width.toFloat()
            val canvasHeight = canvasSize.height.toFloat()

            var rectW: Float
            var rectH: Float
            val currentSelectedRatio = kropAspectRatio.ratio

            if (isAspectLocked && currentSelectedRatio != null) {

                if (canvasWidth / canvasHeight > currentSelectedRatio) {

                    rectH = canvasHeight * 0.8F
                    rectW = rectH * currentSelectedRatio
                } else {

                    rectW = canvasWidth * 0.8F
                    rectH = rectW / currentSelectedRatio
                }
            } else {

                rectW = canvasWidth.coerceAtMost(canvasHeight) * 0.8F
                rectH = rectW
            }

            rectW = rectW.coerceAtLeast(cropSizeLimit)
            rectH = rectH.coerceAtLeast(cropSizeLimit)

            if (isAspectLocked && currentSelectedRatio != null) {

                val checkH = rectW / currentSelectedRatio

                if (abs(checkH - rectH) > 1.0F) {

                    rectH = checkH

                    if (rectH < cropSizeLimit) {

                        rectH = cropSizeLimit
                        rectW = rectH * currentSelectedRatio
                    }
                }

                if (rectW > canvasWidth) {

                    rectW = canvasWidth
                    rectH = rectW / currentSelectedRatio
                }

                if (rectH > canvasHeight) {

                    rectH = canvasHeight
                    rectW = rectH * currentSelectedRatio
                }
            }


            val initialTopLeftX = (canvasWidth - rectW) / 2
            val initialTopLeftY = (canvasHeight - rectH) / 2

            topLeft = Offset(initialTopLeftX, initialTopLeftY)
            topRight = Offset(initialTopLeftX + rectW, initialTopLeftY)
            bottomLeft = Offset(initialTopLeftX, initialTopLeftY + rectH)
            bottomRight = Offset(initialTopLeftX + rectW, initialTopLeftY + rectH)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {

            ImageKropTopBar(
                modifier = Modifier.fillMaxWidth(),
                onRefreshing = { isVisible ->

                    isRefreshing = isVisible
                },
                imageBitmap = imageBitmap,
                onModifiedImage = { result ->

                    modifiedImageBitmap = result
                },
                onImageKropDone = onImageKropDone,
                canvasSize = canvasSize,
                topLeft = topLeft,
                bottomRight = bottomRight,
                onUndoImageBitmap = {

                    isRefreshing = true
                    modifiedImageBitmap = originalImageBitmap
                    isRefreshing = false
                },
                imagePreviewSheetState = imagePreviewSheetState,
                snackbarCoroutineScope = snackbarCoroutineScope,
                snackbarHostState = snackbarHostState,
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {

            ImageKropBottomBar(
                modifier = Modifier.fillMaxWidth(),
                kropAspectRatio = kropAspectRatio,
                onKropAspectRatio = { aspect ->

                    kropAspectRatio = aspect
                },
                onRefreshing = { isVisible ->

                    isRefreshing = isVisible
                },
                imageBitmap = imageBitmap,
                onModifiedImage = { result ->

                    modifiedImageBitmap = result
                },
                onImageKropDone = onImageKropDone,
                canvasSize = canvasSize,
                topLeft = topLeft,
                bottomRight = bottomRight,
                onUndoImageBitmap = {

                    isRefreshing = true
                    modifiedImageBitmap = originalImageBitmap
                    isRefreshing = false
                },
                snackbarCoroutineScope = snackbarCoroutineScope,
                snackbarHostState = snackbarHostState,
                isAspectLocked = isAspectLocked,
                onAspectLocked = { isLocked ->

                    isAspectLocked = isLocked
                }
            )
        },
        snackbarHost = { snackbarHostState }
    ) { paddingValues ->

        KropImagePreview(
            sheetState = imagePreviewSheetState,
            originalImageBitmap = originalImageBitmap,
            modifiedImageBitmap = modifiedImageBitmap
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues = paddingValues),
            contentAlignment = Alignment.Center
        ) {

            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = modifiedBitmapRatio)
                    .onPlaced { layoutCoordinates ->

                        val imageWidth = layoutCoordinates.size.width.toFloat()
                        val imageHeight = layoutCoordinates.size.height.toFloat()

                        topLeft = Offset(imageWidth * 0.05F, imageHeight * 0.05F)
                        topRight = Offset(imageWidth * 0.95F, imageHeight * 0.05F)
                        bottomLeft = Offset(imageWidth * 0.05F, imageHeight * 0.95F)
                        bottomRight = Offset(imageWidth * 0.95F, imageHeight * 0.95F)
                    },
                bitmap = modifiedImageBitmap,
                contentScale = ContentScale.Fit,
                contentDescription = "Image View"
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = modifiedBitmapRatio)
                    .onPlaced { layoutCoordinates -> canvasSize = layoutCoordinates.size }
                    .then(pointerInputModifier),
                contentDescription = "Image Crop Gesture"
            ) {

                // Top overlay
                drawRect(
                    topLeft = Offset.Zero,
                    color = overlayColor,
                    size = Size(canvasSize.width.toFloat(), topLeft.y)
                )

                // Bottom overlay
                drawRect(
                    topLeft = Offset(0.0F, bottomLeft.y),
                    color = overlayColor,
                    size = Size(
                        canvasSize.width.toFloat(),
                        canvasSize.height.toFloat() - bottomLeft.y
                    )
                )

                // Left overlay
                drawRect(
                    topLeft = Offset(0.0F, topLeft.y),
                    color = overlayColor,
                    size = Size(topLeft.x, rectSize.height)
                )

                // Right overlay
                drawRect(
                    topLeft = Offset(topRight.x, topRight.y),
                    color = overlayColor,
                    size = Size(canvasSize.width.toFloat() - topRight.x, rectSize.height)
                )

                // Border
                drawRect(
                    topLeft = topLeft,
                    size = rectSize,
                    style = Stroke(width = 2.0F),
                    color = Color.Yellow
                )

                drawPlus(
                    topLeft = topLeft,
                    rectSize = rectSize,
                    kropConfig = kropConfig
                )

                drawHandle(
                    corner = KropCorner.TOP_LEFT,
                    center = topLeft,
                    kropConfig = kropConfig
                )

                drawHandle(
                    corner = KropCorner.TOP_RIGHT,
                    center = topRight,
                    kropConfig = kropConfig
                )

                drawHandle(
                    corner = KropCorner.BOTTOM_LEFT,
                    center = bottomLeft,
                    kropConfig = kropConfig
                )

                drawHandle(
                    corner = KropCorner.BOTTOM_RIGHT,
                    center = bottomRight,
                    kropConfig = kropConfig
                )

                drawHandle(
                    corner = KropCorner.TOP_CENTRE,
                    center = topCenter,
                    kropConfig = kropConfig
                )

                drawHandle(
                    corner = KropCorner.BOTTOM_CENTRE,
                    center = bottomCenter,
                    kropConfig = kropConfig
                )

                drawHandle(
                    corner = KropCorner.LEFT_CENTRE,
                    center = leftCenter,
                    kropConfig = kropConfig
                )

                drawHandle(
                    corner = KropCorner.RIGHT_CENTRE,
                    center = rightCenter,
                    kropConfig = kropConfig
                )
            }

            if (isRefreshing) CircularProgressIndicator()
        }
    }
}