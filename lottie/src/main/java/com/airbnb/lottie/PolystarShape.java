package com.airbnb.lottie;

import org.json.JSONException;
import org.json.JSONObject;

class PolystarShape {
  private static final String TAG = PolystarShape.class.getSimpleName();

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

  PolystarShape(JSONObject json, LottieComposition composition) throws JSONException {
    type = Type.forValue(json.getInt("sy"));
    points = new AnimatableFloatValue(json.getJSONObject("pt"), composition, false);
    position = AnimatablePathValue.createAnimatablePathOrSplitDimensionPath(
        json.getJSONObject("p"), composition);
    rotation = new AnimatableFloatValue(json.getJSONObject("r"), composition, false);

    outerRadius = new AnimatableFloatValue(json.getJSONObject("or"), composition);
    outerRoundedness = new AnimatableFloatValue(json.getJSONObject("os"), composition, false);

    if (type == Type.Star) {
      innerRadius = new AnimatableFloatValue(json.getJSONObject("ir"), composition);
      innerRoundedness = new AnimatableFloatValue(json.getJSONObject("is"), composition, false);
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
