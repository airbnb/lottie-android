package com.airbnb.lottie;

import android.graphics.PointF;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Keyframe<T> {
  /**
   * Some animations get exported with insane cp values in the tens of thousands.
   * PathInterpolator fails to create the interpolator in those cases and hangs.
   * Clamping the cp helps prevent that.
   */
  private static final float MAX_CP_VALUE = 100;
  private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

  /**
   * The json doesn't include end frames. The data can be taken from the start frame of the next
   * keyframe though.
   */
  static void setEndFrames(List<? extends Keyframe<?>> keyframes) {
    int size = keyframes.size();
    for (int i = 0; i < size - 1; i++) {
      // In the json, the keyframes only contain their starting frame.
      keyframes.get(i).endFrame = keyframes.get(i + 1).startFrame;
    }
    Keyframe<?> lastKeyframe = keyframes.get(size - 1);
    if (lastKeyframe.startValue == null) {
      // The only purpose the last keyframe has is to provide the end frame of the previous
      // keyframe.
      //noinspection SuspiciousMethodCalls
      keyframes.remove(lastKeyframe);
    }
  }


  private final LottieComposition composition;
  @Nullable final T startValue;
  @Nullable final T endValue;
  @Nullable final Interpolator interpolator;
  @SuppressWarnings("WeakerAccess") final float startFrame;
  @SuppressWarnings("WeakerAccess") @Nullable Float endFrame;

  public Keyframe(LottieComposition composition, @Nullable T startValue, @Nullable T endValue,
      @Nullable Interpolator interpolator, float startFrame, @Nullable Float endFrame) {
    this.composition = composition;
    this.startValue = startValue;
    this.endValue = endValue;
    this.interpolator = interpolator;
    this.startFrame = startFrame;
    this.endFrame = endFrame;
  }

  @FloatRange(from = 0f, to = 1f)
  float getStartProgress() {
    return startFrame / composition.getDurationFrames();
  }

  @FloatRange(from = 0f, to = 1f)
  float getEndProgress() {
    //noinspection Range
    return endFrame == null ? 1f : endFrame / composition.getDurationFrames();
  }

  boolean isStatic() {
    return interpolator == null;
  }

  boolean containsProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    return progress >= getStartProgress() && progress <= getEndProgress();
  }

  @Override public String toString() {
    return "Keyframe{" + "startValue=" + startValue +
        ", endValue=" + endValue +
        ", startFrame=" + startFrame +
        ", endFrame=" + endFrame +
        ", interpolator=" + interpolator +
        '}';
  }

  static class Factory {
    private Factory() {
    }

    static <T> Keyframe<T> newInstance(JSONObject json, LottieComposition composition, float scale,
        AnimatableValue.Factory<T> valueFactory) {
      PointF cp1 = null;
      PointF cp2 = null;
      float startFrame = 0;
      T startValue = null;
      T endValue = null;
      Interpolator interpolator = null;

      if (json.has("t")) {
        startFrame = (float) json.optDouble("t", 0);
        Object startValueJson = json.opt("s");
        if (startValueJson != null) {
          startValue = valueFactory.valueFromObject(startValueJson, scale);
        }

        Object endValueJson = json.opt("e");
        if (endValueJson != null) {
          endValue = valueFactory.valueFromObject(endValueJson, scale);
        }

        JSONObject cp1Json = json.optJSONObject("o");
        JSONObject cp2Json = json.optJSONObject("i");
        if (cp1Json != null && cp2Json != null) {
          cp1 = JsonUtils.pointFromJsonObject(cp1Json, scale);
          cp2 = JsonUtils.pointFromJsonObject(cp2Json, scale);
        }

        boolean hold = json.optInt("h", 0) == 1;

        if (hold) {
          endValue = startValue;
          // TODO: create a HoldInterpolator so progress changes don't invalidate.
          interpolator = LINEAR_INTERPOLATOR;
        } else if (cp1 != null) {
          cp1.x = MiscUtils.clamp(cp1.x, -scale, scale);
          cp1.y = MiscUtils.clamp(cp1.y, -MAX_CP_VALUE, MAX_CP_VALUE);
          cp2.x = MiscUtils.clamp(cp2.x, -scale, scale);
          cp2.y = MiscUtils.clamp(cp2.y, -MAX_CP_VALUE, MAX_CP_VALUE);
          interpolator = PathInterpolatorCompat.create(
              cp1.x / scale, cp1.y / scale, cp2.x / scale, cp2.y / scale);
        } else {
          interpolator = LINEAR_INTERPOLATOR;
        }
      } else {
        startValue = valueFactory.valueFromObject(json, scale);
        endValue = startValue;
      }
      return new Keyframe<>(composition, startValue, endValue, interpolator, startFrame, null);
    }

    static <T> List<Keyframe<T>> parseKeyframes(JSONArray json, LottieComposition composition,
        float scale, AnimatableValue.Factory<T> valueFactory) {
      int length = json.length();
      if (length == 0) {
        return Collections.emptyList();
      }
      List<Keyframe<T>> keyframes = new ArrayList<>();
      for (int i = 0; i < length; i++) {
        keyframes.add(Keyframe.Factory.newInstance(json.optJSONObject(i), composition, scale,
            valueFactory));
      }

      setEndFrames(keyframes);
      return keyframes;
    }
  }
}
