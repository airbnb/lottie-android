package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.airbnb.lottie.Utils.closeQuietly;

/**
 * After Effects/Bodymovin composition model. This is the serialized model from which the
 * animation will be created.
 * It can be used with a {@link com.airbnb.lottie.LottieAnimationView} or
 * {@link com.airbnb.lottie.LottieDrawable}.
 */
public class LottieComposition {

  private final Map<String, List<Layer>> precomps = new HashMap<>();
  private final Map<String, LottieImageAsset> images = new HashMap<>();
  /** Map of font names to fonts */
  private final Map<String, Font> fonts = new HashMap<>();
  private final SparseArrayCompat<FontCharacter> characters = new SparseArrayCompat<>();
  private final LongSparseArray<Layer> layerMap = new LongSparseArray<>();
  private final List<Layer> layers = new ArrayList<>();
  // This is stored as a set to avoid duplicates.
  private final HashSet<String> warnings = new HashSet<>();
  private final PerformanceTracker performanceTracker = new PerformanceTracker();
  private final Rect bounds;
  private final long startFrame;
  private final long endFrame;
  private final float frameRate;
  private final float dpScale;
  /* Bodymovin version */
  private final int majorVersion;
  private final int minorVersion;
  private final int patchVersion;

  private LottieComposition(Rect bounds, long startFrame, long endFrame, float frameRate,
      float dpScale, int major, int minor, int patch) {
    this.bounds = bounds;
    this.startFrame = startFrame;
    this.endFrame = endFrame;
    this.frameRate = frameRate;
    this.dpScale = dpScale;
    this.majorVersion = major;
    this.minorVersion = minor;
    this.patchVersion = patch;
    if (!Utils.isAtLeastVersion(this, 4, 5, 0)) {
      addWarning("Lottie only supports bodymovin >= 4.5.0");
    }
  }

  void addWarning(String warning) {
    Log.w(L.TAG, warning);
    warnings.add(warning);
  }

  public ArrayList<String> getWarnings() {
    return new ArrayList<>(Arrays.asList(warnings.toArray(new String[warnings.size()])));
  }

  public void setPerformanceTrackingEnabled(boolean enabled) {
    performanceTracker.setEnabled(enabled);
  }

  public PerformanceTracker getPerformanceTracker() {
    return performanceTracker;
  }

  Layer layerModelForId(long id) {
    return layerMap.get(id);
  }

  @SuppressWarnings("WeakerAccess") public Rect getBounds() {
    return bounds;
  }

  @SuppressWarnings("WeakerAccess") public long getDuration() {
    long frameDuration = endFrame - startFrame;
    return (long) (frameDuration / frameRate * 1000);
  }

  int getMajorVersion() {
    return majorVersion;
  }

  int getMinorVersion() {
    return minorVersion;
  }

  int getPatchVersion() {
    return patchVersion;
  }

  long getStartFrame() {
    return startFrame;
  }

  long getEndFrame() {
    return endFrame;
  }

  List<Layer> getLayers() {
    return layers;
  }

  @Nullable
  List<Layer> getPrecomps(String id) {
    return precomps.get(id);
  }

  SparseArrayCompat<FontCharacter> getCharacters() {
    return characters;
  }

  Map<String, Font> getFonts() {
    return fonts;
  }

  public boolean hasImages() {
    return !images.isEmpty();
  }

  Map<String, LottieImageAsset> getImages() {
    return images;
  }

  float getDurationFrames() {
    return getDuration() * frameRate / 1000f;
  }


  float getDpScale() {
    return dpScale;
  }

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder("LottieComposition:\n");
    for (Layer layer : layers) {
      sb.append(layer.toString("\t"));
    }
    return sb.toString();
  }

  public static class Factory {
    private Factory() {
    }

    /**
     * Loads a composition from a file stored in /assets.
     */
    public static Cancellable fromAssetFileName(Context context, String fileName,
        OnCompositionLoadedListener loadedListener) {
      InputStream stream;
      try {
        stream = context.getAssets().open(fileName);
      } catch (IOException e) {
        throw new IllegalStateException("Unable to find file " + fileName, e);
      }
      return fromInputStream(context, stream, loadedListener);
    }

    /**
     * Loads a composition from an arbitrary input stream.
     * <p>
     * ex: fromInputStream(context, new FileInputStream(filePath), (composition) -> {});
     */
    public static Cancellable fromInputStream(Context context, InputStream stream,
        OnCompositionLoadedListener loadedListener) {
      FileCompositionLoader loader =
          new FileCompositionLoader(context.getResources(), loadedListener);
      loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, stream);
      return loader;
    }

    @SuppressWarnings("WeakerAccess")
    public static LottieComposition fromFileSync(Context context, String fileName) {
      InputStream stream;
      try {
        stream = context.getAssets().open(fileName);
      } catch (IOException e) {
        throw new IllegalStateException("Unable to find file " + fileName, e);
      }
      return fromInputStream(context.getResources(), stream);
    }

    /**
     * Loads a composition from a raw json object. This is useful for animations loaded from the
     * network.
     */
    public static Cancellable fromJson(Resources res, JSONObject json,
        OnCompositionLoadedListener loadedListener) {
      JsonCompositionLoader loader = new JsonCompositionLoader(res, loadedListener);
      loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, json);
      return loader;
    }

    @Nullable
    @SuppressWarnings("WeakerAccess")
    static LottieComposition fromInputStream(Resources res, InputStream stream) {
      try {
        // TODO: It's not correct to use available() to allocate the byte array.
        int size = stream.available();
        byte[] buffer = new byte[size];
        //noinspection ResultOfMethodCallIgnored
        stream.read(buffer);
        String json = new String(buffer, "UTF-8");
        JSONObject jsonObject = new JSONObject(json);
        return fromJsonSync(res, jsonObject);
      } catch (IOException e) {
        Log.e(L.TAG, "Failed to load composition.",
            new IllegalStateException("Unable to find file.", e));
      } catch (JSONException e) {
        Log.e(L.TAG, "Failed to load composition.",
            new IllegalStateException("Unable to load JSON.", e));
      } finally {
        closeQuietly(stream);
      }
      return null;
    }

    @SuppressWarnings("WeakerAccess")
    static LottieComposition fromJsonSync(Resources res, JSONObject json) {
      Rect bounds = null;
      float scale = res.getDisplayMetrics().density;
      int width = json.optInt("w", -1);
      int height = json.optInt("h", -1);

      if (width != -1 && height != -1) {
        int scaledWidth = (int) (width * scale);
        int scaledHeight = (int) (height * scale);
        bounds = new Rect(0, 0, scaledWidth, scaledHeight);
      }

      long startFrame = json.optLong("ip", 0);
      long endFrame = json.optLong("op", 0);
      float frameRate = (float) json.optDouble("fr", 0);
      String version = json.optString("v");
      String[] versions = version.split("[.]");
      int major = Integer.parseInt(versions[0]);
      int minor = Integer.parseInt(versions[1]);
      int patch = Integer.parseInt(versions[2]);
      LottieComposition composition = new LottieComposition(
          bounds, startFrame, endFrame, frameRate, scale, major, minor, patch);
      JSONArray assetsJson = json.optJSONArray("assets");
      parseImages(assetsJson, composition);
      parsePrecomps(assetsJson, composition);
      parseFonts(json.optJSONObject("fonts"), composition);
      parseChars(json.optJSONArray("chars"), composition);
      parseLayers(json, composition);
      return composition;
    }

    private static void parseLayers(JSONObject json, LottieComposition composition) {
      JSONArray jsonLayers = json.optJSONArray("layers");
      // This should never be null. Bodymovin always exports at least an empty array.
      // However, it seems as if the unmarshalling from the React Native library sometimes
      // causes this to be null. The proper fix should be done there but this will prevent a crash.
      // https://github.com/airbnb/lottie-android/issues/279
      if (jsonLayers == null) {
        return;
      }
      int length = jsonLayers.length();
      int imageCount = 0;
      for (int i = 0; i < length; i++) {
        Layer layer = Layer.Factory.newInstance(jsonLayers.optJSONObject(i), composition);
        if (layer.getLayerType() == Layer.LayerType.Image) {
          imageCount++;
        }
        addLayer(composition.layers, composition.layerMap, layer);
      }

      if (imageCount > 4) {
        composition.addWarning("You have " + imageCount + " images. Lottie should primarily be " +
            "used with shapes. If you are using Adobe Illustrator, convert the Illustrator layers" +
            " to shape layers.");
      }
    }

    private static void parsePrecomps(
        @Nullable JSONArray assetsJson, LottieComposition composition) {
      if (assetsJson == null) {
        return;
      }
      int length = assetsJson.length();
      for (int i = 0; i < length; i++) {
        JSONObject assetJson = assetsJson.optJSONObject(i);
        JSONArray layersJson = assetJson.optJSONArray("layers");
        if (layersJson == null) {
          continue;
        }
        List<Layer> layers = new ArrayList<>(layersJson.length());
        LongSparseArray<Layer> layerMap = new LongSparseArray<>();
        for (int j = 0; j < layersJson.length(); j++) {
          Layer layer = Layer.Factory.newInstance(layersJson.optJSONObject(j), composition);
          layerMap.put(layer.getId(), layer);
          layers.add(layer);
        }
        String id = assetJson.optString("id");
        composition.precomps.put(id, layers);
      }
    }

    private static void parseImages(
        @Nullable JSONArray assetsJson, LottieComposition composition) {
      if (assetsJson == null) {
        return;
      }
      int length = assetsJson.length();
      for (int i = 0; i < length; i++) {
        JSONObject assetJson = assetsJson.optJSONObject(i);
        if (!assetJson.has("p")) {
          continue;
        }
        LottieImageAsset image = LottieImageAsset.Factory.newInstance(assetJson);
        composition.images.put(image.getId(), image);
      }
    }

    private static void parseFonts(@Nullable JSONObject fonts, LottieComposition composition) {
      if (fonts == null) {
        return;
      }
      JSONArray fontsList = fonts.optJSONArray("list");
      if (fontsList == null) {
        return;
      }
      int length = fontsList.length();
      for (int i = 0; i < length; i++) {
        Font font = Font.Factory.newInstance(fontsList.optJSONObject(i));
        composition.fonts.put(font.getName(), font);
      }
    }

    private static void parseChars(@Nullable JSONArray charsJson, LottieComposition composition) {
      if (charsJson == null) {
        return;
      }

      int length = charsJson.length();
      for (int i = 0; i < length; i++) {
        FontCharacter character =
            FontCharacter.Factory.newInstance(charsJson.optJSONObject(i), composition);
        composition.characters.put(character.hashCode(), character);
      }
    }

    private static void addLayer(List<Layer> layers, LongSparseArray<Layer> layerMap, Layer layer) {
      layers.add(layer);
      layerMap.put(layer.getId(), layer);
    }
  }
}