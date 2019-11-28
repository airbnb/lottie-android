package com.airbnb.lottie.model.content;

import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatableShapeValue;

public class Mask {
  public enum MaskMode {
    MASK_MODE_ADD,
    MASK_MODE_SUBTRACT,
    MASK_MODE_INTERSECT,
    MASK_MODE_NONE
  }

  private final MaskMode maskMode;
  private final AnimatableShapeValue maskPath;
  private final AnimatableIntegerValue opacity;
  private final boolean inverted;

  public Mask(MaskMode maskMode, AnimatableShapeValue maskPath, AnimatableIntegerValue opacity, boolean inverted) {
    this.maskMode = maskMode;
    this.maskPath = maskPath;
    this.opacity = opacity;
    this.inverted = inverted;
  }

  public MaskMode getMaskMode() {
    return maskMode;
  }

  public AnimatableShapeValue getMaskPath() {
    return maskPath;
  }

  public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  public boolean isInverted() {
    return inverted;
  }
}
