package com.airbnb.lottie;

import android.graphics.Rect;
import androidx.collection.LongSparseArray;
import androidx.collection.SparseArrayCompat;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.layer.Layer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class LottieDrawableTest extends BaseTest {

  @SuppressWarnings("SameParameterValue")
  private LottieComposition createComposition(int startFrame, int endFrame) {
    LottieComposition composition = new LottieComposition();
    composition.init(new Rect(), startFrame, endFrame, 1000, new ArrayList<Layer>(),
            new LongSparseArray<Layer>(0), new HashMap<String, List<Layer>>(0),
            new HashMap<String, LottieImageAsset>(0), new SparseArrayCompat<FontCharacter>(0),
            new HashMap<String, Font>(0));
    return composition;
  }

  @Test
  public void testMinFrame() {
    LottieComposition composition = createComposition(31, 391);
    LottieDrawable drawable = new LottieDrawable();
    drawable.setComposition(composition);
    drawable.setMinProgress(0.42f);
    assertEquals(182f, drawable.getMinFrame());
  }

  @Test
  public void testMinWithStartFrameFrame() {
    LottieComposition composition = createComposition(100, 200);
    LottieDrawable drawable = new LottieDrawable();
    drawable.setComposition(composition);
    drawable.setMinProgress(0.5f);
    assertEquals(150f, drawable.getMinFrame());
  }

  @Test
  public void testMaxFrame() {
    LottieComposition composition = createComposition(31, 391);
    LottieDrawable drawable = new LottieDrawable();
    drawable.setComposition(composition);
    drawable.setMaxProgress(0.25f);
    assertEquals(121f, drawable.getMaxFrame());
  }

  @Test
  public void testMinMaxFrame() {
    LottieComposition composition = createComposition(31, 391);
    LottieDrawable drawable = new LottieDrawable();
    drawable.setComposition(composition);
    drawable.setMinAndMaxProgress(0.25f, 0.42f);
    assertEquals(121f, drawable.getMinFrame());
    assertEquals(182f, drawable.getMaxFrame());
  }
}
