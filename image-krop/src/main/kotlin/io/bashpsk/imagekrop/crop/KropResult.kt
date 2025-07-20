package io.bashpsk.imagekrop.crop

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Represents the result of a Krop operation.
 *
 * This sealed interface defines the possible outcomes of an image cropping operation:
 *  - [Init]: The initial state before any cropping has occurred.
 *  - [Failed]: Indicates that the cropping operation failed.
 *  - [Success]: Indicates that the cropping operation was successful.
 */
sealed interface KropResult {

    /**
     * Initial state of the KropResult, indicating that no cropping operation has been performed
     * yet.
     */
    data object Init : KropResult

    /**
     * Represents a failed crop operation.
     *
     * @property message A message describing the reason for the failure.
     * @property original The original [ImageBitmap] that was attempted to be cropped, or null if
     * not available.
     */
    data class Failed(val message: String, val original: ImageBitmap?) : KropResult

    /**
     * Represents a successful crop operation.
     *
     * @property cropped The resulting cropped image.
     * @property original The original image that was cropped.
     */
    data class Success(val cropped: ImageBitmap, val original: ImageBitmap) : KropResult
}