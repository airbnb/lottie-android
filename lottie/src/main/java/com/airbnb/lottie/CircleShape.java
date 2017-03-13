package com.airbnb.lottie;

import android.graphics.PointF;

import org.json.JSONObject;

class CircleShape {
  private final AnimatableValue<PointF> position;
  private final AnimatablePointValue size;

  private CircleShape(AnimatableValue<PointF> position, AnimatablePointValue size) {
    this.position = position;
    this.size = size;
  }

  static class Factory {
    private Factory() {
    }

    static CircleShape newInstance(JSONObject json, LottieComposition composition) {
      return new CircleShape(
          AnimatablePathValue
              .createAnimatablePathOrSplitDimensionPath(json.optJSONObject("p"), composition),
          AnimatablePointValue.Factory.newInstance(json.optJSONObject("s"), composition));
    }
  }

  public AnimatableValue<PointF> getPosition() {
    return position;
  }

  public AnimatablePointValue getSize() {
    return size;
  }
}
