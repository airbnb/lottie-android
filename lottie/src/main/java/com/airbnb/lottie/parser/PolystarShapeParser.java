package com.airbnb.lottie.parser;

import android.graphics.PointF;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.model.content.PolystarShape;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

class PolystarShapeParser {
  private static final JsonReader.Options NAMES = JsonReader.Options.of(
      "nm",
      "sy",
      "pt",
      "p",
      "r",
      "or",
      "os",
      "ir",
      "is",
      "hd"
  );

  private PolystarShapeParser() {
  }

  static PolystarShape parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    PolystarShape.Type type = null;
    AnimatableFloatValue points = null;
    AnimatableValue<PointF, PointF> position = null;
    AnimatableFloatValue rotation = null;
    AnimatableFloatValue outerRadius = null;
    AnimatableFloatValue outerRoundedness = null;
    AnimatableFloatValue innerRadius = null;
    AnimatableFloatValue innerRoundedness = null;
    boolean hidden = false;

    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0:
          name = reader.nextString();
          break;
        case 1:
          type = PolystarShape.Type.forValue(reader.nextInt());
          break;
        case 2:
          points = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case 3:
          position = AnimatablePathValueParser.parseSplitPath(reader, composition);
          break;
        case 4:
          rotation = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case 5:
          outerRadius = AnimatableValueParser.parseFloat(reader, composition);
          break;
        case 6:
          outerRoundedness = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case 7:
          innerRadius = AnimatableValueParser.parseFloat(reader, composition);
          break;
        case 8:
          innerRoundedness = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case 9:
          hidden = reader.nextBoolean();
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }

    return new PolystarShape(
        name, type, points, position, rotation, innerRadius, outerRadius,
        innerRoundedness, outerRoundedness, hidden);
  }
}
