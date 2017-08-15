package com.airbnb.lottie;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This can be used to show an lottie animation in any place that would normally take a drawable.
 * If there are masks or mattes, then you MUST call {@link #recycleBitmaps()} when you are done
 * or else you will leak bitmaps.
 * <p>
 * It is preferable to use {@link com.airbnb.lottie.LottieAnimationView} when possible because it
 * handles bitmap recycling and asynchronous loading
 * of compositions.
 */
@SuppressWarnings({"WeakerAccess", "unused"}) public class LottieDrawable extends Drawable implements Drawable.Callback {
  private static final String TAG = LottieDrawable.class.getSimpleName();

  private interface LazyCompositionTask {
    void run(LottieComposition composition);
  }

  private final Matrix matrix = new Matrix();
  private LottieComposition composition;
  private final LottieValueAnimator animator = new LottieValueAnimator();
  private float speed = 1f;
  private float progress = 0f;
  private float scale = 1f;

  private final Set<ColorFilterData> colorFilterData = new HashSet<>();
  private final ArrayList<LazyCompositionTask> lazyCompositionTasks = new ArrayList<>();
  @Nullable private ImageAssetManager imageAssetManager;
  @Nullable private String imageAssetsFolder;
  @Nullable private ImageAssetDelegate imageAssetDelegate;
  @Nullable private FontAssetManager fontAssetManager;
  @Nullable FontAssetDelegate fontAssetDelegate;
  @Nullable TextDelegate textDelegate;
  private boolean systemAnimationsAreDisabled;
  private boolean enableMergePaths;
  @Nullable private CompositionLayer compositionLayer;
  private int alpha = 255;
  private boolean performanceTrackingEnabled;

  public LottieDrawable() {
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
  public boolean hasMasks() {
    return compositionLayer != null && compositionLayer.hasMasks();
  }

  /**
   * Returns whether or not any layers in this composition has a matte layer.
   */
  public boolean hasMatte() {
    return compositionLayer != null && compositionLayer.hasMatte();
  }

  boolean enableMergePathsForKitKatAndAbove() {
    return enableMergePaths;
  }

  /**
   * Enable this to get merge path support for devices running KitKat (19) and above.
   *
   * Merge paths currently don't work if the the operand shape is entirely contained within the
   * first shape. If you need to cut out one shape from another shape, use an even-odd fill type
   * instead of using merge paths.
   */
  public void enableMergePathsForKitKatAndAbove(boolean enable) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      Log.w(TAG, "Merge paths are not supported pre-Kit Kat.");
      return;
    }
    enableMergePaths = enable;
    if (composition != null) {
      buildCompositionLayer();
    }
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
  public void setImagesAssetsFolder(@Nullable String imageAssetsFolder) {
    this.imageAssetsFolder = imageAssetsFolder;
  }

  @Nullable public String getImageAssetsFolder() {
    return imageAssetsFolder;
  }

  /**
   * If you have image assets and use {@link LottieDrawable} directly, you must call this yourself.
   *
   * Calling recycleBitmaps() doesn't have to be final and {@link LottieDrawable}
   * will recreate the bitmaps if needed but they will leak if you don't recycle them.
   *
   */
  public void recycleBitmaps() {
    if (imageAssetManager != null) {
      imageAssetManager.recycleBitmaps();
    }
  }

  /**
   * @return True if the composition is different from the previously set composition, false otherwise.
   */
  public boolean setComposition(LottieComposition composition) {
    if (this.composition == composition) {
      return false;
    }

    clearComposition();
    this.composition = composition;
    setSpeed(speed);
    updateBounds();
    buildCompositionLayer();
    applyColorFilters();

    setProgress(progress);
    Iterator<LazyCompositionTask> it = lazyCompositionTasks.iterator();
    while (it.hasNext()) {
      LazyCompositionTask t = it.next();
      t.run(composition);
      it.remove();
    }
    composition.setPerformanceTrackingEnabled(performanceTrackingEnabled);

    return true;
  }

  public void setPerformanceTrackingEnabled(boolean enabled) {
    performanceTrackingEnabled = enabled;
    if (composition != null) {
      composition.setPerformanceTrackingEnabled(enabled);
    }
  }

  @Nullable
  public PerformanceTracker getPerformanceTracker() {
    if (composition != null) {
      return composition.getPerformanceTracker();
    }
    return null;
  }

  private void buildCompositionLayer() {
    compositionLayer = new CompositionLayer(
        this, Layer.Factory.newInstance(composition), composition.getLayers(), composition);
  }

  private void applyColorFilters() {
    if (compositionLayer == null) {
      return;
    }

    for (ColorFilterData data : colorFilterData) {
      compositionLayer.addColorFilter(data.layerName, data.contentName, data.colorFilter);
    }
  }

  private void clearComposition() {
    recycleBitmaps();
    compositionLayer = null;
    imageAssetManager = null;
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

  /**
   * Add a color filter to specific content on a specific layer.
   * @param layerName name of the layer where the supplied content name lives
   * @param contentName name of the specific content that the color filter is to be applied
   * @param colorFilter the color filter, null to clear the color filter
   */
  public void addColorFilterToContent(String layerName, String contentName,
      @Nullable ColorFilter colorFilter) {
    addColorFilterInternal(layerName, contentName, colorFilter);
  }

  /**
   * Add a color filter to a whole layer
   * @param layerName name of the layer that the color filter is to be applied
   * @param colorFilter the color filter, null to clear the color filter
   */
  public void addColorFilterToLayer(String layerName, @Nullable ColorFilter colorFilter) {
    addColorFilterInternal(layerName, null, colorFilter);
  }

  /**
   * Add a color filter to all layers
   * @param colorFilter the color filter, null to clear all color filters
   */
  public void addColorFilter(ColorFilter colorFilter) {
    addColorFilterInternal(null, null, colorFilter);
  }

  /**
   * Clear all color filters on all layers and all content in the layers
   */
  public void clearColorFilters() {
    colorFilterData.clear();
    addColorFilterInternal(null, null, null);
  }

  /**
   * Private method to capture all color filter additions.
   * There are 3 different behaviors here.
   * 1. layerName is null. All layers supporting color filters will apply the passed in color filter
   * 2. layerName is not null, contentName is null. This will apply the passed in color filter
   *    to the whole layer
   * 3. layerName is not null, contentName is not null. This will apply the pass in color filter
   *    to a specific composition content.
   */
  private void addColorFilterInternal(@Nullable String layerName, @Nullable String contentName,
      @Nullable ColorFilter colorFilter) {
    final ColorFilterData data = new ColorFilterData(layerName, contentName, colorFilter);
    if (colorFilter == null && colorFilterData.contains(data)) {
      colorFilterData.remove(data);
    } else {
      colorFilterData.add(new ColorFilterData(layerName, contentName, colorFilter));
    }

    if (compositionLayer == null) {
      return;
    }

    compositionLayer.addColorFilter(layerName, contentName, colorFilter);
  }

  @Override public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override public void draw(@NonNull Canvas canvas) {
    L.beginSection("Drawable#draw");
    if (compositionLayer == null) {
      return;
    }
    float scale = this.scale;
    float extraScale = 1f;
    boolean hasExtraScale = false;
    float maxScale = getMaxScale(canvas);
    if (compositionLayer.hasMatte() || compositionLayer.hasMasks()) {
      // Since we can only scale up the animation so much before masks and mattes get clipped, we
      // may have to scale the canvas to fake the rest. This isn't a problem for software rendering
      // but hardware accelerated scaling is rasterized so it will appear pixelated.
      extraScale = scale / maxScale;
      scale = Math.min(scale, maxScale);
      // This check fixes some floating point rounding issues.
      hasExtraScale = extraScale > 1.001f;
    }

    if (hasExtraScale) {
      canvas.save();
      // This is extraScale ^2 because what happens is when the scale increases, the intrinsic size
      // of the view increases. That causes the drawable to keep growing even though we are only
      // rendering to the size of the view in the top left quarter, leaving the rest blank.
      // The first scale by extraScale scales up the canvas so that we are back at the original
      // size. The second extraScale is what actually has the scaling effect.
      float extraScaleSquared = extraScale * extraScale;
      int px = (int) ((composition.getBounds().width() * scale / 2f));
      int py = (int) ((composition.getBounds().height() * scale / 2f));
      canvas.scale(extraScaleSquared, extraScaleSquared, px, py);

    }
    matrix.reset();
    matrix.preScale(scale, scale);
    compositionLayer.draw(canvas, matrix, alpha);
    if (hasExtraScale) {
      canvas.restore();
    }
    L.endSection("Drawable#draw");
  }

  void systemAnimationsAreDisabled() {
    systemAnimationsAreDisabled = true;
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
    playAnimation((progress > 0.0 && progress < 1.0));
  }

  public void resumeAnimation() {
    playAnimation(true);
  }

  private void playAnimation(boolean setStartTime) {
    if (compositionLayer == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override public void run(LottieComposition composition) {
          playAnimation();
        }
      });
      return;
    }
    long playTime = setStartTime ? (long) (progress * animator.getDuration()) : 0;
    animator.start();
    if (setStartTime) {
      animator.setCurrentPlayTime(playTime);
    }
  }

  public void playAnimation(final int startFrame, final int endFrame) {
    if (composition == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override public void run(LottieComposition composition) {
          playAnimation(startFrame / composition.getDurationFrames(),
              endFrame / composition.getDurationFrames());
        }
      });
      return;
    }
    playAnimation(startFrame / composition.getDurationFrames(),
        endFrame / composition.getDurationFrames());
  }

  public void playAnimation(@FloatRange(from = 0f, to = 1f) float startProgress,
      @FloatRange(from = 0f, to = 1f) float endProgress) {
    animator.updateValues(startProgress, endProgress);
    animator.setCurrentPlayTime(0);
    setProgress(startProgress);
    playAnimation(false);
  }

  public void resumeReverseAnimation() {
    reverseAnimation(true);
  }

  public void reverseAnimation() {
    reverseAnimation((progress > 0.0 && progress < 1.0));
  }

  private void reverseAnimation(boolean setStartTime) {
    if (compositionLayer == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override public void run(LottieComposition composition) {
          reverseAnimation();
        }
      });
      return;
    }
    if (setStartTime) {
      animator.setCurrentPlayTime((long) (progress * animator.getDuration()));
    }
    animator.reverse();
  }

  public void setMinFrame(final int startFrame) {
    if (composition == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override public void run(LottieComposition composition) {
          setMinProgress(startFrame / composition.getDurationFrames());
        }
      });
      return;
    }
    setMinProgress(startFrame / composition.getDurationFrames());
  }

   public void setMinProgress(float startProgress) {
    animator.setStartProgress(startProgress);
  }

  public void setMaxFrame(final int endFrame) {
    if (composition == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override public void run(LottieComposition composition) {
          setMaxProgress(endFrame / composition.getDurationFrames());
        }
      });
      return;
    }
    setMaxProgress(endFrame / composition.getDurationFrames());
  }

  public void setMaxProgress(float endProgress) {
    animator.setEndProgress(endProgress);
  }

  public void setMinAndMaxFrame(int minFrame, int maxFrame) {
    setMinFrame(minFrame);
    setMaxFrame(maxFrame);
  }

  public void setMinAndMaxProgress(float minProgress, float maxProgress) {
    setMinProgress(minProgress);
    setMaxProgress(maxProgress);
  }

  public void setSpeed(float speed) {
    this.speed = speed;
    animator.setIsReversed(speed < 0);

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

  /**
   * Set the scale on the current composition. The only cost of this function is re-rendering the
   * current frame so you may call it frequent to scale something up or down.
   *
   * The smaller the animation is, the better the performance will be. You may find that scaling an
   * animation down then rendering it in a larger ImageView and letting ImageView scale it back up
   * with a scaleType such as centerInside will yield better performance with little perceivable
   * quality loss.
   */
  public void setScale(float scale) {
    this.scale = scale;
    updateBounds();
  }

  /**
   * Use this if you can't bundle images with your app. This may be useful if you download the
   * animations from the network or have the images saved to an SD Card. In that case, Lottie
   * will defer the loading of the bitmap to this delegate.
   */
  public void setImageAssetDelegate(
      @SuppressWarnings("NullableProblems") ImageAssetDelegate assetDelegate) {
    this.imageAssetDelegate = assetDelegate;
    if (imageAssetManager != null) {
      imageAssetManager.setDelegate(assetDelegate);
    }
  }

  /**
   * Use this to manually set fonts.
   */
  public void setFontAssetDelegate(
      @SuppressWarnings("NullableProblems") FontAssetDelegate assetDelegate) {
    this.fontAssetDelegate = assetDelegate;
    if (fontAssetManager != null) {
      fontAssetManager.setDelegate(assetDelegate);
    }
  }

  public void setTextDelegate(@SuppressWarnings("NullableProblems") TextDelegate textDelegate) {
    this.textDelegate = textDelegate;
  }

  @Nullable TextDelegate getTextDelegate() {
    return textDelegate;
  }

  boolean useTextGlyphs() {
    return textDelegate == null && composition.getCharacters().size() > 0;
  }

  public float getScale() {
    return scale;
  }

  public LottieComposition getComposition() {
    return composition;
  }

  private void updateBounds() {
    if (composition == null) {
      return;
    }
    float scale = getScale();
    setBounds(0, 0, (int) (composition.getBounds().width() * scale),
        (int) (composition.getBounds().height() * scale));
  }

  public void cancelAnimation() {
    lazyCompositionTasks.clear();
    animator.cancel();
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

  @Override public int getIntrinsicWidth() {
    return composition == null ? -1 : (int) (composition.getBounds().width() * scale);
  }

  @Override public int getIntrinsicHeight() {
    return composition == null ? -1 : (int) (composition.getBounds().height() * scale);
  }

  /**
   * Allows you to modify or clear a bitmap that was loaded for an image either automatically
   * through {@link #setImagesAssetsFolder(String)} or with an {@link ImageAssetDelegate}.
   *
   * @return the previous Bitmap or null.
   */
  @Nullable
  public Bitmap updateBitmap(String id, @Nullable Bitmap bitmap) {
    ImageAssetManager bm = getImageAssetManager();
    if (bm == null) {
      Log.w(L.TAG, "Cannot update bitmap. Most likely the drawable is not added to a View " +
        "which prevents Lottie from getting a Context.");
      return null;
    }
    Bitmap ret = bm.updateBitmap(id, bitmap);
    invalidateSelf();
    return ret;
  }

  @Nullable
  Bitmap getImageAsset(String id) {
    ImageAssetManager bm = getImageAssetManager();
    if (bm != null) {
      return bm.bitmapForId(id);
    }
    return null;
  }

  private ImageAssetManager getImageAssetManager() {
    if (getCallback() == null) {
      // We can't get a bitmap since we can't get a Context from the callback.
      return null;
    }

    if (imageAssetManager != null && !imageAssetManager.hasSameContext(getContext())) {
      imageAssetManager.recycleBitmaps();
      imageAssetManager = null;
    }

    if (imageAssetManager == null) {
      imageAssetManager = new ImageAssetManager(getCallback(),
          imageAssetsFolder, imageAssetDelegate, composition.getImages());
    }

    return imageAssetManager;
  }

  @Nullable
  Typeface getTypeface(String fontFamily, String style) {
    FontAssetManager assetManager = getFontAssetManager();
    if (assetManager != null) {
      return assetManager.getTypeface(fontFamily, style);
    }
    return null;
  }

  private FontAssetManager getFontAssetManager() {
    if (getCallback() == null) {
      // We can't get a bitmap since we can't get a Context from the callback.
      return null;
    }

    if (fontAssetManager == null) {
      fontAssetManager = new FontAssetManager(getCallback(), fontAssetDelegate);
    }

    return fontAssetManager;
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
   * If there are masks or mattes, we can't scale the animation larger than the canvas or else
   * the off screen rendering for masks and mattes after saveLayer calls will get clipped.
   */
  private float getMaxScale(@NonNull Canvas canvas) {
    float maxScaleX = canvas.getWidth() / (float) composition.getBounds().width();
    float maxScaleY = canvas.getHeight() / (float) composition.getBounds().height();
    return Math.min(maxScaleX, maxScaleY);
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

  private static class ColorFilterData {

    final String layerName;
    @Nullable final String contentName;
    @Nullable final ColorFilter colorFilter;

    ColorFilterData(@Nullable String layerName, @Nullable String contentName,
        @Nullable ColorFilter colorFilter) {
      this.layerName = layerName;
      this.contentName = contentName;
      this.colorFilter = colorFilter;
    }

    @Override public int hashCode() {
      int hashCode = 17;
      if (layerName != null) {
        hashCode = hashCode * 31 * layerName.hashCode();
      }

      if (contentName != null) {
        hashCode = hashCode * 31 * contentName.hashCode();
      }
      return hashCode;
    }

    @Override public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }

      if (!(obj instanceof ColorFilterData)) {
        return false;
      }

      final ColorFilterData other = (ColorFilterData) obj;

      return hashCode() == other.hashCode() && colorFilter == other.colorFilter;

    }
  }
}
