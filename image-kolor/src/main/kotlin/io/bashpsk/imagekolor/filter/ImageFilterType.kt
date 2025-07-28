package io.bashpsk.imagekolor.filter

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix

enum class ImageFilterType(val label: String) {

    Original(label = "Original") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix()
    },

    BlackAndWhite(label = "Black & White") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix().apply {

                setToSaturation(0F)
            }
    },

    Sepia(label = "Sepia") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix().apply {

                val sepiaMatrixValues = floatArrayOf(
                    0.393F, 0.769F, 0.189F, 0F, 0F,
                    0.349F, 0.686F, 0.168F, 0F, 0F,
                    0.272F, 0.534F, 0.131F, 0F, 0F,
                    0F, 0F, 0F, 1F, 0F
                )

                setToSaturation(0F)
                timesAssign(ColorMatrix(sepiaMatrixValues))
            }
    },

    Invert(label = "Invert") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix(
                floatArrayOf(
                    -1F, 0F, 0F, 0F, 255F,
                    0F, -1F, 0F, 0F, 255F,
                    0F, 0F, -1F, 0F, 255F,
                    0F, 0F, 0F, 1F, 0F
                )
            )
    },

    Grayscale(label = "Grayscale") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix(
                floatArrayOf(
                    0.299F, 0.587F, 0.114F, 0F, 0F,
                    0.299F, 0.587F, 0.114F, 0F, 0F,
                    0.299F, 0.587F, 0.114F, 0F, 0F,
                    0F, 0F, 0F, 1F, 0F
                )
            )
    },

    Vintage(label = "Vintage") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix().apply {

                val vintageMatrixValues = floatArrayOf(
                    1.1F, 0F, 0F, 0F, -20F,
                    0F, 1.05F, 0F, 0F, -10F,
                    0F, 0F, 0.8F, 0F, 30F,
                    0F, 0F, 0F, 1F, 0F
                )

                setToSaturation(0.2F)
                timesAssign(ColorMatrix(vintageMatrixValues))
            }
    },

    Technicolor(label = "Technicolor") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix(
                floatArrayOf(
                    1.5F, 0F, 0F, 0F, -50F,
                    0F, 0.8F, 0F, 0F, 0F,
                    0F, 0F, 1.5F, 0F, -50F,
                    0F, 0F, 0F, 1F, 0F
                )
            )
    },

    Polaroid(label = "Polaroid") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix().apply {

                val polaroidMatrixValues = floatArrayOf(
                    1.2F, 0.1F, 0.1F, 0F, -30F,
                    0.1F, 1.2F, 0.1F, 0F, -30F,
                    0.1F, 0.1F, 1.0F, 0F, -10F,
                    0F, 0F, 0F, 1F, 0F
                )

                setToSaturation(0.5F)
                timesAssign(ColorMatrix(polaroidMatrixValues))
            }
    },

    Cool(label = "Cool") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix(
                floatArrayOf(
                    0.9F, 0F, 0F, 0F, 0F,
                    0F, 1.0F, 0F, 0F, 0F,
                    0F, 0F, 1.2F, 0F, 10F,
                    0F, 0F, 0F, 1F, 0F
                )
            )
    },

    Warm(label = "Warm") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix(
                floatArrayOf(
                    1.2F, 0F, 0F, 0F, 10F,
                    0F, 1.1F, 0F, 0F, 0F,
                    0F, 0F, 0.9F, 0F, 0F,
                    0F, 0F, 0F, 1F, 0F
                )
            )
    },

    HighContrast(label = "High Contrast") {

        override val colorMatrix: ColorMatrix
            get() {

                val factor = 1.5F
                val offset = 128F * (1F - factor)

                return ColorMatrix(
                    floatArrayOf(
                        factor, 0F, 0F, 0F, offset,
                        0F, factor, 0F, 0F, offset,
                        0F, 0F, factor, 0F, offset,
                        0F, 0F, 0F, 1F, 0F
                    )
                )
            }
    },

    LowContrast(label = "Low Contrast") {

        override val colorMatrix: ColorMatrix
            get() {

                val factor = 0.7F
                val offset = 128F * (1F - factor)

                return ColorMatrix(
                    floatArrayOf(
                        factor, 0F, 0F, 0F, offset,
                        0F, factor, 0F, 0F, offset,
                        0F, 0F, factor, 0F, offset,
                        0F, 0F, 0F, 1F, 0F
                    )
                )
            }
    },

    Brighter(label = "Brighter") {

        override val colorMatrix: ColorMatrix
            get() {

                val brightness = 50F

                return ColorMatrix(
                    floatArrayOf(
                        1F, 0F, 0F, 0F, brightness,
                        0F, 1F, 0F, 0F, brightness,
                        0F, 0F, 1F, 0F, brightness,
                        0F, 0F, 0F, 1F, 0F
                    )
                )
            }
    },

    Darker(label = "Darker") {

        override val colorMatrix: ColorMatrix
            get() {

                val darkness = -50F

                return ColorMatrix(
                    floatArrayOf(
                        1F, 0F, 0F, 0F, darkness,
                        0F, 1F, 0F, 0F, darkness,
                        0F, 0F, 1F, 0F, darkness,
                        0F, 0F, 0F, 1F, 0F
                    )
                )
            }
    },

    HueRotateRed(label = "Hue Rotate (Red)") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix(
                floatArrayOf(
                    1.2F, 0.0F, -0.2F, 0F, 0F,
                    -0.1F, 1.0F, 0.1F, 0F, 0F,
                    -0.1F, 0.2F, 0.8F, 0F, 0F,
                    0F, 0F, 0F, 1F, 0F
                )
            )
    },

    HueRotateGreen(label = "Hue Rotate (Green)") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix(
                floatArrayOf(
                    0.8F, 0.2F, 0.0F, 0F, 0F,
                    0.1F, 1.1F, -0.1F, 0F, 0F,
                    -0.2F, 0.0F, 1.2F, 0F, 0F,
                    0F, 0F, 0F, 1F, 0F
                )
            )
    },

    HueRotateBlue(label = "Hue Rotate (Blue)") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix(
                floatArrayOf(
                    0.9F, -0.1F, 0.2F, 0F, 0F,
                    0.1F, 0.8F, 0.1F, 0F, 0F,
                    0.0F, -0.2F, 1.2F, 0F, 0F,
                    0F, 0F, 0F, 1F, 0F
                )
            )
    },

    NightVision(label = "Night Vision") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix().apply {

                val nightVisionMatrixValues = floatArrayOf(
                    0.1F, 0.4F, 0.1F, 0F, -30F,
                    0.3F, 1.7F, 0.3F, 0F, -40F,
                    0.1F, 0.4F, 0.1F, 0F, -30F,
                    0F, 0F, 0F, 1F, 0F
                )

                setToSaturation(0.1F)
                timesAssign(ColorMatrix(nightVisionMatrixValues))

                val factor = 1.3F
                val offset = 128F * (1F - factor)

                val contrastValues = floatArrayOf(
                    factor, 0F, 0F, 0F, offset,
                    0F, factor, 0F, 0F, offset,
                    0F, 0F, factor, 0F, offset,
                    0F, 0F, 0F, 1F, 0F
                )

                timesAssign(ColorMatrix(contrastValues))
            }
    },

    Kodachrome(label = "Kodachrome") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix(
                floatArrayOf(
                    1.12855F, -0.39673F, -0.03992F, 0F, 0.24991F,
                    -0.16404F, 1.08352F, -0.05498F, 0F, 0.09698F,
                    -0.16786F, -0.56034F, 1.60148F, 0F, 0.35334F,
                    0F, 0F, 0F, 1F, 0F
                )
            )
    },

    Saturate(label = "Saturate") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix().apply {

                setToSaturation(1.5F)
            }
    },

    SepiaAlternative(label = "Sepia Alt") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix(
                floatArrayOf(
                    0.393F, 0.769F, 0.189F, 0F, 0F,
                    0.349F, 0.686F, 0.168F, 0F, 0F,
                    0.272F, 0.534F, 0.131F, 0F, 0F,
                    0F, 0F, 0F, 1F, 0F
                )
            )
    },

    BoostRed(label = "Boost Red") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix().apply {

                this[0, 0] = 1.5F
            }
    },

    BoostGreen(label = "Boost Green") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix().apply {

                this[1, 1] = 1.5F
            }
    },

    BoostBlue(label = "Boost Blue") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix().apply {

                this[2, 2] = 1.5F
            }
    },

    Cyanotype(label = "Cyanotype") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix().apply {

                val cyanotypeMatrixValues = floatArrayOf(
                    0.1F, 0.5F, 1.0F, 0F, 0F,
                    0.1F, 0.4F, 0.8F, 0F, 0F,
                    0.0F, 0.3F, 0.6F, 0F, 0F,
                    0F, 0F, 0F, 1F, 0F
                )

                setToSaturation(0F)
                this[0, 4] = 10F
                this[1, 4] = 20F
                this[2, 4] = 30F
                timesAssign(ColorMatrix(cyanotypeMatrixValues))
            }
    },

    Moon(label = "Moon") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix().apply {

                val moonMatrixValues = floatArrayOf(
                    0.8F, 0.1F, 0.1F, 0F, -20F,
                    0.1F, 0.7F, 0.1F, 0F, -20F,
                    0.2F, 0.2F, 1.0F, 0F, 10F,
                    0F, 0F, 0F, 1F, 0F
                )

                setToSaturation(0.1F)
                timesAssign(ColorMatrix(moonMatrixValues))
            }
    },

    Lomo(label = "Lomo") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix().apply {

                val lomoMatrixValues = floatArrayOf(
                    1.7F, 0.1F, 0.1F, 0F, -73.1F,
                    0F, 1.7F, 0.1F, 0F, -73.1F,
                    0F, 0.1F, 1.6F, 0F, -73.1F,
                    0F, 0F, 0F, 1F, 0F
                )

                timesAssign(ColorMatrix(lomoMatrixValues))

                val contrast = 1.2F
                val scale = contrast + 1F
                val translate = (-.5F * scale + .5F) * 255F

                val contrastMatrixValues = floatArrayOf(
                    scale, 0F, 0F, 0F, translate,
                    0F, scale, 0F, 0F, translate,
                    0F, 0F, scale, 0F, translate,
                    0F, 0F, 0F, 1F, 0F
                )

                timesAssign(ColorMatrix(contrastMatrixValues))
            }
    },

    Clarendon(label = "Clarendon") {

        override val colorMatrix: ColorMatrix
            get() = ColorMatrix().apply {

                setToSaturation(1.35F)

                val contrastValue = 1.2F
                val scale = contrastValue
                val translate = (-.5F * scale + .5F) * 255F

                val contrastMatrixValues = floatArrayOf(
                    scale, 0F, 0F, 0F, translate,
                    0F, scale, 0F, 0F, translate,
                    0F, 0F, scale, 0F, translate,
                    0F, 0F, 0F, 1F, 0F
                )

                timesAssign(ColorMatrix(contrastMatrixValues))

                val cyanTintMatrixValues = floatArrayOf(
                    0.9F, 0F, 0F, 0F, 5F,
                    0F, 1.1F, 0F, 0F, 5F,
                    0F, 0F, 1.25F, 0F, 10F,
                    0F, 0F, 0F, 1F, 0F
                )

                timesAssign(ColorMatrix(cyanTintMatrixValues))
            }
    };

    internal abstract val colorMatrix: ColorMatrix

    val colorFilter: ColorFilter by lazy { ColorFilter.colorMatrix(colorMatrix = colorMatrix) }
}