package com.airbnb.lottie;

import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class LottiePropertyTest extends BaseTest {

  @Test
  public void dropShadowPropertiesDoNotCollideWithTransformOrBlur() {
    assertNotEquals(LottieProperty.TRANSFORM_POSITION_X, LottieProperty.DROP_SHADOW_OPACITY);
    assertNotEquals(LottieProperty.TRANSFORM_POSITION_Y, LottieProperty.DROP_SHADOW_DIRECTION);
    assertNotEquals(LottieProperty.BLUR_RADIUS, LottieProperty.DROP_SHADOW_DISTANCE);
  }
}
