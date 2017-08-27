package com.airbnb.lottie.utils;

/**
 * Class to calculate the average in a stream of numbers on a continuous basis.
 */
public class MeanCalculator {

  private float sum;
  private int n;

  public void add(float number) {
    sum += number;
    n++;
    if (n == Integer.MAX_VALUE) {
      sum /= 2f;
      n /= 2;
    }
  }

  public float getMean() {
    if (n == 0) {
      return 0;
    }
    return sum / (float) n;
  }
}
