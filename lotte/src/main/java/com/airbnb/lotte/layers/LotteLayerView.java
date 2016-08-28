package com.airbnb.lotte.layers;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
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

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class LotteLayerView extends LotteAnimatableLayer {

    /** CALayer#mask */
    protected LotteMaskLayer mask;
    protected LotteLayerView matte;

    private final Bitmap bitmap;
    private Bitmap maskBitmap;
    private Bitmap matteBitmap;
    private final Canvas contentCanvas;
    private Canvas maskCanvas;
    private Canvas matteCanvas;
    private final Paint maskShapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final LotteLayer layerModel;
    private final LotteComposition composition;

    private long parentId = -1;
    private LotteLayer rotationLayer;
    private LotteAnimationGroup animation;
    private LotteKeyframeAnimation inOutAnimation;


    public LotteLayerView(LotteLayer layerModel, LotteComposition composition) {
        super(composition.getDuration());
        this.layerModel = layerModel;
        this.composition = composition;
        setBounds(composition.getBounds());
        bitmap = Bitmap.createBitmap(composition.getBounds().width(), composition.getBounds().height(), Bitmap.Config.ARGB_8888);
        maskPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        contentCanvas = new Canvas(bitmap);
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
        while (parentId >= 0) {
            if (parentId >= 0) {
                this.parentId = parentId;
            }
            LotteLayer parentModel = composition.layerModelForId(parentId);
            LotteParentLayer parentLayer = new LotteParentLayer(parentModel, composition);
            parentLayer.addLayer(currentChild);
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
            maskBitmap = Bitmap.createBitmap(
                    composition.getBounds().width(),
                    composition.getBounds().height(),
                    Bitmap.Config.ALPHA_8);
            maskCanvas = new Canvas(maskBitmap);
        }
        buildAnimations();
    }

    public void setMask(LotteMaskLayer mask) {
        this.mask = mask;
    }

    public void setMatte(LotteLayerView matte) {
        this.matte = matte;
        matteBitmap = Bitmap.createBitmap(composition.getBounds().width(), composition.getBounds().height(), Bitmap.Config.ARGB_8888);
        matteCanvas = new Canvas(matteBitmap);
    }

    @Override
    public void draw(@NonNull Canvas mainCanvas) {
        super.draw(contentCanvas);

        if (mask != null && !mask.getMasks().isEmpty()) {
            int maskSaveCount = maskCanvas.save();
            long parentId = this.parentId;
            while (parentId >= 0) {
                LotteLayer parent = composition.layerModelForId(parentId);
                applyTransformForLayer(maskCanvas, parent);
                parentId = parent.getParentId();
            }

            applyTransformForLayer(maskCanvas, layerModel);

            for (LotteMask m : mask.getMasks()) {
                maskCanvas.drawPath(m.getMaskPath().getInitialShape(), maskShapePaint);
            }
            maskCanvas.restoreToCount(maskSaveCount);
            if (matte == null) {
                mainCanvas.drawBitmap(maskBitmap, 0, 0, maskPaint);
            }
        } else {
            if (matte == null) {
                mainCanvas.drawBitmap(bitmap, 0, 0, null);
            }
        }

        if (matte != null) {
            // a = Sa * Da
            // c = Dc
            Paint mattePaint = new Paint();
            mattePaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            matte.draw(matteCanvas);

            mainCanvas.drawBitmap(matteBitmap, 0, 0, mattePaint);
        }
    }

    private void applyTransformForLayer(Canvas canvas, LotteLayer layer) {
        if (layer.getPosition() != null) {
            canvas.translate(layer.getPosition().getInitialPoint().x, layer.getPosition().getInitialPoint().y);
        }
        if (layer.getScale().getInitialScale() != null) {
            canvas.scale(layer.getScale().getInitialScale().getScaleX(), layer.getScale().getInitialScale().getScaleY());
        }

        if (layer.getRotation().getInitialValue() != 0) {
            canvas.rotate(layer.getRotation().getInitialValue());
        }

        if (layer.getAnchor() != null) {
            canvas.translate(-layer.getAnchor().getInitialPoint().x, -layer.getAnchor().getInitialPoint().y);
        }
    }

    private void buildAnimations() {
        // TODO
    }

    public long getId() {
        return layerModel.getId();
    }
}
