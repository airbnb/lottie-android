package com.airbnb.lottie.model.content;

import com.airbnb.lottie.utils.GammaEvaluator;
import com.airbnb.lottie.utils.MiscUtils;

import java.util.Arrays;


public class GradientColor {
  private final float[] positions;
  private final int[] colors;

  public GradientColor(float[] positions, int[] colors) {
    this.positions = positions;
    this.colors = colors;
  }

  public float[] getPositions() {
    return positions;
  }

  public int[] getColors() {
    return colors;
  }

  public int getSize() {
    return colors.length;
  }

  public void lerp(GradientColor gc1, GradientColor gc2, float progress) {
    if (gc1.colors.length != gc2.colors.length) {
      throw new IllegalArgumentException("Cannot interpolate between gradients. Lengths vary (" +
          gc1.colors.length + " vs " + gc2.colors.length + ")");
    }

    for (int i = 0; i < gc1.colors.length; i++) {
      positions[i] = MiscUtils.lerp(gc1.positions[i], gc2.positions[i], progress);
      colors[i] = GammaEvaluator.evaluate(progress, gc1.colors[i], gc2.colors[i]);
    }
  }

  public GradientColor copyWithPositions(float[] positions) {
    int[] colors = new int[positions.length];
    for (int i = 0; i < positions.length; i++) {
      colors[i] = getColorForPosition(positions[i]);
    }
    return new GradientColor(positions, colors);
  }

  private int getColorForPosition(float position) {
    int existingIndex = Arrays.binarySearch(positions, position);
    if (existingIndex >= 0) {
      return colors[existingIndex];
    }
    // binarySearch returns -insertionPoint - 1 if it is not found.
    int insertionPoint = -(existingIndex + 1);
    if (insertionPoint == 0) {
      return colors[0];
    } else if (insertionPoint == colors.length - 1) {
      return colors[colors.length - 1];
    }
    float startPosition = positions[insertionPoint - 1];
    float endPosition = positions[insertionPoint];
    int startColor = colors[insertionPoint - 1];
    int endColor = colors[insertionPoint];

    float fraction = (position - startPosition) / (endPosition - startPosition);
    return GammaEvaluator.evaluate(fraction, startColor, endColor);
  }
}
