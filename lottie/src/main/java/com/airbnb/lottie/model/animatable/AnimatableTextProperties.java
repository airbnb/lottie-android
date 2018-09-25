package com.airbnb.lottie.model.animatable;

import androidx.annotation.Nullable;

public class AnimatableTextProperties {

  @Nullable public final AnimatableColorValue color;
  @Nullable public final AnimatableColorValue stroke;
  @Nullable public final AnimatableFloatValue strokeWidth;
  @Nullable public final AnimatableFloatValue tracking;

  public AnimatableTextProperties(@Nullable AnimatableColorValue color,
      @Nullable AnimatableColorValue stroke, @Nullable AnimatableFloatValue strokeWidth,
      @Nullable AnimatableFloatValue tracking) {
    this.color = color;
    this.stroke = stroke;
    this.strokeWidth = strokeWidth;
    this.tracking = tracking;
  }
}
