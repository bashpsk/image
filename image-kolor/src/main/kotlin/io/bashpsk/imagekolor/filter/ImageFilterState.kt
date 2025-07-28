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

@Composable
fun rememberImageFilterState(previewImage: ImageBitmap? = null): ImageFilterState {

    val imageBitmap = previewImage ?: ImageBitmap.imageResource(R.drawable.flower_02)

    return remember(imageBitmap) { ImageFilterState(previewImage = imageBitmap) }
}

@Stable
class ImageFilterState(val previewImage: ImageBitmap) {

    var selectedFilter by mutableStateOf(ImageFilterType.Original)
        private set

    fun onSelectFilter(filter: ImageFilterType) {

        selectedFilter = filter
    }
}