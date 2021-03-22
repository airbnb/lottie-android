package com.airbnb.lottie.parser;

import com.airbnb.lottie.model.content.MergePaths;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

class MergePathsParser {
  private static final JsonReader.Options NAMES = JsonReader.Options.of(
      "nm",
      "mm",
      "hd"
  );

  private MergePathsParser() {
  }

  static MergePaths parse(JsonReader reader) throws IOException {
    String name = null;
    MergePaths.MergePathsMode mode = null;
    boolean hidden = false;

    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0:
          name = reader.nextString();
          break;
        case 1:
          mode = MergePaths.MergePathsMode.forId(reader.nextInt());
          break;
        case 2:
          hidden = reader.nextBoolean();
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }

    return new MergePaths(name, mode, hidden);
  }
}
