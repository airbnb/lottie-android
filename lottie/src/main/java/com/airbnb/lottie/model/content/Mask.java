package com.airbnb.lottie.model.content;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatableShapeValue;

import org.json.JSONObject;

public class Mask {
  public enum MaskMode {
    MaskModeAdd,
    MaskModeSubtract,
    MaskModeIntersect,
    MaskModeUnknown
  }

  private final MaskMode maskMode;
  private final AnimatableShapeValue maskPath;
  private final AnimatableIntegerValue opacity;

  private Mask(MaskMode maskMode, AnimatableShapeValue maskPath, AnimatableIntegerValue opacity) {
    this.maskMode = maskMode;
    this.maskPath = maskPath;
    this.opacity = opacity;
  }

  public static class Factory {
    private Factory() {
    }

    public static Mask newMask(JSONObject json, LottieComposition composition) {
      MaskMode maskMode;
      switch (json.optString("mode")) {
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

      AnimatableShapeValue maskPath = AnimatableShapeValue.Factory.newInstance(
          json.optJSONObject("pt"), composition);
      JSONObject opacityJson = json.optJSONObject("o");
      AnimatableIntegerValue opacity =
          AnimatableIntegerValue.Factory.newInstance(opacityJson, composition);
      return new Mask(maskMode, maskPath, opacity);
    }
  }

  public MaskMode getMaskMode() {
    return maskMode;
  }

  public AnimatableShapeValue getMaskPath() {
    return maskPath;
  }

  public AnimatableIntegerValue getOpacity() {
    return opacity;
  }
}
