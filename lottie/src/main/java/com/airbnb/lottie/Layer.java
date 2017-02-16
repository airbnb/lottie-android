package com.airbnb.lottie;

import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class Layer implements Transform {
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

  static Layer fromJson(JSONObject json, LottieComposition composition) throws JSONException {
    Layer layer = new Layer(composition);
    layer.layerName = json.getString("nm");
    layer.layerId = json.getLong("ind");

    int layerType = json.getInt("ty");
    if (layerType <= LottieLayerType.Shape.ordinal()) {
      layer.layerType = LottieLayerType.values()[layerType];
    } else {
      layer.layerType = LottieLayerType.Unknown;
    }

    try {
      layer.parentId = json.getLong("parent");
    } catch (JSONException e) {
      // Do nothing.
    }

    if (layer.layerType == LottieLayerType.Solid) {
      layer.solidWidth = (int) (json.getInt("sw") * composition.getScale());
      layer.solidHeight = (int) (json.getInt("sh") * composition.getScale());
      layer.solidColor = Color.parseColor(json.getString("sc"));
      if (L.DBG) {
        Log.d(TAG, "\tSolid=" + Integer.toHexString(layer.solidColor) + " " +
            layer.solidWidth + "x" + layer.solidHeight + " " + composition.getBounds());
      }
    }

    JSONObject ks = json.getJSONObject("ks");

    JSONObject opacity = null;
    try {
      opacity = ks.getJSONObject("o");
    } catch (JSONException e) {
      // Do nothing.
    }
    if (opacity != null) {
      layer.opacity = new AnimatableIntegerValue(opacity, composition, false, true);
    }

    JSONObject rotation;
    try {
      rotation = ks.getJSONObject("r");
    } catch (JSONException e) {
      rotation = ks.getJSONObject("rz");
    }

    if (rotation != null) {
      layer.rotation = new AnimatableFloatValue(rotation, composition, false);
    }

    JSONObject position = null;
    try {
      position = ks.getJSONObject("p");
    } catch (JSONException e) {
      // Do nothing.
    }
    if (position != null) {
      layer.position = AnimatablePathValue.createAnimatablePathOrSplitDimensionPath(position, composition);
    }

    JSONObject anchor = null;
    try {
      anchor = ks.getJSONObject("a");
    } catch (JSONException e) {
      // DO nothing.
    }
    if (anchor != null) {
      layer.anchor = new AnimatablePathValue(anchor.get("k"), composition);
    }

    JSONObject scale = null;
    try {
      scale = ks.getJSONObject("s");
    } catch (JSONException e) {
      // Do nothing.
    }
    if (scale != null) {
      layer.scale = new AnimatableScaleValue(scale, composition, false);
    }

    try {
      layer.matteType = MatteType.values()[json.getInt("tt")];
    } catch (JSONException e) {
      // Do nothing.
    }

    JSONArray jsonMasks = null;
    try {
      jsonMasks = json.getJSONArray("masksProperties");
    } catch (JSONException e) {
      // Do nothing.
    }
    if (jsonMasks != null) {
      for (int i = 0; i < jsonMasks.length(); i++) {
        Mask mask = new Mask(jsonMasks.getJSONObject(i), composition);
        layer.masks.add(mask);
      }
    }

    JSONArray shapes = null;
    try {
      shapes = json.getJSONArray("shapes");
    } catch (JSONException e) {
      // Do nothing.
    }
    if (shapes != null) {
      for (int i = 0; i < shapes.length(); i++) {
        Object shape = ShapeGroup.shapeItemWithJson(shapes.getJSONObject(i), composition);
        if (shape != null) {
          layer.shapes.add(shape);
        }
      }
    }

    long inFrame = json.getLong("ip");
    long outFrame = json.getLong("op");

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
  private long parentId = -1;

  private final List<Mask> masks = new ArrayList<>();

  private int solidWidth;
  private int solidHeight;
  private int solidColor;

  private AnimatableIntegerValue opacity;
  private AnimatableFloatValue rotation;
  private IAnimatablePathValue position;

  private AnimatablePathValue anchor;
  private AnimatableScaleValue scale;

  private final List<Keyframe<Float>> inOutKeyframes = new ArrayList<>();

  private MatteType matteType;

  private Layer(LottieComposition composition) {
    this.composition = composition;
  }

  @Override public Rect getBounds() {
    return composition.getBounds();
  }

  @Override public AnimatablePathValue getAnchor() {
    return anchor;
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

  List<Mask> getMasks() {
    return masks;
  }

  MatteType getMatteType() {
    return matteType;
  }

  @Override public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  long getParentId() {
    return parentId;
  }

  @Override public IAnimatablePathValue getPosition() {
    return position;
  }

  @Override public AnimatableFloatValue getRotation() {
    return rotation;
  }

  @Override public AnimatableScaleValue getScale() {
    return scale;
  }

  List<Object> getShapes() {
    return shapes;
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
    if (getPosition().hasAnimation() || getPosition().getInitialPoint().length() != 0) {
      sb.append(prefix).append("\tPosition: ").append(getPosition()).append("\n");
    }
    if (getRotation().hasAnimation() || getRotation().getInitialValue() != 0f) {
      sb.append(prefix).append("\tRotation: ").append(getRotation()).append("\n");
    }
    if (getScale().hasAnimation() || !getScale().getInitialValue().isDefault()) {
      sb.append(prefix).append("\tScale: ").append(getScale()).append("\n");
    }
    if (getAnchor().hasAnimation() || getAnchor().getInitialPoint().length() != 0) {
      sb.append(prefix).append("\tAnchor: ").append(getAnchor()).append("\n");
    }
    if (!getMasks().isEmpty()) {
      sb.append(prefix).append("\tMasks: ").append(getMasks().size()).append("\n");
    }
    if (getSolidWidth() != 0 && getSolidHeight() != 0) {
      sb.append(prefix).append("\tBackground: ").append(String.format(Locale.US, "%dx%d %X\n", getSolidWidth(), getSolidHeight(), getSolidColor()));
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
