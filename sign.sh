#! /bin/bash

if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  # Don't sign PR builds.
  exit 0
fi

echo "----------Signing APK"
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore lottie-upload-key.jks -storepass $LOTTIE_UPLOAD_CERT_KEY_STORE_PASSWORD LottieSample/build/outputs/apk/release/LottieSample-release-unsigned.apk upload -keypass $LOTTIE_UPLOAD_CERT_KEY_PASSWORD -signedjar LottieSample/build/outputs/apk/release/LottieSample-release-signed.apk

echo "----------Zipaligning APK"
${ANDROID_HOME}/build-tools/27.0.3/zipalign 4 LottieSample/build/outputs/apk/release/LottieSample-release-unsigned.apk LottieSample/build/outputs/apk/release/LottieSample-release-aligned.apk