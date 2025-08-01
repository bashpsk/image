package io.bashpsk.imagekropdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.bashpsk.imagekropdemo.ui.theme.ImageKropTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            ImageKropTheme {

//                TransformImageDemoScreen()
//                ImageCropDemoScreen()
//                ImageFilterDemoScreen()
                ImageKolorDemoScreen()
            }
        }
    }
}