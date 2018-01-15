package com.airbnb.lottie.model.animatable;

import android.graphics.PointF;
import android.util.JsonReader;
import android.util.JsonToken;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.PathKeyframe;
import com.airbnb.lottie.animation.keyframe.PathKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.PointKeyframeAnimation;
import com.airbnb.lottie.parser.AnimatableValueParser;
import com.airbnb.lottie.parser.KeyframesParser;
import com.airbnb.lottie.parser.PathParser;
import com.airbnb.lottie.utils.JsonUtils;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnimatablePathValue implements AnimatableValue<PointF, PointF> {
  public static AnimatableValue<PointF, PointF> createAnimatablePathOrSplitDimensionPath(
      JsonReader reader, LottieComposition composition) throws IOException {

    AnimatablePathValue pathAnimation = null;
    AnimatableFloatValue xAnimation = null;
    AnimatableFloatValue yAnimation = null;

    boolean hasExpressions = false;

    reader.beginObject();
    while (reader.peek() != JsonToken.END_OBJECT) {
      switch (reader.nextName()) {
        case "k":
          pathAnimation = new AnimatablePathValue(reader, composition);
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

  private final List<Keyframe<PointF>> keyframes = new ArrayList<>();

  /**
   * Create a default static animatable path.
   */
  AnimatablePathValue() {
    keyframes.add(new Keyframe<>(new PointF(0, 0)));
  }

  AnimatablePathValue(JsonReader reader, LottieComposition composition) throws IOException {
    if (reader.peek() == JsonToken.BEGIN_ARRAY) {
      reader.beginArray();
      while (reader.hasNext()) {
        PathKeyframe keyframe =
            PathKeyframe.Factory.newInstance(reader, composition, PathParser.INSTANCE);
        keyframes.add(keyframe);
      }
      reader.endArray();
      KeyframesParser.setEndFrames(keyframes);
    } else {
      keyframes.add(new Keyframe<>(JsonUtils.jsonToPoint(reader, Utils.dpScale())));
    }
  }

  @Override
  public BaseKeyframeAnimation<PointF, PointF> createAnimation() {
    if (keyframes.get(0).isStatic()) {
      return new PointKeyframeAnimation(keyframes);
    }
    return new PathKeyframeAnimation(keyframes);
  }
}
