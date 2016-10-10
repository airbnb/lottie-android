package com.airbnb.lotte.model;

import android.util.Log;

import com.airbnb.lotte.L;
import com.airbnb.lotte.animation.LotteAnimatableColorValue;
import com.airbnb.lotte.animation.LotteAnimatableIntegerValue;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"EmptyCatchBlock", "WeakerAccess", "FieldCanBeLocal"})
public class LotteShapeFill {
    private static final String TAG = LotteShapeFill.class.getSimpleName();

    private boolean fillEnabled;
    private LotteAnimatableColorValue color;
    private LotteAnimatableIntegerValue opacity;

    public LotteShapeFill(JSONObject json, int frameRate, long compDuration) {
        JSONObject jsonColor = null;
        try {
            jsonColor = json.getJSONObject("c");
        } catch (JSONException e) { }
        if (jsonColor != null) {
            color = new LotteAnimatableColorValue(jsonColor, frameRate, compDuration);
        }

        JSONObject jsonOpacity = null;
        try {
            jsonOpacity = json.getJSONObject("o");
        } catch (JSONException e) { }
        if (jsonOpacity != null) {
            opacity = new LotteAnimatableIntegerValue(jsonOpacity, frameRate, compDuration);
            opacity.remapValues(0, 100, 0, 255);
        }

        try {
            fillEnabled = json.getBoolean("fillEnabled");
        } catch (JSONException e) { }
        if (L.DBG) Log.d(TAG, "Parsed new shape fill " + toString());
    }

    public LotteAnimatableColorValue getColor() {
        return color;
    }

    public LotteAnimatableIntegerValue getOpacity() {
        return opacity;
    }

    @Override
    public String toString() {
        return "LotteShapeFill{" + "color=" + Integer.toHexString(color.getInitialValue()) +
                ", fillEnabled=" + fillEnabled +
                ", opacity=" + opacity.getInitialValue() +
                '}';
    }
}
