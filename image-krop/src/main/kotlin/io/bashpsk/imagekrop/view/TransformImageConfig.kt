package io.bashpsk.imagekrop.view

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Configuration for image transformation capabilities.
 *
 * This data class defines the settings for enabling or disabling various image manipulation
 * features such as zooming, rotation, panning, and swiping. It is designed to be immutable,
 * parcelable for Android, and serializable for data persistence or transfer.
 *
 * @property isZoomEnabled Whether zooming the image is allowed. Defaults to `true`.
 * @property isRotationEnabled Whether rotating the image is allowed. Defaults to `true`.
 * @property isPanEnabled Whether panning (moving) the image is allowed. Defaults to `true`.
 * @property isSwipeEnabled Whether swiping gestures on the image are allowed. Defaults to `false`.
 */
@Immutable
@Parcelize
@Serializable
data class TransformImageConfig(
    val isZoomEnabled: Boolean = true,
    val isRotationEnabled: Boolean = true,
    val isPanEnabled: Boolean = true,
    val isSwipeEnabled: Boolean = false
) : Parcelable