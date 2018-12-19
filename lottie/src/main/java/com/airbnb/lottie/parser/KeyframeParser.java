package com.airbnb.lottie.parser;

import android.graphics.PointF;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.core.view.animation.PathInterpolatorCompat;
import android.util.JsonReader;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.lang.ref.WeakReference;

class KeyframeParser {
  /**
   * Some animations get exported with insane cp values in the tens of thousands.
   * PathInterpolator fails to create the interpolator in those cases and hangs.
   * Clamping the cp helps prevent that.
   */
  private static final float MAX_CP_VALUE = 100;
  private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
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
    synchronized (KeyframeParser.class) {
      return pathInterpolatorCache().get(hash);
    }
  }

  private static void putInterpolator(int hash, WeakReference<Interpolator> interpolator) {
    // This must be synchronized because get and put isn't thread safe because
    // SparseArrayCompat has to create new sized arrays sometimes.
    synchronized (KeyframeParser.class) {
      pathInterpolatorCache.put(hash, interpolator);
    }
  }

  static <T> Keyframe<T> parse(JsonReader reader, LottieComposition composition,
      float scale, ValueParser<T> valueParser, boolean animated) throws IOException {

    if (animated) {
      return parseKeyframe(composition, reader, scale, valueParser);
    } else {
      return parseStaticValue(reader, scale, valueParser);
    }
  }

  /**
   * beginObject will already be called on the keyframe so it can be differentiated with
   * a non animated value.
   */
  private static <T> Keyframe<T> parseKeyframe(LottieComposition composition, JsonReader reader,
      float scale, ValueParser<T> valueParser) throws IOException {
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
          startValue = valueParser.parse(reader, scale);
          break;
        case "e":
          endValue = valueParser.parse(reader, scale);
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
      float scale, ValueParser<T> valueParser) throws IOException {
    T value = valueParser.parse(reader, scale);
    return new Keyframe<>(value);
  }
}
