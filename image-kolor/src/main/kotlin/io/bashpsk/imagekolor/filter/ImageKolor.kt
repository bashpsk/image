package io.bashpsk.imagekolor.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.bashpsk.imagekolor.R
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ImageKolor(
    modifier: Modifier = Modifier,
    selectedKolorFilter: KolorFilter,
    onFilterClick: (filter: KolorFilter) -> Unit
) {

    val kolorFilterList = remember { KolorFilter.entries.toImmutableList() }

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 128.dp),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {

        items(
            items = kolorFilterList,
            key = { kolorFilter -> kolorFilter.name }
        ) { kolorFilter ->

            val isSelected by remember(kolorFilter, selectedKolorFilter) {
                derivedStateOf { kolorFilter == selectedKolorFilter }
            }

            KolorFilterView(
                modifier = Modifier.fillMaxWidth(),
                kolorFilter = { kolorFilter },
                imageModel = { R.drawable.flower_01 },
                isSelected = { isSelected },
                onFilterClick = onFilterClick
            )
        }
    }
}