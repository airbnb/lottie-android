package com.airbnb.lottie.model.layer;

import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;

import com.airbnb.lottie.L;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Layer {
  private static final String TAG = Layer.class.getSimpleName();

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
  private final float startProgress;
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
      float timeStretch, float startProgress, int preCompWidth, int preCompHeight,
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
    this.startProgress = startProgress;
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
    return startProgress;
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
          Collections.<ContentModel>emptyList(), composition, "root", -1,
          LayerType.PreComp, -1, null, Collections.<Mask>emptyList(),
          AnimatableTransform.Factory.newInstance(), 0, 0, 0, 0, 0,
          bounds.width(), bounds.height(), null, null, Collections.<Keyframe<Float>>emptyList(),
          MatteType.None, null);
    }

    public static Layer newInstance(JSONObject json, LottieComposition composition) {
      String layerName = json.optString("nm");
      String refId = json.optString("refId");

      if (layerName.endsWith(".ai") || json.optString("cl", "").equals("ai")) {
        composition.addWarning("Convert your Illustrator layers to shape layers.");
      }

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

      if (layerType == LayerType.Text && !Utils.isAtLeastVersion(composition, 4, 8, 0)) {
        layerType = LayerType.Unknown;
        composition.addWarning("Text is only supported on bodymovin >= 4.8.0");
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
      List<Mask> masks = new ArrayList<>();
      JSONArray jsonMasks = json.optJSONArray("masksProperties");
      if (jsonMasks != null) {
        for (int i = 0; i < jsonMasks.length(); i++) {
          Mask mask = Mask.Factory.newMask(jsonMasks.optJSONObject(i), composition);
          masks.add(mask);
        }
      }

      List<ContentModel> shapes = new ArrayList<>();
      JSONArray shapesJson = json.optJSONArray("shapes");
      if (shapesJson != null) {
        for (int i = 0; i < shapesJson.length(); i++) {
          ContentModel shape = ShapeGroup.shapeItemWithJson(shapesJson.optJSONObject(i), composition);
          if (shape != null) {
            shapes.add(shape);
          }
        }
      }

      AnimatableTextFrame text = null;
      AnimatableTextProperties textProperties = null;
      JSONObject textJson = json.optJSONObject("t");
      if (textJson != null) {
        text = AnimatableTextFrame.Factory.newInstance(textJson.optJSONObject("d"), composition);
        JSONObject propertiesJson = textJson.optJSONArray("a").optJSONObject(0);
        textProperties = AnimatableTextProperties.Factory.newInstance(propertiesJson, composition);
      }

      if (json.has("ef")) {
        JSONArray effects = json.optJSONArray("ef");
        String[] effectNames = new String[effects.length()];
        for (int i = 0; i < effects.length(); i++) {
          effectNames[i] = effects.optJSONObject(i).optString("nm");
        }
        composition.addWarning("Lottie doesn't support layer effects. If you are using them for " +
            " fills, strokes, trim paths etc. then try adding them directly as contents " +
            " in your shape. Found: " + Arrays.toString(effectNames));
      }

      float timeStretch = (float) json.optDouble("sr", 1.0);
      float startFrame = (float) json.optDouble("st");
      float frames = composition.getDurationFrames();
      float startProgress = startFrame / frames;

      if (layerType == LayerType.PreComp) {
        preCompWidth = (int) (json.optInt("w") * composition.getDpScale());
        preCompHeight = (int) (json.optInt("h") * composition.getDpScale());
      }

      // Bodymovin pre-scales the in frame and out frame by the time stretch. However, that will
      // cause the stretch to be double counted since the in out animation gets treated the same
      // as all other animations and will have stretch applied to it again.
      float inFrame = json.optLong("ip") / timeStretch;
      float outFrame = json.optLong("op") / timeStretch;

      List<Keyframe<Float>> inOutKeyframes = new ArrayList<>();
      // Before the in frame
      if (inFrame > 0) {
        Keyframe<Float> preKeyframe = new Keyframe<>(composition, 0f, 0f, null, 0f, inFrame);
        inOutKeyframes.add(preKeyframe);
      }

      // The animation should not be visible on the out frame itself.
      outFrame = (outFrame > 0 ? outFrame : composition.getEndFrame());
      Keyframe<Float> visibleKeyframe =
          new Keyframe<>(composition, 1f, 1f, null, inFrame, outFrame);
      inOutKeyframes.add(visibleKeyframe);

      Keyframe<Float> outKeyframe = new Keyframe<>(
          composition, 0f, 0f, null, outFrame, Float.MAX_VALUE);
      inOutKeyframes.add(outKeyframe);

      AnimatableFloatValue timeRemapping = null;
      if (json.has("tm")) {
        timeRemapping =
            AnimatableFloatValue.Factory.newInstance(json.optJSONObject("tm"), composition, false);
      }

      return new Layer(shapes, composition, layerName, layerId, layerType, parentId, refId,
          masks, transform, solidWidth, solidHeight, solidColor, timeStretch, startProgress,
          preCompWidth, preCompHeight, text, textProperties, inOutKeyframes, matteType,
          timeRemapping);
    }
  }
}
