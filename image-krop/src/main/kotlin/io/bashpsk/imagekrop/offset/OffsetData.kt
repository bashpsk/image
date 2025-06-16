package io.bashpsk.imagekrop.offset

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Immutable
@Parcelize
@Serializable
data class OffsetData(val x: Float = 0.0F, val y: Float = 0.0F) : Parcelable