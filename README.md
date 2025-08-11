# Image Kolor & Image Krop for Jetpack Compose

This project provides two powerful Jetpack Compose libraries for enhancing your Android image editing capabilities:

*   **`image-kolor`**: A comprehensive library for fine-grained color adjustments and applying a wide variety of pre-defined color filter templates to your images.
*   **`image-krop`**: An intuitive and feature-rich library for image cropping, including flipping, shaping, and various aspect ratio options.

Both libraries are designed specifically for use with **Jetpack Compose**.

---

## Installation

Add the JitPack repository to your root `build.gradle` (or `settings.gradle` for newer Android Studio versions) file:

**Groovy DSL (`build.gradle`):**
```groovy
dependencyResolutionManagement {
    repositories {
        // existing code
        maven { url "https://jitpack.io" }
    }
}
```

**Kotlin DSL (`settings.gradle.kts`):**
```kotlin
dependencyResolutionManagement {
    repositories {
        // existing code
        maven("https://jitpack.io")
    }
}
```


Then, add the dependencies for the libraries you want to use in your module-level `build.gradle.kts` (or `build.gradle`) file:

**Groovy DSL (`build.gradle`):**
```groovy
dependencies {

    implementation 'com.github.bashpsk.image:image-kolor:1.0.0-beta04'
    implementation 'com.github.bashpsk.image:image-krop:1.0.0-beta04'
}
```

**Kotlin DSL (`build.gradle.kts`):**
```kotlin
dependencies {

    implementation("com.github.bashpsk.image:image-kolor:1.0.0-beta04")
    implementation("com.github.bashpsk.image:image-krop:1.0.0-beta04")
}
```

---

## `image-kolor` (Image Color Adjustments & Filters)

`image-kolor` empowers you to perform detailed color adjustments and apply stunning filters to your images within your Jetpack Compose UI.


### Features

*   **Precise Color Adjustments:**
    *   Brightness
    *   Exposure
    *   Contrast
    *   Highlights
    *   Shadows
    *   Saturation
    *   Warmth
    *   Tint
*   **Rich Set of Color Filter Templates:**
    *   Black and White
    *   Sepia
    *   Invert
    *   Grayscale
    *   Vintage
    *   Technicolor
    *   Polaroid
    *   Cool
    *   Warm
    *   High Contrast
    *   Low Contrast
    *   Brighter
    *   Darker
    *   Hue Rotate Red
    *   Hue Rotate Green
    *   Hue Rotate Blue
    *   Night Vision
    *   Kodachrome
    *   Saturate
    *   Sepia Alternative
    *   Boost Red
    *   Boost Green
    *   Boost Blue
    *   Cyanotype
    *   Moon
    *   Lomo
    *   Clarendon
*   **Jetpack Compose Integration:** Seamlessly apply adjustments and filters to your `Image` composables.
*   **Real-time Previews:** (Assuming your library supports this) See the effects of your changes instantly.


### Usage Example (`image-kolor`)

1. Color Adjustment:
```kotlin
val kolorState = rememberImageKolorState(imageBitmap = imageBitmap)

Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(space = 4.dp)
) {

    KolorImageView(
        modifier = Modifier.fillMaxWidth(),
        state = kolorState
    )

    KolorAdjustmentSliders(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        state = kolorState
    )
}
```

2. Color Filter Template:
```kotlin
val imageFilterState = rememberImageFilterState()

Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
    verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {

    Image(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16F / 9F),
        bitmap = imageBitmap,
        contentScale = ContentScale.Fit,
        colorFilter = imageFilterState.selectedFilter.colorFilter,
        contentDescription = "Image Color Filter"
    )

    ImageFilter(
        modifier = Modifier.weight(weight = 1.0F),
        state = imageFilterState
    )
}
```

3. Image Transform View - Zoom, Pan, Rotation:
```kotlin
val imageTransformState = rememberImageTransformState()

TransformImageView(
    modifier = Modifier.fillMaxWidth(),
    imageModel = { R.drawable.empty_layer },
    state = imageTransformState
)
```

---

## `image-krop` (Image Cropping UI & Functionality)

`image-krop` provides a user-friendly and customizable Jetpack Compose component for cropping images.


### Features

*   **Interactive Cropping UI:** Intuitive drag and resize handles for selecting the crop area.
*   **Image Flipping:** Support for horizontal and vertical flipping of the image within the cropper.
*   **Shape Cropping:** Crop images into various shapes (e.g., Rectangle, Star, Circle, Polygon).
*   **Aspect Ratios:**
    *   Freeform cropping
    *   Fixed aspect ratios (e.g., 1:1, 4:3, 16:9 & etc.)
*   **Customizable Overlay:** Modify the appearance of the crop colors, handles, and dimming.
*   **Bitmap Output:** Retrieve the cropped `ImageBitmap`.


### Usage Example (`image-krop`)

```kotlin
val imageKropState = rememberImageKropState(imageBitmap = imageBitmap)

ImageKrop(
    modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding(),
    state = imageKropState,
    onKropFinished = {},
    onNavigateBack = {}
)
```
---
