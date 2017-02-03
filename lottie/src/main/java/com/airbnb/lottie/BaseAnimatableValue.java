package com.airbnb.lottie;

import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class BaseAnimatableValue<V, O> implements AnimatableValue {
  final List<V> keyValues = new ArrayList<>();
  final List<Float> keyTimes = new ArrayList<>();
  final List<Interpolator> interpolators = new ArrayList<>();
  long delay;
  long duration;
  final LottieComposition composition;
  private final boolean isDp;

  private long startFrame;
  private long durationFrames;
  private final int frameRate;

  V initialValue;

  /** Create a default static animatable path. */
  BaseAnimatableValue(LottieComposition composition) {
    this.composition = composition;
    isDp = false;
    frameRate = 0;
  }

  BaseAnimatableValue(@Nullable JSONObject json, int frameRate, LottieComposition composition,
      boolean isDp) {
    this.frameRate = frameRate;
    this.composition = composition;
    this.isDp = isDp;
    if (json != null) {
      init(json);
    }
  }

  void init(JSONObject json) {
    try {
      Object value = json.get("k");

      if (value instanceof JSONArray &&
          ((JSONArray) value).get(0) instanceof JSONObject &&
          ((JSONArray) value).getJSONObject(0).has("t")) {
        buildAnimationForKeyframes((JSONArray) value);
      } else {
        initialValue = valueFromObject(value, getScale());
      }
    } catch (JSONException e) {
      throw new IllegalStateException("Unable to parse json " + json, e);
    }
  }

  @SuppressWarnings("Duplicates")
  private void buildAnimationForKeyframes(JSONArray keyframes) {
    try {
      for (int i = 0; i < keyframes.length(); i++) {
        JSONObject kf = keyframes.getJSONObject(i);
        if (kf.has("t")) {
          startFrame = kf.getLong("t");
          break;
        }
      }

      for (int i = keyframes.length() - 1; i >= 0; i--) {
        JSONObject keyframe = keyframes.getJSONObject(i);
        if (keyframe.has("t")) {
          long endFrame = keyframe.getLong("t");
          if (endFrame <= startFrame) {
            throw new IllegalStateException(
                "Invalid frame compDuration " + startFrame + "->" + endFrame);
          }
          durationFrames = endFrame - startFrame;
          duration = (long) (durationFrames / (float) frameRate * 1000);
          delay = (long) (startFrame / (float) frameRate * 1000);
          break;
        }
      }

      boolean addStartValue = true;
      boolean addTimePadding = false;
      V outValue = null;

      for (int i = 0; i < keyframes.length(); i++) {
        JSONObject keyframe = keyframes.getJSONObject(i);
        long frame = keyframe.getLong("t");
        float timePercentage = (float) (frame - startFrame) / (float) durationFrames;

        if (outValue != null) {
          keyValues.add(outValue);
          interpolators.add(new LinearInterpolator());
          outValue = null;
        }

        V startValue =
            keyframe.has("s") ? valueFromObject(keyframe.getJSONArray("s"), getScale()) : null;
        if (addStartValue) {
          if (startValue != null) {
            if (i == 0) {
              //noinspection ResourceAsColor
              initialValue = startValue;
            }
            keyValues.add(startValue);
            if (!interpolators.isEmpty()) {
              interpolators.add(new LinearInterpolator());
            }
          }
          addStartValue = false;
        }

        if (addTimePadding) {
          float holdPercentage = timePercentage - 0.00001f;
          keyTimes.add(holdPercentage);
          addTimePadding = false;
        }

        if (keyframe.has("e")) {
          V endValue = valueFromObject(keyframe.getJSONArray("e"), getScale());
          keyValues.add(endValue);
                    /*
                      Timing function for time interpolation between keyframes.
                      Should be n - 1 where n is the number of keyframes.
                     */
          Interpolator timingFunction;
          if (keyframe.has("o") && keyframe.has("i")) {
            JSONObject timingControlPoint1 = keyframe.getJSONObject("o");
            JSONObject timingControlPoint2 = keyframe.getJSONObject("i");
            PointF cp1 = JsonUtils.pointValueFromJsonObject(timingControlPoint1, 1);
            PointF cp2 = JsonUtils.pointValueFromJsonObject(timingControlPoint2, 1);

            timingFunction = PathInterpolatorCompat.create(cp1.x, cp1.y, cp2.x, cp2.y);
          } else {
            timingFunction = new LinearInterpolator();
          }
          interpolators.add(timingFunction);
        }

        keyTimes.add(timePercentage);

        if (keyframe.has("h") && keyframe.getInt("h") == 1) {
          outValue = startValue;
          addStartValue = true;
          addTimePadding = true;
        }
      }
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse values.", e);
    }
  }

  private float getScale() {
    return isDp ? composition.getScale() : 1f;
  }

  O convertType(V value) {
    //noinspection unchecked
    return (O) value;
  }

  public boolean hasAnimation() {
    return !keyValues.isEmpty();
  }

  public O getInitialValue() {
    return convertType(initialValue);
  }

  protected abstract V valueFromObject(Object object, float scale) throws JSONException;

  public abstract KeyframeAnimation createAnimation();

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("initialValue=").append(initialValue);
    if (!keyValues.isEmpty()) {
      sb.append(", values=").append(Arrays.toString(keyTimes.toArray()));
    }
    return sb.toString();
  }
}
