package com.airbnb.lotte.model;

import com.airbnb.lotte.animation.LotteAnimatableIntegerValue;
import com.airbnb.lotte.animation.LotteAnimatableShapeValue;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteMask {

    private enum MaskMode {
        MaskModeAdd,
        MaskModeSubtract,
        MaskModeIntersect,
        MaskModeUnknown
    }

    private MaskMode maskMode;
    private LotteAnimatableShapeValue maskPath;

    public LotteMask(JSONObject json, int frameRate, long compDuration) {
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

            maskPath = new LotteAnimatableShapeValue(json.getJSONObject("pt"), frameRate, compDuration, closed);
            LotteAnimatableIntegerValue opacity = new LotteAnimatableIntegerValue(json.getJSONObject("o"), frameRate, compDuration);
            opacity.remapValues(0, 100, 0, 255);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse mask. " + json, e);
        }
    }


    public MaskMode getMaskMode() {
        return maskMode;
    }

    public LotteAnimatableShapeValue getMaskPath() {
        return maskPath;
    }
}
