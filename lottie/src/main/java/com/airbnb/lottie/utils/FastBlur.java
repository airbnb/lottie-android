package com.airbnb.lottie.utils;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class FastBlur {

  private IntBuffer buf1;
  private IntBuffer buf2;

  private void ensureCapacity(Bitmap image) {
    int requiredCapacity = image.getWidth() * image.getHeight();
    int newCapacity = (int) ((1.05f * image.getWidth()) * (1.05f * image.getHeight())); // add 5% margin to avoid frequent reallocation
    if (buf1 == null || buf1.capacity() < requiredCapacity) {
      buf1 = IntBuffer.allocate(newCapacity);
    }
    if (buf2 == null || buf2.capacity() < requiredCapacity) {
      buf2 = IntBuffer.allocate(newCapacity);
    }

    buf1.rewind();
    buf2.rewind();
  }

  private void initialAccumulateHorizontal(int[] src, int[] sumsByChannel, int rowStart, int radius) {
    int startVal = src[rowStart];
    int startValR = startVal & 0xff;
    int startValG = (startVal >> 8) & 0xff;
    int startValB = (startVal >> 16) & 0xff;
    int startValA = (startVal >> 24) & 0xff;

    sumsByChannel[0] = (radius + 1) * startValR;
    sumsByChannel[1] = (radius + 1) * startValG;
    sumsByChannel[2] = (radius + 1) * startValB;
    sumsByChannel[3] = (radius + 1) * startValA;

    for (int i = 1; i <= radius; i++) {
      int val = src[rowStart + i];
      sumsByChannel[0] += (val & 0xff);
      sumsByChannel[1] += (val >> 8) & 0xff;
      sumsByChannel[2] += (val >> 16) & 0xff;
      sumsByChannel[3] += (val >> 24) & 0xff;
    }
  }

  private void naiveHorizontalPass(int[] src, int[] dst, int stride, Rect rect, int radius) {
    int kernelSize = 2 * radius + 1;
    int[] sumsByChannel = new int[4];

    int firstPixel = rect.top * stride + rect.left;
    int height = rect.height();
    int width = rect.width();
    for (int y = 0; y < height; y++) {
      int rowStart = firstPixel + y * stride;
      int lastPixel = rowStart + width - 1;

      initialAccumulateHorizontal(src, sumsByChannel, rowStart, radius);

      int leftPixelOffset = -radius;
      int rightPixeloffset = radius + 1;

      int x = 0;
      while (x < width) {
        int base = rowStart + x;
        int baseLeft = Math.max(base + leftPixelOffset, rowStart);
        int baseRight = Math.min(base + rightPixeloffset, lastPixel);

        int newVal = (
            (sumsByChannel[0] / kernelSize) |
                (sumsByChannel[1] / kernelSize << 8) |
                (sumsByChannel[2] / kernelSize << 16) |
                (sumsByChannel[3] / kernelSize << 24)
        );

        while (dst[base] == newVal &&
            x < width - radius - 1 &&
            src[baseLeft] == src[baseRight]) {
          x++;
          base++;
          baseLeft++;
          baseRight++;
        }

        int left = src[baseLeft];
        int right = src[baseRight];

        sumsByChannel[0] += -(left & 0xff) + (right & 0xff);
        sumsByChannel[1] += -((left >> 8) & 0xff) + ((right >> 8) & 0xff);
        sumsByChannel[2] += -((left >> 16) & 0xff) + ((right >> 16) & 0xff);
        sumsByChannel[3] += -((left >> 24) & 0xff) + ((right >> 24) & 0xff);

        x++;
      }
    }
  }

  private void horizontalPass(int[] src, int[] dst, int stride, Rect rect, int radius) {
    int kernelSize = 2 * radius + 1;
    int[] sumsByChannel = new int[4];

    int firstPixel = rect.top * stride + rect.left;
    int width = rect.width();
    int height = rect.height();
    for (int y = 0; y < height; y++) {
      int rowStart = firstPixel + y * stride;

      initialAccumulateHorizontal(src, sumsByChannel, rowStart, radius);

      int leftPixelOffset = -radius;
      int rightPixelOffset = radius + 1;

      int x = 0;

      // X is clamped on the left side
      while (x < radius) {
        int base = rowStart + x;
        int baseLeft = rowStart;
        int baseRight = base + rightPixelOffset;

        dst[base] = (
            (sumsByChannel[0] / kernelSize) |
                (sumsByChannel[1] / kernelSize << 8) |
                (sumsByChannel[2] / kernelSize << 16) |
                (sumsByChannel[3] / kernelSize << 24)
        );

        int left = src[baseLeft];
        int right = src[baseRight];

        sumsByChannel[0] += -(left & 0xff) + (right & 0xff);
        sumsByChannel[1] += -((left >> 8) & 0xff) + ((right >> 8) & 0xff);
        sumsByChannel[2] += -((left >> 16) & 0xff) + ((right >> 16) & 0xff);
        sumsByChannel[3] += -((left >> 24) & 0xff) + ((right >> 24) & 0xff);

        x++;
      }

      // X is not clamped at all
      while (x < width - radius - 1) {
        int base = rowStart + x;
        int baseLeft = base + leftPixelOffset;
        int baseRight = base + rightPixelOffset;

        int newVal = (
            (sumsByChannel[0] / kernelSize) |
                (sumsByChannel[1] / kernelSize << 8) |
                (sumsByChannel[2] / kernelSize << 16) |
                (sumsByChannel[3] / kernelSize << 24)
        );

        while (dst[base] == newVal &&
            x < width - radius - 1 &&
            src[baseLeft] == src[baseRight]) {
          x++;
          base++;
          baseLeft++;
          baseRight++;
        }

        dst[base] = newVal;

        int left = src[baseLeft];
        int right = src[baseRight];

        sumsByChannel[0] += -(left & 0xff) + (right & 0xff);
        sumsByChannel[1] += -((left >> 8) & 0xff) + ((right >> 8) & 0xff);
        sumsByChannel[2] += -((left >> 16) & 0xff) + ((right >> 16) & 0xff);
        sumsByChannel[3] += -((left >> 24) & 0xff) + ((right >> 24) & 0xff);

        x++;
      }

      // X is clamped on the right side
      int lastPixel = rowStart + 4 * (width - 1);
      while (x < width) {
        int base = rowStart + x;
        int baseLeft = base + leftPixelOffset;
        int baseRight = lastPixel;

        dst[base] = (
            (sumsByChannel[0] / kernelSize) |
                (sumsByChannel[1] / kernelSize << 8) |
                (sumsByChannel[2] / kernelSize << 16) |
                (sumsByChannel[3] / kernelSize << 24)
        );

        int left = src[baseLeft];
        int right = src[baseRight];

        sumsByChannel[0] += -(left & 0xff) + (right & 0xff);
        sumsByChannel[1] += -((left >> 8) & 0xff) + ((right >> 8) & 0xff);
        sumsByChannel[2] += -((left >> 16) & 0xff) + ((right >> 16) & 0xff);
        sumsByChannel[3] += -((left >> 24) & 0xff) + ((right >> 24) & 0xff);

        x++;
      }
    }
  }

  private void initialAccumulateVertical(int[] src, int[] sumsByChannel, int columnStart, int stride, int radius) {
    int startVal = src[columnStart];
    int startValR = startVal & 0xff;
    int startValG = (startVal >> 8) & 0xff;
    int startValB = (startVal >> 16) & 0xff;
    int startValA = (startVal >> 24) & 0xff;

    sumsByChannel[0] = (radius + 1) * startValR;
    sumsByChannel[1] = (radius + 1) * startValG;
    sumsByChannel[2] = (radius + 1) * startValB;
    sumsByChannel[3] = (radius + 1) * startValA;

    for (int i = 1; i <= radius; i++) {
      int val = src[columnStart + stride * i];
      sumsByChannel[0] += (val & 0xff);
      sumsByChannel[1] += (val >> 8) & 0xff;
      sumsByChannel[2] += (val >> 16) & 0xff;
      sumsByChannel[3] += (val >> 24) & 0xff;
    }
  }

  private void verticalPass(int[] src, int[] dst, int stride, Rect rect, int radius) {
    int kernelSize = 2 * radius + 1;
    int[] sumsByChannel = new int[4];

    int firstPixel = stride * rect.top + rect.left;
    int width = rect.width();
    int height = rect.height();
    for (int x = 0; x < width; x++) {
      // Init with the first element only
      int columnStart = firstPixel + x;
      int lastPixel = columnStart + stride * (height - 1);

      initialAccumulateVertical(src, sumsByChannel, columnStart, stride, radius);

      int topPixelOffset = (-radius) * stride;
      int bottomPixelOffset = (radius + 1) * stride;

      // Y is clamped to the top
      int y = 0;
      while (y < radius) {
        int base = columnStart + stride * y;
        int baseTop = columnStart;
        int baseBottom = base + bottomPixelOffset;

        int newVal = (
            (sumsByChannel[0] / kernelSize) |
                (sumsByChannel[1] / kernelSize << 8) |
                (sumsByChannel[2] / kernelSize << 16) |
                (sumsByChannel[3] / kernelSize << 24)
        );

        while (dst[base] == newVal &&
            y < height - radius - 1 &&
            src[baseTop] == src[baseBottom]) {
          y++;
          base += stride;
          baseTop += stride;
          baseBottom += stride;
        }

        dst[base] = newVal;
        int top = src[baseTop];
        int bottom = src[baseBottom];

        sumsByChannel[0] += -(top & 0xff) + (bottom & 0xff);
        sumsByChannel[1] += -((top >> 8) & 0xff) + ((bottom >> 8) & 0xff);
        sumsByChannel[2] += -((top >> 16) & 0xff) + ((bottom >> 16) & 0xff);
        sumsByChannel[3] += -((top >> 24) & 0xff) + ((bottom >> 24) & 0xff);

        y++;
      }

      // Y is not clamped
      while (y < height - radius - 1) {
        int base = columnStart + stride * y;
        int baseTop = base + topPixelOffset;
        int baseBottom = base + bottomPixelOffset;

        dst[base] = (
            (sumsByChannel[0] / kernelSize) |
                (sumsByChannel[1] / kernelSize << 8) |
                (sumsByChannel[2] / kernelSize << 16) |
                (sumsByChannel[3] / kernelSize << 24)
        );

        int top = src[baseTop];
        int bottom = src[baseBottom];

        sumsByChannel[0] += -(top & 0xff) + (bottom & 0xff);
        sumsByChannel[1] += -((top >> 8) & 0xff) + ((bottom >> 8) & 0xff);
        sumsByChannel[2] += -((top >> 16) & 0xff) + ((bottom >> 16) & 0xff);
        sumsByChannel[3] += -((top >> 24) & 0xff) + ((bottom >> 24) & 0xff);

        y++;
      }

      // Y is clamped to the bottom
      while (y < height) {
        int base = columnStart + stride * y;
        int baseTop = base + topPixelOffset;
        int baseBottom = lastPixel;

        dst[base] = (
            (sumsByChannel[0] / kernelSize) |
                (sumsByChannel[1] / kernelSize << 8) |
                (sumsByChannel[2] / kernelSize << 16) |
                (sumsByChannel[3] / kernelSize << 24)
        );

        int top = src[baseTop];
        int bottom = src[baseBottom];

        sumsByChannel[0] += -(top & 0xff) + (bottom & 0xff);
        sumsByChannel[1] += -((top >> 8) & 0xff) + ((bottom >> 8) & 0xff);
        sumsByChannel[2] += -((top >> 16) & 0xff) + ((bottom >> 16) & 0xff);
        sumsByChannel[3] += -((top >> 24) & 0xff) + ((bottom >> 24) & 0xff);

        y++;
      }
    }
  }

  private void naiveVerticalPass(int[] src, int[] dst, int stride, Rect rect, int radius) {
    int kernelSize = 2 * radius + 1;
    int[] sumsByChannel = new int[4];

    int firstPixel = stride * rect.top + rect.left;
    int width = rect.width();
    int height = rect.height();
    for (int x = 0; x < width; x++) {
      // Init with the first element only
      int columnStart = firstPixel + x;
      int lastPixel = columnStart + stride * (height - 1);

      initialAccumulateVertical(src, sumsByChannel, columnStart, stride, radius);

      int topPixelOffset = (-radius) * stride;
      int bottomPixelOffset = (radius + 1) * stride;

      int y = 0;
      while (y < height) {
        int base = columnStart + stride * y;
        int baseTop = Math.max(base + topPixelOffset, columnStart);
        int baseBottom = Math.min(base + bottomPixelOffset, lastPixel);

        int newVal = (
            (sumsByChannel[0] / kernelSize) |
                (sumsByChannel[1] / kernelSize << 8) |
                (sumsByChannel[2] / kernelSize << 16) |
                (sumsByChannel[3] / kernelSize << 24)
        );

        while (dst[base] == newVal &&
            y < height - radius - 1 &&
            src[baseTop] == src[baseBottom]) {
          y++;
          base += stride;
          baseTop += stride;
          baseBottom += stride;
        }

        dst[base] = newVal;

        int top = src[baseTop];
        int bottom = src[baseBottom];

        sumsByChannel[0] += -(top & 0xff) + (bottom & 0xff);
        sumsByChannel[1] += -((top >> 8) & 0xff) + ((bottom >> 8) & 0xff);
        sumsByChannel[2] += -((top >> 16) & 0xff) + ((bottom >> 16) & 0xff);
        sumsByChannel[3] += -((top >> 24) & 0xff) + ((bottom >> 24) & 0xff);

        y++;
      }
    }
  }

  void blurPass(int[] src, int[] dst, int byteStride, Rect rect, int radius) {
    int kernelSize = 2 * radius - 1;
    int stride = byteStride / 4;

    if (rect.width() >= kernelSize) {
      horizontalPass(src, dst, stride, rect, radius);
    } else {
      naiveHorizontalPass(src, dst, stride, rect, radius);
    }

    if (rect.height() >= kernelSize) {
      verticalPass(dst, src, stride, rect, radius);
    } else {
      naiveVerticalPass(dst, src, stride, rect, radius);
    }
  }

  public void applyBlur(Bitmap image, int radius, Rect rect) {
    if (radius < 1) {
      return;
    }

    // Buffer setup
    this.ensureCapacity(image);

    image.copyPixelsToBuffer(buf1);
    buf1.rewind();

    int stride = image.getRowBytes();
    blurPass(buf1.array(), buf2.array(), stride, rect, radius);

    //blurPass(buf1.array(), buf2.array(), stride, rect, radius / 2);

    image.copyPixelsFromBuffer(buf1);
  }
}
