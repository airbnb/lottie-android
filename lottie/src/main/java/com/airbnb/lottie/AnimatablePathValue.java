package com.airbnb.lottie;

import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class AnimatablePathValue implements AnimatableValue<PointF> {
  static AnimatableValue<PointF> createAnimatablePathOrSplitDimensionPath(
      JSONObject json, LottieComposition composition) {
    if (json.has("k")) {
      return new AnimatablePathValue(json.opt("k"), composition);
    } else {
      return new AnimatableSplitDimensionPathValue(
          AnimatableFloatValue.Factory.newInstance(json.optJSONObject("x"), composition),
          AnimatableFloatValue.Factory.newInstance(json.optJSONObject("y"), composition));
    }
  }

  private final List<PathKeyframe> keyframes = new ArrayList<>();
  private PointF initialPoint;

  /**
   * Create a default static animatable path.
   */
  AnimatablePathValue() {
    this.initialPoint = new PointF(0, 0);
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
      initialPoint = JsonUtils.pointFromJsonArray((JSONArray) json, composition.getDpScale());
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
  public BaseKeyframeAnimation<?, PointF> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialPoint);
    }

    return new PathKeyframeAnimation(keyframes);
  }

  @Override
  public boolean hasAnimation() {
    return !keyframes.isEmpty();
  }

  @Override
  public String toString() {
    return "initialPoint=" + initialPoint;
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
