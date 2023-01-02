package com.airbnb.lottie.utils;

import android.animation.ValueAnimator;
import android.view.Choreographer;

import androidx.annotation.FloatRange;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;

/**
 * This is a slightly modified {@link ValueAnimator} that allows us to update start and end values
 * easily optimizing for the fact that we know that it's a value animator with 2 floats.
 */
public class LottieValueAnimator extends BaseLottieAnimator implements Choreographer.FrameCallback {


  private float speed = 1f;
  private boolean speedReversedForRepeatMode = false;
  private long lastFrameTimeNs = 0;
  private float frameRaw = 0;
  private float frame = 0;
  private int repeatCount = 0;
  private float minFrame = Integer.MIN_VALUE;
  private float maxFrame = Integer.MAX_VALUE;
  @Nullable private LottieComposition composition;
  @VisibleForTesting protected boolean running = false;
  private boolean useCompositionFrameRate = false;

  public LottieValueAnimator() {
  }

  /**
   * Returns a float representing the current value of the animation from 0 to 1
   * regardless of the animation speed, direction, or min and max frames.
   */
  @Override public Object getAnimatedValue() {
    return getAnimatedValueAbsolute();
  }

  /**
   * Returns the current value of the animation from 0 to 1 regardless
   * of the animation speed, direction, or min and max frames.
   */
  @FloatRange(from = 0f, to = 1f) public float getAnimatedValueAbsolute() {
    if (composition == null) {
      return 0;
    }
    return (frame - composition.getStartFrame()) / (composition.getEndFrame() - composition.getStartFrame());

  }

  /**
   * Returns the current value of the currently playing animation taking into
   * account direction, min and max frames.
   */
  @Override @FloatRange(from = 0f, to = 1f) public float getAnimatedFraction() {
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
    return running;
  }

  public void setUseCompositionFrameRate(boolean useCompositionFrameRate) {
    this.useCompositionFrameRate = useCompositionFrameRate;
  }

  @Override public void doFrame(long frameTimeNanos) {
    postFrameCallback();
    if (composition == null || !isRunning()) {
      return;
    }

    L.beginSection("LottieValueAnimator#doFrame");
    long timeSinceFrame = lastFrameTimeNs == 0 ? 0 : frameTimeNanos - lastFrameTimeNs;
    float frameDuration = getFrameDurationNs();
    float dFrames = timeSinceFrame / frameDuration;

    float newFrameRaw = frameRaw + (isReversed() ? -dFrames : dFrames);
    boolean ended = !MiscUtils.contains(newFrameRaw, getMinFrame(), getMaxFrame());
    float previousFrameRaw = frameRaw;
    frameRaw = MiscUtils.clamp(newFrameRaw, getMinFrame(), getMaxFrame());
    frame = useCompositionFrameRate ? (float) Math.floor(frameRaw) : frameRaw;

    lastFrameTimeNs = frameTimeNanos;

    if (!useCompositionFrameRate || frameRaw != previousFrameRaw) {
      notifyUpdate();
    }
    if (ended) {
      if (getRepeatCount() != INFINITE && repeatCount >= getRepeatCount()) {
        frameRaw = speed < 0 ? getMinFrame() : getMaxFrame();
        frame = frameRaw;
        removeFrameCallback();
        notifyEnd(isReversed());
      } else {
        notifyRepeat();
        repeatCount++;
        if (getRepeatMode() == REVERSE) {
          speedReversedForRepeatMode = !speedReversedForRepeatMode;
          reverseAnimationSpeed();
        } else {
          frameRaw = isReversed() ? getMaxFrame() : getMinFrame();
          frame = frameRaw;
        }
        lastFrameTimeNs = frameTimeNanos;
      }
    }

    verifyFrame();
    L.endSection("LottieValueAnimator#doFrame");
  }

  private float getFrameDurationNs() {
    if (composition == null) {
      return Float.MAX_VALUE;
    }
    return Utils.SECOND_IN_NANOS / composition.getFrameRate() / Math.abs(speed);
  }

  public void clearComposition() {
    this.composition = null;
    minFrame = Integer.MIN_VALUE;
    maxFrame = Integer.MAX_VALUE;
  }

  public void setComposition(LottieComposition composition) {
    // Because the initial composition is loaded async, the first min/max frame may be set
    boolean keepMinAndMaxFrames = this.composition == null;
    this.composition = composition;

    if (keepMinAndMaxFrames) {
      setMinAndMaxFrames(
          Math.max(this.minFrame, composition.getStartFrame()),
          Math.min(this.maxFrame, composition.getEndFrame())
      );
    } else {
      setMinAndMaxFrames((int) composition.getStartFrame(), (int) composition.getEndFrame());
    }
    float frame = this.frame;
    this.frame = 0f;
    this.frameRaw = 0f;
    setFrame((int) frame);
    notifyUpdate();
  }

  public void setFrame(float frame) {
    if (this.frameRaw == frame) {
      return;
    }
    this.frameRaw = MiscUtils.clamp(frame, getMinFrame(), getMaxFrame());
    this.frame = useCompositionFrameRate ? ((float) Math.floor(frameRaw)) : frameRaw;
    lastFrameTimeNs = 0;
    notifyUpdate();
  }

  public void setMinFrame(int minFrame) {
    setMinAndMaxFrames(minFrame, (int) maxFrame);
  }

  public void setMaxFrame(float maxFrame) {
    setMinAndMaxFrames(minFrame, maxFrame);
  }

  public void setMinAndMaxFrames(float minFrame, float maxFrame) {
    if (minFrame > maxFrame) {
      throw new IllegalArgumentException(String.format("minFrame (%s) must be <= maxFrame (%s)", minFrame, maxFrame));
    }
    float compositionMinFrame = composition == null ? -Float.MAX_VALUE : composition.getStartFrame();
    float compositionMaxFrame = composition == null ? Float.MAX_VALUE : composition.getEndFrame();
    float newMinFrame = MiscUtils.clamp(minFrame, compositionMinFrame, compositionMaxFrame);
    float newMaxFrame = MiscUtils.clamp(maxFrame, compositionMinFrame, compositionMaxFrame);
    if (newMinFrame != this.minFrame || newMaxFrame != this.maxFrame) {
      this.minFrame = newMinFrame;
      this.maxFrame = newMaxFrame;
      setFrame((int) MiscUtils.clamp(frame, newMinFrame, newMaxFrame));
    }
  }

  public void reverseAnimationSpeed() {
    setSpeed(-getSpeed());
  }

  public void setSpeed(float speed) {
    this.speed = speed;
  }

  /**
   * Returns the current speed. This will be affected by repeat mode REVERSE.
   */
  public float getSpeed() {
    return speed;
  }

  @Override public void setRepeatMode(int value) {
    super.setRepeatMode(value);
    if (value != REVERSE && speedReversedForRepeatMode) {
      speedReversedForRepeatMode = false;
      reverseAnimationSpeed();
    }
  }

  @MainThread
  public void playAnimation() {
    running = true;
    notifyStart(isReversed());
    setFrame((int) (isReversed() ? getMaxFrame() : getMinFrame()));
    lastFrameTimeNs = 0;
    repeatCount = 0;
    postFrameCallback();
  }

  @MainThread
  public void endAnimation() {
    removeFrameCallback();
    notifyEnd(isReversed());
  }

  @MainThread
  public void pauseAnimation() {
    removeFrameCallback();
    notifyPause();
  }

  @MainThread
  public void resumeAnimation() {
    running = true;
    postFrameCallback();
    lastFrameTimeNs = 0;
    if (isReversed() && getFrame() == getMinFrame()) {
      setFrame(getMaxFrame());
    } else if (!isReversed() && getFrame() == getMaxFrame()) {
      setFrame(getMinFrame());
    }
    notifyResume();
  }

  @MainThread
  @Override public void cancel() {
    notifyCancel();
    removeFrameCallback();
  }

  private boolean isReversed() {
    return getSpeed() < 0;
  }

  public float getMinFrame() {
    if (composition == null) {
      return 0;
    }
    return minFrame == Integer.MIN_VALUE ? composition.getStartFrame() : minFrame;
  }

  public float getMaxFrame() {
    if (composition == null) {
      return 0;
    }
    return maxFrame == Integer.MAX_VALUE ? composition.getEndFrame() : maxFrame;
  }

  @Override void notifyCancel() {
    super.notifyCancel();
    notifyEnd(isReversed());
  }

  protected void postFrameCallback() {
    if (isRunning()) {
      removeFrameCallback(false);
      Choreographer.getInstance().postFrameCallback(this);
    }
  }

  @MainThread
  protected void removeFrameCallback() {
    this.removeFrameCallback(true);
  }

  @MainThread
  protected void removeFrameCallback(boolean stopRunning) {
    Choreographer.getInstance().removeFrameCallback(this);
    if (stopRunning) {
      running = false;
    }
  }

  private void verifyFrame() {
    if (composition == null) {
      return;
    }
    if (frame < minFrame || frame > maxFrame) {
      throw new IllegalStateException(String.format("Frame must be [%f,%f]. It is %f", minFrame, maxFrame, frame));
    }
  }
}
