package com.airbnb.lottie.parser;

import androidx.annotation.Nullable;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.Logger;

import java.io.IOException;

class ContentModelParser {

  private static JsonReader.Options NAMES = JsonReader.Options.of(
      "ty",
      "d"
  );

  private ContentModelParser() {
  }

  @Nullable
  static ContentModel parse(JsonReader reader, LottieComposition composition)
      throws IOException {
    String type = null;

    reader.beginObject();
    // Unfortunately, for an ellipse, d is before "ty" which means that it will get parsed
    // before we are in the ellipse parser.
    // "d" is 2 for normal and 3 for reversed.
    int d = 2;
    typeLoop:
    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0:
          type = reader.nextString();
          break typeLoop;
        case 1:
          d = reader.nextInt();
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }

    if (type == null) {
      return null;
    }

    ContentModel model = null;
    switch (type) {
      case "gr":
        model = ShapeGroupParser.parse(reader, composition);
        break;
      case "st":
        model = ShapeStrokeParser.parse(reader, composition);
        break;
      case "gs":
        model = GradientStrokeParser.parse(reader, composition);
        break;
      case "fl":
        model = ShapeFillParser.parse(reader, composition);
        break;
      case "gf":
        model = GradientFillParser.parse(reader, composition);
        break;
      case "tr":
        model = AnimatableTransformParser.parse(reader, composition);
        break;
      case "sh":
        model = ShapePathParser.parse(reader, composition);
        break;
      case "el":
        model = CircleShapeParser.parse(reader, composition, d);
        break;
      case "rc":
        model = RectangleShapeParser.parse(reader, composition);
        break;
      case "tm":
        model = ShapeTrimPathParser.parse(reader, composition);
        break;
      case "sr":
        model = PolystarShapeParser.parse(reader, composition);
        break;
      case "mm":
        model = MergePathsParser.parse(reader);
        composition.addWarning("Animation contains merge paths. Merge paths are only " +
            "supported on KitKat+ and must be manually enabled by calling " +
            "enableMergePathsForKitKatAndAbove().");
        break;
      case "rp":
        model = RepeaterParser.parse(reader, composition);
        break;
      default:
        Logger.warning("Unknown shape type " + type);
    }

    while (reader.hasNext()) {
      reader.skipValue();
    }
    reader.endObject();

    return model;
  }
}
