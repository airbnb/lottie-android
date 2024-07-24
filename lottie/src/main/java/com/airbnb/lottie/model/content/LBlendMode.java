package com.airbnb.lottie.model.content;

import androidx.annotation.Nullable;
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
        return null;
      case MULTIPLY:
        // BlendModeCompat.MULTIPLY does not exist on Android < Q. Instead, there's
        // BlendModeCompat.MODULATE, which maps to PorterDuff.Mode.MODULATE and not
        // PorterDuff.Mode.MULTIPLY.
        //
        // MODULATE differs from MULTIPLY in that it doesn't perform
        // any alpha blending. It just does a component-wise multiplication
        // of the colors.
        //
        // For proper results on all platforms, we will map the MULTIPLY
        // blend mode to MODULATE, and then do a slight adjustment to
        // how we render such layers to still achieve the correct result.
        // See BaseLayer.draw().
        return BlendModeCompat.MODULATE;
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

      // Blend modes below were not added to the platform until Q.
      // To prevent unexpected issues where animations look correct
      // during development but silently break for users with older devices
      // we won't support any of these until Q is widely used.
      case COLOR_DODGE:
      case COLOR_BURN:
      case HARD_LIGHT:
      case SOFT_LIGHT:
      case DIFFERENCE:
      case EXCLUSION:
      case HUE:
      case SATURATION:
      case COLOR:
      case LUMINOSITY:
      case HARD_MIX:
      default:
        return null;
    }
  }

}
