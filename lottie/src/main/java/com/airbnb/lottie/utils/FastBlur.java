package com.airbnb.lottie.utils;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.nio.ByteBuffer;

public class FastBlur {
  private ByteBuffer buf1;
  private ByteBuffer buf2;

  private void ensureCapacity(Bitmap image) {
    int requiredCapacity = image.getWidth() * image.getHeight() * 4;
    int newCapacity = 4 * (int)((1.05f * image.getWidth()) * (1.05f * image.getHeight())); // add 5% margin to avoid frequent reallocation
    if (buf1 == null || buf1.capacity() < requiredCapacity) {
      buf1 = ByteBuffer.allocate(newCapacity);
    }
    if (buf2 == null || buf2.capacity() < requiredCapacity) {
      buf2 = ByteBuffer.allocate(newCapacity);
    }

    buf1.rewind();
    buf2.rewind();
  }

  private void initialAccumulateHorizontal(byte[] src, int[] sumsByChannel, int rowStart, int radius) {
    for (int channel = 0; channel < 4; channel++) {
      sumsByChannel[channel] = src[rowStart + channel];
    }

    // Accumulate the initial sum for the kernel centered at x=0
    for (int i = 1; i <= radius; i++) {
      int base = rowStart + 4 * i;
      for (int channel = 0; channel < 4; channel++) {
        sumsByChannel[channel] += (int)src[base + channel] & 0xff; // On the right side
        sumsByChannel[channel] += (int)src[rowStart + channel] & 0xff; // On the left side
      }
    }
  }

  private void naiveHorizontalPass(byte[] src, byte[] dst, int stride, Rect rect, int radius) {
    int kernelSize = 2 * radius + 1;
    int[] sumsByChannel = new int[4];

    int firstPixel = rect.top * stride + 4 * rect.left;
    int height = rect.height();
    int width = rect.width();
    for (int y = 0; y < height; y++) {
      int rowStart = firstPixel + y * stride;
      int lastPixel = rowStart + 4 * (width - 1);

      initialAccumulateHorizontal(src, sumsByChannel, rowStart, radius);

      int leftPixelOffset = (-radius) * 4;
      int rightPixeloffset = (radius + 1) * 4;

      int x = 0;
      while (x < width) {
        int base = rowStart + 4 * x;
        int baseLeft = Math.max(base + leftPixelOffset, rowStart);
        int baseRight = Math.min(base + rightPixeloffset, lastPixel);
        for (int channel = 0; channel < 4; channel++) {
          dst[base + channel] = (byte) (sumsByChannel[channel] / kernelSize);

          int left = (int)src[baseLeft + channel] & 0xff;
          int right = (int)src[baseRight + channel] & 0xff;
          sumsByChannel[channel] += -left + right;
        }

        x++;
      }
    }
  }

  private void horizontalPass(byte[] src, byte[] dst, int stride, Rect rect, int radius) {
    int kernelSize = 2 * radius + 1;
    int[] sumsByChannel = new int[4];

    int firstPixel = rect.top * stride + 4 * rect.left;
    int width = rect.width();
    int height = rect.height();
    for (int y = 0; y < height; y++) {
      int rowStart = firstPixel + y * stride;

      initialAccumulateHorizontal(src, sumsByChannel, rowStart, radius);

      int leftPixelOffset = (-radius) * 4;
      int rightPixelOffset = (radius + 1) * 4;

      int x = 0;

      // X is clamped on the left side
      while (x < radius) {
        int base = rowStart + 4 * x;
        int baseLeft = rowStart;
        int baseRight = base + rightPixelOffset;
        for (int channel = 0; channel < 4; channel++) {
          dst[base + channel] = (byte) (sumsByChannel[channel] / kernelSize);

          int left = (int)src[baseLeft + channel] & 0xff;
          int right = (int)src[baseRight + channel] & 0xff;
          sumsByChannel[channel] += -left + right;
        }

        x++;
      }

      // X is not clamped at all
      while (x < width - radius - 1) {
        int base = rowStart + 4 * x;
        int baseLeft = base + leftPixelOffset;
        int baseRight = base + rightPixelOffset;

        for (int channel = 0; channel < 4; channel++) {
          dst[base + channel] = (byte) (sumsByChannel[channel] / kernelSize);

          int left = (int)src[baseLeft + channel] & 0xff;
          int right = (int)src[baseRight + channel] & 0xff;
          sumsByChannel[channel] += -left + right;
        }

        x++;
      }

      // X is clamped on the right side
      int lastPixel = rowStart + 4 * (width - 1);
      while (x < width) {
        int base = rowStart + 4 * x;
        int baseLeft = base + leftPixelOffset;
        int baseRight = lastPixel;
        for (int channel = 0; channel < 4; channel++) {
          dst[base + channel] = (byte) (sumsByChannel[channel] / kernelSize);

          int left = (int)src[baseLeft + channel] & 0xff;
          int right = (int)src[baseRight + channel] & 0xff;
          sumsByChannel[channel] += -left + right;
        }

        x++;
      }
    }
  }

  private void initialAccumulateVertical(byte[] src, int[] sumsByChannel, int columnStart, int stride, int radius) {
    for (int channel = 0; channel < 4; channel++) {
      sumsByChannel[channel] = src[columnStart + channel];
    }

    // Accumulate the initial sum for the kernel centered at y=0
    for (int i = 1; i <= radius; i++) {
      int base = columnStart + stride * i;
      for (int channel = 0; channel < 4; channel++) {
        sumsByChannel[channel] += (int)src[base + channel] & 0xff; // On the bottom side
        sumsByChannel[channel] += (int)src[columnStart + channel] & 0xff; // On the top side
      }
    }
  }

  private void verticalPass(byte[] src, byte[] dst, int stride, Rect rect, int radius) {
    int kernelSize = 2 * radius + 1;
    int[] sumsByChannel = new int[4];

    int firstPixel = stride * rect.top + 4 * rect.left;
    int width = rect.width();
    int height = rect.height();
    for (int x = 0; x < width; x++) {
      // Init with the first element only
      int columnStart = firstPixel + 4 * x;
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
        for (int channel = 0; channel < 4; channel++) {
          dst[base + channel] = (byte) (sumsByChannel[channel] / kernelSize);

          int top = (int)src[baseTop + channel] & 0xff;
          int bottom = (int)src[baseBottom + channel] & 0xff;
          sumsByChannel[channel] += -top + bottom;
        }

        y++;
      }

      // Y is not clamped
      while (y < height - radius - 1) {
        int base = columnStart + stride * y;
        int baseTop = base + topPixelOffset;
        int baseBottom = base + bottomPixelOffset;

        for (int channel = 0; channel < 4; channel++) {
          dst[base + channel] = (byte) (sumsByChannel[channel] / kernelSize);

          int top = (int)src[baseTop + channel] & 0xff;
          int bottom = (int)src[baseBottom + channel] & 0xff;
          sumsByChannel[channel] += -top + bottom;
        }

        y++;
      }

      // Y is clamped to the bottom
      while (y < height) {
        int base = columnStart + stride * y;
        int baseTop = base + topPixelOffset;
        int baseBottom = lastPixel;
        for (int channel = 0; channel < 4; channel++) {
          dst[base + channel] = (byte) (sumsByChannel[channel] / kernelSize);

          int top = (int)src[baseTop + channel] & 0xff;
          int bottom = (int)src[baseBottom + channel] & 0xff;
          sumsByChannel[channel] += -top + bottom;
        }

        y++;
      }
    }
  }

  private void naiveVerticalPass(byte[] src, byte[] dst, int stride, Rect rect, int radius) {
    int kernelSize = 2 * radius + 1;
    int[] sumsByChannel = new int[4];

    int firstPixel = stride * rect.top + 4 * rect.left;
    int width = rect.width();
    int height = rect.height();
    for (int x = 0; x < width; x++) {
      // Init with the first element only
      int columnStart = firstPixel + 4 * x;
      int lastPixel = columnStart + stride * (height - 1);

      initialAccumulateVertical(src, sumsByChannel, columnStart, stride, radius);

      int topPixelOffset = (-radius) * stride;
      int bottomPixelOffset = (radius + 1) * stride;

      int y = 0;
      while (y < height) {
        int base = columnStart + stride * y;
        int baseTop = Math.max(base + topPixelOffset, columnStart);
        int baseBottom = Math.min(base + bottomPixelOffset, lastPixel);
        for (int channel = 0; channel < 4; channel++) {
          dst[base + channel] = (byte) (sumsByChannel[channel] / kernelSize);

          int top = (int)src[baseTop + channel] & 0xff;
          int bottom = (int)src[baseBottom + channel] & 0xff;
          sumsByChannel[channel] += -top + bottom;
        }

        y++;
      }
    }
  }

  void blurPass(byte[] src, byte[] dst, int stride, Rect rect, int radius) {
    int kernelSize = 2 * radius - 1;

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
    
    image.copyPixelsFromBuffer(buf1);
  }
}
