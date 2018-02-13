package com.airbnb.lottie.utils;

import android.animation.ValueAnimator;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.view.Choreographer;

import com.airbnb.lottie.LottieComposition;

/**
 * This is a slightly modified {@link ValueAnimator} that allows us to update start and end values
 * easily optimizing for the fact that we know that it's a value animator with 2 floats.
 */
public class LottieValueAnimator extends BaseLottieAnimator implements Choreographer.FrameCallback {


  private float speed = 1f;
  private long frameTime = 0;
  private float frame = 0;
  private int repeatCount = 0;
  private float minFrame = Integer.MIN_VALUE;
  private float maxFrame = Integer.MAX_VALUE;
  @Nullable private LottieComposition composition;
  @VisibleForTesting protected boolean isRunning = false;

  public LottieValueAnimator() {
  }

  @Override public Object getAnimatedValue() {
    return getAnimatedFraction();
  }

  @Override public float getAnimatedFraction() {
    if (composition == null) {
      return 0;
    }
    if (isReversed()) {
      return (getMaxFrame() - frame) / (getMaxFrame() - getMinFrame());
    } else {
      return (frame - getMinFrame()) / (getMaxFrame() - getMinFrame());
    }
  }

  @Override public long getDuration() {
    return composition == null ? 0 : (long) composition.getDuration();
  }

  public float getFrame() {
    return frame;
  }

  @Override public boolean isRunning() {
    return isRunning;
  }

  public void systemAnimationsAreDisabled() {
    // Delete me?
  }

  @Override public void doFrame(long frameTimeNanos) {
    postFrameCallback();
    if (composition == null || !isRunning()) {
      return;
    }

    long now = System.nanoTime();
    long timeSinceFrame = now - frameTime;
    float frameDuration = getFrameDurationNs();
    float frames = timeSinceFrame / frameDuration;
    int wholeFrames = (int) frames;
    if (wholeFrames == 0) {
      return;
    }
    frame += isReversed() ? -wholeFrames : wholeFrames;
    boolean ended = !MiscUtils.contains(frame, getMinFrame(), getMaxFrame());
    frame = MiscUtils.clamp(frame, getMinFrame(), getMaxFrame());

    float partialFramesDuration = (frames - wholeFrames) * frameDuration;
    frameTime = (long) (now - partialFramesDuration);

    notifyUpdate();
    if (ended) {
      if (getRepeatCount() != INFINITE && repeatCount >= getRepeatCount()) {
        frame = getMaxFrame();
        notifyEnd(isReversed());
        removeFrameCallback();
      } else {
        notifyRepeat();
        repeatCount++;
        if (getRepeatMode() == REVERSE) {
          reverseAnimationSpeed();
        } else {
          frame = getMinFrame();
        }
        frameTime = now;
      }
    }
  }

  private float getFrameDurationNs() {
    if (composition == null) {
      return Float.MAX_VALUE;
    }
    return Utils.SECOND_IN_NANOS / composition.getFrameRate() / Math.abs(speed);
  }

  public void setComposition(LottieComposition composition) {
    this.composition = composition;
    frame = 0;
    frameTime = System.nanoTime();
  }

  public void setFrame(int frame) {
    if (this.frame == frame) {
      return;
    }
    this.frame = frame;
    frameTime = System.nanoTime();
    notifyUpdate();
  }

  public void setMinFrame(int minFrame) {
    this.minFrame = minFrame;
    if (frame < minFrame) {
      frame = minFrame;
    }
  }

  public void setMaxFrame(int maxFrame) {
    this.maxFrame = maxFrame;
    if (frame > maxFrame) {
      frame = maxFrame;
    }
  }

  public void setMinAndMaxFrames(int minFrame, int maxFrame) {
    this.minFrame = minFrame;
    this.maxFrame = maxFrame;
    frame = MiscUtils.clamp(frame, minFrame, maxFrame);
  }

  public void reverseAnimationSpeed() {
    setSpeed(-getSpeed());
  }

  public void setSpeed(float speed) {
    this.speed = speed;
  }

  public float getSpeed() {
    return speed;
  }

  public void playAnimation() {
    frame = isReversed() ? getMaxFrame() : getMinFrame();
    frameTime = System.nanoTime();
    repeatCount = 0;
    postFrameCallback();
    notifyStart(isReversed());
  }

  public void endAnimation() {
    removeFrameCallback();
    notifyEnd(isReversed());
  }

  public void pauseAnimation() {
    removeFrameCallback();
    // TODO: do we have to call cancel?
  }

  public void resumeAnimation() {
    postFrameCallback();
    frameTime = System.nanoTime();
    if (isReversed() && getFrame() == getMinFrame()) {
      frame = getMaxFrame();
    } else if (!isReversed() && getFrame() == getMaxFrame()) {
      frame = getMinFrame();
    }
  }

  @Override public void cancel() {
    notifyCancel();
    removeFrameCallback();
  }

  private boolean isReversed() {
    return speed < 0;
  }

  private float getMinFrame() {
    if (composition == null) {
      return 0;
    }
    return minFrame == Integer.MIN_VALUE ? 0 : minFrame;
  }

  private float getMaxFrame() {
    if (composition == null) {
      return 0;
    }
    return maxFrame == Integer.MAX_VALUE ? composition.getEndFrame() : maxFrame;
  }

  protected void postFrameCallback() {
    removeFrameCallback();
    Choreographer.getInstance().postFrameCallback(this);
    isRunning = true;
  }

  protected void removeFrameCallback() {
    Choreographer.getInstance().removeFrameCallback(this);
    isRunning = false;
  }
}
