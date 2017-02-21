package com.airbnb.lottie;

import org.json.JSONObject;

class ImageAsset {
  private final int width;
  private final int height;
  private final String id;
  private final String fileName;

  ImageAsset(JSONObject imageJson) {
    width = imageJson.optInt("w");
    height = imageJson.optInt("h");
    id = imageJson.optString("id");
    fileName = imageJson.optString("p");
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
