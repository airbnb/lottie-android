package com.airbnb.lottie.model.animatable;

import android.graphics.PointF;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.PathKeyframe;
import com.airbnb.lottie.animation.keyframe.PointKeyframeAnimation;
import com.airbnb.lottie.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AnimatablePathValue implements AnimatableValue<PointF, PointF> {
  public static AnimatableValue<PointF, PointF> createAnimatablePathOrSplitDimensionPath(
      JSONObject json, LottieComposition composition) {
    if (json.has("k")) {
      return new AnimatablePathValue(json.opt("k"), composition);
    } else {
      return new AnimatableSplitDimensionPathValue(
          AnimatableFloatValue.Factory.newInstance(json.optJSONObject("x"), composition),
          AnimatableFloatValue.Factory.newInstance(json.optJSONObject("y"), composition));
    }
  }

  private final List<Keyframe<PointF>> keyframes = new ArrayList<>();

  /**
   * Create a default static animatable path.
   */
  AnimatablePathValue() {
    keyframes.add(new Keyframe<>(new PointF(0, 0)));
  }

  AnimatablePathValue(Object json, LottieComposition composition) {
    if (hasKeyframes(json)) {
      JSONArray jsonArray = (JSONArray) json;
      int length = jsonArray.length();
      for (int i = 0; i < length; i++) {
        JSONObject jsonKeyframe = jsonArray.optJSONObject(i);
        PathKeyframe keyframe = PathKeyframe.Factory.newInstance(jsonKeyframe, composition,
            ValueFactory.INSTANCE);
        keyframes.add(keyframe);
      }
      Keyframe.setEndFrames(keyframes);
    } else {
      keyframes.add(
          new Keyframe<>(JsonUtils.pointFromJsonArray((JSONArray) json, composition.getDpScale())));
    }
  }



  private boolean hasKeyframes(Object json) {
    if (!(json instanceof JSONArray)) {
      return false;
    }

    Object firstObject = ((JSONArray) json).opt(0);
    return firstObject instanceof JSONObject && ((JSONObject) firstObject).has("t");
  }

  @Override
  public BaseKeyframeAnimation<PointF, PointF> createAnimation() {
    return new PointKeyframeAnimation(keyframes);
  }

  private static class ValueFactory implements AnimatableValue.Factory<PointF> {
    private static final Factory<PointF> INSTANCE = new ValueFactory();

    private ValueFactory() {
    }

    @Override public PointF valueFromObject(Object object, float scale) {
      return JsonUtils.pointFromJsonArray((JSONArray) object, scale);
    }
  }
}
