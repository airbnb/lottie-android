package com.airbnb.lottie.utils;

import java.util.Arrays;

/**
 * List of floats of unknown size that is optimized to be initialized iteratively and then turned
 * into a float array.
 */
public class FloatArrayList {

  private float[] backingArray = new float[10];
  private int size = 0;

  public void add(float value) {
    if (size == backingArray.length) {
      backingArray = Arrays.copyOf(backingArray, backingArray.length * 2);
    }
    backingArray[size] = value;
  }

  /**
   * Trims the backing array to the current size and returns it.
   */
  public float[] complete() {
    backingArray = Arrays.copyOf(backingArray, size);
    return backingArray;
  }
}
