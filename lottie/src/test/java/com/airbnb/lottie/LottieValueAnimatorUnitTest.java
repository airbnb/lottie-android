package com.airbnb.lottie;

import android.graphics.Rect;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SparseArrayCompat;

import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.utils.LottieValueAnimator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class LottieValueAnimatorUnitTest {

  private LottieValueAnimator animator;

  @Before
  public void setup() {
    animator = new LottieValueAnimator();
    LottieComposition composition = new LottieComposition();
    composition.init(new Rect(), 0, 1000, 1000, new ArrayList<Layer>(),
        new LongSparseArray<Layer>(0), new HashMap<String, List<Layer>>(0),
        new HashMap<String, LottieImageAsset>(0), new SparseArrayCompat<FontCharacter>(0),
        new HashMap<String, Font>(0));
    animator.setComposition(composition);
  }

  @Test
  public void testInitialState() {
    assertEquals(0f, animator.getFrame());
  }

  @Test
  public void testResumingMaintainsValue() {
    animator.setFrame(500);
    animator.resumeAnimation();
    assertEquals(500, animator.getFrame());
  }

  @Test
  public void testFrameConvertsToAnimatedFraction() {
    animator.setFrame(500);
    animator.resumeAnimation();
    assertEquals(0.5f, animator.getAnimatedFraction());
  }

  @Test
    public void testPlayingResetsValue() {
    animator.setFrame(500);
    animator.playAnimation();
    assertEquals(0, animator.getFrame());
    assertEquals(0f, animator.getAnimatedFraction());
  }

  @Test
  public void testReversingMaintainsValue() {
    animator.setFrame(250);
    animator.reverseAnimationSpeed();
    assertEquals(250, animator.getFrame());
    assertEquals(0.75f, animator.getAnimatedFraction());
  }

  @Test
    public void testReversingWithMinValueMaintainsValue() {
    animator.setMinFrame(100);
    animator.setFrame(1000);
    animator.reverseAnimationSpeed();
    assertEquals(1000, animator.getFrame());
    assertEquals(0f, animator.getAnimatedFraction());
  }

  @Test
  public void testReversingWithMaxValueMaintainsValue() {
    animator.setMaxFrame(900);
    animator.reverseAnimationSpeed();
    assertEquals(0, animator.getFrame());
    assertEquals(1f, animator.getAnimatedFraction());
  }

  @Test
  public void testResumeReversingWithMinValueMaintainsValue() {
    animator.setMaxFrame(900);
    animator.reverseAnimationSpeed();
    animator.resumeAnimation();
    assertEquals(900, animator.getFrame());
    assertEquals(0f, animator.getAnimatedFraction());
  }

  @Test
  public void testPlayReversingWithMinValueMaintainsValue() {
    animator.setMaxFrame(900);
    animator.reverseAnimationSpeed();
    animator.playAnimation();
    assertEquals(900, animator.getFrame());
    assertEquals(0f, animator.getAnimatedFraction());
  }

  @Test
  public void testMinAndMaxBothSet() {
    animator.setMinFrame(200);
    animator.setMaxFrame(800);
    animator.setFrame(400);
    assertEquals(0.33f, animator.getAnimatedFraction(), 0.01);
    animator.reverseAnimationSpeed();
    assertEquals(400, animator.getFrame());
    assertEquals(0.66f, animator.getAnimatedFraction(), 0.01);
    animator.resumeAnimation();
    assertEquals(400, animator.getFrame());
    assertEquals(0.66f, animator.getAnimatedFraction(), 0.01);
    animator.playAnimation();
    assertEquals(800, animator.getFrame());
    assertEquals(0f, animator.getAnimatedFraction());
  }
}
