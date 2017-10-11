package com.airbnb.lottie;

import com.airbnb.lottie.utils.LottieValueAnimator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class LottieValueAnimatorUnitTest {

  private LottieValueAnimator animator;

  @Before
  public void setup() {
    animator = new LottieValueAnimator();
    animator.setCompositionDuration(1000);
  }

  @Test
  public void testInitialState() {
    assertEquals(0f, animator.getValue());
  }

  @Test
    public void testResumingMaintainsValue() {
    animator.setValue(0.5f);
    animator.resumeAnimation();
    assertEquals(0.5f, animator.getValue());
  }

  @Test
    public void testPlayingResetsValue() {
    animator.setValue(0.5f);
    animator.playAnimation();
    assertEquals(0f, animator.getValue());
    assertEquals(0f, animator.getAnimatedFraction());
  }

  @Test
  public void testReversingMaintainsValue() {
    animator.setValue(0.25f);
    animator.reverseAnimationSpeed();
    assertEquals(0.25f, animator.getValue());
    assertEquals(0.75f, animator.getAnimatedFraction());
  }

  @Test
    public void testReversingWithMinValueMaintainsValue() {
    animator.setMinValue(0.1f);
    animator.setValue(1f);
    animator.reverseAnimationSpeed();
    assertEquals(1f, animator.getValue());
    assertEquals(0f, animator.getAnimatedFraction());
  }

  @Test
  public void testReversingWithMaxValueMaintainsValue() {
    animator.setMaxValue(0.9f);
    animator.reverseAnimationSpeed();
    assertEquals(0f, animator.getValue());
    assertEquals(1f, animator.getAnimatedFraction());
  }

  @Test
  public void testResumeReversingWithMinValueMaintainsValue() {
    animator.setMaxValue(0.9f);
    animator.reverseAnimationSpeed();
    animator.resumeAnimation();
    assertEquals(0.9f, animator.getValue());
    assertEquals(0f, animator.getAnimatedFraction());
  }

  @Test
  public void testPlayReversingWithMinValueMaintainsValue() {
    animator.setMaxValue(0.9f);
    animator.reverseAnimationSpeed();
    animator.playAnimation();
    assertEquals(0.9f, animator.getValue());
    assertEquals(0f, animator.getAnimatedFraction());
  }

  @Test
  public void testMinAndMaxBothSet() {
    animator.setMinValue(0.2f);
    animator.setMaxValue(0.8f);
    animator.setValue(0.4f);
    assertEquals(0.33f, animator.getAnimatedFraction(), 0.01);
    animator.reverseAnimationSpeed();
    assertEquals(0.4f, animator.getValue(), 0.01);
    assertEquals(0.66f, animator.getAnimatedFraction(), 0.01);
    animator.resumeAnimation();
    assertEquals(0.4f, animator.getValue(), 0.01);
    assertEquals(0.66f, animator.getAnimatedFraction(), 0.01);
    animator.playAnimation();
    assertEquals(0.8f, animator.getValue());
    assertEquals(0f, animator.getAnimatedFraction());
  }
}
