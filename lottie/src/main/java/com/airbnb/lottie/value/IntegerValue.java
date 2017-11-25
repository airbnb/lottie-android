package com.airbnb.lottie.value;

import android.support.annotation.IntDef;
import android.support.annotation.IntRange;

import com.airbnb.lottie.animation.content.BaseStrokeContent;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.FillContent;
import com.airbnb.lottie.animation.content.StrokeContent;
import com.airbnb.lottie.animation.content.TransformableContent;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;

@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public class IntegerValue extends LottieValue<Integer> {

  @IntDef({ OPACITY, COLOR })
  private @interface Property {}
  private static final int OPACITY = 1;
  private static final int COLOR = 2;

  /**
   * Update an opacity value from 0 (transparent) to 100 (opaque).
   */
  public static IntegerValue forOpacity(@IntRange(from = 0, to = 100) int value) {
    return forOpacity(value, 0, true);
  }

  /**
   * Update an a specific keyframe opacity value from 0 (transparent) to 100 (opaque).
   *
   * @see LottieValue for frame and updateValue docs.
   */
  public static IntegerValue forOpacity(
      @IntRange(from = 0, to = 100) int value, int frame, boolean updateValue) {
    return new IntegerValue(OPACITY, value, frame, updateValue);
  }

  public static IntegerValue forColor(int value) {
    return forColor(value, 0, true);
  }

  public static IntegerValue forColor(int value, int frame, boolean updateValue) {
    return new IntegerValue(COLOR, value, frame, updateValue);
  }

  private final int property;

  private IntegerValue(@Property int property,  Integer value, int frame, boolean updateValue) {
    super(value, frame, updateValue);
    this.property = property;
  }

  @Override public void offsetValue(Integer value) {
    setValue(getValue() + value);
  }

  @Override public void apply(Content content) {
    switch (property) {
      case OPACITY:
        applyOpacity(content);
        break;
      case COLOR:
        applyColor(content);
        break;
    }
  }

  private void applyOpacity(Content content) {
    if (content instanceof TransformableContent) {
      TransformKeyframeAnimation transform = ((TransformableContent) content).getTransform();
      if (transform == null) {
        return;
      }
      BaseKeyframeAnimation<Integer, Integer> rotation = transform.getOpacity();
      rotation.setValue(getValue(), frame, updateValue);
    } else if (content instanceof FillContent) {
      ((FillContent) content).getOpacity().setValue(getValue(), frame, updateValue);
    } else if (content instanceof BaseStrokeContent) {
      ((BaseStrokeContent) content).getOpacity().setValue(getValue(), frame, updateValue);
    }
  }

  private void applyColor(Content content) {
    if (content instanceof FillContent) {
      ((FillContent) content).getColor().setValue(getValue(), frame, updateValue);
    } else if (content instanceof StrokeContent) {
      ((StrokeContent) content).getColor().setValue(getValue(), frame, updateValue);
    }
  }
}
