package io.bashpsk.imagekrop.crop

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImageKropTopBar(
    modifier: Modifier = Modifier,
    onRefreshing: (isVisible:Boolean)-> Unit,
    imageBitmap: ImageBitmap?,
    onModifiedImage: (image: ImageBitmap) -> Unit,
    onImageKropDone: (result: KropResult) -> Unit,
    canvasSize: IntSize,
    topLeft: Offset,
    bottomRight: Offset,
    onUndoImageBitmap: ()-> Unit,
    imagePreviewSheetState: SheetState,
    snackbarCoroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onNavigateBack:()-> Unit
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
                        canvasWidth = canvasSize.width.toFloat(),
                        canvasHeight = canvasSize.height.toFloat()
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
                        canvasWidth = canvasSize.width.toFloat(),
                        canvasHeight = canvasSize.height.toFloat()
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

@Composable
internal fun ImageKropBottomBar(
    modifier: Modifier = Modifier,
    imageBitmap: ImageBitmap?,
    kropConfig: KropConfig = KropConfig(),
    canvasSize: Size,
    topLeft: Offset,
    bottomRight: Offset,
    onImageKropResult: (result: KropResult) -> Unit
) {

}