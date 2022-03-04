# Lottie for Android, [iOS](https://github.com/airbnb/lottie-ios), [React Native](https://github.com/airbnb/lottie-react-native), [Web](https://github.com/airbnb/lottie-web), and [Windows](https://aka.ms/lottie)
![Build Status](https://github.com/airbnb/lottie-android/workflows/Verify/badge.svg)


<a href='https://play.google.com/store/apps/details?id=com.airbnb.lottie'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height="50px"/></a>

Lottie is a mobile library for Android and iOS that parses [Adobe After Effects](http://www.adobe.com/products/aftereffects.html) animations exported as json with [Bodymovin](https://github.com/airbnb/lottie-web) and renders them natively on mobile!

For the first time, designers can create **and ship** beautiful animations without an engineer painstakingly recreating it by hand. They say a picture is worth 1,000 words so here are 13,000:

# Sponsors
Lottie is maintained and improved on nights and weekends. If you use Lottie in your app, please consider sponsoring it to help ensure that we can continue to improve the project we love.
Click the sponsor button above to learn more

<img src="gifs/Sponsor.png" alt="Sponsor Button" width="100"/>

## Lead Sponsors
<a href="https://www.lottiefiles.com/"><img src="images/lottiefiles.svg" alt="Lottiefiles" width="300" /></a>

<a href="https://lottielab.io/"><img src="images/lottielab.png" alt="Lottie Lab" width="300" /></a>

<a href="https://www.airbnb.com/"><img src="images/airbnb.svg" alt="Airbnb" width="300" /></a>

<a href="https://www.tonal.com/"><img src="images/tonal.svg" alt="Tonal" width="300" /></a>

<a href="https://getstream.io/chat/sdk/android/?utm_source=OpenCollective&utm_medium=Github_Repo_Content_Ad&utm_content=Developer&utm_campaign=OpenCollective_Jan2022_AndroidChatSDK"><img src="images/stream.png" alt="Stream" width="300" /></a>

<a href="https://www.coinbase.com/"><img src="images/coinbase.svg" alt="Coinbase" width="300" /></a>

## View documentation, FAQ, help, examples, and more at [airbnb.io/lottie](http://airbnb.io/lottie/)



![Example1](gifs/Example1.gif)


![Example2](gifs/Example2.gif)


![Example3](gifs/Example3.gif)


![Community](gifs/Community%202_3.gif)


![Example4](gifs/Example4.gif)


## Download

Gradle is the only supported build configuration, so just add the dependency to your project `build.gradle` file:

```groovy
dependencies {
  implementation 'com.airbnb.android:lottie:$lottieVersion'
}
```
The latest Lottie version is:
![lottieVersion](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/lottie/badge.svg)

The latest stable [Lottie-Compose](http://airbnb.io/lottie/#/android-compose) version is:
![lottieVersion](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/lottie-compose/badge.svg)
Click [here](http://airbnb.io/lottie/#/android-compose) for more information on Lottie-Compose.

Lottie 2.8.0 and above only supports projects that have been migrated to [androidx](https://developer.android.com/jetpack/androidx/). For more information, read Google's [migration guide](https://developer.android.com/jetpack/androidx/migrate).

# Contributing

Because development has started for Lottie Compose, Gradle, and the Android Gradle Plugin will be kept up to date with the latest canaries. This also requires  you to use Android Studio Canary builds. [Preview builds](https://developer.android.com/studio/preview) can be installed side by side with stable versions.
