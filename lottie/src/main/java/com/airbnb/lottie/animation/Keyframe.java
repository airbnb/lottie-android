package com.airbnb.lottie.animation;

import android.graphics.PointF;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.utils.JsonUtils;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Keyframe<T> {
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
  public static void setEndFrames(List<? extends Keyframe<?>> keyframes) {
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

  @Nullable private final LottieComposition composition;
  @Nullable public final T startValue;
  @Nullable public final T endValue;
  @Nullable public final Interpolator interpolator;
  public final float startFrame;
  @Nullable public Float endFrame;

  private float startProgress = Float.MIN_VALUE;
  private float endProgress = Float.MIN_VALUE;

  // Used by PathKeyframe but it has to be parsed by KeyFrame because we use a JsonReader to
  // deserialzie the data so we have to parse everything in order
  public PointF pathCp1 = null;
  public PointF pathCp2 = null;


  public Keyframe(@SuppressWarnings("NullableProblems") LottieComposition composition,
      @Nullable T startValue, @Nullable T endValue,
      @Nullable Interpolator interpolator, float startFrame, @Nullable Float endFrame) {
    this.composition = composition;
    this.startValue = startValue;
    this.endValue = endValue;
    this.interpolator = interpolator;
    this.startFrame = startFrame;
    this.endFrame = endFrame;
  }

  /**
   * Non-animated value.
   */
  public Keyframe(@SuppressWarnings("NullableProblems") T value) {
    composition = null;
    startValue = value;
    endValue = value;
    interpolator = null;
    startFrame = Float.MIN_VALUE;
    endFrame = Float.MAX_VALUE;
  }

  public float getStartProgress() {
    if (composition == null) {
      return 0f;
    }
    if (startProgress == Float.MIN_VALUE) {
      startProgress = (startFrame  - composition.getStartFrame()) / composition.getDurationFrames();
    }
    return startProgress;
  }

  public float getEndProgress() {
    if (composition == null) {
      return 1f;
    }
    if (endProgress == Float.MIN_VALUE) {
      if (endFrame == null) {
        endProgress = 1f;
      } else {
        float startProgress = getStartProgress();
        float durationFrames = endFrame - startFrame;
        float durationProgress = durationFrames / composition.getDurationFrames();
        endProgress = startProgress + durationProgress;
      }
    }
    return endProgress;
  }

  public boolean isStatic() {
    return interpolator == null;
  }

  public boolean containsProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    return progress >= getStartProgress() && progress < getEndProgress();
  }

  @Override public String toString() {
    return "Keyframe{" + "startValue=" + startValue +
        ", endValue=" + endValue +
        ", startFrame=" + startFrame +
        ", endFrame=" + endFrame +
        ", interpolator=" + interpolator +
        '}';
  }

  public static class Factory {
    private static SparseArrayCompat<WeakReference<Interpolator>> pathInterpolatorCache;

    // https://github.com/airbnb/lottie-android/issues/464
    private static SparseArrayCompat<WeakReference<Interpolator>> pathInterpolatorCache() {
      if (pathInterpolatorCache == null) {
        pathInterpolatorCache = new SparseArrayCompat<>();
      }
      return pathInterpolatorCache;
    }

    @Nullable
    private static WeakReference<Interpolator> getInterpolator(int hash) {
      // This must be synchronized because get and put isn't thread safe because
      // SparseArrayCompat has to create new sized arrays sometimes.
      synchronized (Factory.class) {
        return pathInterpolatorCache().get(hash);
      }
    }

    private static void putInterpolator(int hash, WeakReference<Interpolator> interpolator) {
      // This must be synchronized because get and put isn't thread safe because
      // SparseArrayCompat has to create new sized arrays sometimes.
      synchronized (Factory.class) {
        pathInterpolatorCache.put(hash, interpolator);
      }
    }

    private Factory() {
    }

    public static <T> Keyframe<T> newInstance(JsonReader reader, LottieComposition composition,
        float scale, AnimatableValue.Factory<T> valueFactory, boolean animated) throws IOException {

      if (animated) {
        return parseKeyframe(composition, reader, scale, valueFactory);
      } else {
        return parseStaticValue(reader, scale, valueFactory);
      }
    }

    /**
     * beginObject will already be called on the keyframe so it can be differentiated with
     * a non animated value.
     */
    private static <T> Keyframe<T> parseKeyframe(LottieComposition composition, JsonReader reader,
        float scale, AnimatableValue.Factory<T> valueFactory) throws IOException {
      PointF cp1 = null;
      PointF cp2 = null;
      float startFrame = 0;
      T startValue = null;
      T endValue = null;
      boolean hold = false;
      Interpolator interpolator = null;

      // Only used by PathKeyframe
      PointF pathCp1 = null;
      PointF pathCp2 = null;

      reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "t":
            startFrame = (float) reader.nextDouble();
            break;
          case "s":
            startValue = valueFactory.valueFromObject(reader, scale);
            break;
          case "e":
            endValue = valueFactory.valueFromObject(reader, scale);
            break;
          case "o":
            cp1 = JsonUtils.jsonToPoint(reader, scale);
            break;
          case "i":
            cp2 = JsonUtils.jsonToPoint(reader, scale);
            break;
          case "h":
            hold = reader.nextInt() == 1;
            break;
          case "to":
            pathCp1 = JsonUtils.jsonToPoint(reader, scale);
            break;
          case "ti":
            pathCp2 = JsonUtils.jsonToPoint(reader, scale);
            break;
          default:
            reader.skipValue();
        }
      }
      reader.endObject();

      if (hold) {
        endValue = startValue;
        // TODO: create a HoldInterpolator so progress changes don't invalidate.
        interpolator = LINEAR_INTERPOLATOR;
      } else if (cp1 != null && cp2 != null) {
        cp1.x = MiscUtils.clamp(cp1.x, -scale, scale);
        cp1.y = MiscUtils.clamp(cp1.y, -MAX_CP_VALUE, MAX_CP_VALUE);
        cp2.x = MiscUtils.clamp(cp2.x, -scale, scale);
        cp2.y = MiscUtils.clamp(cp2.y, -MAX_CP_VALUE, MAX_CP_VALUE);
        int hash = Utils.hashFor(cp1.x, cp1.y, cp2.x, cp2.y);
        WeakReference<Interpolator> interpolatorRef = getInterpolator(hash);
        if (interpolatorRef != null) {
          interpolator = interpolatorRef.get();
        }
        if (interpolatorRef == null || interpolator == null) {
          interpolator = PathInterpolatorCompat.create(
              cp1.x / scale, cp1.y / scale, cp2.x / scale, cp2.y / scale);
          try {
            putInterpolator(hash, new WeakReference<>(interpolator));
          } catch (ArrayIndexOutOfBoundsException e) {
            // It is not clear why but SparseArrayCompat sometimes fails with this:
            //     https://github.com/airbnb/lottie-android/issues/452
            // Because this is not a critical operation, we can safely just ignore it.
            // I was unable to repro this to attempt a proper fix.
          }
        }

      } else {
        interpolator = LINEAR_INTERPOLATOR;
      }

      Keyframe<T> keyframe =
          new Keyframe<>(composition, startValue, endValue, interpolator, startFrame, null);
      keyframe.pathCp1 = pathCp1;
      keyframe.pathCp2 = pathCp2;
      return keyframe;
    }

    private static <T> Keyframe<T> parseStaticValue(JsonReader reader,
        float scale, AnimatableValue.Factory<T> valueFactory) throws IOException {
      T value = valueFactory.valueFromObject(reader, scale);
      return new Keyframe<>(value);
    }

    public static <T> List<Keyframe<T>> parseKeyframes(JsonReader reader,
        LottieComposition composition, float scale, AnimatableValue.Factory<T> valueFactory)
        throws IOException {
      List<Keyframe<T>> keyframes = new ArrayList<>();

      if (reader.peek() == JsonToken.STRING) {
        composition.addWarning("Lottie doesn't support expressions.");
        return keyframes;
      }

      reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "k":
            if (reader.peek() == JsonToken.BEGIN_ARRAY) {
              reader.beginArray();

              if (reader.peek() == JsonToken.NUMBER) {
                // For properties in which the static value is an array of numbers.
                keyframes.add(Keyframe.Factory.newInstance(reader, composition, scale, valueFactory, false));
              } else {
                while (reader.hasNext()) {
                  keyframes.add(Keyframe.Factory.newInstance(reader, composition, scale, valueFactory, true));
                }
              }
              reader.endArray();
            } else {
              keyframes.add(Keyframe.Factory.newInstance(reader, composition, scale, valueFactory, false));
            }
            break;
          default:
            reader.skipValue();
        }
      }
      reader.endObject();

      setEndFrames(keyframes);
      return keyframes;
    }
  }
}
