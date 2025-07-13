package io.bashpsk.imagekrop.view

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Immutable
@Parcelize
@Serializable
data class ImageTransformData(
    val zoom: Float = 1.0F,
    val rotation: Int = 0,
    val positionX: Float = 0.0F,
    val positionY: Float = 0.0F,
) : Parcelable