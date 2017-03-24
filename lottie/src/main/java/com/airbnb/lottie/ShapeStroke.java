package com.airbnb.lottie;

import android.graphics.Paint;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class ShapeStroke {
  enum LineCapType {
    Butt,
    Round,
    Unknown;

    Paint.Cap toPaintCap() {
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

  enum LineJoinType {
    Miter,
    Round,
    Bevel;

    Paint.Join toPaintJoin() {
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

  String getName() {
    return name;
  }

  AnimatableColorValue getColor() {
    return color;
  }

  AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  AnimatableFloatValue getWidth() {
    return width;
  }

  List<AnimatableFloatValue> getLineDashPattern() {
    return lineDashPattern;
  }

  AnimatableFloatValue getDashOffset() {
    return offset;
  }

  LineCapType getCapType() {
    return capType;
  }

  LineJoinType getJoinType() {
    return joinType;
  }
}
