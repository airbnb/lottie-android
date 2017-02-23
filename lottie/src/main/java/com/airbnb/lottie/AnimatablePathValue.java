package com.airbnb.lottie;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class AnimatablePathValue implements IAnimatablePathValue {
  static IAnimatablePathValue createAnimatablePathOrSplitDimensionPath(
          JSONObject json, LottieComposition composition) {
    if (json.length() == 2) {
      MagicPointFF magicPointF = null;
      String exp = null;
      if (json.has("x")) {
        exp = json.optString("x", null);
        magicPointF = ExpressionFactory.matcher(exp);
      }
      if (json.has("k")) {
        if (magicPointF != null)
          return new AnimatablePathValue(json.opt("k"), composition, magicPointF, exp);
        else
          return new AnimatablePathValue(json.opt("k"), composition);
      }

    }

    if (json.has("k")) {
      return new AnimatablePathValue(json.opt("k"), composition);
    } else {
      return new AnimatableSplitDimensionPathValue(
              new AnimatableFloatValue(json.optJSONObject("x"), composition),
              new AnimatableFloatValue(json.optJSONObject("y"), composition));
    }
  }

  private final List<Keyframe<CPointF>> keyframes = new ArrayList<>();
  private CPointF initialPoint;

  /**
   * Create a default static animatable path.
   */
  AnimatablePathValue() {
    this.initialPoint = new CPointF(0, 0);
  }

  AnimatablePathValue(Object json, LottieComposition composition) {
    if (hasKeyframes(json)) {
      JSONArray jsonArray = (JSONArray) json;
      int length = jsonArray.length();
      for (int i = 0; i < length; i++) {
        JSONObject jsonKeyframe = jsonArray.optJSONObject(i);
        PathKeyframe keyframe = PathKeyframe.Factory.newInstance(jsonKeyframe, composition, this);
        keyframes.add(keyframe);
      }
      Keyframe.setEndFrames(keyframes);
    } else {
      initialPoint = JsonUtils.pointFromJsonArray((JSONArray) json, composition.getScale());
    }
  }

  AnimatablePathValue(Object json, LottieComposition composition, MagicPointFF magicPointF, String exp) {

    magicPointF.setExpression(exp);

    if (hasKeyframes(json)) {
      JSONArray jsonArray = (JSONArray) json;
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonKeyframe = jsonArray.optJSONObject(i);
        DynamicPathKeyframe keyframe =  DynamicPathKeyframe.Factory.newInstance(jsonKeyframe, composition, this, magicPointF);
        keyframes.add(keyframe);
      }
      Keyframe.setEndFrames(keyframes);
    } else {
      initialPoint = JsonUtils.pointFromJsonArray((JSONArray) json, composition.getScale());
      magicPointF.init(initialPoint, composition);
      initialPoint = magicPointF;
    }
  }



  private boolean hasKeyframes(Object json) {
    if (!(json instanceof JSONArray)) {
      return false;
    }

    Object firstObject = ((JSONArray) json).opt(0);
    return firstObject instanceof JSONObject && ((JSONObject) firstObject).has("t");
  }

  @Override public CPointF valueFromObject(Object object, float scale) {
    return JsonUtils.pointFromJsonArray((JSONArray) object, scale);
  }

  @Override
  public BaseKeyframeAnimation<?, CPointF> createAnimation() {
    if (initialPoint instanceof MagicPointFF)
      return new DynamicKeyframeAnimation<>(initialPoint);


    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialPoint);
    }

    if(keyframes.get(0) instanceof DynamicPathKeyframe)
      return  new DynamicPathKeyframeAnimation(keyframes);

    return new PathKeyframeAnimation(keyframes);
  }

  @Override
  public boolean hasAnimation() {
    return !keyframes.isEmpty();
  }

  @Override
  public CPointF getInitialPoint() {
    return initialPoint;
  }

  @Override
  public String toString() {
    return "initialPoint=" + initialPoint;
  }
}