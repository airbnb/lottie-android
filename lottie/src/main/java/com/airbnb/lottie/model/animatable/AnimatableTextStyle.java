package com.airbnb.lottie.model.animatable;

import androidx.annotation.Nullable;

public class AnimatableTextStyle {

  @Nullable public final AnimatableColorValue color;
  @Nullable public final AnimatableColorValue stroke;
  @Nullable public final AnimatableFloatValue strokeWidth;
  @Nullable public final AnimatableFloatValue tracking;
  @Nullable public final AnimatableIntegerValue opacity;

  public AnimatableTextStyle(
      @Nullable AnimatableColorValue color,
      @Nullable AnimatableColorValue stroke,
      @Nullable AnimatableFloatValue strokeWidth,
      @Nullable AnimatableFloatValue tracking,
      @Nullable AnimatableIntegerValue opacity) {
    this.color = color;
    this.stroke = stroke;
    this.strokeWidth = strokeWidth;
    this.tracking = tracking;
    this.opacity = opacity;
  }
}
