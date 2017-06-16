package com.airbnb.lottie;

import android.support.annotation.Nullable;

import org.json.JSONObject;

class AnimatableTextProperties {

  @Nullable final AnimatableColorValue color;
  @Nullable final AnimatableColorValue stroke;
  @Nullable final AnimatableFloatValue strokeWidth;

  AnimatableTextProperties(@Nullable AnimatableColorValue color,
      @Nullable AnimatableColorValue stroke, @Nullable AnimatableFloatValue strokeWidth) {
    this.color = color;
    this.stroke = stroke;
    this.strokeWidth = strokeWidth;
  }


  static final class Factory {

    private Factory() {
    }

    static AnimatableTextProperties newInstance(JSONObject json, LottieComposition composition) {
      if (json == null || !json.has("a")) {
        return new AnimatableTextProperties(null, null, null);
      }
      JSONObject animatablePropertiesJson = json.optJSONObject("a");
      JSONObject colorJson = animatablePropertiesJson.optJSONObject("fc");
      AnimatableColorValue color = AnimatableColorValue.Factory.newInstance(colorJson, composition);

      JSONObject strokeJson = animatablePropertiesJson.optJSONObject("sc");
      AnimatableColorValue stroke =
          AnimatableColorValue.Factory.newInstance(strokeJson, composition);

      JSONObject strokeWidthJson = animatablePropertiesJson.optJSONObject("sw");
      AnimatableFloatValue strokeWidth =
          AnimatableFloatValue.Factory.newInstance(strokeWidthJson, composition);


      return new AnimatableTextProperties(color, stroke, strokeWidth);
    }
  }
}
