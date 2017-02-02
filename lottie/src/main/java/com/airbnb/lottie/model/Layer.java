package com.airbnb.lottie.model;

import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.animatable.AnimatableFloatValue;
import com.airbnb.lottie.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.animatable.AnimatablePathValue;
import com.airbnb.lottie.animatable.AnimatableScaleValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class Layer implements Transform {
    private static final String TAG = Layer.class.getSimpleName();
    private final LottieComposition composition;

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

    static Layer fromJson(JSONObject json, LottieComposition composition) {
        Layer layer = new Layer(composition);
        try {
            if (L.DBG) Log.d(TAG, "Parsing new layer.");
            layer.layerName = json.getString("nm");
            if (L.DBG) Log.d(TAG, "\tName=" + layer.layerName);
            layer.layerId = json.getLong("ind");
            if (L.DBG) Log.d(TAG, "\tId=" + layer.layerId);
            layer.frameRate = composition.getFrameRate();

            int layerType = json.getInt("ty");
            if (layerType <= LottieLayerType.Shape.ordinal()) {
                layer.layerType = LottieLayerType.values()[layerType];
            } else {
                layer.layerType = LottieLayerType.Unknown;
            }

            try {
                layer.parentId = json.getLong("parent");
            } catch (JSONException e) {
                // Do nothing.
            }
            layer.inFrame = json.getLong("ip");
            layer.outFrame = json.getLong("op");
            if (L.DBG) Log.d(TAG, "\tFrames=" + layer.inFrame + "->" + layer.outFrame);

            if (layer.layerType == LottieLayerType.Solid) {
                layer.solidWidth = (int) (json.getInt("sw") * composition.getScale());
                layer.solidHeight = (int) (json.getInt("sh") * composition.getScale());
                layer.solidColor = Color.parseColor(json.getString("sc"));
                if (L.DBG) {
                    Log.d(TAG, "\tSolid=" + Integer.toHexString(layer.solidColor) + " " +
                            layer.solidWidth + "x" + layer.solidHeight + " " + composition.getBounds());
                }
            }

            JSONObject ks = json.getJSONObject("ks");

            JSONObject opacity = null;
            try {
                opacity = ks.getJSONObject("o");
            } catch (JSONException e) {
                // Do nothing.
            }
            if (opacity != null) {
                layer.opacity = new AnimatableIntegerValue(opacity, layer.frameRate, composition, false, true);
                if (L.DBG) Log.d(TAG, "\tOpacity=" + layer.opacity.getInitialValue());
            }

            JSONObject rotation;
            try {
                rotation = ks.getJSONObject("r");
            } catch (JSONException e) {
                rotation = ks.getJSONObject("rz");
            }

            if (rotation != null) {
                layer.rotation = new AnimatableFloatValue(rotation, layer.frameRate, composition, false);
                if (L.DBG) Log.d(TAG, "\tRotation=" + layer.rotation.getInitialValue());
            }

            JSONObject position = null;
            try {
                position = ks.getJSONObject("p");
            } catch (JSONException e) {
                // Do nothing.
            }
            if (position != null) {
                layer.position = new AnimatablePathValue(position, layer.frameRate, composition);
                if (L.DBG) Log.d(TAG, "\tPosition=" + layer.getPosition().toString());
            }

            JSONObject anchor = null;
            try {
                anchor = ks.getJSONObject("a");
            } catch (JSONException e) {
                // DO nothing.
            }
            if (anchor != null) {
                layer.anchor = new AnimatablePathValue(anchor, layer.frameRate, composition);
                if (L.DBG) Log.d(TAG, "\tAnchor=" + layer.anchor.toString());
            }

            JSONObject scale = null;
            try {
                scale = ks.getJSONObject("s");
            } catch (JSONException e) {
                // Do nothing.
            }
            if (scale != null) {
                layer.scale = new AnimatableScaleValue(scale, layer.frameRate, composition, false);
                if (L.DBG) Log.d(TAG, "\tScale=" + layer.scale.toString());
            }

            try {
                layer.matteType = MatteType.values()[json.getInt("tt")];
                if (L.DBG) Log.d(TAG, "\tMatte=" + layer.matteType);
            } catch (JSONException e) {
                // Do nothing.
            }

            JSONArray jsonMasks = null;
            try {
                jsonMasks = json.getJSONArray("masksProperties");
            } catch (JSONException e) {
                // Do nothing.
            }
            if (jsonMasks != null) {
                for (int i = 0; i < jsonMasks.length(); i++) {
                    Mask mask = new Mask(jsonMasks.getJSONObject(i), layer.frameRate, composition);
                    layer.masks.add(mask);
                    if (L.DBG) Log.d(TAG, "\tMask=" + mask.getMaskMode());
                }
            }

            JSONArray shapes = null;
            try {
                shapes = json.getJSONArray("shapes");
            } catch (JSONException e) {
                // Do nothing.
            }
            if (shapes != null) {
                for (int i = 0; i < shapes.length(); i++) {
                    Object shape = ShapeGroup.shapeItemWithJson(shapes.getJSONObject(i), layer.frameRate, composition);
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
    private int frameRate;

    private final List<Mask> masks = new ArrayList<>();

    private int solidWidth;
    private int solidHeight;
    private int solidColor;

    private AnimatableIntegerValue opacity;
    private AnimatableFloatValue rotation;
    private AnimatablePathValue position;

    private AnimatablePathValue anchor;
    private AnimatableScaleValue scale;

    private boolean hasOutAnimation;
    private boolean hasInAnimation;
    private boolean hasInOutAnimation;
    @Nullable private List<Float> inOutKeyFrames;
    @Nullable private List<Float> inOutKeyTimes;

    private MatteType matteType;

    private Layer(LottieComposition composition) {
        this.composition = composition;
    }

    @Override
    public Rect getBounds() {
        return composition.getBounds();
    }

    @Override
    public AnimatablePathValue getAnchor() {
        return anchor;
    }

    public LottieComposition getComposition() {
        return composition;
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

    public String getName() {
        return layerName;
    }

    public List<Mask> getMasks() {
        return masks;
    }

    public MatteType getMatteType() {
        return matteType;
    }

    @Override
    public AnimatableIntegerValue getOpacity() {
        return opacity;
    }

    public long getParentId() {
        return parentId;
    }

    @Override
    public AnimatablePathValue getPosition() {
        return position;
    }

    @Override
    public AnimatableFloatValue getRotation() {
        return rotation;
    }

    @Override
    public AnimatableScaleValue getScale() {
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
        return toString("");
    }

    public String toString(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(getName()).append("\n");
        Layer parent = composition.layerModelForId(getParentId());
        if (parent != null) {
            sb.append("\t\tParents: ").append(parent.getName());
            parent = composition.layerModelForId(parent.getParentId());
            while (parent != null) {
                sb.append("->").append(parent.getName());
                parent = composition.layerModelForId(parent.getParentId());
            }
            sb.append(prefix).append("\n");
        }
        if (getPosition().hasAnimation() || getPosition().getInitialPoint().length() != 0) {
            sb.append(prefix).append("\tPosition: ").append(getPosition()).append("\n");
        }
        if (getRotation().hasAnimation() || getRotation().getInitialValue() != 0f) {
            sb.append(prefix).append("\tRotation: ").append(getRotation()).append("\n");
        }
        if (getScale().hasAnimation() || !getScale().getInitialValue().isDefault()) {
            sb.append(prefix).append("\tScale: ").append(getScale()).append("\n");
        }
        if (getAnchor().hasAnimation() || getAnchor().getInitialPoint().length() != 0) {
            sb.append(prefix).append("\tAnchor: ").append(getAnchor()).append("\n");
        }
        if (!getMasks().isEmpty()) {
            sb.append(prefix).append("\tMasks: ").append(getMasks().size()).append("\n");
        }
        if (getSolidWidth() != 0 && getSolidHeight() != 0) {
            sb.append(prefix).append("\tBackground: ").append(String.format(Locale.US, "%dx%d %X\n", getSolidWidth(), getSolidHeight(), getSolidColor()));
        }
        if (!shapes.isEmpty()) {
            sb.append(prefix).append("\tShapes:\n");
            for (Object shape : shapes) {
                sb.append(prefix).append("\t\t").append(shape).append("\n");
            }
        }
        return sb.toString();
    }
}
