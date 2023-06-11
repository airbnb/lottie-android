package com.airbnb.lottie.parser;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableCameraTransform;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatablePoint3Value;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

public class AnimatableCameraTransformParser {

  private AnimatableCameraTransformParser() {
  }


  private static final JsonReader.Options NAMES = JsonReader.Options.of(
      "a",  // 0
      "p",  // 1
      "or", // 2
      "rx", // 3
      "ry", // 4
      "rz"  // 5
  );
  private static final JsonReader.Options ANIMATABLE_NAMES = JsonReader.Options.of("k");

  public static AnimatableCameraTransform parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    AnimatablePoint3Value pointOfInterest = null;
    AnimatablePoint3Value position = null;
    AnimatablePoint3Value orientation = null;
    AnimatableFloatValue rotationX = null;
    AnimatableFloatValue rotationY = null;
    AnimatableFloatValue rotationZ = null;

    boolean isObject = reader.peek() == JsonReader.Token.BEGIN_OBJECT;
    if (isObject) {
      reader.beginObject();
    }
    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0: // a
          pointOfInterest = AnimatableValueParser.parsePoint3(reader, composition, true);
          break;
        case 1: // p
          position = AnimatableValueParser.parsePoint3(reader, composition, true);
          break;
        case 2: // or
          orientation = AnimatableValueParser.parsePoint3(reader, composition, false);
          break;
        case 3: // rx
          rotationX = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case 4: // ry
          rotationY = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case 5: // rz
          rotationZ = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    if (isObject) {
      reader.endObject();
    }

    return new AnimatableCameraTransform(pointOfInterest, position, orientation ,rotationX, rotationY, rotationZ);
  }
}
