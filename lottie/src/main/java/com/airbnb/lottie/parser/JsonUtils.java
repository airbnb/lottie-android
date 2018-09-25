package com.airbnb.lottie.parser;

import android.graphics.Color;
import android.graphics.PointF;
import androidx.annotation.ColorInt;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class JsonUtils {
  private JsonUtils() {
  }

  /**
   * [r,g,b]
   */
  @ColorInt static int jsonToColor(JsonReader reader) throws IOException {
    reader.beginArray();
    int r = (int) (reader.nextDouble() * 255);
    int g = (int) (reader.nextDouble() * 255);
    int b = (int) (reader.nextDouble() * 255);
    while (reader.hasNext()) {
      reader.skipValue();
    }
    reader.endArray();
    return Color.argb(255, r, g, b);
  }

  static List<PointF> jsonToPoints(JsonReader reader, float scale) throws IOException {
    List<PointF> points = new ArrayList<>();

    reader.beginArray();
    while (reader.peek() == JsonToken.BEGIN_ARRAY) {
      reader.beginArray();
      points.add(jsonToPoint(reader, scale));
      reader.endArray();
    }
    reader.endArray();
    return points;
  }

  static PointF jsonToPoint(JsonReader reader, float scale) throws IOException {
    switch (reader.peek()) {
      case NUMBER: return jsonNumbersToPoint(reader, scale);
      case BEGIN_ARRAY: return jsonArrayToPoint(reader, scale);
      case BEGIN_OBJECT: return jsonObjectToPoint(reader, scale);
      default: throw new IllegalArgumentException("Unknown point starts with " + reader.peek());
    }
  }

  private static PointF jsonNumbersToPoint(JsonReader reader, float scale) throws IOException {
    float x = (float) reader.nextDouble();
    float y = (float) reader.nextDouble();
    while (reader.hasNext()) {
      reader.skipValue();
    }
    return new PointF(x * scale, y * scale);
  }

  private static PointF jsonArrayToPoint(JsonReader reader, float scale) throws IOException {
    float x;
    float y;
    reader.beginArray();
    x = (float) reader.nextDouble();
    y = (float) reader.nextDouble();
    while (reader.peek() != JsonToken.END_ARRAY) {
      reader.skipValue();
    }
    reader.endArray();
    return new PointF(x * scale, y * scale);
  }

  private static PointF jsonObjectToPoint(JsonReader reader, float scale) throws IOException {
    float x = 0f;
    float y = 0f;
    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "x":
          x = valueFromObject(reader);
          break;
        case "y":
          y = valueFromObject(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return new PointF(x * scale, y * scale);
  }

  static float valueFromObject(JsonReader reader) throws IOException {
    JsonToken token = reader.peek();
    switch (token) {
      case NUMBER:
        return (float) reader.nextDouble();
      case BEGIN_ARRAY:
        reader.beginArray();
        float val = (float) reader.nextDouble();
        while (reader.hasNext()) {
          reader.skipValue();
        }
        reader.endArray();
        return val;
      default:
        throw new IllegalArgumentException("Unknown value for token of type " + token);
    }
  }
}
