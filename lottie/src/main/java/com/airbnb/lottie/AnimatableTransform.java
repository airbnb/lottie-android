package com.airbnb.lottie;

import org.json.JSONException;
import org.json.JSONObject;

class AnimatableTransform {
  private AnimatablePathValue anchorPoint;
  private IAnimatablePathValue position;
  private AnimatableScaleValue scale;
  private AnimatableFloatValue rotation;
  private AnimatableIntegerValue opacity;

  AnimatableTransform(LottieComposition composition) {
    this.anchorPoint = new AnimatablePathValue();
    this.position = new AnimatablePathValue();
    this.scale = new AnimatableScaleValue(composition);
    this.rotation = new AnimatableFloatValue(composition, 0f);
    this.opacity = new AnimatableIntegerValue(composition, 255);
  }

  AnimatableTransform(JSONObject json, LottieComposition composition) throws JSONException {
    JSONObject anchorJson = json.optJSONObject("a");
    if (anchorJson != null) {
      anchorPoint = new AnimatablePathValue(anchorJson.get("k"), composition);
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
      scale = new AnimatableScaleValue(scaleJson, composition, false);
    } else {
      throwMissingTransform("scale");
    }

    JSONObject rotationJson = json.optJSONObject("r");
    if (rotationJson == null) {
      rotationJson = json.optJSONObject("rz");
    }
    if (rotationJson != null) {
      rotation = new AnimatableFloatValue(rotationJson, composition, false);
    } else {
      throwMissingTransform("rotation");
    }

    JSONObject opacityJson = json.optJSONObject("o");
    if (opacityJson != null) {
      opacity = new AnimatableIntegerValue(opacityJson, composition, false, true);
    } else {
      throwMissingTransform("opacity");
    }
  }

  private void throwMissingTransform(String missingProperty) {
    throw new IllegalArgumentException("Missing transform for " + missingProperty);
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
