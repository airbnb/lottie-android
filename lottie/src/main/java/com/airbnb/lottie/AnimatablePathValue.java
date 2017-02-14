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
  private final LottieComposition composition;

  /**
   * Create a default static animatable path.
   */
  AnimatablePathValue(LottieComposition composition) {
    this.composition = composition;
    this.initialPoint = new PointF(0, 0);
  }

  AnimatablePathValue(Object json, LottieComposition composition) throws JSONException {
    this.composition = composition;

    if (hasKeyframes(json)) {
      buildAnimationForKeyframes((JSONArray) json);
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

  private void buildAnimationForKeyframes(JSONArray jsonKeyframes) throws JSONException {
    for (int i = 0; i < jsonKeyframes.length(); i++) {
      JSONObject jsonKeyframe = jsonKeyframes.getJSONObject(i);
      PathKeyframe keyframe = new PathKeyframe(jsonKeyframe, composition, this);
      keyframes.add(keyframe);
    }
    Keyframe.setEndFrames(keyframes);
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
