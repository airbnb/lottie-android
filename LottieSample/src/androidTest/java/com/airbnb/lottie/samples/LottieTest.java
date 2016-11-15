package com.airbnb.lottie.samples;


import android.test.ActivityInstrumentationTestCase2;

import org.junit.Test;

/**
 * Run these with: ./gradlew --daemon recordMode screenshotTests
 * If you run that command, it completes successfully, and nothing shows up in git, then you haven't broken anything!
 */
public class LottieTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public LottieTest() {
        super(MainActivity.class);
    }

    @Test
    public void testAll() {
        TestRobot.testAnimatedFile(getActivity(), "Alarm.json");
        TestRobot.testAnimatedFile(getActivity(), "BusinessTravel01.json");
        TestRobot.testAnimatedFile(getActivity(), "BusinessTravel03.json");
        TestRobot.testAnimatedFile(getActivity(), "CheckSwitch.json");
        TestRobot.testAnimatedFile(getActivity(), "Diamond.json");
        TestRobot.testAnimatedFile(getActivity(), "Heart.json");
        TestRobot.testAnimatedFile(getActivity(), "Hosts.json");
        TestRobot.testAnimatedFile(getActivity(), "LightBulb.json");
        TestRobot.testAnimatedFile(getActivity(), "Stars.json");
        TestRobot.testAnimatedFile(getActivity(), "LoopPlayOnce.json");
    }
}
