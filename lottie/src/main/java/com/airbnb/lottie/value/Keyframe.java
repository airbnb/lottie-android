package com.airbnb.lottie.value;

import android.graphics.PointF;
import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import android.view.animation.Interpolator;

import com.airbnb.lottie.LottieComposition;

public class Keyframe<T> {
  @Nullable private final LottieComposition composition;
  @Nullable public final T startValue;
  @Nullable public final T endValue;
  @Nullable public final Interpolator interpolator;
  public final float startFrame;
  @Nullable public Float endFrame;

  private float startProgress = Float.MIN_VALUE;
  private float endProgress = Float.MIN_VALUE;

  // Used by PathKeyframe but it has to be parsed by KeyFrame because we use a JsonReader to
  // deserialzie the data so we have to parse everything in order
  public PointF pathCp1 = null;
  public PointF pathCp2 = null;


  public Keyframe(@SuppressWarnings("NullableProblems") LottieComposition composition,
      @Nullable T startValue, @Nullable T endValue,
      @Nullable Interpolator interpolator, float startFrame, @Nullable Float endFrame) {
    this.composition = composition;
    this.startValue = startValue;
    this.endValue = endValue;
    this.interpolator = interpolator;
    this.startFrame = startFrame;
    this.endFrame = endFrame;
  }

  /**
   * Non-animated value.
   */
  public Keyframe(@SuppressWarnings("NullableProblems") T value) {
    composition = null;
    startValue = value;
    endValue = value;
    interpolator = null;
    startFrame = Float.MIN_VALUE;
    endFrame = Float.MAX_VALUE;
  }

  public float getStartProgress() {
    if (composition == null) {
      return 0f;
    }
    if (startProgress == Float.MIN_VALUE) {
      startProgress = (startFrame  - composition.getStartFrame()) / composition.getDurationFrames();
    }
    return startProgress;
  }

  public float getEndProgress() {
    if (composition == null) {
      return 1f;
    }
    if (endProgress == Float.MIN_VALUE) {
      if (endFrame == null) {
        endProgress = 1f;
      } else {
        float startProgress = getStartProgress();
        float durationFrames = endFrame - startFrame;
        float durationProgress = durationFrames / composition.getDurationFrames();
        endProgress = startProgress + durationProgress;
      }
    }
    return endProgress;
  }

  public boolean isStatic() {
    return interpolator == null;
  }

  public boolean containsProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    return progress >= getStartProgress() && progress < getEndProgress();
  }

  @Override public String toString() {
    return "Keyframe{" + "startValue=" + startValue +
        ", endValue=" + endValue +
        ", startFrame=" + startFrame +
        ", endFrame=" + endFrame +
        ", interpolator=" + interpolator +
        '}';
  }
}
