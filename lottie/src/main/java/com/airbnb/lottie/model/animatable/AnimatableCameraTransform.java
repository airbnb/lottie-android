package com.airbnb.lottie.model.animatable;

import androidx.annotation.Nullable;

public class AnimatableCameraTransform {
  @Nullable private final AnimatablePoint3Value pointOfInterest;
  private final AnimatablePoint3Value position;
  private final AnimatablePoint3Value orientation;
  private final AnimatableFloatValue rotationX;
  private final AnimatableFloatValue rotationY;
  private final AnimatableFloatValue rotationZ;

  public AnimatableCameraTransform(@Nullable AnimatablePoint3Value pointOfInterest, AnimatablePoint3Value position,
      AnimatablePoint3Value orientation, AnimatableFloatValue rotationX, AnimatableFloatValue rotationY,
      AnimatableFloatValue rotationZ) {
    this.pointOfInterest = pointOfInterest;
    this.position = position;
    this.orientation = orientation;
    this.rotationX = rotationX;
    this.rotationY = rotationY;
    this.rotationZ = rotationZ;
  }

  @Nullable
  public AnimatablePoint3Value getPointOfInterest() {
    return pointOfInterest;
  }

  public AnimatablePoint3Value getPosition() {
    return position;
  }

  public AnimatablePoint3Value getOrientation() {
    return orientation;
  }

  public AnimatableFloatValue getRotationX() {
    return rotationX;
  }

  public AnimatableFloatValue getRotationY() {
    return rotationY;
  }

  public AnimatableFloatValue getRotationZ() {
    return rotationZ;
  }
}
