package com.airbnb.lottie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class ShapeStroke {
  enum LineCapType {
    Butt,
    Round,
    Unknown
  }

  enum LineJoinType {
    Miter,
    Round,
    Bevel
  }

  private AnimatableFloatValue offset;
  private final List<AnimatableFloatValue> lineDashPattern = new ArrayList<>();

  private final AnimatableColorValue color;
  private final AnimatableIntegerValue opacity;
  private final AnimatableFloatValue width;
  private final LineCapType capType;
  private final LineJoinType joinType;

  ShapeStroke(JSONObject json, LottieComposition composition) {
    try {
      JSONObject colorJson = json.getJSONObject("c");
      color = new AnimatableColorValue(colorJson, composition);

      JSONObject widthJson = json.getJSONObject("w");
      width = new AnimatableFloatValue(widthJson, composition);

      JSONObject opacityJson = json.getJSONObject("o");
      opacity = new AnimatableIntegerValue(opacityJson, composition, false, true);

      capType = LineCapType.values()[json.getInt("lc") - 1];
      joinType = LineJoinType.values()[json.getInt("lj") - 1];

      if (json.has("d")) {
        JSONArray dashesJson = json.getJSONArray("d");
        for (int i = 0; i < dashesJson.length(); i++) {
          JSONObject dashJson = dashesJson.getJSONObject(i);
          String n = dashJson.getString("n");
          if (n.equals("o")) {
            JSONObject value = dashJson.getJSONObject("v");
            offset = new AnimatableFloatValue(value, composition);
          } else if (n.equals("d") || n.equals("g")) {
            JSONObject value = dashJson.getJSONObject("v");
            lineDashPattern.add(new AnimatableFloatValue(value, composition));
          }
        }
        if (lineDashPattern.size() == 1) {
          // If there is only 1 value then it is assumed to be equal parts on and off.
          lineDashPattern.add(lineDashPattern.get(0));
        }
      }
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse stroke " + json, e);
    }
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
