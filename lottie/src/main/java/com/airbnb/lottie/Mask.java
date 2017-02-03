package com.airbnb.lottie;

import org.json.JSONException;
import org.json.JSONObject;

class Mask {
  private enum MaskMode {
    MaskModeAdd,
    MaskModeSubtract,
    MaskModeIntersect,
    MaskModeUnknown
  }

  private final MaskMode maskMode;
  private final AnimatableShapeValue maskPath;

  Mask(JSONObject json, int frameRate, LottieComposition composition) {
    try {
      boolean closed = false;
      if (json.has("cl")) {
        closed = json.getBoolean("cl");
      }
      String mode = json.getString("mode");
      switch (mode) {
        case "a":
          maskMode = MaskMode.MaskModeAdd;
          break;
        case "s":
          maskMode = MaskMode.MaskModeSubtract;
          break;
        case "i":
          maskMode = MaskMode.MaskModeIntersect;
          break;
        default:
          maskMode = MaskMode.MaskModeUnknown;
      }

      maskPath = new AnimatableShapeValue(json.getJSONObject("pt"), frameRate, composition, closed);
      //noinspection unused
      AnimatableIntegerValue opacity =
          new AnimatableIntegerValue(json.getJSONObject("o"), frameRate, composition, false, true);
      // TODO: use this.
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse mask. " + json, e);
    }
  }


  MaskMode getMaskMode() {
    return maskMode;
  }

  AnimatableShapeValue getMaskPath() {
    return maskPath;
  }
}
