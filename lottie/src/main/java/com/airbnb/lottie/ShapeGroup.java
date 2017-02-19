package com.airbnb.lottie;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ShapeGroup {

  @Nullable
  static Object shapeItemWithJson(JSONObject json, LottieComposition composition) {
    String type = json.optString("ty");

    switch (type) {
      case "gr":
        return new ShapeGroup(json, composition);
      case "st":
        return new ShapeStroke(json, composition);
      case "fl":
        return new ShapeFill(json, composition);
      case "tr":
        return new AnimatableTransform(json, composition);
      case "sh":
        return new ShapePath(json, composition);
      case "el":
        return new CircleShape(json, composition);
      case "rc":
        return new RectangleShape(json, composition);
      case "tm":
        return new ShapeTrimPath(json, composition);
      case "sr":
        return new PolystarShape(json, composition);
      default:
        Log.w(L.TAG, "Unknown shape type " + type);
    }
    return null;
  }

  private String name;
  private final List<Object> items = new ArrayList<>();

  private ShapeGroup(JSONObject json, LottieComposition composition) {
    JSONArray jsonItems =  json.optJSONArray("it");
    name = json.optString("nm");

    for (int i = 0; i < jsonItems.length(); i++) {
      Object newItem = shapeItemWithJson(jsonItems.optJSONObject(i), composition);
      if (newItem != null) {
        items.add(newItem);
      }
    }
  }

  List<Object> getItems() {
    return items;
  }

  @Override
  public String toString() {
    return "ShapeGroup{" + "name='" + name + "\' Shapes: " + Arrays.toString(items.toArray()) + '}';
  }
}
