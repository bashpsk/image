package io.bashpsk.imagekrop.view

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Represents the transformation data for an image.
 * This data class stores the zoom level, rotation angle, and position (X and Y coordinates) of
 * an image.
 * It is designed to be immutable, parcelable (for Android), and serializable (for data persistence
 * or transfer).
 *
 * @property zoom The zoom level of the image. Defaults to 1.0F (no zoom).
 * @property rotation The rotation angle of the image in degrees. Defaults to 0 (no rotation).
 * @property positionX The X-coordinate of the image's position. Defaults to 0.0F.
 * @property positionY The Y-coordinate of the image's position. Defaults to 0.0F.
 */
@Immutable
@Parcelize
@Serializable
data class ImageTransformData(
    val zoom: Float = 1.0F,
    val rotation: Int = 0,
    val positionX: Float = 0.0F,
    val positionY: Float = 0.0F,
) : Parcelable