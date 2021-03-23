package com.airbnb.lottie.parser;

import android.graphics.PointF;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.core.view.animation.PathInterpolatorCompat;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.Keyframe;

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

  static JsonReader.Options NAMES = JsonReader.Options.of(
      "t",  // 1
      "s",  // 2
      "e",  // 3
      "o",  // 4
      "i",  // 5
      "h",  // 6
      "to", // 7
      "ti"  // 8
  );
  static JsonReader.Options INTERPOLATOR_NAMES = JsonReader.Options.of(
      "x",  // 1
      "y"   // 2
  );

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

  /**
   * @param multiDimensional When true, the keyframe interpolators can be independent for the X and Y axis.
   */
  static <T> Keyframe<T> parse(JsonReader reader, LottieComposition composition,
      float scale, ValueParser<T> valueParser, boolean animated, boolean multiDimensional) throws IOException {

    if (animated && multiDimensional) {
      return parseMultiDimensionalKeyframe(composition, reader, scale, valueParser);
    } else if (animated) {
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
      switch (reader.selectName(NAMES)) {
        case 0: // t
          startFrame = (float) reader.nextDouble();
          break;
        case 1: // s
          startValue = valueParser.parse(reader, scale);
          break;
        case 2: // e
          endValue = valueParser.parse(reader, scale);
          break;
        case 3: // o
          cp1 = JsonUtils.jsonToPoint(reader, 1f);
          break;
        case 4: // i
          cp2 = JsonUtils.jsonToPoint(reader, 1f);
          break;
        case 5: // h
          hold = reader.nextInt() == 1;
          break;
        case 6: // to
          pathCp1 = JsonUtils.jsonToPoint(reader, scale);
          break;
        case 7: // ti
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
      interpolator = interpolatorFor(cp1, cp2);
    } else {
      interpolator = LINEAR_INTERPOLATOR;
    }

    Keyframe<T> keyframe = new Keyframe<>(composition, startValue, endValue, interpolator, startFrame, null);

    keyframe.pathCp1 = pathCp1;
    keyframe.pathCp2 = pathCp2;
    return keyframe;
  }

  private static <T> Keyframe<T> parseMultiDimensionalKeyframe(LottieComposition composition, JsonReader reader,
      float scale, ValueParser<T> valueParser) throws IOException {
    PointF cp1 = null;
    PointF cp2 = null;

    PointF xCp1 = null;
    PointF xCp2 = null;
    PointF yCp1 = null;
    PointF yCp2 = null;

    float startFrame = 0;
    T startValue = null;
    T endValue = null;
    boolean hold = false;
    Interpolator interpolator = null;
    Interpolator xInterpolator = null;
    Interpolator yInterpolator = null;

    // Only used by PathKeyframe
    PointF pathCp1 = null;
    PointF pathCp2 = null;

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0: // t
          startFrame = (float) reader.nextDouble();
          break;
        case 1: // s
          startValue = valueParser.parse(reader, scale);
          break;
        case 2: // e
          endValue = valueParser.parse(reader, scale);
          break;
        case 3: // o
          if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
            reader.beginObject();
            float xCp1x = 0f;
            float xCp1y = 0f;
            float yCp1x = 0f;
            float yCp1y = 0f;
            while (reader.hasNext()) {
              switch (reader.selectName(INTERPOLATOR_NAMES)) {
                case 0: // x
                  if (reader.peek() == JsonReader.Token.NUMBER) {
                    xCp1x = (float) reader.nextDouble();
                    yCp1x = xCp1x;
                  } else {
                    reader.beginArray();
                    xCp1x = (float) reader.nextDouble();
                    if (reader.peek() == JsonReader.Token.NUMBER) {
                      yCp1x = (float) reader.nextDouble();
                    } else {
                      yCp1x = xCp1x;
                    }
                    reader.endArray();
                  }
                  break;
                case 1: // y
                  if (reader.peek() == JsonReader.Token.NUMBER) {
                    xCp1y = (float) reader.nextDouble();
                    yCp1y = xCp1y;
                  } else {
                    reader.beginArray();
                    xCp1y = (float) reader.nextDouble();
                    if (reader.peek() == JsonReader.Token.NUMBER) {
                      yCp1y = (float) reader.nextDouble();
                    } else {
                      yCp1y = xCp1y;
                    }
                    reader.endArray();
                  }
                  break;
                default:
                  reader.skipValue();
              }
            }
            xCp1 = new PointF(xCp1x, xCp1y);
            yCp1 = new PointF(yCp1x, yCp1y);
            reader.endObject();
          } else {
            cp1 = JsonUtils.jsonToPoint(reader, scale);
          }
          break;
        case 4: // i
          if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
            reader.beginObject();
            float xCp2x = 0f;
            float xCp2y = 0f;
            float yCp2x = 0f;
            float yCp2y = 0f;
            while (reader.hasNext()) {
              switch (reader.selectName(INTERPOLATOR_NAMES)) {
                case 0: // x
                  if (reader.peek() == JsonReader.Token.NUMBER) {
                    xCp2x = (float) reader.nextDouble();
                    yCp2x = xCp2x;
                  } else {
                    reader.beginArray();
                    xCp2x = (float) reader.nextDouble();
                    if (reader.peek() == JsonReader.Token.NUMBER) {
                      yCp2x = (float) reader.nextDouble();
                    } else {
                      yCp2x = xCp2x;
                    }
                    reader.endArray();
                  }
                  break;
                case 1: // y
                  if (reader.peek() == JsonReader.Token.NUMBER) {
                    xCp2y = (float) reader.nextDouble();
                    yCp2y = xCp2y;
                  } else {
                    reader.beginArray();
                    xCp2y = (float) reader.nextDouble();
                    if (reader.peek() == JsonReader.Token.NUMBER) {
                      yCp2y = (float) reader.nextDouble();
                    } else {
                      yCp2y = xCp2y;
                    }
                    reader.endArray();
                  }
                  break;
                default:
                  reader.skipValue();
              }
            }
            xCp2 = new PointF(xCp2x, xCp2y);
            yCp2 = new PointF(yCp2x, yCp2y);
            reader.endObject();
          } else {
            cp2 = JsonUtils.jsonToPoint(reader, scale);
          }
          break;
        case 5: // h
          hold = reader.nextInt() == 1;
          break;
        case 6: // to
          pathCp1 = JsonUtils.jsonToPoint(reader, scale);
          break;
        case 7: // ti
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
      interpolator = interpolatorFor(cp1, cp2);
    } else if (xCp1 != null && yCp1 != null && xCp2 != null && yCp2 != null) {
      xInterpolator = interpolatorFor(xCp1, xCp2);
      yInterpolator = interpolatorFor(yCp1, yCp2);
    } else {
      interpolator = LINEAR_INTERPOLATOR;
    }

    Keyframe<T> keyframe;
    if (xInterpolator != null && yInterpolator != null) {
      keyframe = new Keyframe<>(composition, startValue, endValue, xInterpolator, yInterpolator, startFrame, null);
    } else {
      keyframe = new Keyframe<>(composition, startValue, endValue, interpolator, startFrame, null);
    }

    keyframe.pathCp1 = pathCp1;
    keyframe.pathCp2 = pathCp2;
    return keyframe;
  }

  private static Interpolator interpolatorFor(PointF cp1, PointF cp2) {
    Interpolator interpolator = null;
    cp1.x = MiscUtils.clamp(cp1.x, -1f, 1f);
    cp1.y = MiscUtils.clamp(cp1.y, -MAX_CP_VALUE, MAX_CP_VALUE);
    cp2.x = MiscUtils.clamp(cp2.x, -1f, 1f);
    cp2.y = MiscUtils.clamp(cp2.y, -MAX_CP_VALUE, MAX_CP_VALUE);
    int hash = Utils.hashFor(cp1.x, cp1.y, cp2.x, cp2.y);
    WeakReference<Interpolator> interpolatorRef = getInterpolator(hash);
    if (interpolatorRef != null) {
      interpolator = interpolatorRef.get();
    }
    if (interpolatorRef == null || interpolator == null) {
      try {
        interpolator = PathInterpolatorCompat.create(cp1.x, cp1.y, cp2.x, cp2.y);
      } catch (IllegalArgumentException e) {
        if ("The Path cannot loop back on itself.".equals(e.getMessage())) {
          // If a control point extends beyond the previous/next point then it will cause the value of the interpolator to no
          // longer monotonously increase. This clips the control point bounds to prevent that from happening.
          // NOTE: this will make the rendered animation behave slightly differently than the original.
          interpolator = PathInterpolatorCompat.create(Math.min(cp1.x, 1f), cp1.y, Math.max(cp2.x, 0f), cp2.y);
        } else {
          // We failed to create the interpolator. Fall back to linear.
          interpolator = new LinearInterpolator();
        }
      }
      try {
        putInterpolator(hash, new WeakReference<>(interpolator));
      } catch (ArrayIndexOutOfBoundsException e) {
        // It is not clear why but SparseArrayCompat sometimes fails with this:
        //     https://github.com/airbnb/lottie-android/issues/452
        // Because this is not a critical operation, we can safely just ignore it.
        // I was unable to repro this to attempt a proper fix.
      }
    }
    return interpolator;
  }

  private static <T> Keyframe<T> parseStaticValue(JsonReader reader,
      float scale, ValueParser<T> valueParser) throws IOException {
    T value = valueParser.parse(reader, scale);
    return new Keyframe<>(value);
  }
}
