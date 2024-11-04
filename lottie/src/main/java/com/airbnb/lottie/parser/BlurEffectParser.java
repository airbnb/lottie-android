package com.airbnb.lottie.parser;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.content.BlurEffect;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

class BlurEffectParser {

  private static final JsonReader.Options BLUR_EFFECT_NAMES = JsonReader.Options.of(
      "ef"
  );
  private static final JsonReader.Options INNER_BLUR_EFFECT_NAMES = JsonReader.Options.of(
      "ty",
      "v"
  );

  @Nullable
  static BlurEffect parse(JsonReader reader, LottieComposition composition) throws IOException {
    BlurEffect blurEffect = null;
    while (reader.hasNext()) {
      switch (reader.selectName(BLUR_EFFECT_NAMES)) {
        case 0:
          reader.beginArray();
            while (reader.hasNext()) {
              BlurEffect be = maybeParseInnerEffect(reader, composition);
              if (be != null) {
                blurEffect = be;
              }
            }
          reader.endArray();
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    return blurEffect;
  }

  @Nullable
  private static BlurEffect maybeParseInnerEffect(JsonReader reader, LottieComposition composition) throws IOException {
    BlurEffect blurEffect = null;
    boolean isCorrectType = false;
    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(INNER_BLUR_EFFECT_NAMES)) {
        case 0:
          isCorrectType = reader.nextInt() == 0;
          break;
        case 1:
          if (isCorrectType) {
            blurEffect = new BlurEffect(AnimatableValueParser.parseFloat(reader, composition));
            break;
          } else {
            reader.skipValue();
          }
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    reader.endObject();
    return blurEffect;
  }
}
