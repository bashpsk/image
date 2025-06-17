package io.bashpsk.imagekropdemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import io.bashpsk.imagekrop.crop.ImageKrop
import io.bashpsk.imagekrop.crop.KropResult

@Composable
fun ImageCropDemoScreen() {

    val imageBitmap = ImageBitmap.imageResource(R.drawable.wallpaper)

    var kropResult by remember { mutableStateOf<KropResult>(value = KropResult.Init) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ImageKrop(
                modifier = Modifier.wrapContentSize(),
                imageBitmap = when (kropResult) {

                    is KropResult.Init -> imageBitmap
                    is KropResult.Failed -> (kropResult as KropResult.Failed).original
                    is KropResult.Success -> (kropResult as KropResult.Success).cropped
                },
                onImageKropResult = { result ->

                    kropResult = result
                }
            )
        }
    }
}