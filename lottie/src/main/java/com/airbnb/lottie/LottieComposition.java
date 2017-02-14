package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.LongSparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * After Effects/Bodymovin composition model. This is the serialized model from which the
 * animation will be created.
 * It can be used with a {@link com.airbnb.lottie.LottieAnimationView} or
 * {@link com.airbnb.lottie.LottieDrawable}.
 */
public class LottieComposition {
  public interface OnCompositionLoadedListener {
    void onCompositionLoaded(LottieComposition composition);
  }

  interface Cancellable {
    void cancel();
  }

  /**
   * The largest bitmap drawing cache can be is 8,294,400 bytes. There are 4 bytes per pixel
   * leaving ~2.3M pixels available.
   * Reduce the number a little bit for safety.
   * <p>
   * Hopefully this can be hardware accelerated someday.
   */
  private static final int MAX_PIXELS = 1000;

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
    JsonCompositionLoader loader = new JsonCompositionLoader(res, loadedListener);
    loader.execute(json);
    return loader;
  }

  @SuppressWarnings("WeakerAccess")
  static LottieComposition fromInputStream(Resources res, InputStream file) {
    try {
      int size = file.available();
      byte[] buffer = new byte[size];
      //noinspection ResultOfMethodCallIgnored
      file.read(buffer);
      file.close();
      String json = new String(buffer, "UTF-8");

      JSONObject jsonObject = new JSONObject(json);
      return LottieComposition.fromJsonSync(res, jsonObject);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to find file.", e);
    } catch (JSONException e) {
      throw new IllegalStateException("Unable to load JSON.", e);
    }
  }

  @SuppressWarnings("WeakerAccess")
  static LottieComposition fromJsonSync(Resources res, JSONObject json) {
    LottieComposition composition = new LottieComposition(res);

    int width = -1;
    int height = -1;
    try {
      width = json.getInt("w");
      height = json.getInt("h");
    } catch (JSONException e) {
      // ignore.
    }
    if (width != -1 && height != -1) {
      int scaledWidth = (int) (width * composition.scale);
      int scaledHeight = (int) (height * composition.scale);
      if (Math.max(scaledWidth, scaledHeight) > MAX_PIXELS) {
        float factor = (float) MAX_PIXELS / (float) Math.max(scaledWidth, scaledHeight);
        scaledWidth *= factor;
        scaledHeight *= factor;
        composition.scale *= factor;
      }
      composition.bounds = new Rect(0, 0, scaledWidth, scaledHeight);
    }

    try {
      composition.startFrame = json.getLong("ip");
      composition.endFrame = json.getLong("op");
      composition.frameRate = json.getInt("fr");
    } catch (JSONException e) {
      //
    }

    if (composition.endFrame != 0 && composition.frameRate != 0) {
      long frameDuration = composition.endFrame - composition.startFrame;
      composition.duration = (long) (frameDuration / (float) composition.frameRate * 1000);
    }

    try {
      JSONArray jsonLayers = json.getJSONArray("layers");
      for (int i = 0; i < jsonLayers.length(); i++) {
        Layer layer = Layer.fromJson(jsonLayers.getJSONObject(i), composition);
        addLayer(composition, layer);
      }
    } catch (JSONException e) {
      throw new IllegalStateException("Unable to find layers.", e);
    }

    // These are precomps. This naively adds the precomp layers to the main composition.
    // TODO: Significant work will have to be done to properly support them.
    try {
      JSONArray assets = json.getJSONArray("assets");
      for (int i = 0; i < assets.length(); i++) {
        JSONObject asset = assets.getJSONObject(i);
        JSONArray layers = asset.getJSONArray("layers");
        for (int j = 0; j < layers.length(); j++) {
          Layer layer = Layer.fromJson(layers.getJSONObject(j), composition);
          addLayer(composition, layer);
        }
      }
    } catch (JSONException e) {
      // Do nothing.
    }

    return composition;
  }

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

  private final LongSparseArray<Layer> layerMap = new LongSparseArray<>();
  private final List<Layer> layers = new ArrayList<>();
  private Rect bounds;
  private long startFrame;
  private long endFrame;
  private int frameRate;
  private long duration;
  private boolean hasMasks;
  private boolean hasMattes;
  private float scale;

  private LottieComposition(Resources res) {
    scale = res.getDisplayMetrics().density;
  }

  @VisibleForTesting
  LottieComposition(long duration) {
    scale = 1f;
    this.duration = duration;
  }


  Layer layerModelForId(long id) {
    return layerMap.get(id);
  }

  Rect getBounds() {
    return bounds;
  }

  long getDuration() {
    return duration;
  }

  long getEndFrame() {
    return endFrame;
  }

  List<Layer> getLayers() {
    return layers;
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

  private static final class FileCompositionLoader extends CompositionLoader<InputStream> {

    private final Resources res;
    private final OnCompositionLoadedListener loadedListener;

    FileCompositionLoader(Resources res, OnCompositionLoadedListener loadedListener) {
      this.res = res;
      this.loadedListener = loadedListener;
    }

    @Override
    protected LottieComposition doInBackground(InputStream... params) {
      return fromInputStream(res, params[0]);
    }

    @Override
    protected void onPostExecute(LottieComposition composition) {
      loadedListener.onCompositionLoaded(composition);
    }
  }

  private static final class JsonCompositionLoader extends CompositionLoader<JSONObject> {

    private final Resources res;
    private final OnCompositionLoadedListener loadedListener;

    JsonCompositionLoader(Resources res, OnCompositionLoadedListener loadedListener) {
      this.res = res;
      this.loadedListener = loadedListener;
    }

    @Override
    protected LottieComposition doInBackground(JSONObject... params) {
      return fromJsonSync(res, params[0]);
    }

    @Override
    protected void onPostExecute(LottieComposition composition) {
      loadedListener.onCompositionLoaded(composition);
    }
  }

  private abstract static class CompositionLoader<Params>
      extends AsyncTask<Params, Void, LottieComposition>
      implements Cancellable {

    @Override public void cancel() {
      cancel(true);
    }
  }
}