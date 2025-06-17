package io.bashpsk.imagekrop.crop

import androidx.compose.ui.graphics.ImageBitmap

sealed interface KropResult {

    data object Init : KropResult

    data class Failed(val message: String, val original: ImageBitmap) : KropResult

    data class Success(val cropped: ImageBitmap, val original: ImageBitmap) : KropResult
}