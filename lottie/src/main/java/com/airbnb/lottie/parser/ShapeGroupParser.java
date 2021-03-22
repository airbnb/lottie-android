package com.airbnb.lottie.parser;


import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.content.ShapeGroup;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ShapeGroupParser {

  private ShapeGroupParser() {
  }

  private static JsonReader.Options NAMES = JsonReader.Options.of(
      "nm",
      "hd",
      "it"
  );

  static ShapeGroup parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    boolean hidden = false;
    List<ContentModel> items = new ArrayList<>();

    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0:
          name = reader.nextString();
          break;
        case 1:
          hidden = reader.nextBoolean();
          break;
        case 2:
          reader.beginArray();
          while (reader.hasNext()) {
            ContentModel newItem = ContentModelParser.parse(reader, composition);
            if (newItem != null) {
              items.add(newItem);
            }
          }
          reader.endArray();
          break;
        default:
          reader.skipValue();
      }
    }

    return new ShapeGroup(name, items, hidden);
  }
}
