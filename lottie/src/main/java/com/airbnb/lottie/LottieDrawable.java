package com.airbnb.lottie;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * This can be used to show an lottie animation in any place that would normally take a drawable.
 * If there are masks or mattes, then you MUST call {@link #recycleBitmaps()} when you are done
 * or else you will leak bitmaps.
 * <p>
 * It is preferable to use {@link com.airbnb.lottie.LottieAnimationView} when possible because it
 * handles bitmap recycling and asynchronous loading
 * of compositions.
 */
public class LottieDrawable extends Drawable implements Drawable.Callback {
  private final Matrix matrix = new Matrix();
  private LottieComposition composition;
  private final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
  private float speed = 1f;
  private float scale = 1f;
  private float progress = 0f;

  @Nullable private ImageAssetBitmapManager imageAssetBitmapManager;
  @Nullable private String imageAssetsFolder;
  @Nullable private ImageAssetDelegate imageAssetDelegate;
  private boolean playAnimationWhenCompositionAdded;
  private boolean reverseAnimationWhenCompositionAdded;
  private boolean systemAnimationsAreDisabled;
  @Nullable private CompositionLayer compositionLayer;
  private int alpha = 255;

  @SuppressWarnings("WeakerAccess") public LottieDrawable() {
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
   * Returns whether or not any layers in this composition has masks.
   */
  @SuppressWarnings({"unused", "WeakerAccess"}) public boolean hasMasks() {
    return compositionLayer != null && compositionLayer.hasMasks();
  }

  /**
   * Returns whether or not any layers in this composition has a matte layer.
   */
  @SuppressWarnings({"unused", "WeakerAccess"}) public boolean hasMatte() {
    return compositionLayer != null && compositionLayer.hasMatte();
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
   * Calling recycleBitmaps() doesn't have to be final and {@link LottieDrawable}
   * will recreate the bitmaps if needed but they will leak if you don't recycle them.
   *
   */
  @SuppressWarnings("WeakerAccess") public void recycleBitmaps() {
    if (imageAssetBitmapManager != null) {
      imageAssetBitmapManager.recycleBitmaps();
    }
  }

  /**
   * @return True if the composition is different from the previously set composition, false otherwise.
   */
  @SuppressWarnings("WeakerAccess") public boolean setComposition(LottieComposition composition) {
    if (getCallback() == null) {
      throw new IllegalStateException(
          "You or your view must set a Drawable.Callback before setting the composition. This " +
              "gets done automatically when added to an ImageView. " +
              "Either call ImageView.setImageDrawable() before setComposition() or call " +
              "setCallback(yourView.getCallback()) first.");
    }

    if (this.composition == composition) {
      return false;
    }

    clearComposition();
    this.composition = composition;
    setSpeed(speed);
    setScale(5f);
    updateBounds();
    compositionLayer = new CompositionLayer(
        this, Layer.Factory.newInstance(composition), composition.getLayers(), composition);

    setProgress(progress);
    if (playAnimationWhenCompositionAdded) {
      playAnimationWhenCompositionAdded = false;
      playAnimation();
    }
    if (reverseAnimationWhenCompositionAdded) {
      reverseAnimationWhenCompositionAdded = false;
      reverseAnimation();
    }

    return true;
  }

  private void clearComposition() {
    recycleBitmaps();
    compositionLayer = null;
    imageAssetBitmapManager = null;
    invalidateSelf();
  }

  @Override public void invalidateSelf() {
    final Callback callback = getCallback();
    if (callback != null) {
      callback.invalidateDrawable(this);
    }
  }

  @Override public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
    this.alpha = alpha;
  }

  @Override public int getAlpha() {
    return alpha;
  }

  @Override public void setColorFilter(@Nullable ColorFilter colorFilter) {
    // Do nothing.
  }

  @Override public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override public void draw(@NonNull Canvas canvas) {
    if (compositionLayer == null) {
      return;
    }
    matrix.reset();
    matrix.preScale(scale, scale);
    compositionLayer.draw(canvas, matrix, alpha);
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

  @SuppressWarnings("WeakerAccess") public void playAnimation() {
    playAnimation(false);
  }

  @SuppressWarnings("WeakerAccess") public void resumeAnimation() {
    playAnimation(true);
  }

  private void playAnimation(boolean setStartTime) {
    if (compositionLayer == null) {
      playAnimationWhenCompositionAdded = true;
      reverseAnimationWhenCompositionAdded = false;
      return;
    }
    if (setStartTime) {
      animator.setCurrentPlayTime((long) (progress * animator.getDuration()));
    }
    animator.start();
  }

  @SuppressWarnings({"unused", "WeakerAccess"}) public void resumeReverseAnimation() {
    reverseAnimation(true);
  }

  @SuppressWarnings("WeakerAccess") public void reverseAnimation() {
    reverseAnimation(false);
  }

  private void reverseAnimation(boolean setStartTime) {
    if (compositionLayer == null) {
      playAnimationWhenCompositionAdded = false;
      reverseAnimationWhenCompositionAdded = true;
      return;
    }
    if (setStartTime) {
      animator.setCurrentPlayTime((long) (progress * animator.getDuration()));
    }
    animator.reverse();
  }

  @SuppressWarnings("WeakerAccess") public void setSpeed(float speed) {
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

  public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    this.progress = progress;
    if (compositionLayer != null) {
      compositionLayer.setProgress(progress);
    }
  }

  public float getProgress() {
    return progress;
  }

  @SuppressWarnings("WeakerAccess") public void setScale(float scale) {
    this.scale = scale;
    updateBounds();
  }

  /**
   * Use this if you can't bundle images with your app. This may be useful if you download the
   * animations from the network or have the images saved to an SD Card. In that case, Lottie
   * will defer the loading of the bitmap to this delegate.
   */
  @SuppressWarnings({"unused", "WeakerAccess"}) public void setImageAssetDelegate(
      @SuppressWarnings("NullableProblems") ImageAssetDelegate assetDelegate) {
    this.imageAssetDelegate = assetDelegate;
    if (imageAssetBitmapManager != null) {
      imageAssetBitmapManager.setAssetDelegate(assetDelegate);
    }
  }

  @SuppressWarnings("WeakerAccess") public float getScale() {
    return scale;
  }

  LottieComposition getComposition() {
    return composition;
  }

  private void updateBounds() {
    if (composition == null) {
      return;
    }
    setBounds(0, 0, (int) (composition.getBounds().width() * scale),
        (int) (composition.getBounds().height() * scale));
  }

  void cancelAnimation() {
    playAnimationWhenCompositionAdded = false;
    reverseAnimationWhenCompositionAdded = false;
    animator.cancel();
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
    return composition == null ? -1 : (int) (composition.getBounds().width() * scale);
  }

  @Override public int getIntrinsicHeight() {
    return composition == null ? -1 : (int) (composition.getBounds().height() * scale);
  }

  Bitmap getImageAsset(String id) {
    return getImageAssetBitmapManager().bitmapForId(id);
  }

  private ImageAssetBitmapManager getImageAssetBitmapManager() {
    if (imageAssetBitmapManager != null && !imageAssetBitmapManager.hasSameContext(getContext())) {
      imageAssetBitmapManager.recycleBitmaps();
      imageAssetBitmapManager = null;
    }

    if (imageAssetBitmapManager == null) {
      imageAssetBitmapManager = new ImageAssetBitmapManager(getCallback(),
          imageAssetsFolder, imageAssetDelegate, composition.getImages());
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
  @Override public void invalidateDrawable(@NonNull Drawable who) {
    Callback callback = getCallback();
    if (callback == null) {
      return;
    }
    callback.invalidateDrawable(this);
  }

  @Override public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
    Callback callback = getCallback();
    if (callback == null) {
      return;
    }
    callback.scheduleDrawable(this, what, when);
  }

  @Override public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
    Callback callback = getCallback();
    if (callback == null) {
      return;
    }
    callback.unscheduleDrawable(this, what);
  }
}
