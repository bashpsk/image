package io.bashpsk.imagekolor.color

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import kotlin.math.pow

@Composable
fun rememberImageKolorState(
    imageBitmap: ImageBitmap?,
    config: ImageKolorConfig = ImageKolorConfig()
): ImageKolorState {

    return remember(imageBitmap) { ImageKolorState(imageBitmap = imageBitmap, config = config) }
}

@Stable
class ImageKolorState(
    val imageBitmap: ImageBitmap?,
    val config: ImageKolorConfig
) {

    var brightness by mutableFloatStateOf(0F)
        internal set

    var exposure by mutableFloatStateOf(0F)
        internal set

    var contrast by mutableFloatStateOf(1F)
        internal set

    var highlights by mutableFloatStateOf(0F)
        internal set

    var shadows by mutableFloatStateOf(0F)
        internal set

    var saturation by mutableFloatStateOf(1F)
        internal set

    var warmth by mutableFloatStateOf(0F)
        internal set

    var tint by mutableFloatStateOf(0F)
        internal set

    fun resetAllValues() {

        brightness = 0F
        exposure = 0F
        contrast = 1F
        highlights = 0F
        shadows = 0F
        saturation = 1F
        warmth = 0F
        tint = 0F
    }

    fun getColorMatrix(): ColorMatrix {

        val finalMatrix = ColorMatrix()

        finalMatrix.timesAssign(brightnessMatrix(brightness))
        finalMatrix.timesAssign(exposureMatrix(exposure))
        finalMatrix.timesAssign(contrastMatrix(contrast))
        finalMatrix.timesAssign(saturationMatrix(saturation))
        finalMatrix.timesAssign(warmthMatrix(warmth))
        finalMatrix.timesAssign(tintMatrix(tint))

        if (highlights != 0F) {

            val highlightBrightnessShift = highlights * 20F

            val matrixArray = floatArrayOf(
                1F, 0F, 0F, 0F, highlightBrightnessShift,
                0F, 1F, 0F, 0F, highlightBrightnessShift,
                0F, 0F, 1F, 0F, highlightBrightnessShift,
                0F, 0F, 0F, 1F, 0F
            )

            val newColorMatrix = ColorMatrix(matrixArray)

            finalMatrix.timesAssign(newColorMatrix)
        }

        if (shadows != 0F) {

            val shadowBrightnessShift = shadows * 20F

            val matrixArray = floatArrayOf(
                1F, 0F, 0F, 0F, shadowBrightnessShift,
                0F, 1F, 0F, 0F, shadowBrightnessShift,
                0F, 0F, 1F, 0F, shadowBrightnessShift,
                0F, 0F, 0F, 1F, 0F
            )

            val newColorMatrix = ColorMatrix(matrixArray)

            finalMatrix.timesAssign(newColorMatrix)
        }

        return finalMatrix
    }

    fun getColorFilter(): ColorFilter {

        return ColorFilter.colorMatrix(colorMatrix = getColorMatrix())
    }

    private fun brightnessMatrix(value: Float): ColorMatrix {

        val brightnessValue = value * 100F

        val matrixArray = floatArrayOf(
            1F, 0F, 0F, 0F, brightnessValue,
            0F, 1F, 0F, 0F, brightnessValue,
            0F, 0F, 1F, 0F, brightnessValue,
            0F, 0F, 0F, 1F, 0F
        )

        return ColorMatrix(matrixArray)
    }

    private fun exposureMatrix(value: Float): ColorMatrix {

        val scale = 2.0.pow(value.toDouble()).toFloat()

        val matrixArray = floatArrayOf(
            scale, 0F, 0F, 0F, 0F,
            0F, scale, 0F, 0F, 0F,
            0F, 0F, scale, 0F, 0F,
            0F, 0F, 0F, 1F, 0F
        )

        return ColorMatrix(matrixArray)
    }

    private fun contrastMatrix(value: Float): ColorMatrix {

        val translate = (-0.5F * value + 0.5F) * 255F

        val matrixArray = floatArrayOf(
            value, 0F, 0F, 0F, translate,
            0F, value, 0F, 0F, translate,
            0F, 0F, value, 0F, translate,
            0F, 0F, 0F, 1F, 0F
        )

        return ColorMatrix(matrixArray)
    }

    private fun saturationMatrix(value: Float): ColorMatrix {

        return ColorMatrix().apply {

            setToSaturation(value)
        }
    }

    private fun warmthMatrix(value: Float): ColorMatrix {

        val warmFactor = value * 0.2F

        val matrixArray = floatArrayOf(
            1F + warmFactor, 0F, 0F, 0F, 0F,
            0F, 1F, 0F, 0F, 0F,
            0F, 0F, 1F - warmFactor, 0F, 0F,
            0F, 0F, 0F, 1F, 0F
        )

        return ColorMatrix(matrixArray)
    }

    private fun tintMatrix(value: Float): ColorMatrix {

        val tintFactor = value * 0.15F

        val matrixArray = floatArrayOf(
            1F + tintFactor, 0F, 0F, 0F, 0F,
            0F, 1F - tintFactor, 0F, 0F, 0F,
            0F, 0F, 1F + tintFactor, 0F, 0F,
            0F, 0F, 0F, 1F, 0F
        )

        return ColorMatrix(matrixArray)
    }
}