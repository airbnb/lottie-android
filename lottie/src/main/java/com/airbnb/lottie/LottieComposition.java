package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.width;
import static com.airbnb.lottie.Utils.closeQuietly;

/**
 * After Effects/Bodymovin composition model. This is the serialized model from which the
 * animation will be created.
 * It can be used with a {@link com.airbnb.lottie.LottieAnimationView} or
 * {@link com.airbnb.lottie.LottieDrawable}.
 */
public class LottieComposition {
  /**
   * The largest bitmap drawing cache can be is 8,294,400 bytes. There are 4 bytes per pixel
   * leaving ~2.3M pixels available.
   * Reduce the number a little bit for safety.
   * <p>
   * Hopefully this can be hardware accelerated someday.
   */
  private static final int MAX_PIXELS = 1000;

  private static void addLayer(LottieComposition composition, Layer layer) {
    composition.layers.add(layer);
    composition.layerMap.put(layer.getId(), layer);
    if (!layer.getMasks().isEmpty()) {
      composition.hasMasks = true;
    }
    if (layer.getMatteType() != null && layer.getMatteType() != Layer.MatteType.None) {
      composition.hasMattes = true;
    }
  }

  private final Map<String, List<Layer>> precomps;
  private final LongSparseArray<Layer> layerMap;
  private final List<Layer> layers;
  private final Rect bounds;
  private final long startFrame;
  private final long endFrame;
  private final int frameRate;
  private final boolean hasMasks;
  private final boolean hasMattes;
  private final float scale;

  private LottieComposition(Map<String, List<Layer>> precomps, LongSparseArray<Layer> layerMap,
      List<Layer> layers, float scale, Rect bounds, long startFrame, long endFrame, int frameRate,
      boolean hasMasks, boolean hasMattes) {
    this.precomps = precomps;
    this.layerMap = layerMap;
    this.layers = layers;
    this.scale = scale;
    this.bounds = bounds;
    this.startFrame = startFrame;
    this.endFrame = endFrame;
    this.frameRate = frameRate;
    this.hasMasks = hasMasks;
    this.hasMattes = hasMattes;
  }

  Layer layerModelForId(long id) {
    return layerMap.get(id);
  }

  Rect getBounds() {
    return bounds;
  }

  long getDuration() {
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

  float getDurationFrames() {
    return getDuration() * (float) frameRate / 1000f;
  }


  boolean hasMasks() {
    return hasMasks;
  }

  boolean hasMattes() {
    return hasMattes;
  }

  float getScale() {
    return scale;
  }

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder("LottieComposition:\n");
    for (Layer layer : layers) {
      sb.append(layer.toString("\t"));
    }
    return sb.toString();
  }

  public static class Factory {
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
      loader.execute(stream);
      return loader;
    }

    static LottieComposition fromFileSync(Context context, String fileName) {
      InputStream file;
      try {
        file = context.getAssets().open(fileName);
      } catch (IOException e) {
        throw new IllegalStateException("Unable to find file " + fileName, e);
      }
      return fromInputStream(context.getResources(), file);
    }

    /**
     * Loads a composition from a raw json object. This is useful for animations loaded from the
     * network.
     */
    public static Cancellable fromJson(Resources res, JSONObject json,
        OnCompositionLoadedListener loadedListener) {
      JsonCompositionLoader
          loader = new JsonCompositionLoader(res, loadedListener);
      loader.execute(json);
      return loader;
    }

    @SuppressWarnings("WeakerAccess")
    static LottieComposition fromInputStream(Resources res, InputStream stream) {
      try {
      /* TODO:
       *  It is never correct to use the return value of this method to allocate
       *  a buffer intended to hold all data in this stream.
       */
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
      float scale = res.getDisplayMetrics().density;
      Rect bounds = null;
      boolean hasMasks = false;
      boolean hasMattes = false;
      Map<String, List<Layer>> precomps = new HashMap<>();
      LongSparseArray<Layer> layerMap = new LongSparseArray<>();

      int width = json.optInt("w", -1);
      int height = json.optInt("h", -1);
      if (width != -1 && height != -1) {
        int scaledWidth = (int) (width * scale);
        int scaledHeight = (int) (height * scale);
        if (Math.max(scaledWidth, scaledHeight) > LottieComposition.MAX_PIXELS) {
          float factor =
              (float) LottieComposition.MAX_PIXELS / (float) Math.max(scaledWidth, scaledHeight);
          scaledWidth *= factor;
          scaledHeight *= factor;
          scale *= factor;
        }
        bounds = new Rect(0, 0, scaledWidth, scaledHeight);
      }

      long startFrame = json.optLong("ip", 0);
      long endFrame = json.optLong("op", 0);
      int frameRate = json.optInt("fr", 0);

      JSONArray jsonLayers = json.optJSONArray("layers");
      for (int i = 0; i < jsonLayers.length(); i++) {
        Layer layer = new Layer(jsonLayers.optJSONObject(i), composition);
        LottieComposition.addLayer(composition, layer);
      }

      JSONArray precompsJson = json.optJSONArray("assets");
      for (int i = 0; i < precompsJson.length(); i++) {
        JSONObject precomp = precompsJson.optJSONObject(i);
        JSONArray layersJson = precomp.optJSONArray("layers");
        if (layersJson == null) {
          Log.w(L.TAG, "Lottie doesn't yet support images.");
          // TODO: image support
          continue;
        }
        List<Layer> layers = new ArrayList<>(layersJson.length());
        LongSparseArray<Layer> layerMap = new LongSparseArray<>();
        for (int j = 0; j < layersJson.length(); j++) {
          Layer layer = new Layer(layersJson.optJSONObject(j), composition);
          layerMap.put(layer.getId(), layer);
          layers.add(layer);
          if (!layer.getMasks().isEmpty()) {
            hasMasks = true;
          }
          if (layer.getMatteType() != null && layer.getMatteType() != Layer.MatteType.None) {
            hasMattes = true;
          }
        }
        String id = precomp.optString("id");
        precomps.put(id, layers);
      }

      return new LottieComposition(precomps, layerMap, layers, scale, bounds, startFrame, endFrame,
          frameRate, hasMasks, hasMattes);
    }
  }
}