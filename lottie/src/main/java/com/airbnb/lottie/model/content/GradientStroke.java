package com.airbnb.lottie.model.content;

import android.support.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.GradientStrokeContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.layer.BaseLayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GradientStroke implements ContentModel {

  private final String name;
  private final GradientType gradientType;
  private final AnimatableGradientColorValue gradientColor;
  private final AnimatableIntegerValue opacity;
  private final AnimatablePointValue startPoint;
  private final AnimatablePointValue endPoint;
  private final AnimatableFloatValue width;
  private final ShapeStroke.LineCapType capType;
  private final ShapeStroke.LineJoinType joinType;
  private final List<AnimatableFloatValue> lineDashPattern;
  @Nullable private final AnimatableFloatValue dashOffset;

  private GradientStroke(String name, GradientType gradientType,
      AnimatableGradientColorValue gradientColor,
      AnimatableIntegerValue opacity, AnimatablePointValue startPoint,
      AnimatablePointValue endPoint, AnimatableFloatValue width, ShapeStroke.LineCapType capType,
      ShapeStroke.LineJoinType joinType, List<AnimatableFloatValue> lineDashPattern,
      @Nullable AnimatableFloatValue dashOffset) {
    this.name = name;
    this.gradientType = gradientType;
    this.gradientColor = gradientColor;
    this.opacity = opacity;
    this.startPoint = startPoint;
    this.endPoint = endPoint;
    this.width = width;
    this.capType = capType;
    this.joinType = joinType;
    this.lineDashPattern = lineDashPattern;
    this.dashOffset = dashOffset;
  }

  public String getName() {
    return name;
  }

  public GradientType getGradientType() {
    return gradientType;
  }

  public AnimatableGradientColorValue getGradientColor() {
    return gradientColor;
  }

  public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  public AnimatablePointValue getStartPoint() {
    return startPoint;
  }

  public AnimatablePointValue getEndPoint() {
    return endPoint;
  }

  public AnimatableFloatValue getWidth() {
    return width;
  }

  public ShapeStroke.LineCapType getCapType() {
    return capType;
  }

  public ShapeStroke.LineJoinType getJoinType() {
    return joinType;
  }

  public List<AnimatableFloatValue> getLineDashPattern() {
    return lineDashPattern;
  }

  @Nullable public AnimatableFloatValue getDashOffset() {
    return dashOffset;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new GradientStrokeContent(drawable, layer, this);
  }

  static class Factory {
    private Factory() {
    }

    static GradientStroke newInstance(JSONObject json, LottieComposition composition) {
      final String name = json.optString("nm");
      JSONObject jsonColor = json.optJSONObject("g");
      if (jsonColor != null && jsonColor.has("k")) {
        jsonColor = jsonColor.optJSONObject("k");
      }
      AnimatableGradientColorValue color = null;
      if (jsonColor != null) {
        color = AnimatableGradientColorValue.Factory.newInstance(jsonColor, composition);
      }

      JSONObject jsonOpacity = json.optJSONObject("o");
      AnimatableIntegerValue opacity = null;
      if (jsonOpacity != null) {
        opacity = AnimatableIntegerValue.Factory.newInstance(jsonOpacity, composition);
      }

      int gradientTypeInt = json.optInt("t", 1);
      GradientType gradientType = gradientTypeInt == 1 ? GradientType.Linear : GradientType.Radial;

      JSONObject jsonStartPoint = json.optJSONObject("s");
      AnimatablePointValue startPoint = null;
      if (jsonStartPoint != null) {
        startPoint = AnimatablePointValue.Factory.newInstance(jsonStartPoint, composition);
      }

      JSONObject jsonEndPoint = json.optJSONObject("e");
      AnimatablePointValue endPoint = null;
      if (jsonEndPoint != null) {
        endPoint = AnimatablePointValue.Factory.newInstance(jsonEndPoint, composition);
      }
      AnimatableFloatValue width = AnimatableFloatValue.Factory.newInstance(json.optJSONObject("w"),
          composition);


      ShapeStroke.LineCapType capType = ShapeStroke.LineCapType.values()[json.optInt("lc") - 1];
      ShapeStroke.LineJoinType joinType = ShapeStroke.LineJoinType.values()[json.optInt("lj") - 1];

      AnimatableFloatValue offset = null;
      List<AnimatableFloatValue> lineDashPattern = new ArrayList<>();
      if (json.has("d")) {
        JSONArray dashesJson = json.optJSONArray("d");
        for (int i = 0; i < dashesJson.length(); i++) {
          JSONObject dashJson = dashesJson.optJSONObject(i);
          String n = dashJson.optString("n");
          if (n.equals("o")) {
            JSONObject value = dashJson.optJSONObject("v");
            offset = AnimatableFloatValue.Factory.newInstance(value, composition);
          } else if (n.equals("d") || n.equals("g")) {
            JSONObject value = dashJson.optJSONObject("v");
            lineDashPattern.add(AnimatableFloatValue.Factory.newInstance(value, composition));
          }
        }
        if (lineDashPattern.size() == 1) {
          // If there is only 1 value then it is assumed to be equal parts on and off.
          lineDashPattern.add(lineDashPattern.get(0));
        }
      }

      return new GradientStroke(
          name, gradientType, color, opacity, startPoint, endPoint, width, capType, joinType,
          lineDashPattern, offset);
    }
  }
}
