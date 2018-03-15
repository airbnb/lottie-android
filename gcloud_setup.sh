#! /bin/bash
echo Slug $TRAVIS_PULL_REQUEST_SLUG
if [ -z $TRAVIS_PULL_REQUEST_SLUG ] && [ "$TRAVIS_PULL_REQUEST_SLUG" != "airbnb/lottie-android" ]; then
  echo "Skipping gcloud setup for PR because api keys are not available from forks."
  exit 0
fi
echo $GCLOUD_SERVICE_KEY | base64 --decode --ignore-garbage > ${HOME}/gcloud-service-key.json
curl https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-182.0.0-linux-x86_64.tar.gz -o gcloud.tar.gz
tar xzf gcloud.tar.gz -C ${HOME}
${HOME}/google-cloud-sdk/install.sh --quiet --usage-reporting false
gcloud auth activate-service-account --key-file ${HOME}/gcloud-service-key.json
  # - gcloud components update
gcloud config set project lottie-snapshots
export TRAVIS_GIT_BRANCH=$(if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then echo $TRAVIS_BRANCH; else echo $TRAVIS_PULL_REQUEST_BRANCH; fi)
export GIT_SHA=$(git rev-parse HEAD)
export GIT_MERGE_BASE=$(git merge-base master)
echo GIT_SHA $GIT_SHA
echo GIT_MERGE_BASE $GIT_MERGE_BASE