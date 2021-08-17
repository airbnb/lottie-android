package com.airbnb.lottie.parser;

import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;

public class DropShadowEffect {
  private final AnimatableColorValue color;
  private final AnimatableFloatValue opacity;
  private final AnimatableFloatValue direction;
  private final AnimatableFloatValue distance;
  private final AnimatableFloatValue radius;

  DropShadowEffect(AnimatableColorValue color, AnimatableFloatValue opacity, AnimatableFloatValue direction,
      AnimatableFloatValue distance, AnimatableFloatValue radius) {
    this.color = color;
    this.opacity = opacity;
    this.direction = direction;
    this.distance = distance;
    this.radius = radius;
  }

  public AnimatableColorValue getColor() {
    return color;
  }

  public AnimatableFloatValue getOpacity() {
    return opacity;
  }

  public AnimatableFloatValue getDirection() {
    return direction;
  }

  public AnimatableFloatValue getDistance() {
    return distance;
  }

  public AnimatableFloatValue getRadius() {
    return radius;
  }
}
