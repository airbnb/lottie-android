package com.airbnb.lottie;

import org.json.JSONObject;

import java.util.List;

class AnimatableTextFrame extends BaseAnimatableValue<DocumentData, DocumentData> {

  AnimatableTextFrame(List<Keyframe<DocumentData>> keyframes, DocumentData initialValue) {
    super(keyframes, initialValue);
  }

  @Override public TextKeyframeAnimation createAnimation() {
    return new TextKeyframeAnimation(keyframes);
  }

  static final class Factory {
    private Factory() {
    }

    static AnimatableTextFrame newInstance(
        JSONObject json, LottieComposition composition) {
      if (json.has("x")) {
        composition.addWarning("Lottie doesn't support expressions.");
      }
      AnimatableValueParser.Result<DocumentData> result = AnimatableValueParser
          .newInstance(json, 1, composition, AnimatableTextFrame.ValueFactory.INSTANCE)
          .parseJson();
      return new AnimatableTextFrame(result.keyframes, result.initialValue);
    }
  }

  private static class ValueFactory implements AnimatableValue.Factory<DocumentData> {
    private static final AnimatableTextFrame.ValueFactory
        INSTANCE = new AnimatableTextFrame.ValueFactory();

    private ValueFactory() {
    }

    @Override public DocumentData valueFromObject(Object object, float scale) {
      return DocumentData.Factory.newInstance((JSONObject) object);
    }
  }
}
