package com.airbnb.lottie.model.animatable;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ScaleKeyframeAnimation;
import com.airbnb.lottie.value.ScaleXY;

import java.io.IOException;
import java.util.List;

public class AnimatableScaleValue extends BaseAnimatableValue<ScaleXY, ScaleXY> {

  private AnimatableScaleValue() {
    this(new ScaleXY(1f, 1f));
  }

  AnimatableScaleValue(ScaleXY value) {
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
        JsonReader reader, LottieComposition composition) throws IOException {
      return new AnimatableScaleValue(
          AnimatableValueParser.newInstance(reader, 1, composition, ScaleXY.Factory.INSTANCE)
      );
    }

    static AnimatableScaleValue newInstance() {
      return new AnimatableScaleValue();
    }
  }
}
