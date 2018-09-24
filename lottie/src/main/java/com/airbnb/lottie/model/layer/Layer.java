package com.airbnb.lottie.model.layer;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableTextFrame;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.content.Mask;

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

  public enum MatteType {
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

  public Layer(List<ContentModel> shapes, LottieComposition composition, String layerName, long layerId,
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
}
