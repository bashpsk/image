package io.bashpsk.imagekrop.crop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
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
    selectedAspectRatio: KropAspectRatio,
    onKropAspectRatio: (aspect: KropAspectRatio) -> Unit,
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
) {

    var isAspectRatioMenuExpanded by remember { mutableStateOf(false) }
    var aspectRatioMenuPosition by remember { mutableStateOf(IntOffset.Zero) }

    var currentHorizontalFlip by remember { mutableStateOf(KropImageFlip.LeftToRight) }
    var currentVerticalFlip by remember { mutableStateOf(KropImageFlip.TopToBottom) }

    DropdownMenu(
//        modifier = Modifier.offset { aspectRatioMenuPosition },
        expanded = isAspectRatioMenuExpanded,
        onDismissRequest = {

            isAspectRatioMenuExpanded = false
        }
    ) {

        KropAspectRatio.entries.forEach { aspectRatio ->

            val isSelected by remember { derivedStateOf { selectedAspectRatio == aspectRatio } }

            DropdownMenuItem(
                text = {

                    Text(
                        text = "${aspectRatio.widthRatio}:${aspectRatio.heightRatio}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                trailingIcon = {

                    AnimatedVisibility(visible = isSelected, enter = fadeIn(), exit = fadeOut()) {

                        Icon(imageVector = Icons.Filled.Check, contentDescription = "Selected")
                    }
                },
                onClick = {

                    onKropAspectRatio(aspectRatio)
                    isAspectRatioMenuExpanded = false
                }
            )
        }
    }

    BottomAppBar(
        modifier = modifier,
    ) {

        IconButton(
            onClick = {

                onRefreshing(true)

                val newFlip = if (currentHorizontalFlip == KropImageFlip.LeftToRight)
                    KropImageFlip.RightToLeft
                else
                    KropImageFlip.LeftToRight

                currentHorizontalFlip = newFlip

                val kropResult = imageBitmap?.getCroppedImageBitmap(
                    cropRect = Rect(topLeft = topLeft, bottomRight = bottomRight),
                    canvasSize = canvasSize,
                    imageFlip = currentHorizontalFlip
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

                currentHorizontalFlip = KropImageFlip.None
                onRefreshing(false)
            }
        ) {

            Icon(
                modifier = Modifier.rotate(currentHorizontalFlip.rotationAngleForHorizontal()),
                imageVector = Icons.Filled.Flip,
                contentDescription = "Flip Horizontal"
            )
        }

        IconButton(
            onClick = {

                onRefreshing(true)

                val newFlip = if (currentVerticalFlip == KropImageFlip.TopToBottom)
                    KropImageFlip.BottomToTop
                else
                    KropImageFlip.TopToBottom

                currentVerticalFlip = newFlip

                val kropResult = imageBitmap?.getCroppedImageBitmap(
                    cropRect = Rect(topLeft = topLeft, bottomRight = bottomRight),
                    canvasSize = canvasSize,
                    imageFlip = currentVerticalFlip
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

                currentVerticalFlip = KropImageFlip.None
                onRefreshing(false)
            }
        ) {

            Icon(
                modifier = Modifier.rotate(currentVerticalFlip.rotationAngleForVertical()),
                imageVector = Icons.Filled.Flip,
                contentDescription = "Flip Vertical"
            )
        }

        IconButton(
            modifier = Modifier.onPlaced { layoutCoordinates ->

                aspectRatioMenuPosition = layoutCoordinates.positionOnScreen().round()
            },
            onClick = {

                isAspectRatioMenuExpanded = true
            }
        ) {

            Icon(
                imageVector = Icons.Filled.AspectRatio,
                contentDescription = "Aspect Ratio"
            )
        }
    }
}