package com.airbnb.lottie.model;

import com.airbnb.lottie.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.animatable.AnimatableShapeValue;

import org.json.JSONException;
import org.json.JSONObject;

public class Mask {

    private enum MaskMode {
        MaskModeAdd,
        MaskModeSubtract,
        MaskModeIntersect,
        MaskModeUnknown
    }

    private MaskMode maskMode;
    private AnimatableShapeValue maskPath;

    public Mask(JSONObject json, int frameRate, LottieComposition composition) {
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
            AnimatableIntegerValue opacity = new AnimatableIntegerValue(json.getJSONObject("o"), frameRate, composition, false, true);
            // TODO: use this.
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse mask. " + json, e);
        }
    }


    public MaskMode getMaskMode() {
        return maskMode;
    }

    public AnimatableShapeValue getMaskPath() {
        return maskPath;
    }
}
