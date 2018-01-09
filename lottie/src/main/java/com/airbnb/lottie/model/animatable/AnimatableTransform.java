package com.airbnb.lottie.model.animatable;

import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ModifierContent;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.value.ScaleXY;

import java.io.IOException;

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

    public static AnimatableTransform newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      AnimatablePathValue anchorPoint = null;
      AnimatableValue<PointF, PointF> position = null;
      AnimatableScaleValue scale = null;
      AnimatableFloatValue rotation = null;
      AnimatableIntegerValue opacity = null;
      AnimatableFloatValue startOpacity = null;
      AnimatableFloatValue endOpacity = null;

      boolean isObject = reader.peek() == JsonToken.BEGIN_OBJECT;
      if (isObject) {
        reader.beginObject();
      }
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "a":
            reader.beginObject();
            while (reader.hasNext()) {
              if (reader.nextName().equals("k")) {
                anchorPoint = new AnimatablePathValue(reader, composition);
              } else {
                reader.skipValue();
              }
            }
            reader.endObject();
            break;
          case "p":
            position =
                AnimatablePathValue.createAnimatablePathOrSplitDimensionPath(reader, composition);
            break;
          case "s":
            scale = AnimatableScaleValue.Factory.newInstance(reader, composition);
            break;
          case "rz":
            composition.addWarning("Lottie doesn't support 3D layers.");
          case "r":
            rotation = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
            break;
          case "o":
            opacity = AnimatableIntegerValue.Factory.newInstance(reader, composition);
            break;
          case "so":
            startOpacity =
                AnimatableFloatValue.Factory.newInstance(reader, composition, false);
            break;
          case "eo":
            endOpacity =
                AnimatableFloatValue.Factory.newInstance(reader, composition, false);
            break;
          default:
            reader.skipValue();
        }
      }
      if (isObject) {
        reader.endObject();
      }

      if (anchorPoint == null) {
        // Cameras don't have an anchor point property. Although we don't support them, at least
        // we won't crash.
        Log.w(L.TAG, "Layer has no transform property. You may be using an unsupported " +
            "layer type such as a camera.");
        anchorPoint = new AnimatablePathValue();
      }

      if (scale == null) {
        // Somehow some community animations don't have scale in the transform.
        scale = new AnimatableScaleValue(new ScaleXY(1f, 1f));
      }

      if (opacity == null) {
        // Repeaters have start/end opacity instead of opacity
        opacity = new AnimatableIntegerValue(100);
      }

      return new AnimatableTransform(
          anchorPoint, position, scale, rotation, opacity, startOpacity, endOpacity);
    }
  }
}
