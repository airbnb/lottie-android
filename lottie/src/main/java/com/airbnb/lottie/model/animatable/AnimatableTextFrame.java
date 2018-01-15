package com.airbnb.lottie.model.animatable;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.TextKeyframeAnimation;
import com.airbnb.lottie.model.DocumentData;
import com.airbnb.lottie.parser.DocumentDataParser;

import java.io.IOException;
import java.util.List;

public class AnimatableTextFrame extends BaseAnimatableValue<DocumentData, DocumentData> {

  AnimatableTextFrame(List<Keyframe<DocumentData>> keyframes) {
    super(keyframes);
  }

  @Override public TextKeyframeAnimation createAnimation() {
    return new TextKeyframeAnimation(keyframes);
  }

  public static final class Factory {
    private Factory() {
    }

    public static AnimatableTextFrame newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      return new AnimatableTextFrame(
          AnimatableValueParser
              .newInstance(reader, 1, composition, AnimatableTextFrame.ValueFactory.INSTANCE));
    }
  }

  private static class ValueFactory implements AnimatableValue.Factory<DocumentData> {
    private static final AnimatableTextFrame.ValueFactory INSTANCE =
        new AnimatableTextFrame.ValueFactory();

    private ValueFactory() {
    }

    @Override
    public DocumentData valueFromObject(JsonReader reader, float scale) throws IOException {
      return DocumentDataParser.parse(reader);
    }
  }
}
