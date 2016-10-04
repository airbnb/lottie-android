package com.airbnb.lotte.model;

import android.graphics.Rect;
import android.util.LongSparseArray;

import com.airbnb.lotte.layers.LotteLayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LotteComposition {

    public static LotteComposition fromJson(JSONObject json) {
        LotteComposition composition = new LotteComposition();

        int width = -1;
        int height = -1;
        try {
            width = json.getInt("w");
            height = json.getInt("h");
        } catch (JSONException e) {
            // ignore.
        }
        if (width != -1 && height != -1) {
            composition.bounds = new Rect(0, 0, width, height);
        }

        try {
            composition.startFrame = json.getLong("ip");
            composition.endFrame = json.getLong("op");
            composition.frameRate = json.getInt("fr");
        } catch (JSONException e) {
            //
        }

        if (composition.endFrame != 0 && composition.frameRate != 0) {
            long frameDuration = composition.endFrame - composition.startFrame;
            composition.duration = (long) (frameDuration / (float) composition.frameRate * 1000);
        }

        try {
            JSONArray jsonLayers = json.getJSONArray("layers");
            for (int i = 0; i < jsonLayers.length(); i++) {
                LotteLayer layer = LotteLayer.fromJson(jsonLayers.getJSONObject(i), composition);
                composition.layers.add(layer);
                composition.layerMap.put(layer.getId(), layer);
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Unable to find layers.", e);
        }


        return composition;
    }

    private final LongSparseArray<LotteLayer> layerMap = new LongSparseArray<>();
    private final List<LotteLayer> layers = new ArrayList<>();
    private Rect bounds;
    private long startFrame;
    private long endFrame;
    private int frameRate;
    private long duration;

    public LotteLayer layerModelForId(long id) {
        return layerMap.get(id);
    }

    public Rect getBounds() {
        return bounds;
    }

    public void setBounds(Rect bounds) {
        this.bounds = bounds;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getEndFrame() {
        return endFrame;
    }

    public void setEndFrame(long endFrame) {
        this.endFrame = endFrame;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public LongSparseArray<LotteLayer> getLayerMap() {
        return layerMap;
    }

    public List<LotteLayer> getLayers() {
        return layers;
    }

    public long getStartFrame() {
        return startFrame;
    }

    public void setStartFrame(long startFrame) {
        this.startFrame = startFrame;
    }
}
