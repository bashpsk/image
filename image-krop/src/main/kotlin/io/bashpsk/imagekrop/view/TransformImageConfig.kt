package io.bashpsk.imagekrop.view

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Immutable
@Parcelize
@Serializable
data class TransformImageConfig(
    val isZoomEnabled: Boolean = true,
    val isRotationEnabled: Boolean = true,
    val isPanEnabled: Boolean = true,
    val isSwipeEnabled: Boolean = true
) : Parcelable