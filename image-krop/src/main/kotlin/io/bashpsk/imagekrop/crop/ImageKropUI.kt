package io.bashpsk.imagekrop.crop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
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
    originalImageBitmap: ImageBitmap?,
    modifiedImageBitmap: ImageBitmap,
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

    val isUndoImageBitmap by remember(originalImageBitmap, modifiedImageBitmap) {
        derivedStateOf { originalImageBitmap?.sameAs(modifiedImageBitmap)?.not() ?: false }
    }

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

            IconButton(
//                enabled = isUndoImageBitmap,
                onClick = onUndoImageBitmap
            ) {

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Image Crop Undo"
                )
            }

            IconButton(
                onClick = {

                    onRefreshing(true)

                    val kropResult = originalImageBitmap?.getCroppedImageBitmap(
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

                    val kropResult = originalImageBitmap?.getCroppedImageBitmap(
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

                            onNavigateBack()
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
    originalImageBitmap: ImageBitmap?,
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

    var isAspectRatioMenuExpanded by remember { mutableStateOf(false) }
    var isShapeMenuExpanded by remember { mutableStateOf(false) }

    val aspectLockIcon by remember(isAspectLocked) {
        derivedStateOf { if (isAspectLocked) Icons.Filled.Lock else Icons.Filled.LockOpen }
    }

    BottomAppBar(modifier = modifier) {

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.SpaceAround,
            itemVerticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {

                    onRefreshing(true)

                    val kropResult = originalImageBitmap?.getCroppedImageBitmap(
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

                    val kropResult = originalImageBitmap?.getCroppedImageBitmap(
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
                        modifier = Modifier.size(size = 64.dp),
                        text = {

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {

                                Icon(
                                    imageVector = aspectLockIcon,
                                    contentDescription = "Aspect Locked"
                                )
                            }
                        },
                        onClick = {

                            onAspectLocked(isAspectLocked.not())
                        }
                    )

                    KropAspectRatio.entries.forEach { aspectRatio ->

                        val isSelected by remember(kropAspectRatio, aspectRatio) {
                            derivedStateOf { kropAspectRatio == aspectRatio }
                        }

                        DropdownMenuItem(
                            modifier = Modifier.size(size = 64.dp),
                            text = {

                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .alpha(if (isSelected) 0.50F else 1.0F),
                                    text = "${aspectRatio.width}:${aspectRatio.height}",
                                    textAlign = TextAlign.Center,
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

                        val isSelected by remember(kropShape, shape) {
                            derivedStateOf { kropShape == shape }
                        }

                        DropdownMenuItem(
                            modifier = Modifier.size(size = 44.dp),
                            text = {

                                KropShapeView(kropShape = shape, isSelected = isSelected)
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
}

@Composable
private fun KropShapeView(
    kropShape: KropShape,
    isSelected: Boolean
) {

    val unSelectedIconColor = MaterialTheme.colorScheme.onSurface
    val selectedIconColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.5F)

    val shapeColor by remember(isSelected, selectedIconColor, unSelectedIconColor) {
        derivedStateOf { if (isSelected) selectedIconColor else unSelectedIconColor }
    }

    Canvas(
        modifier = Modifier.size(size = 20.dp),
        contentDescription = kropShape.name
    ) {

        drawKropShapePreview(kropShape = kropShape, shapeColor = shapeColor)
    }
}

@Composable
internal fun SelectedIcon(isSelected: Boolean) {

    AnimatedVisibility(
        visible = isSelected,
        enter = fadeIn(),
        exit = fadeOut()
    ) {

        Icon(imageVector = Icons.Filled.Check, contentDescription = "Selected")
    }
}