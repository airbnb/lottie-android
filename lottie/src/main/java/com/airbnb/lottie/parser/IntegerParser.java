package com.airbnb.lottie.parser;

import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

public class IntegerParser implements ValueParser<Integer> {
  public static final IntegerParser INSTANCE = new IntegerParser();

  private IntegerParser() {
  }

  @Override public Integer parse(JsonReader reader, float scale) throws IOException {
    return Math.round(JsonUtils.valueFromObject(reader) * scale);
  }
}
