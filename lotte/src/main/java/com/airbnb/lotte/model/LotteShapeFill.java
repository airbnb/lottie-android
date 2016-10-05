package com.airbnb.lotte.model;

import android.util.Log;

import com.airbnb.lotte.L;
import com.airbnb.lotte.animation.LotteAnimatableColorValue;
import com.airbnb.lotte.animation.LotteAnimatableNumberValue;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"EmptyCatchBlock", "WeakerAccess", "FieldCanBeLocal"})
public class LotteShapeFill {
    private static final String TAG = LotteShapeFill.class.getSimpleName();

    private boolean fillEnabled;
    private LotteAnimatableColorValue color;
    private LotteAnimatableNumberValue opacity;

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
            opacity = new LotteAnimatableNumberValue(jsonOpacity, frameRate, compDuration);
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

    public LotteAnimatableNumberValue getOpacity() {
        return opacity;
    }

    public boolean isFillEnabled() {
        return fillEnabled;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteShapeFill{");
        sb.append("color=").append(Integer.toHexString(color.getInitialValue()));
        sb.append(", fillEnabled=").append(fillEnabled);
        sb.append(", opacity=").append(opacity.getInitialValue());
        sb.append('}');
        return sb.toString();
    }
}
