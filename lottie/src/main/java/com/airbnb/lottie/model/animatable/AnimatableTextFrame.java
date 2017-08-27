package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.TextKeyframeAnimation;
import com.airbnb.lottie.model.DocumentData;

import org.json.JSONObject;

import java.util.List;

public class AnimatableTextFrame extends BaseAnimatableValue<DocumentData, DocumentData> {

  AnimatableTextFrame(List<Keyframe<DocumentData>> keyframes, DocumentData initialValue) {
    super(keyframes, initialValue);
  }

  @Override public TextKeyframeAnimation createAnimation() {
    return new TextKeyframeAnimation(keyframes);
  }

  public static final class Factory {
    private Factory() {
    }

    public static AnimatableTextFrame newInstance(JSONObject json, LottieComposition composition) {
      if (json != null && json.has("x")) {
        composition.addWarning("Lottie doesn't support expressions.");
      }
      AnimatableValueParser.Result<DocumentData> result = AnimatableValueParser
          .newInstance(json, 1, composition, AnimatableTextFrame.ValueFactory.INSTANCE)
          .parseJson();
      return new AnimatableTextFrame(result.keyframes, result.initialValue);
    }
  }

  private static class ValueFactory implements AnimatableValue.Factory<DocumentData> {
    private static final AnimatableTextFrame.ValueFactory INSTANCE =
        new AnimatableTextFrame.ValueFactory();

    private ValueFactory() {
    }

    @Override public DocumentData valueFromObject(Object object, float scale) {
      return DocumentData.Factory.newInstance((JSONObject) object);
    }
  }
}
