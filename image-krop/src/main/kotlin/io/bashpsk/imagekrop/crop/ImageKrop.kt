package io.bashpsk.imagekrop.crop

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.bashpsk.imagekrop.offset.hasNeared

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ImageKrop(
    modifier: Modifier = Modifier,
    imageBitmap: ImageBitmap,
    onImageKropResult: (result: KropResult) -> Unit
) {

    val newImageBitmap by remember(imageBitmap) { derivedStateOf { imageBitmap } }

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

    var kropCorner by remember { mutableStateOf<KropCorner?>(value = null) }
    var isMovingCropRect by remember { mutableStateOf(value = false) }
    var canvasSize by remember { mutableStateOf(value = IntSize.Zero) }

    val rectSize by remember {
        derivedStateOf { Size(width = topRight.x - topLeft.x, height = bottomLeft.y - topLeft.y) }
    }

    val threshold = 52.0F
    val minCropSize = 400.0F

    val rectPointerInput = Modifier.pointerInput(Unit) {

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
                    topLeft, bottomRight
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

                        if (potentialWidth >= minCropSize && potentialHeight >= minCropSize) {

                            val newX = (topLeft.x + dragAmount.x).coerceIn(
                                minX..bottomRight.x - minCropSize
                            )

                            val newY = (topLeft.y + dragAmount.y).coerceIn(
                                minY..bottomRight.y - minCropSize
                            )

                            topLeft = Offset(newX, newY)
                            topRight = topRight.copy(y = newY)
                            bottomLeft = bottomLeft.copy(x = newX)
                        }
                    }

                    KropCorner.TOP_RIGHT -> {

                        val potentialWidth = (topRight.x + dragAmount.x) - bottomLeft.x
                        val potentialHeight = bottomRight.y - (topRight.y + dragAmount.y)

                        if (potentialWidth >= minCropSize && potentialHeight >= minCropSize) {

                            val newX = (topRight.x + dragAmount.x).coerceIn(
                                topLeft.x + minCropSize..maxX
                            )

                            val newY = (topRight.y + dragAmount.y).coerceIn(
                                minY..bottomLeft.y - minCropSize
                            )

                            topRight = Offset(newX, newY)
                            topLeft = topLeft.copy(y = newY)
                            bottomRight = bottomRight.copy(x = newX)
                        }
                    }

                    KropCorner.BOTTOM_LEFT -> {

                        val potentialWidth = bottomRight.x - (bottomLeft.x + dragAmount.x)
                        val potentialHeight = (bottomLeft.y + dragAmount.y) - topRight.y

                        if (potentialWidth >= minCropSize && potentialHeight >= minCropSize) {

                            val newX = (bottomLeft.x + dragAmount.x).coerceIn(
                                minX..bottomRight.x - minCropSize
                            )

                            val newY = (bottomLeft.y + dragAmount.y).coerceIn(
                                topLeft.y + minCropSize..maxY
                            )

                            bottomLeft = Offset(newX, newY)
                            topLeft = topLeft.copy(x = newX)
                            bottomRight = bottomRight.copy(y = newY)
                        }
                    }

                    KropCorner.BOTTOM_RIGHT -> {

                        val potentialWidth = (bottomRight.x + dragAmount.x) - topLeft.x
                        val potentialHeight = (bottomRight.y + dragAmount.y) - topLeft.y

                        if (potentialWidth >= minCropSize && potentialHeight >= minCropSize) {

                            val newX = (bottomRight.x + dragAmount.x).coerceIn(
                                bottomLeft.x + minCropSize..maxX
                            )

                            val newY = (bottomRight.y + dragAmount.y).coerceIn(
                                topRight.y + minCropSize..maxY
                            )

                            bottomRight = Offset(newX, newY)
                            topRight = topRight.copy(x = newX)
                            bottomLeft = bottomLeft.copy(y = newY)
                        }
                    }

                    KropCorner.TOP_CENTRE -> {

                        val potentialHeight = bottomLeft.y - (topLeft.y + dragAmount.y)

                        if (potentialHeight >= minCropSize) {

                            val newY = (topLeft.y + dragAmount.y).coerceIn(
                                minY..bottomLeft.y - minCropSize
                            )

                            topLeft = topLeft.copy(y = newY)
                            topRight = topRight.copy(y = newY)
                        }
                    }

                    KropCorner.LEFT_CENTRE -> {

                        val potentialWidth = bottomRight.x - (topLeft.x + dragAmount.x)

                        if (potentialWidth >= minCropSize) {

                            val newX = (topLeft.x + dragAmount.x).coerceIn(
                                minX..bottomRight.x - minCropSize
                            )

                            topLeft = topLeft.copy(x = newX)
                            bottomLeft = bottomLeft.copy(x = newX)
                        }
                    }

                    KropCorner.RIGHT_CENTRE -> {

                        val potentialWidth = (topRight.x + dragAmount.x) - topLeft.x

                        if (potentialWidth >= minCropSize) {

                            val newX = (topRight.x + dragAmount.x).coerceIn(
                                topLeft.x + minCropSize..maxX
                            )

                            topRight = topRight.copy(x = newX)
                            bottomRight = bottomRight.copy(x = newX)
                        }
                    }

                    KropCorner.BOTTOM_CENTRE -> {

                        val potentialHeight = (bottomLeft.y + dragAmount.y) - topLeft.y

                        if (potentialHeight >= minCropSize) {

                            val newY = (bottomLeft.y + dragAmount.y).coerceIn(
                                topLeft.y + minCropSize..maxY
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

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        Image(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(newImageBitmap.width.toFloat() / newImageBitmap.height)
                .onPlaced { layoutCoordinates ->

                    val imageWidth = layoutCoordinates.size.width.toFloat()
                    val imageHeight = layoutCoordinates.size.height.toFloat()

                    topLeft = Offset(imageWidth * 0.05F, imageHeight * 0.05F)
                    topRight = Offset(imageWidth * 0.95F, imageHeight * 0.05F)
                    bottomLeft = Offset(imageWidth * 0.05F, imageHeight * 0.95F)
                    bottomRight = Offset(imageWidth * 0.95F, imageHeight * 0.95F)
                },
            bitmap = newImageBitmap,
            contentScale = ContentScale.Fit,
            contentDescription = "Image View"
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(newImageBitmap.width.toFloat() / newImageBitmap.height)
                .onPlaced { layoutCoordinates ->

                    canvasSize = layoutCoordinates.size
                }
                .then(rectPointerInput),
            contentDescription = "Image Crop Gesture"
        ) {

            drawRect(
                topLeft = topLeft,
                size = rectSize,
                style = Stroke(width = 2.0F),
                color = Color.Yellow
            )

            drawPlus(topLeft = topLeft, rectSize = rectSize)

            drawHandle(corner = KropCorner.TOP_LEFT, center = topLeft)
            drawHandle(corner = KropCorner.TOP_RIGHT, center = topRight)
            drawHandle(corner = KropCorner.BOTTOM_LEFT, center = bottomLeft)
            drawHandle(corner = KropCorner.BOTTOM_RIGHT, center = bottomRight)

            drawHandle(corner = KropCorner.TOP_CENTRE, center = topCenter)
            drawHandle(corner = KropCorner.BOTTOM_CENTRE, center = bottomCenter)
            drawHandle(corner = KropCorner.LEFT_CENTRE, center = leftCenter)
            drawHandle(corner = KropCorner.RIGHT_CENTRE, center = rightCenter)
        }

        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 100.dp),
            onClick = {

                val kropResult = imageBitmap.getCroppedImageBitmap(
                    cropRect = Rect(topLeft = topLeft, bottomRight = bottomRight),
                    canvasWidth = canvasSize.width.toFloat(),
                    canvasHeight = canvasSize.height.toFloat()
                )

                onImageKropResult(kropResult)
            }
        ) {

            Text(text = "Crop Image")
        }
    }
}