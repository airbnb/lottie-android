package com.airbnb.lottie.parser;

import androidx.annotation.Nullable;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.model.animatable.AnimatableTextRangeSelector;
import com.airbnb.lottie.model.animatable.AnimatableTextStyle;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.value.Keyframe;

import java.io.IOException;
import java.util.Collections;

public class AnimatableTextPropertiesParser {

  private static final JsonReader.Options PROPERTIES_NAMES = JsonReader.Options.of("s", "a");

  private static final JsonReader.Options ANIMATABLE_RANGE_PROPERTIES_NAMES = JsonReader.Options.of(
      "s", // start
      "e", // end
      "o" // offset
  );
  private static final JsonReader.Options ANIMATABLE_PROPERTIES_NAMES = JsonReader.Options.of(
      "fc",
      "sc",
      "sw",
      "t",
      "o" // opacity
  );

  private AnimatableTextPropertiesParser() {
  }

  public static AnimatableTextProperties parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    @Nullable AnimatableTextStyle textStyle = null;
    @Nullable AnimatableTextRangeSelector rangeSelector = null;

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(PROPERTIES_NAMES)) {
        case 0: // Range selector
          rangeSelector = parseAnimatableTextRangeSelector(reader, composition);
          break;
        case 1: // Text style for this range
          textStyle = parseAnimatableTextStyle(reader, composition);
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    reader.endObject();

    return new AnimatableTextProperties(textStyle, rangeSelector);
  }

  private static AnimatableTextRangeSelector parseAnimatableTextRangeSelector(JsonReader reader, LottieComposition composition) throws IOException {
    // TODO These may need to be floats in the future if we want to support percentage based ranges.
    AnimatableIntegerValue start = null;
    AnimatableIntegerValue end = null;
    AnimatableIntegerValue offset = null;

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(ANIMATABLE_RANGE_PROPERTIES_NAMES)) {
        case 0: // start
          start = AnimatableValueParser.parseInteger(reader, composition);
          break;
        case 1: // end
          end = AnimatableValueParser.parseInteger(reader, composition);
          break;
        case 2: // offset
          offset = AnimatableValueParser.parseInteger(reader, composition);
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    reader.endObject();

    // If no start value is provided, default to a non-animated value of 0 to match After Effects/Bodymovin.
    if (start == null && end != null) {
      start = new AnimatableIntegerValue(Collections.singletonList(new Keyframe<>(0)));
    }

    return new AnimatableTextRangeSelector(start, end, offset);
  }

  private static AnimatableTextStyle parseAnimatableTextStyle(
      JsonReader reader, LottieComposition composition) throws IOException {
    AnimatableColorValue color = null;
    AnimatableColorValue stroke = null;
    AnimatableFloatValue strokeWidth = null;
    AnimatableFloatValue tracking = null;
    AnimatableIntegerValue opacity = null;

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
        case 4: // opacity
          opacity = AnimatableValueParser.parseInteger(reader, composition);
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    reader.endObject();

    return new AnimatableTextStyle(color, stroke, strokeWidth, tracking, opacity);
  }
}
