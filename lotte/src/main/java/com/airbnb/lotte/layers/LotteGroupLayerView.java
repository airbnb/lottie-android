package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.airbnb.lotte.model.LotteShapeCircle;
import com.airbnb.lotte.model.LotteShapeFill;
import com.airbnb.lotte.model.LotteShapeGroup;
import com.airbnb.lotte.model.LotteShapePath;
import com.airbnb.lotte.model.LotteShapeRectangle;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.model.LotteShapeTransform;
import com.airbnb.lotte.model.LotteShapeTrimPath;
import com.airbnb.lotte.animation.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LotteGroupLayerView extends LotteAnimatableLayer {

    private final List<LotteGroupLayerView> groupLayers = new ArrayList<>();
    private final List<LotteAnimatableLayer> shapeLayers = new ArrayList<>();

    private LotteShapeGroup shapeGroup;
    @Nullable private LotteShapeTransform shapeTransform;
    private LotteAnimationGroup animation;

    public LotteGroupLayerView(LotteShapeGroup shapeGroup, @Nullable LotteShapeFill previousFill,
            @Nullable LotteShapeStroke previousStroke, @Nullable LotteShapeTrimPath previousTrimPath,
            @Nullable LotteShapeTransform previousTransform, long duration) {
        super(duration);
        this.shapeGroup = shapeGroup;
        shapeTransform = previousTransform;
        setupShapeGroupWithFill(previousFill, previousStroke, previousTrimPath);
    }

    private void setupShapeGroupWithFill(LotteShapeFill previousFill,
            LotteShapeStroke previousStroke, LotteShapeTrimPath previousTrimPath) {
        if (shapeTransform != null) {
            setBounds(shapeTransform.getCompBounds());
            anchorPoint = shapeTransform.getAnchor().getInitialPoint();
            position.setValue(shapeTransform.getPosition().getInitialPoint());
            setAlpha((int) (shapeTransform.getOpacity().getInitialValue()));
            transform = shapeTransform.getScale().getInitialScale();
            sublayerTransform = new LotteTransform3D();
            sublayerTransform.rotateZ(shapeTransform.getRotation().getInitialValue());
        }

        List<Object> reversedItems = shapeGroup.getItems();
        Collections.reverse(reversedItems);

        LotteShapeFill currentFill = previousFill;
        LotteShapeStroke currentStroke = previousStroke;
        LotteShapeTransform currentTransform = null;
        LotteShapeTrimPath currentTrim = previousTrimPath;

        for (Object item : reversedItems) {
            if (item instanceof LotteShapeTransform) {
                currentTransform = (LotteShapeTransform) item;
            } else if (item instanceof LotteShapeStroke) {
                currentStroke = (LotteShapeStroke) item;
            } else if (item instanceof LotteShapeFill) {
                currentFill = (LotteShapeFill) item;
            } else if (item instanceof LotteShapeTrimPath) {
                currentTrim = (LotteShapeTrimPath) item;
            } else if (item instanceof LotteShapePath) {
                LotteShapePath shapePath = (LotteShapePath) item;
                LotteShapeLayerView shapeLayer = new LotteShapeLayerView(shapePath, currentFill, currentStroke, currentTrim, currentTransform, duration);
                shapeLayers.add(shapeLayer);
                addLayer(shapeLayer);
            } else if (item instanceof LotteShapeRectangle) {
                LotteShapeRectangle shapeRect = (LotteShapeRectangle) item;
                LotteRectShapeLayer shapeLayer = new LotteRectShapeLayer(shapeRect, currentFill, currentStroke, currentTransform, duration);
                shapeLayers.add(shapeLayer);
                addLayer(shapeLayer);
            } else if (item instanceof LotteShapeCircle) {
                LotteShapeCircle shapeCircle = (LotteShapeCircle) item;
                LotteEllipseShapeLayer shapeLayer = new LotteEllipseShapeLayer(shapeCircle, currentFill, currentStroke, currentTrim, currentTransform, duration);
                shapeLayers.add(shapeLayer);
                addLayer(shapeLayer);
            } else if (item instanceof LotteShapeGroup) {
                LotteShapeGroup shapeGroup = (LotteShapeGroup) item;
                LotteGroupLayerView groupLayer = new LotteGroupLayerView(shapeGroup, currentFill, currentStroke, currentTrim, currentTransform, duration);
                groupLayers.add(groupLayer);
                addLayer(groupLayer);
            }
        }

        buildAnimation();
    }

    private void buildAnimation() {
        // TODO
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
    }
}
