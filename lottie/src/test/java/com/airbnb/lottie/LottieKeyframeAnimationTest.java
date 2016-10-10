package com.airbnb.lottie;


import android.view.animation.Interpolator;

import com.airbnb.lottie.utils.LottieNumberKeyframeAnimation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LottieKeyframeAnimationTest {
  @Test
  public void simpleAnimation() {

    List<Float> keyTimes = Arrays.asList(0f, 0.5f, 1f);
    List<Float> values = Arrays.asList(0f, 1f, 10f);

    LottieNumberKeyframeAnimation<Float> animation = new LottieNumberKeyframeAnimation<>(1000, 1000, keyTimes, Float.class, values, new ArrayList<Interpolator>(0));

    animation.setProgress(0f);
    assertEquals(animation.getValue(), 0f);
    animation.setProgress(0.25f);
    assertEquals(animation.getValue(), 0.5f);
    animation.setProgress(0.5f);
    assertEquals(animation.getValue(), 1f);
    animation.setProgress(0.75f);
    assertEquals(animation.getValue(), 5.5f);
    animation.setProgress(1f);
    assertEquals(animation.getValue(), 10f);
  }
}