package com.airbnb.lottie.model.content;

import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatableShapeValue;

public class Mask {
  public enum MaskMode {
    MASK_MODE_ADD,
    MASK_MODE_SUBTRACT,
    MASK_MODE_INTERSECT
  }

  private final MaskMode maskMode;
  private final AnimatableShapeValue maskPath;
  private final AnimatableIntegerValue opacity;

  public Mask(MaskMode maskMode, AnimatableShapeValue maskPath, AnimatableIntegerValue opacity) {
    this.maskMode = maskMode;
    this.maskPath = maskPath;
    this.opacity = opacity;
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
}
