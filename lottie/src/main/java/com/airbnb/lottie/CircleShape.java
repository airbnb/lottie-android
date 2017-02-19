package com.airbnb.lottie;

import org.json.JSONObject;

class CircleShape {
  private final IAnimatablePathValue position;
  private final AnimatablePointValue size;

  CircleShape(JSONObject json, LottieComposition composition) {
    position = AnimatablePathValue.createAnimatablePathOrSplitDimensionPath(
        json.optJSONObject("p"), composition);
    size = new AnimatablePointValue(json.optJSONObject("s"), composition);
  }

  public IAnimatablePathValue getPosition() {
    return position;
  }

  public AnimatablePointValue getSize() {
    return size;
  }
}
