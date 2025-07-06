package io.bashpsk.imagekrop.crop

enum class KropAspectRatio(val widthRatio: Int, val heightRatio: Int) {

    FreeForm(widthRatio = 0, heightRatio = 0),
    Square(widthRatio = 1, heightRatio = 1),
    Wide(widthRatio = 16, heightRatio = 9),
    Portrait(widthRatio = 3, heightRatio = 4),
    Landscape(widthRatio = 4, heightRatio = 3);
}