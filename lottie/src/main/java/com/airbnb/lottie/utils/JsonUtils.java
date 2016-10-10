package com.airbnb.lottie.utils;

import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {


    private JsonUtils() {
    }

    public static PointF pointValueFromDict(JSONObject values) {
        PointF point = new PointF();
        try {
            Object x = values.get("x");
            if (x instanceof Float) {
                point.x = (float) x;
            } else if (x instanceof JSONArray) {
                point.x = (float) ((JSONArray) x).getDouble(0);
            }

            Object y = values.get("y");
            if (y instanceof Float) {
                point.y = (float) y;
            } else if (y instanceof JSONArray) {
                point.y = (float) ((JSONArray) y).getDouble(0);
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse point " + values, e);
        }
        return point;
    }
}
