package com.airbnb.lottie;

import android.graphics.Path;

import org.json.JSONObject;

class AnimatableShapeValue extends BaseAnimatableValue<ShapeData, Path> {
  private final Path convertTypePath = new Path();

  AnimatableShapeValue(JSONObject json, LottieComposition composition) {
    super(json, composition, true);
  }

  @Override public ShapeData valueFromObject(Object object, float scale) {
    return ShapeData.Factory.newInstance(object, scale);
  }

  @Override public BaseKeyframeAnimation<?, Path> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(convertType(initialValue));
    }

    return new ShapeKeyframeAnimation(keyframes);
  }

  @Override Path convertType(ShapeData shapeData) {
    convertTypePath.reset();
    MiscUtils.getPathFromData(shapeData, convertTypePath);
    return convertTypePath;
  }
}
