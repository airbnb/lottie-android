package com.airbnb.lottie;

import android.support.annotation.Nullable;

import org.json.JSONObject;

class ShapeTrimPath {

  enum Type {
    Simultaneously,
    Individually;

    static Type forId(int id) {
      switch (id) {
        case 1:
          return Simultaneously;
        case 2:
          return Individually;
        default:
          throw new IllegalArgumentException("Unknown trim path type " + id);
      }
    }
  }

  private final String name;
  private final Type type;
  private final AnimatableFloatValue start;
  private final AnimatableFloatValue end;
  private final AnimatableFloatValue offset;

  private ShapeTrimPath(String name, Type type, AnimatableFloatValue start,
      AnimatableFloatValue end, AnimatableFloatValue offset) {
    this.name = name;
    this.type = type;
    this.start = start;
    this.end = end;
    this.offset = offset;
  }

  static class Factory {
    private Factory() {
    }

    static ShapeTrimPath newInstance(JSONObject json, LottieComposition composition) {
      return new ShapeTrimPath(
          json.optString("nm"),
          Type.forId(json.optInt("m", 1)),
          AnimatableFloatValue.Factory.newInstance(json.optJSONObject("s"), composition, false),
          AnimatableFloatValue.Factory.newInstance(json.optJSONObject("e"), composition, false),
          AnimatableFloatValue.Factory.newInstance(json.optJSONObject("o"), composition, false));
    }
  }

  String getName() {
    return name;
  }

  Type getType() {
    return type;
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
