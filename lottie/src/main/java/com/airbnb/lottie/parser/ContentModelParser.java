package com.airbnb.lottie.parser;

import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.content.ContentModel;

import java.io.IOException;

public class ContentModelParser {

  private ContentModelParser() {}

  @Nullable
  public static ContentModel parse(JsonReader reader, LottieComposition composition)
      throws IOException {
    String type = null;

    reader.beginObject();
    while (reader.hasNext()) {
      if (reader.nextName().equals("ty")) {
        type = reader.nextString();
        break;
      } else {
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
        model = CircleShapeParser.parse(reader, composition);
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
        break;
      case "rp":
        model = RepeaterParser.parse(reader, composition);
        break;
      default:
        Log.w(L.TAG, "Unknown shape type " + type);
    }

    while (reader.hasNext()) {
      reader.skipValue();
    }
    reader.endObject();

    return model;
  }
}
