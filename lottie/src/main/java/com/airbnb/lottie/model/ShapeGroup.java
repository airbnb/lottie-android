package com.airbnb.lottie.model;

import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class ShapeGroup {

  @Nullable
  static Object shapeItemWithJson(JSONObject json, int framerate, LottieComposition composition) {
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
        return new ShapeGroup(json, framerate, composition);
      case "st":
        return new ShapeStroke(json, framerate, composition);
      case "fl":
        return new ShapeFill(json, framerate, composition);
      case "tr":
        return new ShapeTransform(json, framerate, composition);
      case "sh":
        return new ShapePath(json, framerate, composition);
      case "el":
        return new CircleShape(json, framerate, composition);
      case "rc":
        return new RectangleShape(json, framerate, composition);
      case "tm":
        return new ShapeTrimPath(json, framerate, composition);
    }
    return null;
  }

  private String name;
  private final List<Object> items = new ArrayList<>();

  private ShapeGroup(JSONObject json, int frameRate, LottieComposition composition) {
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


      Object newItem = shapeItemWithJson(jsonItem, frameRate, composition);
      if (newItem != null) {
        items.add(newItem);
      }
    }
  }

  public List<Object> getItems() {
    return items;
  }

  @Override
  public String toString() {
    return "ShapeGroup{" + "name='" + name + "\' Shapes: " + Arrays.toString(items.toArray()) + '}';
  }
}
