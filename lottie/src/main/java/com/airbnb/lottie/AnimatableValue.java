package com.airbnb.lottie;

import org.json.JSONException;

interface AnimatableValue<V, O> {
  V valueFromObject(Object object, float scale) throws JSONException;
  BaseKeyframeAnimation<?, O> createAnimation();
  boolean hasAnimation();
}
