#!/bin/bash
./gradlew clean lottie:assembleRelease lottie-compose:assembleRelease lottie:uploadArchives --rerun-tasks --no-parallel