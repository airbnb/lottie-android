package com.airbnb.lottie.parser;

import android.graphics.Color;
import android.graphics.Rect;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableTextFrame;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.content.LBlendMode;
import com.airbnb.lottie.model.content.BlurEffect;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.content.Mask;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.Keyframe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LayerParser {

  private LayerParser() {
  }

  private static final JsonReader.Options NAMES = JsonReader.Options.of(
      "nm",     // 0
      "ind",    // 1
      "refId",  // 2
      "ty",     // 3
      "parent", // 4
      "sw",     // 5
      "sh",     // 6
      "sc",     // 7
      "ks",     // 8
      "tt",     // 9
      "masksProperties", // 10
      "shapes", // 11
      "t",  // 12
      "ef", // 13
      "sr", // 14
      "st", // 15
      "w",  // 16
      "h",  // 17
      "ip", // 18
      "op", // 19
      "tm", // 20
      "cl", // 21
      "hd", // 22
      "ao", // 23
      "bm"  // 24
  );

  public static Layer parse(LottieComposition composition) {
    Rect bounds = composition.getBounds();
    return new Layer(
        Collections.<ContentModel>emptyList(), composition, "__container", -1,
        Layer.LayerType.PRE_COMP, -1, null, Collections.<Mask>emptyList(),
        new AnimatableTransform(), 0, 0, 0, 0, 0,
        bounds.width(), bounds.height(), null, null, Collections.<Keyframe<Float>>emptyList(),
        Layer.MatteType.NONE, null, false, null, null,
        LBlendMode.NORMAL);
  }

  private static final JsonReader.Options TEXT_NAMES = JsonReader.Options.of(
      "d",
      "a"
  );

  private static final JsonReader.Options EFFECTS_NAMES = JsonReader.Options.of(
      "ty",
      "nm"
  );

  public static Layer parse(JsonReader reader, LottieComposition composition) throws IOException {
    // This should always be set by After Effects. However, if somebody wants to minify
    // and optimize their json, the name isn't critical for most cases so it can be removed.
    String layerName = "UNSET";
    Layer.LayerType layerType = null;
    String refId = null;
    long layerId = 0;
    int solidWidth = 0;
    int solidHeight = 0;
    int solidColor = 0;
    float preCompWidth = 0;
    float preCompHeight = 0;
    long parentId = -1;
    float timeStretch = 1f;
    float startFrame = 0f;
    float inFrame = 0f;
    float outFrame = 0f;
    String cl = null;
    boolean hidden = false;
    BlurEffect blurEffect = null;
    DropShadowEffect dropShadowEffect = null;
    boolean autoOrient = false;

    Layer.MatteType matteType = Layer.MatteType.NONE;
    LBlendMode blendMode = LBlendMode.NORMAL;
    AnimatableTransform transform = null;
    AnimatableTextFrame text = null;
    AnimatableTextProperties textProperties = null;
    AnimatableFloatValue timeRemapping = null;

    List<Mask> masks = new ArrayList<>();
    List<ContentModel> shapes = new ArrayList<>();

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0:
          layerName = reader.nextString();
          break;
        case 1:
          layerId = reader.nextInt();
          break;
        case 2:
          refId = reader.nextString();
          break;
        case 3:
          int layerTypeInt = reader.nextInt();
          if (layerTypeInt < Layer.LayerType.UNKNOWN.ordinal()) {
            layerType = Layer.LayerType.values()[layerTypeInt];
          } else {
            layerType = Layer.LayerType.UNKNOWN;
          }
          break;
        case 4:
          parentId = reader.nextInt();
          break;
        case 5:
          solidWidth = (int) (reader.nextInt() * Utils.dpScale());
          break;
        case 6:
          solidHeight = (int) (reader.nextInt() * Utils.dpScale());
          break;
        case 7:
          solidColor = Color.parseColor(reader.nextString());
          break;
        case 8:
          transform = AnimatableTransformParser.parse(reader, composition);
          break;
        case 9:
          int matteTypeIndex = reader.nextInt();
          if (matteTypeIndex >= Layer.MatteType.values().length) {
            composition.addWarning("Unsupported matte type: " + matteTypeIndex);
            break;
          }
          matteType = Layer.MatteType.values()[matteTypeIndex];
          switch (matteType) {
            case LUMA:
              composition.addWarning("Unsupported matte type: Luma");
              break;
            case LUMA_INVERTED:
              composition.addWarning("Unsupported matte type: Luma Inverted");
              break;
          }
          composition.incrementMatteOrMaskCount(1);
          break;
        case 10:
          reader.beginArray();
          while (reader.hasNext()) {
            masks.add(MaskParser.parse(reader, composition));
          }
          composition.incrementMatteOrMaskCount(masks.size());
          reader.endArray();
          break;
        case 11:
          reader.beginArray();
          while (reader.hasNext()) {
            ContentModel shape = ContentModelParser.parse(reader, composition);
            if (shape != null) {
              shapes.add(shape);
            }
          }
          reader.endArray();
          break;
        case 12: // Text data
          reader.beginObject();
          while (reader.hasNext()) {
            switch (reader.selectName(TEXT_NAMES)) {
              case 0: // "d", Text contents
                text = AnimatableValueParser.parseDocumentData(reader, composition);
                break;
              case 1: // "a", Text ranges with custom animations and style
                reader.beginArray();
                if (reader.hasNext()) {
                  textProperties = AnimatableTextPropertiesParser.parse(reader, composition);
                }
                // TODO support more than one text range
                while (reader.hasNext()) {
                  reader.skipValue();
                }
                reader.endArray();
                break;
              // TODO support Text follow path and Text alignment
              default:
                reader.skipName();
                reader.skipValue();
            }
          }
          reader.endObject();
          break;
        case 13:
          reader.beginArray();
          List<String> effectNames = new ArrayList<>();
          while (reader.hasNext()) {
            reader.beginObject();
            while (reader.hasNext()) {
              switch (reader.selectName(EFFECTS_NAMES)) {
                case 0:
                  int type = reader.nextInt();
                  if (type == 29) {
                    blurEffect = BlurEffectParser.parse(reader, composition);
                  } else if (type == 25) {
                    dropShadowEffect = new DropShadowEffectParser().parse(reader, composition);
                  }
                  break;
                case 1:
                  String effectName = reader.nextString();
                  effectNames.add(effectName);
                  break;
                default:
                  reader.skipName();
                  reader.skipValue();

              }
            }
            reader.endObject();
          }
          reader.endArray();
          composition.addWarning("Lottie doesn't support layer effects. If you are using them for " +
              " fills, strokes, trim paths etc. then try adding them directly as contents " +
              " in your shape. Found: " + effectNames);
          break;
        case 14:
          timeStretch = (float) reader.nextDouble();
          break;
        case 15:
          startFrame = (float) reader.nextDouble();
          break;
        case 16:
          preCompWidth = (float) (reader.nextDouble() * Utils.dpScale());
          break;
        case 17:
          preCompHeight = (float) (reader.nextDouble() * Utils.dpScale());
          break;
        case 18:
          inFrame = (float) reader.nextDouble();
          break;
        case 19:
          outFrame = (float) reader.nextDouble();
          break;
        case 20:
          timeRemapping = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case 21:
          cl = reader.nextString();
          break;
        case 22:
          hidden = reader.nextBoolean();
          break;
        case 23:
          autoOrient = reader.nextInt() == 1;
          break;
        case 24:
          int blendModeIndex = reader.nextInt();
          if (blendModeIndex >= LBlendMode.values().length) {
            composition.addWarning("Unsupported Blend Mode: " + blendModeIndex);
            blendMode = LBlendMode.NORMAL;
            break;
          }
          blendMode = LBlendMode.values()[blendModeIndex];
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    reader.endObject();

    List<Keyframe<Float>> inOutKeyframes = new ArrayList<>();
    // Before the in frame
    if (inFrame > 0) {
      Keyframe<Float> preKeyframe = new Keyframe<>(composition, 0f, 0f, null, 0f, inFrame);
      inOutKeyframes.add(preKeyframe);
    }

    // The + 1 is because the animation should be visible on the out frame itself.
    outFrame = (outFrame > 0 ? outFrame : composition.getEndFrame());
    Keyframe<Float> visibleKeyframe =
        new Keyframe<>(composition, 1f, 1f, null, inFrame, outFrame);
    inOutKeyframes.add(visibleKeyframe);

    Keyframe<Float> outKeyframe = new Keyframe<>(
        composition, 0f, 0f, null, outFrame, Float.MAX_VALUE);
    inOutKeyframes.add(outKeyframe);

    if (layerName.endsWith(".ai") || "ai".equals(cl)) {
      composition.addWarning("Convert your Illustrator layers to shape layers.");
    }

    if (autoOrient) {
      if (transform == null) {
        transform = new AnimatableTransform();
      }
      transform.setAutoOrient(autoOrient);
    }
    return new Layer(shapes, composition, layerName, layerId, layerType, parentId, refId,
        masks, transform, solidWidth, solidHeight, solidColor, timeStretch, startFrame,
        preCompWidth, preCompHeight, text, textProperties, inOutKeyframes, matteType,
        timeRemapping, hidden, blurEffect, dropShadowEffect, blendMode);
  }
}
