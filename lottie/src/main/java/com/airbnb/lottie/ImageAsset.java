package com.airbnb.lottie;

import org.json.JSONObject;

class ImageAsset {

  private int width;
  private int height;
  private String id;
  private String fileName;

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
