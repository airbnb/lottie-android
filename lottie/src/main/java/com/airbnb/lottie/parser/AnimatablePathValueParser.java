package com.airbnb.lottie.parser;

import android.graphics.PointF;
import android.util.JsonReader;
import android.util.JsonToken;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatablePathValue;
import com.airbnb.lottie.model.animatable.AnimatableSplitDimensionPathValue;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnimatablePathValueParser {

  private AnimatablePathValueParser() {}

  public static AnimatablePathValue parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    List<Keyframe<PointF>> keyframes = new ArrayList<>();
    if (reader.peek() == JsonToken.BEGIN_ARRAY) {
      reader.beginArray();
      while (reader.hasNext()) {
        keyframes.add(PathKeyframeParser.parse(reader, composition));
      }
      reader.endArray();
      KeyframesParser.setEndFrames(keyframes);
    } else {
      keyframes.add(new Keyframe<>(JsonUtils.jsonToPoint(reader, Utils.dpScale())));
    }
    return new AnimatablePathValue(keyframes);
  }

  /**
   * Returns either an {@link AnimatablePathValue} or an {@link AnimatableSplitDimensionPathValue}.
   */
  static AnimatableValue<PointF, PointF> parseSplitPath(
      JsonReader reader, LottieComposition composition) throws IOException {

    AnimatablePathValue pathAnimation = null;
    AnimatableFloatValue xAnimation = null;
    AnimatableFloatValue yAnimation = null;

    boolean hasExpressions = false;

    reader.beginObject();
    while (reader.peek() != JsonToken.END_OBJECT) {
      switch (reader.nextName()) {
        case "k":
          pathAnimation = AnimatablePathValueParser.parse(reader, composition);
          break;
        case "x":
          if (reader.peek() == JsonToken.STRING) {
            hasExpressions = true;
            reader.skipValue();
          } else {
            xAnimation = AnimatableValueParser.parseFloat(reader, composition);
          }
          break;
        case "y":
          if (reader.peek() == JsonToken.STRING) {
            hasExpressions = true;
            reader.skipValue();
          } else {
            yAnimation = AnimatableValueParser.parseFloat(reader, composition);
          }
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();

    if (hasExpressions) {
      composition.addWarning("Lottie doesn't support expressions.");
    }

    if (pathAnimation != null) {
      return pathAnimation;
    }
    return new AnimatableSplitDimensionPathValue(xAnimation, yAnimation);
  }

}
