#!/bin/bash
git_branch=`git rev-parse --abbrev-ref HEAD`
if [ "$git_branch" != "master" ]; then
    echo "You must run this from master!"
    exit 1
fi
./gradlew clean lottie:assembleRelease lottie-compose:assembleRelease lottie:publish lottie-compose:publish --rerun-tasks --no-parallel --no-configuration-cache --stacktrace
