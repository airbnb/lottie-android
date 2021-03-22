package com.airbnb.lottie.parser;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatableShapeValue;
import com.airbnb.lottie.model.content.Mask;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.Logger;

import java.io.IOException;

class MaskParser {

  private MaskParser() {
  }

  static Mask parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    Mask.MaskMode maskMode = null;
    AnimatableShapeValue maskPath = null;
    AnimatableIntegerValue opacity = null;
    boolean inverted = false;

    reader.beginObject();
    while (reader.hasNext()) {
      String mode = reader.nextName();
      switch (mode) {
        case "mode":
          switch (reader.nextString()) {
            case "a":
              maskMode = Mask.MaskMode.MASK_MODE_ADD;
              break;
            case "s":
              maskMode = Mask.MaskMode.MASK_MODE_SUBTRACT;
              break;
            case "n":
              maskMode = Mask.MaskMode.MASK_MODE_NONE;
              break;
            case "i":
              composition.addWarning(
                  "Animation contains intersect masks. They are not supported but will be treated like add masks.");
              maskMode = Mask.MaskMode.MASK_MODE_INTERSECT;
              break;
            default:
              Logger.warning("Unknown mask mode " + mode + ". Defaulting to Add.");
              maskMode = Mask.MaskMode.MASK_MODE_ADD;
          }
          break;
        case "pt":
          maskPath = AnimatableValueParser.parseShapeData(reader, composition);
          break;
        case "o":
          opacity = AnimatableValueParser.parseInteger(reader, composition);
          break;
        case "inv":
          inverted = reader.nextBoolean();
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();

    return new Mask(maskMode, maskPath, opacity, inverted);
  }

}
