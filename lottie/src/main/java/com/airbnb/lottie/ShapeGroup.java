package com.airbnb.lottie;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ShapeGroup implements ContentModel {
  @Nullable static ContentModel shapeItemWithJson(JSONObject json, LottieComposition composition) {
    String type = json.optString("ty");

    switch (type) {
      case "gr":
        return ShapeGroup.Factory.newInstance(json, composition);
      case "st":
        return ShapeStroke.Factory.newInstance(json, composition);
      case "gs":
        return GradientStroke.Factory.newInstance(json, composition);
      case "fl":
        return ShapeFill.Factory.newInstance(json, composition);
      case "gf":
        return GradientFill.Factory.newInstance(json, composition);
      case "tr":
        return AnimatableTransform.Factory.newInstance(json, composition);
      case "sh":
        return ShapePath.Factory.newInstance(json, composition);
      case "el":
        return CircleShape.Factory.newInstance(json, composition);
      case "rc":
        return RectangleShape.Factory.newInstance(json, composition);
      case "tm":
        return ShapeTrimPath.Factory.newInstance(json, composition);
      case "sr":
        return PolystarShape.Factory.newInstance(json, composition);
      case "mm":
        return MergePaths.Factory.newInstance(json);
      case "rp":
        return Repeater.Factory.newInstance(json, composition);
      default:
        Log.w(L.TAG, "Unknown shape type " + type);
    }
    return null;
  }

  private final String name;
  private final List<ContentModel> items;

  ShapeGroup(String name, List<ContentModel> items) {
    this.name = name;
    this.items = items;
  }

  static class Factory {
    private Factory() {
    }

    private static ShapeGroup newInstance(JSONObject json, LottieComposition composition) {
      JSONArray jsonItems = json.optJSONArray("it");
      String name = json.optString("nm");
      List<ContentModel> items = new ArrayList<>();

      for (int i = 0; i < jsonItems.length(); i++) {
        ContentModel newItem = shapeItemWithJson(jsonItems.optJSONObject(i), composition);
        if (newItem != null) {
          items.add(newItem);
        }
      }
      return new ShapeGroup(name, items);
    }
  }

  public String getName() {
    return name;
  }

  List<ContentModel> getItems() {
    return items;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new ContentGroup(drawable, layer, this);
  }

  @Override public String toString() {
    return "ShapeGroup{" + "name='" + name + "\' Shapes: " + Arrays.toString(items.toArray()) + '}';
  }
}
