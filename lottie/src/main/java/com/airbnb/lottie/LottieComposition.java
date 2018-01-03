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

import com.airbnb.lottie.model.FileCompositionLoader;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.JsonCompositionLoader;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.utils.Utils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
  private Rect bounds;
  private float startFrame;
  private float endFrame;
  private float frameRate;
  /* Bodymovin version */
  private int majorVersion;
  private int minorVersion;
  private int patchVersion;

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
  public int getMajorVersion() {
    return majorVersion;
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public int getMinorVersion() {
    return minorVersion;
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public int getPatchVersion() {
    return patchVersion;
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
     * Loads a composition from a file stored in res/raw.
     */
    @SuppressWarnings("WeakerAccess") public static Cancellable fromRawFile(
        Context context, @RawRes int resId, OnCompositionLoadedListener loadedListener) {
      return fromInputStream(context, context.getResources().openRawResource(resId), loadedListener);
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
      JsonCompositionLoader loader = new JsonCompositionLoader(loadedListener);
      loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, json);
      return loader;
    }

    @Nullable
    public static LottieComposition fromInputStream(Resources res, InputStream stream) {
      try {
        return fromJsonSync(new JsonReader(new InputStreamReader(stream)));
      } catch (IOException e) {
        Log.e(L.TAG, "Failed to load composition.",
            new IllegalStateException("Unable to find file.", e));
      } finally {
        closeQuietly(stream);
      }
      return null;
    }

    /**
     * Use {@link #fromJsonSync(JsonReader)}
     */
    @Deprecated
    public static LottieComposition fromJsonSync(Resources res, JSONObject json) {
      try {
        return fromJsonSync(res, new JsonReader(new StringReader(json.toString())));
      } catch (IOException e) {
        throw new IllegalArgumentException("Unable to parse json", e);
      }
    }

    /**
     * Use {@link #fromJsonSync(JsonReader)}
     */
    @Deprecated
    public static LottieComposition fromJsonSync(
        @SuppressWarnings("unused") Resources res, JsonReader reader) throws IOException {
      return fromJsonSync(reader);
    }

    public static LottieComposition fromJsonSync(JsonReader reader) throws IOException {
      float scale = Utils.dpScale();
      int width = -1;
      LottieComposition composition = new LottieComposition();

      reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "w":
            width = reader.nextInt();
            break;
          case "h":
            int height = reader.nextInt();
            int scaledWidth = (int) (width * scale);
            int scaledHeight = (int) (height * scale);
            composition.bounds = new Rect(0, 0, scaledWidth, scaledHeight);
            break;
          case "ip":
            composition.startFrame = (float) reader.nextDouble();
            break;
          case "op":
            composition.endFrame = (float) reader.nextDouble();
            break;
          case "fr":
            composition.frameRate = (float) reader.nextDouble();
            break;
          case "v":
            String version = reader.nextString();
            String[] versions = version.split("\\.");
            composition.majorVersion = Integer.parseInt(versions[0]);
            composition.minorVersion = Integer.parseInt(versions[1]);
            composition.patchVersion = Integer.parseInt(versions[2]);
            if (!Utils.isAtLeastVersion(composition, 4, 5, 0)) {
              composition.addWarning("Lottie only supports bodymovin >= 4.5.0");
            }
            break;
          case "layers":
            parseLayers(reader, composition);
            break;
          case "assets":
            parseAssets(reader, composition);
            break;
          case "fonts":
            parseFonts(reader, composition);
            break;
          case "chars":
            parseChars(reader, composition);
            break;
          default:
            reader.skipValue();
        }
      }
      reader.endObject();
      return composition;
    }

    private static void parseLayers(JsonReader reader, LottieComposition composition)
        throws IOException {
      int imageCount = 0;
      reader.beginArray();
      while (reader.hasNext()) {
        Layer layer = Layer.Factory.newInstance(reader, composition);
        if (layer.getLayerType() == Layer.LayerType.Image) {
          imageCount++;
        }
        addLayer(composition.layers, composition.layerMap, layer);

        if (imageCount > 4) {
          composition.warnings.add("You have " + imageCount + " images. Lottie should primarily be " +
              "used with shapes. If you are using Adobe Illustrator, convert the Illustrator layers" +
              " to shape layers.");
        }
      }
      reader.endArray();
    }

    private static void parseAssets(
        JsonReader reader, LottieComposition composition) throws IOException {
      reader.beginArray();
      while (reader.hasNext()) {
        String id = null;
        // For precomps
        List<Layer> layers = new ArrayList<>();
        LongSparseArray<Layer> layerMap = new LongSparseArray<>();
        // For images
        int width = 0;
        int height = 0;
        String imageFileName = null;
        String relativeFolder = null;
        reader.beginObject();
        while (reader.hasNext()) {
          switch (reader.nextName()) {
            case "id":
              id = reader.nextString();
              break;
            case "layers":
              reader.beginArray();
              while (reader.hasNext()) {
                Layer layer = Layer.Factory.newInstance(reader, composition);
                layerMap.put(layer.getId(), layer);
                layers.add(layer);
              }
              reader.endArray();
              break;
            case "w":
              width = reader.nextInt();
              break;
            case "h":
              height = reader.nextInt();
              break;
            case "p":
              imageFileName = reader.nextString();
              break;
            case "u":
              relativeFolder = reader.nextString();
              break;
            default:
              reader.skipValue();
          }
        }
        reader.endObject();
        if (!layers.isEmpty()) {
          composition.precomps.put(id, layers);
        } else if (imageFileName != null) {
          LottieImageAsset image =
              new LottieImageAsset(width, height, id, imageFileName, relativeFolder);
          composition.images.put(image.getId(), image);
        }
      }
      reader.endArray();
    }

    private static void parseFonts(
        JsonReader reader, LottieComposition composition) throws IOException {

      reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "list":
            reader.beginArray();
            while (reader.hasNext()) {
              Font font = Font.Factory.newInstance(reader);
              composition.fonts.put(font.getName(), font);
            }
            reader.endArray();
            break;
          default:
            reader.skipValue();
        }
      }
      reader.endObject();
    }

    private static void parseChars(
        JsonReader reader, LottieComposition composition) throws IOException {
      reader.beginArray();
      while (reader.hasNext()) {
        FontCharacter character =
            FontCharacter.Factory.newInstance(reader, composition);
        composition.characters.put(character.hashCode(), character);
      }
      reader.endArray();
    }

    private static void addLayer(List<Layer> layers, LongSparseArray<Layer> layerMap, Layer layer) {
      layers.add(layer);
      layerMap.put(layer.getId(), layer);
    }
  }
}
