package com.airbnb.lottie.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import org.json.JSONArray;
import org.json.JSONObject;

public class DocumentData {

  public String text;
  @SuppressWarnings("WeakerAccess") public String fontName;
  public int size;
  @SuppressWarnings("WeakerAccess") int justification;
  public int tracking;
  @SuppressWarnings("WeakerAccess") double lineHeight;
  public double baselineShift;
  @ColorInt public int color;
  @ColorInt public int strokeColor;
  public int strokeWidth;
  public boolean strokeOverFill;


  DocumentData(String text, String fontName, int size, int justification, int tracking,
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

    public static DocumentData newInstance(JSONObject json) {
      String text = json.optString("t");
      String fontName = json.optString("f");
      int size = json.optInt("s");
      int justification = json.optInt("j");
      int tracking = json.optInt("tr");
      double lineHeight = json.optDouble("lh");
      double baselineShift = json.optDouble("ls");
      JSONArray colorArray = json.optJSONArray("fc");
      int color = Color.argb(
          255,
          (int) (colorArray.optDouble(0) * 255),
          (int) (colorArray.optDouble(1) * 255),
          (int) (colorArray.optDouble(2) * 255));
      JSONArray strokeArray = json.optJSONArray("sc");
      int strokeColor = 0;
      if (strokeArray != null) {
        strokeColor = Color.argb(
            255,
            (int) (strokeArray.optDouble(0) * 255),
            (int) (strokeArray.optDouble(1) * 255),
            (int) (strokeArray.optDouble(2) * 255));
      }

      int strokeWidth = json.optInt("sw");
      boolean strokeOverFill = json.optBoolean("of");

      return new DocumentData(text, fontName, size, justification, tracking, lineHeight,
          baselineShift, color, strokeColor, strokeWidth, strokeOverFill);
    }
  }

  @Override public int hashCode() {
    int result;
    long temp;
    result = text.hashCode();
    result = 31 * result + fontName.hashCode();
    result = 31 * result + size;
    result = 31 * result + justification;
    result = 31 * result + tracking;
    temp = Double.doubleToLongBits(lineHeight);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + color;
    return result;
  }
}
