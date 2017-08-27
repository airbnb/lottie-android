package com.airbnb.lottie.model.animatable;

import android.graphics.PointF;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.KeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.PointKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.model.PointFFactory;

import org.json.JSONObject;

import java.util.List;

public class AnimatablePointValue extends BaseAnimatableValue<PointF, PointF> {
  private AnimatablePointValue(List<Keyframe<PointF>> keyframes, PointF initialValue) {
    super(keyframes, initialValue);
  }

  @Override public KeyframeAnimation<PointF> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    } else {
      return new PointKeyframeAnimation(keyframes);
    }
  }

  public static final class Factory {
    private Factory() {
    }

    public static AnimatablePointValue newInstance(JSONObject json, LottieComposition composition) {
      AnimatableValueParser.Result<PointF> result = AnimatableValueParser
          .newInstance(json, composition.getDpScale(), composition, PointFFactory.INSTANCE)
          .parseJson();
      return new AnimatablePointValue(result.keyframes, result.initialValue);
    }
  }
}
