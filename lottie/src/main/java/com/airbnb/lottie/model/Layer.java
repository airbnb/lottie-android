package com.airbnb.lottie.model;

import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.animatable.AnimatableFloatValue;
import com.airbnb.lottie.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.animatable.AnimatablePathValue;
import com.airbnb.lottie.animatable.AnimatablePointValue;
import com.airbnb.lottie.animatable.AnimatableScaleValue;
import com.airbnb.lottie.animatable.AnimationGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"EmptyCatchBlock"})
public class Layer {
    private static final String TAG = Layer.class.getSimpleName();

    @SuppressWarnings("WeakerAccess")
    public enum LottieLayerType {
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

    @SuppressWarnings("UnusedAssignment")
    static Layer fromJson(JSONObject json, Composition composition) {
        Layer layer = new Layer();
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
            if (layerType <= LottieLayerType.Shape.ordinal()) {
                layer.layerType = LottieLayerType.values()[layerType];
            } else {
                layer.layerType = LottieLayerType.Unknown;
            }

            try {
                layer.parentId = json.getLong("parent");
                if (layer.parentId != -1 && L.DBG) {
                    long parentId = layer.parentId;
                    List<String> parentNames = new ArrayList<>();
                    Layer parent = composition.layerModelForId(parentId);
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

            if (layer.layerType == LottieLayerType.Solid) {
                layer.solidWidth = (int) (json.getInt("sw") * L.SCALE);
                layer.solidHeight = (int) (json.getInt("sh") * L.SCALE);
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
                layer.opacity = new AnimatableIntegerValue(opacity, layer.frameRate, composition.getDuration(), false);
                layer.opacity.remap100To255();
                if (L.DBG) Log.d(TAG, "\tOpacity=" + layer.opacity.getInitialValue());
            }

            JSONObject rotation = null;
            try {
                rotation = ks.getJSONObject("r");
            } catch (JSONException e) {
            }
            if (rotation != null) {
                layer.rotation = new AnimatableFloatValue(rotation, layer.frameRate, composition.getDuration(), false);
                if (L.DBG) Log.d(TAG, "\tRotation=" + layer.rotation.getInitialValue());
            }

            JSONObject position = null;
            try {
                position = ks.getJSONObject("p");
            } catch (JSONException e) {
            }
            if (position != null) {
                layer.position = new AnimatablePointValue(position, layer.frameRate, composition.getDuration());
                if (L.DBG) Log.d(TAG, "\tPosition=" + layer.getPosition().toString());
            }

            JSONObject anchor = null;
            try {
                anchor = ks.getJSONObject("a");
            } catch (JSONException e) {
            }
            if (anchor != null) {
                layer.anchor = new AnimatablePathValue(anchor, layer.frameRate, composition.getDuration());
                if (L.DBG) Log.d(TAG, "\tAnchor=" + layer.anchor.toString());
            }

            JSONObject scale = null;
            try {
                scale = ks.getJSONObject("s");
            } catch (JSONException e) {
            }
            if (scale != null) {
                layer.scale = new AnimatableScaleValue(scale, layer.frameRate, composition.getDuration(), false);
                if (L.DBG) Log.d(TAG, "\tScale=" + layer.scale.toString());
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
                    Mask mask = new Mask(jsonMasks.getJSONObject(i), layer.frameRate, composition.getDuration());
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
                    Object shape = ShapeGroup.shapeItemWithJson(shapes.getJSONObject(i), layer.frameRate, composition.getDuration(), layer.compBounds);
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
    private LottieLayerType layerType;
    private long parentId = -1;
    private long inFrame;
    private long outFrame;
    private Rect compBounds;
    private int frameRate;

    private final List<Mask> masks = new ArrayList<>();

    private int solidWidth;
    private int solidHeight;
    private int solidColor;

    private AnimatableIntegerValue opacity;
    private AnimatableFloatValue rotation;
    private AnimatablePointValue position;

    private AnimatablePathValue anchor;
    private AnimatableScaleValue scale;

    private boolean hasOutAnimation;
    private boolean hasInAnimation;
    private boolean hasInOutAnimation;
    @Nullable private List<Float> inOutKeyFrames;
    @Nullable private List<Float> inOutKeyTimes;
    private long compDuration;

    private MatteType matteType;

    public AnimatablePathValue getAnchor() {
        return anchor;
    }

    public Rect getCompBounds() {
        return compBounds;
    }

    public long getCompDuration() {
        return compDuration;
    }

    public boolean hasInAnimation() {
        return hasInAnimation;
    }

    public boolean hasInOutAnimation() {
        return hasInOutAnimation;
    }

    @Nullable
    public List<Float> getInOutKeyFrames() {
        return inOutKeyFrames;
    }

    @Nullable
    public List<Float> getInOutKeyTimes() {
        return inOutKeyTimes;
    }

    public long getId() {
        return layerId;
    }

    public String getLayerName() {
        return layerName;
    }

    public List<Mask> getMasks() {
        return masks;
    }

    public MatteType getMatteType() {
        return matteType;
    }

    public AnimatableIntegerValue getOpacity() {
        return opacity;
    }

    public long getParentId() {
        return parentId;
    }

    public AnimatablePointValue getPosition() {
        return position;
    }

    public AnimatableFloatValue getRotation() {
        return rotation;
    }

    public AnimatableScaleValue getScale() {
        return scale;
    }

    public List<Object> getShapes() {
        return shapes;
    }

    public AnimationGroup createAnimation() {
        return AnimationGroup.forAnimatableValues(getOpacity(), getPosition(), getAnchor(), getScale(), getRotation());
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
        return "Layer{" + "layerName='" + layerName +
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
