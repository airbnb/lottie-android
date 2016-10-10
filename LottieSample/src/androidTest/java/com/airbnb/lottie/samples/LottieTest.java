package com.airbnb.lottie.samples;


import android.test.ActivityInstrumentationTestCase2;

import org.junit.Test;

public class LottieTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public LottieTest() {
        super(MainActivity.class);
    }

    @Test
    public void testAll() {
        TestRobot.testStaticFile(getActivity(), "Hosts Still.json");
        TestRobot.testAnimatedFile(getActivity(), "BusinessTravel01.json");
        TestRobot.testAnimatedFile(getActivity(), "BusinessTravel03.json");
        TestRobot.testAnimatedFile(getActivity(), "LightBulb.json");
        TestRobot.testAnimatedFile(getActivity(), "Diamond 2.json");
        TestRobot.testAnimatedFile(getActivity(), "CheckSwitch.json");
        TestRobot.testAnimatedFile(getActivity(), "Alarm.json");
        TestRobot.testAnimatedFile(getActivity(), "Stars.json");
        TestRobot.testAnimatedFile(getActivity(), "Selfie.json");
        TestRobot.testAnimatedFile(getActivity(), "LoopPlayOnce.json");
        TestRobot.testAnimatedFile(getActivity(), "City.json");
        TestRobot.testAnimatedFile(getActivity(), "House.json");
    }
}
