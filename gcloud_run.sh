#! /bin/bash
if [ "$TRAVIS_PULL_REQUEST_SLUG" != "airbnb/lottie-android" ]; then
  echo "Skipping gcloud run for PR because api keys are not available from forks."
  exit 0
fi

gcloud firebase test android run --type instrumentation --device model=Nexus5X,version=26 --app LottieSample/build/outputs/apk/debug/LottieSample-debug.apk --test LottieSample/build/outputs/apk/androidTest/debug/LottieSample-debug-androidTest.apk
./post_pr_comment.js