package com.airbnb.lottie;

/**
 * Data class describing an image asset exported by bodymovin.
 */
@SuppressWarnings("WeakerAccess")
public class LottieImageAsset {
  private final int width;
  private final int height;
  private final String id;
  private final String fileName;
  private final String dirName;

  LottieImageAsset(int width, int height, String id, String fileName, String dirName) {
    this.width = width;
    this.height = height;
    this.id = id;
    this.fileName = fileName;
    this.dirName = dirName;
  }

  @SuppressWarnings("WeakerAccess") public int getWidth() {
    return width;
  }

  @SuppressWarnings("WeakerAccess")public int getHeight() {
    return height;
  }

  public String getId() {
    return id;
  }

  public String getFileName() {
    return fileName;
  }

  @SuppressWarnings("unused") public String getDirName() {
    return dirName;
  }
}
