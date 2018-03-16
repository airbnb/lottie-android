#! /bin/bash
if [ "$TRAVIS_PULL_REQUEST_SLUG" != "airbnb/lottie-android" ]; then
  echo "Skipping gcloud run for PR because api keys are not available from forks."
  exit 0
fi


echo $GCLOUD_SERVICE_KEY | base64 --decode --ignore-garbage > ${HOME}/gcloud-service-key.json
curl https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-182.0.0-linux-x86_64.tar.gz -o gcloud.tar.gz
tar xzf gcloud.tar.gz -C ${HOME}
${HOME}/google-cloud-sdk/install.sh --quiet --usage-reporting false
gcloud auth activate-service-account --key-file ${HOME}/gcloud-service-key.json
  # - gcloud components update
gcloud config set project lottie-snapshots

gcloud firebase test android run --no-auto-google-login --type instrumentation --device model=Nexus5X,version=26 --app LottieSample/build/outputs/apk/debug/LottieSample-debug.apk --test LottieSample/build/outputs/apk/androidTest/debug/LottieSample-debug-androidTest.apk
./post_pr_comment.js