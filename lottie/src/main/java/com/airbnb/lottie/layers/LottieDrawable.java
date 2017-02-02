package com.airbnb.lottie.layers;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.util.LongSparseArray;
import android.view.animation.LinearInterpolator;

import com.airbnb.lottie.model.Layer;
import com.airbnb.lottie.model.LottieComposition;

import java.util.ArrayList;
import java.util.List;

/**
 * This can be used to show an lottie animation in any place that would normally take a drawable.
 * If there are masks or mattes, then you MUST call {@link #recycleBitmaps()} when you are done or else you will leak bitmaps.
 *
 * It is preferable to use {@link com.airbnb.lottie.LottieAnimationView} when possible because it handles bitmap recycling and asynchronous loading
 * of compositions.
 */
@RestrictTo(RestrictTo.Scope.GROUP_ID)
public class LottieDrawable extends AnimatableLayer {

    private LottieComposition composition;

    private final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);

    @Nullable private Bitmap mainBitmap = null;
    @Nullable private Bitmap maskBitmap = null;
    @Nullable private Bitmap matteBitmap = null;
    @Nullable private Bitmap mainBitmapForMatte = null;
    @Nullable private Bitmap maskBitmapForMatte = null;
    private boolean playAnimationWhenLayerAdded;

    public LottieDrawable() {
        super(null);

        animator.setRepeatCount(0);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setProgress(animation.getAnimatedFraction());
            }
        });
    }

    public void setComposition(LottieComposition composition) {
        if (getCallback() == null) {
            throw new IllegalStateException("You or your view must set a Drawable.Callback before setting the composition. This gets done automatically when added to an ImageView. " +
                    "Either call ImageView.setImageDrawable() before setComposition() or call setCallback(yourView.getCallback()) first.");
        }
        clearComposition();
        this.composition = composition;
        animator.setDuration(composition.getDuration());
        setBounds(0, 0, composition.getBounds().width(), composition.getBounds().height());
        buildLayersForComposition(composition);

        getCallback().invalidateDrawable(this);
    }

    private void clearComposition() {
        recycleBitmaps();
        clearLayers();
    }

    private void buildLayersForComposition(LottieComposition composition) {
        if (composition == null) {
            throw new IllegalStateException("Composition is null");
        }
        Rect bounds = composition.getBounds();
        if (composition.hasMasks() || composition.hasMattes()) {
            mainBitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        }
        if (composition.hasMasks()) {
            maskBitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ALPHA_8);
        }
        if (composition.hasMattes()) {
            matteBitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        }
        LongSparseArray<LayerView> layerMap = new LongSparseArray<>(composition.getLayers().size());
        List<LayerView> layers = new ArrayList<>(composition.getLayers().size());
        LayerView maskedLayer = null;
        for (int i = composition.getLayers().size() - 1; i >= 0; i--) {
            Layer layer = composition.getLayers().get(i);
            LayerView layerView;
            if (maskedLayer == null) {
                layerView = new LayerView(layer, composition, getCallback(), mainBitmap, maskBitmap, matteBitmap);
            } else {
                if (mainBitmapForMatte == null) {
                    mainBitmapForMatte = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ALPHA_8);
                }
                if (maskBitmapForMatte == null && !layer.getMasks().isEmpty()) {
                    maskBitmapForMatte = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ALPHA_8);
                }

                layerView = new LayerView(layer, composition, getCallback(), mainBitmapForMatte, maskBitmapForMatte, null);
            }
            layerMap.put(layerView.getId(), layerView);
            if (maskedLayer != null) {
                maskedLayer.setMatteLayer(layerView);
                maskedLayer = null;
            } else {
                layers.add(layerView);
                if (layer.getMatteType() == Layer.MatteType.Add) {
                    maskedLayer = layerView;
                }
            }
        }

        for (int i = 0; i < layers.size(); i++) {
            LayerView layerView = layers.get(i);
            addLayer(layerView);
        }

        for (int i = 0; i < layerMap.size(); i++) {
            long key = layerMap.keyAt(i);
            LayerView layerView = layerMap.get(key);
            LayerView parentLayer = layerMap.get(layerView.getLayerModel().getParentId());
            if (parentLayer != null) {
                layerView.setParentLayer(parentLayer);
            }
        }
    }

    @Override
    public void invalidateSelf() {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (composition == null) {
            return;
        }
        Rect bounds = getBounds();
        Rect compBounds = composition.getBounds();
        int saveCount = canvas.save();
        if (!bounds.equals(compBounds)) {
            float scaleX = bounds.width() / (float) compBounds.width();
            float scaleY = bounds.height() / (float) compBounds.height();
            canvas.scale(scaleX, scaleY);
        }
        super.draw(canvas);
        canvas.clipRect(getBounds());
        canvas.restoreToCount(saveCount);

    }

    public void loop(boolean loop) {
        animator.setRepeatCount(loop ? ValueAnimator.INFINITE : 0);
    }

    public boolean isLooping() {
        return animator.getRepeatCount() == ValueAnimator.INFINITE;
    }

    public boolean isAnimating() {
        return animator.isRunning();
    }

    public void playAnimation() {
        if (layers.isEmpty()) {
            playAnimationWhenLayerAdded = true;
            return;
        }
        animator.setCurrentPlayTime((long) (getProgress() * animator.getDuration()));
        animator.start();
    }

    public void cancelAnimation() {
        playAnimationWhenLayerAdded = false;
        animator.cancel();
    }

    @Override
    public void addLayer(AnimatableLayer layer) {
        super.addLayer(layer);
        if (playAnimationWhenLayerAdded) {
            playAnimationWhenLayerAdded = false;
            playAnimation();
        }
    }

    public void addAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
        animator.addUpdateListener(updateListener);
    }

    public void removeAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
        animator.removeUpdateListener(updateListener);
    }

    public void addAnimatorListener(Animator.AnimatorListener listener) {
        animator.addListener(listener);
    }

    public void removeAnimatorListener(Animator.AnimatorListener listener) {
        animator.removeListener(listener);
    }

    @Override
    public int getIntrinsicWidth() {
        return composition == null ? -1 : composition.getBounds().width();
    }

    @Override
    public int getIntrinsicHeight() {
        return composition == null ? -1 : composition.getBounds().height();
    }

    @VisibleForTesting
    public void recycleBitmaps() {
        if (mainBitmap != null) {
            mainBitmap.recycle();
            mainBitmap = null;
        }
        if (maskBitmap != null) {
            maskBitmap.recycle();
            maskBitmap = null;
        }
        if (matteBitmap != null) {
            matteBitmap.recycle();
            matteBitmap = null;
        }
        if (mainBitmapForMatte != null) {
            mainBitmapForMatte.recycle();
            mainBitmapForMatte = null;
        }
        if (maskBitmapForMatte != null) {
            maskBitmapForMatte.recycle();
            maskBitmapForMatte = null;
        }
    }
}
