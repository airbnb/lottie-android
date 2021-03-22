package com.airbnb.lottie.parser;

import android.graphics.PointF;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePathValue;
import com.airbnb.lottie.model.animatable.AnimatableScaleValue;
import com.airbnb.lottie.model.animatable.AnimatableSplitDimensionPathValue;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.value.Keyframe;

import java.io.IOException;

public class AnimatableTransformParser {

  private AnimatableTransformParser() {
  }


  private static final JsonReader.Options NAMES = JsonReader.Options.of(
      "a",  // 1
      "p",  // 2
      "s",  // 3
      "rz", // 4
      "r",  // 5
      "o",  // 6
      "so", // 7
      "eo", // 8
      "sk", // 9
      "sa"  // 10
  );
  private static final JsonReader.Options ANIMATABLE_NAMES = JsonReader.Options.of("k");

  public static AnimatableTransform parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    AnimatablePathValue anchorPoint = null;
    AnimatableValue<PointF, PointF> position = null;
    AnimatableScaleValue scale = null;
    AnimatableFloatValue rotation = null;
    AnimatableIntegerValue opacity = null;
    AnimatableFloatValue startOpacity = null;
    AnimatableFloatValue endOpacity = null;
    AnimatableFloatValue skew = null;
    AnimatableFloatValue skewAngle = null;

    boolean isObject = reader.peek() == JsonReader.Token.BEGIN_OBJECT;
    if (isObject) {
      reader.beginObject();
    }
    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0: // a
          reader.beginObject();
          while (reader.hasNext()) {
            switch (reader.selectName(ANIMATABLE_NAMES)) {
              case 0:
                anchorPoint = AnimatablePathValueParser.parse(reader, composition);
                break;
              default:
                reader.skipName();
                reader.skipValue();
            }
          }
          reader.endObject();
          break;
        case 1: // p
          position =
              AnimatablePathValueParser.parseSplitPath(reader, composition);
          break;
        case 2: // s
          scale = AnimatableValueParser.parseScale(reader, composition);
          break;
        case 3: // rz
          composition.addWarning("Lottie doesn't support 3D layers.");
        case 4: // r
          /*
           * Sometimes split path rotation gets exported like:
           *         "rz": {
           *           "a": 1,
           *           "k": [
           *             {}
           *           ]
           *         },
           * which doesn't parse to a real keyframe.
           */
          rotation = AnimatableValueParser.parseFloat(reader, composition, false);
          if (rotation.getKeyframes().isEmpty()) {
            rotation.getKeyframes().add(new Keyframe<>(composition, 0f, 0f, null, 0f, composition.getEndFrame()));
          } else if (rotation.getKeyframes().get(0).startValue == null) {
            rotation.getKeyframes().set(0, new Keyframe<>(composition, 0f, 0f, null, 0f, composition.getEndFrame()));
          }
          break;
        case 5: // o
          opacity = AnimatableValueParser.parseInteger(reader, composition);
          break;
        case 6: // so
          startOpacity = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case 7: // eo
          endOpacity = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case 8: // sk
          skew = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case 9: // sa
          skewAngle = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    if (isObject) {
      reader.endObject();
    }

    if (isAnchorPointIdentity(anchorPoint)) {
      anchorPoint = null;
    }
    if (isPositionIdentity(position)) {
      position = null;
    }
    if (isRotationIdentity(rotation)) {
      rotation = null;
    }
    if (isScaleIdentity(scale)) {
      scale = null;
    }
    if (isSkewIdentity(skew)) {
      skew = null;
    }
    if (isSkewAngleIdentity(skewAngle)) {
      skewAngle = null;
    }
    return new AnimatableTransform(anchorPoint, position, scale, rotation, opacity, startOpacity, endOpacity, skew, skewAngle);
  }

  private static boolean isAnchorPointIdentity(AnimatablePathValue anchorPoint) {
    return anchorPoint == null || (anchorPoint.isStatic() && anchorPoint.getKeyframes().get(0).startValue.equals(0f, 0f));
  }

  private static boolean isPositionIdentity(AnimatableValue<PointF, PointF> position) {
    return position == null || (
        !(position instanceof AnimatableSplitDimensionPathValue) &&
            position.isStatic() && position.getKeyframes().get(0).startValue.equals(0f, 0f));
  }

  private static boolean isRotationIdentity(AnimatableFloatValue rotation) {
    return rotation == null || (rotation.isStatic() && rotation.getKeyframes().get(0).startValue == 0f);
  }

  private static boolean isScaleIdentity(AnimatableScaleValue scale) {
    return scale == null || (scale.isStatic() && scale.getKeyframes().get(0).startValue.equals(1f, 1f));
  }

  private static boolean isSkewIdentity(AnimatableFloatValue skew) {
    return skew == null || (skew.isStatic() && skew.getKeyframes().get(0).startValue == 0f);
  }

  private static boolean isSkewAngleIdentity(AnimatableFloatValue skewAngle) {
    return skewAngle == null || (skewAngle.isStatic() && skewAngle.getKeyframes().get(0).startValue == 0f);
  }
}
