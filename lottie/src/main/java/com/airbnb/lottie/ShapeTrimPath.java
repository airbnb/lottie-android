package com.airbnb.lottie;

import org.json.JSONObject;

class ShapeTrimPath {
  private final AnimatableFloatValue start;
  private final AnimatableFloatValue end;
  private final AnimatableFloatValue offset;

  private ShapeTrimPath(AnimatableFloatValue start, AnimatableFloatValue end, AnimatableFloatValue
      offset) {
    this.start = start;
    this.end = end;
    this.offset = offset;
  }

  static class Factory {
    private Factory() {
    }

    static ShapeTrimPath newInstance(JSONObject json, LottieComposition composition) {
      return new ShapeTrimPath(
          new AnimatableFloatValue(json.optJSONObject("s"), composition, false),
          new AnimatableFloatValue(json.optJSONObject("e"), composition, false),
          new AnimatableFloatValue(json.optJSONObject("o"), composition, false));
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
