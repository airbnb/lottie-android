package com.airbnb.lottie;

import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

class Layer {
  private static final String TAG = Layer.class.getSimpleName();

  enum LayerType {
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

  private final List<Object> shapes;
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
  private final float startProgress;
  private final int preCompWidth;
  private final int preCompHeight;
  private final List<Keyframe<Float>> inOutKeyframes;
  private final MatteType matteType;

  private Layer(List<Object> shapes, LottieComposition composition, String layerName, long layerId,
      LayerType layerType, long parentId, @Nullable String refId, List<Mask> masks,
      AnimatableTransform transform, int solidWidth, int solidHeight, int solidColor,
      float timeStretch, float startProgress, int preCompWidth, int preCompHeight,
      List<Keyframe<Float>> inOutKeyframes, MatteType matteType) {
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
    this.startProgress = startProgress;
    this.preCompWidth = preCompWidth;
    this.preCompHeight = preCompHeight;
    this.inOutKeyframes = inOutKeyframes;
    this.matteType = matteType;
  }

  LottieComposition getComposition() {
    return composition;
  }

  float getTimeStretch() {
    return timeStretch;
  }

  float getStartProgress() {
    return startProgress;
  }

  List<Keyframe<Float>> getInOutKeyframes() {
    return inOutKeyframes;
  }

  long getId() {
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

  LayerType getLayerType() {
    return layerType;
  }

  MatteType getMatteType() {
    return matteType;
  }

  long getParentId() {
    return parentId;
  }

  List<Object> getShapes() {
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

  @Override public String toString() {
    return toString("");
  }

  String toString(String prefix) {
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

  static class Factory {
    private Factory() {
    }

    static Layer newInstance(LottieComposition composition) {
      // TODO: make sure in out keyframes work
      Rect bounds = composition.getBounds();
      return new Layer(
          Collections.emptyList(), composition, null, -1, LayerType.PreComp, -1, null,
          Collections.<Mask>emptyList(), AnimatableTransform.Factory.newInstance(),
          0, 0, 0, 0, 0,
          bounds.width(), bounds.height(), Collections.<Keyframe<Float>>emptyList(), MatteType
          .None);
    }

    static Layer newInstance(JSONObject json, LottieComposition composition) {
      String layerName = json.optString("nm");
      String refId = json.optString("refId");
      long layerId = json.optLong("ind");
      int solidWidth = 0;
      int solidHeight = 0;
      int solidColor = 0;
      int preCompWidth = 0;
      int preCompHeight = 0;
      LayerType layerType;
      int layerTypeInt = json.optInt("ty", -1);
      if (layerTypeInt < LayerType.Unknown.ordinal()) {
        layerType = LayerType.values()[layerTypeInt];
      } else {
        layerType = LayerType.Unknown;
      }

      long parentId = json.optLong("parent", -1);

      if (layerType == LayerType.Solid) {
        solidWidth = (int) (json.optInt("sw") * composition.getDpScale());
        solidHeight = (int) (json.optInt("sh") * composition.getDpScale());
        solidColor = Color.parseColor(json.optString("sc"));
        if (L.DBG) {
          Log.d(TAG, "\tSolid=" + Integer.toHexString(solidColor) + " " +
              solidWidth + "x" + solidHeight + " " + composition.getBounds());
        }
      }

      AnimatableTransform transform = AnimatableTransform.Factory.newInstance(json.optJSONObject("ks"),
          composition);
      MatteType matteType = MatteType.values()[json.optInt("tt")];
      List<Object> shapes = new ArrayList<>();
      List<Mask> masks = new ArrayList<>();
      List<Keyframe<Float>> inOutKeyframes = new ArrayList<>();
      JSONArray jsonMasks = json.optJSONArray("masksProperties");
      if (jsonMasks != null) {
        for (int i = 0; i < jsonMasks.length(); i++) {
          Mask mask = Mask.Factory.newMask(jsonMasks.optJSONObject(i), composition);
          masks.add(mask);
        }
      }

      JSONArray shapesJson = json.optJSONArray("shapes");
      if (shapesJson != null) {
        for (int i = 0; i < shapesJson.length(); i++) {
          Object shape = ShapeGroup.shapeItemWithJson(shapesJson.optJSONObject(i), composition);
          if (shape != null) {
            shapes.add(shape);
          }
        }
      }

      float timeStretch = (float) json.optDouble("sr", 1.0);
      float startFrame = (float) json.optDouble("st");
      float frames = composition.getDurationFrames();
      float startProgress = startFrame / frames;

      if (layerType == LayerType.PreComp) {
        preCompWidth = (int) (json.optInt("w") * composition.getDpScale());
        preCompHeight = (int) (json.optInt("h") * composition.getDpScale());
      }

      float inFrame = json.optLong("ip");
      float outFrame = json.optLong("op");

      // Before the in frame
      if (inFrame > 0) {
        Keyframe<Float> preKeyframe = new Keyframe<>(composition, 0f, 0f, null, 0f, inFrame);
        inOutKeyframes.add(preKeyframe);
      }

      // The + 1 is because the animation should be visible on the out frame itself.
      outFrame = (outFrame > 0 ? outFrame : composition.getEndFrame() + 1);
      Keyframe<Float> visibleKeyframe =
          new Keyframe<>(composition, 1f, 1f, null, inFrame, outFrame);
      inOutKeyframes.add(visibleKeyframe);

      if (outFrame <= composition.getDurationFrames()) {
        Keyframe<Float> outKeyframe =
            new Keyframe<>(composition, 0f, 0f, null, outFrame, (float) composition.getEndFrame());
        inOutKeyframes.add(outKeyframe);
      }

      return new Layer(shapes, composition, layerName, layerId, layerType, parentId, refId,
          masks, transform, solidWidth, solidHeight, solidColor, timeStretch, startProgress,
          preCompWidth, preCompHeight, inOutKeyframes, matteType);
    }
  }
}
