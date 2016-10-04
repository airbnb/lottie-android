package com.airbnb.lotte.model;

import com.airbnb.lotte.animation.LotteAnimatableNumberValue;
import com.airbnb.lotte.animation.LotteAnimatableShapeValue;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteMask {

    public enum MaskMode {
        MaskModeAdd,
        MaskModeSubtract,
        MaskModeIntersect,
        MaskModeUnknown
    }

    private boolean closed;
    private boolean inverted;
    private MaskMode maskMode;
    private LotteAnimatableShapeValue maskPath;
    private LotteAnimatableNumberValue opacity;

    public LotteMask(JSONObject json, int frameRate, long compDuration) {
        try {
            closed = json.getBoolean("cl");
            inverted = json.getBoolean("inv");
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
            opacity = new LotteAnimatableNumberValue(json.getJSONObject("o"), frameRate, compDuration);
            opacity.remapValues(0, 100, 0, 255);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse mask. " + json, e);
        }
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isInverted() {
        return inverted;
    }

    public MaskMode getMaskMode() {
        return maskMode;
    }

    public LotteAnimatableShapeValue getMaskPath() {
        return maskPath;
    }

    public LotteAnimatableNumberValue getOpacity() {
        return opacity;
    }
}
