package com.airbnb.lotte.model;

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

    public LotteMask(JSONObject json, long frameRate) {
        // TODO
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
