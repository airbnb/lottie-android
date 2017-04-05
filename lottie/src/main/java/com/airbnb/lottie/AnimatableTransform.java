package com.airbnb.lottie;

import android.graphics.PointF;
import android.util.Log;

import org.json.JSONObject;

import java.util.Collections;

class AnimatableTransform implements ModifierContent {
  private final AnimatablePathValue anchorPoint;
  private final AnimatableValue<PointF> position;
  private final AnimatableScaleValue scale;
  private final AnimatableFloatValue rotation;
  private final AnimatableIntegerValue opacity;

  private AnimatableTransform(AnimatablePathValue anchorPoint, AnimatableValue<PointF> position,
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

    static AnimatableTransform newInstance() {
      AnimatablePathValue anchorPoint = new AnimatablePathValue();
      AnimatableValue<PointF> position = new AnimatablePathValue();
      AnimatableScaleValue scale = AnimatableScaleValue.Factory.newInstance();
      AnimatableFloatValue rotation = AnimatableFloatValue.Factory.newInstance();
      AnimatableIntegerValue opacity = AnimatableIntegerValue.Factory.newInstance();
      return new AnimatableTransform(anchorPoint, position, scale, rotation, opacity);
    }

    static AnimatableTransform newInstance(JSONObject json, LottieComposition composition) {
      AnimatablePathValue anchorPoint = null;
      AnimatableValue<PointF> position = null;
      AnimatableScaleValue scale;
      AnimatableFloatValue rotation = null;
      AnimatableIntegerValue opacity;
      JSONObject anchorJson = json.optJSONObject("a");
      if (anchorJson != null) {
        anchorPoint = new AnimatablePathValue(anchorJson.opt("k"), composition);
      } else {
        // Cameras don't have an anchor point property. Although we don't support them, at least
        // we won't crash.
        Log.w(L.TAG, "Layer has no transform property. You may be using an unsupported " +
            "layer type such as a camera.");
        anchorPoint = new AnimatablePathValue();
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
        scale = AnimatableScaleValue.Factory.newInstance(scaleJson, composition);
      } else {
        // Somehow some community animations don't have scale in the transform.
        scale = new AnimatableScaleValue(Collections.<Keyframe<ScaleXY>>emptyList(), new ScaleXY());
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
        opacity = AnimatableIntegerValue.Factory.newInstance(opacityJson, composition);
      } else {
        // Somehow some community animations don't have opacity in the transform.
        opacity = new AnimatableIntegerValue(Collections.<Keyframe<Integer>>emptyList(), 100);
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

  AnimatableValue<PointF> getPosition() {
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
