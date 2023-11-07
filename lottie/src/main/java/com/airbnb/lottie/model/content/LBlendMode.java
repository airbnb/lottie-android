package com.airbnb.lottie.model.content;

import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.BlendModeCompat;

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
  public BlendModeCompat toNativeBlendMode() {
    switch (this) {
      case NORMAL:
        return null; // BlendMode.CLEAR?
      case SCREEN:
        return BlendModeCompat.SCREEN;
      case OVERLAY:
        return BlendModeCompat.OVERLAY;
      case DARKEN:
        return BlendModeCompat.DARKEN;
      case LIGHTEN:
        return BlendModeCompat.LIGHTEN;
      case ADD:
        return BlendModeCompat.PLUS;
      default:
        // return toNativeBlendModeQ(); // enable via flag or wait for support
        return null;
    }
  }

  /**
   * BlendModes that are only supported on Q+
   * Placeholder, to be enabled via flag or
   * enabled when platform is more widely adopted
   */
  @Nullable
  @RequiresApi(api = Build.VERSION_CODES.Q)
  private BlendModeCompat toNativeBlendModeQ() {
    switch (this) {
      case MULTIPLY:
        return BlendModeCompat.MULTIPLY;
      case COLOR_DODGE:
        return BlendModeCompat.COLOR_DODGE;
      case COLOR_BURN:
        return BlendModeCompat.COLOR_BURN;
      case HARD_LIGHT:
        return BlendModeCompat.HARD_LIGHT;
      case SOFT_LIGHT:
        return BlendModeCompat.SOFT_LIGHT;
      case DIFFERENCE:
        return BlendModeCompat.DIFFERENCE;
      case EXCLUSION:
        return BlendModeCompat.EXCLUSION;
      case HUE:
        return BlendModeCompat.HUE;
      case SATURATION:
        return BlendModeCompat.SATURATION;
      case COLOR:
        return BlendModeCompat.COLOR;
      case LUMINOSITY:
        return BlendModeCompat.LUMINOSITY;
      case HARD_MIX: // present in Lottie, but requires platform support
        return null;
      default:
        return null;
    }
  }

}
