package com.airbnb.lottie;

import org.json.JSONObject;

class ShapeTrimPath {
  private final AnimatableFloatValue start;
  private final AnimatableFloatValue end;
  private final AnimatableFloatValue offset;

  ShapeTrimPath(JSONObject json, LottieComposition composition) {
    start = new AnimatableFloatValue(json.optJSONObject("s"), composition, false);
    end = new AnimatableFloatValue(json.optJSONObject("e"), composition, false);
    offset = new AnimatableFloatValue(json.optJSONObject("o"), composition, false);
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
