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

  private final List<Object> shapes = new ArrayList<>();
  private final LottieComposition composition;

  private final String layerName;
  private final long layerId;
  private final LayerType layerType;
  private final long parentId;
  @Nullable private final String precompId;

  private final List<Mask> masks = new ArrayList<>();

  private final AnimatableTransform transform;
  private int solidWidth;
  private int solidHeight;
  private int solidColor;

  private final float timeStretch;
  private final float startProgress;
  private float preCompStartProgress;
  private int preCompWidth;
  private int preCompHeight;

  private final List<Keyframe<Float>> inOutKeyframes = new ArrayList<>();

  private MatteType matteType;

  Layer(JSONObject json, LottieComposition composition) {
    this.composition = composition;
    layerName = json.optString("nm");
    layerId = json.optLong("ind");
    precompId = json.optString("refId");

    int layerTypeInt = json.optInt("ty", -1);
    if (layerTypeInt < LayerType.Unknown.ordinal()) {
      layerType = LayerType.values()[layerTypeInt];
    } else {
      layerType = LayerType.Unknown;
    }

    parentId = json.optLong("parent", -1);

    if (layerType == LayerType.Solid) {
      solidWidth = (int) (json.optInt("sw") * composition.getScale());
      solidHeight = (int) (json.optInt("sh") * composition.getScale());
      solidColor = Color.parseColor(json.optString("sc"));
      if (L.DBG) {
        Log.d(TAG, "\tSolid=" + Integer.toHexString(solidColor) + " " +
            solidWidth + "x" + solidHeight + " " + composition.getBounds());
      }
    }

    transform = new AnimatableTransform(json.optJSONObject("ks"), composition);

    matteType = MatteType.values()[json.optInt("tt")];

    JSONArray jsonMasks = json.optJSONArray("masksProperties");
    if (jsonMasks != null) {
      for (int i = 0; i < jsonMasks.length(); i++) {
        Mask mask = new Mask(jsonMasks.optJSONObject(i), composition);
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

    timeStretch = (float) json.optDouble("sr", 1.0);
    float startFrame = (float) json.optDouble("st");
    float frames = composition.getDurationFrames();
    startProgress = startFrame / frames;

    if (layerType == LayerType.PreComp) {
      preCompWidth = (int) (json.optInt("w") * composition.getScale());
      preCompHeight = (int) (json.optInt("h") * composition.getScale());
    }

    long inFrame = json.optLong("ip");
    long outFrame = json.optLong("op");

    // Before the in frame
    if (inFrame > 0) {
      Keyframe<Float> preKeyframe = new Keyframe<>(composition, 0, inFrame);
      preKeyframe.startValue = 0f;
      preKeyframe.endValue = 0f;
      inOutKeyframes.add(preKeyframe);
    }

    // The + 1 is because the animation should be visible on the out frame itself.
    outFrame = (outFrame > 0 ? outFrame : composition.getEndFrame() + 1);
    Keyframe<Float> visibleKeyframe =
        new Keyframe<>(composition, inFrame, outFrame);
    visibleKeyframe.startValue = 1f;
    visibleKeyframe.endValue = 1f;
    inOutKeyframes.add(visibleKeyframe);

    if (outFrame <= composition.getDurationFrames()) {
      Keyframe<Float> outKeyframe =
          new Keyframe<>(composition, outFrame, composition.getEndFrame());
      outKeyframe.startValue = 0f;
      outKeyframe.endValue = 0f;
      inOutKeyframes.add(outKeyframe);
    }
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

  @Nullable String getPrecompId() {
    return precompId;
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
}
