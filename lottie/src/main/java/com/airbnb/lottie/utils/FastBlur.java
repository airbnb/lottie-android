package com.airbnb.lottie.utils;

import android.graphics.Bitmap;

public class FastBlur {

  /**
   * WIP: Fast blur implementation
   * <p>
   * Still to do:
   * - Reuse arrays
   * - Use a Buffer to avoid unnecessary copies or colorspace conversions
   * - Try to get ART to autovectorize the code (if at all possible)
   * - Experiment with alternatives e.g. one-per-channel BlurMaskFilter
   */
  public static void apply(Bitmap image, int radius) {
    radius /= 7; // Normalize the looks of the blur to what BlurMaskFilter produces
    if (radius < 1) {
      return;
    }

    int width = image.getWidth();
    int height = image.getHeight();
    int totalPixels = width * height;
    int kernelSize = 2 * radius + 1;

    int[] sumsByChannel = new int[4];
    int x, y, i;
    int pixelValue, pixel1, pixel2;
    int yPosition, pixelIndex, yWidth;

    int[] pixels = new int[totalPixels];
    char[] dst = new char[4 * totalPixels];

    // Get the pixel data from the image
    image.setPremultiplied(false);
    image.getPixels(pixels, 0, width, 0, 0, width, height);

    yWidth = pixelIndex = 0;

    // Horizontal blur
    for (y = 0; y < height; y++) {
      for (int channel = 0; channel < 4; channel++) {
        sumsByChannel[channel] = 0;
      }

      // Accumulate the sum of the color channels within the radius
      for (i = -radius; i <= radius; i++) {
        pixelValue = pixels[pixelIndex + Math.min(width - 1, Math.max(i, 0))];
        sumsByChannel[0] += getRed(pixelValue);
        sumsByChannel[1] += getGreen(pixelValue);
        sumsByChannel[2] += getBlue(pixelValue);
        sumsByChannel[3] += getAlpha(pixelValue);
      }

      for (x = 0; x < width; x++) {
        // Assign the average to the blur arrays
        int outIdx = 4 * pixelIndex;
        for (int channel = 0; channel < 4; channel++) {
          dst[outIdx++] = (char) (sumsByChannel[channel] / kernelSize);
        }

        // Subtract the left pixel and add the right pixel
        int minVal = Math.min(x + radius + 1, width - 1);
        int maxVal = Math.max(x - radius, 0);

        pixel1 = pixels[yWidth + minVal];
        pixel2 = pixels[yWidth + maxVal];

        sumsByChannel[0] += getRed(pixel1) - getRed(pixel2);
        sumsByChannel[1] += getGreen(pixel1) - getGreen(pixel2);
        sumsByChannel[2] += getBlue(pixel1) - getBlue(pixel2);
        sumsByChannel[3] += getAlpha(pixel1) - getAlpha(pixel2);

        pixelIndex++;
      }

      yWidth += width;
    }

    // Vertical blur
    for (x = 0; x < width; x++) {
      for (int channel = 0; channel < 4; channel++) {
        sumsByChannel[channel] = 0;
      }

      yPosition = -radius * width;

      // Accumulate the sum over the radius
      for (i = -radius; i <= radius; i++) {
        pixelIndex = Math.max(0, yPosition) + x;

        // Assign the average to the blur arrays
        int outIdx = 4 * pixelIndex;
        for (int channel = 0; channel < 4; channel++) {
          dst[outIdx++] = (char) (sumsByChannel[channel] / kernelSize);
        }

        yPosition += width;
      }

      pixelIndex = x;
      for (y = 0; y < height; y++) {
        pixels[pixelIndex] =
            setColor(sumsByChannel[3] / kernelSize, sumsByChannel[0] / kernelSize, sumsByChannel[1] / kernelSize, sumsByChannel[2] / kernelSize);

        int minVal = (Math.min(y + radius + 1, height - 1)) * width;
        int maxVal = (Math.max(y - radius, 0)) * width;

        // Subtract the top pixel and add the bottom pixel
        int pixel1Red = dst[4 * (x + minVal) + 0];
        int pixel1Green = dst[4 * (x + minVal) + 1];
        int pixel1Blue = dst[4 * (x + minVal) + 2];
        int pixel1Alpha = dst[4 * (x + minVal) + 3];

        int pixel2Red = dst[4 * (x + maxVal) + 0];
        int pixel2Green = dst[4 * (x + maxVal) + 1];
        int pixel2Blue = dst[4 * (x + maxVal) + 2];
        int pixel2Alpha = dst[4 * (x + maxVal) + 3];

        sumsByChannel[0] += pixel1Red - pixel2Red;
        sumsByChannel[1] += pixel1Green - pixel2Green;
        sumsByChannel[2] += pixel1Blue - pixel2Blue;
        sumsByChannel[3] += pixel1Alpha - pixel2Alpha;

        pixelIndex += width;
      }
    }

    // Set the blurred pixels back to the image
    image.setPixels(pixels, 0, width, 0, 0, width, height);
    image.setPremultiplied(true);
  }

  // Helper methods to extract and set color components
  private static int getAlpha(int color) {
    return (color >> 24) & 0xFF;
  }

  private static int getRed(int color) {
    return (color >> 16) & 0xFF;
  }

  private static int getGreen(int color) {
    return ((color >> 8) & 0xFF);
  }

  private static int getBlue(int color) {
    return (color & 0xFF);
  }

  private static int setColor(int alpha, int red, int green, int blue) {
    return (alpha << 24) | (red << 16) | (green << 8) | blue;
  }
}
