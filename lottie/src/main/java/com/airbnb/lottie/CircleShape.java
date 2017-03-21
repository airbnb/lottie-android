package com.airbnb.lottie;

import android.graphics.PointF;
import android.support.annotation.Nullable;

import org.json.JSONObject;

class CircleShape {
  private final String name;
  private final AnimatableValue<PointF> position;
  private final AnimatablePointValue size;

  private CircleShape(String name, AnimatableValue<PointF> position,
      AnimatablePointValue size) {
    this.name = name;
    this.position = position;
    this.size = size;
  }

  static class Factory {
    private Factory() {
    }

    static CircleShape newInstance(JSONObject json, LottieComposition composition) {
      return new CircleShape(
          json.optString("nm"),
          AnimatablePathValue
              .createAnimatablePathOrSplitDimensionPath(json.optJSONObject("p"), composition),
          AnimatablePointValue.Factory.newInstance(json.optJSONObject("s"), composition));
    }
  }

  String getName() {
    return name;
  }

  public AnimatableValue<PointF> getPosition() {
    return position;
  }

  public AnimatablePointValue getSize() {
    return size;
  }
}
