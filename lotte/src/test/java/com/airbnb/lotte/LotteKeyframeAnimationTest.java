package com.airbnb.lotte;


import com.airbnb.lotte.utils.LotteKeyframeAnimation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LotteKeyframeAnimationTest {
  @Test
  public void simpleAnimation() throws Exception {

    List<Float> keyTimes = Arrays.asList(0f, 0.5f, 1f);
    List<Float> values = Arrays.asList(0f, 1f, 10f);

    LotteKeyframeAnimation animation = new LotteKeyframeAnimation("alpha", 1000, keyTimes, values);

    assertEquals(animation.getValueForProgress(0f), 0f);
    assertEquals(animation.getValueForProgress(0.25f), 0.5f);
    assertEquals(animation.getValueForProgress(0.5f), 1f);
    assertEquals(animation.getValueForProgress(0.75f), 5.5f);
    assertEquals(animation.getValueForProgress(1f), 10f);
  }
}