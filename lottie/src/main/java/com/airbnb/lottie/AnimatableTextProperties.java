package com.airbnb.lottie;

import org.json.JSONObject;

class AnimatableTextProperties {

  final AnimatableColorValue color;

  AnimatableTextProperties(AnimatableColorValue color) {
    this.color = color;
  }


  static final class Factory {

    private Factory() {
    }

    static AnimatableTextProperties newInstance(JSONObject json, LottieComposition composition) {

      JSONObject colorJson = json.optJSONObject("a").optJSONObject("fc");
      AnimatableColorValue color = AnimatableColorValue.Factory.newInstance(colorJson, composition);

      return new AnimatableTextProperties(color);
    }
  }
}
