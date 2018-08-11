#! /bin/bash
if [ -z $TRAVIS_PULL_REQUEST_SLUG ] && [ "$TRAVIS_REPO_SLUG" != "airbnb/lottie-android" ]; then
  echo "Skipping gcloud run for PR because api keys are not available from forks."
  exit 0
fi

if [ ! -f ${HOME}/google-cloud-sdk/install.sh ]; then
  mkdir $HOME/.cache
  echo "File not found!"
  curl https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-209.0.0-linux-x86_64.tar.gz -o gcloud.tar.gz
  tar xzf gcloud.tar.gz -C ${HOME}
  ${HOME}/google-cloud-sdk/install.sh --quiet --usage-reporting false
fi
echo $GCLOUD_SERVICE_KEY | base64 --decode --ignore-garbage > ${HOME}/.cache/gcloud-service-key.json
gcloud auth activate-service-account --key-file ${HOME}/.cache/gcloud-service-key.json
gcloud config set project lottie-snapshots

RunTests()
{
gcloud firebase test android run --no-auto-google-login --type instrumentation --device model=Nexus5X,version=26 --app LottieSample/build/outputs/apk/debug/LottieSample-debug.apk --test LottieSample/build/outputs/apk/androidTest/debug/LottieSample-debug-androidTest.apk
result=$?
}

RunTests

if [ "$result" -ne "0" ]; then
  # Retry if it fails. Sometimes the tests fail on Firebase with a native error
  echo "Firebase tests failed. Trying again."
  RunTests
fi

if [ "$result" -ne "0" ]; then
  # Retry if it fails. Sometimes the tests fail on Firebase with a native error
  echo "Firebase tests failed. Trying again."
  RunTests
fi

if [ "$result" -eq "0" ]; then
  ./post_pr_comment.js
fi
exit $result