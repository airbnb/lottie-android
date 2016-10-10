package com.airbnb.lottie.model;

import android.graphics.Rect;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"EmptyCatchBlock"})
public class LotteShapeGroup {

    @Nullable
    public static Object shapeItemWithJson(JSONObject json, int framerate, long compDuration, Rect compBounds) {
        String type = null;
        try {
            type = json.getString("ty");
        } catch (JSONException e) { }
        if (type == null) {
            throw new IllegalStateException("Shape has no type.");
        }

        switch (type) {
            case "gr":
                return new LotteShapeGroup(json, framerate, compDuration, compBounds);
            case "st":
                return new LotteShapeStroke(json, framerate, compDuration);
            case "fl":
                return new LotteShapeFill(json, framerate, compDuration);
            case "tr":
                return new LotteShapeTransform(json, framerate, compDuration, compBounds);
            case "sh":
                return new LotteShapePath(json, framerate, compDuration);
            case "el":
                return new LotteShapeCircle(json, framerate, compDuration);
            case "rc":
                return new LotteShapeRectangle(json, framerate, compDuration);
            case "tm":
                return new LotteShapeTrimPath(json, framerate, compDuration);
        }
        return null;
    }

    private String name;
    private final List<Object> items = new ArrayList<>();

    private LotteShapeGroup(JSONObject json, int frameRate, long compDuration, Rect compBounds) {
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


            Object newItem = shapeItemWithJson(jsonItem, frameRate, compDuration, compBounds);
            if (newItem != null) {
                items.add(newItem);
            }
        }
    }

    public List<Object> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "LotteShapeGroup{" + "name='" + name + '\'' + '}';
    }
}
