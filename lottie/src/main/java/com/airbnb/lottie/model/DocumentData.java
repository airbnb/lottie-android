package com.airbnb.lottie.model;

import android.support.annotation.ColorInt;

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


  public DocumentData(String text, String fontName, double size, int justification, int tracking,
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
