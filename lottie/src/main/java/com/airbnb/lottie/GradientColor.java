package com.airbnb.lottie;

class GradientColor {
  private final float[] positions;
  private final int[] colors;

  GradientColor(float[] positions, int[] colors) {
    this.positions = positions;
    this.colors = colors;
  }

  float[] getPositions() {
    return positions;
  }

  int[] getColors() {
    return colors;
  }

  int getSize() {
    return colors.length;
  }

  void lerp(GradientColor gc1, GradientColor gc2, float progress) {
    if (gc1.colors.length != gc2.colors.length) {
      throw new IllegalArgumentException("Cannot interpolate between gradients. Lengths vary (" +
          gc1.colors.length + " vs " + gc2.colors.length + ")");
    }

    for (int i = 0; i < gc1.colors.length; i++) {
      positions[i] = MiscUtils.lerp(gc1.positions[i], gc2.positions[i], progress);
      colors[i] = GammaEvaluator.evaluate(progress, gc1.colors[i], gc2.colors[i]);
    }
  }
}
