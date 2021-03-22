package com.airbnb.lottie.model;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import androidx.annotation.ColorInt;
import androidx.annotation.RestrictTo;

@RestrictTo(LIBRARY)
public class DocumentData {

  public enum Justification {
    LEFT_ALIGN,
    RIGHT_ALIGN,
    CENTER
  }

  public final String text;
  @SuppressWarnings("WeakerAccess") public final String fontName;
  public final float size;
  @SuppressWarnings("WeakerAccess") public final Justification justification;
  public final int tracking;
  @SuppressWarnings("WeakerAccess") public final float lineHeight;
  public final float baselineShift;
  @ColorInt public final int color;
  @ColorInt public final int strokeColor;
  public final float strokeWidth;
  public final boolean strokeOverFill;


  public DocumentData(String text, String fontName, float size, Justification justification, int tracking,
      float lineHeight, float baselineShift, @ColorInt int color, @ColorInt int strokeColor,
      float strokeWidth, boolean strokeOverFill) {
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
    result = 31 * result + justification.ordinal();
    result = 31 * result + tracking;
    temp = Float.floatToRawIntBits(lineHeight);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + color;
    return result;
  }
}
