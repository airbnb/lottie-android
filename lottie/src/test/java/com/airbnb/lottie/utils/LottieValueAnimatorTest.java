package com.airbnb.lottie.utils;

import android.animation.Animator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.airbnb.lottie.BaseTest;

import org.junit.Test;

public class LottieValueAnimatorTest extends BaseTest {
  @Test
  public void callOfPauseAnimationShouldFireCallbackOnAnimationPause() {
    // Set
    Animator.AnimatorPauseListener listener = mock(Animator.AnimatorPauseListener.class);

    LottieValueAnimator animator = new LottieValueAnimator();
    animator.addPauseListener(listener);

    // Do
    animator.pauseAnimation();

    // Check
    verify(listener, times(1)).onAnimationPause(eq(animator));
    verify(listener, times(0)).onAnimationResume(any());
  }

  @Test
  public void callOfResumeAnimationShouldFireCallbackOnAnimationResume() {
    // Set
    Animator.AnimatorPauseListener listener = mock(Animator.AnimatorPauseListener.class);

    LottieValueAnimator animator = new LottieValueAnimator();
    animator.addPauseListener(listener);

    // Do
    animator.resumeAnimation();

    // Check
    verify(listener, times(0)).onAnimationPause(any());
    verify(listener, times(1)).onAnimationResume(eq(animator));
  }
}
