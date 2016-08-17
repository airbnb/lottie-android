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

    private final List<Object> items = new ArrayList<>();

    public LotteShapeGroup(JSONObject json, int frameRate, Rect compBounds) {
        if (L.DBG) Log.d(TAG, "Parsing new shape group.");
        JSONArray jsonItems = null;
        try {
            jsonItems = json.getJSONArray("it");
        } catch (JSONException e) {}
        if (jsonItems == null) {
            throw new IllegalStateException("There are no items.");
        }

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
    }

    @Nullable
    private Object shapeItemWithJson(JSONObject json, int framerate, Rect compBounds) {
        String type = null;
        try {
            type = json.getString("ty");
        } catch (JSONException e) { }
        if (type == null) {
            throw new IllegalStateException("Shape has no type.");
        }

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

    public List<Object> getItems() {
        return items;
    }
}
