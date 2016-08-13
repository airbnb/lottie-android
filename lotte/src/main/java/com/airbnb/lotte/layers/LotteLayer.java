package com.airbnb.lotte.layers;

import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;

import com.airbnb.lotte.model.LotteAnimatableNumberValue;
import com.airbnb.lotte.model.LotteAnimatablePointValue;
import com.airbnb.lotte.model.LotteAnimatableScaleValue;
import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.model.LotteMask;
import com.airbnb.lotte.model.LotteShapeGroup;
import com.airbnb.lotte.model.RemapInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedAssignment", "unused", "EmptyCatchBlock"})
public class LotteLayer {

    @SuppressWarnings("WeakerAccess")
    public enum LotteLayerType {
        None,
        Solid,
        Unknown,
        Null,
        Shape
    }

    public enum MatteType {
        None,
        Add,
        Invert,
        Unknown
    }

    public static LotteLayer fromJson(JSONObject json, LotteComposition composition) {
        LotteLayer layer = new LotteLayer();
        try {
            layer.layerName = json.getString("nm");
            layer.layerId = json.getLong("ind");
            layer.compBounds = composition.getBounds();
            layer.frameRate = composition.getFrameRate();

            int layerType = json.getInt("ty");
            if (layerType <= LotteLayerType.Shape.ordinal()) {
                layer.layerType = LotteLayerType.values()[layerType];
            } else {
                layer.layerType = LotteLayerType.Unknown;
            }

            layer.parentId = json.getLong("parent");
            layer.inFrame = json.getLong("ip");
            layer.outFrame = json.getLong("op");

            if (layer.layerType == LotteLayerType.Solid) {
                layer.solidWidth = json.getInt("sw");
                layer.solidHeight = json.getInt("sh");
                layer.compBounds = new Rect(0, 0, layer.solidWidth, layer.solidHeight);
                layer.solidColor = Color.parseColor(json.getString("sc"));
            }

            JSONObject ks = json.getJSONObject("ks");

            JSONObject opacity = null;
            try {
                opacity = json.getJSONObject("o");
            } catch (JSONException e) { }
            if (opacity != null) {
                layer.opacity = new LotteAnimatableNumberValue(opacity, layer.frameRate);
                layer.opacity.remapValues(0, 100, 0, 1);
            }

            JSONObject rotation = null;
            try {
                rotation = json.getJSONObject("r");
            } catch (JSONException e) { }
            if (rotation != null) {
                layer.rotation = new LotteAnimatableNumberValue(rotation, layer.frameRate);
                layer.rotation.remapWith(new RemapInterface() {
                    @Override
                    public float remap(float inValue) {
                        return (float) Math.toRadians(inValue);
                    }
                });
            }

            JSONObject position = null;
            try {
                position = json.getJSONObject("p");
            } catch (JSONException e) { }
            if (position != null) {
                layer.position = new LotteAnimatablePointValue(position, layer.frameRate);
            }

            JSONObject anchor = null;
            try {
                anchor = json.getJSONObject("a");
            } catch (JSONException e) { }
            if (anchor != null) {
                layer.anchor = new LotteAnimatablePointValue(anchor, layer.frameRate);
                layer.anchor.remapPointsFromBounds(new Rect(0, 0, 1, 1));
                layer.anchor.setUsePathAnimation(false);
            }

            JSONObject scale = null;
            try {
                scale = json.getJSONObject("s");
            } catch (JSONException e) { }
            if (scale != null) {
                layer.scale = new LotteAnimatableScaleValue(scale, layer.frameRate);
            }

            layer.matteType = MatteType.values()[json.getInt("tt")];

            JSONArray jsonMasks = null;
            try {
                jsonMasks = json.getJSONArray("masksProperties");
            } catch (JSONException e) { }
            if (jsonMasks != null) {
                for (int i = 0; i < jsonMasks.length(); i++) {
                    LotteMask mask = new LotteMask(jsonMasks.getJSONObject(i), layer.frameRate);
                    layer.masks.add(mask);
                }
            }

            JSONArray shapes = null;
            try {
                shapes = json.getJSONArray("shapes");
            } catch (JSONException e) { }
            if (shapes != null) {
                for (int i = 0; i < shapes.length(); i++) {
                    LotteShapeGroup shapeGroup = new LotteShapeGroup(shapes.getJSONObject(i), layer.frameRate, layer.compBounds);
                    layer.shapes.add(shapeGroup);
                }
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Unable to parse layer json.", e);
        }

        layer.hasInAnimation = layer.inFrame > composition.getStartFrame();
        layer.hasOutAnimation = layer.outFrame < composition.getEndFrame();
        layer.hasInOutAnimation = layer.hasInOutAnimation || layer.hasOutAnimation;

        if (layer.hasInOutAnimation) {
            List<Long> keys = new ArrayList<>();
            List<Long> keyTimes = new ArrayList<>();
            long length = composition.getEndFrame() - composition.getStartFrame();

            if (layer.hasInAnimation) {
                keys.add(1L);
                keyTimes.add(0L);
                keys.add(0L);
                float inTime = layer.inFrame / (float) length;
                keyTimes.add((long) inTime);
            } else {
                keys.add(0L);
                keyTimes.add(0L);
            }

            if (layer.hasOutAnimation) {
                keys.add(1L);
                float outTime = layer.outFrame / (float) length;
                keyTimes.add((long) outTime);
                keys.add(1L);
                keyTimes.add(1L);
            } else {
                keys.add(0L);
                keyTimes.add(1L);
            }

            layer.compDuration = composition.getDuration();
            layer.inOutKeyTimes = keyTimes;
            layer.inOutKeyFrames = keys;

        }

        return layer;
    }

    private String layerName;
    private long layerId;
    private LotteLayerType layerType;
    private long parentId;
    private long inFrame;
    private long outFrame;
    private Rect compBounds;
    private long frameRate;

    private List<LotteShapeGroup> shapes;
    private List<LotteMask> masks;

    private int solidWidth;
    private int solidHeight;
    private int solidColor;

    private LotteAnimatableNumberValue opacity;
    private LotteAnimatableNumberValue rotation;
    private LotteAnimatablePointValue position;

    private LotteAnimatablePointValue anchor;
    private LotteAnimatableScaleValue scale;

    private boolean hasOutAnimation;
    private boolean hasInAnimation;
    private boolean hasInOutAnimation;
    @Nullable private List<Long> inOutKeyFrames;
    @Nullable private List<Long> inOutKeyTimes;
    private long compDuration;

    private MatteType matteType;

    public LotteAnimatablePointValue getAnchor() {
        return anchor;
    }

    public Rect getCompBounds() {
        return compBounds;
    }

    public long getCompDuration() {
        return compDuration;
    }

    public long getFrameRate() {
        return frameRate;
    }

    public boolean isHasInAnimation() {
        return hasInAnimation;
    }

    public boolean isHasInOutAnimation() {
        return hasInOutAnimation;
    }

    public boolean isHasOutAnimation() {
        return hasOutAnimation;
    }

    public long getInFrame() {
        return inFrame;
    }

    @Nullable
    public List<Long> getInOutKeyFrames() {
        return inOutKeyFrames;
    }

    @Nullable
    public List<Long> getInOutKeyTimes() {
        return inOutKeyTimes;
    }

    public long getId() {
        return layerId;
    }

    public String getLayerName() {
        return layerName;
    }

    public LotteLayerType getLayerType() {
        return layerType;
    }

    public List<LotteMask> getMasks() {
        return masks;
    }

    public MatteType getMatteType() {
        return matteType;
    }

    public LotteAnimatableNumberValue getOpacity() {
        return opacity;
    }

    public long getOutFrame() {
        return outFrame;
    }

    public long getParentId() {
        return parentId;
    }

    public LotteAnimatablePointValue getPosition() {
        return position;
    }

    public LotteAnimatableNumberValue getRotation() {
        return rotation;
    }

    public LotteAnimatableScaleValue getScale() {
        return scale;
    }

    public List<LotteShapeGroup> getShapes() {
        return shapes;
    }

    public int getSolidColor() {
        return solidColor;
    }

    public long getSolidHeight() {
        return solidHeight;
    }

    public long getSolidWidth() {
        return solidWidth;
    }
}
