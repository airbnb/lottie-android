package com.airbnb.lottie;

import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class AnimatablePathValue implements IAnimatablePathValue {

  static IAnimatablePathValue createAnimatablePathOrSplitDimensionPath(
      JSONObject json, LottieComposition composition) throws JSONException {
    if (json.has("k")) {
      return new AnimatablePathValue(json.get("k"), composition);
    } else {
      return new AnimatableSplitDimensionPathValue(
          new AnimatableFloatValue(json.getJSONObject("x"), composition),
          new AnimatableFloatValue(json.getJSONObject("y"), composition)
      );
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

  AnimatablePathValue(Object json, LottieComposition composition) throws JSONException {

    if (hasKeyframes(json)) {
      JSONArray jsonArray = (JSONArray) json;
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonKeyframe = jsonArray.getJSONObject(i);
        PathKeyframe keyframe = new PathKeyframe(jsonKeyframe, composition, this);
        keyframes.add(keyframe);
      }
      Keyframe.setEndFrames(keyframes);
    } else {
      initialPoint = JsonUtils.pointFromJsonArray((JSONArray) json, composition.getScale());
    }
  }

  private boolean hasKeyframes(Object json) throws JSONException {
    if (!(json instanceof JSONArray)) {
      return false;
    }

    Object firstObject = ((JSONArray) json).get(0);
    return firstObject instanceof JSONObject && ((JSONObject) firstObject).has("t");
  }

  @Override public PointF valueFromObject(Object object, float scale) throws JSONException {
    return JsonUtils.pointFromJsonArray((JSONArray) object, scale);
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
  public PointF getInitialPoint() {
    return initialPoint;
  }

  @Override
  public String toString() {
    return "initialPoint=" + initialPoint;
  }
}
