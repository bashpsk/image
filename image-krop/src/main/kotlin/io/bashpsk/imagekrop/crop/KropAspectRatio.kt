package io.bashpsk.imagekrop.crop

internal enum class KropAspectRatio(
    val width: Int = 1,
    val height: Int = 1,
    val ratio: Float? = null
) {

    Square(width = 1, height = 1, ratio = 1F / 1F),
    FourByThree(width = 4, height = 3, ratio = 4F / 3F),
    ThreeByFour(width = 3, height = 4, ratio = 3F / 4F),
    SixteenByNine(width = 16, height = 9, ratio = 16F / 9F),
    NineBySixteen(width = 9, height = 16, ratio = 9F / 16F);
}