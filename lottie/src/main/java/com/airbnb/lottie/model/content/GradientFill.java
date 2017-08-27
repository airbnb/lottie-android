package com.airbnb.lottie.model.content;

import android.graphics.Path;
import android.support.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.GradientFillContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.layer.BaseLayer;

import org.json.JSONException;
import org.json.JSONObject;

public class GradientFill implements ContentModel {

  private final GradientType gradientType;
  private final Path.FillType fillType;
  private final AnimatableGradientColorValue gradientColor;
  private final AnimatableIntegerValue opacity;
  private final AnimatablePointValue startPoint;
  private final AnimatablePointValue endPoint;
  private final String name;
  @Nullable private final AnimatableFloatValue highlightLength;
  @Nullable private final AnimatableFloatValue highlightAngle;

  private GradientFill(String name, GradientType gradientType, Path.FillType fillType,
      AnimatableGradientColorValue gradientColor,
      AnimatableIntegerValue opacity, AnimatablePointValue startPoint,
      AnimatablePointValue endPoint, AnimatableFloatValue highlightLength,
      AnimatableFloatValue highlightAngle) {
    this.gradientType = gradientType;
    this.fillType = fillType;
    this.gradientColor = gradientColor;
    this.opacity = opacity;
    this.startPoint = startPoint;
    this.endPoint = endPoint;
    this.name = name;
    this.highlightLength = highlightLength;
    this.highlightAngle = highlightAngle;
  }

  public String getName() {
    return name;
  }

  public GradientType getGradientType() {
    return gradientType;
  }

  public Path.FillType getFillType() {
    return fillType;
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

  @Nullable AnimatableFloatValue getHighlightLength() {
    return highlightLength;
  }

  @Nullable AnimatableFloatValue getHighlightAngle() {
    return highlightAngle;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new GradientFillContent(drawable, layer, this);
  }

  static class Factory {
    private Factory() {
    }

    static GradientFill newInstance(JSONObject json, LottieComposition composition) {
      final String name = json.optString("nm");

      JSONObject jsonColor = json.optJSONObject("g");
      if (jsonColor != null && jsonColor.has("k")) {
        // This is a hack because the "p" value which contains the number of color points is outside
        // of "k" which contains the useful data.
        int points = jsonColor.optInt("p");
        jsonColor = jsonColor.optJSONObject("k");
        try {
          jsonColor.put("p", points);
        } catch (JSONException e) {
          // Do nothing. This shouldn't fail.
        }
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

      int fillTypeInt = json.optInt("r", 1);
      Path.FillType fillType = fillTypeInt == 1 ? Path.FillType.WINDING : Path.FillType.EVEN_ODD;

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

      return new GradientFill(name, gradientType, fillType, color, opacity, startPoint, endPoint,
          null, null);
    }
  }
}
