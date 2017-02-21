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
    MainActivity activity = activityRule.getActivity();
    TestRobot.testAnimation(activity, "9squares-AlBoardman.json");
    TestRobot.testAnimation(activity, "EmptyState.json");
    TestRobot.testAnimation(activity, "HamburgerArrow.json");
    TestRobot.testAnimation(activity, "LottieLogo1.json");
    TestRobot.testAnimation(activity, "LottieLogo2.json");
    TestRobot.testAnimation(activity, "MotionCorpse-Jrcanest.json");
    TestRobot.testAnimation(activity, "PinJump.json");
    TestRobot.testAnimation(activity, "TwitterHeart.json");
    TestRobot.testAnimation(activity, "Tests/Hosts.json");
    TestRobot.testAnimation(activity, "Tests/LightBulb.json", null,
        new float[]{0f, 0.05f, 0.10f, 0.2f, 0.3f, 0.4f, 0.5f, 1f});
    TestRobot.testAnimation(activity, "Tests/LoopPlayOnce.json");
    TestRobot.testAnimation(activity, "Tests/Alarm.json");
    TestRobot.testAnimation(activity, "Tests/CheckSwitch.json");
    TestRobot.testAnimation(activity, "Tests/EllipseTrimPath.json");
    TestRobot.testAnimation(activity, "Tests/SplitDimensions.json");
    TestRobot.testAnimation(activity, "Tests/TrimPathsFull.json");
    TestRobot.testAnimation(activity, "Tests/Laugh4.json");
    TestRobot.testAnimation(activity, "Tests/Star.json");
    TestRobot.testAnimation(activity, "Tests/Polygon.json");
    TestRobot.testAnimation(activity, "Tests/AllSet.json");
    TestRobot.testAnimation(activity, "Tests/City.json");
    TestRobot.testAnimation(activity, "Tests/PreCompMadness.json");
    TestRobot.testAnimation(activity, "Tests/MatteParentPrecomp.json");
    TestRobot.testAnimation(activity, "Tests/Image.json", "Tests/weaccept");
    TestRobot.testStatic(activity, "Tests/TrimPathFill.json");
    TestRobot.testStatic(activity, "Tests/Mask_26.json");
    TestRobot.testStatic(activity, "Tests/MatteInv.json");
    TestRobot.testStatic(activity, "Tests/MaskInv.json");
  }
}
