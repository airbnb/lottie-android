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
    TestRobot.testAnimation(activity, "Tests/CheckSwitch.json");
    TestRobot.testAnimation(activity, "Tests/Fill.json");
    TestRobot.testAnimation(activity, "Tests/Image.json", "Tests/weaccept");
    TestRobot.testAnimation(activity, "Tests/KeyframeTypes.json");
    TestRobot.testAnimation(activity, "Tests/Laugh4.json");
    TestRobot.testAnimation(activity, "Tests/LoopPlayOnce.json");
    TestRobot.testAnimation(activity, "Tests/Parenting.json");
    TestRobot.testAnimation(activity, "Tests/Precomps.json");
    TestRobot.testAnimation(activity, "Tests/ShapeTypes.json");
    TestRobot.testAnimation(activity, "Tests/SplitDimensions.json");
    TestRobot.testAnimation(activity, "Tests/Stroke.json");
    TestRobot.testAnimation(activity, "Tests/TrackMattes.json");
    TestRobot.testAnimation(activity, "Tests/TrimPaths.json");
    TestRobot.testChangingCompositions(activity, "TwitterHeart.json", "PinJump.json");
    TestRobot.testSettingSameComposition(activity, "PinJump.json");
  }
}
