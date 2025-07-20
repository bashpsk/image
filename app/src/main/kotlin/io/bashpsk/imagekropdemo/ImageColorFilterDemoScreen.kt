package io.bashpsk.imagekropdemo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.bashpsk.imagekolor.filter.ImageKolor
import io.bashpsk.imagekolor.filter.KolorFilter

@Composable
fun ImageColorFilterDemoScreen() {

    var selectedKolorFilter by rememberSaveable { mutableStateOf(KolorFilter.Original) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(space = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                modifier = Modifier.weight(weight = 1.0F),
                painter = painterResource(R.drawable.wallpaper02),
                contentScale = ContentScale.Fit,
                colorFilter = selectedKolorFilter.colorFilter,
                contentDescription = "Image Color Filter"
            )

            ImageKolor(
                modifier = Modifier.weight(weight = 1.0F),
                selectedKolorFilter = selectedKolorFilter,
                onFilterClick = { filter ->

                    selectedKolorFilter = filter
                }
            )
        }
    }
}