package com.airbnb.lotte.layers;

import android.graphics.Camera;
import android.graphics.Point;

import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.model.LotteKeyframeAnimation;
import com.airbnb.lotte.model.LotteShapeFill;
import com.airbnb.lotte.model.LotteShapeGroup;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.model.LotteShapeTransform;
import com.airbnb.lotte.model.LotteShapeTrimPath;
import com.airbnb.lotte.utils.LotteAnimationGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LotteLayerView extends LotteAnimatableLayer {

    private final LotteLayer layerModel;
    private final LotteComposition composition;

    private List<LotteGroupLayerView> shapeLayers = new ArrayList<>();
    private LotteLayer childContainerLayer;
    private LotteLayer rotationLayer;
    private LotteAnimationGroup animation;
    private LotteKeyframeAnimation inOutAnimation;
    private List<LotteParentLayer> parentLayers;
    private LotteMaskLayer maskLayer;
    private LotteLayerView mask;


    public LotteLayerView(LotteLayer layerModel, LotteComposition composition) {
        super(composition.getDuration());
        this.layerModel = layerModel;
        this.composition = composition;
        setBounds(composition.getBounds());
        setupForModel();
    }

    private void setupForModel() {
        setBounds(composition.getBounds());
        anchorPoint = new Point();

        LotteAnimatableLayer childContainerLayer = new LotteAnimatableLayer(0);
        childContainerLayer.setBackgroundColor(layerModel.getSolidColor());
        childContainerLayer.setBounds(0, 0, layerModel.getSolidWidth(), layerModel.getSolidHeight());

        long parentId = layerModel.getParentId();
        LotteAnimatableLayer currentChild = childContainerLayer;
        parentLayers = new ArrayList<>();
        while (parentId >= 0) {
            LotteLayer parentModel = composition.layerModelForId(parentId);
            LotteParentLayer parentLayer = new LotteParentLayer(parentModel, composition);
            parentLayer.addLayer(currentChild);
            parentLayers.add(parentLayer);
            currentChild = parentLayer;
            parentId = parentModel.getParentId();
        }

        childContainerLayer.setAlpha((int) (layerModel.getOpacity().getInitialValue() * 255));
        childContainerLayer.position = layerModel.getPosition().getInitialPoint();
        childContainerLayer.anchorPoint = layerModel.getAnchor().getInitialPoint();
        childContainerLayer.transform = layerModel.getScale().getInitialScale();
        childContainerLayer.sublayerTransform = new Camera();
        childContainerLayer.sublayerTransform.rotateZ(layerModel.getRotation().getInitialValue());
        setVisible(layerModel.isHasInAnimation(), false);

        List<Object> reversedItems = layerModel.getShapes();
        Collections.reverse(reversedItems);
        LotteShapeTransform currentTransform = null;
        LotteShapeTrimPath currentTrimPath = null;
        LotteShapeFill currentFill = null;
        LotteShapeStroke currentStroke = null;

        for (Object item : reversedItems) {
            if (item instanceof LotteShapeGroup) {
                LotteGroupLayerView groupLayer = new LotteGroupLayerView((LotteShapeGroup) item, currentTransform, currentFill, currentStroke,
                        currentTrimPath, duration);
                childContainerLayer.addLayer(groupLayer);
                shapeLayers.add(groupLayer);
            } else if (item instanceof LotteShapeTransform) {
                currentTransform = (LotteShapeTransform) item;
            } else if (item instanceof LotteShapeFill) {
                currentFill = (LotteShapeFill) item;
            } else if (item instanceof LotteShapeTrimPath) {
                currentTrimPath = (LotteShapeTrimPath) item;
            } else if (item instanceof LotteShapeStroke) {
                currentStroke = (LotteShapeStroke) item;
            }
        }

        if (layerModel.getMasks() != null) {
            maskLayer = new LotteMaskLayer(layerModel.getMasks(), composition);
            childContainerLayer.setMask(maskLayer);
        }

        List<Object> childLayers = new ArrayList<>(parentLayers.size() + shapeLayers.size() + (maskLayer == null ? 0 : 1));
        childLayers.addAll(parentLayers);
        childLayers.addAll(shapeLayers);
        if (maskLayer != null) {
            childLayers.add(maskLayer);
        }
        buildAnimations();
    }

    private void buildAnimations() {
        // TODO
    }

    public long getId() {
        return layerModel.getId();
    }

    public LotteMaskLayer getMaskLayer() {
        return maskLayer;
    }

    public void setMaskLayer(LotteMaskLayer maskLayer) {
        this.maskLayer = maskLayer;
    }

    public void setMask(LotteLayerView mask) {
        this.mask = mask;
    }
}
