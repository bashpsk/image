package io.bashpsk.imagekolor.color

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.roundToInt

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

@Composable
fun KolorImageView(modifier: Modifier = Modifier, state: ImageKolorState) {

    val colorFilter by remember(state) { derivedStateOf { state.getColorFilter() } }

    state.imageBitmap?.let { bitmap ->

        Image(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 16F / 9F),
            bitmap = bitmap,
            contentScale = ContentScale.Fit,
            colorFilter = colorFilter,
            contentDescription = "Image with Color Filter"
        )
    } ?: Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio = 16F / 9F),
        contentAlignment = Alignment.Center
    ) {

        BasicText(
            modifier = Modifier.fillMaxWidth(),
            text = "Image Load Failed!",
            style = MaterialTheme.typography.titleMedium,
            autoSize = TextAutoSize.StepBased(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun KolorAdjustmentSliders(modifier: Modifier = Modifier, state: ImageKolorState) {

    Column(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {

        AdjustmentSlider(
            modifier = Modifier.fillMaxWidth(),
            label = "Brightness",
            value = state.brightness,
            valueRange = -1F..1F,
            enabled = state.config.enableBrightness,
            onValueChange = { newValue ->

                state.brightness = newValue
            }
        )

        AdjustmentSlider(
            modifier = Modifier.fillMaxWidth(),
            label = "Exposure",
            value = state.exposure,
            valueRange = -1F..1F,
            enabled = state.config.enableExposure,
            onValueChange = { newValue ->

                state.exposure = newValue
            }
        )

        AdjustmentSlider(
            modifier = Modifier.fillMaxWidth(),
            label = "Contrast",
            value = state.contrast,
            valueRange = 0F..2F,
            enabled = state.config.enableContrast,
            onValueChange = { newValue ->

                state.contrast = newValue
            }
        )

        AdjustmentSlider(
            modifier = Modifier.fillMaxWidth(),
            label = "Saturation",
            value = state.saturation,
            valueRange = 0F..2F,
            enabled = state.config.enableSaturation,
            onValueChange = { newValue ->

                state.saturation = newValue
            }
        )

        AdjustmentSlider(
            modifier = Modifier.fillMaxWidth(),
            label = "Warmth",
            value = state.warmth,
            valueRange = -1F..1F,
            enabled = state.config.enableWarmth,
            onValueChange = { newValue ->

                state.warmth = newValue
            }
        )

        AdjustmentSlider(
            modifier = Modifier.fillMaxWidth(),
            label = "Tint",
            value = state.tint,
            valueRange = -1F..1F,
            enabled = state.config.enableTint,
            onValueChange = { newValue ->

                state.tint = newValue
            }
        )

        AdjustmentSlider(
            modifier = Modifier.fillMaxWidth(),
            label = "Highlights",
            value = state.highlights,
            valueRange = -1F..1F,
            enabled = state.config.enableHighlights,
            onValueChange = { newValue ->

                state.highlights = newValue
            }
        )

        AdjustmentSlider(
            modifier = Modifier.fillMaxWidth(),
            label = "Shadows",
            value = state.shadows,
            valueRange = -1F..1F,
            enabled = state.config.enableShadows,
            onValueChange = { newValue ->

                state.shadows = newValue
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentSlider(
    modifier: Modifier = Modifier,
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean = true,
    onValueChange: (Float) -> Unit
) {

    val valuePercentage by remember(value, valueRange) {
        derivedStateOf {

            val range = valueRange.endInclusive - valueRange.start
            val adjustedValue = value - valueRange.start
            val normalizedValue = adjustedValue / range

            ((normalizedValue * 200) - 100).roundToInt().coerceIn(range = -100..100)
        }
    }

    val sliderLabel by remember(label, valuePercentage) {
        derivedStateOf {

            val percentage = if (valuePercentage > 0) "+${valuePercentage}" else "$valuePercentage"
            "$label : $percentage%"
        }
    }

    if (enabled) {

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {

            Text(
                text = sliderLabel,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            Slider(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                thumb = { sliderState ->

                    SliderDefaults.Thumb(
                        interactionSource = remember { MutableInteractionSource() },
                        thumbSize = DpSize(
                            ButtonDefaults.IconSize,
                            ButtonDefaults.IconSize
                        )
                    )
                }
            )
        }
    }
}