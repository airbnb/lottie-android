package com.airbnb.lottie;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

class AnimatableValueParser<T> {
  @Nullable private final JSONObject json;
  private final float scale;
  private final LottieComposition composition;
  private final AnimatableValue.Factory<T> valueFactory;

  private AnimatableValueParser(@Nullable JSONObject json, float scale, LottieComposition
      composition, AnimatableValue.Factory<T> valueFactory) {
    this.json = json;
    this.scale = scale;
    this.composition = composition;
    this.valueFactory = valueFactory;
  }

  static <T> AnimatableValueParser<T> newInstance(@Nullable JSONObject json, float scale,
      LottieComposition composition, AnimatableValue.Factory<T> valueFactory) {
    return new AnimatableValueParser<>(json, scale, composition, valueFactory);
  }

  Result<T> parseJson() {
    List<Keyframe<T>> keyframes = parseKeyframes();
    T initialValue = parseInitialValue(keyframes);
    return new Result<>(keyframes, initialValue);
  }

  private List<Keyframe<T>> parseKeyframes() {
    if (json != null) {
      Object k = json.opt("k");
      if (hasKeyframes(k)) {
        return Keyframe.Factory.parseKeyframes((JSONArray) k, composition, scale, valueFactory);
      } else {
        return Collections.emptyList();
      }
    } else {
      return Collections.emptyList();
    }
  }

  @Nullable private T parseInitialValue(List<Keyframe<T>> keyframes) {
    if (json != null) {
      if (!keyframes.isEmpty()) {
        return keyframes.get(0).startValue;
      } else {
        return valueFactory.valueFromObject(json.opt("k"), scale);
      }
    } else {
      return null;
    }
  }

  private static boolean hasKeyframes(Object json) {
    if (!(json instanceof JSONArray)) {
      return false;
    } else {
      Object firstObject = ((JSONArray) json).opt(0);
      return firstObject instanceof JSONObject && ((JSONObject) firstObject).has("t");
    }
  }

  static class Result<T> {
    final List<Keyframe<T>> keyframes;
    final @Nullable T initialValue;

    Result(List<Keyframe<T>> keyframes, @Nullable T initialValue) {
      this.keyframes = keyframes;
      this.initialValue = initialValue;
    }
  }
}
