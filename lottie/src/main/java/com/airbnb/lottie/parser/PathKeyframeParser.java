package com.airbnb.lottie.parser;

import android.graphics.PointF;
import android.util.JsonReader;
import android.util.JsonToken;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.PathKeyframe;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;

public class PathKeyframeParser {

  private PathKeyframeParser() {}

  public static PathKeyframe parse(JsonReader reader, LottieComposition composition,
      ValueParser<PointF> valueParser) throws IOException {
    boolean animated = reader.peek() == JsonToken.BEGIN_OBJECT;
    Keyframe<PointF> keyframe = KeyframeParser.parse(
        reader, composition, Utils.dpScale(), valueParser, animated);

    return new PathKeyframe(composition, keyframe);
  }
}
