package com.airbnb.lottie.parser;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

public class DropShadowEffectParser {

  private static final JsonReader.Options DROP_SHADOW_EFFECT_NAMES = JsonReader.Options.of(
      "ef"
  );
  private static final JsonReader.Options INNER_EFFECT_NAMES = JsonReader.Options.of(
      "nm",
      "v"
  );

  private AnimatableColorValue color;
  private AnimatableFloatValue opacity;
  private AnimatableFloatValue direction;
  private AnimatableFloatValue distance;
  private AnimatableFloatValue radius;

  @Nullable
  DropShadowEffect parse(JsonReader reader, LottieComposition composition) throws IOException {
    while (reader.hasNext()) {
      switch (reader.selectName(DROP_SHADOW_EFFECT_NAMES)) {
        case 0:
          reader.beginArray();
          while (reader.hasNext()) {
            maybeParseInnerEffect(reader, composition);
          }
          reader.endArray();
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    if (color != null && opacity != null && direction != null && distance != null && radius != null) {
      return new DropShadowEffect(color, opacity, direction, distance, radius);
    }
    return null;
  }

  private void maybeParseInnerEffect(JsonReader reader, LottieComposition composition) throws IOException {
    String currentEffectName = "";
    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(INNER_EFFECT_NAMES)) {
        case 0:
          currentEffectName = reader.nextString();
          break;
        case 1:
          switch (currentEffectName) {
            case "Shadow Color":
              color = AnimatableValueParser.parseColor(reader, composition);
              break;
            case "Opacity":
              opacity = AnimatableValueParser.parseFloat(reader, composition, false);
              break;
            case "Direction":
              direction = AnimatableValueParser.parseFloat(reader, composition, false);
              break;
            case "Distance":
              distance = AnimatableValueParser.parseFloat(reader, composition, false);
              break;
            case "Softness":
              radius = AnimatableValueParser.parseFloat(reader, composition, false);
              break;
            default:
              reader.skipValue();
              break;
          }
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    reader.endObject();
  }
}
