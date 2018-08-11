#!/bin/bash
git diff-index --quiet HEAD --

if [ $? -ne 0 ]; then
  echo "Working tree must be empty before bumping the version"
fi

sed -i '' "s/    versionName \".*\"/    versionName \"$1\"/" lottie/build.gradle

sed -i '' "s/    versionName \".*\"/    versionName \"$1\"/" LottieSample/build.gradle
versionCode=$((`cat LottieSample/build.gradle | grep versionCode | awk '{print $2}'` + 1))
sed -i '' "s/    versionCode .*/    versionCode $versionCode/" LottieSample/build.gradle

sed -i '' "s/VERSION_NAME=.*/VERSION_NAME=$1/" gradle.properties

git add -A
git commit -m "v$1"
git tag "v$1"
git push --follow-tags
git push origin v$1