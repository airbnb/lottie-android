package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.model.Font;

import java.io.IOException;

public class FontParser {

  private FontParser() {}

  public static Font parse(JsonReader reader) throws IOException {
    String family = null;
    String name = null;
    String style = null;
    float ascent = 0;

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "fFamily":
          family = reader.nextString();
          break;
        case "fName":
          name = reader.nextString();
          break;
        case "fStyle":
          style = reader.nextString();
          break;
        case "ascent":
          ascent = (float) reader.nextDouble();
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();

    return new Font(family, name, style, ascent);
  }
}
