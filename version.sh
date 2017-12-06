#!/bin/bash

sed -i '' "s/    versionName \".*\"/    versionName \"$1\"/" lottie/build.gradle

sed -i '' "s/    versionName \".*\"/    versionName \"$1\"/" LottieSample/build.gradle
versionCode=$((`cat LottieSample/build.gradle | grep versionCode | awk '{print $2}'` + 1))
sed -i '' "s/    versionCode .*/    versionCode $versionCode/" LottieSample/build.gradle

sed -i '' "s/VERSION_NAME=.*/VERSION_NAME=$1/" gradle.properties

sed -i '' "s/  compile 'com[.]airbnb[.]android[:]lottie[:].*'/  compile 'com\.airbnb\.android\:lottie\:$1'/" README.md