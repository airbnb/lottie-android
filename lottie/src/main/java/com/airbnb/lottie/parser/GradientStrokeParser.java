package com.airbnb.lottie.parser;


import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.content.GradientStroke;
import com.airbnb.lottie.model.content.GradientType;
import com.airbnb.lottie.model.content.ShapeStroke;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class GradientStrokeParser {

  private GradientStrokeParser() {}
  private static JsonReader.Options NAMES = JsonReader.Options.of(
      "nm",
      "g",
      "o",
      "t",
      "s",
      "e",
      "w",
      "lc",
      "lj",
      "ml",
      "hd",
      "d"





  );

  static GradientStroke parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    AnimatableGradientColorValue color = null;
    AnimatableIntegerValue opacity = null;
    GradientType gradientType = null;
    AnimatablePointValue startPoint = null;
    AnimatablePointValue endPoint = null;
    AnimatableFloatValue width = null;
    ShapeStroke.LineCapType capType = null;
    ShapeStroke.LineJoinType joinType = null;
    AnimatableFloatValue offset = null;
    float miterLimit = 0f;
    boolean hidden = false;


    List<AnimatableFloatValue> lineDashPattern = new ArrayList<>();

    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0:
          name = reader.nextString();
          break;
        case 1:
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
        case 2:
          opacity = AnimatableValueParser.parseInteger(reader, composition);
          break;
        case 3:
          gradientType = reader.nextInt() == 1 ? GradientType.LINEAR : GradientType.RADIAL;
          break;
        case 4:
          startPoint = AnimatableValueParser.parsePoint(reader, composition);
          break;
        case 5:
          endPoint = AnimatableValueParser.parsePoint(reader, composition);
          break;
        case 6:
          width = AnimatableValueParser.parseFloat(reader, composition);
          break;
        case 7:
          capType = ShapeStroke.LineCapType.values()[reader.nextInt() - 1];
          break;
        case 8:
          joinType = ShapeStroke.LineJoinType.values()[reader.nextInt() - 1];
          break;
        case 9:
          miterLimit = (float) reader.nextDouble();
          break;
        case 10:
          hidden = reader.nextBoolean();
          break;
        case 11:
          reader.beginArray();
          while (reader.hasNext()) {
            String n = null;
            AnimatableFloatValue val = null;
            reader.beginObject();
            while (reader.hasNext()) {
              switch (reader.nextName()) {
                case "n":
                  n = reader.nextString();
                  break;
                case "v":
                  val = AnimatableValueParser.parseFloat(reader, composition);
                  break;
                default:
                  reader.skipValue();
              }
            }
            reader.endObject();

            if (n.equals("o")) {
              offset = val;
            } else if (n.equals("d") || n.equals("g")) {
              composition.setHasDashPattern(true);
              lineDashPattern.add(val);
            }
          }
          reader.endArray();
          if (lineDashPattern.size() == 1) {
            // If there is only 1 value then it is assumed to be equal parts on and off.
            lineDashPattern.add(lineDashPattern.get(0));
          }
          break;
        default:
          reader.skipValue();
      }
    }

    return new GradientStroke(
        name, gradientType, color, opacity, startPoint, endPoint, width, capType, joinType,
        miterLimit, lineDashPattern, offset, hidden);
  }
}
