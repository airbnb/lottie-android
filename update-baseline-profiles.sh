#!/usr/bin/env bash
set -uo pipefail

# If on CI, add indirect swiftshader arg
# Source: https://developer.android.com/studio/test/gradle-managed-devices
gpu_arg=""
if [ "${CI:-}" == "true" ]; then
  gpu_arg="-Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect"
fi

./gradlew cleanManagedDevices --unused-only &&
    ./gradlew lottie:generateBaselineProfile lottie-compose:generateBaselineProfile \
    -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
    -Pandroid.experimental.testOptions.managedDevices.setupTimeoutMinutes=20 \
    "${gpu_arg}" \
    --info
