package io.bashpsk.imagekrop.offset

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Immutable
@Parcelize
@Serializable
data class IntOffsetData(val x: Int = 0, val y: Int = 0) : Parcelable