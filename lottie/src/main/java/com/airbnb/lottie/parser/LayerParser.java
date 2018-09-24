package com.airbnb.lottie.parser;

import android.graphics.Color;
import android.graphics.Rect;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableTextFrame;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.content.Mask;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LayerParser {

  private LayerParser() {}

  public static Layer parse(LottieComposition composition) {
    Rect bounds = composition.getBounds();
    return new Layer(
        Collections.<ContentModel>emptyList(), composition, "__container", -1,
        Layer.LayerType.PreComp, -1, null, Collections.<Mask>emptyList(),
        new AnimatableTransform(), 0, 0, 0, 0, 0,
        bounds.width(), bounds.height(), null, null, Collections.<Keyframe<Float>>emptyList(),
        Layer.MatteType.None, null);
  }

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
    int preCompWidth = 0;
    int preCompHeight = 0;
    long parentId = -1;
    float timeStretch = 1f;
    float startFrame = 0f;
    float inFrame = 0f;
    float outFrame = 0f;
    String cl = null;

    Layer.MatteType matteType = Layer.MatteType.None;
    AnimatableTransform transform = null;
    AnimatableTextFrame text = null;
    AnimatableTextProperties textProperties = null;
    AnimatableFloatValue timeRemapping = null;

    List<Mask> masks = new ArrayList<>();
    List<ContentModel> shapes = new ArrayList<>();

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "nm":
          layerName = reader.nextString();
          break;
        case "ind":
          layerId = reader.nextInt();
          break;
        case "refId":
          refId = reader.nextString();
          break;
        case "ty":
          int layerTypeInt = reader.nextInt();
          if (layerTypeInt < Layer.LayerType.Unknown.ordinal()) {
            layerType = Layer.LayerType.values()[layerTypeInt];
          } else {
            layerType = Layer.LayerType.Unknown;
          }
          break;
        case "parent":
          parentId = reader.nextInt();
          break;
        case "sw":
          solidWidth = (int) (reader.nextInt() * Utils.dpScale());
          break;
        case "sh":
          solidHeight = (int) (reader.nextInt() * Utils.dpScale());
          break;
        case "sc":
          solidColor = Color.parseColor(reader.nextString());
          break;
        case "ks":
          transform = AnimatableTransformParser.parse(reader, composition);
          break;
        case "tt":
          matteType = Layer.MatteType.values()[reader.nextInt()];
          break;
        case "masksProperties":
          reader.beginArray();
          while (reader.hasNext()) {
            masks.add(MaskParser.parse(reader, composition));
          }
          reader.endArray();
          break;
        case "shapes":
          reader.beginArray();
          while (reader.hasNext()) {
            ContentModel shape = ContentModelParser.parse(reader, composition);
            if (shape != null) {
              shapes.add(shape);
            }
          }
          reader.endArray();
          break;
        case "t":
          reader.beginObject();
          while (reader.hasNext()) {
            switch (reader.nextName()) {
              case "d":
                text = AnimatableValueParser.parseDocumentData(reader, composition);
                break;
              case "a":
                reader.beginArray();
                if (reader.hasNext()) {
                  textProperties = AnimatableTextPropertiesParser.parse(reader, composition);
                }
                while (reader.hasNext()) {
                  reader.skipValue();
                }
                reader.endArray();
                break;
              default:
                reader.skipValue();
            }
          }
          reader.endObject();
          break;
        case "ef":
          reader.beginArray();
          List<String> effectNames = new ArrayList<>();
          while (reader.hasNext()) {
            reader.beginObject();
            while (reader.hasNext()) {
              switch (reader.nextName()) {
                case "nm":
                  effectNames.add(reader.nextString());
                  break;
                default:
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
        case "sr":
          timeStretch = (float) reader.nextDouble();
          break;
        case "st":
          startFrame = (float) reader.nextDouble();
          break;
        case "w":
          preCompWidth = (int) (reader.nextInt() * Utils.dpScale());
          break;
        case "h":
          preCompHeight = (int) (reader.nextInt() * Utils.dpScale());
          break;
        case "ip":
          inFrame = (float) reader.nextDouble();
          break;
        case "op":
          outFrame = (float) reader.nextDouble();
          break;
        case "tm":
          timeRemapping = AnimatableValueParser.parseFloat(reader, composition, false);
          break;
        case "cl":
          cl = reader.nextString();
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();

    // Bodymovin pre-scales the in frame and out frame by the time stretch. However, that will
    // cause the stretch to be double counted since the in out animation gets treated the same
    // as all other animations and will have stretch applied to it again.
    inFrame /= timeStretch;
    outFrame /= timeStretch;

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

    return new Layer(shapes, composition, layerName, layerId, layerType, parentId, refId,
        masks, transform, solidWidth, solidHeight, solidColor, timeStretch, startFrame,
        preCompWidth, preCompHeight, text, textProperties, inOutKeyframes, matteType,
        timeRemapping);
  }
}
