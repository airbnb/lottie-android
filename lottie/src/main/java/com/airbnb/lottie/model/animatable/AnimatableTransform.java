package com.airbnb.lottie.model.animatable;

import android.graphics.PointF;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ModifierContent;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.layer.BaseLayer;

public class AnimatableTransform implements ModifierContent, ContentModel {
  @Nullable
  private final AnimatablePathValue anchorPoint;
  @Nullable
  private final AnimatableValue<PointF, PointF> position;
  @Nullable
  private final AnimatableScaleValue scale;
  @Nullable
  private final AnimatableFloatValue rotation;
  @Nullable
  private final AnimatableIntegerValue opacity;
  @Nullable
  private final AnimatableFloatValue skew;
  @Nullable
  private final AnimatableFloatValue skewAngle;

  // 3D properties
  @Nullable
  private final AnimatableFloatValue rotationX;
  @Nullable
  private final AnimatableFloatValue rotationY;
  @Nullable
  private final AnimatableFloatValue rotationZ;

  // Used for repeaters
  @Nullable
  private final AnimatableFloatValue startOpacity;
  @Nullable
  private final AnimatableFloatValue endOpacity;

  private boolean autoOrient = false;

  public AnimatableTransform() {
    this(null, null, null, null, null, null, null, null, null, null, null, null);
  }

  public AnimatableTransform(@Nullable AnimatablePathValue anchorPoint,
      @Nullable AnimatableValue<PointF, PointF> position, @Nullable AnimatableScaleValue scale,
      @Nullable AnimatableFloatValue rotation, @Nullable AnimatableIntegerValue opacity,
      @Nullable AnimatableFloatValue startOpacity, @Nullable AnimatableFloatValue endOpacity,
      @Nullable AnimatableFloatValue skew, @Nullable AnimatableFloatValue skewAngle) {
    this(anchorPoint, position, scale, rotation, opacity, startOpacity, endOpacity, skew, skewAngle, null, null, null);
  }

  public AnimatableTransform(@Nullable AnimatablePathValue anchorPoint,
      @Nullable AnimatableValue<PointF, PointF> position, @Nullable AnimatableScaleValue scale,
      @Nullable AnimatableFloatValue rotation, @Nullable AnimatableIntegerValue opacity,
      @Nullable AnimatableFloatValue startOpacity, @Nullable AnimatableFloatValue endOpacity,
      @Nullable AnimatableFloatValue skew, @Nullable AnimatableFloatValue skewAngle,
      @Nullable AnimatableFloatValue rotationX, @Nullable AnimatableFloatValue rotationY,
      @Nullable AnimatableFloatValue rotationZ) {
    this.anchorPoint = anchorPoint;
    this.position = position;
    this.scale = scale;
    this.rotation = rotation;
    this.opacity = opacity;
    this.startOpacity = startOpacity;
    this.endOpacity = endOpacity;
    this.skew = skew;
    this.skewAngle = skewAngle;
    this.rotationX = rotationX;
    this.rotationY = rotationY;
    this.rotationZ = rotationZ;
  }

  /**
   * This is set as a property of the layer so it is parsed and set separately.
   */
  public void setAutoOrient(boolean autoOrient) {
    this.autoOrient = autoOrient;
  }

  @Nullable
  public AnimatablePathValue getAnchorPoint() {
    return anchorPoint;
  }

  @Nullable
  public AnimatableValue<PointF, PointF> getPosition() {
    return position;
  }

  @Nullable
  public AnimatableScaleValue getScale() {
    return scale;
  }

  @Nullable
  public AnimatableFloatValue getRotation() {
    return rotation;
  }

  @Nullable
  public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  @Nullable
  public AnimatableFloatValue getStartOpacity() {
    return startOpacity;
  }

  @Nullable
  public AnimatableFloatValue getEndOpacity() {
    return endOpacity;
  }

  @Nullable
  public AnimatableFloatValue getSkew() {
    return skew;
  }

  @Nullable
  public AnimatableFloatValue getSkewAngle() {
    return skewAngle;
  }

  @Nullable
  public AnimatableFloatValue getRotationX() {
    return rotationX;
  }

  @Nullable
  public AnimatableFloatValue getRotationY() {
    return rotationY;
  }

  @Nullable
  public AnimatableFloatValue getRotationZ() {
    return rotationZ;
  }

  public boolean isAutoOrient() {
    return autoOrient;
  }

  public boolean has3DRotation() {
    return rotationX != null || rotationY != null || rotationZ != null;
  }

  public TransformKeyframeAnimation createAnimation() {
    return new TransformKeyframeAnimation(this);
  }

  @Nullable
  @Override
  public Content toContent(LottieDrawable drawable, LottieComposition composition, BaseLayer layer) {
    return null;
  }
}
