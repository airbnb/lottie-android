package com.airbnb.lottie.value;

import android.graphics.PointF;
import android.support.annotation.IntDef;

import com.airbnb.lottie.L;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.TransformableContent;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;

@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public class PointFValue extends LottieValue<PointF> {

  @IntDef({ POSITION, ANCHOR_POINT })
  private @interface Property {}
  private static final int POSITION = 1;
  private static final int ANCHOR_POINT = 2;

  public static PointFValue forAnchorPoint(PointF value) {
    return forAnchorPoint(value, 0, true);
  }

  public static PointFValue forAnchorPoint(PointF value, int frame, boolean updateValue) {
    return new PointFValue(ANCHOR_POINT, scaledPoint(value), frame, updateValue);
  }

  public static PointFValue forPosition(PointF value) {
    return forPosition(value, 0, true);
  }

  public static PointFValue forPosition(PointF value, int frame, boolean updateValue) {
    return new PointFValue(POSITION, scaledPoint(value), frame, updateValue);
  }

  private static PointF scaledPoint(PointF value) {
    return new PointF(value.x * L.getDpScale(), value.y * L.getDpScale());
  }

  private final int property;

  private PointFValue(@Property int property, PointF value, int frame, boolean updateValue) {
    super(value, frame, updateValue);
    this.property = property;
  }

  public void offsetValue(float d) {
    offsetValue(d, d);
  }

  public void offsetValue(float dx, float dy) {
    PointF value = getValue();
    setValue(new PointF(value.x + dx, value.y + dy));
  }

  @Override public void offsetValue(PointF d) {
    PointF value = getValue();
    setValue(new PointF(value.x + d.x, value.y + d.y));
  }

  @Override public void apply(Content content) {
    switch (property) {
      case POSITION:
        applyPosition(content);
        break;
      case ANCHOR_POINT:
        applyAnchorPoint(content);
        break;
      default:
        throw new IllegalArgumentException("Cannot apply property " + property);
    }
  }

  private void applyPosition(Content content) {
    if (content instanceof TransformableContent) {
      TransformKeyframeAnimation transform = ((TransformableContent) content).getTransform();
      if (transform == null) {
        return;
      }
      BaseKeyframeAnimation<PointF, PointF> position = transform.getPosition();
      position.setValue(getValue(), frame, updateValue);
    }
  }

  private void applyAnchorPoint(Content content) {
    if (content instanceof TransformableContent) {
      TransformKeyframeAnimation transform = ((TransformableContent) content).getTransform();
      if (transform == null) {
        return;
      }
      BaseKeyframeAnimation<PointF, PointF> anchorPoint = transform.getAnchorPoint();
      anchorPoint.setValue(getValue(), frame, updateValue);
    }
  }
}
