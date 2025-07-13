package io.bashpsk.imagekrop.crop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImageKropTopBar(
    modifier: Modifier = Modifier,
    onRefreshing: (isVisible: Boolean) -> Unit,
    imageBitmap: ImageBitmap?,
    onModifiedImage: (image: ImageBitmap) -> Unit,
    onImageKropDone: (result: KropResult) -> Unit,
    canvasSize: IntSize,
    topLeft: Offset,
    bottomRight: Offset,
    onUndoImageBitmap: () -> Unit,
    imagePreviewSheetState: SheetState,
    snackbarCoroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    kropShape: KropShape,
    onNavigateBack: () -> Unit
) {

    val imagePreviewCoroutineScope = rememberCoroutineScope()

    TopAppBar(
        modifier = modifier,
        navigationIcon = {

            IconButton(onClick = onNavigateBack) {

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate Back"
                )
            }
        },
        title = {},
        actions = {

            IconButton(onClick = onUndoImageBitmap) {

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Image Crop Undo"
                )
            }

            IconButton(
                onClick = {

                    onRefreshing(true)

                    val kropResult = imageBitmap?.getCroppedImageBitmap(
                        cropRect = Rect(topLeft = topLeft, bottomRight = bottomRight),
                        canvasSize = canvasSize,
                        kropShape = kropShape
                    ) ?: KropResult.Failed(message = "Image is Null.", original = null)

                    when (kropResult) {

                        is KropResult.Init -> {

                            snackbarCoroutineScope.coroutineContext.cancelChildren()

                            snackbarCoroutineScope.launch {

                                val message = "Cropped Image Not Found!"

                                snackbarHostState.showSnackbar(message = message)
                            }
                        }

                        is KropResult.Failed -> {

                            snackbarCoroutineScope.coroutineContext.cancelChildren()

                            snackbarCoroutineScope.launch {

                                val message = "Image Crop Failed!"

                                snackbarHostState.showSnackbar(message = message)
                            }
                        }

                        is KropResult.Success -> imagePreviewCoroutineScope.launch {

                            onModifiedImage(kropResult.cropped)
                            imagePreviewSheetState.expand()
                        }
                    }

                    onRefreshing(false)
                }
            ) {

                Icon(
                    imageVector = Icons.Filled.Preview,
                    contentDescription = "Image Crop Preview"
                )
            }

            IconButton(
                onClick = {

                    onRefreshing(true)

                    val kropResult = imageBitmap?.getCroppedImageBitmap(
                        cropRect = Rect(topLeft = topLeft, bottomRight = bottomRight),
                        canvasSize = canvasSize,
                        kropShape = kropShape
                    ) ?: KropResult.Failed(message = "Image is Null.", original = null)

                    when (kropResult) {

                        is KropResult.Init -> {

                            snackbarCoroutineScope.coroutineContext.cancelChildren()

                            snackbarCoroutineScope.launch {

                                val message = "Cropped Image Not Found!"

                                snackbarHostState.showSnackbar(message = message)
                            }
                        }

                        is KropResult.Failed -> {

                            snackbarCoroutineScope.coroutineContext.cancelChildren()

                            snackbarCoroutineScope.launch {

                                val message = "Image Crop Failed!"

                                snackbarHostState.showSnackbar(message = message)
                            }
                        }

                        is KropResult.Success -> {

//                            onNavigateBack()
                            onModifiedImage(kropResult.cropped)
                            onImageKropDone(kropResult)
                        }
                    }

                    onRefreshing(false)
                }
            ) {

                Icon(
                    imageVector = Icons.Filled.DoneAll,
                    contentDescription = "Image Crop Done"
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImageKropBottomBar(
    modifier: Modifier = Modifier,
    kropAspectRatio: KropAspectRatio,
    onKropAspectRatio: (aspect: KropAspectRatio) -> Unit,
    kropShapeList: ImmutableList<KropShape>? = null,
    kropShape: KropShape,
    onKropShape: (shape: KropShape) -> Unit,
    onRefreshing: (isVisible: Boolean) -> Unit,
    imageBitmap: ImageBitmap?,
    onModifiedImage: (image: ImageBitmap) -> Unit,
    onImageKropDone: (result: KropResult) -> Unit,
    canvasSize: IntSize,
    topLeft: Offset,
    bottomRight: Offset,
    onUndoImageBitmap: () -> Unit,
    snackbarCoroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    isAspectLocked: Boolean,
    onAspectLocked: (isLocked: Boolean) -> Unit
) {

    val unSelectedIconColor = MaterialTheme.colorScheme.onSurface
    val selectedIconColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.5F)

    var isAspectRatioMenuExpanded by remember { mutableStateOf(false) }
    var isShapeMenuExpanded by remember { mutableStateOf(false) }

    val aspectLockIcon by remember(isAspectLocked) {
        derivedStateOf { if (isAspectLocked) Icons.Filled.Lock else Icons.Filled.LockOpen }
    }

    BottomAppBar(
        modifier = modifier,
    ) {

        IconButton(
            onClick = {

                onRefreshing(true)

                val kropResult = imageBitmap?.getCroppedImageBitmap(
                    cropRect = Rect(topLeft = topLeft, bottomRight = bottomRight),
                    canvasSize = canvasSize,
                    imageFlip = KropImageFlip.Horizontal,
                    kropShape = kropShape
                ) ?: KropResult.Failed(message = "Image is Null.", original = null)

                when (kropResult) {

                    is KropResult.Init -> {

                        snackbarCoroutineScope.coroutineContext.cancelChildren()

                        snackbarCoroutineScope.launch {

                            val message = "Flip Image Not Found!"

                            snackbarHostState.showSnackbar(message = message)
                        }
                    }

                    is KropResult.Failed -> {

                        snackbarCoroutineScope.coroutineContext.cancelChildren()

                        snackbarCoroutineScope.launch {

                            val message = "Image Flip Failed!"

                            snackbarHostState.showSnackbar(message = message)
                        }
                    }

                    is KropResult.Success -> {

                        onModifiedImage(kropResult.cropped)
                        onImageKropDone(kropResult)
                    }
                }

                onRefreshing(false)
            }
        ) {

            Icon(
                imageVector = Icons.Filled.Flip,
                contentDescription = "Flip Horizontal"
            )
        }

        IconButton(
            onClick = {

                onRefreshing(true)

                val kropResult = imageBitmap?.getCroppedImageBitmap(
                    cropRect = Rect(topLeft = topLeft, bottomRight = bottomRight),
                    canvasSize = canvasSize,
                    imageFlip = KropImageFlip.Vertical,
                    kropShape = kropShape
                ) ?: KropResult.Failed(message = "Image is Null.", original = null)

                when (kropResult) {

                    is KropResult.Init -> {

                        snackbarCoroutineScope.coroutineContext.cancelChildren()

                        snackbarCoroutineScope.launch {

                            val message = "Flip Image Not Found!"

                            snackbarHostState.showSnackbar(message = message)
                        }
                    }

                    is KropResult.Failed -> {

                        snackbarCoroutineScope.coroutineContext.cancelChildren()

                        snackbarCoroutineScope.launch {

                            val message = "Image Flip Failed!"

                            snackbarHostState.showSnackbar(message = message)
                        }
                    }

                    is KropResult.Success -> {

                        onModifiedImage(kropResult.cropped)
                        onImageKropDone(kropResult)
                    }
                }

                onRefreshing(false)
            }
        ) {

            Icon(
                modifier = Modifier.rotate(degrees = 90.0F),
                imageVector = Icons.Filled.Flip,
                contentDescription = "Flip Vertical"
            )
        }

        IconButton(
            onClick = {

                isAspectRatioMenuExpanded = true
            }
        ) {

            Icon(
                imageVector = Icons.Filled.AspectRatio,
                contentDescription = "Aspect Ratio"
            )

            DropdownMenu(
                expanded = isAspectRatioMenuExpanded,
                onDismissRequest = {

                    isAspectRatioMenuExpanded = false
                }
            ) {

                DropdownMenuItem(
                    text = {

                        Icon(
                            imageVector = aspectLockIcon,
                            contentDescription = "Aspect Locked"
                        )
                    },
                    onClick = {

                        onAspectLocked(isAspectLocked.not())
                    }
                )

                KropAspectRatio.entries.forEach { aspectRatio ->

                    val isSelected by remember(kropAspectRatio) {
                        derivedStateOf { kropAspectRatio == aspectRatio }
                    }

                    DropdownMenuItem(
                        text = {

                            Text(
                                modifier = Modifier.alpha(if (isSelected) 0.50F else 1.0F),
                                text = "${aspectRatio.width}:${aspectRatio.height}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {

                            onKropAspectRatio(aspectRatio)
                            isAspectRatioMenuExpanded = false
                        }
                    )
                }
            }
        }

        IconButton(
            onClick = {

                isShapeMenuExpanded = true
            }
        ) {

            Icon(
                imageVector = Icons.Filled.StarBorder,
                contentDescription = "Shape"
            )

            DropdownMenu(
                expanded = isShapeMenuExpanded,
                onDismissRequest = {

                    isShapeMenuExpanded = false
                }
            ) {

                (kropShapeList ?: KropShape.entries.toImmutableList()).forEach { shape ->

                    val isSelected by remember(kropShape) { derivedStateOf { kropShape == shape } }

                    val shapeColor by remember(isSelected, selectedIconColor, unSelectedIconColor) {
                        derivedStateOf { if (isSelected) selectedIconColor else unSelectedIconColor }
                    }

                    DropdownMenuItem(
                        text = {

                            Canvas(
                                modifier = Modifier.size(size = 20.dp),
                                contentDescription = shape.name
                            ) {

                                drawKropShapePreview(
                                    kropShape = shape,
                                    shapeColor = shapeColor
                                )
                            }
                        },
                        onClick = {

                            onKropShape(shape)
                            isShapeMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}