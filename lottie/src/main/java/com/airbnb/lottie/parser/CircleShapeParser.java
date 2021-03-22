package com.airbnb.lottie.parser;

import android.graphics.PointF;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.model.content.CircleShape;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

class CircleShapeParser {

  private static JsonReader.Options NAMES = JsonReader.Options.of(
      "nm",
      "p",
      "s",
      "hd",
      "d"
  );

  private CircleShapeParser() {
  }

  static CircleShape parse(
      JsonReader reader, LottieComposition composition, int d) throws IOException {
    String name = null;
    AnimatableValue<PointF, PointF> position = null;
    AnimatablePointValue size = null;
    boolean reversed = d == 3;
    boolean hidden = false;

    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0:
          name = reader.nextString();
          break;
        case 1:
          position = AnimatablePathValueParser.parseSplitPath(reader, composition);
          break;
        case 2:
          size = AnimatableValueParser.parsePoint(reader, composition);
          break;
        case 3:
          hidden = reader.nextBoolean();
          break;
        case 4:
          // "d" is 2 for normal and 3 for reversed.
          reversed = reader.nextInt() == 3;
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }

    return new CircleShape(name, position, size, reversed, hidden);
  }
}
