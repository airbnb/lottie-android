package com.airbnb.lottie.parser;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.content.ShapeGroup;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class FontCharacterParser {
  private static final JsonReader.Options NAMES = JsonReader.Options.of(
      "ch",
      "size",
      "w",
      "style",
      "fFamily",
      "data"
  );
  private static final JsonReader.Options DATA_NAMES = JsonReader.Options.of("shapes");

  private FontCharacterParser() {
  }

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
      switch (reader.selectName(NAMES)) {
        case 0:
          character = reader.nextString().charAt(0);
          break;
        case 1:
          size = reader.nextDouble();
          break;
        case 2:
          width = reader.nextDouble();
          break;
        case 3:
          style = reader.nextString();
          break;
        case 4:
          fontFamily = reader.nextString();
          break;
        case 5:
          reader.beginObject();
          while (reader.hasNext()) {
            switch (reader.selectName(DATA_NAMES)) {
              case 0:
                reader.beginArray();
                while (reader.hasNext()) {
                  shapes.add((ShapeGroup) ContentModelParser.parse(reader, composition));
                }
                reader.endArray();
                break;
              default:
                reader.skipName();
                reader.skipValue();
            }
          }
          reader.endObject();
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    reader.endObject();

    return new FontCharacter(shapes, character, size, width, style, fontFamily);
  }
}
