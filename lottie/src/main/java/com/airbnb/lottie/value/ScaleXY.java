package com.airbnb.lottie.value;

import android.util.JsonReader;
import android.util.JsonToken;

import com.airbnb.lottie.model.animatable.AnimatableValue;

import java.io.IOException;

public class ScaleXY {
  private final float scaleX;
  private final float scaleY;

  public ScaleXY(float sx, float sy) {
    this.scaleX = sx;
    this.scaleY = sy;
  }

  public ScaleXY() {
    this(1f, 1f);
  }

  public float getScaleX() {
    return scaleX;
  }

  public float getScaleY() {
    return scaleY;
  }

  @Override public String toString() {
    return getScaleX() + "x" + getScaleY();
  }

  public static class Factory implements AnimatableValue.Factory<ScaleXY> {
    public static final Factory INSTANCE = new Factory();

    private Factory() {
    }

    @Override public ScaleXY valueFromObject(JsonReader reader, float scale) throws IOException {
      boolean isArray = reader.peek() == JsonToken.BEGIN_ARRAY;
      if (isArray) {
        reader.beginArray();
      }
      float sx = (float) reader.nextDouble();
      float sy = (float) reader.nextDouble();
      while (reader.hasNext()) {
        reader.skipValue();
      }
      if (isArray) {
        reader.endArray();
      }
      return new ScaleXY(sx / 100f * scale, sy / 100f * scale);
    }
  }
}
