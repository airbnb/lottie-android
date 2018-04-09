package com.airbnb.lottie;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class LottieValueAnimatorUnitTest {
  private interface VerifyListener {
    void verify(InOrder inOrder);
  }

  private LottieComposition composition;
  private LottieValueAnimator animator;
  private Animator.AnimatorListener spyListener;
  private InOrder inOrder;
  private AtomicBoolean isDone;

  @Before
  public void setup() {
    // Choreographer#postFrameCallback hangs with robolectric.
    animator = new LottieValueAnimator() {
      @Override public void postFrameCallback() {
        isRunning = true;
      }

      @Override public void removeFrameCallback() {
        isRunning = false;
      }
    };
    composition = new LottieComposition();
    composition.init(new Rect(), 0, 1000, 1000, new ArrayList<Layer>(),
        new LongSparseArray<Layer>(0), new HashMap<String, List<Layer>>(0),
        new HashMap<String, LottieImageAsset>(0), new SparseArrayCompat<FontCharacter>(0),
        new HashMap<String, Font>(0));
    animator.setComposition(composition);
    spyListener = Mockito.mock(Animator.AnimatorListener.class);
    isDone = new AtomicBoolean(false);
  }

  @Test
  public void testInitialState() {
    assertEquals(0f, animator.getFrame());
  }

  @Test
  public void testResumingMaintainsValue() {
    animator.setFrame(500);
    animator.resumeAnimation();
    assertEquals(500f, animator.getFrame());
  }

  @Test
  public void testFrameConvertsToAnimatedFraction() {
    animator.setFrame(500);
    animator.resumeAnimation();
    assertEquals(0.5f, animator.getAnimatedFraction());
    assertEquals(0.5f, animator.getAnimatedValueAbsolute());
  }

  @Test
    public void testPlayingResetsValue() {
    animator.setFrame(500);
    animator.playAnimation();
    assertEquals(0f, animator.getFrame());
    assertEquals(0f, animator.getAnimatedFraction());
  }

  @Test
  public void testReversingMaintainsValue() {
    animator.setFrame(250);
    animator.reverseAnimationSpeed();
    assertEquals(250f, animator.getFrame());
    assertEquals(0.75f, animator.getAnimatedFraction());
    assertEquals(0.25f, animator.getAnimatedValueAbsolute());
  }

  @Test
    public void testReversingWithMinValueMaintainsValue() {
    animator.setMinFrame(100);
    animator.setFrame(1000);
    animator.reverseAnimationSpeed();
    assertEquals(1000f, animator.getFrame());
    assertEquals(0f, animator.getAnimatedFraction());
    assertEquals(1f, animator.getAnimatedValueAbsolute());
  }

  @Test
  public void testReversingWithMaxValueMaintainsValue() {
    animator.setMaxFrame(900);
    animator.reverseAnimationSpeed();
    assertEquals(0f, animator.getFrame());
    assertEquals(1f, animator.getAnimatedFraction());
    assertEquals(0f, animator.getAnimatedValueAbsolute());
  }

  @Test
  public void testResumeReversingWithMinValueMaintainsValue() {
    animator.setMaxFrame(900);
    animator.reverseAnimationSpeed();
    animator.resumeAnimation();
    assertEquals(900f, animator.getFrame());
    assertEquals(0f, animator.getAnimatedFraction());
    assertEquals(0.9f, animator.getAnimatedValueAbsolute());
  }

  @Test
  public void testPlayReversingWithMinValueMaintainsValue() {
    animator.setMaxFrame(900);
    animator.reverseAnimationSpeed();
    animator.playAnimation();
    assertEquals(900f, animator.getFrame());
    assertEquals(0f, animator.getAnimatedFraction());
    assertEquals(0.9f, animator.getAnimatedValueAbsolute());
  }

  @Test
  public void testMinAndMaxBothSet() {
    animator.setMinFrame(200);
    animator.setMaxFrame(800);
    animator.setFrame(400);
    assertEquals(0.33f, animator.getAnimatedFraction(), 0.01);
    assertEquals(0.4f, animator.getAnimatedValueAbsolute());
    animator.reverseAnimationSpeed();
    assertEquals(400f, animator.getFrame());
    assertEquals(0.66f, animator.getAnimatedFraction(), 0.01);
    assertEquals(0.4f, animator.getAnimatedValueAbsolute());
    animator.resumeAnimation();
    assertEquals(400f, animator.getFrame());
    assertEquals(0.66f, animator.getAnimatedFraction(), 0.01);
    assertEquals(0.4f, animator.getAnimatedValueAbsolute());
    animator.playAnimation();
    assertEquals(800f, animator.getFrame());
    assertEquals(0f, animator.getAnimatedFraction());
    assertEquals(0.8f, animator.getAnimatedValueAbsolute());
  }

  @Test
  public void testDefaultAnimator() {
    testAnimator(new VerifyListener() {
      @Override public void verify(InOrder inOrder) {
        inOrder.verify(spyListener, times(1)).onAnimationStart(animator, false);
        inOrder.verify(spyListener, times(1)).onAnimationEnd(animator, false);
        Mockito.verify(spyListener, times(0)).onAnimationCancel(animator);
        Mockito.verify(spyListener, times(0)).onAnimationRepeat(animator);
      }
    });
  }

  @Test
  public void testReverseAnimator() {
    animator.reverseAnimationSpeed();
    testAnimator(new VerifyListener() {
      @Override public void verify(InOrder inOrder) {
        inOrder.verify(spyListener, times(1)).onAnimationStart(animator, true);
        inOrder.verify(spyListener, times(1)).onAnimationEnd(animator, true);
        Mockito.verify(spyListener, times(0)).onAnimationCancel(animator);
        Mockito.verify(spyListener, times(0)).onAnimationRepeat(animator);
      }
    });
  }

  @Test
  public void testLoopingAnimatorOnce() {
    animator.setRepeatCount(1);
    testAnimator(new VerifyListener() {
      @Override public void verify(InOrder inOrder) {
        Mockito.verify(spyListener, times(1)).onAnimationStart(animator, false);
        Mockito.verify(spyListener, times(1)).onAnimationRepeat(animator);
        Mockito.verify(spyListener, times(1)).onAnimationEnd(animator, false);
        Mockito.verify(spyListener, times(0)).onAnimationCancel(animator);
      }
    });
  }

  @Test
  public void testLoopingAnimatorZeroTimes() {
    animator.setRepeatCount(0);
    testAnimator(new VerifyListener() {
      @Override public void verify(InOrder inOrder) {
        Mockito.verify(spyListener, times(1)).onAnimationStart(animator, false);
        Mockito.verify(spyListener, times(0)).onAnimationRepeat(animator);
        Mockito.verify(spyListener, times(1)).onAnimationEnd(animator, false);
        Mockito.verify(spyListener, times(0)).onAnimationCancel(animator);
      }
    });
  }

  @Test
  public void testLoopingAnimatorTwice() {
    animator.setRepeatCount(2);
    testAnimator(new VerifyListener() {
      @Override public void verify(InOrder inOrder) {
        Mockito.verify(spyListener, times(1)).onAnimationStart(animator, false);
        Mockito.verify(spyListener, times(2)).onAnimationRepeat(animator);
        Mockito.verify(spyListener, times(1)).onAnimationEnd(animator, false);
        Mockito.verify(spyListener, times(0)).onAnimationCancel(animator);
      }
    });
  }

  @Test
  public void testLoopingAnimatorOnceReverse() {
    animator.setFrame(1000);
    animator.setRepeatCount(1);
    animator.reverseAnimationSpeed();
    testAnimator(new VerifyListener() {
      @Override public void verify(InOrder inOrder) {
        inOrder.verify(spyListener, times(1)).onAnimationStart(animator, true);
        inOrder.verify(spyListener, times(1)).onAnimationRepeat(animator);
        inOrder.verify(spyListener, times(1)).onAnimationEnd(animator, true);
        Mockito.verify(spyListener, times(0)).onAnimationCancel(animator);
      }
    });
  }

  @Test
  public void setMinFrameSmallerThanComposition() {
    animator.setMinFrame(-9000);
    assertEquals(animator.getMinFrame(), composition.getStartFrame());
  }

  @Test
  public void setMaxFrameLargerThanComposition() {
    animator.setMaxFrame(9000);
    assertEquals(animator.getMaxFrame(), composition.getEndFrame());
  }

  private void testAnimator(final VerifyListener verifyListener) {
    spyListener = Mockito.spy(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        verifyListener.verify(inOrder);
        isDone.set(true);
      }
    });
    inOrder = inOrder(spyListener);
    animator.addListener(spyListener);

    animator.playAnimation();
    while (!isDone.get()) {
      animator.doFrame(System.nanoTime());
    }
  }
}
