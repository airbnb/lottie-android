package com.airbnb.lottie.model;

import android.graphics.Color;

import com.airbnb.lottie.model.animatable.AnimatableValue;

import org.json.JSONArray;

public class ColorFactory implements AnimatableValue.Factory<Integer> {
  public static final ColorFactory INSTANCE = new ColorFactory();

  @Override public Integer valueFromObject(Object object) {
    JSONArray colorArray = (JSONArray) object;
    if (colorArray.length() == 4) {
      boolean shouldUse255 = true;
      for (int i = 0; i < colorArray.length(); i++) {
        double colorChannel = colorArray.optDouble(i);
        if (colorChannel > 1f) {
          shouldUse255 = false;
        }
      }

      float multiplier = shouldUse255 ? 255f : 1f;
      return Color.argb(
          (int) (colorArray.optDouble(3) * multiplier),
          (int) (colorArray.optDouble(0) * multiplier),
          (int) (colorArray.optDouble(1) * multiplier),
          (int) (colorArray.optDouble(2) * multiplier));
    }
    return Color.BLACK;
  }
}
