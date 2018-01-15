package com.airbnb.lottie.model.animatable;

import android.support.annotation.Nullable;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.parser.KeyframesParser;

import java.io.IOException;
import java.util.List;

class AnimatableValueParser<T> {
  private final JsonReader reader;
  private final float scale;
  private final LottieComposition composition;
  private final AnimatableValue.Factory<T> valueFactory;

  private AnimatableValueParser(JsonReader reader, float scale, LottieComposition
      composition, AnimatableValue.Factory<T> valueFactory) {
    this.reader = reader;
    this.scale = scale;
    this.composition = composition;
    this.valueFactory = valueFactory;
  }

  /**
   * Will return null if the animation can't be played such as if it has expressions.
   */
  @Nullable static <T> List<Keyframe<T>> newInstance(JsonReader reader, float scale,
      LottieComposition composition, AnimatableValue.Factory<T> valueFactory) throws IOException {
    AnimatableValueParser<T> parser =
        new AnimatableValueParser<>(reader, scale, composition, valueFactory);
    return parser.parseKeyframes();
  }

  /**
   * Will return null if the animation can't be played such as if it has expressions.
   */
  private List<Keyframe<T>> parseKeyframes() throws IOException {
    return KeyframesParser.parse(reader, composition, scale, valueFactory);
  }
}
