package com.airbnb.lottie.parser;


import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.content.ShapeStroke;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.value.Keyframe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ShapeStrokeParser {

  private static final JsonReader.Options NAMES = JsonReader.Options.of(
      "nm",
      "c",
      "w",
      "o",
      "lc",
      "lj",
      "ml",
      "hd",
      "d"
  );
  private static final JsonReader.Options DASH_PATTERN_NAMES = JsonReader.Options.of(
      "n",
      "v"
  );

  private ShapeStrokeParser() {
  }

  static ShapeStroke parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    AnimatableColorValue color = null;
    AnimatableFloatValue width = null;
    AnimatableIntegerValue opacity = null;
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
          color = AnimatableValueParser.parseColor(reader, composition);
          break;
        case 2:
          width = AnimatableValueParser.parseFloat(reader, composition);
          break;
        case 3:
          opacity = AnimatableValueParser.parseInteger(reader, composition);
          break;
        case 4:
          capType = ShapeStroke.LineCapType.values()[reader.nextInt() - 1];
          break;
        case 5:
          joinType = ShapeStroke.LineJoinType.values()[reader.nextInt() - 1];
          break;
        case 6:
          miterLimit = (float) reader.nextDouble();
          break;
        case 7:
          hidden = reader.nextBoolean();
          break;
        case 8:
          reader.beginArray();
          while (reader.hasNext()) {
            String n = null;
            AnimatableFloatValue val = null;

            reader.beginObject();
            while (reader.hasNext()) {
              switch (reader.selectName(DASH_PATTERN_NAMES)) {
                case 0:
                  n = reader.nextString();
                  break;
                case 1:
                  val = AnimatableValueParser.parseFloat(reader, composition);
                  break;
                default:
                  reader.skipName();
                  reader.skipValue();
              }
            }
            reader.endObject();

            switch (n) {
              case "o":
                offset = val;
                break;
              case "d":
              case "g":
                composition.setHasDashPattern(true);
                lineDashPattern.add(val);
                break;
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

    // Telegram sometimes omits opacity.
    // https://github.com/airbnb/lottie-android/issues/1600
    opacity = opacity == null ? new AnimatableIntegerValue(Collections.singletonList(new Keyframe<>(100))) : opacity;
    return new ShapeStroke(
        name, offset, lineDashPattern, color, opacity, width, capType, joinType, miterLimit, hidden);
  }
}
