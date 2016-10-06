package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import com.airbnb.lotte.animation.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;
import com.airbnb.lotte.utils.Observable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class LotteAnimatableLayer extends Drawable {
    private static final String TAG = LotteAnimatableLayer.class.getSimpleName();

    protected final List<LotteAnimatableLayer> layers = new ArrayList<>();

    protected final Observable.OnChangedListener changedListener = new Observable.OnChangedListener() {
        @Override
        public void onChanged() {
            invalidateSelf();
        }
    };

    /** This should mimic CALayer#position */
    private Observable<PointF> position;
    /** This should mimic CALayer#anchorPoint */
    private Observable<PointF> anchorPoint;
    /** This should mimic CALayer#transform */
    private Observable<LotteTransform3D> transform;
    /** This should mimic CALayer#sublayerTransform */
    private Observable<Number> alpha;
    private Observable<Float> sublayerTransform;
    protected long compDuration;

    private final Paint solidBackgroundPaint = new Paint();
    protected final List<LotteAnimationGroup> animations = new ArrayList<>();

    public LotteAnimatableLayer(long compDuration, Drawable.Callback callback) {
        setCallback(callback);
        this.compDuration = compDuration;

        solidBackgroundPaint.setAlpha(0);
        solidBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    public void setBackgroundColor(@ColorInt int color) {
        solidBackgroundPaint.setColor(color);
        solidBackgroundPaint.setAlpha(Color.alpha(color));
        invalidateSelf();
    }

    public void addAnimation(LotteAnimationGroup animation) {
        animations.add(animation);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.save();
        if (position != null && position.getValue() != null) {
            if (position.getValue().x != 0 || position.getValue().y != 0) {
                canvas.translate(position.getValue().x, position.getValue().y);
            }
        }
        if (transform != null && transform.getValue() != null) {
            if (transform.getValue().getScaleX() != 1f || transform.getValue().getScaleY() != 1f) {
                canvas.scale(transform.getValue().getScaleX(), transform.getValue().getScaleY());
            }
        }

        if (sublayerTransform != null && sublayerTransform.getValue() != null) {
            float rotation = sublayerTransform.getValue();
            if (rotation != 0f) {
                canvas.rotate(rotation);
            }
        }

        if (anchorPoint != null && anchorPoint.getValue() != null) {
            if (anchorPoint.getValue().x != 0 || anchorPoint.getValue().y != 0) {
                canvas.translate(-anchorPoint.getValue().x, -anchorPoint.getValue().y);
            }
        }

        if (solidBackgroundPaint.getAlpha() != 0) {
            canvas.drawRect(getBounds(), solidBackgroundPaint);
        }
        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).draw(canvas);
        }
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        throw new IllegalArgumentException("This shouldn't be used.");
    }

    public void setAlpha(Observable<Number> alpha) {
        this.alpha = alpha;
        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return (int) alpha.getValue();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    public void setAnchorPoint(Observable<PointF> anchorPoint) {
        if (this.anchorPoint != null) {
            this.anchorPoint.removeChangeListener(changedListener);
        }
        this.anchorPoint = anchorPoint;
        anchorPoint.addChangeListener(changedListener);
    }

    public void setPosition(Observable<PointF> position) {
        if (this.position != null) {
            this.position.removeChangeListener(changedListener);
        }
        this.position = position;
        position.addChangeListener(changedListener);
    }

    public void setTransform(Observable<LotteTransform3D> transform) {
        if (this.transform != null) {
            this.transform.removeChangeListener(changedListener);
        }
        this.transform = transform;
        transform.addChangeListener(changedListener);
    }

    public void setSublayerTransform(Observable<Float> sublayerTransform) {
        if (this.sublayerTransform != null) {
            this.sublayerTransform.removeChangeListener(changedListener);
        }
        this.sublayerTransform = sublayerTransform;
        sublayerTransform.addChangeListener(changedListener);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void addLayer(LotteAnimatableLayer layer) {
        layers.add(layer);
        int width = Math.max(getBounds().width(), layer.getBounds().width());
        int height = Math.max(getBounds().height(), layer.getBounds().height());
        setBounds(0, 0, width, height);
        invalidateSelf();
    }

    public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        for (int i = 0; i < animations.size(); i++) {
            animations.get(i).setProgress(progress);
        }

        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).setProgress(progress);
        }
    }
}
