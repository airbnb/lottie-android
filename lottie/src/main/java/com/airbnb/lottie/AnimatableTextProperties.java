package com.airbnb.lottie;

import android.support.annotation.Nullable;

import org.json.JSONObject;

class AnimatableTextProperties {

  @Nullable final AnimatableColorValue color;
  @Nullable final AnimatableColorValue stroke;
  @Nullable final AnimatableFloatValue strokeWidth;
  @Nullable final AnimatableFloatValue tracking;

  AnimatableTextProperties(@Nullable AnimatableColorValue color,
      @Nullable AnimatableColorValue stroke, @Nullable AnimatableFloatValue strokeWidth,
      @Nullable AnimatableFloatValue tracking) {
    this.color = color;
    this.stroke = stroke;
    this.strokeWidth = strokeWidth;
    this.tracking = tracking;
  }

  static final class Factory {

    private Factory() {
    }

    static AnimatableTextProperties newInstance(JSONObject json, LottieComposition composition) {
      if (json == null || !json.has("a")) {
        return new AnimatableTextProperties(null, null, null, null);
      }
      JSONObject animatablePropertiesJson = json.optJSONObject("a");
      JSONObject colorJson = animatablePropertiesJson.optJSONObject("fc");
      AnimatableColorValue color = null;
      if (colorJson != null) {
        color = AnimatableColorValue.Factory.newInstance(colorJson, composition);
      }

      JSONObject strokeJson = animatablePropertiesJson.optJSONObject("sc");
      AnimatableColorValue stroke = null;
      if (strokeJson != null) {
        stroke = AnimatableColorValue.Factory.newInstance(strokeJson, composition);
      }

      JSONObject strokeWidthJson = animatablePropertiesJson.optJSONObject("sw");
      AnimatableFloatValue strokeWidth = null;
      if (strokeWidthJson != null) {
        strokeWidth = AnimatableFloatValue.Factory.newInstance(strokeWidthJson, composition);
      }

      JSONObject trackingJson = animatablePropertiesJson.optJSONObject("t");
      AnimatableFloatValue tracking = null;
      if (trackingJson != null) {
        tracking = AnimatableFloatValue.Factory.newInstance(trackingJson, composition);
      }

      return new AnimatableTextProperties(color, stroke, strokeWidth, tracking);
    }
  }
}
