package io.bashpsk.imagekrop.crop

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class KropConfig(
    val handleThreshold: Float = 52.0F,
    val handleLength: Float = 48.0F,
    val handleStroke: Float = 8.0F,
    val minimumCropSize: Float = 200.0F,
    val handleColor: Color = Color.White,
    val borderColor: Color = Color.Cyan,
    val targetSize: Float = 40.0F,
    val targetStroke: Float = 6.0F,
    val targetColor: Color = Color.Yellow,
    val overlayColor: Color = Color.Black.copy(alpha = 0.5F),
)