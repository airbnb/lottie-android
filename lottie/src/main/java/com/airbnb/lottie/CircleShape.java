package com.airbnb.lottie;

import org.json.JSONException;
import org.json.JSONObject;

class CircleShape {
  private final AnimatablePathValue position;
  private final AnimatablePointValue size;

  CircleShape(JSONObject json, int frameRate, LottieComposition composition) {
    try {
      position = new AnimatablePathValue(json.getJSONObject("p"), frameRate, composition);
      size = new AnimatablePointValue(json.getJSONObject("s"), frameRate, composition);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse circle " + json, e);
    }
  }

  public AnimatablePathValue getPosition() {
    return position;
  }

  public AnimatablePointValue getSize() {
    return size;
  }
}
