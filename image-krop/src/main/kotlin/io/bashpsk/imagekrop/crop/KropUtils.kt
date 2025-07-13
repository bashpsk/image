package io.bashpsk.imagekrop.crop

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

@Composable
internal fun Dp.toPixel(): Float {

    return with(receiver = LocalDensity.current) { this@toPixel.toPx() }
}

internal fun Dp.toPixel(density: Density): Float {

    return with(receiver = density) { this@toPixel.toPx() }
}