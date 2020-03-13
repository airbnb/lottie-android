package com.airbnb.lottie.model.layer.effect;

import android.graphics.BlurMaskFilter;
import android.graphics.MaskFilter;

public class BlurLayerEffect extends LayerEffect {
  private final double strength;
  private final BlurMaskFilter filter;

  public BlurLayerEffect(double strength) {
    this.strength = strength;
    this.filter = new BlurMaskFilter((float) strength, BlurMaskFilter.Blur.NORMAL);
  }

  public double getStrength() {
    return strength;
  }

  @Override public MaskFilter getMaskFilter() {
    return filter;
  }

  @Override
  public String toString() {
    return "BlurLayerEffect{" + "strength=" +  strength + '}';
  }
}
