package com.airbnb.lottie;

import org.json.JSONObject;

class Mask {
  enum MaskMode {
    MaskModeAdd,
    MaskModeSubtract,
    MaskModeIntersect,
    MaskModeUnknown
  }

  private final MaskMode maskMode;
  private final AnimatableShapeValue maskPath;

  Mask(JSONObject json, LottieComposition composition) {
    String mode = json.optString("mode");
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

    maskPath = new AnimatableShapeValue(json.optJSONObject("pt"), composition);
    // TODO: use this
    // JSONObject opacityJson = json.optJSONObject("o");
    // if (opacityJson != null) {
    //   AnimatableIntegerValue opacity =
    //       new AnimatableIntegerValue(opacityJson, composition, false, true);
    // }
  }


  @SuppressWarnings("unused") MaskMode getMaskMode() {
    return maskMode;
  }

  AnimatableShapeValue getMaskPath() {
    return maskPath;
  }
}
