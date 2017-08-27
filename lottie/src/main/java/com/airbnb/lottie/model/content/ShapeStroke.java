package com.airbnb.lottie.model.content;

import android.graphics.Paint;
import android.support.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.StrokeContent;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.layer.BaseLayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShapeStroke implements ContentModel {
  public enum LineCapType {
    Butt,
    Round,
    Unknown;

    public Paint.Cap toPaintCap() {
      switch (this) {
        case Butt:
          return Paint.Cap.BUTT;
        case Round:
          return Paint.Cap.ROUND;
        case Unknown:
        default:
          return Paint.Cap.SQUARE;
      }
    }
  }

  public enum LineJoinType {
    Miter,
    Round,
    Bevel;

    public Paint.Join toPaintJoin() {
      switch (this) {
        case Bevel:
          return Paint.Join.BEVEL;
        case Miter:
          return Paint.Join.MITER;
        case Round:
          return Paint.Join.ROUND;
      }
      return null;
    }
  }

  private final String name;
  @Nullable private final AnimatableFloatValue offset;
  private final List<AnimatableFloatValue> lineDashPattern;
  private final AnimatableColorValue color;
  private final AnimatableIntegerValue opacity;
  private final AnimatableFloatValue width;
  private final LineCapType capType;
  private final LineJoinType joinType;

  private ShapeStroke(String name, @Nullable AnimatableFloatValue offset,
      List<AnimatableFloatValue> lineDashPattern, AnimatableColorValue color,
      AnimatableIntegerValue opacity, AnimatableFloatValue width, LineCapType capType,
      LineJoinType joinType) {
    this.name = name;
    this.offset = offset;
    this.lineDashPattern = lineDashPattern;
    this.color = color;
    this.opacity = opacity;
    this.width = width;
    this.capType = capType;
    this.joinType = joinType;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new StrokeContent(drawable, layer, this);
  }

  static class Factory {
    private Factory() {
    }

    static ShapeStroke newInstance(JSONObject json, LottieComposition composition) {
      final String name = json.optString("nm");
      List<AnimatableFloatValue> lineDashPattern = new ArrayList<>();
      AnimatableColorValue color = AnimatableColorValue.Factory.newInstance(json.optJSONObject("c"),
          composition);
      AnimatableFloatValue width = AnimatableFloatValue.Factory.newInstance(json.optJSONObject("w"),
          composition);
      AnimatableIntegerValue opacity = AnimatableIntegerValue.Factory.newInstance(
          json.optJSONObject("o"), composition);
      LineCapType capType = LineCapType.values()[json.optInt("lc") - 1];
      LineJoinType joinType = LineJoinType.values()[json.optInt("lj") - 1];
      AnimatableFloatValue offset = null;

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
      return new ShapeStroke(name, offset, lineDashPattern, color, opacity, width, capType,
          joinType);
    }
  }

  public String getName() {
    return name;
  }

  public AnimatableColorValue getColor() {
    return color;
  }

  public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  public AnimatableFloatValue getWidth() {
    return width;
  }

  public List<AnimatableFloatValue> getLineDashPattern() {
    return lineDashPattern;
  }

  public AnimatableFloatValue getDashOffset() {
    return offset;
  }

  public LineCapType getCapType() {
    return capType;
  }

  public LineJoinType getJoinType() {
    return joinType;
  }
}
