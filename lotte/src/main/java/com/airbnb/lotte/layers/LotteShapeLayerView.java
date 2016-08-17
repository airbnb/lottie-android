package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.airbnb.lotte.model.LotteShapeFill;
import com.airbnb.lotte.model.LotteShapePath;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.model.LotteShapeTransform;
import com.airbnb.lotte.model.LotteShapeTrimPath;
import com.airbnb.lotte.utils.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;

public class LotteShapeLayerView extends LotteAnimatableLayer {

    private final Paint fillPaint = new Paint();
    private final Paint strokePaint = new Paint();

    private final LotteShapePath path;
    private final LotteShapeFill fill;
    private final LotteShapeStroke stroke;
    private final LotteShapeTrimPath trim;
    private final LotteShapeTransform transformModel;

    private LotteShapeLayer fillLayer;
    private LotteShapeLayer strokeLayer;

    private LotteAnimationGroup animation;
    private LotteAnimationGroup strokeAnimation;
    private LotteAnimationGroup fillAnimation;

    public LotteShapeLayerView(LotteShapePath shape, @Nullable LotteShapeFill fill,
            @Nullable LotteShapeStroke stroke, @Nullable LotteShapeTrimPath trim,
            LotteShapeTransform transformModel, long duration) {
        super(duration);
        path = shape;
        this.fill = fill;
        this.stroke = stroke;
        this.trim = trim;
        this.transformModel = transformModel;

        fillPaint.setAlpha(0);
        fillPaint.setAntiAlias(true);
        strokePaint.setAntiAlias(true);

        setBounds(transformModel.getCompBounds());
        anchorPoint = transformModel.getAnchor().getInitialPoint();
        setAlpha((int) (transformModel.getOpacity().getInitialValue() * 255));
        position = transformModel.getPosition().getInitialPoint();
        this.transform = transformModel.getScale().getInitialScale();
        sublayerTransform = new LotteTransform3D();
        sublayerTransform.rotateZ((float) Math.toDegrees(transformModel.getRotation().getInitialValue()));

        if (fill != null) {
            fillLayer = new LotteShapeLayer();
            fillLayer.setPath(path.getShapePath().getInitialShape());
            fillLayer.setFillColor(fill.getColor().getInitialColor());
            fillLayer.setAlpha((int) (fill.getOpacity().getInitialValue() * 255));
            addLayer(fillLayer);
        }

        if (stroke != null) {
            // TODO
        }

        if (trim != null) {
            // TODO
        }

    }

    public LotteShapeFill getFill() {
        return fill;
    }

    public LotteShapePath getShape() {
        return path;
    }

    public LotteShapeStroke getStroke() {
        return stroke;
    }

    public LotteShapeTransform getTransform() {
        return transformModel;
    }

    public LotteShapeTrimPath getTrim() {
        return trim;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        if (fillPaint.getAlpha() != 0) {
            canvas.drawPath(path.getShapePath().getInitialShape(), fillPaint);
        }
    }
}
