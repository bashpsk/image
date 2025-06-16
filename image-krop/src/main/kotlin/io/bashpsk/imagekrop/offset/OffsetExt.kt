package io.bashpsk.imagekrop.offset

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset

val Offset.toOffsetData: OffsetData
    get() = OffsetData(x = x, y = y)

val OffsetData.toOffset: Offset
    get() = Offset(x = x, y = y)

val IntOffset.toIntOffsetData: IntOffsetData
    get() = IntOffsetData(x = x, y = y)

val IntOffsetData.toIntOffset: IntOffset
    get() = IntOffset(x = x, y = y)