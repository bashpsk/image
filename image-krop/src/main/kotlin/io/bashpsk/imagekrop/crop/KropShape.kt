package io.bashpsk.imagekrop.crop

/**
 * Represents the different shapes that can be used for cropping an image.
 *
 * Each enum constant defines a specific geometric shape.
 */
enum class KropShape {

    /**
     * Represents a star shape.
     */
    Star,

    /**
     * Represents a circular shape.
     */
    Circle,

    /**
     * Represents a cut corners, typically a rectangle with its corners diagonally sliced off.
     */
    CutCorner,

    /**
     * Represents a triangle shape.
     */
    Triangle,

    /**
     * Represents a pentagon shape.
     */
    Pentagon,

    /**
     * Represents a hexagon shape for cropping.
     */
    Hexagon,

    /**
     * Represents a regular polygon with seven sides.
     */
    Heptagon,

    /**
     * Represents a regular polygon with eight sides.
     */
    Octagon,

    /**
     * Represents a regular polygon with nine sides.
     */
    Nonagon,

    /**
     * Represents a regular polygon with ten sides.
     */
    Decagon,

    /**
     * Represents a sharp corners. Its Default Shape.
     */
    SharpeCorner,

    /**
     * Represents a rounded corners.
     */
    RoundedCorner;
}