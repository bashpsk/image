package io.bashpsk.imagekrop.crop

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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

    val aspectRatio by remember(modifiedImageBitmap) {
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
    var selectedAspectRatio by remember { mutableStateOf(KropAspectRatio.Square) }
    var isMovingCropRect by remember { mutableStateOf(false) }
    var isAspectLocked by remember { mutableStateOf(true) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val rectSize by remember {
        derivedStateOf { Size(width = topRight.x - topLeft.x, height = bottomLeft.y - topLeft.y) }
    }

    val threshold by remember(kropConfig) { derivedStateOf { kropConfig.handleThreshold } }
    val cropSizeLimit by remember(kropConfig) { derivedStateOf { kropConfig.minimumCropSize } }
    val overlayColor = remember { Color.Black.copy(alpha = 0.5F) }

    val cropPointerInputModifier = Modifier.pointerInput(Unit) {

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

    LaunchedEffect(canvasSize, selectedAspectRatio) {

        val cropRect = getCropRectWithAspect(canvasSize, selectedAspectRatio)

        topLeft = cropRect.topLeft
        topRight = Offset(cropRect.right, cropRect.top)
        bottomLeft = Offset(cropRect.left, cropRect.bottom)
        bottomRight = cropRect.bottomRight
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
                selectedAspectRatio = selectedAspectRatio,
                onKropAspectRatio = { aspect ->

                    selectedAspectRatio = aspect
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
                snackbarHostState = snackbarHostState
            )
        },
        snackbarHost = { snackbarHostState }
    ) { paddingValues ->

        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
            isRefreshing = isRefreshing,
            onRefresh = {}
        ) {

            KropImagePreview(
                sheetState = imagePreviewSheetState,
                originalImageBitmap = originalImageBitmap,
                modifiedImageBitmap = modifiedImageBitmap
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .consumeWindowInsets(paddingValues = paddingValues),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(ratio = aspectRatio)
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
                        .aspectRatio(ratio = aspectRatio)
                        .onPlaced { layoutCoordinates ->

                            canvasSize = layoutCoordinates.size
                        }
                        .then(cropPointerInputModifier),
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
                        topLeft = Offset(0f, bottomLeft.y),
                        color = overlayColor,
                        size = Size(
                            canvasSize.width.toFloat(),
                            canvasSize.height.toFloat() - bottomLeft.y
                        )
                    )

                    // Left overlay
                    drawRect(
                        topLeft = Offset(0f, topLeft.y),
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
            }
        }
    }
}