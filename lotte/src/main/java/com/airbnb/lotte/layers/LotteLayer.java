package com.airbnb.lotte.layers;

import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;

import com.airbnb.lotte.L;
import com.airbnb.lotte.animation.LotteAnimatableFloatValue;
import com.airbnb.lotte.animation.LotteAnimatableNumberValue;
import com.airbnb.lotte.animation.LotteAnimatablePointValue;
import com.airbnb.lotte.animation.LotteAnimatableScaleValue;
import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.model.LotteMask;
import com.airbnb.lotte.model.LotteShapeGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"EmptyCatchBlock"})
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
                if (layer.parentId != -1 && L.DBG) {
                    long parentId = layer.parentId;
                    List<String> parentNames = new ArrayList<>();
                    LotteLayer parent = composition.layerModelForId(parentId);
                    while (parent != null) {
                        parentNames.add(parent.getLayerName());
                        parent = composition.layerModelForId(parent.getParentId());
                    }
                    Log.d(TAG, "\tParents=" + Arrays.toString(parentNames.toArray()));
                }
            } catch (JSONException e) {
            }
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
            } catch (JSONException e) {
            }
            if (opacity != null) {
                layer.opacity = new LotteAnimatableNumberValue(opacity, layer.frameRate, composition.getDuration());
                layer.opacity.remapValues(0, 100, 0, 255);
                if (L.DBG) Log.d(TAG, "\tOpacity=" + layer.opacity.getInitialValue());
            }

            JSONObject rotation = null;
            try {
                rotation = ks.getJSONObject("r");
            } catch (JSONException e) {
            }
            if (rotation != null) {
                layer.rotation = new LotteAnimatableFloatValue(rotation, layer.frameRate, composition.getDuration());
                if (L.DBG) Log.d(TAG, "\tRotation=" + layer.rotation.getInitialValue());
            }

            JSONObject position = null;
            try {
                position = ks.getJSONObject("p");
            } catch (JSONException e) {
            }
            if (position != null) {
                layer.position = new LotteAnimatablePointValue(position, layer.frameRate, composition.getDuration());
                if (L.DBG) Log.d(TAG, "\tPosition=" + layer.getPosition().getInitialPoint());
            }

            JSONObject anchor = null;
            try {
                anchor = ks.getJSONObject("a");
            } catch (JSONException e) {
            }
            if (anchor != null) {
                layer.anchor = new LotteAnimatablePointValue(anchor, layer.frameRate, composition.getDuration());
                layer.anchor.setUsePathAnimation(false);
                if (L.DBG) Log.d(TAG, "\tAnchor=" + layer.anchor.getInitialPoint());
            }

            JSONObject scale = null;
            try {
                scale = ks.getJSONObject("s");
            } catch (JSONException e) {
            }
            if (scale != null) {
                layer.scale = new LotteAnimatableScaleValue(scale, layer.frameRate, composition.getDuration());
                if (L.DBG) Log.d(TAG, "\tScale=" + layer.scale.getInitialScale());
            }

            try {
                layer.matteType = MatteType.values()[json.getInt("tt")];
                if (L.DBG) Log.d(TAG, "\tMatte=" + layer.matteType);
            } catch (JSONException e) {
            }

            JSONArray jsonMasks = null;
            try {
                jsonMasks = json.getJSONArray("masksProperties");
            } catch (JSONException e) {
            }
            if (jsonMasks != null) {
                for (int i = 0; i < jsonMasks.length(); i++) {
                    LotteMask mask = new LotteMask(jsonMasks.getJSONObject(i), layer.frameRate, composition.getDuration());
                    layer.masks.add(mask);
                    if (L.DBG) Log.d(TAG, "\tMask=" + mask.getMaskMode());
                }
            }

            JSONArray shapes = null;
            try {
                shapes = json.getJSONArray("shapes");
            } catch (JSONException e) {
            }
            if (shapes != null) {
                for (int i = 0; i < shapes.length(); i++) {
                    Object shape = LotteShapeGroup.shapeItemWithJson(shapes.getJSONObject(i), layer.frameRate, composition.getDuration(), layer.compBounds);
                    if (shape != null) {
                        layer.shapes.add(shape);
                        if (L.DBG) Log.d(TAG, "\tShapes+=" + shape.getClass().getSimpleName());
                    }
                }
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Unable to parse layer json.", e);
        }

        layer.hasInAnimation = layer.inFrame > composition.getStartFrame();
        layer.hasOutAnimation = layer.outFrame < composition.getEndFrame();
        layer.hasInOutAnimation = layer.hasInAnimation || layer.hasOutAnimation;

        if (layer.hasInOutAnimation) {
            List<Float> keys = new ArrayList<>();
            List<Float> keyTimes = new ArrayList<>();
            long length = composition.getEndFrame() - composition.getStartFrame();

            if (layer.hasInAnimation) {
                keys.add(0f);
                keyTimes.add(0f);
                keys.add(1f);
                float inTime = layer.inFrame / (float) length;
                keyTimes.add(inTime);
            } else {
                keys.add(1f);
                keyTimes.add(0f);
            }

            if (layer.hasOutAnimation) {
                keys.add(0f);
                keyTimes.add(layer.outFrame / (float) length);
                keys.add(0f);
                keyTimes.add(1f);
            } else {
                keys.add(1f);
                keyTimes.add(1f);
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

    private final List<LotteMask> masks = new ArrayList<>();

    private int solidWidth;
    private int solidHeight;
    private int solidColor;

    private LotteAnimatableNumberValue opacity;
    private LotteAnimatableFloatValue rotation;
    private LotteAnimatablePointValue position;

    private LotteAnimatablePointValue anchor;
    private LotteAnimatableScaleValue scale;

    private boolean hasOutAnimation;
    private boolean hasInAnimation;
    private boolean hasInOutAnimation;
    @Nullable private List<Float> inOutKeyFrames;
    @Nullable private List<Float> inOutKeyTimes;
    private long compDuration;

    private MatteType matteType;

    LotteAnimatablePointValue getAnchor() {
        return anchor;
    }

    Rect getCompBounds() {
        return compBounds;
    }

    public long getCompDuration() {
        return compDuration;
    }

    boolean hasInAnimation() {
        return hasInAnimation;
    }

    boolean hasInOutAnimation() {
        return hasInOutAnimation;
    }

    @Nullable
    List<Float> getInOutKeyFrames() {
        return inOutKeyFrames;
    }

    @Nullable
    List<Float> getInOutKeyTimes() {
        return inOutKeyTimes;
    }

    public long getId() {
        return layerId;
    }

    String getLayerName() {
        return layerName;
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

    long getParentId() {
        return parentId;
    }

    public LotteAnimatablePointValue getPosition() {
        return position;
    }

    LotteAnimatableFloatValue getRotation() {
        return rotation;
    }

    LotteAnimatableScaleValue getScale() {
        return scale;
    }

    public List<Object> getShapes() {
        return shapes;
    }

    int getSolidColor() {
        return solidColor;
    }

    int getSolidHeight() {
        return solidHeight;
    }

    int getSolidWidth() {
        return solidWidth;
    }

    @Override
    public String toString() {
        return "LotteLayer{" + "layerName='" + layerName +
                ", anchor=" + anchor +
                ", shapes=" + shapes +
                ", layerId=" + layerId +
                ", layerType=" + layerType +
                ", parentId=" + parentId +
                ", inFrame=" + inFrame +
                ", outFrame=" + outFrame +
                ", compBounds=" + compBounds +
                ", frameRate=" + frameRate +
                ", masks=" + masks +
                ", solidWidth=" + solidWidth +
                ", solidHeight=" + solidHeight +
                ", solidColor=" + solidColor +
                ", opacity=" + opacity +
                ", rotation=" + rotation +
                ", position=" + position +
                ", scale=" + scale +
                ", hasOutAnimation=" + hasOutAnimation +
                ", hasInAnimation=" + hasInAnimation +
                ", hasInOutAnimation=" + hasInOutAnimation +
                ", inOutKeyFrames=" + inOutKeyFrames +
                ", inOutKeyTimes=" + inOutKeyTimes +
                ", compDuration=" + compDuration +
                ", matteType=" + matteType +
                '}';
    }
}
