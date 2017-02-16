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

  Mask(JSONObject json, LottieComposition composition) throws JSONException {
    try {
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

      maskPath = new AnimatableShapeValue(json.getJSONObject("pt"), composition);
      //noinspection unused
      AnimatableIntegerValue opacity =
          new AnimatableIntegerValue(json.getJSONObject("o"), composition, false, true);
      // TODO: use this.
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse mask. " + json, e);
    }
  }


  @SuppressWarnings("unused") MaskMode getMaskMode() {
    return maskMode;
  }

  AnimatableShapeValue getMaskPath() {
    return maskPath;
  }
}
