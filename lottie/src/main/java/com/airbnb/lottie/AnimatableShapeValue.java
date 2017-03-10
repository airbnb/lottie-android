package com.airbnb.lottie;

import android.graphics.Path;

import org.json.JSONObject;

import java.util.List;

class AnimatableShapeValue extends BaseAnimatableValue<ShapeData, Path> {
  private final Path convertTypePath = new Path();

  private AnimatableShapeValue(List<Keyframe<ShapeData>> keyframes, ShapeData initialValue) {
    super(keyframes, initialValue);
  }

  @Override public BaseKeyframeAnimation<?, Path> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(convertType(initialValue));
    } else {
      return new ShapeKeyframeAnimation(keyframes);
    }
  }

  @Override Path convertType(ShapeData shapeData) {
    convertTypePath.reset();
    MiscUtils.getPathFromData(shapeData, convertTypePath);
    return convertTypePath;
  }

  static final class Factory {
    private Factory() {
    }

    static AnimatableShapeValue newInstance(JSONObject json, LottieComposition composition) {
      AnimatableValueParser.Result<ShapeData> result = AnimatableValueParser
          .newInstance(json, composition.getDpScale(), composition, ShapeData.Factory.INSTANCE)
          .parseJson();
      return new AnimatableShapeValue(result.keyframes, result.initialValue);
    }
  }
}
