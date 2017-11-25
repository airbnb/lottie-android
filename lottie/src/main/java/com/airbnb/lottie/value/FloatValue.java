package com.airbnb.lottie.value;

import android.support.annotation.IntDef;

import com.airbnb.lottie.L;
import com.airbnb.lottie.animation.content.BaseStrokeContent;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.TransformableContent;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;

@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public class FloatValue extends LottieValue<Float> {

  @IntDef({ ROTATION, STROKE_WIDTH })
  private @interface Property {}
  private static final int ROTATION = 1;
  private static final int STROKE_WIDTH = 2;

  /**
   * Update a rotation value in degrees.
   */
  public static FloatValue forRotation(Float value) {
    return forRotation(value, 0, true);
  }

  /**
   * Update a specific keyframe rotation value in degrees.
   *
   * @see LottieValue for frame and updateValue docs.
   */
  public static FloatValue forRotation(Float value, int frame, boolean updateValue) {
    return new FloatValue(ROTATION, value, frame, updateValue);
  }

  /**
   * Update a stroke width value in dps.
   */
  public static FloatValue forStrokeWidth(Float value) {
    return forStrokeWidth(value, 0, true);
  }

  /**
   * Update a specific stroke with value in dps.
   *
   * @see LottieValue for frame and updateValue docs.
   */
  public static FloatValue forStrokeWidth(Float value, int frame, boolean updateValue) {
    return new FloatValue(STROKE_WIDTH, value * L.getDpScale(), frame, updateValue);
  }

  private final int property;

  private FloatValue(@Property int property,  Float value, int frame, boolean updateValue) {
    super(value, frame, updateValue);
    this.property = property;
  }

  @Override public void offsetValue(Float value) {
    setValue(getValue() + value);
  }

  @Override public void apply(Content content) {
    switch (property) {
      case ROTATION:
        applyRotation(content);
        break;
      case STROKE_WIDTH:
        applyStrokeWidth(content);
        break;
    }
  }

  private void applyRotation(Content content) {
    if (content instanceof TransformableContent) {
      TransformKeyframeAnimation transform = ((TransformableContent) content).getTransform();
      if (transform == null)
        return;
      BaseKeyframeAnimation<Float, Float> rotation = transform.getRotation();
      rotation.setValue(getValue(), frame, updateValue);
    }
  }

  private void applyStrokeWidth(Content content) {
    if (!(content instanceof BaseStrokeContent)) {
      return;
    }
    BaseKeyframeAnimation<Float, Float> width = ((BaseStrokeContent) content).getWidth();
    width.setValue(getValue(), frame, updateValue);
  }
}
