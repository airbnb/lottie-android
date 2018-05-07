package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.content.ShapeGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class FontCharacterParser {

  private FontCharacterParser() {}

  static FontCharacter parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    char character = '\0';
    double size = 0;
    double width = 0;
    String style = null;
    String fontFamily = null;
    List<ShapeGroup> shapes = new ArrayList<>();

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "ch":
          character = reader.nextString().charAt(0);
          break;
        case "size":
          size = reader.nextDouble();
          break;
        case "w":
          width = reader.nextDouble();
          break;
        case "style":
          style = reader.nextString();
          break;
        case "fFamily":
          fontFamily = reader.nextString();
          break;
        case "data":
          reader.beginObject();
          while (reader.hasNext()) {
            if ("shapes".equals(reader.nextName())) {
              reader.beginArray();
              while (reader.hasNext()) {
                shapes.add((ShapeGroup) ContentModelParser.parse(reader, composition));
              }
              reader.endArray();
            } else {
              reader.skipValue();
            }
          }
          reader.endObject();
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();

    return new FontCharacter(shapes, character, size, width, style, fontFamily);
  }
}
