package com.airbnb.lottie;

import org.json.JSONObject;

class AnimatableTransform implements ModifierContent {
  private final AnimatablePathValue anchorPoint;
  private final IAnimatablePathValue position;
  private final AnimatableScaleValue scale;
  private final AnimatableFloatValue rotation;
  private final AnimatableIntegerValue opacity;

  private AnimatableTransform(AnimatablePathValue anchorPoint, IAnimatablePathValue position,
      AnimatableScaleValue scale, AnimatableFloatValue rotation, AnimatableIntegerValue opacity) {
    this.anchorPoint = anchorPoint;
    this.position = position;
    this.scale = scale;
    this.rotation = rotation;
    this.opacity = opacity;
  }

  static class Factory {
    private Factory() {
    }

    static AnimatableTransform newInstance(LottieComposition composition) {
      AnimatablePathValue anchorPoint = new AnimatablePathValue();
      IAnimatablePathValue position = new AnimatablePathValue();
      AnimatableScaleValue scale = AnimatableScaleValue.Factory.newInstance(composition);
      AnimatableFloatValue rotation = AnimatableFloatValue.Factory.newInstance(composition, 0f);
      AnimatableIntegerValue opacity = AnimatableIntegerValue.Factory.newInstance(composition, 100);
      return new AnimatableTransform(anchorPoint, position, scale, rotation, opacity);
    }

    static AnimatableTransform newInstance(JSONObject json, LottieComposition composition) {
      AnimatablePathValue anchorPoint = null;
      IAnimatablePathValue position = null;
      AnimatableScaleValue scale = null;
      AnimatableFloatValue rotation = null;
      AnimatableIntegerValue opacity = null;
      JSONObject anchorJson = json.optJSONObject("a");
      if (anchorJson != null) {
        anchorPoint = new AnimatablePathValue(anchorJson.opt("k"), composition);
      } else {
        throwMissingTransform("anchor");
      }

      JSONObject positionJson = json.optJSONObject("p");
      if (positionJson != null) {
        position =
            AnimatablePathValue.createAnimatablePathOrSplitDimensionPath(positionJson, composition);
      } else {
        throwMissingTransform("position");
      }

      JSONObject scaleJson = json.optJSONObject("s");
      if (scaleJson != null) {
        scale = AnimatableScaleValue.Factory.newInstance(scaleJson, composition, false);
      } else {
        throwMissingTransform("scale");
      }

      JSONObject rotationJson = json.optJSONObject("r");
      if (rotationJson == null) {
        rotationJson = json.optJSONObject("rz");
      }
      if (rotationJson != null) {
        rotation = AnimatableFloatValue.Factory.newInstance(rotationJson, composition, false);
      } else {
        throwMissingTransform("rotation");
      }

      JSONObject opacityJson = json.optJSONObject("o");
      if (opacityJson != null) {
        opacity = AnimatableIntegerValue.Factory.newInstance(opacityJson, composition, false);
      } else {
        throwMissingTransform("opacity");
      }
      return new AnimatableTransform(anchorPoint, position, scale, rotation, opacity);
    }

    private static void throwMissingTransform(String missingProperty) {
      throw new IllegalArgumentException("Missing transform for " + missingProperty);
    }
  }

  AnimatablePathValue getAnchorPoint() {
    return anchorPoint;
  }

  IAnimatablePathValue getPosition() {
    return position;
  }

  AnimatableScaleValue getScale() {
    return scale;
  }

  AnimatableFloatValue getRotation() {
    return rotation;
  }

  AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  public TransformKeyframeAnimation createAnimation() {
    return new TransformKeyframeAnimation(this);
  }
}
