package io.bashpsk.imagekrop.crop

enum class KropImageFlip {

    None,
    LeftToRight,
    RightToLeft,
    TopToBottom,
    BottomToTop
}

internal fun KropImageFlip.rotationAngleForHorizontal(): Float {

    return when (this) {

        KropImageFlip.LeftToRight -> 0.0F
        KropImageFlip.RightToLeft -> 180.0F
        else -> 0.0F
    }
}

internal fun KropImageFlip.rotationAngleForVertical(): Float {

    return when (this) {

        KropImageFlip.TopToBottom -> 90.0F
        KropImageFlip.BottomToTop -> 270.0F
        else -> 90.0F
    }
}