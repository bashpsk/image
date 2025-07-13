package io.bashpsk.imagekrop.offset

import androidx.compose.ui.geometry.Offset

fun Offset.hasNeared(point: Offset, threshold: Float = 24.0F): Boolean {

    return (this - point).getDistance() <= threshold
}