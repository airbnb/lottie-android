package com.airbnb.lottie.parser;

import android.graphics.Path;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.content.GradientFill;
import com.airbnb.lottie.model.content.GradientType;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

class GradientFillParser {

  private GradientFillParser() {}

  static GradientFill parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    AnimatableGradientColorValue color = null;
    AnimatableIntegerValue opacity = null;
    GradientType gradientType = null;
    AnimatablePointValue startPoint = null;
    AnimatablePointValue endPoint = null;
    Path.FillType fillType = Path.FillType.WINDING;
    boolean hidden = false;

    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "nm":
          name = reader.nextString();
          break;
        case "g":
          int points = -1;
          reader.beginObject();
          while (reader.hasNext()) {
            switch (reader.nextName()) {
              case "p":
                points = reader.nextInt();
                break;
              case "k":
                color = AnimatableValueParser.parseGradientColor(reader, composition, points);
                break;
              default:
                reader.skipValue();
            }
          }
          reader.endObject();
          break;
        case "o":
          opacity = AnimatableValueParser.parseInteger(reader, composition);
          break;
        case "t":
          gradientType = reader.nextInt() == 1 ? GradientType.LINEAR : GradientType.RADIAL;
          break;
        case "s":
          startPoint = AnimatableValueParser.parsePoint(reader, composition);
          break;
        case "e":
          endPoint = AnimatableValueParser.parsePoint(reader, composition);
          break;
        case "r":
          fillType = reader.nextInt() == 1 ? Path.FillType.WINDING : Path.FillType.EVEN_ODD;
          break;
        case "hd":
          hidden = reader.nextBoolean();
          break;
        default:
          reader.skipValue();
      }
    }

    return new GradientFill(
        name, gradientType, fillType, color, opacity, startPoint, endPoint, null, null, hidden);
  }
}
