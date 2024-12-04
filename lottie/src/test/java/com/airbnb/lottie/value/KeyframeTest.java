package com.airbnb.lottie.value;

import static org.junit.Assert.*;

import android.graphics.Rect;
import android.view.animation.LinearInterpolator;
import androidx.collection.LongSparseArray;
import androidx.collection.SparseArrayCompat;
import com.airbnb.lottie.LottieComposition;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class KeyframeTest {

  @Test
  public void testStartFrame() {
    LottieComposition composition = new LottieComposition();
    composition.init(
        new Rect(),
        0f,
        504.99f,
        60f,
        new ArrayList<>(),
        new LongSparseArray<>(),
        new HashMap<>(),
        new HashMap<>(),
        1f,
        new SparseArrayCompat<>(),
        new HashMap<>(),
        new ArrayList<>(),
        0,
        0
    );
    Keyframe<Float> keyframe1 = new Keyframe<>(composition, 200f, 321f, new LinearInterpolator(), 28f, 48f);
    Keyframe<Float> keyframe2 = new Keyframe<>(composition, 321f, 300f, new LinearInterpolator(), 48f, 56f);
    assertEquals(keyframe2.getStartProgress(), keyframe1.getEndProgress(), 0f);
  }
}
