package com.airbnb.lottie;

import org.json.JSONObject;

class RectangleShape {
  private static final String TAG = RectangleShape.class.getSimpleName();

  private final IAnimatablePathValue position;
  private final AnimatablePointValue size;
  private final AnimatableFloatValue cornerRadius;

  RectangleShape(JSONObject json, LottieComposition composition) {
    position = AnimatablePathValue.createAnimatablePathOrSplitDimensionPath(
        json.optJSONObject("p"), composition);
    cornerRadius = new AnimatableFloatValue(json.optJSONObject("r"), composition);
    size = new AnimatablePointValue(json.optJSONObject("s"), composition);
  }

  AnimatableFloatValue getCornerRadius() {
    return cornerRadius;
  }

  AnimatablePointValue getSize() {
    return size;
  }

  IAnimatablePathValue getPosition() {
    return position;
  }

  @Override public String toString() {
    return "RectangleShape{" + "cornerRadius=" + cornerRadius.getInitialValue() +
        ", position=" + position +
        ", size=" + size +
        '}';
  }
}
