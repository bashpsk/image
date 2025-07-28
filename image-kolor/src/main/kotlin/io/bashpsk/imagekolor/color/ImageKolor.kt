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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

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