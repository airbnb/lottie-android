package com.airbnb.lottie.model.animatable;

import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ModifierContent;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;
import com.airbnb.lottie.value.ScaleXY;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.layer.BaseLayer;

import org.json.JSONObject;

import java.util.Collections;

public class AnimatableTransform implements ModifierContent, ContentModel {
  private final AnimatablePathValue anchorPoint;
  private final AnimatableValue<PointF, PointF> position;
  private final AnimatableScaleValue scale;
  private final AnimatableFloatValue rotation;
  private final AnimatableIntegerValue opacity;

  // Used for repeaters
  @Nullable private final AnimatableFloatValue startOpacity;
  @Nullable private final AnimatableFloatValue endOpacity;

  private AnimatableTransform(AnimatablePathValue anchorPoint,
      AnimatableValue<PointF, PointF> position, AnimatableScaleValue scale,
      AnimatableFloatValue rotation, AnimatableIntegerValue opacity,
      @Nullable AnimatableFloatValue startOpacity, @Nullable AnimatableFloatValue endOpacity) {
    this.anchorPoint = anchorPoint;
    this.position = position;
    this.scale = scale;
    this.rotation = rotation;
    this.opacity = opacity;
    this.startOpacity = startOpacity;
    this.endOpacity = endOpacity;
  }

  public AnimatablePathValue getAnchorPoint() {
    return anchorPoint;
  }

  public AnimatableValue<PointF, PointF> getPosition() {
    return position;
  }

  public AnimatableScaleValue getScale() {
    return scale;
  }

  public AnimatableFloatValue getRotation() {
    return rotation;
  }

  public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  @Nullable public AnimatableFloatValue getStartOpacity() {
    return startOpacity;
  }

  @Nullable public AnimatableFloatValue getEndOpacity() {
    return endOpacity;
  }

  public TransformKeyframeAnimation createAnimation() {
    return new TransformKeyframeAnimation(this);
  }

  @Nullable @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return null;
  }

  public static class Factory {
    private Factory() {
    }

    public static AnimatableTransform newInstance() {
      AnimatablePathValue anchorPoint = new AnimatablePathValue();
      AnimatableValue<PointF, PointF> position = new AnimatablePathValue();
      AnimatableScaleValue scale = AnimatableScaleValue.Factory.newInstance();
      AnimatableFloatValue rotation = AnimatableFloatValue.Factory.newInstance();
      AnimatableIntegerValue opacity = AnimatableIntegerValue.Factory.newInstance();
      AnimatableFloatValue startOpacity = AnimatableFloatValue.Factory.newInstance();
      AnimatableFloatValue endOpacity = AnimatableFloatValue.Factory.newInstance();
      return new AnimatableTransform(anchorPoint, position, scale, rotation, opacity, startOpacity,
          endOpacity);
    }

    public static AnimatableTransform newInstance(JSONObject json, LottieComposition composition) {
      AnimatablePathValue anchorPoint;
      AnimatableValue<PointF, PointF> position = null;
      AnimatableScaleValue scale;
      AnimatableFloatValue rotation = null;
      AnimatableIntegerValue opacity;
      AnimatableFloatValue startOpacity = null;
      AnimatableFloatValue endOpacity = null;
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
        // Repeaters have start/end opacity instead of opacity
        opacity = new AnimatableIntegerValue(Collections.<Keyframe<Integer>>emptyList(), 100);
      }

      JSONObject startOpacityJson = json.optJSONObject("so");
      if (startOpacityJson != null) {
        startOpacity =
            AnimatableFloatValue.Factory.newInstance(startOpacityJson, composition, false);
      }

      JSONObject endOpacityJson = json.optJSONObject("eo");
      if (endOpacityJson != null) {
        endOpacity =
            AnimatableFloatValue.Factory.newInstance(endOpacityJson, composition, false);
      }

      return new AnimatableTransform(
          anchorPoint, position, scale, rotation, opacity, startOpacity, endOpacity);
    }

    private static void throwMissingTransform(String missingProperty) {
      throw new IllegalArgumentException("Missing transform for " + missingProperty);
    }
  }
}
