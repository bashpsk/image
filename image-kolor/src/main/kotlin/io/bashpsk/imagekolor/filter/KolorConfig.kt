package io.bashpsk.imagekolor.filter

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class KolorConfig(
    val previewSize: Dp = 240.dp,
)