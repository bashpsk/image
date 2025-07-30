package io.bashpsk.imagekrop.crop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun rememberImageKropState(
    imageBitmap: ImageBitmap,
    config: KropConfig = KropConfig(),
    shapeList: ImmutableList<KropShape> = KropShape.entries.toImmutableList()
): ImageKropState {

    return remember(imageBitmap, config) {
        ImageKropState(
            imageBitmap = imageBitmap,
            config = config,
            shapeList = shapeList,
        )
    }
}

class ImageKropState(
    val imageBitmap: ImageBitmap,
    val config: KropConfig,
    val shapeList: ImmutableList<KropShape>
) {

    var originalImage by mutableStateOf(imageBitmap)
        private set

    var modifiedImage by mutableStateOf<ImageBitmap?>(null)
        private set

    var imageList by mutableStateOf<PersistentList<ImageBitmap>>(persistentListOf())
        private set

    var kropAspectRatio by mutableStateOf(KropAspectRatio.Square)
        private set

    var isAspectLocked by mutableStateOf(false)
        private set

    var kropShape by mutableStateOf(KropShape.SharpeCorner)
        private set

    internal var kropCorner by mutableStateOf<KropCorner?>(null)
    internal var isMovingCropRect by mutableStateOf(false)
    internal var canvasSize by mutableStateOf(IntSize.Zero)
    internal var topLeft by mutableStateOf(Offset.Zero)
    internal var topRight by mutableStateOf(Offset.Zero)
    internal var bottomLeft by mutableStateOf(Offset.Zero)
    internal var bottomRight by mutableStateOf(Offset.Zero)
    internal var isAspectRatioMenuExpanded by mutableStateOf(false)
    internal var isShapeMenuExpanded by mutableStateOf(false)

    fun updateOriginalImage(bitmap: ImageBitmap) {

        originalImage = bitmap
    }

    fun updateModifiedImage(bitmap: ImageBitmap) {

        modifiedImage = bitmap
        addImage(bitmap = bitmap)
    }

    fun addImage(bitmap: ImageBitmap) {

        imageList = imageList.add(element = bitmap)
    }

    fun removeLastImage() {

        imageList.lastIndex.takeIf { index -> index >= 0 }?.let { index ->

            imageList = imageList.removeAt(index = index)
        }
    }

    fun clearImages() {

        imageList = persistentListOf()
    }

    fun updateAspectRatio(aspect: KropAspectRatio) {

        kropAspectRatio = aspect
    }

    fun updateAspectLocked(locked: Boolean) {

        isAspectLocked = locked
    }

    fun updateKropShape(shape: KropShape) {

        kropShape = shape
    }
}