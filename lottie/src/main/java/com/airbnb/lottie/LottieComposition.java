package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.RestrictTo;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SparseArrayCompat;
import android.util.JsonReader;
import android.util.Log;

import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.parser.AsyncCompositionLoader;
import com.airbnb.lottie.parser.LottieCompositionParser;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.airbnb.lottie.utils.Utils.closeQuietly;

/**
 * After Effects/Bodymovin composition model. This is the serialized model from which the
 * animation will be created.
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
    float frameDuration = endFrame - startFrame;
    return (long) (frameDuration / frameRate * 1000);
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public float getStartFrame() {
    return startFrame;
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public float getEndFrame() {
    return endFrame;
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
    return getDuration() * frameRate / 1000f;
  }


  @Override public String toString() {
    final StringBuilder sb = new StringBuilder("LottieComposition:\n");
    for (Layer layer : layers) {
      sb.append(layer.toString("\t"));
    }
    return sb.toString();
  }

  @SuppressWarnings({"WeakerAccess"})
  public static class Factory {
    private Factory() {
    }

    /**
     * Loads a composition from a file stored in /assets.
     */
    public static Cancellable fromAssetFileName(
        Context context, String fileName, OnCompositionLoadedListener listener) {
      InputStream stream;
      try {
        stream = context.getAssets().open(fileName);
      } catch (IOException e) {
        throw new IllegalArgumentException("Unable to find file " + fileName, e);
      }
      return fromInputStream(stream, listener);
    }

    /**
     * Loads a composition from a file stored in res/raw.
     */
     public static Cancellable fromRawFile(
         Context context, @RawRes int resId, OnCompositionLoadedListener listener) {
      return fromInputStream(context.getResources().openRawResource(resId), listener);
    }

    /**
     * Loads a composition from an arbitrary input stream.
     * <p>
     * ex: fromInputStream(context, new FileInputStream(filePath), (composition) -> {});
     */
    public static Cancellable fromInputStream(
        InputStream stream, OnCompositionLoadedListener listener) {
      return fromJsonReader(new JsonReader(new InputStreamReader(stream)), listener);
    }

    /**
     * Loads a composition from a json string. This is preferable to loading a JSONObject because
     * internally, Lottie uses {@link JsonReader} so any original overhead to create the JSONObject
     * is wasted.
     *
     * This is the preferred method to use when loading an animation from the network because you
     * have the response body as a raw string already. No need to convert it to a JSONObject.
     *
     * If you do have a JSONObject, you can call:
     *    `new JsonReader(new StringReader(jsonObject));`
     * However, this is not recommended.
     */
    public static Cancellable fromJsonString(
        String jsonString, OnCompositionLoadedListener listener) {
      return fromJsonReader(new JsonReader(new StringReader(jsonString)), listener);
    }

    /**
     * Loads a composition from a json reader.
     * <p>
     * ex: fromInputStream(context, new FileInputStream(filePath), (composition) -> {});
     */
    public static Cancellable fromJsonReader(
        JsonReader reader, OnCompositionLoadedListener listener) {
      AsyncCompositionLoader loader = new AsyncCompositionLoader(listener);
      loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, reader);
      return loader;
    }

    @Nullable
    public static LottieComposition fromFileSync(Context context, String fileName) {
      try {
        return fromInputStreamSync(context.getAssets().open(fileName));
      } catch (IOException e) {
        throw new IllegalArgumentException("Unable to open asset " + fileName, e);
      }
    }

    @Nullable
    public static LottieComposition fromInputStreamSync(InputStream stream) {
      LottieComposition composition;
      try {
        composition = fromJsonSync(new JsonReader(new InputStreamReader(stream)));
      } catch (IOException e) {
        throw new IllegalArgumentException("Unable to parse composition.", e);
      } finally {
        closeQuietly(stream);
      }
      return composition;
    }

    /**
     * Lottie now uses a streaming json parser. Prefer {@link #fromJsonSync(JsonReader)} if possible.
     * <p>
     * This will call toString() on your entire JSONObject.
     */
    @Deprecated
    public static LottieComposition fromJsonSync(@SuppressWarnings("unused") Resources res, JSONObject json) {
      return fromJsonSync(json.toString());
    }

    /**
     * Prefer using a JsonReader directly when possible. Reference the source for the async
     * factory methods above.
     */
    public static LottieComposition fromJsonSync(String string) {
      try {
        return fromJsonSync(new JsonReader(new StringReader(string)));
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }

    public static LottieComposition fromJsonSync(JsonReader reader) throws IOException {
      return LottieCompositionParser.parse(reader);
    }
  }
}
