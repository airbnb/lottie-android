package com.airbnb.lottie.parser;

import android.graphics.Path;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.content.ShapeFill;

import java.io.IOException;

class ShapeFillParser {

  private ShapeFillParser() {}

  static ShapeFill parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    AnimatableColorValue color = null;
    boolean fillEnabled = false;
    AnimatableIntegerValue opacity = null;
    String name = null;
    int fillTypeInt = 1;

    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "nm":
          name = reader.nextString();
          break;
        case "c":
          color = AnimatableValueParser.parseColor(reader, composition);
          break;
        case "o":
          opacity = AnimatableValueParser.parseInteger(reader, composition);
          break;
        case "fillEnabled":
          fillEnabled = reader.nextBoolean();
          break;
        case "r":
          fillTypeInt = reader.nextInt();
          break;
        default:
          reader.skipValue();
      }
    }

    Path.FillType fillType = fillTypeInt == 1 ? Path.FillType.WINDING : Path.FillType.EVEN_ODD;
    return new ShapeFill(name, fillEnabled, fillType, color, opacity);
  }
}
