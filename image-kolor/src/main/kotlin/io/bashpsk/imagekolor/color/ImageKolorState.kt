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

/**
 * Remembers and creates an [ImageKolorState] instance.
 *
 * This composable function is used to create and manage the state for image color adjustments.
 * It takes an [ImageBitmap] and an optional [ImageKolorConfig] as input.
 * The state is remembered across recompositions, and will be re-initialized if the [imageBitmap]
 * changes.
 *
 * @param imageBitmap The [ImageBitmap] to apply color adjustments to. Can be null if no image is
 * loaded.
 * @param config The [ImageKolorConfig] to use for configuring the color adjustments. Defaults to a
 * default [ImageKolorConfig].
 * @return An [ImageKolorState] instance that holds the current color adjustment values and provides
 * methods to modify them.
 */
@Composable
fun rememberImageKolorState(
    imageBitmap: ImageBitmap?,
    config: ImageKolorConfig = ImageKolorConfig()
): ImageKolorState {

    return remember(imageBitmap) { ImageKolorState(imageBitmap = imageBitmap, config = config) }
}

/**
 * State object that can be used to control the color adjustments applied to an image.
 * This state is typically remembered using [rememberImageKolorState].
 *
 * @param imageBitmap The [ImageBitmap] to apply color adjustments to. Can be null if no image is
 * loaded yet.
 * @param config The [ImageKolorConfig] that defines the behavior and appearance of the color
 * adjustment controls.
 *
 * @property brightness Controls the overall lightness or darkness of the image.
 * Value typically ranges from -1F (darker) to 1F (lighter), with 0F being the original brightness.
 * @property exposure Controls the exposure level of the image, simulating changes in camera exposure.
 * Value typically ranges from -1F (underexposed) to 1F (overexposed), with 0F being the original
 * exposure.
 * @property contrast Controls the difference between light and dark areas of the image.
 * Value typically ranges from 0F (no contrast) to 2F (high contrast), with 1F being the original
 * contrast.
 * @property highlights Adjusts the brightness of the brightest areas of the image.
 * Value typically ranges from -1F (darker highlights) to 1F (brighter highlights), with 0F being no
 * change.
 * @property shadows Adjusts the brightness of the darkest areas of the image.
 * Value typically ranges from -1F (darker shadows) to 1F (brighter shadows), with 0F being no
 * change.
 * @property saturation Controls the intensity of colors in the image.
 * Value typically ranges from 0F (grayscale) to 2F (highly saturated), with 1F being the original
 * saturation.
 * @property warmth Adjusts the color temperature of the image, making it appear warmer
 * (more orange) or cooler (more blue).
 * Value typically ranges from -1F (cooler) to 1F (warmer), with 0F being the original warmth.
 * @property tint Adjusts the green-magenta balance of the image.
 * Value typically ranges from -1F (more green) to 1F (more magenta), with 0F being the original
 * tint.
 */
@Stable
class ImageKolorState(val imageBitmap: ImageBitmap?, val config: ImageKolorConfig) {

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