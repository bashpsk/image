package io.bashpsk.imagekolor.color

import androidx.compose.runtime.Immutable

@Immutable
data class ImageKolorConfig(
    val enableBrightness: Boolean = true,
    val enableExposure: Boolean = true,
    val enableContrast: Boolean = true,
    val enableHighlights: Boolean = true,
    val enableShadows: Boolean = true,
    val enableSaturation: Boolean = true,
    val enableWarmth: Boolean = true,
    val enableTint: Boolean = true
)