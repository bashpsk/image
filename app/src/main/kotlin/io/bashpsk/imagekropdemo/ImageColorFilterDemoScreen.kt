package io.bashpsk.imagekropdemo

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import io.bashpsk.imagekolor.filter.ImageKolor
import io.bashpsk.imagekolor.filter.KolorFilter
import io.bashpsk.imagekolor.filter.getKolorFilterBitmap
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageColorFilterDemoScreen() {

    val context = LocalContext.current
    val bitmapCoroutineScope = rememberCoroutineScope()

    val imageBitmap = ImageBitmap.imageResource(R.drawable.wallpaper02)
    var selectedKolorFilter by rememberSaveable { mutableStateOf(KolorFilter.Original) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {

            TopAppBar(
                title = {

                    Text(text = "Image Kolor")
                },
                actions = {

                    IconButton(
                        onClick = {

                            bitmapCoroutineScope.launch {

                                imageBitmap.getKolorFilterBitmap(
                                    filter = selectedKolorFilter
                                ).saveAsFile(name = "PSK-Colored").let { file ->

                                    Toast.makeText(
                                        context,
                                        if (file?.exists() == true) "Image Saved" else "Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = "Done"
                        )
                    }
                }
            )
        }
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
                bitmap = imageBitmap,
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