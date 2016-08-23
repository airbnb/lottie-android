package com.airbnb.lotte.layers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.NonNull;

import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.model.LotteKeyframeAnimation;
import com.airbnb.lotte.model.LotteMask;
import com.airbnb.lotte.model.LotteShapeFill;
import com.airbnb.lotte.model.LotteShapeGroup;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.model.LotteShapeTransform;
import com.airbnb.lotte.model.LotteShapeTrimPath;
import com.airbnb.lotte.utils.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LotteLayerView extends LotteAnimatableLayer {

    private final Bitmap bitmap;
    private final Canvas canvas;
    private final Paint paint = new Paint();
    private final Paint maskPaint = new Paint();

    private final LotteLayer layerModel;
    private final LotteComposition composition;

    private List<LotteGroupLayerView> shapeLayers = new ArrayList<>();
    private LotteLayer childContainerLayer;
    private LotteLayer rotationLayer;
    private LotteAnimationGroup animation;
    private LotteKeyframeAnimation inOutAnimation;
    private List<LotteParentLayer> parentLayers;


    public LotteLayerView(LotteLayer layerModel, LotteComposition composition) {
        super(composition.getDuration());
        this.layerModel = layerModel;
        this.composition = composition;
        setBounds(composition.getBounds());
        bitmap = Bitmap.createBitmap(composition.getBounds().width(), composition.getBounds().height(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        setupForModel();
    }

    private void setupForModel() {
        setBounds(composition.getBounds());
        anchorPoint = new PointF();

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
        addLayer(currentChild);

        childContainerLayer.setAlpha((int) (layerModel.getOpacity().getInitialValue() * 255));
        childContainerLayer.position = layerModel.getPosition().getInitialPoint();
        childContainerLayer.anchorPoint = layerModel.getAnchor().getInitialPoint();
        childContainerLayer.transform = layerModel.getScale().getInitialScale();
        childContainerLayer.sublayerTransform = new LotteTransform3D();
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
                LotteGroupLayerView groupLayer = new LotteGroupLayerView((LotteShapeGroup) item, currentFill,
                        currentStroke, currentTrimPath, currentTransform, duration);
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
            mask = new LotteMaskLayer(layerModel.getMasks(), composition);
            childContainerLayer.setMask(mask);
        }
        buildAnimations();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int saveCount = canvas.save();
        super.draw(this.canvas);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.restoreToCount(saveCount);

        if (mask != null) {
            for (LotteMask m : mask.getMasks()) {
                canvas.drawPath(m.getMaskPath().getInitialShape(), maskPaint);
            }
        }
    }

    private void buildAnimations() {
        // TODO
    }

    public long getId() {
        return layerModel.getId();
    }
}
