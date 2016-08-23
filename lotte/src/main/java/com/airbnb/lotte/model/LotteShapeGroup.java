package com.airbnb.lotte.model;

import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;

import com.airbnb.lotte.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"EmptyCatchBlock", "FieldCanBeLocal"})
public class LotteShapeGroup {
    private static final String TAG = LotteShapeGroup.class.getSimpleName();

    @Nullable
    public static Object shapeItemWithJson(JSONObject json, int framerate, Rect compBounds) {
        String type = null;
        try {
            type = json.getString("ty");
        } catch (JSONException e) { }
        if (type == null) {
            throw new IllegalStateException("Shape has no type.");
        }

        try {
            if (L.DBG) Log.d(TAG, "Parsing group layer " + json.getString("nm") + " type " + type);
        } catch (JSONException e) { }

        switch (type) {
            case "gr":
                return new LotteShapeGroup(json, framerate, compBounds);
            case "st":
                return new LotteShapeStroke(json, framerate);
            case "fl":
                return new LotteShapeFill(json, framerate);
            case "tr":
                return new LotteShapeTransform(json, framerate, compBounds);
            case "sh":
                return new LotteShapePath(json, framerate);
            case "el":
                return new LotteShapeCircle(json, framerate);
            case "rc":
                return new LotteShapeRectangle(json, framerate);
            case "tm":
                return new LotteShapeTrimPath(json, framerate);
        }
        return null;
    }

    private String name;
    private final List<Object> items = new ArrayList<>();

    public LotteShapeGroup(JSONObject json, int frameRate, Rect compBounds) {
        JSONArray jsonItems = null;
        try {
            jsonItems = json.getJSONArray("it");
        } catch (JSONException e) {}
        if (jsonItems == null) {
            // Thought this was necessary but maybe not?
            // throw new IllegalStateException("There are no items.");
            jsonItems = new JSONArray();
        }

        try {
            name = json.getString("nm");
        } catch (JSONException e) {}

        for (int i = 0; i < jsonItems.length(); i++) {
            JSONObject jsonItem = null;
            try {
                jsonItem = jsonItems.getJSONObject(i);
            } catch (JSONException e) { }
            if (jsonItem == null) {
                throw new IllegalStateException("Unable to get jsonItem");
            }


            Object newItem = shapeItemWithJson(jsonItem, frameRate, compBounds);
            if (newItem != null) {
                items.add(newItem);
            }
        }

        if (L.DBG) Log.d(TAG, "Parsed new group " + name);
    }

    public List<Object> getItems() {
        return items;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteShapeGroup{");
        sb.append("name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
