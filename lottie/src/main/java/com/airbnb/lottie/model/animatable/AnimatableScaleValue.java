package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ScaleKeyframeAnimation;
import com.airbnb.lottie.value.ScaleXY;

import org.json.JSONObject;

import java.util.List;

public class AnimatableScaleValue extends BaseAnimatableValue<ScaleXY, ScaleXY> {

  private AnimatableScaleValue() {
    this(new ScaleXY(1f, 1f));
  }

  private AnimatableScaleValue(ScaleXY value) {
    super(value);
  }

  AnimatableScaleValue(List<Keyframe<ScaleXY>> keyframes) {
    super(keyframes);
  }

  @Override public BaseKeyframeAnimation<ScaleXY, ScaleXY> createAnimation() {
    return new ScaleKeyframeAnimation(keyframes);
  }

  static final class Factory {
    private Factory() {
    }

    static AnimatableScaleValue newInstance(
        JSONObject json, LottieComposition composition) {
      return new AnimatableScaleValue(
          AnimatableValueParser.newInstance(json, 1, composition, ScaleXY.Factory.INSTANCE)
      );
    }

    static AnimatableScaleValue newInstance() {
      return new AnimatableScaleValue();
    }
  }
}
