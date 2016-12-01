package com.airbnb.lottie.model;

import android.graphics.Rect;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"EmptyCatchBlock"})
public class ShapeGroup {

    @Nullable
    public static Object shapeItemWithJson(JSONObject json, int framerate, LottieComposition composition, Rect compBounds) {
        String type = null;
        try {
            type = json.getString("ty");
        } catch (JSONException e) { }
        if (type == null) {
            throw new IllegalStateException("Shape has no type.");
        }

        switch (type) {
            case "gr":
                return new ShapeGroup(json, framerate, composition, compBounds);
            case "st":
                return new ShapeStroke(json, framerate, composition);
            case "fl":
                return new ShapeFill(json, framerate, composition);
            case "tr":
                return new ShapeTransform(json, framerate, composition, compBounds);
            case "sh":
                return new ShapePath(json, framerate, composition);
            case "el":
                return new CircleShape(json, framerate, composition);
            case "rc":
                return new RectangleShape(json, framerate, composition);
            case "tm":
                return new ShapeTrimPath(json, framerate, composition);
        }
        return null;
    }

    private String name;
    private final List<Object> items = new ArrayList<>();

    private ShapeGroup(JSONObject json, int frameRate, LottieComposition composition, Rect compBounds) {
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


            Object newItem = shapeItemWithJson(jsonItem, frameRate, composition, compBounds);
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
        return "ShapeGroup{" + "name='" + name + '\'' + '}';
    }
}
