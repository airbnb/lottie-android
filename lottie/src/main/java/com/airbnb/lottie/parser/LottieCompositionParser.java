package com.airbnb.lottie.parser;

import android.graphics.Rect;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SparseArrayCompat;
import android.util.JsonReader;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieImageAsset;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LottieCompositionParser {
  public static LottieComposition parse(JsonReader reader) throws IOException {
    float scale = Utils.dpScale();
    float startFrame = 0f;
    float endFrame = 0f;
    float frameRate = 0f;
    int majorVersion = 0;
    int minorVersion = 0;
    int patchVersion = 0;
    final LongSparseArray<Layer> layerMap = new LongSparseArray<>();
    final List<Layer> layers = new ArrayList<>();
    int width = 0;
    int height = 0;
    Map<String, List<Layer>> precomps = new HashMap<>();
    Map<String, LottieImageAsset> images = new HashMap<>();
    Map<String, Font> fonts = new HashMap<>();
    SparseArrayCompat<FontCharacter> characters = new SparseArrayCompat<>();

    LottieComposition composition = new LottieComposition();

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "w":
          width = reader.nextInt();
          break;
        case "h":
          height = reader.nextInt();
          break;
        case "ip":
          startFrame = (float) reader.nextDouble();
          break;
        case "op":
          endFrame = (float) reader.nextDouble();
          break;
        case "fr":
          frameRate = (float) reader.nextDouble();
          break;
        case "v":
          String version = reader.nextString();
          String[] versions = version.split("\\.");
          majorVersion = Integer.parseInt(versions[0]);
          minorVersion = Integer.parseInt(versions[1]);
          patchVersion = Integer.parseInt(versions[2]);
          if (!Utils.isAtLeastVersion(composition, 4, 5, 0)) {
            composition.addWarning("Lottie only supports bodymovin >= 4.5.0");
          }
          break;
        case "layers":
          parseLayers(reader, composition, layers, layerMap);
          break;
        case "assets":
          parseAssets(reader, composition, precomps, images);
          break;
        case "fonts":
          parseFonts(reader, fonts);
          break;
        case "chars":
          parseChars(reader, composition, characters);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();

    int scaledWidth = (int) (width * scale);
    int scaledHeight = (int) (height * scale);
    Rect bounds = new Rect(0, 0, scaledWidth, scaledHeight);

    composition.init(bounds, startFrame, endFrame, frameRate, majorVersion, minorVersion,
        patchVersion, layers, layerMap, precomps, images, characters, fonts);

    return composition;
  }

  private static void parseLayers(JsonReader reader, LottieComposition composition,
      List<Layer> layers, LongSparseArray<Layer> layerMap) throws IOException {
    int imageCount = 0;
    reader.beginArray();
    while (reader.hasNext()) {
      Layer layer = Layer.Factory.newInstance(reader, composition);
      if (layer.getLayerType() == Layer.LayerType.Image) {
        imageCount++;
      }
      layers.add(layer);
      layerMap.put(layer.getId(), layer);

      if (imageCount > 4) {
        L.warn("You have " + imageCount + " images. Lottie should primarily be " +
            "used with shapes. If you are using Adobe Illustrator, convert the Illustrator layers" +
            " to shape layers.");
      }
    }
    reader.endArray();
  }

  private static void parseAssets(JsonReader reader, LottieComposition composition,
      Map<String, List<Layer>> precomps, Map<String, LottieImageAsset> images) throws IOException {
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
        precomps.put(id, layers);
      } else if (imageFileName != null) {
        LottieImageAsset image =
            new LottieImageAsset(width, height, id, imageFileName, relativeFolder);
        images.put(image.getId(), image);
      }
    }
    reader.endArray();
  }

  private static void parseFonts(JsonReader reader, Map<String, Font> fonts) throws IOException {

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "list":
          reader.beginArray();
          while (reader.hasNext()) {
            Font font = Font.Factory.newInstance(reader);
            fonts.put(font.getName(), font);
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
      JsonReader reader, LottieComposition composition,
      SparseArrayCompat<FontCharacter> characters) throws IOException {
    reader.beginArray();
    while (reader.hasNext()) {
      FontCharacter character = FontCharacter.Factory.newInstance(reader, composition);
      characters.put(character.hashCode(), character);
    }
    reader.endArray();
  }
}
