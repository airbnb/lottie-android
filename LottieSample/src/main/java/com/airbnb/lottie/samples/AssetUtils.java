package com.airbnb.lottie.samples;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class AssetUtils {

  static List<String> getJsonAssets(Context context, String path) throws IOException {
    String[] assetList = context.getAssets().list(path);
    List<String> files = new ArrayList<>();
    for (String asset : assetList) {
      if (asset.toLowerCase().endsWith(".json")) {
        files.add(asset);
      }
    }
    return files;
  }
}
