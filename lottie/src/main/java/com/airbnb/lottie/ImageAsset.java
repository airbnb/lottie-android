package com.airbnb.lottie;

import org.json.JSONObject;

class ImageAsset {
  private final int width;
  private final int height;
  private final String id;
  private final String fileName;

  private ImageAsset(int width, int height, String id, String fileName) {
    this.width = width;
    this.height = height;
    this.id = id;
    this.fileName = fileName;
  }

  static class Factory {
    static ImageAsset newInstance(JSONObject imageJson) {
      return new ImageAsset(imageJson.optInt("w"), imageJson.optInt("h"), imageJson.optString("id"),
          imageJson.optString("p"));
    }
  }

  int getWidth() {
    return width;
  }

  int getHeight() {
    return height;
  }

  String getId() {
    return id;
  }

  String getFileName() {
    return fileName;
  }
}
