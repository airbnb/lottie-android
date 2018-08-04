#! /bin/bash

jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore lottie-upload-key.jks -storepass $LOTTIE_UPLOAD_CERT_KEY_STORE_PASSWORD LottieSample/build/outputs/apk/release/LottieSample-release-unsigned.apk upload
