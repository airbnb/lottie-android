# 3.5.0
### Features and Improvements
* Added a new global configuration to add a custom network stack, custom network cache, enable systrace markers, and more ([#1629](https://github.com/airbnb/lottie-android/pull/1629))

  * To use it, checkout the docs for `Lottie.initialize` and `LottieConfig.Builder`.
* Add support for parsing dotLottie files ([#1660](https://github.com/airbnb/lottie-android/pull/1660))
* Added support for pause listeners on `LottieDrawable` and `LottieAnimationView` ([#1662](https://github.com/airbnb/lottie-android/pull/1662))
### Bugs Fixed
* Properly cache animations loaded via url in memory ([#1657](https://github.com/airbnb/lottie-android/pull/1657))

# 3.4.4
### Bugs Fixed
* Properly clamp gradient values ([#1636](https://github.com/airbnb/lottie-android/pull/1636))
* Fix some scaling issues with text ([#1635](https://github.com/airbnb/lottie-android/pull/1635))
* Add a warning instead of crashing when parsing an unknown matte type ([#1638](https://github.com/airbnb/lottie-android/pull/1638))
* Clear cached gradients when setting a new value callback ([#1639](https://github.com/airbnb/lottie-android/pull/1639))

# 3.4.2
### Feature and Improvements
* Disable hardware acceleration by default on Android 7.x ([#1586](https://github.com/airbnb/lottie-android/pull/1586))
* Enable Lottie animations to preview in Android Studio (they may not be accurate, though)
([#1572](https://github.com/airbnb/lottie-android/pull/1572))
* More leniently parse opacity and colors to render Telegram stickers better ([#1612](https://github.com/airbnb/lottie-android/pull/1612) and [#1613](https://github.com/airbnb/lottie-android/pull/1612))
* Use the correct cacheKey when LottieAnimationView loads an rawRes animation ([#1617](https://github.com/airbnb/lottie-android/pull/1617))
* Prevent animations from blinking if they are rendered on multiple threads ([#1575](https://github.com/airbnb/lottie-android/pull/1575))


# 3.4.1
### Feature and Improvements
* Added a KeyPath.COMPOSITION constant to set dynamic properties on the animation's root composition layer ([#1559](https://github.com/airbnb/lottie-android/pull/1559)).
* A default style can now be set for all AnimationViews with lottieAnimationViewStyle ([#1524](https://github.com/airbnb/lottie-android/pull/1524)).

# 3.4.0
### Features and Improvements
* Added optional cache key parameters to url loading to enable skipping the cache.
* Added the ability to clear the Lottie cache via `LottieCompositionFactory.clearCache()`.

### Bugs Fixed
* Properly pass in progress to ValueCallbacks.
* Clear existing ValueCallbacks if new ones overwrite old ones.
* Clip interpolators that might loop back on themself to render something very close rather than crashing.
* Fix time stretch  + time remap when there is a start offset.
* Ensure that the first frame is rendered when a new composition is set even if it is not yet playing.
* Properly render Telegram stickers that use [0,1] for color but [0,255] for alpha.
* Ensure that LottieDrawable has the correct bounds when the composition updates before Marshmallow.
* Fully clear off screen buffers pre-pie to prevent artifacts.
* Play, not resume animations if they are played while not shown.

# 3.3.1
### Bugs Fixed
* Clear alpha values when applying a mask or matte

# 3.3.0
### Features and Improvements
* Added a safeMode API that wraps draw with a try/catch. Can be used for problematic devices
 ([#1449](https://github.com/airbnb/lottie-android/pull/1449)).
* Add the ability to skip composition caching ([#1450](https://github.com/airbnb/lottie-android/pull/1450)).
* Add support for mask mode none.
* Add an API to set the min and max frame from 2 markers.
* Add support for TEXT_SIZE as a dynamic property for text layers.
### Bugs Fixed
* Improve the performance of setProgress, particularly for animations with many non-animated
 properties.
 * Fix a bug where animations may not resume on reattach if their state was saved after they were
  detached.

# 3.2.2
# Bugs Fixed
* Fixed two potential NPEs.

# 3.2.0
### Feature and Improvements
* Added support for FIT_XY scale type.
### Bugs Fixed
* Improved testability while system animations are disabled.

# 3.1.0
### Features and Improvements
* **Breaking Change** Replace JsonReader parsing APIs with InputStream variants to prevent
exposing Lottie's copy of Moshi's json parser.
* Add the ability to catch all Lottie composition errors with `setFailureListener` and
`resetFailureListener` (#1321).
* Add the ability to set a fallback drawable res when Lottie fails to parse a composition or
load it from the internet. Use `setFallbackResource` from code or`lottie_fallbackRes` from xml.
* Add the ability to render opacity on the layer level rather than the shape level. View the docs
 for `setApplyingOpacityToLayersEnabled` for more info.
* Add the ability to use dynamic properties on text that wasn't already animated.
* Minor performance optimization when interpolating between colors.
## Bugs Fixed
* Fix the renderMode attribute from getting overwritten.
* Prevent masks from either clipping edges or having thin borders pre-Pie.
* Apply animation scale to dash pattern offsets.
* Apply animation scale to gradient strokes.
* Fuzzy match content types when downloading animations from the internet.
* Prevent a StackOverflowException on KitKat.
* Prevent resume() from resuming when system animations are disabled.
* Prevent removeAllUpdateListeners() from removing internally used listeners.
* Fix some time remap calculations.

# 3.0.7
* Fixed renderMode XML attr being ignored.
* Allow progress to be set in between frames.
* Fix a NullPointerException on 5.x.x devices for apps that use Proguard.

# 3.0.6
### Bugs Fixed
* Fixed another LottieAnimationView visibility bug.

# 3.0.5
### Bugs Fixed
* Fixed a native crash on Nougat.
* Improved the performance of animations that have masks and mattes that are partially or fully off screen.


# 3.0.4
### Bugs Fixed
* Use a copy of [Moshi's](https://github.com/square/moshi) JsonReader implementation to fix [#667](https://github.com/airbnb/lottie-android/issues/667).
* Fix animations not autoplaying when they became visible on pre-marshmallow devices.
* Fix PerformanceTracker#removeFrameListener not working.

# 3.0.3
### Bugs Fixed
* Prevent network connections from being closed before parsing is finished.
* invalidateSelf() after settings alpha.
* Set the correct frame when animations end but speed is < 0.
* Default missing content types to application/json.
* Consistently use frameTimeNanos to prevent animation frame time from being < 0.

# 3.0.2
### Features and Improvements
* Zipped animations with images now support WebP.

### Bugs Fixed
* Use frameTimeNanos LottieAnimator.
* Set wasAnimatingWhenDetached to false at pause().

# 3.0.1
### Bugs Fixed
* Fixed an edge case that would resume an animation when it is not shown.
* Disable animations when the system animation scale is 0.
* Fall back to hardware rendering when the animation is too large to create a drawing cache.

# 3.0.0
### Features and Improvements
* **Significant** mask and matte performance improvements by only calling saveLayer() on the intersection bounds of the content and mask/matte.
* Added support for dynamic properties on rectangles, gradient colors, and gradient fill opacity.
* Added support for inverted and intersect masks.
* Improved support for multiple masks per layer.
* Added support for optimized bodymovin json with static transforms removed (v5.5.0+).
* Added support for optimized bodymovin json that will omit duplicated vertex out points to reduce json file size (v5.5.0+).
* Added support for centered, multiline text, and emojis.
* Added support for masked text.
* Added support for skew and skew angle in transforms.
* Added support for markers. You can now call `setMinFrame`, `setMaxFrame` and `setMinAndMaxFrame` with a marker name.
* Added support for hidden layers and properties (the eye button in After Effects).
* Added support for multiple trim paths to be applied on a shape.
* Removed **all** memory allocations during playback including autoboxing.
* Replaced `enableHardwareAcceleration` with a new `setRenderMode` API because it has a third (`Automatic`) option. Refer to the docs for more info.
* Added an XML attr for animation speed (lottie_speed).
* Removed the recycleBitmaps() API because it is not neccesary anymore.
* Prevented `invalidateSelf()` from being called and recalculating bounds many times per frame.
### Bugs Fixed
* Only redraw the animation when a value changed. This will have a major impact on animations that are static for part of their playback.
* Optimized keyframes to recalculate values less frequently
* Optimized static identity transforms so their matrix doesn't get recalculated on every frame.
* Ensure that the last frame is played when setMaxFrame is called.
* Prevent strokes from drawing when the scale is 0.
* Prevented minFrame from being larger than maxFrame.
* Return the correct (previous) bitmap when updating the BitmapAssetManager.
* Properly use the in-memory cache for network animations.
* Prevented color animations from interpolating before/after the start/end colors even if their interpolator goes <0 or >1.
* Annotate `fetchBitmap()` as `@Nullable`
* Fixed a bug in the local file cache that would save it with the wrong extensions.
* Fixed a crash when an animation was missing gradient fill type.
* Prevent shapes that have different numbers of control points in different keyframes from crashing.
* Fixed an IndexOutOfBoundsException.
* Pause Lottie in onVisibilityChanged.
* Properly limited the LRU cache an enable its maximum size to be configured.
* Prevented the cache from returning null values after a key was cleared.
* Properly closed JsonReader in all cases.
* Fixed text alignment for scaled text when drawn using fonts.
* Use FutureTask rather than polling for composition parsing to complete.
* [Sample App] Fixed Lottiefiles integration.

# 2.8.0
### Features and Improvements
* Migrated to androidx. This release and all future releases are only compatible with projects that have been migrated to androidx.

# 2.7.0
### Features and Improvements
* Removed deprecated LottieCompositionFactory APIs. If you were using JsonObjects, switch to Strings (#959).
* Made LottieTask.EXECUTOR public and static so tests can set it.
* Allow layer names to be stripped from JSON if desired.
* Allow returning null from LottieValueCallbacks to fall back to the default value.
### Bugs Fixed
* Allow text stroke width to be a double (#940).

# 2.6.0
### Features and Improvements
* Added support for loading an image from a url directly. See LottieCompositionFactory for more information.
* Added support for loading an animation from a zip file that contains the json as well as images.
    * URLs supports zip files as well as json files.
* Deprecated `LottieComposition.Factory` in favor of LottieCompositionFactory.
    * The new factory methods make it easier to catch exceptions by separating out success and
      failure handlers. Previously, catching exceptions was impossible and would crash your app.
    * All APIs now have a mandatory cacheKey that uses an LRU cache rather than a strong/weak ref cache.
    * If the same animation is fetched multiple times in parallel, the same task will be returned.
      This will be massively helpful for animations that are loaded in a list.
    * InputStreams are now always closed even if you use the old APIs. Please be aware if you were
      using this while upgrading.
* Added support for miter limit.
* [Sample App] Added the ability to load a file from assets.
### Bugs Fixed
* Fixed a timing issue when there was time stretch on a masked layer.
* Fixed support for Android P.
* Make a best-effort attempt at rendering shapes in which the number of vertices changes rather than crashing.
* Fixed a bug in which the inner radius animation of a polystar wouldn't update.

# 2.5.7
* Reapply min/max frame once composition is loaded (#827).
* Fixed a bug that would ignore setting minFrame to 0 before the composition was set (#820).
* Prevented Lottie from drawing a recycled bitmap (#828).

# 2.5.6
* Added support for targeting Android P
* Fixed a potential dangling Choreographer callback ([#775](https://githubcom/airbnb/lottie-android/pull/775))

# 2.5.5
* Fixed end times for layers/animations. Before, if the layer/animation out frame was 20, it would fully render frame 20. This is incorrect. The last rendered frame should be 19.999... in this case. This should make Lottie reflect After Effects more accurately. However, if you are getting the frame in onAnimationEnd or onAnimationRepeat, it will be one less than it used to be.
* Added support for base64 encoded images directly in the json instead of the filename. They are 33% larger than their equivalent image file but enables you to have images with a single file.
* Fixed a lint error about KeyPath visibility.
* A few min/max progress bug fixes.
* Prevent autoPlay from starting before the animation was attached to the window. This caused animations in a RecyclerView to start playing before they were on screen.

# 2.5.4
* You can now call playAnimation() from onAnimationEnd
* Min/Max frames are clipped to the composition start/end
* setProgress takes into account start and end frame


# 2.5.2
### Features and Improvements
* Totally new sample app!
    * Rebuilt from the ground up.
    * Lottiefiles integration
    * Render times per layer
    * Can open zip files with images from lottiefiles, even with qr scanning.
    * Change speed
# Bugs Fixed
* Fixed a regression with ellipse direction

# 2.5.1
### Features and Improvements
* Removed framerate restriction introduced in 2.5.0 that caused Lottie to attempt to render at the After Effects framerate. This caused animations to appear unexpectedly janky in most cases.
### Bugs Fixed
* Many minor bug fixes around setting min/max frames
* Removed @RestrictTo on LottieValueCallback
* Improved thread safety of animation listeners
* Fixed looping when the animation speed is reversed


# 2.5.0
### Features and Improvements
* Added the ability to dynamically change properties at runtime. See [docs](http://airbnb.io/lottie/android/dynamic.html) for more info. This feature removed the existing APIs for
changing the color dynamically with a color filter. Refer to the docs for migration info from
existing ColorFilter APIs.
* Added a setRepeatMode and setRepeatCount (Thanks Fabio Nuno!).
* Completely overhauled json deserialization. Deserializing a composition takes half as long and
can deserialize much larger json files (tested 50mb) without ooming.
* Overhauled the underlying time animator. It now:
    * More accurately handles setFrame/getFrame/minFrame/maxFrame APIs. There were cases where they
    could be off by one before.
    * Renders at the fps specified by After Effects.
    * Added docs and clearer rules around animatedValue and animatedFraction in animator callbacks.
* API to remove all animator listeners.
* Adhere to the Animatable interface.
* Bumped the minSdk from 14 to 16 to use Choreographer in the animator mentioned above.
### Bugs Fixed
* Fixed a bug that made it difficult to chain animations in onAnimationEnd callbacks.
* Fixed a regression with unknown masks modes.
* Fixed a crash trying to recycle a null bitmap.
* Fixed a bug when an opacity animation time interpolator was >1.

# 2.3.1
### Features and Improvements
* Expose `LottieComposition#getImages()` to aid in preloading images.
* Added support for text baseline.
### Bugs Fixed
* Prevented a crash when setting min frame > previous max frame.
* Fixed some bugs in subtract masks.
* Fixed some animation clamping when an animation was longer than its parent and time stretched.
* Stopped applying time stretch to a layer transform.


# 2.3.0
### Features and Improvements
* Animator fixes:
    * Previously, some usages of lottie animator apis/api listeners would cause unexpected
    behavior, especially calling apis from listener callbacks.
    * This is breaking change if you use `playAnimation(start, end)`. It has been removed in
    favor of explicit methods for `setMinFrame/Progress`, `setMaxFrame/Progress` and
    `setMinAndMaxFrame/Progress` followed by an explicit call to `playAnimation` or
    `resumeAnimation`.
    * `reverseAnimation` and `resumeReverseAnimation` apis have been removed in favor of
    `play` and `resume` with `speed` < 0.
    * If you have created hack around these limitations or complex animator chaining, please test
     your animations after updating.
* Set an animation from R.raw (res/raw) if you want static references to your animation files.
This can help prevent mismatches between api calls and file names. Thanks @cyrilmottier!
* Support for ellipse direction.
* Expose image directory name if set from bodymovin.
### Bugs Fixed
* Fixed a bug with animations that use both mattes and time stretch.
* Fixed a few keyframe issues where keyframe start/end values would be off by 1 frame.

# 2.2.5
### Bugs Fixed
* Call onAnimationEnd when system animations are disabled.

# 2.2.4
### Bugs Fixed
* Improved the reliability of scaling and using ImageView scale types.
* Fixed a clipping issue with precomps.
* Fixed an ArrayIndexOutOfBounds crash in the interpolator cache.

# 2.2.3
### Bugs Fixed
* Fixed some issues with progress and resume
* Fixed a StackOverflowErrors when animations are disabled
* Fixed a NPE in Keyframe.Factory

# 2.2.1
### Features and Improvements
* Text now supports opacity.
### Bugs Fixed
* Fixed a couple of couple of concurrency crashes.
* Fixed a crash when animations are disabled.
* Fixed a crash for letters with no shapes.


# 2.2.0
### Features and Improvements
* Added `play(startFrame, endFrame)` and `play(startProgress, endProgress)`.
* Added the ability to set a minimum and maximum frame/progress for a given animation.
* Made WeakRef the default cache strategy.
### Bugs Fixed
* Cancel loading animations when non-Lottie animations are set.

# 2.1.2
### Bugs Fixed
* Reduced memory footprint.
* Allowed animations with masks and mattes to scale larger than their view.
* Respect hardware acceleration when async loading a composition.
* Fixed a gradient point counting issue.
* Fixed a trim path issue.

# 2.1.0
### Features and Improvements
* Added support for animated text (glyphs and fonts)
* Enabled text to be set dynamically
* Added support for repeaters
* Added support for time stretching
* Added support for work area (trimming start and end frames)
* Added support for mask opacity
* Migrated the sample app to Kotlin
* Added a real time render performance graph in the sample app
* Added many systrace markers to improve performance tracking

# 2.0.0
### Features and Improvements
* Increased version number.
* New animations from lottiefiles in the sample app.
### Bugs Fixed
* Minor trim path fix.
* Minor gradient caching fix.

# 2.0.0-rc2
### Features and Improvements
* Paste json into the sample app.
* Update a bitmap asset dynamically.
* Set scale from xml.
### Bugs Fixed
* Resume no longer restarts the animation.
* More lenient crashes and getCallback() checks.
* Fewer addUpdateListener calls.
* Fix cache strategy when it and the animation is set from xml.

# 2.0.0-rc1
### Features and Improvements
* Set a color filter with xml
#### Bugs Fixed
* Prevent a rare crash when used from React Native.
* Apply transformation to gradient fills.
* Clip precomps to their bounds.
* Prevent a crash with certain extreme keyframe values.
* Fix the sample app for pre-lollipop.
* Fix some pre-lollipop rendering bugs.


# 2.0.0-beta4
### Features and Improvements
* Added support to load an animation by scanning a qr code with a url to the json. All
lottiefiles.com animations now have qr codes.
* Added support for opacity stops in gradients.
* Exposed setScale to arbitrarily scale up or down an animation. Added a slider in Lottie Sample
to try it.

#### Bugs Fixed
* Fixed several subtle trim path and dash pattern bugs.
* Fixed a bug with path animations on Jelly Bean.
* Fixed a bug that would incorrectly draw rounded rectangles.

# 2.0.0-beta3
#### Features and Improvements
* Ground up rewrite of the rendering engine.
* Vastly improved the performance of masks and mattes.
* Support for fill types (non-zero or even-odd).
* Support for gradient fills.
* Some merge path support. Enable it with `enableMergePathsForKitKatAndAbove` and read the
documentation before using it.
* Support for multiple fills and strokes on shapes.
* Exposed several package private methods on `LottieAnimationView` and `LottieDrawable`
* Better support for hardware acceleration.
* Added a ton of samples from lottiefiles.com to the sample app.

#### Bugs Fixed
* Invalidate whatever drawable is set as the image drawable even if it's not LottieDrawable.

# 1.5.3
#### Features and Improvements
* Added an image asset delegate so you can provide your own bitmaps from and sd card, for example.
* Added an attr for setting progress.
* Improved the performance of very large compositions by scaling them down to the screen size.
* Separate APIs for play/cancel with or without setting progress.

#### Bugs Fixed
* Several issues that could cause graphical corruption in masks or mattes.
* LottieAnimationView checks whether it's current drawable is LottieDrawable for some calls.


# 1.5.2
#### Features and Improvements
* Significantly improved memory usage when using masks or mattes. No more bitmaps!
#### Bugs Fixed
* Further improved the compatibility of masks and mattes.

# 1.5.1
##### Features and Improvements
* Use a thread pool executor for deserialization.
* Allow setting a default cache strategy
* Drop repeated calls to setComposition with the same composition.
##### Bugs Fixed
* Fixed an image scaling issue.
* Fixed a crash when leaving a screen with an image animation.
* Fixed a crash when the json has no assets.

# 1.5.0
* Precomps.
* 60% performance and memory improvement for masks and mattes.
* Images support. See documentation for more information.
* Polystars.
* Polygons.
* Alpha inverted masks.
* Subtract masks.
* Trim paths on rectangles.
* Variable speed (positive or negative).
* Improved color interpolation (rgb -> gamma color space).

# 1.0.3
* Make `LottieDrawable` public again.
* Apply trim paths to shape fills.
* Expose reverseAnimation in addition to playAnimation.

# 1.0.2
* Added support for split dimension positions.
* Fixed a crash with decimal opacity values.
* Allow trim paths to rotate indefinitely.
* Lowered the minSdk to 14 (ICS).
* Prevent multiple caches from created on different threads.
* Allow animations to show final state when system animations are disabled.

# 1.0.1 (2/2/2017)
* Fixes an issue in which a parent layer will overwrite child alpha.
* Fixes drawable invalidation when `LottieDrawable` is used outside of `LottieAnimationView`

# 1.0 (01/30/2017)

* Initial release
