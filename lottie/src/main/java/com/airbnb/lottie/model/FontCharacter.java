package com.airbnb.lottie.model;

import com.airbnb.lottie.model.content.ShapeGroup;

import java.util.List;

public class FontCharacter {

  public static int hashFor(char character, String fontFamily, String style) {
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

  public FontCharacter(List<ShapeGroup> shapes, char character, int size,
      double width, String style, String fontFamily) {
    this.shapes = shapes;
    this.character = character;
    this.size = size;
    this.width = width;
    this.style = style;
    this.fontFamily = fontFamily;
  }

  public List<ShapeGroup> getShapes() {
    return shapes;
  }

  int getSize() {
    return size;
  }

  public double getWidth() {
    return width;
  }

  String getStyle() {
    return style;
  }

  @Override public int hashCode() {
    return hashFor(character, fontFamily, style);
  }
}
