package com.airbnb.lottie;

import org.json.JSONObject;

class PolystarShape {
  enum Type {
    Star(1),
    Polygon(2);

    private final int value;

    Type(int value) {
      this.value = value;
    }

    static Type forValue(int value) {
      for (Type type : Type.values()) {
        if (type.value == value) {
          return type;
        }
      }
      return null;
    }
  }

  private final Type type;
  private final AnimatableFloatValue points;
  private final IAnimatablePathValue position;
  private final AnimatableFloatValue rotation;
  private final AnimatableFloatValue innerRadius;
  private final AnimatableFloatValue outerRadius;
  private final AnimatableFloatValue innerRoundedness;
  private final AnimatableFloatValue outerRoundedness;

  PolystarShape(JSONObject json, LottieComposition composition) {
    type = Type.forValue(json.optInt("sy"));
    points = new AnimatableFloatValue(json.optJSONObject("pt"), composition, false);
    position = AnimatablePathValue.createAnimatablePathOrSplitDimensionPath(
        json.optJSONObject("p"), composition);
    rotation = new AnimatableFloatValue(json.optJSONObject("r"), composition, false);

    outerRadius = new AnimatableFloatValue(json.optJSONObject("or"), composition);
    outerRoundedness = new AnimatableFloatValue(json.optJSONObject("os"), composition, false);

    if (type == Type.Star) {
      innerRadius = new AnimatableFloatValue(json.optJSONObject("ir"), composition);
      innerRoundedness = new AnimatableFloatValue(json.optJSONObject("is"), composition, false);
    } else {
      innerRadius = null;
      innerRoundedness = null;
    }
  }

  Type getType() {
    return type;
  }

  AnimatableFloatValue getPoints() {
    return points;
  }

  IAnimatablePathValue getPosition() {
    return position;
  }

  AnimatableFloatValue getRotation() {
    return rotation;
  }

  AnimatableFloatValue getInnerRadius() {
    return innerRadius;
  }

  AnimatableFloatValue getOuterRadius() {
    return outerRadius;
  }

  AnimatableFloatValue getInnerRoundedness() {
    return innerRoundedness;
  }

  AnimatableFloatValue getOuterRoundedness() {
    return outerRoundedness;
  }
}
