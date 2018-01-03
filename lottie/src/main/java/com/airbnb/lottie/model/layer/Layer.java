package com.airbnb.lottie.model.layer;

import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableTextFrame;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.content.Mask;
import com.airbnb.lottie.model.content.ShapeGroup;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Layer {

  public enum LayerType {
    PreComp,
    Solid,
    Image,
    Null,
    Shape,
    Text,
    Unknown
  }

  enum MatteType {
    None,
    Add,
    Invert,
    Unknown
  }

  private final List<ContentModel> shapes;
  private final LottieComposition composition;
  private final String layerName;
  private final long layerId;
  private final LayerType layerType;
  private final long parentId;
  @Nullable private final String refId;
  private final List<Mask> masks;
  private final AnimatableTransform transform;
  private final int solidWidth;
  private final int solidHeight;
  private final int solidColor;
  private final float timeStretch;
  private final float startFrame;
  private final int preCompWidth;
  private final int preCompHeight;
  @Nullable private final AnimatableTextFrame text;
  @Nullable private final AnimatableTextProperties textProperties;
  @Nullable private final AnimatableFloatValue timeRemapping;
  private final List<Keyframe<Float>> inOutKeyframes;
  private final MatteType matteType;

  private Layer(List<ContentModel> shapes, LottieComposition composition, String layerName, long layerId,
      LayerType layerType, long parentId, @Nullable String refId, List<Mask> masks,
      AnimatableTransform transform, int solidWidth, int solidHeight, int solidColor,
      float timeStretch, float startFrame, int preCompWidth, int preCompHeight,
      @Nullable AnimatableTextFrame text, @Nullable AnimatableTextProperties textProperties,
      List<Keyframe<Float>> inOutKeyframes, MatteType matteType,
      @Nullable AnimatableFloatValue timeRemapping) {
    this.shapes = shapes;
    this.composition = composition;
    this.layerName = layerName;
    this.layerId = layerId;
    this.layerType = layerType;
    this.parentId = parentId;
    this.refId = refId;
    this.masks = masks;
    this.transform = transform;
    this.solidWidth = solidWidth;
    this.solidHeight = solidHeight;
    this.solidColor = solidColor;
    this.timeStretch = timeStretch;
    this.startFrame = startFrame;
    this.preCompWidth = preCompWidth;
    this.preCompHeight = preCompHeight;
    this.text = text;
    this.textProperties = textProperties;
    this.inOutKeyframes = inOutKeyframes;
    this.matteType = matteType;
    this.timeRemapping = timeRemapping;
  }

  LottieComposition getComposition() {
    return composition;
  }

  float getTimeStretch() {
    return timeStretch;
  }

  float getStartProgress() {
    return startFrame / composition.getDurationFrames();
  }

  List<Keyframe<Float>> getInOutKeyframes() {
    return inOutKeyframes;
  }

  public long getId() {
    return layerId;
  }

  String getName() {
    return layerName;
  }

  @Nullable String getRefId() {
    return refId;
  }

  int getPreCompWidth() {
    return preCompWidth;
  }

  int getPreCompHeight() {
    return preCompHeight;
  }

  List<Mask> getMasks() {
    return masks;
  }

  public LayerType getLayerType() {
    return layerType;
  }

  MatteType getMatteType() {
    return matteType;
  }

  long getParentId() {
    return parentId;
  }

  List<ContentModel> getShapes() {
    return shapes;
  }

  AnimatableTransform getTransform() {
    return transform;
  }

  int getSolidColor() {
    return solidColor;
  }

  int getSolidHeight() {
    return solidHeight;
  }

  int getSolidWidth() {
    return solidWidth;
  }

  @Nullable AnimatableTextFrame getText() {
    return text;
  }

  @Nullable AnimatableTextProperties getTextProperties() {
    return textProperties;
  }

  @Nullable AnimatableFloatValue getTimeRemapping() {
    return timeRemapping;
  }

  @Override public String toString() {
    return toString("");
  }

  public String toString(String prefix) {
    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append(getName()).append("\n");
    Layer parent = composition.layerModelForId(getParentId());
    if (parent != null) {
      sb.append("\t\tParents: ").append(parent.getName());
      parent = composition.layerModelForId(parent.getParentId());
      while (parent != null) {
        sb.append("->").append(parent.getName());
        parent = composition.layerModelForId(parent.getParentId());
      }
      sb.append(prefix).append("\n");
    }
    if (!getMasks().isEmpty()) {
      sb.append(prefix).append("\tMasks: ").append(getMasks().size()).append("\n");
    }
    if (getSolidWidth() != 0 && getSolidHeight() != 0) {
      sb.append(prefix).append("\tBackground: ").append(String
          .format(Locale.US, "%dx%d %X\n", getSolidWidth(), getSolidHeight(), getSolidColor()));
    }
    if (!shapes.isEmpty()) {
      sb.append(prefix).append("\tShapes:\n");
      for (Object shape : shapes) {
        sb.append(prefix).append("\t\t").append(shape).append("\n");
      }
    }
    return sb.toString();
  }

  public static class Factory {
    private Factory() {
    }

    public static Layer newInstance(LottieComposition composition) {
      Rect bounds = composition.getBounds();
      return new Layer(
          Collections.<ContentModel>emptyList(), composition, "__container", -1,
          LayerType.PreComp, -1, null, Collections.<Mask>emptyList(),
          AnimatableTransform.Factory.newInstance(), 0, 0, 0, 0, 0,
          bounds.width(), bounds.height(), null, null, Collections.<Keyframe<Float>>emptyList(),
          MatteType.None, null);
    }

    public static Layer newInstance(
        JsonReader reader, LottieComposition composition) throws IOException{
      String layerName = null;
      LayerType layerType = null;
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

      MatteType matteType = MatteType.None;
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
            if (layerTypeInt < LayerType.Unknown.ordinal()) {
              layerType = LayerType.values()[layerTypeInt];
            } else {
              layerType = LayerType.Unknown;
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
            transform = AnimatableTransform.Factory.newInstance(reader, composition);
            break;
          case "tt":
            matteType = MatteType.values()[reader.nextInt()];
            break;
          case "masksProperties":
            reader.beginArray();
            while (reader.hasNext()) {
              masks.add(Mask.Factory.newMask(reader, composition));
            }
            reader.endArray();
            break;
          case "shapes":
            reader.beginArray();
            while (reader.hasNext()) {
              ContentModel shape = ShapeGroup.shapeItemWithJson(reader, composition);
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
                  text = AnimatableTextFrame.Factory.newInstance(reader, composition);
                  break;
                case "a":
                  reader.beginArray();
                  if (reader.hasNext()) {
                    textProperties = AnimatableTextProperties.Factory.newInstance(reader, composition);
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
            timeRemapping = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
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
      outFrame = (outFrame > 0 ? outFrame : composition.getEndFrame()) + 1;
      Keyframe<Float> visibleKeyframe =
          new Keyframe<>(composition, 1f, 1f, null, inFrame, outFrame);
      inOutKeyframes.add(visibleKeyframe);

      Keyframe<Float> outKeyframe = new Keyframe<>(
          composition, 0f, 0f, null, outFrame, Float.MAX_VALUE);
      inOutKeyframes.add(outKeyframe);

      if (layerName.endsWith(".ai") || "ai".equals(cl)) {
        composition.addWarning("Convert your Illustrator layers to shape layers.");
      }

      if (layerType == LayerType.Text && !Utils.isAtLeastVersion(composition, 4, 8, 0)) {
        layerType = LayerType.Unknown;
        composition.addWarning("Text is only supported on bodymovin >= 4.8.0");
      }

      return new Layer(shapes, composition, layerName, layerId, layerType, parentId, refId,
          masks, transform, solidWidth, solidHeight, solidColor, timeStretch, startFrame,
          preCompWidth, preCompHeight, text, textProperties, inOutKeyframes, matteType,
          timeRemapping);
    }
  }
}
