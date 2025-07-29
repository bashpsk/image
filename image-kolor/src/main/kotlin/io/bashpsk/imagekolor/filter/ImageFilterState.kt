package io.bashpsk.imagekolor.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Creates and remembers an [ImageFilterState] instance that survives configuration changes.
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

    return rememberSaveable(previewImage, saver = ImageFilterState.StateSaver) {

        ImageFilterState(previewImage = previewImage)
    }
}

/**
 * Represents the state of the image filter, including the preview image and the selected filter.
 *
 * @param previewImage The image to be displayed and filtered.
 */
@Stable
class ImageFilterState(val previewImage: ImageBitmap?) {

    var selectedFilter by mutableStateOf(ImageFilterType.Original)
        private set

    fun onSelectFilter(filter: ImageFilterType) {
        selectedFilter = filter
    }

    companion object {

        val StateSaver = Saver<ImageFilterState, List<Any?>>(
            save = { state ->

                listOf(state.previewImage, state.selectedFilter)
            },
            restore = { elements ->

                val savedPreviewImage=elements.getOrNull(0) as? ImageBitmap

                val savedSelectedFilter = elements.getOrNull(1) as? ImageFilterType
                    ?: ImageFilterType.Original

                ImageFilterState(
                    previewImage = savedPreviewImage
                ).apply {

                    selectedFilter = savedSelectedFilter
                }
            }
        )
    }
}