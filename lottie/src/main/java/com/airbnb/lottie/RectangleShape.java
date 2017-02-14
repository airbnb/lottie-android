package com.airbnb.lottie;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

class RectangleShape {
  private static final String TAG = RectangleShape.class.getSimpleName();

  private final IAnimatablePathValue position;
  private final AnimatablePointValue size;
  private final AnimatableFloatValue cornerRadius;

  RectangleShape(JSONObject json, LottieComposition composition) {
    try {
      JSONObject positionJson = json.getJSONObject("p");
      position = AnimatablePathValue.createAnimatablePathOrSplitDimensionPath(positionJson, composition);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse rectangle position.", e);
    }

    try {
      JSONObject cornerRadiusJson = json.getJSONObject("r");
      cornerRadius = new AnimatableFloatValue(cornerRadiusJson, composition);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse rectangle corner radius.", e);
    }

    try {
      JSONObject sizeJson = json.getJSONObject("s");
      size = new AnimatablePointValue(sizeJson, composition);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse rectangle size.", e);
    }

    if (L.DBG) Log.d(TAG, "Parsed new rectangle " + toString());
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
