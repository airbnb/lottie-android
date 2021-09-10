#!/bin/bash
./gradlew clean lottie:assembleRelease lottie-compose:assembleRelease lottie:publish lottie-compose:publish --rerun-tasks --no-parallel