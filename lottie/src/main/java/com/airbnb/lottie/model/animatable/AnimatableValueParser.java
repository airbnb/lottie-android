package com.airbnb.lottie.model.animatable;

import android.support.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

class AnimatableValueParser<T> {
  private final JSONObject json;
  private final LottieComposition composition;
  private final AnimatableValue.Factory<T> valueFactory;

  private AnimatableValueParser(
      JSONObject json, LottieComposition composition, AnimatableValue.Factory<T> valueFactory) {
    this.json = json;
    this.composition = composition;
    this.valueFactory = valueFactory;
  }

  static <T> List<Keyframe<T>> newInstance(@Nullable JSONObject json,
      LottieComposition composition, AnimatableValue.Factory<T> valueFactory) {
    AnimatableValueParser<T> parser =
        new AnimatableValueParser<>(json, composition, valueFactory);
    return parser.parseKeyframes();
  }

  private List<Keyframe<T>> parseKeyframes() {
    Object k = json.opt("k");
    if (hasKeyframes(k)) {
      return Keyframe.Factory.parseKeyframes((JSONArray) k, composition, valueFactory);
    } else {
      return parseStaticValue();
    }
  }

  private List<Keyframe<T>> parseStaticValue() {
    T initialValue = valueFactory.valueFromObject(json.opt("k"));
    return Collections.singletonList(new Keyframe<>(initialValue));
  }

  private static boolean hasKeyframes(Object json) {
    if (!(json instanceof JSONArray)) {
      return false;
    } else {
      Object firstObject = ((JSONArray) json).opt(0);
      return firstObject instanceof JSONObject && ((JSONObject) firstObject).has("t");
    }
  }
}
