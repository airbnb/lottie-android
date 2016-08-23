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
    private Bitmap maskBitmap;
    private final Canvas canvas;
    private Canvas maskCanvas;
    private final Paint individualMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint compositeMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

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
        float[] invertAlphaMatrix = {
                1, 0, 0, 0, 0,
                0, 1, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, -1, 255
        };
//        compositeMaskPaint.setColorFilter(new ColorMatrixColorFilter(invertAlphaMatrix));
        compositeMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//        contentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
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
            maskBitmap = Bitmap.createBitmap(composition.getBounds().width(), composition.getBounds().height(), Bitmap.Config.ARGB_8888);
            maskCanvas = new Canvas(maskBitmap);
//            childContainerLayer.setMask(mask);
        }
        buildAnimations();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int saveCount = canvas.save();
        super.draw(this.canvas);

        if (mask != null && !mask.getMasks().isEmpty()) {
            int maskSaveCount = maskCanvas.save();
            if (position != null) {
                maskCanvas.translate(position.x, position.y);
            }
            if (transform != null) {
                maskCanvas.scale(transform.getScaleX(), transform.getScaleY());
            }

            if (sublayerTransform != null) {
                maskCanvas.rotate(sublayerTransform.getRotationZ());
            }

            if (anchorPoint != null) {
                maskCanvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
                maskCanvas.translate(-anchorPoint.x, -anchorPoint.y);
            }

            for (LotteMask m : mask.getMasks()) {
                maskCanvas.drawPath(m.getMaskPath().getInitialShape(), individualMaskPaint);
            }
            maskCanvas.restoreToCount(maskSaveCount);
            canvas.drawBitmap(bitmap, 0, 0, individualMaskPaint /*contentPaint*/);
            this.canvas.drawBitmap(maskBitmap, 0, 0, compositeMaskPaint /*compositeMaskPaint*/);
            canvas.drawBitmap(bitmap, 0, 0, individualMaskPaint);
        } else {
            canvas.drawBitmap(bitmap, 0, 0, individualMaskPaint);
        }

        canvas.restoreToCount(saveCount);
    }

    private void buildAnimations() {
        // TODO
    }

    public long getId() {
        return layerModel.getId();
    }
}
