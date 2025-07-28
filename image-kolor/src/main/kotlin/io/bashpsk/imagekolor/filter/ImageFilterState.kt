package io.bashpsk.imagekolor.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import io.bashpsk.imagekolor.R

/**
 * Creates and remembers an [ImageFilterState] instance.
 *
 * This composable function is used to manage the state of image filters,
 * including the currently selected filter and the preview image.
 *
 * @param previewImage The optional [ImageBitmap] to be used as the preview image.
 * If `null`, a default image (R.drawable.flower_02) will be used.
 * @return An [ImageFilterState] instance that can be used to control and observe the image filter
 * state.
 */
@Composable
fun rememberImageFilterState(previewImage: ImageBitmap? = null): ImageFilterState {

    val imageBitmap = previewImage ?: ImageBitmap.imageResource(R.drawable.flower_02)

    return remember(imageBitmap) { ImageFilterState(previewImage = imageBitmap) }
}

/**
 * Represents the state of the image filter, including the preview image and the selected filter.
 *
 * @param previewImage The image to be displayed and filtered.
 */
@Stable
class ImageFilterState(val previewImage: ImageBitmap) {

    var selectedFilter by mutableStateOf(ImageFilterType.Original)
        private set

    fun onSelectFilter(filter: ImageFilterType) {

        selectedFilter = filter
    }
}