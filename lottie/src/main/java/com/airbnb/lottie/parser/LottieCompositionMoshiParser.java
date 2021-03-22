package com.airbnb.lottie.parser;

import android.graphics.Rect;

import androidx.collection.LongSparseArray;
import androidx.collection.SparseArrayCompat;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieImageAsset;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.Marker;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LottieCompositionMoshiParser {
  private static final JsonReader.Options NAMES = JsonReader.Options.of(
      "w", // 0
      "h", // 1
      "ip", // 2
      "op", // 3
      "fr", // 4
      "v", // 5
      "layers", // 6
      "assets", // 7
      "fonts", // 8
      "chars", // 9
      "markers" // 10
  );

  public static LottieComposition parse(JsonReader reader) throws IOException {
    float scale = Utils.dpScale();
    float startFrame = 0f;
    float endFrame = 0f;
    float frameRate = 0f;
    final LongSparseArray<Layer> layerMap = new LongSparseArray<>();
    final List<Layer> layers = new ArrayList<>();
    int width = 0;
    int height = 0;
    Map<String, List<Layer>> precomps = new HashMap<>();
    Map<String, LottieImageAsset> images = new HashMap<>();
    Map<String, Font> fonts = new HashMap<>();
    List<Marker> markers = new ArrayList<>();
    SparseArrayCompat<FontCharacter> characters = new SparseArrayCompat<>();

    LottieComposition composition = new LottieComposition();
    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0:
          width = reader.nextInt();
          break;
        case 1:
          height = reader.nextInt();
          break;
        case 2:
          startFrame = (float) reader.nextDouble();
          break;
        case 3:
          endFrame = (float) reader.nextDouble() - 0.01f;
          break;
        case 4:
          frameRate = (float) reader.nextDouble();
          break;
        case 5:
          String version = reader.nextString();
          String[] versions = version.split("\\.");
          int majorVersion = Integer.parseInt(versions[0]);
          int minorVersion = Integer.parseInt(versions[1]);
          int patchVersion = Integer.parseInt(versions[2]);
          if (!Utils.isAtLeastVersion(majorVersion, minorVersion, patchVersion,
              4, 4, 0)) {
            composition.addWarning("Lottie only supports bodymovin >= 4.4.0");
          }
          break;
        case 6:
          parseLayers(reader, composition, layers, layerMap);
          break;
        case 7:
          parseAssets(reader, composition, precomps, images);
          break;
        case 8:
          parseFonts(reader, fonts);
          break;
        case 9:
          parseChars(reader, composition, characters);
          break;
        case 10:
          parseMarkers(reader, composition, markers);
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    int scaledWidth = (int) (width * scale);
    int scaledHeight = (int) (height * scale);
    Rect bounds = new Rect(0, 0, scaledWidth, scaledHeight);

    composition.init(bounds, startFrame, endFrame, frameRate, layers, layerMap, precomps,
        images, characters, fonts, markers);

    return composition;
  }

  private static void parseLayers(JsonReader reader, LottieComposition composition,
      List<Layer> layers, LongSparseArray<Layer> layerMap) throws IOException {
    int imageCount = 0;
    reader.beginArray();
    while (reader.hasNext()) {
      Layer layer = LayerParser.parse(reader, composition);
      if (layer.getLayerType() == Layer.LayerType.IMAGE) {
        imageCount++;
      }
      layers.add(layer);
      layerMap.put(layer.getId(), layer);

      if (imageCount > 4) {
        Logger.warning("You have " + imageCount + " images. Lottie should primarily be " +
            "used with shapes. If you are using Adobe Illustrator, convert the Illustrator layers" +
            " to shape layers.");
      }
    }
    reader.endArray();
  }


  static JsonReader.Options ASSETS_NAMES = JsonReader.Options.of(
      "id", // 0
      "layers", // 1
      "w", // 2
      "h", // 3
      "p", // 4
      "u" // 5
  );

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
        switch (reader.selectName(ASSETS_NAMES)) {
          case 0:
            id = reader.nextString();
            break;
          case 1:
            reader.beginArray();
            while (reader.hasNext()) {
              Layer layer = LayerParser.parse(reader, composition);
              layerMap.put(layer.getId(), layer);
              layers.add(layer);
            }
            reader.endArray();
            break;
          case 2:
            width = reader.nextInt();
            break;
          case 3:
            height = reader.nextInt();
            break;
          case 4:
            imageFileName = reader.nextString();
            break;
          case 5:
            relativeFolder = reader.nextString();
            break;
          default:
            reader.skipName();
            reader.skipValue();
        }
      }
      reader.endObject();
      if (imageFileName != null) {
        LottieImageAsset image =
            new LottieImageAsset(width, height, id, imageFileName, relativeFolder);
        images.put(image.getId(), image);
      } else {
        precomps.put(id, layers);
      }
    }
    reader.endArray();
  }

  private static final JsonReader.Options FONT_NAMES = JsonReader.Options.of("list");

  private static void parseFonts(JsonReader reader, Map<String, Font> fonts) throws IOException {
    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(FONT_NAMES)) {
        case 0:
          reader.beginArray();
          while (reader.hasNext()) {
            Font font = FontParser.parse(reader);
            fonts.put(font.getName(), font);
          }
          reader.endArray();
          break;
        default:
          reader.skipName();
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
      FontCharacter character = FontCharacterParser.parse(reader, composition);
      characters.put(character.hashCode(), character);
    }
    reader.endArray();
  }

  private static final JsonReader.Options MARKER_NAMES = JsonReader.Options.of(
      "cm",
      "tm",
      "dr"
  );

  private static void parseMarkers(
      JsonReader reader, LottieComposition composition, List<Marker> markers) throws IOException {
    reader.beginArray();
    while (reader.hasNext()) {
      String comment = null;
      float frame = 0f;
      float durationFrames = 0f;
      reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.selectName(MARKER_NAMES)) {
          case 0:
            comment = reader.nextString();
            break;
          case 1:
            frame = (float) reader.nextDouble();
            break;
          case 2:
            durationFrames = (float) reader.nextDouble();
            break;
          default:
            reader.skipName();
            reader.skipValue();
        }
      }
      reader.endObject();
      markers.add(new Marker(comment, frame, durationFrames));
    }
    reader.endArray();
  }
}
