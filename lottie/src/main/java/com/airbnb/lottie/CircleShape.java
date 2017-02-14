package com.airbnb.lottie;

import org.json.JSONException;
import org.json.JSONObject;

class CircleShape {
  private final IAnimatablePathValue position;
  private final AnimatablePointValue size;

  CircleShape(JSONObject json, LottieComposition composition) {
    try {
      position = AnimatablePathValue.createAnimatablePathOrSplitDimensionPath(json.getJSONObject("p"), composition);
      size = new AnimatablePointValue(json.getJSONObject("s"), composition);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse circle " + json, e);
    }
  }

  public IAnimatablePathValue getPosition() {
    return position;
  }

  public AnimatablePointValue getSize() {
    return size;
  }
}
