package com.airbnb.lotte.layers;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
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
    private LotteAnimatableLayer childContainerLayer;
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
        individualMaskPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas = new Canvas(bitmap);
        float[] invertAlphaMatrix = {
                1, 0, 0, 0, 0,
                0, 1, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, -1, 255
        };
//        compositeMaskPaint.setColorFilter(new ColorMatrixColorFilter(invertAlphaMatrix));
//        compositeMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//        contentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        setupForModel();
    }

    private void setupForModel() {
        setBounds(composition.getBounds());
        anchorPoint = new PointF();

        childContainerLayer =  new LotteAnimatableLayer(0);
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
//        position = layerModel.getPosition().getInitialPoint();
//        anchorPoint = layerModel.getAnchor().getInitialPoint();
//        transform = layerModel.getScale().getInitialScale();
//        sublayerTransform = new LotteTransform3D();
//        sublayerTransform.rotateZ(layerModel.getRotation().getInitialValue());


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
        }
        buildAnimations();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(this.canvas);

        if (mask != null && !mask.getMasks().isEmpty()) {
            int saveCount = this.canvas.save();
            int maskSaveCount = maskCanvas.save();
            if (childContainerLayer.position != null) {
                this.canvas.translate(childContainerLayer.position.x, childContainerLayer.position.y);
                maskCanvas.translate(childContainerLayer.position.x, childContainerLayer.position.y);
            }
            if (childContainerLayer.transform != null) {
                this.canvas.scale(childContainerLayer.transform.getScaleX(), childContainerLayer.transform.getScaleY());
                maskCanvas.scale(childContainerLayer.transform.getScaleX(), childContainerLayer.transform.getScaleY());
            }

            if (childContainerLayer.sublayerTransform != null) {
                this.canvas.rotate(childContainerLayer.sublayerTransform.getRotationZ());
                maskCanvas.rotate(childContainerLayer.sublayerTransform.getRotationZ());
            }

            if (childContainerLayer.anchorPoint != null) {
                this.canvas.translate(-childContainerLayer.anchorPoint.x, -childContainerLayer.anchorPoint.y);
                maskCanvas.translate(-childContainerLayer.anchorPoint.x, -childContainerLayer.anchorPoint.y);
            }

            for (LotteMask m : mask.getMasks()) {
                maskCanvas.drawPath(m.getMaskPath().getInitialShape(), new Paint());
            }
            this.canvas.restoreToCount(saveCount);
            maskCanvas.restoreToCount(maskSaveCount);
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            maskCanvas.drawBitmap(bitmap, 0, 0, paint);
            canvas.drawBitmap(maskBitmap, 0, 0, new Paint());
        } else {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
    }

    private void buildAnimations() {
        // TODO
    }

    public long getId() {
        return layerModel.getId();
    }
}
