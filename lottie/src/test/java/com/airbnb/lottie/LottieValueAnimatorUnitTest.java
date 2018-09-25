package com.airbnb.lottie;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;
import androidx.collection.LongSparseArray;
import androidx.collection.SparseArrayCompat;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.utils.LottieValueAnimator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

public class LottieValueAnimatorUnitTest extends BaseTest {
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
    animator = createAnimator();
    composition = createComposition(0, 1000);

    animator.setComposition(composition);
    spyListener = Mockito.mock(Animator.AnimatorListener.class);
    isDone = new AtomicBoolean(false);
  }

  private LottieValueAnimator createAnimator() {
    // Choreographer#postFrameCallback hangs with robolectric.
    return new LottieValueAnimator() {
      @Override public void postFrameCallback() {
        running = true;
      }

      @Override public void removeFrameCallback() {
        running = false;
      }
    };
  }

  private LottieComposition createComposition(int startFrame, int endFrame) {
    LottieComposition composition = new LottieComposition();
    composition.init(new Rect(), startFrame, endFrame, 1000, new ArrayList<Layer>(),
            new LongSparseArray<Layer>(0), new HashMap<String, List<Layer>>(0),
            new HashMap<String, LottieImageAsset>(0), new SparseArrayCompat<FontCharacter>(0),
            new HashMap<String, Font>(0));
    return composition;
  }

  @Test
  public void testInitialState() {
    assertClose(0f, animator.getFrame());
  }

  @Test
  public void testResumingMaintainsValue() {
    animator.setFrame(500);
    animator.resumeAnimation();
    assertClose(500f, animator.getFrame());
  }

  @Test
  public void testFrameConvertsToAnimatedFraction() {
    animator.setFrame(500);
    animator.resumeAnimation();
    assertClose(0.5f, animator.getAnimatedFraction());
    assertClose(0.5f, animator.getAnimatedValueAbsolute());
  }

  @Test
    public void testPlayingResetsValue() {
    animator.setFrame(500);
    animator.playAnimation();
    assertClose(0f, animator.getFrame());
    assertClose(0f, animator.getAnimatedFraction());
  }

  @Test
  public void testReversingMaintainsValue() {
    animator.setFrame(250);
    animator.reverseAnimationSpeed();
    assertClose(250f, animator.getFrame());
    assertClose(0.75f, animator.getAnimatedFraction());
    assertClose(0.25f, animator.getAnimatedValueAbsolute());
  }

  @Test
    public void testReversingWithMinValueMaintainsValue() {
    animator.setMinFrame(100);
    animator.setFrame(1000);
    animator.reverseAnimationSpeed();
    assertClose(1000f, animator.getFrame());
    assertClose(0f, animator.getAnimatedFraction());
    assertClose(1f, animator.getAnimatedValueAbsolute());
  }

  @Test
  public void testReversingWithMaxValueMaintainsValue() {
    animator.setMaxFrame(900);
    animator.reverseAnimationSpeed();
    assertClose(0f, animator.getFrame());
    assertClose(1f, animator.getAnimatedFraction());
    assertClose(0f, animator.getAnimatedValueAbsolute());
  }

  @Test
  public void testResumeReversingWithMinValueMaintainsValue() {
    animator.setMaxFrame(900);
    animator.reverseAnimationSpeed();
    animator.resumeAnimation();
    assertClose(900f, animator.getFrame());
    assertClose(0f, animator.getAnimatedFraction());
    assertClose(0.9f, animator.getAnimatedValueAbsolute());
  }

  @Test
  public void testPlayReversingWithMinValueMaintainsValue() {
    animator.setMaxFrame(900);
    animator.reverseAnimationSpeed();
    animator.playAnimation();
    assertClose(900f, animator.getFrame());
    assertClose(0f, animator.getAnimatedFraction());
    assertClose(0.9f, animator.getAnimatedValueAbsolute());
  }

  @Test
  public void testMinAndMaxBothSet() {
    animator.setMinFrame(200);
    animator.setMaxFrame(800);
    animator.setFrame(400);
    assertClose(0.33333f, animator.getAnimatedFraction());
    assertClose(0.4f, animator.getAnimatedValueAbsolute());
    animator.reverseAnimationSpeed();
    assertClose(400f, animator.getFrame());
    assertClose(0.66666f, animator.getAnimatedFraction());
    assertClose(0.4f, animator.getAnimatedValueAbsolute());
    animator.resumeAnimation();
    assertClose(400f, animator.getFrame());
    assertClose(0.66666f, animator.getAnimatedFraction());
    assertClose(0.4f, animator.getAnimatedValueAbsolute());
    animator.playAnimation();
    assertClose(800f, animator.getFrame());
    assertClose(0f, animator.getAnimatedFraction());
    assertClose(0.8f, animator.getAnimatedValueAbsolute());
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
    assertClose(animator.getMinFrame(), composition.getStartFrame());
  }

  @Test
  public void setMaxFrameLargerThanComposition() {
    animator.setMaxFrame(9000);
    assertClose(animator.getMaxFrame(), composition.getEndFrame());
  }

  @Test
  public void setMinFrameBeforeComposition() {
    LottieValueAnimator animator = createAnimator();
    animator.setMinFrame(100);
    animator.setComposition(composition);
    assertClose(100.0f, animator.getMinFrame());
  }

  @Test
  public void setMaxFrameBeforeComposition() {
    LottieValueAnimator animator = createAnimator();
    animator.setMaxFrame(100);
    animator.setComposition(composition);
    assertClose(100.0f, animator.getMaxFrame());
  }

  @Test
  public void setMinAndMaxFrameBeforeComposition() {
    LottieValueAnimator animator = createAnimator();
    animator.setMinAndMaxFrames(100, 900);
    animator.setComposition(composition);
    assertClose(100.0f, animator.getMinFrame());
    assertClose(900.0f, animator.getMaxFrame());
  }

  @Test
  public void setMinFrameAfterComposition() {
    LottieValueAnimator animator = createAnimator();
    animator.setComposition(composition);
    animator.setMinFrame(100);
    assertClose(100.0f, animator.getMinFrame());
  }

  @Test
  public void setMaxFrameAfterComposition() {
    LottieValueAnimator animator = createAnimator();
    animator.setComposition(composition);
    animator.setMaxFrame(100);
    assertEquals(100.0f, animator.getMaxFrame());
  }

  @Test
  public void setMinAndMaxFrameAfterComposition() {
    LottieValueAnimator animator = createAnimator();
    animator.setComposition(composition);
    animator.setMinAndMaxFrames(100, 900);
    assertClose(100.0f, animator.getMinFrame());
    assertClose(900.0f, animator.getMaxFrame());
  }

  @Test
  public void maxFrameOfNewShorterComposition() {
    LottieValueAnimator animator = createAnimator();
    animator.setComposition(composition);
    LottieComposition composition2 = createComposition(0, 500);
    animator.setComposition(composition2);
    assertClose(500.0f, animator.getMaxFrame());
  }

  @Test
  public void maxFrameOfNewLongerComposition() {
    LottieValueAnimator animator = createAnimator();
    animator.setComposition(composition);
    LottieComposition composition2 = createComposition(0, 1500);
    animator.setComposition(composition2);
    assertClose(1500.0f, animator.getMaxFrame());
  }

  @Test
  public void clearComposition() {
    animator.clearComposition();
    assertClose(0.0f, animator.getMaxFrame());
    assertClose(0.0f, animator.getMinFrame());
  }

  @Test
  public void resetComposition() {
    animator.clearComposition();
    animator.setComposition(composition);
    assertClose(0.0f, animator.getMinFrame());
    assertClose(1000.0f, animator.getMaxFrame());
  }

  @Test
  public void resetAndSetMinBeforeComposition() {
    animator.clearComposition();
    animator.setMinFrame(100);
    animator.setComposition(composition);
    assertClose(100.0f, animator.getMinFrame());
    assertClose(1000.0f, animator.getMaxFrame());
  }

  @Test
  public void resetAndSetMinAterComposition() {
    animator.clearComposition();
    animator.setComposition(composition);
    animator.setMinFrame(100);
    assertClose(100.0f, animator.getMinFrame());
    assertClose(1000.0f, animator.getMaxFrame());
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

  /**
   * Animations don't render on the out frame so if an animation is 1000 frames, the actual end will be 999.99. This causes
   * actual fractions to be something like .74999 when you might expect 75.
   */
  private static void assertClose(float expected, float actual) {
    assertEquals(expected, actual, expected * 0.01f);
  }
}
