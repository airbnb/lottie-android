package com.airbnb.lottie;

import android.graphics.PointF;

import org.json.JSONObject;

class RectangleShape implements ContentModel {
  private final String name;
  private final AnimatableValue<PointF> position;
  private final AnimatablePointValue size;
  private final AnimatableFloatValue cornerRadius;

  private RectangleShape(String name, AnimatableValue<PointF> position,
      AnimatablePointValue size, AnimatableFloatValue cornerRadius) {
    this.name = name;
    this.position = position;
    this.size = size;
    this.cornerRadius = cornerRadius;
  }

  static class Factory {
    private Factory() {
    }

    static RectangleShape newInstance(JSONObject json, LottieComposition composition) {
      return new RectangleShape(
          json.optString("nm"),
          AnimatablePathValue.createAnimatablePathOrSplitDimensionPath(
              json.optJSONObject("p"), composition),
          AnimatablePointValue.Factory.newInstance(json.optJSONObject("s"), composition),
          AnimatableFloatValue.Factory.newInstance(json.optJSONObject("r"), composition));
    }
  }

  String getName() {
    return name;
  }

  AnimatableFloatValue getCornerRadius() {
    return cornerRadius;
  }

  AnimatablePointValue getSize() {
    return size;
  }

  AnimatableValue<PointF> getPosition() {
    return position;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new RectangleContent(drawable, layer, this);
  }

  @Override public String toString() {
    return "RectangleShape{" + "cornerRadius=" + cornerRadius.getInitialValue() +
        ", position=" + position +
        ", size=" + size +
        '}';
  }
}
