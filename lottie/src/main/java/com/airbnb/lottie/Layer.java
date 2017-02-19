package com.airbnb.lottie;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class Layer {
  private static final String TAG = Layer.class.getSimpleName();
  private final LottieComposition composition;

  private enum LottieLayerType {
    None,
    Solid,
    Unknown,
    Null,
    Shape
  }

  enum MatteType {
    None,
    Add,
    Invert,
    Unknown
  }

  static Layer fromJson(JSONObject json, LottieComposition composition) {
    Layer layer = new Layer(composition);
    layer.layerName = json.optString("nm");
    layer.layerId = json.optLong("ind");
    layer.precompId = json.optString("refId");

    int layerType = json.optInt("ty", -1);
    if (layerType <= LottieLayerType.Shape.ordinal()) {
      layer.layerType = LottieLayerType.values()[layerType];
    } else {
      layer.layerType = LottieLayerType.Unknown;
    }

    layer.parentId = json.optLong("parent", -1);

    if (layer.layerType == LottieLayerType.Solid) {
      layer.solidWidth = (int) (json.optInt("sw") * composition.getScale());
      layer.solidHeight = (int) (json.optInt("sh") * composition.getScale());
      layer.solidColor = Color.parseColor(json.optString("sc"));
      if (L.DBG) {
        Log.d(TAG, "\tSolid=" + Integer.toHexString(layer.solidColor) + " " +
            layer.solidWidth + "x" + layer.solidHeight + " " + composition.getBounds());
      }
    }

    layer.transform = new AnimatableTransform(json.optJSONObject("ks"), composition);

    layer.matteType = MatteType.values()[json.optInt("tt")];

    JSONArray jsonMasks = json.optJSONArray("masksProperties");
    if (jsonMasks != null) {
      for (int i = 0; i < jsonMasks.length(); i++) {
        Mask mask = new Mask(jsonMasks.optJSONObject(i), composition);
        layer.masks.add(mask);
      }
    }

    JSONArray shapes = json.optJSONArray("shapes");
    if (shapes != null) {
      for (int i = 0; i < shapes.length(); i++) {
        Object shape = ShapeGroup.shapeItemWithJson(shapes.optJSONObject(i), composition);
        if (shape != null) {
          layer.shapes.add(shape);
        }
      }
    }

    long inFrame = json.optLong("ip");
    long outFrame = json.optLong("op");

    // Before the in frame
    if (inFrame > 0) {
      Keyframe<Float> preKeyframe = new Keyframe<>(composition, 0, inFrame);
      preKeyframe.startValue = 0f;
      preKeyframe.endValue = 0f;
      layer.inOutKeyframes.add(preKeyframe);
    }

    // The + 1 is because the animation should be visible on the out frame itself.
    outFrame = (outFrame > 0 ? outFrame : composition.getEndFrame() + 1);
    Keyframe<Float> visibleKeyframe =
        new Keyframe<>(composition, inFrame, outFrame);
    visibleKeyframe.startValue = 1f;
    visibleKeyframe.endValue = 1f;
    layer.inOutKeyframes.add(visibleKeyframe);

    if (outFrame <= composition.getDurationFrames()) {
      Keyframe<Float> outKeyframe =
          new Keyframe<>(composition, outFrame, composition.getEndFrame());
      outKeyframe.startValue = 0f;
      outKeyframe.endValue = 0f;
      layer.inOutKeyframes.add(outKeyframe);
    }

    return layer;
  }

  private final List<Object> shapes = new ArrayList<>();

  private String layerName;
  private long layerId;
  private LottieLayerType layerType;
  private long parentId;
  @Nullable private String precompId;

  private final List<Mask> masks = new ArrayList<>();

  private AnimatableTransform transform;
  private int solidWidth;
  private int solidHeight;
  private int solidColor;


  private final List<Keyframe<Float>> inOutKeyframes = new ArrayList<>();

  private MatteType matteType;

  private Layer(LottieComposition composition) {
    this.composition = composition;
  }

  LottieComposition getComposition() {
    return composition;
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

  @Nullable String getPrecompId() {
    return precompId;
  }

  List<Mask> getMasks() {
    return masks;
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
}
