package io.bashpsk.imagekolor.filter

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.bashpsk.imagekolor.R

@Composable
fun KolorFilterView(
    modifier: Modifier = Modifier,
    kolorFilter: () -> ImageKolorFilter,
    @DrawableRes
    imageModel: () -> Int = { R.drawable.image_broken },
    isSelected: () -> Boolean = { false },
    contentScale: ContentScale = ContentScale.Crop,
    borderWidth: Dp = if (isSelected()) 2.dp else 0.2.dp,
    borderColor: Color = MaterialTheme.colorScheme.error,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    onFilterClick: (filter: ImageKolorFilter) -> Unit
) {

    val borderModifierUnselected = Modifier.border(
        width = borderWidth,
        color = borderColor.copy(alpha = 0.40F),
        shape = shape
    )

    val borderModifierSelected = Modifier.border(
        width = borderWidth,
        color = borderColor,
        shape = shape
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isSelected()) borderModifierSelected else borderModifierUnselected)
            .clip(shape = shape),
        shape = shape,
        onClick = {

            onFilterClick(kolorFilter())
        }
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {

            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = imageModel(),
                contentScale = contentScale,
                colorFilter = kolorFilter().colorFilter,
                contentDescription = kolorFilter().label
            )

            Text(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.65F),
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = kolorFilter().label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.inverseSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}