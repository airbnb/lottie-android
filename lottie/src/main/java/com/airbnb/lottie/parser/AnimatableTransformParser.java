package com.airbnb.lottie.parser;

import android.graphics.PointF;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePathValue;
import com.airbnb.lottie.model.animatable.AnimatableScaleValue;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.value.ScaleXY;

import java.io.IOException;

public class AnimatableTransformParser {

  private AnimatableTransformParser() {}

  public static AnimatableTransform parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    AnimatablePathValue anchorPoint = null;
    AnimatableValue<PointF, PointF> position = null;
    AnimatableScaleValue scale = null;
    AnimatableFloatValue rotation = null;
    AnimatableIntegerValue opacity = null;
    AnimatableFloatValue startOpacity = null;
    AnimatableFloatValue endOpacity = null;

    boolean isObject = reader.peek() == JsonToken.BEGIN_OBJECT;
    if (isObject) {
      reader.beginObject();
    }
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "a":
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
        case "p":
          position =
              AnimatablePathValueParser.parseSplitPath(reader, composition);
          break;
        case "s":
          scale = AnimatableValueParser.parseScale(reader, composition);
          break;
        case "rz":
          composition.addWarning("Lottie doesn't support 3D layers.");
        case "r":
          rotation = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case "o":
          opacity = AnimatableValueParser.parseInteger(reader, composition);
          break;
        case "so":
          startOpacity = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case "eo":
          endOpacity = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        default:
          reader.skipValue();
      }
    }
    if (isObject) {
      reader.endObject();
    }

    if (anchorPoint == null) {
      // Cameras don't have an anchor point property. Although we don't support them, at least
      // we won't crash.
      Log.w(L.TAG, "Layer has no transform property. You may be using an unsupported " +
          "layer type such as a camera.");
      anchorPoint = new AnimatablePathValue();
    }

    if (scale == null) {
      // Somehow some community animations don't have scale in the transform.
      scale = new AnimatableScaleValue(new ScaleXY(1f, 1f));
    }

    if (opacity == null) {
      // Repeaters have start/end opacity instead of opacity
      opacity = new AnimatableIntegerValue();
    }

    return new AnimatableTransform(
        anchorPoint, position, scale, rotation, opacity, startOpacity, endOpacity);
  }

}
