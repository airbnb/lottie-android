# 6.6.0
### New Features
* Overhauled drop shadow support for even better correctness ([#2548](https://github.com/airbnb/lottie-android/pull/2548))
  * Major thanks to David Davidović (who works at [Lottielab](https://www.lottielab.com/)) for his contributions here.
  * Adds a new API: `applyShadowsToLayers` which will apply shadows to a whole layer rather than an individual shape.
  * Use RenderNodes on APIs that support it.
  * Lays a foundation that can potentially add RenderNode support to existing mattes and masks.
  * Fixed a number of existing correctness issues
* Add an overload to LottieCompositionFactory.clearCache that clears the network cache ([#2559](https://github.com/airbnb/lottie-android/pull/2559/))

### Bugs Fixed
* Fix rounded corners when the underlying shape doesn't animate ([#2567](https://github.com/airbnb/lottie-android/pull/2567/))
* Fix animation update listener order to be called after the internal frame has updated ([#2497](https://github.com/airbnb/lottie-android/pull/2497/))
* Unify all parsing to more reliably handle all file types including dotLottie as ZIP files ([#2558](https://github.com/airbnb/lottie-android/pull/2558/))
* Fix keypath resolving for matte layers ([#2544](https://github.com/airbnb/lottie-android/pull/2544/))
* Apply translate before scale in LottieDrawable ([#2565](https://github.com/airbnb/lottie-android/pull/2565/))

# 6.5.2
### Bugs Fixed
* Fix a NPE when running Lottie in instrumentation tests ([#2546](https://github.com/airbnb/lottie-android/pull/2546))
* Add support for new reduced motion options in Compose ([#2542](https://github.com/airbnb/lottie-android/pull/2542))

# 6.5.1
### Bugs Fixed
* Fix value callback is not called for PathKeyframeAnimation ([#2540](https://github.com/airbnb/lottie-android/pull/2540))
* Fix shadow softness accuracy ([#2541](https://github.com/airbnb/lottie-android/pull/2541))
* Add a global configuration to control reduced motion settings for a11y ([#2536](https://github.com/airbnb/lottie-android/pull/2536))
* Fix a NPE on ImageAssetManager#getContext ([#2532](https://github.com/airbnb/lottie-android/pull/2532))
* Improve strokes with skew ([#2531](https://github.com/airbnb/lottie-android/pull/2531))
* LottieCompositionFactory: Avoid NPE when animation contains a Font Family and Context is null ([#2530](https://github.com/airbnb/lottie-android/pull/2530))
* LottieCompositionFactory: Add factory methods that take an okio Source ([#2527](https://github.com/airbnb/lottie-android/pull/2527))
* LottieCompositionFactory#fromZipStreamSyncInternal close FileOutputStream ([#2548](https://github.com/airbnb/lottie-android/pull/2548))

# 6.5.0
### New Features
* Apply blend modes to layers and add Multiply ([#2519](https://github.com/airbnb/lottie-android/pull/2519))
* Add initial support for text range selectors ([#2518](https://github.com/airbnb/lottie-android/pull/2518))
* Add a new feature flag API to allow for opting into newer platform features ([#2512](https://github.com/airbnb/lottie-android/pull/2512))
* Add an API to get the unscaled width/height of a composition ([#2514](https://github.com/airbnb/lottie-android/pull/2514))

### Bugs Fixes
* Improve the accuracy of drop shadow position and softness ([#2523](https://github.com/airbnb/lottie-android/pull/2523))
* Treat appcompat as an API dependency ([#2507](https://github.com/airbnb/lottie-android/pull/2507))

# 6.4.1
### Bugs Fixed
* Scale base64 encoded bitmaps ([#2501](https://github.com/airbnb/lottie-android/pull/2501))
* Prevent systrace strings from getting created when systrace is off ([#2493](https://github.com/airbnb/lottie-android/pull/2493))
* Allow missing end values for integer animations ([#2487](https://github.com/airbnb/lottie-android/pull/2487))
* Add an extra null check in BaseKeyframeAnimation ([#2486](https://github.com/airbnb/lottie-android/pull/2486))

# 6.4.0
### New Features
* Add support for reduced motion marker names ([#2451](https://github.com/airbnb/lottie-android/pull/2451))
* Support GZIP and TGS network downloads ([#2454](https://github.com/airbnb/lottie-android/pull/2454))
### Bugs Fixed
* Allow easings to go <0 and >1 ([#2457](https://github.com/airbnb/lottie-android/pull/2457))
* Fix a memory leak in LottieTask ([#2465](https://github.com/airbnb/lottie-android/pull/2465))
* Prevent play from working after a non-Lottie drawable was set and then returned back ([#2468](https://github.com/airbnb/lottie-android/pull/2468))
* Respect autoPlay in LottieAnimationView when setting a new composition ([#2469](https://github.com/airbnb/lottie-android/pull/2469))
* Call LottieTask synchronously when already on the main thread ([#2470](https://github.com/airbnb/lottie-android/pull/2470))
* Properly rescale bitmaps when the system scale changes ([#2475](https://github.com/airbnb/lottie-android/pull/2475))

# 6.3.0
### New Features
* Add support dynamic path properties on shape contents ([#2439](https://github.com/airbnb/lottie-android/pull/2439))
* Add support for gzipped and tgs files ([#2435](https://github.com/airbnb/lottie-android/pull/2435))
* Add an option to clip text if it extends beyond its bounding box ([#2412](https://github.com/airbnb/lottie-android/pull/2412))

### Bugs Fixed
* Make all LottieAnimationView setters idempotent ([#2441](https://github.com/airbnb/lottie-android/pull/2441))
* Fix a rendering artifact for polygons with large strokes ([#2440](https://github.com/airbnb/lottie-android/pull/2440))
* Re-scale bitmaps if the system scale changes ([#2438](https://github.com/airbnb/lottie-android/pull/2438))
* Handle null color callbacks in solid layer ([#2434](https://github.com/airbnb/lottie-android/pull/2434))
* Handle null shape data end values ([#2433](https://github.com/airbnb/lottie-android/pull/2433))
* Fix gradient colors when the progress is <0 or > 1 ([#2427](https://github.com/airbnb/lottie-android/pull/2427))

# 6.2.0
### New Features
* Implement screen, overlay, darken, lighten, and add blend modes ([#2408](https://github.com/airbnb/lottie-android/pull/2408))
* Implement auto-orient ([#2416](https://github.com/airbnb/lottie-android/pull/2416))
* Allow globally configuring asyncUpdates ([#2356](https://github.com/airbnb/lottie-android/pull/2356))
* Add an optional `close` param to LottieCompositionFactory.fromJsonReader ([#2342](https://github.com/airbnb/lottie-android/pull/2342))
* Allow dynamic properties for solid layer colors ([#2378](https://github.com/airbnb/lottie-android/pull/2378))
* Update baseline profiles ([#2404](https://github.com/airbnb/lottie-android/pull/2404))
* Add a ZipInputStream overload to LottieAnimationView.setAnimation ([#2411](https://github.com/airbnb/lottie-android/pull/2411))

### Bugs Fixed
* Upgrade okio ([#2418](https://github.com/airbnb/lottie-android/pull/2418))
* Improve cache hits for synchronous LottieCompositionFactory methods ([#2379](https://github.com/airbnb/lottie-android/pull/2379))
* Fix gradient interpolation for opacity stops beyond the last color stop ([#2377](https://github.com/airbnb/lottie-android/pull/2377))
* Fix Potential NPE In NetworkCache.clearCache ([#2364](https://github.com/airbnb/lottie-android/pull/2364))
* Fix an IllegalArgumentException when creating a bitmap ([#2351](https://github.com/airbnb/lottie-android/pull/2351))
* Fix rounded corners for non-closed paths ([#2405](https://github.com/airbnb/lottie-android/pull/2405))
* Fix varying opacity stops across keyframes in the same gradient  ([#2406](https://github.com/airbnb/lottie-android/pull/2406))
* Fix a NullPointerException in ColorKeyframeAnimation  ([#2407](https://github.com/airbnb/lottie-android/pull/2407))

# 6.1.0
### New Features
* New multithreaded `asyncUpdates` feature which moves the entire update phase of an animation off of the main thread. For more information, refer to [this blog post](https://gpeal.medium.com/lottie-android-6-1-lottie-goes-multithreaded-67c09c091fd7). ([#2276](https://github.com/airbnb/lottie-android/pull/2276))
* Allow `LottieCompositionFactory` to not close input streams ([#2286](https://github.com/airbnb/lottie-android/pull/2286) and [#2319](https://github.com/airbnb/lottie-android/pull/2319))
* Allow Lottie to be initialized multiple times ([#2323](https://github.com/airbnb/lottie-android/pull/2323))
* Add an additional null check to TransformKeyframeAnimation ([#2381](https://github.com/airbnb/lottie-android/pull/2381))
* Fix asyncUpdates for Nougat and below ([#2380](https://github.com/airbnb/lottie-android/pull/2380))

### Bugs Fixed
* Close input streams for cache hits ([#2253](https://github.com/airbnb/lottie-android/pull/2253))
* Always use ApplicationContext in ImageAssetManager to ensure it can be reused ([#2289](https://github.com/airbnb/lottie-android/pull/2289))
* Hold weak references to success/failure listeners ([#2293](https://github.com/airbnb/lottie-android/pull/2293))
* Add default values for line join and cap types ([#2337](https://github.com/airbnb/lottie-android/pull/2337))
* Apply layer parent opacity to text ([#2336](https://github.com/airbnb/lottie-android/pull/2336))

# 6.0.1
### Bugs Fixed
* Allow loading URLs with a length of greater than 255 chars ([#2311](https://github.com/airbnb/lottie-android/pull/2311))

# 6.0.0
### New Features
* Major overhaul of text layout. Text layout should be more consistent across the board ([#2162](https://github.com/airbnb/lottie-android/pull/2162))
* Allow animations in zip files to contain embedded base64 encoded images ([#2110](https://github.com/airbnb/lottie-android/pull/2110))
* Allow zip files to contain embedded fonts. Context was added to some LottieCompositionFactory APIs to support this ([#2102](https://github.com/airbnb/lottie-android/pull/2102))
* Add fontStyle and fontName as parameters in new overloads in FontAssetDelegate ([#2103](https://github.com/airbnb/lottie-android/pull/2103))
* Allow decimal values for precomp size ([#2138](https://github.com/airbnb/lottie-android/pull/2138))
* Allow interpolating in between gradients that have different numbers of opacity stops ([#2160](https://github.com/airbnb/lottie-android/pull/2160))
* Support box position in document data ([#2139](https://github.com/airbnb/lottie-android/pull/2139))
* Allow repeater contents to be the target of dynamic properties ([#2164](https://github.com/airbnb/lottie-android/pull/2164))
* Provide a global LottieTask listener to aid in Espresso idle resources ([#2161](https://github.com/airbnb/lottie-android/pull/2161))
* Allow setting a default font extension ([#2166](https://github.com/airbnb/lottie-android/pull/2166))
* Add an option to completely disable Lottie's network cache ([#2158](https://github.com/airbnb/lottie-android/pull/2158))
* Allow setting a font map for custom fonts ([#2180](https://github.com/airbnb/lottie-android/pull/2180))
* Allow ImageAssetDelegate to be used when a drawable doesn't have a callback ([#2183](https://github.com/airbnb/lottie-android/pull/2072))
* Make Layer name and refId public ([#2188](https://github.com/airbnb/lottie-android/pull/2188))
* Allow rendering at the composition frame rate ([#2184](https://github.com/airbnb/lottie-android/pull/2184))
### Bugs Fixed
* Fixed an NPE when decoding an invalid bitmap and for transform opacity, and transform anchor position ([#2117](https://github.com/airbnb/lottie-android/pull/2117), [#2179](https://github.com/airbnb/lottie-android/pull/2179), and [#2197](https://github.com/airbnb/lottie-android/pull/2197))
* Only store application context in ImageAssetManager ([#2163](https://github.com/airbnb/lottie-android/pull/2163))
* Prevent rounded corner effects from trying to round a shape that has control points on its vertices already ([#2165](https://github.com/airbnb/lottie-android/pull/2165))
* Pass LottieComposition directly while building layers to avoid race conditions ([#2167](https://github.com/airbnb/lottie-android/pull/2167))
* Allow progress to be restored from saved state ([#2072](https://github.com/airbnb/lottie-android/pull/2072))
* Take top and left Drawable bounds into account to support things like SeekBar thumbs ([#2182](https://github.com/airbnb/lottie-android/pull/2182))
* Use the correct cache key for network animations ([#2198](https://github.com/airbnb/lottie-android/pull/2198))

# 5.2.0
### Bugs Fixed
* De-dupe gradient stops. On pre-Oreo devices, if you had color and opacity stops in the same place and used hardware acceleration, you may have seen artifacts at the stop positions as of 5.1.1 [#20814](https://github.com/airbnb/lottie-android/pull/2081)

# 5.1.1
### New Features
* Added support for gradient opacity stops at different points than color stops ([#2062](https://github.com/airbnb/lottie-android/pull/2062))
* Allow notifying LottieDrawable that system animations are disabled ([#2063](https://github.com/airbnb/lottie-android/pull/2063))

### Bugs Fixed
* Removed some rounding errors that occurred when setting min/max frames ([#2064](https://github.com/airbnb/lottie-android/pull/2064))
* Clear onVisibleAction one it is consumed ([#2066](https://github.com/airbnb/lottie-android/pull/2066))
* Fixed a Xiaomi Android 10 specific crash ([#2061](https://github.com/airbnb/lottie-android/pull/2061))
* Made LottieAnimationView.start() mimic playAnimation ([#2056](https://github.com/airbnb/lottie-android/pull/2056))
* Remove @RestrictTo from LottieNetworkFetcher ([#2049](https://github.com/airbnb/lottie-android/pull/2049))

# 5.0.3
### Bugs Fixed
* Invalidate the software renering bitmap when invalidate is called ([#2034](https://github.com/airbnb/lottie-android/pull/2034))

# 5.0.2
### Bugs Fixed
* Prevent a crash when using software rendering before a composition has been set ([#2025](https://github.com/airbnb/lottie-android/pull/2025))

# 5.0.1
### New Features
* [Removed API] Removed the `setScale(float)` APIs from `LottieAnimationView` and `LottieDrawable`. The expected behavior was highly ambiguous when paired with other scale types and canvas transformations. For the vast majority of cases, ImageView.ScaleType should be sufficient. For remaining cases, you may apply transformations to Canvas and use `LottieDrawable#draw` directly. 
* Added support for the "Rounded Corners" effect on Shape and Rect layers ([#1953](https://github.com/airbnb/lottie-android/pull/1953))
* Prior to 5.0, LottieAnimationView would _always_ call [setLayerType](https://developer.android.com/reference/android/view/View#setLayerType(int,%20android.graphics.Paint)) with either [HARDWARE](https://developer.android.com/reference/android/view/View#LAYER_TYPE_HARDWARE) or [SOFTWARE](https://developer.android.com/reference/android/view/View#LAYER_TYPE_SOFTWARE). In the hardware case, this would case Android to allocate a dedicated hardware buffer for the animation that had to be uploaded to the GPU separately. In the software case, LottieAnimationView would rely on View's internal [drawing cache](https://developer.android.com/reference/android/view/View#isDrawingCacheEnabled()).

    This has a few disadvantages:

  * The hardware/software distinction happened at the LottieAnimationView level. That means that consumers of LottieDrawable (such as lottie-compose) had no way to
          choose a render mode.
  * Dedicated memory for Lottie was _always_ allocated. In the software case, it would be a bitmap that is the size of the LottieAnimationView and in the hardware case, it was a dedicated hardware layer.
  
  Benefits as a result of this change:

  * Reduced memory consumption. In the hardware case, no new memory is allocated. In the software case, Lottie will create a bitmap that is the intersection of your View/Composition bounds mapped with the drawing transformation which often yields a surface are that is smaller than the entire LottieAnimationView.
  * lottie-compose now supports setting a RenderMode.
  * Custom uses of LottieDrawable now support setting a RenderMode via [setRenderMode](https://github.com/airbnb/lottie-android/blob/c5b8318c7cf205e95db143955acbfc69f86bc339/lottie/src/main/java/com/airbnb/lottie/LottieDrawable.java#L329).
* Lottie can now render outside of its composition bounds. To allow this with views such as LottieAnimationView, set `clipToCompositionBounds` to false on `LottieDrawable` or `LottieAnimationView` and `clipChildren` to false on the parent view. For Compose, use the `clipToCompositionBounds` parameter.
* Prior to 5.0, LottieAnimationView handled all animation controls when the view's visibility or attach state changed. This worked fine for consumers of LottieAnimationView. However, custom uses of LottieDrawable were prone to leaking infinite animators if they did not properly handle cancelling animations correctly. This opens up the possibility for unexpected behavior and increased battery drain. Lottie now behaves more like animated drawables in the platform and moves this logic into the Drawable via its [setVisible](https://developer.android.com/reference/android/graphics/drawable/Drawable#setVisible(boolean,%20boolean)) API. This should lead to no explicit behavior changes if you are using LottieAnimationView. However, if you are using LottieDrawable directly and were explicitly pausing/cancelling animations on lifecycle changes, you may want to cross check your expected behavior with that of LottieDrawable after this update. This change also resolved a long standing bug when Lottie is used in RecyclerViews due to the complex way in which RecyclerView handles View lifecycles ([#1495](https://github.com/airbnb/lottie-android/issues/1495)).
  [#1981](https://github.com/airbnb/lottie-android/issues/1981)
* Add an API [setClipToCompositionBounds](https://github.com/airbnb/lottie-android/blob/c5b8318c7cf205e95db143955acbfc69f86bc339/lottie/src/main/java/com/airbnb/lottie/LottieDrawable.java#L218) on LottieAnimationView, LottieDrawable, and the LottieAnimation composable to prevent Lottie from clipping animations to the composition bounds.
* Add an API to always render dynamically set bitmaps at the original animation bounds. Previously, dynamically set bitmaps would be rendered at their own size anchored to the top left
  of the original bitmap. This meant that if you wanted to supply a lower resolution bitmap to save memory, it would render smaller. The default behavior remains the same but you can
  enable [setMaintainOriginalImageBounds](https://github.com/airbnb/lottie-android/blob/c5b8318c7cf205e95db143955acbfc69f86bc339/lottie/src/main/java/com/airbnb/lottie/LottieDrawable.java#L264) to be able to supply lower resolution bitmaps ([#1706](https://github.com/airbnb/lottie-android/issues/1706)).
* Add support for `LottieProperty.TEXT` to use dynamic properties for text. This enables dynamic text support for lottie-compose ([#1995](https://github.com/airbnb/lottie-android/issues/1495)).
* Add getters for Marker fields ([#1998](https://github.com/airbnb/lottie-android/pull/1998))
* Add support for reversed polystar paths ([#2003](https://github.com/airbnb/lottie-android/pull/2003))

### Bugs Fixed
* Fix a rare NPE multi-threaded race condition ([#1959](https://github.com/airbnb/lottie-android/pull/1959))
* Don't cache dpScale to support moving Activities between different displays ([#1915](https://github.com/airbnb/lottie-android/pull/1915))
* Fix some cases that would prevent LottieAnimationView or LottieDrawable from being rendered by the Android Studio layout preview ([#1984](https://github.com/airbnb/lottie-android/pull/1984))
* Better handle animations in which there is only a single color in a gradient ([#1985](https://github.com/airbnb/lottie-android/pull/1985))
* Fix a rare race condition that could leak a LottieTask object ([#1986](https://github.com/airbnb/lottie-android/pull/1986))
* Call onAnimationEnd when animations are cancelled to be consistent with platform APIs ([#1994](https://github.com/airbnb/lottie-android/issues/1994))
* Fix a bug that would only render part of a path if the trim path extended from 0-100 and had an offset ([#1999](https://github.com/airbnb/lottie-android/pull/1999))
* Add support for languages that use DIRECTIONALITY_NONSPACING_MARK like Hindi ([#2001](https://github.com/airbnb/lottie-android/pull/2001))
* Prevent LottieAnimationView from overwriting user actions when restoring instance state ([#2002](https://github.com/airbnb/lottie-android/pull/2002))

# 4.2.2
### Bugs Fixed
* Removed allocations when setting paint alpha prior to API 29 ([#1929](https://github.com/airbnb/lottie-android/pull/1929))
* Added application/x-zip and application/x-zip-compressed as recognized zip mime types ([#1950](https://github.com/airbnb/lottie-android/pull/1950))
* Fixed a rare NPE in TransformKeyframeAnimation ([#1955](https://github.com/airbnb/lottie-android/pull/1955))

# 4.2.1
### Features and Improvements
* Upgraded to Compose 1.0.3 ([#1913](https://github.com/airbnb/lottie-android/pull/1913))
* Added an overload to TextDelegate that provides layerName ([#1931](https://github.com/airbnb/lottie-android/pull/1931))
### Bugs Fixed
* Removed some extra Integer allocations with dynamic colors ([#1927](https://github.com/airbnb/lottie-android/pull/1927))
* Fixed two rare potential NPEs ([#1917](https://github.com/airbnb/lottie-android/pull/1917))

# 4.2.0
* Fixed some rounding errors with trim paths ([#1897](https://github.com/airbnb/lottie-android/pull/1897))

# 4.1.0
* Added support for gaussian blur effects ([#1859](https://github.com/airbnb/lottie-android/pull/1859))
* Added support for drop shadow effects ([#1860](https://github.com/airbnb/lottie-android/pull/1860))

BREAKING CHANGES
Before this release, drop shadows and blurs were completely ignored. They will now be rendered. In most cases, they will now be rendered correctly. However, you should read the implementation details [here](https://airbnb.io/lottie/#/supported-features?id=drop-shadows-and-gaussian-blurs-on-android) if they are not.

# 4.0.0
* Support for lottie-compose 4.0.0

# 3.7.2
* Support for lottie-compose 1.0.0-rc02-1

# 3.7.1
### Bugs Fixed
* Made TextDelegate.getText public ([#1792](https://github.com/airbnb/lottie-android/pull/1792))
* Fixed an incorrect time stretch calculation ([#1818](https://github.com/airbnb/lottie-android/pull/1818))
* Use the application context in NetworkFetcher to prevent memory leaks ([#1832](https://github.com/airbnb/lottie-android/pull/1832))

# 3.7.0
### Features and Improvements
* Added an API to ignore disabled system animations (setIgnoreDisabledSystemAnimations(boolean)) ([#1747](https://github.com/airbnb/lottie-android/pull/1747))
* Added support for jpgs as image assets ([#1769](https://github.com/airbnb/lottie-android/pull/1769))
### Bugs Fixed
* Prevented duplicate positions in gradients which caused hardware accelerated rendering bugs on some phones ([#1768](https://github.com/airbnb/lottie-android/pull/1768))
* Fixed some parsing errors that occurred in some animations exported with Flow ([#1771](https://github.com/airbnb/lottie-android/pull/1771))

# 3.6.1
### Bugs Fixed
* Fixed a bug that would cause animations to stop animating if the same LottieAnimationView was used with multiple animations ([#1727](https://github.com/airbnb/lottie-android/pull/1737))

# 3.6.0
### Features and Improvements
* `LottieProperty.TRANSFORM_POSITION_X` and `LottieProperty.TRANSFORM_POSITION_Y` has been added to enable dynamic properties on transform positions that have split dimensions enabled ([#1714](https://github.com/airbnb/lottie-android/pull/1714))
* Allow targeting matte layers with dynamic properties/`KeyPath` ([#1710](https://github.com/airbnb/lottie-android/pull/1710))
* Properly render points that have different interpolators on the x andy axis ([#1709](https://github.com/airbnb/lottie-android/pull/1709))
* Support `ColorStateLists` in `LottieAnimationView` `lottie_colorFilter` xml attribute ([#1708](https://github.com/airbnb/lottie-android/pull/1708))
### Bugs Fixed
* Don't set `LottieDrawable` bounds internally. It will not respect the bounds set on it ([#1713](https://github.com/airbnb/lottie-android/pull/1713))
* Fix a rare NPE running lazy composition tasks ([#1711](https://github.com/airbnb/lottie-android/pull/1711))
* Don't render masks with <1px of width or height ([#1704](https://github.com/airbnb/lottie-android/pull/1704))
* After replacing a composition on `LottieAnimationView` with another drawable then setting it back would cause the animation to not render ([#1703](https://github.com/airbnb/lottie-android/pull/1703))
* Properly display text at the end of an animation ([#1689](https://github.com/airbnb/lottie-android/pull/1689))
* Always cancel animations when a LottieDrawable is unscheduled from LottieAnimationView ([adb331](https://github.com/airbnb/lottie-android/commit/adb331))

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
