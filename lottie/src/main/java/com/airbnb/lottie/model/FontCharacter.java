package com.airbnb.lottie.model;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.content.ShapeGroup;

import java.io.IOException;
import java.util.ArrayList;
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

  FontCharacter(List<ShapeGroup> shapes, char character, int size,
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

  public static class Factory {

    public static FontCharacter newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      char character = '\0';
      int size = 0;
      double width = 0;
      String style = null;
      String fontFamily = null;
      List<ShapeGroup> shapes = new ArrayList<>();

      reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "ch":
            character = reader.nextString().charAt(0);
            break;
          case "size":
            size = reader.nextInt();
            break;
          case "w":
            width = reader.nextDouble();
            break;
          case "style":
            style = reader.nextString();
            break;
          case "fFamily":
            fontFamily = reader.nextString();
            break;
          case "data":
            reader.beginObject();
            while (reader.hasNext()) {
              if ("shapes".equals(reader.nextName())) {
                reader.beginArray();
                while (reader.hasNext()) {
                  shapes.add((ShapeGroup) ShapeGroup.shapeItemWithJson(reader, composition));
                }
                reader.endArray();
              } else {
                reader.skipValue();
              }
            }
            reader.endObject();
            break;
          default:
            reader.skipValue();
        }
      }
      reader.endObject();

      return new FontCharacter(shapes, character, size, width, style, fontFamily);
    }
  }

  @Override public int hashCode() {
    return hashFor(character, fontFamily, style);
  }
}
