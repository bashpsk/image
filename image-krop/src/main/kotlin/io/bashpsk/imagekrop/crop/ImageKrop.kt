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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntSize
import io.bashpsk.imagekrop.R
import io.bashpsk.imagekrop.offset.hasNeared
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ImageKrop(
    modifier: Modifier = Modifier,
    imageBitmap: ImageBitmap?,
    kropConfig: KropConfig = KropConfig(),
    kropShapeList: ImmutableList<KropShape>? = null,
    onImageKropDone: (result: KropResult) -> Unit,
    onNavigateBack: () -> Unit = {}
) {

    val density = LocalDensity.current
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
    var kropShape by remember { mutableStateOf(KropShape.SharpeCorner) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val rectSize by remember {
        derivedStateOf { Size(width = topRight.x - topLeft.x, height = bottomLeft.y - topLeft.y) }
    }

    val threshold by remember(kropConfig, density) {
        derivedStateOf {
            maxOf(kropConfig.handleWidth, kropConfig.handleHeight).toPixel(density = density)
        }
    }

    val cropSizeLimit by remember(kropConfig, density) {
        derivedStateOf { kropConfig.minimumCropSize.toPixel(density = density) }
    }

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

    val pointerInputWithAspect = Modifier.pointerInput(kropAspectRatio, canvasSize, cropSizeLimit) {

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

                isMovingCropRect = kropCorner == null && Rect(topLeft, bottomRight).contains(offset)
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

                when (kropCorner) {

                    KropCorner.TOP_LEFT, KropCorner.TOP_RIGHT, KropCorner.BOTTOM_LEFT,
                    KropCorner.BOTTOM_RIGHT -> {

                        val anchorCorner = when (kropCorner) {

                            KropCorner.TOP_LEFT -> bottomRight
                            KropCorner.TOP_RIGHT -> bottomLeft
                            KropCorner.BOTTOM_LEFT -> topRight
                            KropCorner.BOTTOM_RIGHT -> topLeft
                            else -> return@detectDragGestures
                        }

                        val draggedCornerCurrent = when (kropCorner) {

                            KropCorner.TOP_LEFT -> topLeft
                            KropCorner.TOP_RIGHT -> topRight
                            KropCorner.BOTTOM_LEFT -> bottomLeft
                            KropCorner.BOTTOM_RIGHT -> bottomRight
                            else -> return@detectDragGestures
                        }

                        calculateNewCropRect(
                            draggedCornerCurrent = draggedCornerCurrent,
                            anchorCorner = anchorCorner,
                            dragDelta = dragAmount,
                            cornerType = kropCorner!!,
                            aspectRatio = currentAspectRatio,
                            minSize = cropSizeLimit,
                            canvasWidth = maxX,
                            canvasHeight = maxY
                        )?.let { (newTopLeft, newBottomRight) ->

                            val boundedTopLeftX = newTopLeft.x.coerceIn(
                                minX..maxX - cropSizeLimit
                            )

                            val boundedTopLeftY = newTopLeft.y.coerceIn(
                                minY..maxY - cropSizeLimit
                            )

                            val boundedBottomRightX = newBottomRight.x.coerceIn(
                                minX + cropSizeLimit..maxX
                            )

                            val boundedBottomRightY = newBottomRight.y.coerceIn(
                                minY + cropSizeLimit..maxY
                            )

                            var finalWidth = boundedBottomRightX - boundedTopLeftX
                            var finalHeight = boundedBottomRightY - boundedTopLeftY

                            if (finalWidth / currentAspectRatio > finalHeight) {

                                finalWidth = finalHeight * currentAspectRatio
                            } else {

                                finalHeight = finalWidth / currentAspectRatio
                            }

                            if (finalWidth >= cropSizeLimit && finalHeight >= cropSizeLimit) {

                                when (kropCorner) {

                                    KropCorner.TOP_LEFT -> {

                                        topLeft = Offset(
                                            x = boundedBottomRightX - finalWidth,
                                            y = boundedBottomRightY - finalHeight
                                        )

                                        bottomRight = Offset(
                                            x = boundedBottomRightX,
                                            y = boundedBottomRightY
                                        )
                                    }

                                    KropCorner.TOP_RIGHT -> {

                                        topLeft = Offset(
                                            x = boundedTopLeftX,
                                            y = boundedBottomRightY - finalHeight
                                        )

                                        bottomRight = Offset(
                                            x = boundedTopLeftX + finalWidth,
                                            y = boundedBottomRightY
                                        )
                                    }

                                    KropCorner.BOTTOM_LEFT -> {

                                        topLeft = Offset(
                                            x = boundedBottomRightX - finalWidth,
                                            y = boundedTopLeftY
                                        )

                                        bottomRight = Offset(
                                            x = boundedBottomRightX,
                                            y = boundedTopLeftY + finalHeight
                                        )
                                    }

                                    KropCorner.BOTTOM_RIGHT -> {

                                        topLeft = Offset(x = boundedTopLeftX, y = boundedTopLeftY)

                                        bottomRight = Offset(
                                            x = boundedTopLeftX + finalWidth,
                                            y = boundedTopLeftY + finalHeight
                                        )
                                    }

                                    else -> Unit
                                }

                                topRight = Offset(bottomRight.x, topLeft.y)
                                bottomLeft = Offset(topLeft.x, bottomRight.y)
                            }
                        }
                    }

                    KropCorner.TOP_CENTRE -> {

                        val potentialNewTopY = topLeft.y + dragAmount.y
                        val currentHeight = bottomRight.y - topLeft.y
                        val currentWidth = bottomRight.x - topLeft.x


                        var newHeight = (bottomRight.y - potentialNewTopY).coerceAtLeast(
                            cropSizeLimit
                        )

                        var newWidth = newHeight * currentAspectRatio

                        if (topLeft.x + newWidth > maxX) {

                            newWidth = maxX - topLeft.x
                            newHeight = newWidth / currentAspectRatio
                        }

                        if (newWidth < cropSizeLimit) {

                            newWidth = cropSizeLimit
                            newHeight = newWidth / currentAspectRatio
                        }

                        val newTopY = (bottomRight.y - newHeight).coerceIn(
                            minY..bottomRight.y - cropSizeLimit
                        )

                        val finalHeight = bottomRight.y - newTopY
                        val finalWidth = finalHeight * currentAspectRatio

                        if (finalHeight >= cropSizeLimit && finalWidth >= cropSizeLimit
                            && topLeft.x + finalWidth <= maxX
                        ) {

                            val horizontalShift = (currentWidth - finalWidth) / 2

                            topLeft = Offset(topLeft.x + horizontalShift, newTopY)
                            topRight = Offset(bottomRight.x - horizontalShift, newTopY)
                            bottomLeft = bottomLeft.copy(x = topLeft.x)
                            bottomRight = bottomRight.copy(x = topRight.x)
                        }
                    }

                    KropCorner.BOTTOM_CENTRE -> {

                        val potentialNewBottomY = bottomRight.y + dragAmount.y
                        val currentHeight = bottomRight.y - topLeft.y
                        val currentWidth = bottomRight.x - topLeft.x

                        var newHeight = (potentialNewBottomY - topLeft.y).coerceAtLeast(
                            cropSizeLimit
                        )

                        var newWidth = newHeight * currentAspectRatio

                        if (topLeft.x + newWidth > maxX) {

                            newWidth = maxX - topLeft.x
                            newHeight = newWidth / currentAspectRatio
                        }

                        if (newWidth < cropSizeLimit) {

                            newWidth = cropSizeLimit
                            newHeight = newWidth / currentAspectRatio
                        }

                        val newBottomY = (topLeft.y + newHeight).coerceIn(
                            topLeft.y + cropSizeLimit..maxY
                        )

                        val finalHeight = newBottomY - topLeft.y
                        val finalWidth = finalHeight * currentAspectRatio

                        if (finalHeight >= cropSizeLimit && finalWidth >= cropSizeLimit
                            && topLeft.x + finalWidth <= maxX
                        ) {

                            val horizontalShift = (currentWidth - finalWidth) / 2

                            bottomLeft = Offset(topLeft.x + horizontalShift, newBottomY)
                            bottomRight = Offset(bottomRight.x - horizontalShift, newBottomY)
                            topLeft = topLeft.copy(x = bottomLeft.x)
                            topRight = topRight.copy(x = bottomRight.x)

                        }
                    }

                    KropCorner.LEFT_CENTRE -> {

                        val potentialNewLeftX = topLeft.x + dragAmount.x
                        val currentWidth = bottomRight.x - topLeft.x
                        val currentHeight = bottomRight.y - topLeft.y

                        var newWidth = (bottomRight.x - potentialNewLeftX).coerceAtLeast(
                            cropSizeLimit
                        )

                        var newHeight = newWidth / currentAspectRatio

                        if (topLeft.y + newHeight > maxY) {

                            newHeight = maxY - topLeft.y
                            newWidth = newHeight * currentAspectRatio
                        }

                        if (newHeight < cropSizeLimit) {

                            newHeight = cropSizeLimit
                            newWidth = newHeight * currentAspectRatio
                        }

                        val newLeftX = (bottomRight.x - newWidth).coerceIn(
                            minX..bottomRight.x - cropSizeLimit
                        )

                        val finalWidth = bottomRight.x - newLeftX
                        val finalHeight = finalWidth / currentAspectRatio

                        if (finalWidth >= cropSizeLimit && finalHeight >= cropSizeLimit
                            && topLeft.y + finalHeight <= maxY
                        ) {

                            val verticalShift = (currentHeight - finalHeight) / 2

                            topLeft = Offset(newLeftX, topLeft.y + verticalShift)
                            bottomLeft = Offset(newLeftX, bottomRight.y - verticalShift)
                            topRight = topRight.copy(y = topLeft.y)
                            bottomRight = bottomRight.copy(y = bottomLeft.y)
                        }
                    }

                    KropCorner.RIGHT_CENTRE -> {

                        val potentialNewRightX = bottomRight.x + dragAmount.x
                        val currentWidth = bottomRight.x - topLeft.x
                        val currentHeight = bottomRight.y - topLeft.y

                        var newWidth = (potentialNewRightX - topLeft.x).coerceAtLeast(cropSizeLimit)
                        var newHeight = newWidth / currentAspectRatio

                        if (topLeft.y + newHeight > maxY) {

                            newHeight = maxY - topLeft.y
                            newWidth = newHeight * currentAspectRatio
                        }

                        if (newHeight < cropSizeLimit) {

                            newHeight = cropSizeLimit
                            newWidth = newHeight * currentAspectRatio
                        }

                        val newRightX = (topLeft.x + newWidth).coerceIn(
                            topLeft.x + cropSizeLimit..maxX
                        )

                        val finalWidth = newRightX - topLeft.x
                        val finalHeight = finalWidth / currentAspectRatio

                        if (finalWidth >= cropSizeLimit && finalHeight >= cropSizeLimit
                            && topLeft.y + finalHeight <= maxY
                        ) {

                            val verticalShift = (currentHeight - finalHeight) / 2

                            topRight = Offset(newRightX, topLeft.y + verticalShift)
                            bottomRight = Offset(newRightX, bottomRight.y - verticalShift)
                            topLeft = topLeft.copy(y = topRight.y)
                            bottomLeft = bottomLeft.copy(y = bottomRight.y)
                        }
                    }

                    null -> if (isMovingCropRect) {

                        val rectWidth = bottomRight.x - topLeft.x
                        val rectHeight = bottomRight.y - topLeft.y

                        val newTopLeftX = (topLeft.x + dragAmount.x).coerceIn(
                            minX..maxX - rectWidth
                        )

                        val newTopLeftY = (topLeft.y + dragAmount.y).coerceIn(
                            minY..maxY - rectHeight
                        )

                        topLeft = Offset(newTopLeftX, newTopLeftY)
                        topRight = Offset(newTopLeftX + rectWidth, newTopLeftY)
                        bottomLeft = Offset(newTopLeftX, newTopLeftY + rectHeight)
                        bottomRight = Offset(newTopLeftX + rectWidth, newTopLeftY + rectHeight)
                    }
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

            if (currentSelectedRatio != null) {

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

            if (rectW > canvasWidth) {

                rectW = canvasWidth

                rectH = if (currentSelectedRatio != null) {

                    (rectW / currentSelectedRatio).coerceAtMost(canvasHeight)
                } else {

                    rectH.coerceAtMost(canvasHeight)
                }
            }

            if (rectH > canvasHeight) {

                rectH = canvasHeight

                rectW = if (currentSelectedRatio != null) {

                    (rectH * currentSelectedRatio).coerceAtMost(canvasWidth)
                } else {

                    rectW.coerceAtMost(canvasWidth)
                }
            }

            rectW = rectW.coerceAtLeast(cropSizeLimit)
            rectH = rectH.coerceAtLeast(cropSizeLimit)

            if (isAspectLocked && currentSelectedRatio != null) {

                if (abs(rectW / rectH - currentSelectedRatio) > 0.01f) {

                    rectH = (rectW / currentSelectedRatio).coerceAtLeast(cropSizeLimit)

                    if (rectH > canvasHeight) {

                        rectH = canvasHeight
                        rectW = (rectH * currentSelectedRatio).coerceAtLeast(cropSizeLimit)
                    }
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
                originalImageBitmap = originalImageBitmap,
                modifiedImageBitmap = modifiedImageBitmap,
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
                kropShape = kropShape,
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
                kropShapeList = kropShapeList,
                kropShape = kropShape,
                onKropShape = { shape ->

                    kropShape = shape
                },
                onRefreshing = { isVisible ->

                    isRefreshing = isVisible
                },
                originalImageBitmap = originalImageBitmap,
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

                drawKropOverlay(
                    kropShape = kropShape,
                    topLeft = topLeft,
                    bottomRight = bottomRight,
                    kropConfig = kropConfig
                )

                drawKropShapeBorder(
                    kropShape = kropShape,
                    topLeft = topLeft,
                    bottomRight = bottomRight,
                    kropConfig = kropConfig
                )

                drawKropBorder(
                    topLeft = topLeft,
                    rectSize = rectSize,
                    kropConfig = kropConfig
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