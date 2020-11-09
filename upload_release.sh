#!/bin/bash
./gradlew clean lottie:assembleRelease lottie-compose:assembleRelease lottie:uploadArchives lottie-compose:uploadArchives --rerun-tasks --no-parallel