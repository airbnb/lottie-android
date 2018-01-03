package com.airbnb.lottie.model.animatable;

import android.graphics.PointF;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.PointKeyframeAnimation;
import com.airbnb.lottie.model.PointFFactory;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.util.List;

public class AnimatablePointValue extends BaseAnimatableValue<PointF, PointF> {
  private AnimatablePointValue(List<Keyframe<PointF>> keyframes) {
    super(keyframes);
  }

  @Override public BaseKeyframeAnimation<PointF, PointF> createAnimation() {
    return new PointKeyframeAnimation(keyframes);
  }

  public static final class Factory {
    private Factory() {
    }

    public static AnimatablePointValue newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      return new AnimatablePointValue(AnimatableValueParser
              .newInstance(reader, Utils.dpScale(), composition, PointFFactory.INSTANCE)
      );
    }
  }
}
