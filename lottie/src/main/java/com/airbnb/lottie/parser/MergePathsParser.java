package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.model.content.MergePaths;

import java.io.IOException;

class MergePathsParser {

  private MergePathsParser() {}

  static MergePaths parse(JsonReader reader) throws IOException {
    String name = null;
    MergePaths.MergePathsMode mode = null;

    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "nm":
          name = reader.nextString();
          break;
        case "mm":
          mode =  MergePaths.MergePathsMode.forId(reader.nextInt());
          break;
        default:
          reader.skipValue();
      }
    }

    return new MergePaths(name, mode);
  }
}
