package com.airbnb.lottie.parser;

import android.graphics.PointF;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;

public class PointFParser implements ValueParser<PointF> {
  public static final PointFParser INSTANCE = new PointFParser();

  private PointFParser() {}

  @Override public PointF parse(JsonReader reader, float scale) throws IOException {
    JsonToken token = reader.peek();
    if (token == JsonToken.BEGIN_ARRAY) {
      return JsonUtils.jsonToPoint(reader, scale);
    } else if (token == JsonToken.BEGIN_OBJECT) {
      return JsonUtils.jsonToPoint(reader, scale);
    } else if (token == JsonToken.NUMBER) {
      // This is the case where the static value for a property is an array of numbers.
      // We begin the array to see if we have an array of keyframes but it's just an array
      // of static numbers instead.
      PointF point = new PointF((float) reader.nextDouble() * scale, (float) reader.nextDouble() * scale);
      while (reader.hasNext()) {
        reader.skipValue();
      }
      return point;
    } else {
      throw new IllegalArgumentException("Cannot convert json to point. Next token is " + token);
    }
  }
}
