package com.airbnb.lottie;

import org.json.JSONException;
import org.json.JSONObject;

class ShapeTrimPath {
  private final AnimatableFloatValue start;
  private final AnimatableFloatValue end;
  private final AnimatableFloatValue offset;

  ShapeTrimPath(JSONObject json, LottieComposition composition)
      throws JSONException {
    try {
      start = new AnimatableFloatValue(json.getJSONObject("s"), composition, false);
      end = new AnimatableFloatValue(json.getJSONObject("e"), composition, false);
      offset = new AnimatableFloatValue(json.getJSONObject("o"), composition, false);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse trim path " + json, e);
    }
  }

  AnimatableFloatValue getEnd() {
    return end;
  }

  AnimatableFloatValue getStart() {
    return start;
  }

  AnimatableFloatValue getOffset() {
    return offset;
  }

  @Override public String toString() {
    return "Trim Path: {start: " + start + ", end: " + end + ", offset: " + offset + "}";
  }
}
