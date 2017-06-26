package com.airbnb.lottie;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class FontCharacter {

  static int hashFor(char character, String fontFamily, String style) {
    int result = 0;
    result = 31 * result + (int) character;
    result = 31 * result + fontFamily.hashCode();
    result = 31 * result + style.hashCode();
    return result;
  }

  private final List<ShapeGroup> shapes;
  private final char character;
  private final int size;
  private final double width;
  private final String style;
  private final String fontFamily;

  FontCharacter(List<ShapeGroup> shapes, char character, int size,
      double width, String style, String fontFamily) {
    this.shapes = shapes;
    this.character = character;
    this.size = size;
    this.width = width;
    this.style = style;
    this.fontFamily = fontFamily;
  }

  List<ShapeGroup> getShapes() {
    return shapes;
  }

  int getSize() {
    return size;
  }

  double getWidth() {
    return width;
  }

  String getStyle() {
    return style;
  }

  static class Factory {

    static FontCharacter newInstance(JSONObject json, LottieComposition composition) {
      char character = json.optString("ch").charAt(0);
      int size = json.optInt("size");
      double width = json.optDouble("w");
      String style = json.optString("style");
      String fontFamily = json.optString("fFamily");
      JSONArray shapesJson = json.optJSONObject("data").optJSONArray("shapes");
      List<ShapeGroup> shapes = Collections.emptyList();
      if (shapesJson != null) {
        shapes = new ArrayList<>(shapesJson.length());
        for (int i = 0; i < shapesJson.length(); i++) {
          shapes.add(
              (ShapeGroup) ShapeGroup.shapeItemWithJson(shapesJson.optJSONObject(i), composition));
        }
      }
      return new FontCharacter(shapes, character, size, width, style, fontFamily);
    }
  }

  @Override public int hashCode() {
    return hashFor(character, fontFamily, style);
  }
}
