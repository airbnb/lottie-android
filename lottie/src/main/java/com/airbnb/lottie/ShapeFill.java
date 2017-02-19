package com.airbnb.lottie;

import org.json.JSONObject;

class ShapeFill {
  private boolean fillEnabled;
  private AnimatableColorValue color;
  private AnimatableIntegerValue opacity;

  ShapeFill(JSONObject json, LottieComposition composition) {
    JSONObject jsonColor = json.optJSONObject("c");
    if (jsonColor != null) {
      color = new AnimatableColorValue(jsonColor, composition);
    }

    JSONObject jsonOpacity = json.optJSONObject("o");
    if (jsonOpacity != null) {
      opacity = new AnimatableIntegerValue(jsonOpacity, composition, false, true);
    }
    fillEnabled = json.optBoolean("fillEnabled");
  }

  public AnimatableColorValue getColor() {
    return color;
  }

  public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  @Override
  public String toString() {
    return "ShapeFill{" + "color=" + Integer.toHexString(color.getInitialValue()) +
        ", fillEnabled=" + fillEnabled +
        ", opacity=" + opacity.getInitialValue() +
        '}';
  }
}
