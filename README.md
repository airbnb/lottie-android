# Lottie for Android (and [iOS](https://github.com/airbnb/lottie-ios))

Lottie is a mobile library for Andorid and iOS that parses [Adobe After Effects](http://www.adobe.com/products/aftereffects.html) animations exported as json with [bodymovin](https://github.com/bodymovin/bodymovin) and renders them natively on mobile!

For the first time, designers can create **and ship** beautiful animations without an enginineer painstakingly recreating it be hand.
* [Painstaking example 1](http://jeremie-martinez.com//2016/09/15/train-animations/)
* [Painstaking example 2](https://blog.twitter.com/2015/hearts-on-twitter)

They say a picture is worth 1,000 words so here are 6,000

![Alarm](gifs/Alarm.gif)
![Diamond](gifs/Diamond.gif)
![Lightbulb](gifs/Lightbulb.gif)
![Business](gifs/Business.gif)
![AllSet](gifs/AllSet.gif)

![Selfie](gifs/Selfie.gif)

## Download

Gradle is the only supported build configuration, so just add the dependency to your project `build.gradle` file:

```groovy
dependencies {
  compile 'com.airbnb.android:lottie-android:1.0.0'
}
```


## Alternatives
1. Build animations by hand. Building animations by hand is a huge time commitment for design and engingeering across Android and iOS. It's often hard or even impossible to justify spending so much time to get an animation right.
2. Gifs. Gifs are more than double the size of a bodymovin JSON and are rendered at a fixed size that can't be scaled up to match large and high density screens.
3. Png sequences. Png sequences are even worse than gifs in that their file sizes are often 30-50x the size of the bodymovin json and also can't be scaled up.

## Using Lottie
Using Lottie is as simple as adding an image to an ImageView. `LottieAnimationView` can consume JSON stored in one of two ways:

1. By specifying the JSON filename in the /assets directory of your app.
2. By passing in the raw JSONObject. With this, you have the option of hosting your animations on your server and downloading them through a network request! This opens up the possibility of a/b testing animations or providing animations that are very specific to the user such as an animated Eifel Tower when an Airbnb traveler books a listing in Paris.

## Why is it called Lottie?
Lottie is named after a German film director and the foremost pioneer of silhouette animation. Her best known films are The Adventures of Prince Achmed (1926) – the oldest surviving feature-length animated film, preceding Walt Disney's feature-length Snow White and the Seven Dwarfs (1937) by over ten years
[The art of Lotte Reineger](https://www.youtube.com/watch?v=LvU55CUw5Ck&feature=youtu.be)

## Contributing
Contributers are more than welcome. Just upload a PR with a description of your changes.
Lottie uses [Facebook screenshot tests for Android](https://github.com/facebook/screenshot-tests-for-android) to identify pixel level changes/breakages. Please run `./gradlew --daemon recordMode screenshotTests` before uploading a PR to ensure that nothing has broken.

If you would like to add more JSON files and screenshot tests, feel free to do so and add the test to `LottieTest`.
