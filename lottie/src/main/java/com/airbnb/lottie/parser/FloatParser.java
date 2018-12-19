package com.airbnb.lottie.parser;

import android.util.JsonReader;

import java.io.IOException;

public class FloatParser implements ValueParser<Float> {
  public static final FloatParser INSTANCE = new FloatParser();

  private FloatParser() {}

  @Override public Float parse(JsonReader reader, float scale) throws IOException {
    return JsonUtils.valueFromObject(reader) * scale;
  }

  @Override
  public Float defaultValue() {
    return 0f;
  }
}