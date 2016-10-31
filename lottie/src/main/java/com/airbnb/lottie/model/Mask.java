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

    public Mask(JSONObject json, int frameRate, long compDuration) {
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

            maskPath = new AnimatableShapeValue(json.getJSONObject("pt"), frameRate, compDuration, closed);
            AnimatableIntegerValue opacity = new AnimatableIntegerValue(json.getJSONObject("o"), frameRate, compDuration, false);
            opacity.remap100To255();
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
