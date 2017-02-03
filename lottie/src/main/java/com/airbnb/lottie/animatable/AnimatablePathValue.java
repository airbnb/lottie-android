package com.airbnb.lottie.animatable;

import android.graphics.PointF;
import android.support.annotation.RestrictTo;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.airbnb.lottie.animation.KeyframeAnimation;
import com.airbnb.lottie.animation.PathKeyframeAnimation;
import com.airbnb.lottie.animation.StaticKeyframeAnimation;
import com.airbnb.lottie.model.LottieComposition;
import com.airbnb.lottie.utils.JsonUtils;
import com.airbnb.lottie.utils.SegmentedPath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class AnimatablePathValue implements AnimatableValue {

  private final List<Float> keyTimes = new ArrayList<>();
  private final List<Interpolator> interpolators = new ArrayList<>();
  private final int frameRate;
  private final LottieComposition composition;

  private PointF initialPoint;
  private final SegmentedPath animationPath = new SegmentedPath();
  private long delay;
  private long duration;
  private long startFrame;
  private long durationFrames;

  /**
   * Create a default static animatable path.
   */
  public AnimatablePathValue(LottieComposition composition) {
    frameRate = 0;
    this.composition = composition;
    this.initialPoint = new PointF(0, 0);
  }

  public AnimatablePathValue(JSONObject pointValues, int frameRate, LottieComposition composition) {
    this.frameRate = frameRate;
    this.composition = composition;

    Object value;
    try {
      value = pointValues.get("k");
    } catch (JSONException e) {
      throw new IllegalArgumentException("Point values have no keyframes.");
    }

    if (value instanceof JSONArray) {
      Object firstObject;
      try {
        firstObject = ((JSONArray) value).get(0);
      } catch (JSONException e) {
        throw new IllegalArgumentException("Unable to parse value.");
      }

      if (firstObject instanceof JSONObject && ((JSONObject) firstObject).has("t")) {
        // Keyframes
        buildAnimationForKeyframes((JSONArray) value);
      } else {
        // Single Value, no animation
        initialPoint = JsonUtils.pointFromJsonArray((JSONArray) value, composition.getScale());
      }
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
            throw new IllegalStateException("Invalid frame compDuration " + startFrame + "->" + endFrame);
          }
          durationFrames = endFrame - startFrame;
          duration = (long) (durationFrames / (float) frameRate * 1000);
          delay = (long) (startFrame / (float) frameRate * 1000);
          break;
        }
      }

      boolean addStartValue = true;
      boolean addTimePadding = false;
      PointF outPoint = null;

      for (int i = 0; i < keyframes.length(); i++) {
        JSONObject keyframe = keyframes.getJSONObject(i);
        long frame = keyframe.getLong("t");
        float timePercentage = (float) (frame - startFrame) / (float) durationFrames;

        if (outPoint != null) {
          PointF vertex = outPoint;
          animationPath.lineTo(vertex.x, vertex.y);
          interpolators.add(new LinearInterpolator());
          outPoint = null;
        }

        float scale = composition.getScale();
        PointF startPoint = keyframe.has("s") ? JsonUtils.pointFromJsonArray(keyframe.getJSONArray("s"), scale) : new PointF();
        if (addStartValue) {
          if (i == 0) {
            animationPath.moveTo(startPoint.x, startPoint.y);
            initialPoint = startPoint;
          } else {
            animationPath.lineTo(startPoint.x, startPoint.y);
            interpolators.add(new LinearInterpolator());
          }
          addStartValue = false;
        }

        if (addTimePadding) {
          float holdPercentage = timePercentage - 0.00001f;
          keyTimes.add(holdPercentage);
          addTimePadding = false;
        }

        PointF cp1;
        PointF cp2;
        if (keyframe.has("e")) {
          cp1 = keyframe.has("to") ? JsonUtils.pointFromJsonArray(keyframe.getJSONArray("to"), scale) : null;
          cp2 = keyframe.has("ti") ? JsonUtils.pointFromJsonArray(keyframe.getJSONArray("ti"), scale) : null;
          PointF vertex = JsonUtils.pointFromJsonArray(keyframe.getJSONArray("e"), scale);
          if (cp1 != null && cp2 != null && cp1.length() != 0 && cp2.length() != 0) {
            animationPath.cubicTo(
                startPoint.x + cp1.x, startPoint.y + cp1.y,
                vertex.x + cp2.x, vertex.y + cp2.y,
                vertex.x, vertex.y);
          } else {
            animationPath.lineTo(vertex.x, vertex.y);
          }

          Interpolator interpolator;
          if (keyframe.has("o") && keyframe.has("i")) {
            cp1 = JsonUtils.pointValueFromJsonObject(keyframe.getJSONObject("o"), scale);
            cp2 = JsonUtils.pointValueFromJsonObject(keyframe.getJSONObject("i"), scale);
            interpolator = PathInterpolatorCompat.create(cp1.x / scale, cp1.y / scale, cp2.x / scale, cp2.y / scale);
          } else {
            interpolator = new LinearInterpolator();
          }
          interpolators.add(interpolator);
        }

        keyTimes.add(timePercentage);

        if (keyframe.has("h") && keyframe.getInt("h") == 1) {
          outPoint = startPoint;
          addStartValue = true;
          addTimePadding = true;
        }
      }
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse keyframes " + keyframes, e);
    }
  }

  @Override
  public KeyframeAnimation<PointF> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialPoint);
    }

    KeyframeAnimation<PointF> animation = new PathKeyframeAnimation(duration, composition, keyTimes, animationPath, interpolators);
    animation.setStartDelay(delay);
    return animation;
  }

  @Override
  public boolean hasAnimation() {
    return animationPath.hasSegments();
  }

  public PointF getInitialPoint() {
    return initialPoint;
  }

  @Override
  public String toString() {
    return "initialPoint=" + initialPoint;
  }
}
