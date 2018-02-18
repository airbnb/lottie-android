# 2.5.0
* Added the ability to dynamically change properties at runtime. See [docs](http://airbnb
.io/lottie/android/dynamic.html) for more info. This feature removed the existing APIs for 
changing the color dynamically with a color filter. Refer to the docs for migration info.
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
