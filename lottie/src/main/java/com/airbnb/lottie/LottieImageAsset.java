package com.airbnb.lottie;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/**
 * Data class describing an image asset embedded in a Lottie json file.
 */
public class LottieImageAsset {
  protected final int width;
  protected final int height;
  private final String id;
  private final String fileName;
  private final String dirName;
  /**
   * Pre-set a bitmap for this asset
   */
  @Nullable private Bitmap bitmap;

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public LottieImageAsset(int width, int height, String id, String fileName, String dirName) {
    this.width = width;
    this.height = height;
    this.id = id;
    this.fileName = fileName;
    this.dirName = dirName;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  /**
   * The reference id in the json file.
   */
  public String getId() {
    return id;
  }

  public String getFileName() {
    return fileName;
  }

  @SuppressWarnings("unused") public String getDirName() {
    return dirName;
  }

  /**
   * Returns the bitmap that has been stored for this image asset if one was explicitly set.
   */
  @Nullable public Bitmap getBitmap() {
    return bitmap;
  }

  /**
   * Permanently sets the bitmap on this LottieImageAsset. This will:
   * 1) Overwrite any existing Bitmaps.
   * 2) Apply to *all* animations that use this LottieComposition.
   *
   * If you only want to replace the bitmap for this animation, use dynamic properties
   * with {@link LottieProperty#IMAGE}.
   */
  public void setBitmap(@Nullable Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  /**
   * Returns whether this asset has an embedded Bitmap or whether the fileName is a base64 encoded bitmap.
   */
  public boolean hasBitmap() {
    return bitmap != null || (fileName.startsWith("data:") && fileName.indexOf("base64,") > 0);
  }
}
