package com.airbnb.lottie;

import android.support.annotation.Nullable;

import org.json.JSONObject;

class Repeater implements ContentModel {
  private final String name;
  private final AnimatableFloatValue copies;
  private final AnimatableFloatValue offset;
  private final AnimatableTransform transform;

  Repeater(String name, AnimatableFloatValue copies, AnimatableFloatValue offset,
      AnimatableTransform transform) {
    this.name = name;
    this.copies = copies;
    this.offset = offset;
    this.transform = transform;
  }

  String getName() {
    return name;
  }

  AnimatableFloatValue getCopies() {
    return copies;
  }

  AnimatableFloatValue getOffset() {
    return offset;
  }

  AnimatableTransform getTransform() {
    return transform;
  }

  @Nullable @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new RepeaterContent(drawable, layer, this);
  }

  final static class Factory {

    private Factory() {
    }

    static Repeater newInstance(JSONObject json, LottieComposition composition) {
      String name = json.optString("nm");
      AnimatableFloatValue copies =
          AnimatableFloatValue.Factory.newInstance(json.optJSONObject("c"), composition, false);
      AnimatableFloatValue offset =
          AnimatableFloatValue.Factory.newInstance(json.optJSONObject("o"), composition, false);
      AnimatableTransform transform =
          AnimatableTransform.Factory.newInstance(json.optJSONObject("tr"), composition);

      return new Repeater(name, copies, offset, transform);
    }
  }
}
