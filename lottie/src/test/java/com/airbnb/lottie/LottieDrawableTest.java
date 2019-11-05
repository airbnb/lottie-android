package com.airbnb.lottie;

import android.animation.Animator;
import android.graphics.Rect;
import androidx.collection.LongSparseArray;
import androidx.collection.SparseArrayCompat;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.Marker;
import com.airbnb.lottie.model.layer.Layer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class LottieDrawableTest extends BaseTest {

  @Mock Animator.AnimatorListener animatorListener;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @SuppressWarnings("SameParameterValue")
  private LottieComposition createComposition(int startFrame, int endFrame) {
    LottieComposition composition = new LottieComposition();
    composition.init(new Rect(), startFrame, endFrame, 1000, new ArrayList<Layer>(),
            new LongSparseArray<Layer>(0), new HashMap<String, List<Layer>>(0),
            new HashMap<String, LottieImageAsset>(0), new SparseArrayCompat<FontCharacter>(0),
            new HashMap<String, Font>(0), new ArrayList<Marker>());
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
    assertEquals(121.99f, drawable.getMaxFrame());
  }

  @Test
  public void testMinMaxFrame() {
    LottieComposition composition = createComposition(31, 391);
    LottieDrawable drawable = new LottieDrawable();
    drawable.setComposition(composition);
    drawable.setMinAndMaxProgress(0.25f, 0.42f);
    assertEquals(121f, drawable.getMinFrame());
    assertEquals(182.99f, drawable.getMaxFrame());
  }

  @Test
  public void testPlayWhenSystemAnimationDisabled() {
    LottieComposition composition = createComposition(31, 391);
    LottieDrawable drawable = new LottieDrawable();
    drawable.addAnimatorListener(animatorListener);
    drawable.setSystemAnimationsAreEnabled(false);
    drawable.setComposition(composition);
    drawable.playAnimation();
    assertEquals(391, drawable.getFrame());
    verify(animatorListener, atLeastOnce()).onAnimationEnd(any(Animator.class), eq(false));
  }

  @Test
  public void testResumeWhenSystemAnimationDisabled() {
    LottieComposition composition = createComposition(31, 391);
    LottieDrawable drawable = new LottieDrawable();
    drawable.addAnimatorListener(animatorListener);
    drawable.setSystemAnimationsAreEnabled(false);
    drawable.setComposition(composition);
    drawable.resumeAnimation();
    assertEquals(391, drawable.getFrame());
    verify(animatorListener, atLeastOnce()).onAnimationEnd(any(Animator.class), eq(false));
  }
}
