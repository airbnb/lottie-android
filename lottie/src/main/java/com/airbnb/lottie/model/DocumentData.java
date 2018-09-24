package com.airbnb.lottie.model;

import androidx.annotation.ColorInt;
import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class DocumentData {

  public final String text;
  @SuppressWarnings("WeakerAccess") public final String fontName;
  public final double size;
  @SuppressWarnings("WeakerAccess") final int justification;
  public final int tracking;
  @SuppressWarnings("WeakerAccess") final double lineHeight;
  public final double baselineShift;
  @ColorInt public final int color;
  @ColorInt public final int strokeColor;
  public final double strokeWidth;
  public final boolean strokeOverFill;


  public DocumentData(String text, String fontName, double size, int justification, int tracking,
      double lineHeight, double baselineShift, @ColorInt int color, @ColorInt int strokeColor,
      double strokeWidth, boolean strokeOverFill) {
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
