package com.airbnb.lottie.model.animatable;

import android.graphics.Path;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ShapeKeyframeAnimation;
import com.airbnb.lottie.model.content.ShapeData;

import org.json.JSONObject;

import java.util.List;

public class AnimatableShapeValue extends BaseAnimatableValue<ShapeData, Path> {

  private AnimatableShapeValue(List<Keyframe<ShapeData>> keyframes) {
    super(keyframes);
  }

  @Override public BaseKeyframeAnimation<ShapeData, Path> createAnimation() {
    return new ShapeKeyframeAnimation(keyframes);
  }

  public static final class Factory {
    private Factory() {
    }

    public static AnimatableShapeValue newInstance(JSONObject json, LottieComposition composition) {
      return new AnimatableShapeValue(
          AnimatableValueParser
              .newInstance(json, composition.getDpScale(), composition, ShapeData.Factory.INSTANCE)
      );
    }
  }
}
