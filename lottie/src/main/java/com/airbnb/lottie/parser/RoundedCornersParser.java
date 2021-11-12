package com.airbnb.lottie.parser;

import android.graphics.PointF;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.model.content.RectangleShape;
import com.airbnb.lottie.model.content.RoundedCorners;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

public class RoundedCornersParser {
  private static final JsonReader.Options NAMES = JsonReader.Options.of(
      "nm", // 0
      "r",  // 1
      "hd"  // 1
  );

  private RoundedCornersParser() {
  }

  @Nullable
  static RoundedCorners parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    AnimatableValue<Float, Float> cornerRadius = null;
    boolean hidden = false;

    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0: //nm
          name = reader.nextString();
          break;
        case 1: // r
          cornerRadius = AnimatableValueParser.parseFloat(reader, composition, true);
          break;
        case 2: // hd
          hidden = reader.nextBoolean();
          break;
        default:
          reader.skipValue();
      }
    }

    return hidden ? null : new RoundedCorners(name, cornerRadius);
  }
}
