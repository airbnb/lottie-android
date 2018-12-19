package com.airbnb.lottie.parser;

import android.util.JsonReader;

import java.io.IOException;

interface ValueParser<V> {
  V parse(JsonReader reader, float scale) throws IOException;

  /**
   * Value to return if the json is empty. This may be done in an optimization to omit
   * properties with no animation.
   */
  V defaultValue();
}
