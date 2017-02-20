package com.airbnb.lottie;


import android.test.ActivityInstrumentationTestCase2;

import com.airbnb.lottie.samples.MainActivity;

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
    TestRobot.testAnimation(getActivity(), "9squares-AlBoardman.json");
    TestRobot.testAnimation(getActivity(), "EmptyState.json");
    TestRobot.testAnimation(getActivity(), "HamburgerArrow.json");
    TestRobot.testAnimation(getActivity(), "LottieLogo1.json");
    TestRobot.testAnimation(getActivity(), "LottieLogo2.json");
    TestRobot.testAnimation(getActivity(), "MotionCorpse-Jrcanest.json");
    TestRobot.testAnimation(getActivity(), "PinJump.json");
    TestRobot.testAnimation(getActivity(), "TwitterHeart.json");
    TestRobot.testAnimation(getActivity(), "Tests/Hosts.json");
    TestRobot.testAnimation(getActivity(), "Tests/LightBulb.json", new float[]{0f, 0.05f, 0.10f, 0.2f, 0.3f, 0.4f, 0.5f, 1f});
    TestRobot.testAnimation(getActivity(), "Tests/LoopPlayOnce.json");
    TestRobot.testAnimation(getActivity(), "Tests/Alarm.json");
    TestRobot.testAnimation(getActivity(), "Tests/CheckSwitch.json");
    TestRobot.testAnimation(getActivity(), "Tests/EllipseTrimPath.json");
    TestRobot.testAnimation(getActivity(), "Tests/SplitDimensions.json");
    TestRobot.testAnimation(getActivity(), "Tests/TrimPathsFull.json");
    TestRobot.testAnimation(getActivity(), "Tests/Laugh4.json");
    TestRobot.testAnimation(getActivity(), "Tests/Star.json");
    TestRobot.testAnimation(getActivity(), "Tests/Polygon.json");
    TestRobot.testAnimation(getActivity(), "Tests/AllSet.json");
    TestRobot.testAnimation(getActivity(), "Tests/City.json");
    TestRobot.testAnimation(getActivity(), "Tests/PreCompMadness.json");
    TestRobot.testAnimation(getActivity(), "Tests/MatteParentPrecomp.json");
    TestRobot.testStatic(getActivity(), "Tests/TrimPathFill.json");
    TestRobot.testStatic(getActivity(), "Tests/Mask_26.json");
    TestRobot.testStatic(getActivity(), "Tests/MatteInv.json");
    TestRobot.testStatic(getActivity(), "Tests/MaskInv.json");
  }
}
