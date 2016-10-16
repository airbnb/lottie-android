package com.airbnb.lottie.utils;

import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {


    private JsonUtils() {
    }

    public static PointF pointValueFromJsonObject(JSONObject values, float scale) {
        PointF point = new PointF();
        try {
            Object x = values.get("x");
            if (x instanceof Float) {
                point.x = (float) x;
            } else if (x instanceof Integer) {
                point.x = (Integer) x;
            } else if (x instanceof Double) {
                point.x = (float) (double) x;
            } else if (x instanceof JSONArray) {
                point.x = (float) ((JSONArray) x).getDouble(0);
            }

            Object y = values.get("y");
            if (y instanceof Float) {
                point.y = (float) y;
            } else if (y instanceof Integer) {
                point.y = (Integer) y;
            } else if (y instanceof Double) {
                point.y = (float) (double) y;
            } else if (y instanceof JSONArray) {
                point.y = (float) ((JSONArray) y).getDouble(0);
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse point " + values, e);
        }
        point.x *= scale;
        point.y *= scale;
        return point;
    }

    public static PointF pointFromJsonArray(JSONArray values, float scale) {
        if (values.length() < 2) {
            throw new IllegalArgumentException("Unable to parse point for " + values);
        }
        try {
            return new PointF((float) values.getDouble(0) * scale, (float) values.getDouble(1) * scale);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse point for " + values, e);
        }
    }
}
