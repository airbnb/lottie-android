package com.airbnb.lottie.model.animatable;

import android.util.JsonReader;

import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;

import java.io.IOException;

public interface AnimatableValue<K, A> {
  BaseKeyframeAnimation<K, A> createAnimation();

  interface Factory<V> {
    V valueFromObject(JsonReader reader, float scale) throws IOException;
  }
}
