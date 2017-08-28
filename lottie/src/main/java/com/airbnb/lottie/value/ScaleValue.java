package com.airbnb.lottie.value;

import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.TransformableContent;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;

@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public class ScaleValue extends LottieValue<ScaleXY> {

  /**
   * Update the scale value. The default value is 1.
   */
  public static ScaleValue forScale(ScaleXY value) {
    return forScale(value, 0, true);
  }

  /**
   * Update the scale value for a specific keyframe. The default value is 1.
   *
   * @see LottieValue for frame and updateValue docs.
   */
  public static ScaleValue forScale(ScaleXY value, int frame, boolean updateValue) {
    return new ScaleValue(value, frame, updateValue);
  }

  private ScaleValue(ScaleXY value, int frame, boolean updateValue) {
    super(value,frame, updateValue);
  }

  @Override public void offsetValue(ScaleXY offset) {
    setValue(new ScaleXY(
        getValue().getScaleX() + offset.getScaleX(),
        getValue().getScaleY() + offset.getScaleX()));
  }

  public void offsetScale(float d) {
    offsetScale(d, d);
  }

  public void offsetScale(float dx, float dy) {
    ScaleXY scale = getValue();
    setValue(new ScaleXY(scale.getScaleX() + dx, scale.getScaleY() + dy));
  }

  public void setValue(float scale) {
    setValue(scale, scale);
  }

  public void setValue(float scaleX, float scaleY) {
    setValue(new ScaleXY(scaleX, scaleY));
  }

  @Override public void apply(Content content) {
    if (content instanceof TransformableContent) {
      TransformKeyframeAnimation transform = ((TransformableContent) content).getTransform();
      if (transform == null) {
        return;
      }
      BaseKeyframeAnimation<ScaleXY, ScaleXY> scale = transform.getScale();
      scale.setValue(getValue(), frame, updateValue);
    }
  }
}
