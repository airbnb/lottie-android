package com.airbnb.lotte.model;

import android.util.Log;

import com.airbnb.lotte.L;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteShapePath {
    private static final String TAG = LotteShapePath.class.getSimpleName();

    private boolean closed;
    private int index;
    private LotteAnimatableShapeValue shapePath;

    public LotteShapePath(JSONObject json, int frameRate) {
        try {
            index = json.getInt("ind");
        } catch (JSONException e) {
            throw new IllegalArgumentException("ShapePath has no index.");
        }

        try {
            closed = json.getBoolean("closed");
        } catch (JSONException e) {
            throw new IllegalArgumentException("ShapePath index " + index + " has no value for 'closed'.");
        }

        JSONObject shape = null;
        try {
            shape = json.getJSONObject("ks");
            shapePath = new LotteAnimatableShapeValue(shape, frameRate, closed);
        } catch (JSONException e) {
            // Ignore
        }

        if (L.DBG) Log.d(TAG, "Parsed new shape path " + toString());
    }

    public LotteAnimatableShapeValue getShapePath() {
        return shapePath;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteShapePath{");
        sb.append("closed=").append(closed);
        sb.append(", index=").append(index);
        sb.append(", shapePath=").append(shapePath.getInitialShape());
        sb.append('}');
        return sb.toString();
    }
}
