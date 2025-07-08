package io.bashpsk.imagekrop.crop

enum class KropShape(val label: String = "None") {

    Star(label = "Star"),
    Circle(label = "Circle"),
    CutCorner(label = "Cut Corner"),
    Triangle(label = "Triangle"),
    Pentagon(label = "Pentagon"),
    Hexagon(label = "Hexagon"),
    Heptagon(label = "Heptagon"),
    Octagon(label = "Octagon"),
    Nonagon(label = "Nonagon"),
    Decagon(label = "Decagon"),
    SharpeCorner(label = "Sharpe Corner"),
    RoundedCorner(label = "Rounded Corner");
}