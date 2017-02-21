package com.airbnb.lottie;

import org.json.JSONObject;

class CircleShape {
  private final IAnimatablePathValue position;
  private final AnimatablePointValue size;

  private CircleShape(IAnimatablePathValue position, AnimatablePointValue size) {
    this.position = position;
    this.size = size;
  }

  static class Factory {
    static CircleShape newInstance(JSONObject json, LottieComposition composition) {
      return new CircleShape(
          AnimatablePathValue
              .createAnimatablePathOrSplitDimensionPath(json.optJSONObject("p"), composition),
          new AnimatablePointValue(json.optJSONObject("s"), composition));
    }
  }

  public IAnimatablePathValue getPosition() {
    return position;
  }

  public AnimatablePointValue getSize() {
    return size;
  }
}
