package com.airbnb.lottie.model;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.graphics.Typeface;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

@RestrictTo(LIBRARY)
public class Font {

  private final String family;
  private final String name;
  private final String style;
  private final float ascent;

  @Nullable
  private Typeface typeface;

  public Font(String family, String name, String style, float ascent) {
    this.family = family;
    this.name = name;
    this.style = style;
    this.ascent = ascent;
  }

  @SuppressWarnings("unused") public String getFamily() {
    return family;
  }

  public String getName() {
    return name;
  }

  public String getStyle() {
    return style;
  }

  @SuppressWarnings("unused") float getAscent() {
    return ascent;
  }

  @Nullable
  public Typeface getTypeface() {
    return typeface;
  }

  public void setTypeface(@Nullable Typeface typeface) {
    this.typeface = typeface;
  }
}
