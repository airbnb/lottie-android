package com.airbnb.lottie;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ShapeGroup {
  private static final String TAG = ShapeGroup.class.getSimpleName();

  @Nullable
  static Object shapeItemWithJson(JSONObject json, LottieComposition composition)
      throws JSONException {
    String type = null;
    try {
      type = json.getString("ty");
    } catch (JSONException e) {
      // Do nothing.
    }
    if (type == null) {
      throw new IllegalStateException("Shape has no type.");
    }

    switch (type) {
      case "gr":
        return new ShapeGroup(json, composition);
      case "st":
        return new ShapeStroke(json, composition);
      case "fl":
        return new ShapeFill(json, composition);
      case "tr":
        return new ShapeTransform(json, composition);
      case "sh":
        return new ShapePath(json, composition);
      case "el":
        return new CircleShape(json, composition);
      case "rc":
        return new RectangleShape(json, composition);
      case "tm":
        return new ShapeTrimPath(json, composition);
      case "sr":
        Log.w(TAG, "Lottie doesn't yet support polystars. Convert your layer to a shape first.");
    }
    return null;
  }

  private String name;
  private final List<Object> items = new ArrayList<>();

  private ShapeGroup(JSONObject json, LottieComposition composition) throws JSONException {
    JSONArray jsonItems = null;
    try {
      jsonItems = json.getJSONArray("it");
    } catch (JSONException e) {
      // Do nothing.
    }
    if (jsonItems == null) {
      // Thought this was necessary but maybe not?
      // throw new IllegalStateException("There are no items.");
      jsonItems = new JSONArray();
    }

    try {
      name = json.getString("nm");
    } catch (JSONException e) {
      // Do nothing.
    }

    for (int i = 0; i < jsonItems.length(); i++) {
      JSONObject jsonItem = null;
      try {
        jsonItem = jsonItems.getJSONObject(i);
      } catch (JSONException e) {
        // Do nothing.
      }
      if (jsonItem == null) {
        throw new IllegalStateException("Unable to get jsonItem");
      }


      Object newItem = shapeItemWithJson(jsonItem, composition);
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
