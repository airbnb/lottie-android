package com.airbnb.lotte.layers;

import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;

import com.airbnb.lotte.L;
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
    private static final String TAG = LotteLayer.class.getSimpleName();

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
            if (L.DBG) Log.d(TAG, "Parsing new layer.");
            layer.layerName = json.getString("nm");
            if (L.DBG) Log.d(TAG, "\tName=" + layer.layerName);
            layer.layerId = json.getLong("ind");
            if (L.DBG) Log.d(TAG, "\tId=" + layer.layerId);
            layer.compBounds = composition.getBounds();
            if (L.DBG) Log.d(TAG, "\tComp Bounds=" + composition.getBounds());
            layer.frameRate = composition.getFrameRate();

            int layerType = json.getInt("ty");
            if (layerType <= LotteLayerType.Shape.ordinal()) {
                layer.layerType = LotteLayerType.values()[layerType];
            } else {
                layer.layerType = LotteLayerType.Unknown;
            }

            try {
                layer.parentId = json.getLong("parent");
                if (L.DBG) Log.d(TAG, "\tparentId=" + layer.parentId);
            } catch (JSONException e) { }
            layer.inFrame = json.getLong("ip");
            layer.outFrame = json.getLong("op");
            if (L.DBG) Log.d(TAG, "\tFrames=" + layer.inFrame + "->" + layer.outFrame);

            if (layer.layerType == LotteLayerType.Solid) {
                layer.solidWidth = json.getInt("sw");
                layer.solidHeight = json.getInt("sh");
                layer.compBounds = new Rect(0, 0, layer.solidWidth, layer.solidHeight);
                layer.solidColor = Color.parseColor(json.getString("sc"));
                if (L.DBG) {
                    Log.d(TAG, "\tSolid=" + Integer.toHexString(layer.solidColor) + " " +
                            layer.solidWidth + "x" + layer.solidHeight + " " + layer.compBounds);
                }
            }

            JSONObject ks = json.getJSONObject("ks");

            JSONObject opacity = null;
            try {
                opacity = ks.getJSONObject("o");
                if (L.DBG) Log.d(TAG, "\tOpacity=" + opacity);
            } catch (JSONException e) { }
            if (opacity != null) {
                layer.opacity = new LotteAnimatableNumberValue(opacity, layer.frameRate);
                layer.opacity.remapValues(0, 100, 0, 1);
            }

            JSONObject rotation = null;
            try {
                rotation = ks.getJSONObject("r");
            } catch (JSONException e) { }
            if (rotation != null) {
                layer.rotation = new LotteAnimatableNumberValue(rotation, layer.frameRate);
                if (L.DBG) Log.d(TAG, "\tRotation=" + layer.rotation.getInitialValue());
                layer.rotation.remapWith(new RemapInterface() {
                    @Override
                    public float remap(float inValue) {
                        return (float) Math.toRadians(inValue);
                    }
                });
            }

            JSONObject position = null;
            try {
                position = ks.getJSONObject("p");
            } catch (JSONException e) { }
            if (position != null) {
                layer.position = new LotteAnimatablePointValue(position, layer.frameRate);
                if (L.DBG) Log.d(TAG, "\tPosition=" + layer.getPosition().getInitialPoint());
            }

            JSONObject anchor = null;
            try {
                anchor = ks.getJSONObject("a");
            } catch (JSONException e) { }
            if (anchor != null) {
                layer.anchor = new LotteAnimatablePointValue(anchor, layer.frameRate);
                layer.anchor.remapPointsFromBounds(new Rect(0, 0, 1, 1));
                layer.anchor.setUsePathAnimation(false);
                if (L.DBG) Log.d(TAG, "\tAnchor=" + layer.anchor.getInitialPoint());
            }

            JSONObject scale = null;
            try {
                scale = ks.getJSONObject("s");
            } catch (JSONException e) { }
            if (scale != null) {
                layer.scale = new LotteAnimatableScaleValue(scale, layer.frameRate);
                if (L.DBG) Log.d(TAG, "\tScale=" + layer.scale.getInitialScale());
            }

            try {
                layer.matteType = MatteType.values()[json.getInt("tt")];
            } catch (JSONException e) { }

            JSONArray jsonMasks = null;
            try {
                jsonMasks = json.getJSONArray("masksProperties");
            } catch (JSONException e) { }
            if (jsonMasks != null) {
                for (int i = 0; i < jsonMasks.length(); i++) {
                    LotteMask mask = new LotteMask(jsonMasks.getJSONObject(i), layer.frameRate);
                    layer.masks.add(mask);
                    if (L.DBG) Log.d(TAG, "\tMask=" + mask.getMaskMode());
                }
            }

            JSONArray shapes = null;
            try {
                shapes = json.getJSONArray("shapes");
            } catch (JSONException e) { }
            if (shapes != null) {
                for (int i = 0; i < shapes.length(); i++) {
                    layer.shapes.add(LotteShapeGroup.shapeItemWithJson(shapes.getJSONObject(i), layer.frameRate, layer.compBounds));
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

    private final List<Object> shapes = new ArrayList<>();

    private String layerName;
    private long layerId;
    private LotteLayerType layerType;
    private long parentId = -1;
    private long inFrame;
    private long outFrame;
    private Rect compBounds;
    private int frameRate;

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

    public List<Object> getShapes() {
        return shapes;
    }

    public int getSolidColor() {
        return solidColor;
    }

    public int getSolidHeight() {
        return solidHeight;
    }

    public int getSolidWidth() {
        return solidWidth;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteLayer{");
        sb.append("anchor=").append(anchor);
        sb.append(", shapes=").append(shapes);
        sb.append(", layerName='").append(layerName).append('\'');
        sb.append(", layerId=").append(layerId);
        sb.append(", layerType=").append(layerType);
        sb.append(", parentId=").append(parentId);
        sb.append(", inFrame=").append(inFrame);
        sb.append(", outFrame=").append(outFrame);
        sb.append(", compBounds=").append(compBounds);
        sb.append(", frameRate=").append(frameRate);
        sb.append(", masks=").append(masks);
        sb.append(", solidWidth=").append(solidWidth);
        sb.append(", solidHeight=").append(solidHeight);
        sb.append(", solidColor=").append(solidColor);
        sb.append(", opacity=").append(opacity);
        sb.append(", rotation=").append(rotation);
        sb.append(", position=").append(position);
        sb.append(", scale=").append(scale);
        sb.append(", hasOutAnimation=").append(hasOutAnimation);
        sb.append(", hasInAnimation=").append(hasInAnimation);
        sb.append(", hasInOutAnimation=").append(hasInOutAnimation);
        sb.append(", inOutKeyFrames=").append(inOutKeyFrames);
        sb.append(", inOutKeyTimes=").append(inOutKeyTimes);
        sb.append(", compDuration=").append(compDuration);
        sb.append(", matteType=").append(matteType);
        sb.append('}');
        return sb.toString();
    }
}
