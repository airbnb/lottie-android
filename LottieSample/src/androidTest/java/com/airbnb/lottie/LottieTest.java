package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.airbnb.lottie.samples.MainActivity;
import com.airbnb.lottie.samples.TestColorFilterActivity;

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
  @Rule public ActivityTestRule<MainActivity> mainActivityRule = new ActivityTestRule<>(
      MainActivity.class);

  @Rule public ActivityTestRule<TestColorFilterActivity> colorFilterActivityRule = new ActivityTestRule<>(
      TestColorFilterActivity.class);

  @Test public void testAll() {
    MainActivity activity = mainActivityRule.getActivity();
    // TestRobot.testLinearAnimation(activity, "9squares-AlBoardman.json");
    // TestRobot.testLinearAnimation(activity, "EmptyState.json");
    // TestRobot.testLinearAnimation(activity, "HamburgerArrow.json");
    // TestRobot.testLinearAnimation(activity, "LottieLogo1.json");
    // TestRobot.testLinearAnimation(activity, "LottieLogo2.json");
    // TestRobot.testLinearAnimation(activity, "MotionCorpse-Jrcanest.json");
    // TestRobot.testLinearAnimation(activity, "PinJump.json");
    // TestRobot.testLinearAnimation(activity, "TwitterHeart.json");
    // TestRobot.testLinearAnimation(activity, "Hello World.json");
    // TestRobot.testLinearAnimation(activity, "WeAccept.json", "Images/WeAccept", null);
    // TextDelegate delegate = new TextDelegate();
    // delegate.setText("NAME", "Gabe");
    // TestRobot.testLinearAnimation(activity, "Name.json", null, delegate);
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Beating Heart.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Progress Success.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Touch ID.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Loading 1.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Loading 2.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Loading 3.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Loading 4.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Retweet.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Camera.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Video Camera.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Emoji Tongue.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Emoji Wink.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Emoji Shock.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Im Thirsty.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Mail Sent.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Play and Like It.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Gears.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Permission.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Postcard.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Preloader.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Star Wars Rey.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Tadah Image.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Tadah Video.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - Favorite Star.json");
    // TestRobot.testLinearAnimation(activity, "lottiefiles.com - ATM.json");
    // TestRobot.testLinearAnimation(activity, "Tests/CheckSwitch.json");
    // TestRobot.testLinearAnimation(activity, "Tests/Fill.json");
    // TestRobot.testLinearAnimation(activity, "Tests/GradientFill.json");
    // TestRobot.testLinearAnimation(activity, "Tests/KeyframeTypes.json");
    // TestRobot.testLinearAnimation(activity, "Tests/Laugh4.json");
    // TestRobot.testLinearAnimation(activity, "Tests/LoopPlayOnce.json");
    // TestRobot.testLinearAnimation(activity, "Tests/Parenting.json");
    // TestRobot.testLinearAnimation(activity, "Tests/Precomps.json");
    // TestRobot.testLinearAnimation(activity, "Tests/Remap.json");
    // TestRobot.testLinearAnimation(activity, "Tests/Repeater.json");
    // TestRobot.testLinearAnimation(activity, "Tests/ShapeTypes.json");
    // TestRobot.testLinearAnimation(activity, "Tests/SplitDimensions.json");
    // TestRobot.testLinearAnimation(activity, "Tests/Stroke.json");
    // TestRobot.testLinearAnimation(activity, "Tests/TimeStretch.json");
    // TestRobot.testLinearAnimation(activity, "Tests/TrackMattes.json");
    // TestRobot.testLinearAnimation(activity, "Tests/TrimPaths.json");
    // TestRobot.testChangingCompositions(activity, "TwitterHeart.json", "PinJump.json");
    // TestRobot.testSettingSameComposition(activity, "PinJump.json");
    //
    // TestColorFilterActivity colorFilterActivity = colorFilterActivityRule.getActivity();
    // TestRobot.testAddYellowColorFilterInXml(colorFilterActivity);
    // TestRobot.testAddNullColorFilterInXml(colorFilterActivity);

    testScaleTypes();
  }

  private void testScaleTypes() {
    Context context = mainActivityRule.getActivity();

    LottieComposition composition = LottieComposition.Factory.fromFileSync(
        context, "LottieLogo1.json");

    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    TestRobot.testAnimationView(context, composition, "Wrap Content", params, null);

    params = new FrameLayout.LayoutParams(dpToPx(300), dpToPx(300));
    TestRobot.testAnimationView(context, composition, "300dp centerCrop", params,
        ImageView.ScaleType.CENTER_CROP);

    params = new FrameLayout.LayoutParams(dpToPx(300), dpToPx(300));
    TestRobot.testAnimationView(context, composition, "300dp centerInside", params,
        ImageView.ScaleType.CENTER_INSIDE);

    params = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    TestRobot.testAnimationView(context, composition, "Match Parent", params, null);
  }

  private int dpToPx(int dp) {
    Resources resources = mainActivityRule.getActivity().getResources();
    return (int) TypedValue.applyDimension(1, (float) dp, resources.getDisplayMetrics());
  }
}
