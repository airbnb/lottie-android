package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
  private final LongSparseArray<Layer> layerMap = new LongSparseArray<>();
  private final List<Layer> layers = new ArrayList<>();
  private final Rect bounds;
  private final long startFrame;
  private final long endFrame;
  private final int frameRate;
  private final float dpScale;

  private LottieComposition(
      Rect bounds, long startFrame, long endFrame, int frameRate, float dpScale) {
    this.bounds = bounds;
    this.startFrame = startFrame;
    this.endFrame = endFrame;
    this.frameRate = frameRate;
    this.dpScale = dpScale;
  }

  Layer layerModelForId(long id) {
    return layerMap.get(id);
  }

  @SuppressWarnings("WeakerAccess") public Rect getBounds() {
    return bounds;
  }

  @SuppressWarnings("WeakerAccess") public long getDuration() {
    long frameDuration = endFrame - startFrame;
    return (long) (frameDuration / (float) frameRate * 1000);
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

  @SuppressWarnings("unused") boolean hasImages() {
    return !images.isEmpty();
  }

  Map<String, LottieImageAsset> getImages() {
    return images;
  }

  float getDurationFrames() {
    return getDuration() * (float) frameRate / 1000f;
  }


  public float getDpScale() {
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
        throw new IllegalStateException("Unable to find file.", e);
      } catch (JSONException e) {
        throw new IllegalStateException("Unable to load JSON.", e);
      } finally {
        closeQuietly(stream);
      }
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
      int frameRate = json.optInt("fr", 0);
      LottieComposition composition =
          new LottieComposition(bounds, startFrame, endFrame, frameRate, scale);
      JSONArray assetsJson = json.optJSONArray("assets");
      parseImages(assetsJson, composition);
      parsePrecomps(assetsJson, composition);
      parseLayers(json, composition);
      return composition;
    }

    private static void parseLayers(JSONObject json, LottieComposition composition) {
      JSONArray jsonLayers = json.optJSONArray("layers");
      // This should never be null. Bodymovin always exports at least an empty array.
      // However, it seems as if the demarshalling from the React Native library sometimes
      // causes this to be null. The proper fix should be done there but this will prevent a crash.
      // https://github.com/airbnb/lottie-android/issues/279
      if (jsonLayers == null) {
        return;
      }
      int length = jsonLayers.length();
      for (int i = 0; i < length; i++) {
        Layer layer = Layer.Factory.newInstance(jsonLayers.optJSONObject(i), composition);
        addLayer(composition.layers, composition.layerMap, layer);
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

    private static void addLayer(List<Layer> layers, LongSparseArray<Layer> layerMap, Layer layer) {
      layers.add(layer);
      layerMap.put(layer.getId(), layer);
    }
  }
}