package com.airbnb.lottie;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

abstract class BaseAnimatableValue<V, O> implements AnimatableValue<V, O> {
  List<Keyframe<V>> keyframes = Collections.emptyList();
  final LottieComposition composition;
  private final boolean isDp;

  V initialValue;

  /** Create a default static animatable path. */
  BaseAnimatableValue(LottieComposition composition) {
    this.composition = composition;
    isDp = false;
  }

  BaseAnimatableValue(@Nullable JSONObject json, LottieComposition composition, boolean isDp)
      throws JSONException {
    this.composition = composition;
    this.isDp = isDp;
    if (json != null) {
      Object k = json.get("k");
      if (hasKeyframes(k)) {
        keyframes = Keyframe.parseKeyframes((JSONArray) k, composition, getScale(), this);
        if (!keyframes.isEmpty()) {
          initialValue = keyframes.get(0).startValue;
        }
      } else {
        initialValue = valueFromObject(k, getScale());
      }
      if (keyframes == null) {
        keyframes = Collections.emptyList();
      }
    }
  }

  private boolean hasKeyframes(Object json) throws JSONException {
    if (!(json instanceof JSONArray)) {
      return false;
    }

    Object firstObject = ((JSONArray) json).get(0);
    return firstObject instanceof JSONObject && ((JSONObject) firstObject).has("t");
  }

  private float getScale() {
    return isDp ? composition.getScale() : 1f;
  }

  /**
   * Convert the value type of the keyframe to the value type of the animation. Often, these
   * are the same type.
   */
  O convertType(V value) {
    //noinspection unchecked
    return (O) value;
  }

  public boolean hasAnimation() {
    return !keyframes.isEmpty();
  }

  public O getInitialValue() {
    return convertType(initialValue);
  }

  public abstract BaseKeyframeAnimation<?, O> createAnimation();

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("initialValue=").append(initialValue);
    if (!keyframes.isEmpty()) {
      sb.append(", values=").append(Arrays.toString(keyframes.toArray()));
    }
    return sb.toString();
  }
}
