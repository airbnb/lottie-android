package com.airbnb.lotte.layers;

import android.graphics.Rect;

import com.airbnb.lotte.model.LotteAnimatableNumberValue;
import com.airbnb.lotte.model.LotteAnimatablePointValue;
import com.airbnb.lotte.model.LotteAnimatableScaleValue;
import com.airbnb.lotte.model.LotteShapeGroup;

import java.util.List;

public class LotteLayer {

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

    private String layerName;
    private long layerId;
    private LotteLayerType layerType;
    private long parentId;
    private long inFrame;
    private long outFrame;
    private Rect compBounds;
    private long frameRate;

    private List<LotteShapeGroup> shapes;
    private List<LotteMaskLayer> masks;

    private long solidWidth;
    private long solidHeight;
    private int solidColor;

    private LotteAnimatableNumberValue opacity;
    private LotteAnimatableNumberValue rotation;
    private LotteAnimatableNumberValue position;

    private LotteAnimatablePointValue anchor;
    private LotteAnimatableScaleValue scale;

    private boolean hasOutAnimation;
    private boolean hasInAnimation;
    private boolean hasInOutAnimation;
    private long[] inOutKeyFrames;
    private long[] inOutKeyTimes;
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

    public long[] getInOutKeyFrames() {
        return inOutKeyFrames;
    }

    public long[] getInOutKeyTimes() {
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

    public List<LotteMaskLayer> getMasks() {
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

    public LotteAnimatableNumberValue getPosition() {
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
