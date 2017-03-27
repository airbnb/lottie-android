# Lottie for Android, [iOS](https://github.com/airbnb/lottie-ios), and [React Native](https://github.com/airbnb/lottie-react-native)
<a href='https://play.google.com/store/apps/details?id=com.airbnb.lottie'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height="80px"/></a> 

Lottie is a mobile library for Android and iOS that parses [Adobe After Effects](http://www.adobe.com/products/aftereffects.html) animations exported as json with [Bodymovin](https://github.com/bodymovin/bodymovin) and renders them natively on mobile!

For the first time, designers can create **and ship** beautiful animations without an engineer painstakingly recreating it by hand. They say a picture is worth 1,000 words so here are 13,000:

![Example1](gifs/Example1.gif)


![Example2](gifs/Example2.gif)


![Example3](gifs/Example3.gif)


![Community](gifs/Community%202_3.gif)


![Example4](gifs/Example4.gif)


All of these animations were created in After Effects, exported with Bodymovin, and rendered natively with no additional engineering effort.

[Bodymovin](https://github.com/bodymovin/bodymovin) is an After Effects plugin created by Hernan Torrisi that exports After effects files as json and includes a javascript web player. We've built on top of his great work to extend its usage to Android, iOS, and React Native.

Read more about it on our [blog post](http://airbnb.design/introducing-lottie/)
Or get in touch on Twitter ([gpeal8](https://twitter.com/gpeal8)) or via lottie@airbnb.com

## Other Platforms
 * [Web](https://github.com/bodymovin/bodymovin)
 * [Xamarin](https://github.com/martijn00/LottieXamarin)
 * [NativeScript](https://github.com/bradmartin/nativescript-lottie)
 * [Appcelerator Titanium](https://github.com/m1ga/ti.animation)

## Sample App

You can build the sample app yourself or download it from the [Play Store](https://play.google.com/store/apps/details?id=com.airbnb.lottie). The sample app includes some built in animations but also allows you to load an animation from internal storage or from a url.


## Download

Gradle is the only supported build configuration, so just add the dependency to your project `build.gradle` file:

```groovy
dependencies {  
  compile 'com.airbnb.android:lottie:2.0.0-beta3'
}
```

## Shipping something with Lottie?

Email us at lottie@airbnb.com and soon we will create a testimonals and use cases page with real world usages of Lottie from around the world.

## Using Lottie
Lottie supports ICS (API 14) and above.
The simplest way to use it is with LottieAnimationView:

```xml
<com.airbnb.lottie.LottieAnimationView
        android:id="@+id/animation_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:lottie_fileName="hello-world.json"
        app:lottie_loop="true"
        app:lottie_autoPlay="true" />
```

Or you can load it programmatically in multiple ways.
From a json asset in app/src/main/assets:
```java
LottieAnimationView animationView = (LottieAnimationView) findViewById(R.id.animation_view);
animationView.setAnimation("hello-world.json");
animationView.loop(true);
animationView.playAnimation();
```
This method will load the file and parse the animation in the background and asynchronously start rendering once completed.

If you want to reuse an animation such as in each item of a list or load it from a network request JSONObject:
```java
 LottieAnimationView animationView = (LottieAnimationView) findViewById(R.id.animation_view);
 ...
 Cancellable compositionCancellable = LottieComposition.Factory.fromJson(getResources(), jsonObject, (composition) -> {
     animationView.setComposition(composition);
     animationView.playAnimation();
 });

 // Cancel to stop asynchronous loading of composition
 // compositionCancellable.cancel();
```

You can then control the animation or add listeners:
```java
animationView.addAnimatorUpdateListener((animation) -> {
    // Do something.
});
animationView.playAnimation();
...
if (animationView.isAnimating()) {
    // Do something.
}
...
animationView.setProgress(0.5f);
...
// Custom animation speed or duration.
ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f)
    .setDuration(500);
animator.addUpdateListener(animation -> {
    animationView.setProgress(animation.getAnimatedValue());
});
animator.start();
...
animationView.cancelAnimation();
```

You can add a color filter to the whole animation, a specific layer, or specific content within a layer:
```java
// Any class that conforms to the ColorFilter interface
final SimpleColorFilter colorFilter = new SimpleColorFilter(Color.RED);

// Adding a color filter to the whole view
animationView.addColorFilter(colorFilter);

// Adding a color filter to a specific layer
animationView.addColorFilterToLayer("hello_layer", colorFilter);

// Adding a color filter to specfic content on the "hello_layer"
animationView.addColorFilterToContent("hello_layer", "hello", colorFilter);

// Clear all color filters
animationView.clearColorFilters();
```
Note: Color filters are only available for layers such as Image layer and Solid layer as well as content that includes fill, stroke, or group content.

Under the hood, `LottieAnimationView` uses `LottieDrawable` to render its animations. If you need to, you can use the drawable form directly:
```java
LottieDrawable drawable = new LottieDrawable();
LottieComposition.Factory.fromAssetFileName(getContext(), "hello-world.json", (composition) -> {
    drawable.setComposition(composition);
});
```

If your animation will be frequently reused, `LottieAnimationView` has an optional caching strategy built in. Use `LottieAnimationView#setAnimation(String, CacheStrategy)`. `CacheStrategy` can be `Strong`, `Weak`, or `None` to have `LottieAnimationView` hold a strong or weak reference to the loaded and parsed animation.
 
 
### Image Support
You can animate images if your animation is loaded from assets and your image file is in a 
subdirectory of assets. Just call `setImageAssetsFolder` on `LottieAnimationView` or 
`LottieDrawable` with the relative folder inside of assets and make sure that the images that 
bodymovin export are in that folder with their names unchanged (should be img_#).
If you use `LottieDrawable` directly, you must call `recycleBitmaps` when you are done with it.

If you need to provide your own bitmaps if you downloaded them from the network or something, you
 can provide a delegate to do that:
 ```java
animationView.setImageAssetDelegate(new ImageAssetDelegate() {
          @Override public Bitmap fetchBitmap(LottieImageAsset asset) {
            getBitmap(asset);
          }
        });
```

## Supported After Effects Features

### Pre-composition

---

### Keyframe Interpolation

---

* Linear Interpolation

* Bezier Interpolation

* Hold Interpolation

* Rove Across Time

* Spatial Bezier

### Solids

---

* Transform Anchor Point

* Transform Position

* Transform Scale

* Transform Rotation

* Transform Opacity

### Masks

---

* Path

* Opacity

* Multiple Masks (additive, subtractive, inverted)

### Track Mattes

---

* Alpha Matte

### Parenting

---

* Multiple Parenting

* Nulls

### Shape Layers

---

* Rectangle (All properties)

* Ellipse (All properties)

* Polystar (All properties)

* Polygon (All properties. Integer point values only.)

* Path (All properties)

* Anchor Point

* Position

* Scale

* Rotation

* Opacity

* Group Transforms (Anchor point, position, scale etc)

* Multiple paths in one group

* Merge paths (off by default and must be explicitly enabled with
`enableMergePathsForKitKatAndAbove`)

#### Stroke (shape layer)

---

* Stroke Color

* Stroke Opacity

* Stroke Width

* Line Cap

* Dashes

#### Fill (shape layer)

---

* Fill Color

* Fill Opacity

#### Trim Paths (shape layer)

---

* Trim Paths Start

* Trim Paths End

* Trim Paths Offset

## Performance and Memory
1. If the composition has no masks or mattes then the performance and memory overhead should be quite good. No bitmaps are created and most operations are simple canvas draw operations.
2. If the composition has masks or mattes, offscreen buffers will be used and there will 
be a performance hit has it gets drawn.  
3. If you are using your animation in a list, it is recommended to use a CacheStrategy in 
LottieAnimationView.setAnimation(String, CacheStrategy) so the animations do not have to be deserialized every time.

## Try it out
Clone this repository and run the LottieSample module to see a bunch of sample animations. The JSON files for them are located in [LottieSample/src/main/assets](https://github.com/airbnb/lottie-android/tree/master/LottieSample/src/main/assets) and the original After Effects files are located in [/After Effects Samples](https://github.com/airbnb/lottie-android/tree/master/After%20Effects%20Samples)

The sample app can also load json files at a given url or locally on your device (like Downloads or on your sdcard).
 
## Alternatives
1. Build animations by hand. Building animations by hand is a huge time commitment for design and engineering across Android and iOS. It's often hard or even impossible to justify spending so much time to get an animation right.
2. [Facebook Keyframes](https://github.com/facebookincubator/Keyframes). Keyframes is a wonderful new library from Facebook that they built for reactions. However, Keyframes doesn't support some of Lottie's features such as masks, mattes, trim paths, dash patterns, and more.
2. Gifs. Gifs are more than double the size of a bodymovin JSON and are rendered at a fixed size that can't be scaled up to match large and high density screens.
3. Png sequences. Png sequences are even worse than gifs in that their file sizes are often 30-50x the size of the bodymovin json and also can't be scaled up.

## Why is it called Lottie?
Lottie is named after a German film director and the foremost pioneer of silhouette animation. Her best known films are The Adventures of Prince Achmed (1926) – the oldest surviving feature-length animated film, preceding Walt Disney's feature-length Snow White and the Seven Dwarfs (1937) by over ten years
[The art of Lotte Reineger](https://www.youtube.com/watch?v=LvU55CUw5Ck&feature=youtu.be)

## Contributing
Contributors are more than welcome. Just upload a PR with a description of your changes.
Lottie uses [Facebook screenshot tests for Android](https://github.com/facebook/screenshot-tests-for-android) to identify pixel level changes/breakages. Please run `./gradlew --daemon recordMode screenshotTests` before uploading a PR to ensure that nothing has broken. Use a Nexus 5 emulator running Lollipop for this. Changed screenshots will show up in your git diff if you have.

If you would like to add more JSON files and screenshot tests, feel free to do so and add the test to `LottieTest`.

## Issues or feature requests?
File github issues for anything that is unexpectedly broken. If an After Effects file is not working, please attach it to your issue. Debugging without the original file is much more difficult.
