package com.airbnb.lottie;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * This can be used to show an lottie animation in any place that would normally take a drawable.
 * If there are masks or mattes, then you MUST call {@link #recycleBitmaps()} when you are done
 * or else you will leak bitmaps.
 * <p>
 * It is preferable to use {@link com.airbnb.lottie.LottieAnimationView} when possible because it
 * handles bitmap recycling and asynchronous loading
 * of compositions.
 */
public class LottieDrawable extends AnimatableLayer implements Drawable.Callback {
  private LottieComposition composition;
  private final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
  private float speed = 1f;

  private final CanvasPool canvasPool = new CanvasPool();
  @Nullable private ImageAssetBitmapManager imageAssetBitmapManager;
  @Nullable private String imageAssetsFolder;
  private boolean playAnimationWhenLayerAdded;
  private boolean reverseAnimationWhenLayerAdded;
  private boolean systemAnimationsAreDisabled;

  @SuppressWarnings("WeakerAccess") public LottieDrawable() {
    super(null);

    animator.setRepeatCount(0);
    animator.setInterpolator(new LinearInterpolator());
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        if (systemAnimationsAreDisabled) {
          animator.cancel();
          setProgress(1f);
        } else {
          setProgress((float) animation.getAnimatedValue());
        }
      }
    });
  }

  /**
   * If you use image assets, you must explicitly specify the folder in assets/ in which they are
   * located because bodymovin uses the name filenames across all compositions (img_#).
   * Do NOT rename the images themselves.
   *
   * If your images are located in src/main/assets/airbnb_loader/ then call
   * `setImageAssetsFolder("airbnb_loader/");`.
   *
   *
   * If you use LottieDrawable directly, you MUST call {@link #recycleBitmaps()} when you
   * are done. Calling {@link #recycleBitmaps()} doesn't have to be final and {@link LottieDrawable}
   * will recreate the bitmaps if needed but they will leak if you don't recycle them.
   */
  @SuppressWarnings("WeakerAccess") public void setImagesAssetsFolder(@Nullable String imageAssetsFolder) {
    this.imageAssetsFolder = imageAssetsFolder;
  }

  /**
   * If you have image assets and use {@link LottieDrawable} directly, you must call this yourself.
   *
   * Calling {@link #recycleBitmaps()} doesn't have to be final and {@link LottieDrawable}
   * will recreate the bitmaps if needed but they will leak if you don't recycle them.
   *
   */
  @SuppressWarnings("WeakerAccess") public void recycleBitmaps() {
    canvasPool.recycleBitmaps();
    if (imageAssetBitmapManager != null) {
      imageAssetBitmapManager.recycleBitmaps();
    }
  }

  void setComposition(LottieComposition composition) {
    if (getCallback() == null) {
      throw new IllegalStateException(
          "You or your view must set a Drawable.Callback before setting the composition. This " +
              "gets done automatically when added to an ImageView. " +
              "Either call ImageView.setImageDrawable() before setComposition() or call " +
              "setCallback(yourView.getCallback()) first.");
    }
    clearComposition();
    this.composition = composition;
    setSpeed(speed);
    setBounds(0, 0, composition.getBounds().width(), composition.getBounds().height());
    buildLayersForComposition(composition);

    setProgress(getProgress());
  }

  private void clearComposition() {
    recycleBitmaps();
    clearLayers();
    imageAssetBitmapManager = null;
  }

  private void buildLayersForComposition(LottieComposition composition) {
    if (composition == null) {
      throw new IllegalStateException("Composition is null");
    }
    LongSparseArray<LayerView> layerMap = new LongSparseArray<>(composition.getLayers().size());
    List<LayerView> layers = new ArrayList<>(composition.getLayers().size());
    LayerView mattedLayer = null;
    for (int i = composition.getLayers().size() - 1; i >= 0; i--) {
      Layer layer = composition.getLayers().get(i);
      LayerView layerView;
      layerView = new LayerView(layer, composition, this, canvasPool);
      layerMap.put(layerView.getId(), layerView);
      if (mattedLayer != null) {
        mattedLayer.setMatteLayer(layerView);
        mattedLayer = null;
      } else {
        layers.add(layerView);
        if (layer.getMatteType() == Layer.MatteType.Add) {
          mattedLayer = layerView;
        } else if (layer.getMatteType() == Layer.MatteType.Invert) {
          mattedLayer = layerView;
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

  @Override public void invalidateSelf() {
    final Callback callback = getCallback();
    if (callback != null) {
      callback.invalidateDrawable(this);
    }
  }

  @Override public void draw(@NonNull Canvas canvas) {
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
    // TODO: delete this?
    canvas.clipRect(getBounds());
    canvas.restoreToCount(saveCount);

  }

  void systemAnimationsAreDisabled() {
    systemAnimationsAreDisabled = true;
  }

  void loop(boolean loop) {
    animator.setRepeatCount(loop ? ValueAnimator.INFINITE : 0);
  }

  boolean isLooping() {
    return animator.getRepeatCount() == ValueAnimator.INFINITE;
  }

  boolean isAnimating() {
    return animator.isRunning();
  }

  void playAnimation() {
    if (layers.isEmpty()) {
      playAnimationWhenLayerAdded = true;
      reverseAnimationWhenLayerAdded = false;
      return;
    }
    animator.setCurrentPlayTime((long) (getProgress() * animator.getDuration()));
    animator.start();
  }

  void reverseAnimation() {
    if (layers.isEmpty()) {
      playAnimationWhenLayerAdded = false;
      reverseAnimationWhenLayerAdded = true;
      return;
    }
    animator.setCurrentPlayTime((long) (getProgress() * animator.getDuration()));
    animator.reverse();
  }

  void setSpeed(float speed) {
    this.speed = speed;
    if (speed < 0) {
      animator.setFloatValues(1f, 0f);
    } else {
      animator.setFloatValues(0f, 1f);
    }

    if (composition != null) {
      animator.setDuration((long) (composition.getDuration() / Math.abs(speed)));
    }
  }

  void cancelAnimation() {
    playAnimationWhenLayerAdded = false;
    reverseAnimationWhenLayerAdded = false;
    animator.cancel();
  }

  @Override
  void addLayer(AnimatableLayer layer) {
    super.addLayer(layer);
    if (playAnimationWhenLayerAdded) {
      playAnimationWhenLayerAdded = false;
      playAnimation();
    }
    if (reverseAnimationWhenLayerAdded) {
      reverseAnimationWhenLayerAdded = false;
      reverseAnimation();
    }
  }

  void addAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
    animator.addUpdateListener(updateListener);
  }

  void removeAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
    animator.removeUpdateListener(updateListener);
  }

  void addAnimatorListener(Animator.AnimatorListener listener) {
    animator.addListener(listener);
  }

  void removeAnimatorListener(Animator.AnimatorListener listener) {
    animator.removeListener(listener);
  }

  @Override public int getIntrinsicWidth() {
    return composition == null ? -1 : composition.getBounds().width();
  }

  @Override public int getIntrinsicHeight() {
    return composition == null ? -1 : composition.getBounds().height();
  }

  Bitmap getImageBitmap(String id) {
    return getImageAssetBitmapManager().bitmapForId(id);
  }

  private ImageAssetBitmapManager getImageAssetBitmapManager() {
    if (imageAssetBitmapManager != null && !imageAssetBitmapManager.hasSameContext(getContext())) {
      imageAssetBitmapManager.recycleBitmaps();
      imageAssetBitmapManager = null;
    }

    if (imageAssetBitmapManager == null) {
      imageAssetBitmapManager = new ImageAssetBitmapManager(getCallback(),
          imageAssetsFolder, composition.getImages());
    }

    return imageAssetBitmapManager;
  }

  private @Nullable Context getContext() {
    Callback callback = getCallback();
    if (callback == null) {
      return null;
    }

    if (callback instanceof View) {
      return ((View) callback).getContext();
    }
    return null;
  }

  /**
   * These Drawable.Callback methods proxy the calls so that this is the drawable that is
   * actually invalidated, not a child one which will not pass the view's validateDrawable check.
   */
  @Override public void invalidateDrawable(Drawable who) {
    Callback callback = getCallback();
    if (callback == null) {
      return;
    }
    callback.invalidateDrawable(this);
  }

  @Override public void scheduleDrawable(Drawable who, Runnable what, long when) {
    Callback callback = getCallback();
    if (callback == null) {
      return;
    }
    callback.scheduleDrawable(this, what, when);
  }

  @Override public void unscheduleDrawable(Drawable who, Runnable what) {
    Callback callback = getCallback();
    if (callback == null) {
      return;
    }
    callback.unscheduleDrawable(this, what);
  }
}
