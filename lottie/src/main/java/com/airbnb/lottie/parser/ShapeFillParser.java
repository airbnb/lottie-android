package com.airbnb.lottie.parser;

import android.graphics.Path;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.content.ShapeFill;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

class ShapeFillParser {
  private static final JsonReader.Options NAMES = JsonReader.Options.of(
      "nm",
      "c",
      "o",
      "fillEnabled",
      "r",
      "hd"
  );

  private ShapeFillParser() {
  }

  static ShapeFill parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    AnimatableColorValue color = null;
    boolean fillEnabled = false;
    AnimatableIntegerValue opacity = null;
    String name = null;
    int fillTypeInt = 1;
    boolean hidden = false;

    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0:
          name = reader.nextString();
          break;
        case 1:
          color = AnimatableValueParser.parseColor(reader, composition);
          break;
        case 2:
          opacity = AnimatableValueParser.parseInteger(reader, composition);
          break;
        case 3:
          fillEnabled = reader.nextBoolean();
          break;
        case 4:
          fillTypeInt = reader.nextInt();
          break;
        case 5:
          hidden = reader.nextBoolean();
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }

    Path.FillType fillType = fillTypeInt == 1 ? Path.FillType.WINDING : Path.FillType.EVEN_ODD;
    return new ShapeFill(name, fillEnabled, fillType, color, opacity, hidden);
  }
}
