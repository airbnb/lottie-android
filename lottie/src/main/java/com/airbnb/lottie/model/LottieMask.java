package com.airbnb.lottie.model;

import com.airbnb.lottie.animation.LottieAnimatableIntegerValue;
import com.airbnb.lottie.animation.LottieAnimatableShapeValue;

import org.json.JSONException;
import org.json.JSONObject;

public class LottieMask {

    private enum MaskMode {
        MaskModeAdd,
        MaskModeSubtract,
        MaskModeIntersect,
        MaskModeUnknown
    }

    private MaskMode maskMode;
    private LottieAnimatableShapeValue maskPath;

    public LottieMask(JSONObject json, int frameRate, long compDuration) {
        try {
            boolean closed = json.getBoolean("cl");
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

            maskPath = new LottieAnimatableShapeValue(json.getJSONObject("pt"), frameRate, compDuration, closed);
            LottieAnimatableIntegerValue opacity = new LottieAnimatableIntegerValue(json.getJSONObject("o"), frameRate, compDuration, false);
            opacity.remapValues(0, 100, 0, 255);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse mask. " + json, e);
        }
    }


    public MaskMode getMaskMode() {
        return maskMode;
    }

    public LottieAnimatableShapeValue getMaskPath() {
        return maskPath;
    }
}
