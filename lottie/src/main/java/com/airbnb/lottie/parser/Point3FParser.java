package com.airbnb.lottie.parser;

import com.airbnb.lottie.animation.Point3F;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

public class Point3FParser implements ValueParser<Point3F> {
  public static final Point3FParser INSTANCE = new Point3FParser();

  private Point3FParser() {
  }

  @Override
  public Point3F parse(JsonReader reader, float scale) throws IOException {
    JsonReader.Token token = reader.peek();
    if (token == JsonReader.Token.BEGIN_ARRAY) {
      return JsonUtils.jsonToPoint3(reader, scale);
    } else if (token == JsonReader.Token.BEGIN_OBJECT) {
      return JsonUtils.jsonToPoint3(reader, scale);
    } else if (token == JsonReader.Token.NUMBER) {
      // This is the case where the static value for a property is an array of numbers.
      // We begin the array to see if we have an array of keyframes but it's just an array
      // of static numbers instead.
      Point3F point = new Point3F((float) reader.nextDouble() * scale, (float) reader.nextDouble() * scale,
          (float) reader.nextDouble() * scale);
      while (reader.hasNext()) {
        reader.skipValue();
      }
      return point;
    } else {
      throw new IllegalArgumentException("Cannot convert json to point. Next token is " + token);
    }
  }
}
