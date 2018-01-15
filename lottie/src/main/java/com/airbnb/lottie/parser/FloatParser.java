package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.utils.JsonUtils;

import java.io.IOException;

public class FloatParser implements ValueParser<Float> {
  public static final FloatParser INSTANCE = new FloatParser();

  private FloatParser() {}

  @Override public Float parse(JsonReader reader, float scale) throws IOException {
    return JsonUtils.valueFromObject(reader) * scale;
  }
}