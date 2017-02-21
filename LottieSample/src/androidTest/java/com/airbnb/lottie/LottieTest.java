package com.airbnb.lottie;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.airbnb.lottie.samples.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Run these with: ./gradlew recordMode screenshotTests
 * If you run that command, it completes successfully, and nothing shows up in git, then you
 * haven't broken anything!
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LottieTest {
  @Rule public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(
      MainActivity.class);

  @Test public void testAll() {
    TestRobot.testAnimation(activityRule.getActivity(), "9squares-AlBoardman.json");
    TestRobot.testAnimation(activityRule.getActivity(), "EmptyState.json");
    TestRobot.testAnimation(activityRule.getActivity(), "HamburgerArrow.json");
    TestRobot.testAnimation(activityRule.getActivity(), "LottieLogo1.json");
    TestRobot.testAnimation(activityRule.getActivity(), "LottieLogo2.json");
    TestRobot.testAnimation(activityRule.getActivity(), "MotionCorpse-Jrcanest.json");
    TestRobot.testAnimation(activityRule.getActivity(), "PinJump.json");
    TestRobot.testAnimation(activityRule.getActivity(), "TwitterHeart.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/Hosts.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/LightBulb.json", null,
        new float[]{0f, 0.05f, 0.10f, 0.2f, 0.3f, 0.4f, 0.5f, 1f});
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/LoopPlayOnce.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/Alarm.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/CheckSwitch.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/EllipseTrimPath.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/SplitDimensions.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/TrimPathsFull.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/Laugh4.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/Star.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/Polygon.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/AllSet.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/City.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/PreCompMadness.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/MatteParentPrecomp.json");
    TestRobot.testAnimation(activityRule.getActivity(), "Tests/Image.json", "Tests/weaccept");
    TestRobot.testStatic(activityRule.getActivity(), "Tests/TrimPathFill.json");
    TestRobot.testStatic(activityRule.getActivity(), "Tests/Mask_26.json");
    TestRobot.testStatic(activityRule.getActivity(), "Tests/MatteInv.json");
    TestRobot.testStatic(activityRule.getActivity(), "Tests/MaskInv.json");
  }
}
