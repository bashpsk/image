package io.bashpsk.imagekrop.crop

/**
 * Represents predefined aspect ratios for image cropping.
 *
 * Each enum constant defines a specific width to height ratio.
 *
 * @property width The width component of the aspect ratio. Defaults to 1.
 * @property height The height component of the aspect ratio. Defaults to 1.
 * @property ratio The calculated float value of the aspect ratio (width / height). This can be null
 * if not explicitly defined.
 */
internal enum class KropAspectRatio(
    val width: Int = 1,
    val height: Int = 1,
    val ratio: Float? = null
) {

    /**
     * Represents a 1:1 aspect ratio (Square).
     */
    Square(width = 1, height = 1, ratio = 1F / 1F),

    /**
     * Represents a 3:4 aspect ratio (Landscape).
     */
    FourByThree(width = 4, height = 3, ratio = 4F / 3F),

    /**
     * Represents a 3:4 aspect ratio (Portrait).
     */
    ThreeByFour(width = 3, height = 4, ratio = 3F / 4F),

    /**
     * Represents a 16:9 aspect ratio (Landscape).
     */
    SixteenByNine(width = 16, height = 9, ratio = 16F / 9F),

    /**
     * Represents a 9:16 aspect ratio (Portrait).
     */
    NineBySixteen(width = 9, height = 16, ratio = 9F / 16F);
}