package com.airbnb.lottie.samples;


import android.test.ActivityInstrumentationTestCase2;

import org.junit.Test;

/**
 * Run these with: ./gradlew --daemon recordMode screenshotTests
 */
public class LottieTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public LottieTest() {
        super(MainActivity.class);
    }

    @Test
    public void testAll() {
        TestRobot.testAnimatedFile(getActivity(), "Alarm.json");
        TestRobot.testAnimatedFile(getActivity(), "BusinessTravel01.json");
        TestRobot.testAnimatedFile(getActivity(), "BusinessTravel02.json");
        TestRobot.testAnimatedFile(getActivity(), "BusinessTravel03.json");
        TestRobot.testAnimatedFile(getActivity(), "CheckSwitch.json");
        TestRobot.testAnimatedFile(getActivity(), "Diamond.json");
        TestRobot.testAnimatedFile(getActivity(), "Heart.json");
        TestRobot.testAnimatedFile(getActivity(), "Hosts.json");
        TestRobot.testAnimatedFile(getActivity(), "House.json");
        TestRobot.testAnimatedFile(getActivity(), "Identity_AllSet.json");
        TestRobot.testAnimatedFile(getActivity(), "Identity_City.json");
        TestRobot.testAnimatedFile(getActivity(), "Identity_GovtID.json");
        TestRobot.testAnimatedFile(getActivity(), "Identity_Selfie.json");
        TestRobot.testAnimatedFile(getActivity(), "Lightbulb.json");
        TestRobot.testAnimatedFile(getActivity(), "Stars.json");
        TestRobot.testAnimatedFile(getActivity(), "LoopPlayOnce.json");
    }
}
