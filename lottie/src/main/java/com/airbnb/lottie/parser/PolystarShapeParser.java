package com.airbnb.lottie.parser;

import android.graphics.PointF;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.model.content.PolystarShape;

import java.io.IOException;

class PolystarShapeParser {

  private PolystarShapeParser() {}

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

    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "nm":
          name = reader.nextString();
          break;
        case "sy":
          type = PolystarShape.Type.forValue(reader.nextInt());
          break;
        case "pt":
          points = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case "p":
          position = AnimatablePathValueParser.parseSplitPath(reader, composition);
          break;
        case "r":
          rotation = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case "or":
          outerRadius = AnimatableValueParser.parseFloat(reader, composition);
          break;
        case "os":
          outerRoundedness = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case "ir":
          innerRadius = AnimatableValueParser.parseFloat(reader, composition);
          break;
        case "is":
          innerRoundedness = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        default:
          reader.skipValue();
      }
    }

    return new PolystarShape(
        name, type, points, position, rotation, innerRadius, outerRadius, innerRoundedness, outerRoundedness);
  }
}
