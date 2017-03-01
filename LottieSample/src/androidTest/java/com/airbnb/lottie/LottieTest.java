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
    TestRobot.testLinearAnimation(activity, "9squares-AlBoardman.json");
    TestRobot.testLinearAnimation(activity, "EmptyState.json");
    TestRobot.testLinearAnimation(activity, "HamburgerArrow.json");
    TestRobot.testLinearAnimation(activity, "LottieLogo1.json");
    TestRobot.testLinearAnimation(activity, "LottieLogo2.json");
    TestRobot.testLinearAnimation(activity, "MotionCorpse-Jrcanest.json");
    TestRobot.testLinearAnimation(activity, "PinJump.json");
    TestRobot.testLinearAnimation(activity, "TwitterHeart.json");
    TestRobot.testLinearAnimation(activity, "Tests/CheckSwitch.json");
    TestRobot.testLinearAnimation(activity, "Tests/Fill.json");
    TestRobot.testLinearAnimation(activity, "Tests/Image.json", "Tests/weaccept");
    TestRobot.testLinearAnimation(activity, "Tests/KeyframeTypes.json");
    TestRobot.testLinearAnimation(activity, "Tests/Laugh4.json");
    TestRobot.testLinearAnimation(activity, "Tests/LoopPlayOnce.json");
    TestRobot.testLinearAnimation(activity, "Tests/Parenting.json");
    TestRobot.testLinearAnimation(activity, "Tests/Precomps.json");
    TestRobot.testLinearAnimation(activity, "Tests/ShapeTypes.json");
    TestRobot.testLinearAnimation(activity, "Tests/SplitDimensions.json");
    TestRobot.testLinearAnimation(activity, "Tests/Stroke.json");
    TestRobot.testLinearAnimation(activity, "Tests/TrackMattes.json");
    TestRobot.testLinearAnimation(activity, "Tests/TrimPaths.json");
    TestRobot.testChangingCompositions(activity, "TwitterHeart.json", "PinJump.json");
    TestRobot.testSettingSameComposition(activity, "PinJump.json");
  }
}
