package com.airbnb.lottie;

import android.graphics.Rect;
import android.support.v4.util.LongSparseArray;
import android.util.JsonReader;

import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LottieCompositionParser {
  public static LottieComposition parse(JsonReader reader) throws IOException {
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
