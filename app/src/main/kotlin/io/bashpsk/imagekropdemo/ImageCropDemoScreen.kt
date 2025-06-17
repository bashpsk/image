package io.bashpsk.imagekropdemo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.imageResource
import io.bashpsk.imagekrop.crop.ImageKrop
import io.bashpsk.imagekrop.crop.KropConfig
import io.bashpsk.imagekrop.crop.KropResult
import io.bashpsk.imagekrop.view.ImageTransformData
import io.bashpsk.imagekrop.view.TransformImageView

@Composable
fun ImageCropDemoScreen() {

    val imageBitmap = ImageBitmap.imageResource(R.drawable.wallpaper)

    var isImageEdit by remember { mutableStateOf(value = false) }
    var kropResult by remember { mutableStateOf<KropResult>(value = KropResult.Init) }

    var selectedImage by remember { mutableStateOf(imageBitmap) }
    var transformData by remember { mutableStateOf(value = ImageTransformData()) }

    val handleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val targetColor = MaterialTheme.colorScheme.inverseSurface
    val borderColor = MaterialTheme.colorScheme.onErrorContainer

    val kropConfig by remember(
        handleColor,
        targetColor,
        borderColor
    ) {
        derivedStateOf {
            KropConfig(
                minimumCropSize = 300.0F,
                handleColor = handleColor,
                targetColor = targetColor,
                borderColor = borderColor
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        AnimatedVisibility(
            visible = isImageEdit,
            enter = slideInHorizontally() + fadeIn(),
            exit = slideOutHorizontally() + fadeOut()
        ) {

            ImageKrop(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding),
                imageBitmap = when (kropResult) {

                    is KropResult.Init -> imageBitmap
                    is KropResult.Failed -> null
                    is KropResult.Success -> (kropResult as KropResult.Success).cropped
                },
                kropConfig = kropConfig,
                onImageKropDone = { result ->

                    kropResult = result
                    (result as? KropResult.Success)?.cropped?.let { selectedImage = it }
                },
                onNavigateBack = {

                    isImageEdit = false
                }
            )
        }

        AnimatedVisibility(
            visible = isImageEdit.not(),
            enter = slideInHorizontally() + fadeIn(),
            exit = slideOutHorizontally() + fadeOut()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TransformImageView(
                    modifier = Modifier.weight(weight = 1.0F),
                    imageModel = { selectedImage.asAndroidBitmap() },
                    transformData = { transformData },
                    onTransformDataChange = { transform ->

                        transformData = transform
                    }
                )

                Button(
                    onClick = {

                        isImageEdit = true
                    }
                ) {

                    Text("Edit Image")
                }
            }
        }
    }
}