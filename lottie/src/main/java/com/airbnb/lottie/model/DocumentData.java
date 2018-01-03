package com.airbnb.lottie.model;

import android.support.annotation.ColorInt;
import android.util.JsonReader;

import com.airbnb.lottie.utils.JsonUtils;

import java.io.IOException;

public class DocumentData {

  public String text;
  @SuppressWarnings("WeakerAccess") public String fontName;
  public double size;
  @SuppressWarnings("WeakerAccess") int justification;
  public int tracking;
  @SuppressWarnings("WeakerAccess") double lineHeight;
  public double baselineShift;
  @ColorInt public int color;
  @ColorInt public int strokeColor;
  public int strokeWidth;
  public boolean strokeOverFill;


  DocumentData(String text, String fontName, double size, int justification, int tracking,
      double lineHeight, double baselineShift, @ColorInt int color, @ColorInt int strokeColor,
      int strokeWidth, boolean strokeOverFill) {
    this.text = text;
    this.fontName = fontName;
    this.size = size;
    this.justification = justification;
    this.tracking = tracking;
    this.lineHeight = lineHeight;
    this.baselineShift = baselineShift;
    this.color = color;
    this.strokeColor = strokeColor;
    this.strokeWidth = strokeWidth;
    this.strokeOverFill = strokeOverFill;
  }


  public static final class Factory {

    private Factory() {
    }

    public static DocumentData newInstance(JsonReader reader) throws IOException {
      String text = null;
      String fontName = null;
      double size = 0;
      int justification = 0;
      int tracking = 0;
      double lineHeight = 0;
      double baselineShift = 0;
      int fillColor = 0;
      int strokeColor = 0;
      int strokeWidth = 0;
      boolean strokeOverFill = true;

      reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "t":
            text = reader.nextString();
            break;
          case "f":
            fontName = reader.nextString();
            break;
          case "s":
            size = reader.nextDouble();
            break;
          case "j":
            justification = reader.nextInt();
            break;
          case "tr":
            tracking = reader.nextInt();
            break;
          case "lh":
            lineHeight = reader.nextDouble();
            break;
          case "ls":
            baselineShift = reader.nextDouble();
            break;
          case "fc":
            fillColor = JsonUtils.jsonToColor(reader);
            break;
          case "sc":
            strokeColor = JsonUtils.jsonToColor(reader);
            break;
          case "sw":
            strokeWidth = reader.nextInt();
            break;
          case "of":
            strokeOverFill = reader.nextBoolean();
            break;
          default:
            reader.skipValue();
        }
      }
      reader.endObject();

      return new DocumentData(text, fontName, size, justification, tracking, lineHeight,
          baselineShift, fillColor, strokeColor, strokeWidth, strokeOverFill);
    }
  }

  @Override public int hashCode() {
    int result;
    long temp;
    result = text.hashCode();
    result = 31 * result + fontName.hashCode();
    result = (int) (31 * result + size);
    result = 31 * result + justification;
    result = 31 * result + tracking;
    temp = Double.doubleToLongBits(lineHeight);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + color;
    return result;
  }
}
