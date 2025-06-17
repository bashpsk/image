package io.bashpsk.imagekrop.view

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage
import io.bashpsk.imagekrop.offset.OffsetData
import io.bashpsk.imagekrop.offset.toOffset
import io.bashpsk.imagekrop.offset.toOffsetData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TransformImageView(
    modifier: Modifier = Modifier,
    imageModel: () -> Any?,
    zoomRange: ClosedFloatingPointRange<Float> = 0.4F..8.0F,
    transformData: () -> ImageTransformData = { ImageTransformData() },
    onTransformDataChange: (transform: ImageTransformData) -> Unit = {},
    onLeftSwipe: () -> Unit = {},
    onRightSwipe: () -> Unit = {},
    onClick: (offset: Offset) -> Unit = {},
    onLongClick: (offset: Offset) -> Unit = {},
    transformConfig: () -> TransformImageConfig = { TransformImageConfig() },
    contentScale: ContentScale = ContentScale.Fit,
) {

    val gestureCoroutineScope = rememberCoroutineScope()

    var imageGesture by remember { mutableStateOf(value = TransformImageGesture.INIT) }
    var dragSwipeMinimum by remember { mutableFloatStateOf(value = 0.0F) }
    var touchCount by remember { mutableIntStateOf(value = 0) }
    val isOneTouch by remember { derivedStateOf { touchCount == 1 } }
    val isTwoTouch by remember { derivedStateOf { touchCount == 2 } }
    val isZoomed by remember(transformData()) { derivedStateOf { transformData().zoom != 1.0F } }

    val isCanSwipe by remember(transformConfig()) {
        derivedStateOf { transformData().zoom == 1.0F && transformConfig().isSwipeEnabled }
    }

    val transformableState = rememberTransformableState { zoomChange, panChange, rotationChange ->

        when {

            isTwoTouch -> when (imageGesture) {

                TransformImageGesture.INIT -> when {

                    abs(rotationChange) >= 1.0F -> if (transformConfig().isRotationEnabled) {

                        imageGesture = TransformImageGesture.ROTATION
                    }

                    zoomChange >= 0.10F -> if (transformConfig().isZoomEnabled) {

                        imageGesture = TransformImageGesture.ZOOM
                    }
                }

                TransformImageGesture.ZOOM -> {

                    val newZoom = when (transformData().zoom * zoomChange) {

                        in 0.0F..zoomRange.start -> zoomRange.start
                        in zoomRange.start..zoomRange.endInclusive -> {
                            transformData().zoom * zoomChange
                        }

                        else -> zoomRange.endInclusive
                    }

                    val newPan = (transformData().position.toOffset + panChange).toOffsetData

                    val newTransformData = transformData().copy(
                        zoom = newZoom,
                        position = newPan
                    )

                    onTransformDataChange(newTransformData)
                    gestureCoroutineScope.coroutineContext.cancelChildren()

                    gestureCoroutineScope.launch(context = Dispatchers.Default) {

                        delay(duration = 300.milliseconds)
                        imageGesture = TransformImageGesture.INIT
                    }
                }

                TransformImageGesture.ROTATION -> {

                    val newPan = (transformData().position.toOffset + panChange).toOffsetData
                    val newRotation = (transformData().rotation + rotationChange).toInt()

                    val newTransformData = transformData().copy(
                        rotation = newRotation.coerceIn(0..360),
                        position = newPan
                    )

                    onTransformDataChange(newTransformData)
                    gestureCoroutineScope.coroutineContext.cancelChildren()

                    gestureCoroutineScope.launch(context = Dispatchers.Default) {

                        delay(duration = 300.milliseconds)
                        imageGesture = TransformImageGesture.INIT
                    }
                }

                TransformImageGesture.RIGHT_SWIPE, TransformImageGesture.LEFT_SWIPE -> {

                    return@rememberTransformableState
                }

                else -> {

                    imageGesture = TransformImageGesture.INIT
                    return@rememberTransformableState
                }
            }

            isOneTouch && isZoomed -> when (imageGesture) {

                TransformImageGesture.INIT -> if (transformConfig().isPanEnabled) {

                    imageGesture = TransformImageGesture.PAN
                }

                TransformImageGesture.PAN -> {

                    val newPan = (transformData().position.toOffset + panChange).toOffsetData
                    val newTransformData = transformData().copy(position = newPan)

                    onTransformDataChange(newTransformData)
                    gestureCoroutineScope.coroutineContext.cancelChildren()

                    gestureCoroutineScope.launch(context = Dispatchers.Default) {

                        delay(duration = 300.milliseconds)
                        imageGesture = TransformImageGesture.INIT
                    }
                }

                TransformImageGesture.RIGHT_SWIPE, TransformImageGesture.LEFT_SWIPE -> {

                    return@rememberTransformableState
                }

                else -> {

                    imageGesture = TransformImageGesture.INIT
                    return@rememberTransformableState
                }
            }

            isCanSwipe -> return@rememberTransformableState

            else -> {

                imageGesture = TransformImageGesture.INIT
                return@rememberTransformableState
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(Unit) {

                awaitEachGesture {

                    do {

                        val event = awaitPointerEvent()

                        touchCount = event.changes.size
                    } while (event.changes.any { change -> change.pressed })
                }
            }
            .transformable(state = transformableState)
            .pointerInput(Unit) {

                detectTapGestures(
                    onDoubleTap = { position ->

                        val zoomFactor = when (transformData().zoom) {

                            in 0.80F..1.40F -> 2.0F
                            in 1.80F..2.40F -> 3.0F
                            in 2.80F..3.40F -> 4.0F
                            else -> 1.0F
                        }

                        val newTransformData = transformData().copy(
                            zoom = zoomFactor,
                            position = OffsetData()
                        )

                        onTransformDataChange(newTransformData)
                    },
                    onTap = onClick,
                    onLongPress = onLongClick
                )
            }
            .pointerInput(Unit) {

                detectHorizontalDragGestures(
                    onDragCancel = {

                        gestureCoroutineScope.coroutineContext.cancelChildren()

                        gestureCoroutineScope.launch(context = Dispatchers.Default) {

                            delay(duration = 200.milliseconds)
                            imageGesture = TransformImageGesture.INIT
                        }
                    },
                    onDragEnd = {

                        gestureCoroutineScope.coroutineContext.cancelChildren()

                        gestureCoroutineScope.launch(context = Dispatchers.Default) {

                            delay(duration = 200.milliseconds)
                            imageGesture = TransformImageGesture.INIT
                        }
                    }
                ) { change, dragAmount ->

                    change.consume()
                    dragSwipeMinimum += dragAmount

                    when {

                        isTwoTouch -> {

                            dragSwipeMinimum = 0.0F
                            imageGesture = TransformImageGesture.INIT
                            change.changedToUp()
                            return@detectHorizontalDragGestures
                        }

                        isOneTouch && abs(x = dragSwipeMinimum) > 75.0F && isCanSwipe -> {

                            imageGesture = when (imageGesture) {

                                TransformImageGesture.INIT -> when {

                                    dragSwipeMinimum > 0 -> TransformImageGesture.RIGHT_SWIPE
                                    dragSwipeMinimum < 0 -> TransformImageGesture.LEFT_SWIPE
                                    else -> TransformImageGesture.INIT
                                }

                                else -> imageGesture
                            }

                            when (imageGesture) {

                                TransformImageGesture.LEFT_SWIPE -> {

                                    onLeftSwipe()
                                    change.changedToUp()
                                }

                                TransformImageGesture.RIGHT_SWIPE -> {

                                    onRightSwipe()
                                    change.changedToUp()
                                }

                                else -> {}
                            }

                            dragSwipeMinimum = 0.0F
                        }

                        else -> {

                            dragSwipeMinimum = 0.0F
                            imageGesture = TransformImageGesture.INIT
                            change.changedToUp()
                            return@detectHorizontalDragGestures
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {

        SubcomposeAsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = transformData().zoom.coerceIn(range = zoomRange),
                    scaleY = transformData().zoom.coerceIn(range = zoomRange),
                    translationX = transformData().position.x,
                    translationY = transformData().position.y,
                    rotationZ = transformData().rotation.toFloat()
                ),
            model = imageModel(),
            contentScale = contentScale,
            loading = {

                Column(
                    modifier = Modifier.matchParentSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator()
                }
            },
            error = {

                Column(
                    modifier = Modifier.matchParentSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        modifier = Modifier.size(size = this@BoxWithConstraints.maxWidth / 4),
                        imageVector = Icons.Filled.BrokenImage,
                        contentDescription = "Image View"
                    )
                }
            },
            contentDescription = "Image View"
        )
    }
}

private enum class TransformImageGesture {

    INIT,
    PAN,
    ZOOM,
    ROTATION,
    LEFT_SWIPE,
    RIGHT_SWIPE
}