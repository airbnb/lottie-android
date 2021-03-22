package com.airbnb.lottie.value;

import android.graphics.PointF;
import android.view.animation.Interpolator;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;

public class Keyframe<T> {
  private static final float UNSET_FLOAT = -3987645.78543923f;
  private static final int UNSET_INT = 784923401;

  @Nullable private final LottieComposition composition;
  @Nullable public final T startValue;
  @Nullable public T endValue;
  @Nullable public final Interpolator interpolator;
  @Nullable public final Interpolator xInterpolator;
  @Nullable public final Interpolator yInterpolator;
  public final float startFrame;
  @Nullable public Float endFrame;

  private float startValueFloat = UNSET_FLOAT;
  private float endValueFloat = UNSET_FLOAT;

  private int startValueInt = UNSET_INT;
  private int endValueInt = UNSET_INT;

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
    xInterpolator = null;
    yInterpolator = null;
    this.startFrame = startFrame;
    this.endFrame = endFrame;
  }

  public Keyframe(@SuppressWarnings("NullableProblems") LottieComposition composition,
      @Nullable T startValue, @Nullable T endValue,
      @Nullable Interpolator xInterpolator, @Nullable Interpolator yInterpolator, float startFrame, @Nullable Float endFrame) {
    this.composition = composition;
    this.startValue = startValue;
    this.endValue = endValue;
    interpolator = null;
    this.xInterpolator = xInterpolator;
    this.yInterpolator = yInterpolator;
    this.startFrame = startFrame;
    this.endFrame = endFrame;
  }

  protected Keyframe(@SuppressWarnings("NullableProblems") LottieComposition composition,
      @Nullable T startValue, @Nullable T endValue,
      @Nullable Interpolator interpolator, @Nullable Interpolator xInterpolator, @Nullable Interpolator yInterpolator,
      float startFrame, @Nullable Float endFrame) {
    this.composition = composition;
    this.startValue = startValue;
    this.endValue = endValue;
    this.interpolator = interpolator;
    this.xInterpolator = xInterpolator;
    this.yInterpolator = yInterpolator;
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
    xInterpolator = null;
    yInterpolator = null;
    startFrame = Float.MIN_VALUE;
    endFrame = Float.MAX_VALUE;
  }

  public float getStartProgress() {
    if (composition == null) {
      return 0f;
    }
    if (startProgress == Float.MIN_VALUE) {
      startProgress = (startFrame - composition.getStartFrame()) / composition.getDurationFrames();
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
    return interpolator == null && xInterpolator == null && yInterpolator == null;
  }

  public boolean containsProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    return progress >= getStartProgress() && progress < getEndProgress();
  }

  /**
   * Optimization to avoid autoboxing.
   */
  public float getStartValueFloat() {
    if (startValueFloat == UNSET_FLOAT) {
      startValueFloat = (float) (Float) startValue;
    }
    return startValueFloat;
  }

  /**
   * Optimization to avoid autoboxing.
   */
  public float getEndValueFloat() {
    if (endValueFloat == UNSET_FLOAT) {
      endValueFloat = (float) (Float) endValue;
    }
    return endValueFloat;
  }

  /**
   * Optimization to avoid autoboxing.
   */
  public int getStartValueInt() {
    if (startValueInt == UNSET_INT) {
      startValueInt = (int) (Integer) startValue;
    }
    return startValueInt;
  }

  /**
   * Optimization to avoid autoboxing.
   */
  public int getEndValueInt() {
    if (endValueInt == UNSET_INT) {
      endValueInt = (int) (Integer) endValue;
    }
    return endValueInt;
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
