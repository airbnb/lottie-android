package com.airbnb.lottie.model.content;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.TrimPathContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.layer.BaseLayer;

import org.json.JSONObject;

public class ShapeTrimPath implements ContentModel {

  public enum Type {
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

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public AnimatableFloatValue getEnd() {
    return end;
  }

  public AnimatableFloatValue getStart() {
    return start;
  }

  public AnimatableFloatValue getOffset() {
    return offset;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new TrimPathContent(layer, this);
  }

  @Override public String toString() {
    return "Trim Path: {start: " + start + ", end: " + end + ", offset: " + offset + "}";
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
}
