package io.bashpsk.imagekrop.crop

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class KropConfig(
    val handleWidth: Dp = 20.dp,
    val handleHeight: Dp = 4.dp,
    val centerHandleWidth: Dp = 16.dp,
    val minimumCropSize: Dp = 160.dp,
    val handleColor: Color = Color.White,
    val borderThickness: Dp = 2.dp,
    val borderColor: Color = Color.Cyan,
    val targetSize: Dp = 16.dp,
    val targetThickness: Dp = 1.dp,
    val targetColor: Color = Color.Yellow,
    val overlayColor: Color = Color.Black.copy(alpha = 0.5F),
    val shapeBorder: Dp = 0.5.dp,
    val shapeColor: Color = Color.Green.copy(alpha = 0.5F),
)