package com.airbnb.lottie.parser;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

public class AnimatableTextPropertiesParser {

  private static JsonReader.Options PROPERTIES_NAMES = JsonReader.Options.of("a");
  private static JsonReader.Options ANIMATABLE_PROPERTIES_NAMES = JsonReader.Options.of(
      "fc",
      "sc",
      "sw",
      "t"
  );

  private AnimatableTextPropertiesParser() {
  }

  public static AnimatableTextProperties parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    AnimatableTextProperties anim = null;

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(PROPERTIES_NAMES)) {
        case 0:
          anim = parseAnimatableTextProperties(reader, composition);
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    reader.endObject();
    if (anim == null) {
      // Not sure if this is possible.
      return new AnimatableTextProperties(null, null, null, null);
    }
    return anim;
  }

  private static AnimatableTextProperties parseAnimatableTextProperties(
      JsonReader reader, LottieComposition composition) throws IOException {
    AnimatableColorValue color = null;
    AnimatableColorValue stroke = null;
    AnimatableFloatValue strokeWidth = null;
    AnimatableFloatValue tracking = null;

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(ANIMATABLE_PROPERTIES_NAMES)) {
        case 0:
          color = AnimatableValueParser.parseColor(reader, composition);
          break;
        case 1:
          stroke = AnimatableValueParser.parseColor(reader, composition);
          break;
        case 2:
          strokeWidth = AnimatableValueParser.parseFloat(reader, composition);
          break;
        case 3:
          tracking = AnimatableValueParser.parseFloat(reader, composition);
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    reader.endObject();

    return new AnimatableTextProperties(color, stroke, strokeWidth, tracking);
  }
}
