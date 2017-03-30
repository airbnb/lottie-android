package com.airbnb.lottie;

import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

class AnimatableGradientColorValue extends BaseAnimatableValue<GradientColor, GradientColor> {
  private AnimatableGradientColorValue(
      List<Keyframe<GradientColor>> keyframes, GradientColor initialValue) {
    super(keyframes, initialValue);
  }

  @Override public KeyframeAnimation<GradientColor> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }
    return new GradientColorKeyframeAnimation(keyframes);
  }

  static final class Factory {
    private Factory() {
    }

    static AnimatableGradientColorValue newInstance(
        JSONObject json, LottieComposition composition) {
      AnimatableValueParser.Result<GradientColor> result = AnimatableValueParser
          .newInstance(json, 1, composition, ValueFactory.INSTANCE)
          .parseJson();
      GradientColor initialValue = result.initialValue;
      return new AnimatableGradientColorValue(result.keyframes, initialValue);
    }
  }

  private static class ValueFactory implements AnimatableValue.Factory<GradientColor> {
    private static final ValueFactory INSTANCE = new ValueFactory();

    private ValueFactory() {
    }

    @Override public GradientColor valueFromObject(Object object, float scale) {
      JSONArray array = (JSONArray) object;
      int size = array.length() / 4;
      float[] positions = new float[size];
      int[] colors = new int[size];
      GradientColor gradientColor = new GradientColor(positions, colors);
      int r = 0;
      int g = 0;
      if (array.length() != size * 4) {
        Log.w(L.TAG, "Unexpected gradient length: " + array.length() +
            ". Expected " + (size * 4) + ". This may affect the appearance of the gradient. " +
            "Make sure to save your After Effects file before exporting an animation with " +
            "gradients.");
      }
      for (int i = 0; i < size * 4; i++) {
        int colorIndex = i / 4;
        double value = array.optDouble(i);
        switch (i % 4) {
          case 0:
            // position
            positions[colorIndex] = (float) value;
            break;
          case 1:
            r = (int) (value * 255);
            break;
          case 2:
            g = (int) (value * 255);
            break;
          case 3:
            int b = (int) (value * 255);
            colors[colorIndex] = Color.argb(255, r, g, b);
            break;
        }
      }
      return gradientColor;
    }
  }
}
