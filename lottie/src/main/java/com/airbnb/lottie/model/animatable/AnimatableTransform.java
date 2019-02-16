package com.airbnb.lottie.model.animatable;

import android.graphics.PointF;
import android.util.Log;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ModifierContent;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.layer.BaseLayer;

public class AnimatableTransform implements ModifierContent, ContentModel {
  private final AnimatablePathValue anchorPoint;
  private final AnimatableValue<PointF, PointF> position;
  private final AnimatableScaleValue scale;
  private final AnimatableFloatValue rotation;
  private final AnimatableIntegerValue opacity;
  private final boolean isIdentity;
  @Nullable private final AnimatableFloatValue skew;
  @Nullable private final AnimatableFloatValue skewAngle;

  // Used for repeaters
  @Nullable private final AnimatableFloatValue startOpacity;
  @Nullable private final AnimatableFloatValue endOpacity;

  public AnimatableTransform() {
    this(
        new AnimatablePathValue(),
        new AnimatablePathValue(),
        new AnimatableScaleValue(),
        new AnimatableFloatValue(),
        new AnimatableIntegerValue(),
        new AnimatableFloatValue(),
        new AnimatableFloatValue(),
        new AnimatableFloatValue(),
        new AnimatableFloatValue()
    );
  }

  public AnimatableTransform(AnimatablePathValue anchorPoint,
      AnimatableValue<PointF, PointF> position, AnimatableScaleValue scale,
      AnimatableFloatValue rotation, AnimatableIntegerValue opacity,
      @Nullable AnimatableFloatValue startOpacity, @Nullable AnimatableFloatValue endOpacity,
      @Nullable AnimatableFloatValue skew, @Nullable AnimatableFloatValue skewAngle) {
    this.anchorPoint = anchorPoint;
    this.position = position;
    this.scale = scale;
    this.rotation = rotation;
    this.opacity = opacity;
    this.startOpacity = startOpacity;
    this.endOpacity = endOpacity;
    this.skew = skew;
    this.skewAngle = skewAngle;
    isIdentity = anchorPoint.isStatic() && anchorPoint.getKeyframes().get(0).startValue.equals(0f, 0f) &&
            !(position instanceof AnimatableSplitDimensionPathValue) &&
            position.isStatic() && position.getKeyframes().get(0).startValue.equals(0f, 0f) &&
            scale.isStatic() && scale.getKeyframes().get(0).startValue.equals(1f, 1f) &&
            (rotation.isStatic() && rotation.getKeyframes().get(0).startValue == 0f || rotation.keyframes.isEmpty()) &&
            (skew == null || (skew.isStatic() && skew.getKeyframes().get(0).startValue == 0f)) &&
            (skewAngle == null || (skewAngle.isStatic() && skewAngle.getKeyframes().get(0).startValue == 0f));
  }

  public AnimatablePathValue getAnchorPoint() {
    return anchorPoint;
  }

  public AnimatableValue<PointF, PointF> getPosition() {
    return position;
  }

  public AnimatableScaleValue getScale() {
    return scale;
  }

  public AnimatableFloatValue getRotation() {
    return rotation;
  }

  public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  @Nullable public AnimatableFloatValue getStartOpacity() {
    return startOpacity;
  }

  @Nullable public AnimatableFloatValue getEndOpacity() {
    return endOpacity;
  }

  @Nullable public AnimatableFloatValue getSkew() {
    return skew;
  }

  @Nullable public AnimatableFloatValue getSkewAngle() {
    return skewAngle;
  }

  public boolean isIdentity() {
    return isIdentity;
  }

  public TransformKeyframeAnimation createAnimation() {
    return new TransformKeyframeAnimation(this);
  }

  @Nullable @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return null;
  }
}
