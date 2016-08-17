package com.airbnb.lotte.model;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteShapePath {

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
    }

    public LotteAnimatableShapeValue getShapePath() {
        return shapePath;
    }
}
