package io.bashpsk.imagekrop.crop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.bashpsk.imagekrop.view.TransformImageConfig
import io.bashpsk.imagekrop.view.TransformImageView
import io.bashpsk.imagekrop.view.rememberImageTransformState
import kotlinx.coroutines.launch

/**
 * Composable function that displays a preview of the original and modified images in a modal bottom
 * sheet.
 *
 * @param sheetState The state of the modal bottom sheet.
 * @param state The [ImageKropState] containing the original and modified image bitmaps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KropImagePreview(
    sheetState: SheetState,
    state: ImageKropState
) {

    val sheetCoroutineScope = rememberCoroutineScope()
    val transformConfig = rememberSaveable { TransformImageConfig(enableRotation = false) }
    val imageTransformState = rememberImageTransformState(config = transformConfig)

    var isOriginalImage by rememberSaveable { mutableStateOf(value = false) }

    val selectedImage by remember(state) {
        derivedStateOf { if (isOriginalImage) state.originalImage else state.previewImage }
    }

    val titleCardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.50F),
        contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainerLowest)
    )

    AnimatedVisibility(
        visible = sheetState.isVisible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {

        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            sheetState = sheetState,
            onDismissRequest = {

                sheetCoroutineScope.launch { sheetState.hide() }
            }
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                selectedImage?.let { bitmap ->

                    Box(
                        modifier = Modifier.wrapContentSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {

                        TransformImageView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(bitmap.width.toFloat() / bitmap.height),
                            imageModel = { bitmap.asAndroidBitmap() },
                            state = imageTransformState,
                            onLeftSwipe = {

                                imageTransformState.resetAllValues()
                                isOriginalImage = isOriginalImage.not()
                            },
                            onRightSwipe = {

                                imageTransformState.resetAllValues()
                                isOriginalImage = isOriginalImage.not()
                            }
                        )

                        Card(
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
                            colors = titleCardColors,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {

                            Text(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                text = if (isOriginalImage) "Original" else "Modified",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        ImageCompareButton(
                            isOriginalImage = isOriginalImage,
                            onShowImageBitmap = { isVisible ->

                                imageTransformState.resetAllValues()
                                isOriginalImage = isVisible
                            }
                        )
                    }
                } ?: Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    text = "Image Not Found",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

/**
 * A composable function that displays a button to toggle between showing the original and modified
 * (cropped) images.
 *
 * @param isOriginalImage A boolean indicating whether the original image is currently displayed.
 * @param onShowImageBitmap A callback function that is invoked when the button is clicked.
 * It receives a boolean value indicating whether the original image should be shown.
 */
@Composable
private fun ImageCompareButton(
    isOriginalImage: Boolean,
    onShowImageBitmap: (isVisible: Boolean) -> Unit
) {

    Button(
        onClick = {

            onShowImageBitmap(isOriginalImage.not())
        }
    ) {

        Icon(
            modifier = Modifier.size(size = 18.dp),
            imageVector = Icons.Filled.Compare,
            contentDescription = "Compare Image"
        )

        Spacer(modifier = Modifier.width(width = 2.dp))

        Text(
            text = if (isOriginalImage) "Show Cropped Image" else "Show Original Image",
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}