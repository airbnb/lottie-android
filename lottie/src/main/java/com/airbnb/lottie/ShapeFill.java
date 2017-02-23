package com.airbnb.lottie;

import android.support.annotation.Nullable;

import org.json.JSONObject;

class ShapeFill {
  private final boolean fillEnabled;
  @Nullable private final AnimatableColorValue color;
  @Nullable private final AnimatableIntegerValue opacity;

  private ShapeFill(boolean fillEnabled, @Nullable AnimatableColorValue color, @Nullable
      AnimatableIntegerValue opacity) {
    this.fillEnabled = fillEnabled;
    this.color = color;
    this.opacity = opacity;
  }

  static class Factory {
    private Factory() {
    }

    static ShapeFill newInstance(JSONObject json, LottieComposition composition) {
      AnimatableColorValue color = null;
      boolean fillEnabled;
      AnimatableIntegerValue opacity = null;

      JSONObject jsonColor = json.optJSONObject("c");
      if (jsonColor != null) {
        color = AnimatableColorValue.Factory.newInstance(jsonColor, composition);
      }

      JSONObject jsonOpacity = json.optJSONObject("o");
      if (jsonOpacity != null) {
        opacity = AnimatableIntegerValue.Factory.newInstance(jsonOpacity, composition, false, true);
      }
      fillEnabled = json.optBoolean("fillEnabled");
      return new ShapeFill(fillEnabled, color, opacity);
    }
  }

  @Nullable public AnimatableColorValue getColor() {
    return color;
  }

  @Nullable public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  @Override
  public String toString() {
    return "ShapeFill{" + "color=" +  Integer.toHexString(color.getInitialValue()) +
        ", fillEnabled=" + fillEnabled +
        ", opacity=" + opacity.getInitialValue() +
        '}';
  }
}
