package com.airbnb.lottie;

import org.json.JSONArray;
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
    color = new AnimatableColorValue(json.optJSONObject("c"), composition);

    width = new AnimatableFloatValue(json.optJSONObject("w"), composition);

    opacity = new AnimatableIntegerValue(json.optJSONObject("o"), composition, false, true);

    capType = LineCapType.values()[json.optInt("lc") - 1];
    joinType = LineJoinType.values()[json.optInt("lj") - 1];

    if (json.has("d")) {
      JSONArray dashesJson = json.optJSONArray("d");
      for (int i = 0; i < dashesJson.length(); i++) {
        JSONObject dashJson = dashesJson.optJSONObject(i);
        String n = dashJson.optString("n");
        if (n.equals("o")) {
          JSONObject value = dashJson.optJSONObject("v");
          offset = new AnimatableFloatValue(value, composition);
        } else if (n.equals("d") || n.equals("g")) {
          JSONObject value = dashJson.optJSONObject("v");
          lineDashPattern.add(new AnimatableFloatValue(value, composition));
        }
      }
      if (lineDashPattern.size() == 1) {
        // If there is only 1 value then it is assumed to be equal parts on and off.
        lineDashPattern.add(lineDashPattern.get(0));
      }
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
