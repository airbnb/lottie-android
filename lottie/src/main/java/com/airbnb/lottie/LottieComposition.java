package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.RestrictTo;
import androidx.annotation.WorkerThread;
import androidx.collection.LongSparseArray;
import androidx.collection.SparseArrayCompat;
import android.util.JsonReader;
import android.util.Log;

import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.layer.Layer;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * After Effects/Bodymovin composition model. This is the serialized model from which the
 * animation will be created.
 *
 * To create one, use {@link LottieCompositionFactory}.
 *
 * It can be used with a {@link com.airbnb.lottie.LottieAnimationView} or
 * {@link com.airbnb.lottie.LottieDrawable}.
 */
public class LottieComposition {

  private final PerformanceTracker performanceTracker = new PerformanceTracker();
  private final HashSet<String> warnings = new HashSet<>();
  private Map<String, List<Layer>> precomps;
  private Map<String, LottieImageAsset> images;
  /** Map of font names to fonts */
  private Map<String, Font> fonts;
  private SparseArrayCompat<FontCharacter> characters;
  private LongSparseArray<Layer> layerMap;
  private List<Layer> layers;
  // This is stored as a set to avoid duplicates.
  private Rect bounds;
  private float startFrame;
  private float endFrame;
  private float frameRate;

  public void init(Rect bounds, float startFrame, float endFrame, float frameRate,
      List<Layer> layers, LongSparseArray<Layer> layerMap, Map<String,
      List<Layer>> precomps, Map<String, LottieImageAsset> images,
      SparseArrayCompat<FontCharacter> characters, Map<String, Font> fonts) {
    this.bounds = bounds;
    this.startFrame = startFrame;
    this.endFrame = endFrame;
    this.frameRate = frameRate;
    this.layers = layers;
    this.layerMap = layerMap;
    this.precomps = precomps;
    this.images = images;
    this.characters = characters;
    this.fonts = fonts;
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public void addWarning(String warning) {
    Log.w(L.TAG, warning);
    warnings.add(warning);
  }

  public ArrayList<String> getWarnings() {
    return new ArrayList<>(Arrays.asList(warnings.toArray(new String[warnings.size()])));
  }

  @SuppressWarnings("WeakerAccess") public void setPerformanceTrackingEnabled(boolean enabled) {
    performanceTracker.setEnabled(enabled);
  }

  public PerformanceTracker getPerformanceTracker() {
    return performanceTracker;
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public Layer layerModelForId(long id) {
    return layerMap.get(id);
  }

  @SuppressWarnings("WeakerAccess") public Rect getBounds() {
    return bounds;
  }

  @SuppressWarnings("WeakerAccess") public float getDuration() {
    return (long) (getDurationFrames() / frameRate * 1000);
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public float getStartFrame() {
    return startFrame;
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public float getEndFrame() {
    return endFrame;
  }

  public float getFrameRate() {
    return frameRate;
  }

  public List<Layer> getLayers() {
    return layers;
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  @Nullable
  public List<Layer> getPrecomps(String id) {
    return precomps.get(id);
  }

  public SparseArrayCompat<FontCharacter> getCharacters() {
    return characters;
  }

  public Map<String, Font> getFonts() {
    return fonts;
  }

  public boolean hasImages() {
    return !images.isEmpty();
  }

  @SuppressWarnings("WeakerAccess") public Map<String, LottieImageAsset> getImages() {
    return images;
  }

  public float getDurationFrames() {
    return endFrame - startFrame;
  }


  @Override public String toString() {
    final StringBuilder sb = new StringBuilder("LottieComposition:\n");
    for (Layer layer : layers) {
      sb.append(layer.toString("\t"));
    }
    return sb.toString();
  }

  /**
   * This will be removed in the next version of Lottie. {@link LottieCompositionFactory} has improved
   * API names, failure handlers, and will return in-progress tasks so you will never parse the same
   * animation twice in parallel.
   *
   * @see LottieCompositionFactory
   */
  @Deprecated
  public static class Factory {
    private Factory() {
    }

    /**
     * @see LottieCompositionFactory#fromAsset(Context, String)
     */
    @Deprecated
    public static Cancellable fromAssetFileName(Context context, String fileName, OnCompositionLoadedListener l) {
      ListenerAdapter listener = new ListenerAdapter(l);
      LottieCompositionFactory.fromAsset(context, fileName).addListener(listener);
      return listener;
    }

    /**
     * @see LottieCompositionFactory#fromRawRes(Context, int)
     */
    @Deprecated
     public static Cancellable fromRawFile(Context context, @RawRes int resId, OnCompositionLoadedListener l) {
       ListenerAdapter listener = new ListenerAdapter(l);
       LottieCompositionFactory.fromRawRes(context, resId).addListener(listener);
       return listener;
    }

    /**
     * @see LottieCompositionFactory#fromJsonInputStream(InputStream)
     */
    @Deprecated
    public static Cancellable fromInputStream(InputStream stream, OnCompositionLoadedListener l) {
      ListenerAdapter listener = new ListenerAdapter(l);
      LottieCompositionFactory.fromJsonInputStream(stream, null).addListener(listener);
      return listener;
    }

    /**
     * @see LottieCompositionFactory#fromJsonString(String)
     */
    @Deprecated
    public static Cancellable fromJsonString(String jsonString, OnCompositionLoadedListener l) {
      ListenerAdapter listener = new ListenerAdapter(l);
      LottieCompositionFactory.fromJsonString(jsonString, null).addListener(listener);
      return listener;
    }

    /**
     * @see LottieCompositionFactory#fromJsonReader(JsonReader)
     */
    @Deprecated
    public static Cancellable fromJsonReader(JsonReader reader, OnCompositionLoadedListener l) {
      ListenerAdapter listener = new ListenerAdapter(l);
      LottieCompositionFactory.fromJsonReader(reader, null).addListener(listener);
      return listener;
    }

    /**
     * @see LottieCompositionFactory#fromAssetSync(Context, String)
     */
    @Nullable
    @WorkerThread
    @Deprecated
    public static LottieComposition fromFileSync(Context context, String fileName) {
      return LottieCompositionFactory.fromAssetSync(context, fileName).getValue();
    }

    /**
     * @see LottieCompositionFactory#fromJsonInputStreamSync(InputStream)
     */
    @Nullable
    @WorkerThread
    @Deprecated
    public static LottieComposition fromInputStreamSync(InputStream stream) {
      return LottieCompositionFactory.fromJsonInputStreamSync(stream, null).getValue();
    }

    /**
     * This will now auto-close the input stream!
     *
     * @see LottieCompositionFactory#fromJsonInputStreamSync(InputStream, boolean)
     */
    @Nullable
    @WorkerThread
    @Deprecated
    public static LottieComposition fromInputStreamSync(InputStream stream, boolean close) {
      if (close) {
        Log.w(L.TAG, "Lottie now auto-closes input stream!");
      }
      return LottieCompositionFactory.fromJsonInputStreamSync(stream, null).getValue();
    }

    /**
     * @see LottieCompositionFactory#fromJsonSync(JSONObject)
     */
    @Nullable
    @WorkerThread
    @Deprecated
    public static LottieComposition fromJsonSync(@SuppressWarnings("unused") Resources res, JSONObject json) {
      return LottieCompositionFactory.fromJsonSync(json, null).getValue();
    }

    /**
     * @see LottieCompositionFactory#fromJsonStringSync(String)
     */
    @Nullable
    @WorkerThread
    @Deprecated
    public static LottieComposition fromJsonSync(String json) {
      return LottieCompositionFactory.fromJsonStringSync(json, null).getValue();
    }

    /**
     * @see LottieCompositionFactory#fromJsonReaderSync(JsonReader)
     */
    @Nullable
    @WorkerThread
    @Deprecated
    public static LottieComposition fromJsonSync(JsonReader reader) throws IOException {
      return LottieCompositionFactory.fromJsonReaderSync(reader, null).getValue();
    }

    private static final class ListenerAdapter implements LottieListener<LottieComposition>, Cancellable {
      private final OnCompositionLoadedListener listener;
      private boolean cancelled = false;

      private ListenerAdapter(OnCompositionLoadedListener listener) {
        this.listener = listener;
      }

      @Override public void onResult(LottieComposition composition) {
        if (cancelled) {
          return;
        }
        listener.onCompositionLoaded(composition);
      }

      @Override public void cancel() {
        cancelled = true;
      }
    }
  }
}
