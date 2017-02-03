package com.airbnb.lottie;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

class RectangleShape {
  private static final String TAG = RectangleShape.class.getSimpleName();

  private final AnimatablePathValue position;
  private final AnimatablePointValue size;
  private final AnimatableFloatValue cornerRadius;

  RectangleShape(JSONObject json, int frameRate, LottieComposition composition) {
    try {
      JSONObject positionJson = json.getJSONObject("p");
      position = new AnimatablePathValue(positionJson, frameRate, composition);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse rectangle position.", e);
    }

    try {
      JSONObject cornerRadiusJson = json.getJSONObject("r");
      cornerRadius = new AnimatableFloatValue(cornerRadiusJson, frameRate, composition);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse rectangle corner radius.", e);
    }

    try {
      JSONObject sizeJson = json.getJSONObject("s");
      size = new AnimatablePointValue(sizeJson, frameRate, composition);
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

  AnimatablePathValue getPosition() {
    return position;
  }

  @Override public String toString() {
    return "RectangleShape{" + "cornerRadius=" + cornerRadius.getInitialValue() +
        ", position=" + position +
        ", size=" + size +
        '}';
  }
}
