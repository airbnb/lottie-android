package com.airbnb.lottie.parser;

import android.graphics.PointF;
import android.util.JsonToken;
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


  static JsonReader.Options NAMES = JsonReader.Options.of(
      "a",
      "p",
      "s",
      "rz",
      "r",
      "o",
      "so",
      "eo",
      "sk",
      "sa"
  );

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
        case 0:
          reader.beginObject();
          while (reader.hasNext()) {
            if (reader.nextName().equals("k")) {
              anchorPoint = AnimatablePathValueParser.parse(reader, composition);
            } else {
              reader.skipValue();
            }
          }
          reader.endObject();
          break;
        case 1:
          position =
              AnimatablePathValueParser.parseSplitPath(reader, composition);
          break;
        case 2:
          scale = AnimatableValueParser.parseScale(reader, composition);
          break;
        case 3:
          composition.addWarning("Lottie doesn't support 3D layers.");
        case 4:
          /**
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
            rotation.getKeyframes().add(new Keyframe(composition, 0f, 0f, null, 0f, composition.getEndFrame()));
          } else if (rotation.getKeyframes().get(0).startValue == null) {
            rotation.getKeyframes().set(0, new Keyframe(composition, 0f, 0f, null, 0f, composition.getEndFrame()));
          }
          break;
        case 5:
          opacity = AnimatableValueParser.parseInteger(reader, composition);
          break;
        case 6:
          startOpacity = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case 7:
          endOpacity = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case 8:
          skew = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case 9:
          skewAngle = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        default:
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
