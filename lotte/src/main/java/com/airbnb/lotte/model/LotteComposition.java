package com.airbnb.lotte.model;

import android.graphics.Rect;

import com.airbnb.lotte.layers.LotteLayer;

import org.json.JSONObject;

import java.util.List;

public class LotteComposition {

    private final JSONObject json;
    private List<LotteLayer> layers;
    private Rect bounds;
    private long startFrame;
    private long endFrame;
    private long frameRate;
    private long duration;

    public LotteComposition(JSONObject json) {
        this.json = json;
    }

    public LotteLayer layerModelForId(long id) {
        // TODO
        return null;
    }

    public List<LotteLayer> getLayers() {
        return layers;
    }

    public Rect getBounds() {
        return bounds;
    }
}
