package com.airbnb.lottie.parser;


import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.animatable.AnimatableScaleValue;
import com.airbnb.lottie.model.animatable.AnimatableShapeValue;
import com.airbnb.lottie.model.animatable.AnimatableTextFrame;
import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.Keyframe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnimatableValueParser {
  private static final JsonReader.Options GRADIENT_NAMES = JsonReader.Options.of(
      "p",
      "k"
  );

  private AnimatableValueParser() {
  }

  public static AnimatableFloatValue parseFloat(
      JsonReader reader, LottieComposition composition) throws IOException {
    return parseFloat(reader, composition, true);
  }

  public static AnimatableFloatValue parseFloat(
      JsonReader reader, LottieComposition composition, boolean isDp) throws IOException {
    return new AnimatableFloatValue(
        parse(reader, isDp ? Utils.dpScale() : 1f, composition, FloatParser.INSTANCE));
  }

  static AnimatableIntegerValue parseInteger(
      JsonReader reader, LottieComposition composition) throws IOException {
    return new AnimatableIntegerValue(parse(reader, composition, IntegerParser.INSTANCE));
  }

  static AnimatablePointValue parsePoint(
      JsonReader reader, LottieComposition composition) throws IOException {
    return new AnimatablePointValue(KeyframesParser.parse(reader, composition, Utils.dpScale(), PointFParser.INSTANCE, true));
  }

  static AnimatableScaleValue parseScale(
      JsonReader reader, LottieComposition composition) throws IOException {
    return new AnimatableScaleValue(parse(reader, composition, ScaleXYParser.INSTANCE));
  }

  static AnimatableShapeValue parseShapeData(
      JsonReader reader, LottieComposition composition) throws IOException {
    return new AnimatableShapeValue(
        parse(reader, Utils.dpScale(), composition, ShapeDataParser.INSTANCE));
  }

  static AnimatableTextFrame parseDocumentData(
      JsonReader reader, LottieComposition composition) throws IOException {
    return new AnimatableTextFrame(parse(reader, Utils.dpScale(), composition, DocumentDataParser.INSTANCE));
  }

  static AnimatableColorValue parseColor(
      JsonReader reader, LottieComposition composition) throws IOException {
    return new AnimatableColorValue(parse(reader, composition, ColorParser.INSTANCE));
  }

  static AnimatableGradientColorValue parseGradientColor(
      JsonReader reader, LottieComposition composition) throws IOException {
    int points = -1;
    AnimatableGradientColorValue color = null;
    List<Keyframe<List<Float>>> rawKeyframes = null;
    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(GRADIENT_NAMES)) {
        case 0:
          points = reader.nextInt();
          break;
        case 1:
          if (points != -1) {
            color = parseGradientColor(reader, composition, points);
          } else {
            rawKeyframes = parse(reader, composition, (r, scale) -> GradientColorParser.readFloatArray(r));
          }
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    reader.endObject();
    if (color == null && rawKeyframes != null) {
      GradientColorParser parser = new GradientColorParser(points);
      List<Keyframe<GradientColor>> keyframes = new ArrayList<>(rawKeyframes.size());
      for (int i = 0; i < rawKeyframes.size(); i++) {
        Keyframe<List<Float>> rawKeyframe = rawKeyframes.get(i);
        if (rawKeyframe.isStatic()) {
          GradientColor value = rawKeyframe.startValue != null ? parser.parse(rawKeyframe.startValue) : null;
          keyframes.add(new Keyframe<>(value));
        } else {
          GradientColor startValue = rawKeyframe.startValue != null ? parser.parse(rawKeyframe.startValue) : null;
          GradientColor endValue = rawKeyframe.endValue == rawKeyframe.startValue
              ? startValue
              : (rawKeyframe.endValue != null ? parser.parse(rawKeyframe.endValue) : null);
          keyframes.add(new Keyframe<>(
              composition, startValue, endValue, rawKeyframe.interpolator, rawKeyframe.startFrame, rawKeyframe.endFrame));
        }
      }
      color = new AnimatableGradientColorValue(keyframes);
    }
    return color;
  }

  static AnimatableGradientColorValue parseGradientColor(
      JsonReader reader, LottieComposition composition, int points) throws IOException {
    AnimatableGradientColorValue animatableGradientColorValue = new AnimatableGradientColorValue(
        parse(reader, composition, new GradientColorParser(points)));
    return animatableGradientColorValue;
  }

  /**
   * Will return null if the animation can't be played such as if it has expressions.
   */
  private static <T> List<Keyframe<T>> parse(JsonReader reader,
      LottieComposition composition, ValueParser<T> valueParser) throws IOException {
    return KeyframesParser.parse(reader, composition, 1, valueParser, false);
  }

  /**
   * Will return null if the animation can't be played such as if it has expressions.
   */
  private static <T> List<Keyframe<T>> parse(
      JsonReader reader, float scale, LottieComposition composition, ValueParser<T> valueParser) throws IOException {
    return KeyframesParser.parse(reader, composition, scale, valueParser, false);
  }
}
