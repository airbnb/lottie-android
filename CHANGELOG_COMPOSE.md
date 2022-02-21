# 5.0.0
* You can tell Lottie to render the full animation even if it extends beyond the original composition bounds by setting `clipToCompositionBounds` to false.
* Add support for dynamic text via `LottieProperty.TEXT` and the existing dynamic property APIs ([#1995](https://github.com/airbnb/lottie-android/issues/1995))
* Respect Android system animator scale (include 0 for modes like battery saver) ([#2000](https://github.com/airbnb/lottie-android/pull/2000))
* Added support for loading animations via content provider URIs (LottieCompositionSpec.ContentProvider) ([#1982](https://github.com/airbnb/lottie-android/pull/1982))
* Fixed dynamic gradient properties ([#1969](https://github.com/airbnb/lottie-android/pull/1969)).
* Prevent infinite animations from hanging espresso tests by using the same mechanism as androidx animations ([#1987](https://github.com/airbnb/lottie-android/pull/1987))
* Set the merge paths property before the composition to avoid building the composition layer with the wrong merge paths value and triggering a second rebuilt ([#1997](https://github.com/airbnb/lottie-android/pull/1997))

# 4.2.0
* [BREAKING CHANGE] LottieAnimation will now default to the composition size rather than `fillMaxSize()` ([#1892](https://github.com/airbnb/lottie-android/pull/1892))
* Improved the performance of loading and parsing animations ([#1891](https://github.com/airbnb/lottie-android/pull/1891))

# 4.1.0
* No changes

# 4.0.0
* Upgrade to Compose 1.0
* Made LottieCompositionSpec an inline class([#1855](https://github.com/airbnb/lottie-android/pull/1855))

# 1.0.0-rc02-1
* Upgrade to Compose rc02
* Add support for ContentScale and Alignment just like the Image composable ([#1844](https://github.com/airbnb/lottie-android/pull/1844))
* Automatically load fonts if possible and allow setting fonts via dynamic properties ([#1842](https://github.com/airbnb/lottie-android/pull/1842))
* Add LottieCancellationBehavior support to animateLottieCompositionAsState ([#1846](https://github.com/airbnb/lottie-android/pull/1846))
* Allow custom cache keys/cache skipping ([#1847](https://github.com/airbnb/lottie-android/pull/1847))
* Always respect LottieClipSpec ([#1848](https://github.com/airbnb/lottie-android/pull/1848))


# 1.0.0-rc01-1
## Breaking Changes
`LottieAnimation` now takes a progress float instead of driving the animation internally.
The driving of animations has been split into a new `LottieAnimatable` class and
`animateLottieCompositionAsState` function. These are analogous to Jetpack's `Animatable` and
`animate*AsState` functions.
Properties that pertain to the animation such as speed, repeat count, and the new clip spec are part of
`animateLottieComposition` whereas properties that are related to rendering such as enabling merge paths
and setting an image asset delegate are on the `LottieAnimation` composable.

`lottieComposition` has also been renamed `rememberLottieComposition`.

There are overloaded version of `LottieAnimation` that merge the properties for convenience. Please
refer to the docs for `LottieAnimation`, `LottieAnimatable`, `animateLottieCompositionAsState`
and `rememberLottieComposition` for more information.
* Added the ability to clip the progress bounds of an animation.
* Added the ability to set and control dynamic properties.
* Removed the existing imageAssetDelegate parameter and moved imageAssetsFolder to rememberLottieComposition.
  Images are now loaded from assets or decoded from the base64 string embedded in the json file during parsing
  and on the IO thread pool rather than upon first render on the main thread during animations. If you want to
  supply your own animations, call `composition.images["your_image_id"].bitmap = yourBitmap`. This lets you control
  exactly how and when the bitmaps get created and set. The previous implementation of calling a callback on every
  frame encouraged the incorrect behavior of doing IO tasks during the animation hot path. Check out ImagesExamplesPage.kt
  for usage.

# 1.0.0-beta07-1
* Compatible with Jetpack Compose Beta 07

# 1.0.0-beta03-1
* Update versioning scheme to match the underlying Compose version
* Compatible with Jetpack Compose Beta 03
* Added support for images ([#1766](https://github.com/airbnb/lottie-android/pull/1766))
* Added the ability to control whether merge paths are enabled or not ([#1744](https://github.com/airbnb/lottie-android/pull/1744))
* Swapped modifier application order to follow Compose guidelines (and support setting specific sizes) ([#1765](https://github.com/airbnb/lottie-android/pull/1765))

# 1.0.0-alpha07-SNAPSHOT
* Add flag for merge paths to LottieAnimationState
* Compatible with Jetpack Compose Beta 02

# 1.0.0-alpha06
* Compatible with Jetpack Compose Alpha 12

# 1.0.0-alpha05
* Jetpack Compose Alpha 9

# 1.0.0-alpha04
* Jetpack Compose Alpha 8

# 1.0.0-alpha01
* Initial release of Lottie Compose
* Compatible with Jetpack Compose alpha 6
* Built with Lottie 3.5.0
* Wraps the existing renderer with Jetpack Compose friendly APIs.
* For up to date docs on how to use it, check out the [docs](http://airbnb.io/lottie/#/android-compose).
