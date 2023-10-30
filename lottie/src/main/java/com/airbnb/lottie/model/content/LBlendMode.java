package com.airbnb.lottie.model.content;

import android.graphics.BlendMode;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Lottie BlendMode,
 * not to be confused with Paint.BlendMode in android graphics core,
 * which we will rely on for rendering.
 */
public enum LBlendMode {
  NORMAL,
  MULTIPLY,
  SCREEN,
  OVERLAY,
  DARKEN,
  LIGHTEN,
  COLOR_DODGE,
  COLOR_BURN,
  HARD_LIGHT,
  SOFT_LIGHT,
  DIFFERENCE,
  EXCLUSION,
  HUE,
  SATURATION,
  COLOR,
  LUMINOSITY,
  ADD,
  HARD_MIX;

  @Nullable
  @RequiresApi(api = Build.VERSION_CODES.Q)
  public BlendMode toCoreBlendMode() {
    switch (this){
      case NORMAL:
        return null; // BlendMode.CLEAR?
      case MULTIPLY:
        return BlendMode.MULTIPLY;
      case SCREEN:
        return BlendMode.SCREEN;
      case OVERLAY:
        return BlendMode.OVERLAY;
      case DARKEN:
        return BlendMode.DARKEN;
      case LIGHTEN:
        return BlendMode.LIGHTEN;
      case COLOR_DODGE:
        return BlendMode.COLOR_DODGE;
      case COLOR_BURN:
        return BlendMode.COLOR_BURN;
      case HARD_LIGHT:
        return BlendMode.HARD_LIGHT;
      case SOFT_LIGHT:
        return BlendMode.SOFT_LIGHT;
      case DIFFERENCE:
        return BlendMode.DIFFERENCE;
      case EXCLUSION:
        return BlendMode.EXCLUSION;
      case HUE:
        return BlendMode.HUE;
      case SATURATION:
        return BlendMode.SATURATION;
      case COLOR:
        return BlendMode.COLOR;
      case LUMINOSITY:
        return BlendMode.LUMINOSITY;
      case ADD:
        return BlendMode.PLUS;
      case HARD_MIX:
        return null;  // todo: fix this
      default:
        return null;
    }
  }

}
