package com.airbnb.lotte.model;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"EmptyCatchBlock", "WeakerAccess", "FieldCanBeLocal"})
public class LotteShapeFill {

    private boolean fillEnabled;
    private LotteAnimatableColorValue color;
    private LotteAnimatableNumberValue opacity;

    public LotteShapeFill(JSONObject json, long frameRate) {
        JSONObject jsonColor = null;
        try {
            jsonColor = json.getJSONObject("c");
        } catch (JSONException e) { }
        if (jsonColor != null) {
            color = new LotteAnimatableColorValue(json, frameRate);
        }

        JSONObject jsonOpacity = null;
        try {
            jsonOpacity = json.getJSONObject("o");
        } catch (JSONException e) { }
        if (jsonOpacity != null) {
            opacity = new LotteAnimatableNumberValue(jsonOpacity, frameRate);
            opacity.remapValues(0, 100, 0, 1);
        }

        try {
            fillEnabled = json.getBoolean("fillEnabled");
        } catch (JSONException e) { }
    }
}
