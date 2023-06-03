package com.airbnb.lottie;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;

import com.airbnb.lottie.animation.LPaint;
import com.airbnb.lottie.manager.FontAssetManager;
import com.airbnb.lottie.manager.ImageAssetManager;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.Marker;
import com.airbnb.lottie.model.layer.CompositionLayer;
import com.airbnb.lottie.parser.LayerParser;
import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.LottieThreadFactory;
import com.airbnb.lottie.utils.LottieValueAnimator;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.LottieValueCallback;
import com.airbnb.lottie.value.SimpleLottieValueCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This can be used to show an lottie animation in any place that would normally take a drawable.
 *
 * @see <a href="http://airbnb.io/lottie">Full Documentation</a>
 */
@SuppressWarnings({"WeakerAccess"})
public class LottieDrawable extends Drawable implements Drawable.Callback, Animatable {
  private interface LazyCompositionTask {
    void run(LottieComposition composition);
  }

  /**
   * Internal record keeping of the desired play state when {@link #isVisible()} transitions to or is false.
   * <p>
   * If the animation was playing when it becomes invisible or play/pause is called on it while it is invisible, it will
   * store the state and then take the appropriate action when the drawable becomes visible again.
   */
  private enum OnVisibleAction {
    NONE,
    PLAY,
    RESUME,
  }

  private LottieComposition composition;
  private final LottieValueAnimator animator = new LottieValueAnimator();

  // Call animationsEnabled() instead of using these fields directly.
  private boolean systemAnimationsEnabled = true;
  private boolean ignoreSystemAnimationsDisabled = false;

  private boolean safeMode = false;
  private OnVisibleAction onVisibleAction = OnVisibleAction.NONE;

  private final ArrayList<LazyCompositionTask> lazyCompositionTasks = new ArrayList<>();

  /**
   * ImageAssetManager created automatically by Lottie for views.
   */
  @Nullable
  private ImageAssetManager imageAssetManager;
  @Nullable
  private String imageAssetsFolder;
  @Nullable
  private ImageAssetDelegate imageAssetDelegate;
  @Nullable
  private FontAssetManager fontAssetManager;
  @Nullable
  private Map<String, Typeface> fontMap;
  /**
   * Will be set if manually overridden by {@link #setDefaultFontFileExtension(String)}.
   * This must be stored as a field in case it is set before the font asset delegate
   * has been created.
   */
  @Nullable String defaultFontFileExtension;
  @Nullable
  FontAssetDelegate fontAssetDelegate;
  @Nullable
  TextDelegate textDelegate;
  private boolean enableMergePaths;
  private boolean maintainOriginalImageBounds = false;
  private boolean clipToCompositionBounds = true;
  @Nullable
  private CompositionLayer compositionLayer;
  private int alpha = 255;
  private boolean performanceTrackingEnabled;
  private boolean outlineMasksAndMattes;
  private boolean isApplyingOpacityToLayersEnabled;

  private RenderMode renderMode = RenderMode.AUTOMATIC;
  /**
   * The actual render mode derived from {@link #renderMode}.
   */
  private boolean useSoftwareRendering = false;
  private final Matrix renderingMatrix = new Matrix();
  private Bitmap softwareRenderingBitmap;
  private Canvas softwareRenderingCanvas;
  private Rect canvasClipBounds;
  private RectF canvasClipBoundsRectF;
  private Paint softwareRenderingPaint;
  private Rect softwareRenderingSrcBoundsRect;
  private Rect softwareRenderingDstBoundsRect;
  private RectF softwareRenderingDstBoundsRectF;
  private RectF softwareRenderingTransformedBounds;
  private Matrix softwareRenderingOriginalCanvasMatrix;
  private Matrix softwareRenderingOriginalCanvasMatrixInverse;

  private AsyncUpdates asyncUpdates = AsyncUpdates.AUTOMATIC;
  private final ValueAnimator.AnimatorUpdateListener progressUpdateListener = animation -> {
    if (getAsyncUpdatesEnabled()) {
      // Render a new frame.
      // If draw is called while lastDrawnProgress is still recent enough, it will
      // draw straight away and then enqueue a background setProgress immediately after draw
      // finishes.
      invalidateSelf();
    } else if (compositionLayer != null) {
      compositionLayer.setProgress(animator.getAnimatedValueAbsolute());
    }
  };

  /**
   * Ensures that setProgress and draw will never happen at the same time on different threads.
   * If that were to happen, parts of the animation may be on one frame while other parts would
   * be on another.
   */
  private final Semaphore setProgressDrawLock = new Semaphore(1);
  /**
   * The executor that {@link AsyncUpdates} will be run on.
   * <p/>
   * Defaults to a core size of 0 so that when no animations are playing, there will be no
   * idle cores consuming resources.
   * <p/>
   * Allows up to two active threads so that if there are many animations, they can all work in parallel.
   * Two was arbitrarily chosen but should be sufficient for most uses cases. In the case of a single
   * animation, this should never exceed one.
   * <p/>
   * Each thread will timeout after 35ms which gives it enough time to persist for one frame, one dropped frame
   * and a few extra ms just in case.
   */
  private static final Executor setProgressExecutor = new ThreadPoolExecutor(0, 2, 35, TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue<>(), new LottieThreadFactory());
  private final Runnable updateProgressRunnable = () -> {
    CompositionLayer compositionLayer = this.compositionLayer;
    if (compositionLayer == null) {
      return;
    }
    try {
      setProgressDrawLock.acquire();
      compositionLayer.setProgress(animator.getAnimatedValueAbsolute());
    } catch (InterruptedException e) {
      // Do nothing.
    } finally {
      setProgressDrawLock.release();
    }
  };
  private float lastDrawnProgress = -Float.MAX_VALUE;
  private static final float MAX_DELTA_MS_ASYNC_SET_PROGRESS = 3 / 60f * 1000;

  /**
   * True if the drawable has not been drawn since the last invalidateSelf.
   * We can do this to prevent things like bounds from getting recalculated
   * many times.
   */
  private boolean isDirty = false;

  @IntDef({RESTART, REVERSE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface RepeatMode {
  }

  /**
   * When the animation reaches the end and <code>repeatCount</code> is INFINITE
   * or a positive value, the animation restarts from the beginning.
   */
  public static final int RESTART = ValueAnimator.RESTART;
  /**
   * When the animation reaches the end and <code>repeatCount</code> is INFINITE
   * or a positive value, the animation reverses direction on every iteration.
   */
  public static final int REVERSE = ValueAnimator.REVERSE;
  /**
   * This value used used with the {@link #setRepeatCount(int)} property to repeat
   * the animation indefinitely.
   */
  public static final int INFINITE = ValueAnimator.INFINITE;

  public LottieDrawable() {
    animator.addUpdateListener(progressUpdateListener);
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

  public boolean enableMergePathsForKitKatAndAbove() {
    return enableMergePaths;
  }

  /**
   * Enable this to get merge path support for devices running KitKat (19) and above.
   * <p>
   * Merge paths currently don't work if the the operand shape is entirely contained within the
   * first shape. If you need to cut out one shape from another shape, use an even-odd fill type
   * instead of using merge paths.
   */
  public void enableMergePathsForKitKatAndAbove(boolean enable) {
    if (enableMergePaths == enable) {
      return;
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      Logger.warning("Merge paths are not supported pre-Kit Kat.");
      return;
    }
    enableMergePaths = enable;
    if (composition != null) {
      buildCompositionLayer();
    }
  }

  public boolean isMergePathsEnabledForKitKatAndAbove() {
    return enableMergePaths;
  }

  /**
   * Sets whether or not Lottie should clip to the original animation composition bounds.
   * <p>
   * Defaults to true.
   */
  public void setClipToCompositionBounds(boolean clipToCompositionBounds) {
    if (clipToCompositionBounds != this.clipToCompositionBounds) {
      this.clipToCompositionBounds = clipToCompositionBounds;
      CompositionLayer compositionLayer = this.compositionLayer;
      if (compositionLayer != null) {
        compositionLayer.setClipToCompositionBounds(clipToCompositionBounds);
      }
      invalidateSelf();
    }
  }

  /**
   * Gets whether or not Lottie should clip to the original animation composition bounds.
   * <p>
   * Defaults to true.
   */
  public boolean getClipToCompositionBounds() {
    return clipToCompositionBounds;
  }

  /**
   * If you use image assets, you must explicitly specify the folder in assets/ in which they are
   * located because bodymovin uses the name filenames across all compositions (img_#).
   * Do NOT rename the images themselves.
   * <p>
   * If your images are located in src/main/assets/airbnb_loader/ then call
   * `setImageAssetsFolder("airbnb_loader/");`.
   * <p>
   * <p>
   * Be wary if you are using many images, however. Lottie is designed to work with vector shapes
   * from After Effects. If your images look like they could be represented with vector shapes,
   * see if it is possible to convert them to shape layers and re-export your animation. Check
   * the documentation at http://airbnb.io/lottie for more information about importing shapes from
   * Sketch or Illustrator to avoid this.
   */
  public void setImagesAssetsFolder(@Nullable String imageAssetsFolder) {
    this.imageAssetsFolder = imageAssetsFolder;
  }

  @Nullable
  public String getImageAssetsFolder() {
    return imageAssetsFolder;
  }

  /**
   * When true, dynamically set bitmaps will be drawn with the exact bounds of the original animation, regardless of the bitmap size.
   * When false, dynamically set bitmaps will be drawn at the top left of the original image but with its own bounds.
   * <p>
   * Defaults to false.
   */
  public void setMaintainOriginalImageBounds(boolean maintainOriginalImageBounds) {
    this.maintainOriginalImageBounds = maintainOriginalImageBounds;
  }

  /**
   * When true, dynamically set bitmaps will be drawn with the exact bounds of the original animation, regardless of the bitmap size.
   * When false, dynamically set bitmaps will be drawn at the top left of the original image but with its own bounds.
   * <p>
   * Defaults to false.
   */
  public boolean getMaintainOriginalImageBounds() {
    return maintainOriginalImageBounds;
  }

  /**
   * Create a composition with {@link LottieCompositionFactory}
   *
   * @return True if the composition is different from the previously set composition, false otherwise.
   */
  public boolean setComposition(LottieComposition composition) {
    if (this.composition == composition) {
      return false;
    }

    isDirty = true;
    clearComposition();
    this.composition = composition;
    buildCompositionLayer();
    animator.setComposition(composition);
    setProgress(animator.getAnimatedFraction());

    // We copy the tasks to a new ArrayList so that if this method is called from multiple threads,
    // then there won't be two iterators iterating and removing at the same time.
    Iterator<LazyCompositionTask> it = new ArrayList<>(lazyCompositionTasks).iterator();
    while (it.hasNext()) {
      LazyCompositionTask t = it.next();
      // The task should never be null but it appears to happen in rare cases. Maybe it's an oem-specific or ART bug.
      // https://github.com/airbnb/lottie-android/issues/1702
      if (t != null) {
        t.run(composition);
      }
      it.remove();
    }
    lazyCompositionTasks.clear();

    composition.setPerformanceTrackingEnabled(performanceTrackingEnabled);
    computeRenderMode();

    // Ensure that ImageView updates the drawable width/height so it can
    // properly calculate its drawable matrix.
    Callback callback = getCallback();
    if (callback instanceof ImageView) {
      ((ImageView) callback).setImageDrawable(null);
      ((ImageView) callback).setImageDrawable(this);
    }

    return true;
  }

  /**
   * Call this to set whether or not to render with hardware or software acceleration.
   * Lottie defaults to Automatic which will use hardware acceleration unless:
   * 1) There are dash paths and the device is pre-Pie.
   * 2) There are more than 4 masks and mattes and the device is pre-Pie.
   * Hardware acceleration is generally faster for those devices unless
   * there are many large mattes and masks in which case there is a lot
   * of GPU uploadTexture thrashing which makes it much slower.
   * <p>
   * In most cases, hardware rendering will be faster, even if you have mattes and masks.
   * However, if you have multiple mattes and masks (especially large ones), you
   * should test both render modes. You should also test on pre-Pie and Pie+ devices
   * because the underlying rendering engine changed significantly.
   *
   * @see <a href="https://developer.android.com/guide/topics/graphics/hardware-accel#unsupported">Android Hardware Acceleration</a>
   */
  public void setRenderMode(RenderMode renderMode) {
    this.renderMode = renderMode;
    computeRenderMode();
  }

  /**
   * Returns the current value of {@link AsyncUpdates}. Refer to the docs for {@link AsyncUpdates} for more info.
   */
  public AsyncUpdates getAsyncUpdates() {
    return asyncUpdates;
  }

  /**
   * Similar to {@link #getAsyncUpdates()} except it returns the actual
   * boolean value for whether async updates are enabled or not.
   * This is useful when the mode is automatic and you want to know
   * whether automatic is defaulting to enabled or not.
   */
  public boolean getAsyncUpdatesEnabled() {
    return asyncUpdates == AsyncUpdates.ENABLED;
  }

  /**
   * **Note: this API is experimental and may changed.**
   * <p/>
   * Sets the current value for {@link AsyncUpdates}. Refer to the docs for {@link AsyncUpdates} for more info.
   */
  public void setAsyncUpdates(AsyncUpdates asyncUpdates) {
    this.asyncUpdates = asyncUpdates;
  }

  /**
   * Returns the actual render mode being used. It will always be {@link RenderMode#HARDWARE} or {@link RenderMode#SOFTWARE}.
   * When the render mode is set to AUTOMATIC, the value will be derived from {@link RenderMode#useSoftwareRendering(int, boolean, int)}.
   */
  public RenderMode getRenderMode() {
    return useSoftwareRendering ? RenderMode.SOFTWARE : RenderMode.HARDWARE;
  }

  private void computeRenderMode() {
    LottieComposition composition = this.composition;
    if (composition == null) {
      return;
    }
    useSoftwareRendering = renderMode.useSoftwareRendering(
        Build.VERSION.SDK_INT, composition.hasDashPattern(), composition.getMaskAndMatteCount());
  }

  public void setPerformanceTrackingEnabled(boolean enabled) {
    performanceTrackingEnabled = enabled;
    if (composition != null) {
      composition.setPerformanceTrackingEnabled(enabled);
    }
  }

  /**
   * Enable this to debug slow animations by outlining masks and mattes. The performance overhead of the masks and mattes will
   * be proportional to the surface area of all of the masks/mattes combined.
   * <p>
   * DO NOT leave this enabled in production.
   */
  public void setOutlineMasksAndMattes(boolean outline) {
    if (outlineMasksAndMattes == outline) {
      return;
    }
    outlineMasksAndMattes = outline;
    if (compositionLayer != null) {
      compositionLayer.setOutlineMasksAndMattes(outline);
    }
  }

  @Nullable
  public PerformanceTracker getPerformanceTracker() {
    if (composition != null) {
      return composition.getPerformanceTracker();
    }
    return null;
  }

  /**
   * Sets whether to apply opacity to the each layer instead of shape.
   * <p>
   * Opacity is normally applied directly to a shape. In cases where translucent shapes overlap, applying opacity to a layer will be more accurate
   * at the expense of performance.
   * <p>
   * The default value is false.
   * <p>
   * Note: This process is very expensive. The performance impact will be reduced when hardware acceleration is enabled.
   *
   * @see android.view.View#setLayerType(int, android.graphics.Paint)
   * @see LottieAnimationView#setRenderMode(RenderMode)
   */
  public void setApplyingOpacityToLayersEnabled(boolean isApplyingOpacityToLayersEnabled) {
    this.isApplyingOpacityToLayersEnabled = isApplyingOpacityToLayersEnabled;
  }

  /**
   * This API no longer has any effect.
   */
  @Deprecated
  public void disableExtraScaleModeInFitXY() {
  }

  public boolean isApplyingOpacityToLayersEnabled() {
    return isApplyingOpacityToLayersEnabled;
  }

  private void buildCompositionLayer() {
    LottieComposition composition = this.composition;
    if (composition == null) {
      return;
    }
    compositionLayer = new CompositionLayer(
        this, LayerParser.parse(composition), composition.getLayers(), composition);
    if (outlineMasksAndMattes) {
      compositionLayer.setOutlineMasksAndMattes(true);
    }
    compositionLayer.setClipToCompositionBounds(clipToCompositionBounds);
  }

  public void clearComposition() {
    if (animator.isRunning()) {
      animator.cancel();
      if (!isVisible()) {
        onVisibleAction = OnVisibleAction.NONE;
      }
    }
    composition = null;
    compositionLayer = null;
    imageAssetManager = null;
    lastDrawnProgress = -Float.MAX_VALUE;
    animator.clearComposition();
    invalidateSelf();
  }

  /**
   * If you are experiencing a device specific crash that happens during drawing, you can set this to true
   * for those devices. If set to true, draw will be wrapped with a try/catch which will cause Lottie to
   * render an empty frame rather than crash your app.
   * <p>
   * Ideally, you will never need this and the vast majority of apps and animations won't. However, you may use
   * this for very specific cases if absolutely necessary.
   */
  public void setSafeMode(boolean safeMode) {
    this.safeMode = safeMode;
  }

  @Override
  public void invalidateSelf() {
    if (isDirty) {
      return;
    }
    isDirty = true;
    final Callback callback = getCallback();
    if (callback != null) {
      callback.invalidateDrawable(this);
    }
  }

  @Override
  public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
    this.alpha = alpha;
    invalidateSelf();
  }

  @Override
  public int getAlpha() {
    return alpha;
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    Logger.warning("Use addColorFilter instead.");
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  /**
   * Helper for the async execution path to potentially call setProgress
   * before drawing if the current progress has drifted sufficiently far
   * from the last set progress.
   *
   * @see AsyncUpdates
   * @see #setAsyncUpdates(AsyncUpdates)
   */
  private boolean shouldSetProgressBeforeDrawing() {
    LottieComposition composition = this.composition;
    if (composition == null) {
      return false;
    }
    float lastDrawnProgress = this.lastDrawnProgress;
    float currentProgress = animator.getAnimatedValueAbsolute();
    this.lastDrawnProgress = currentProgress;

    float duration = composition.getDuration();

    float deltaProgress = Math.abs(currentProgress - lastDrawnProgress);
    float deltaMs = deltaProgress * duration;
    return deltaMs >= MAX_DELTA_MS_ASYNC_SET_PROGRESS;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    CompositionLayer compositionLayer = this.compositionLayer;
    if (compositionLayer == null) {
      return;
    }
    boolean asyncUpdatesEnabled = getAsyncUpdatesEnabled();
    try {
      if (asyncUpdatesEnabled) {
        setProgressDrawLock.acquire();
      }
      L.beginSection("Drawable#draw");

      if (asyncUpdatesEnabled && shouldSetProgressBeforeDrawing()) {
        setProgress(animator.getAnimatedValueAbsolute());
      }

      if (safeMode) {
        try {
          if (useSoftwareRendering) {
            renderAndDrawAsBitmap(canvas, compositionLayer);
          } else {
            drawDirectlyToCanvas(canvas);
          }
        } catch (Throwable e) {
          Logger.error("Lottie crashed in draw!", e);
        }
      } else {
        if (useSoftwareRendering) {
          renderAndDrawAsBitmap(canvas, compositionLayer);
        } else {
          drawDirectlyToCanvas(canvas);
        }
      }

      isDirty = false;
    } catch (InterruptedException e) {
      // Do nothing.
    } finally {
      L.endSection("Drawable#draw");
      if (asyncUpdatesEnabled) {
        setProgressDrawLock.release();
        if (compositionLayer.getProgress() != animator.getAnimatedValueAbsolute()) {
          setProgressExecutor.execute(updateProgressRunnable);
        }
      }
    }
  }

  /**
   * To be used by lottie-compose only.
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public void draw(Canvas canvas, Matrix matrix) {
    CompositionLayer compositionLayer = this.compositionLayer;
    LottieComposition composition = this.composition;
    if (compositionLayer == null || composition == null) {
      return;
    }
    boolean asyncUpdatesEnabled = getAsyncUpdatesEnabled();
    try {
      if (asyncUpdatesEnabled) {
        setProgressDrawLock.acquire();
        if (shouldSetProgressBeforeDrawing()) {
          setProgress(animator.getAnimatedValueAbsolute());
        }
      }

      if (useSoftwareRendering) {
        canvas.save();
        canvas.concat(matrix);
        renderAndDrawAsBitmap(canvas, compositionLayer);
        canvas.restore();
      } else {
        long start = System.nanoTime();
        compositionLayer.draw(canvas, matrix, alpha);
        long end = System.nanoTime();
        L.drawTimeNs.getAndAdd(end - start);
      }
      isDirty = false;
    } catch (InterruptedException e) {
      // Do nothing.
    } finally {
      if (asyncUpdatesEnabled) {
        setProgressDrawLock.release();
        if (compositionLayer.getProgress() != animator.getAnimatedValueAbsolute()) {
          setProgressExecutor.execute(updateProgressRunnable);
        }
      }
    }
  }

  // <editor-fold desc="animator">

  @MainThread
  @Override
  public void start() {
    Callback callback = getCallback();
    if (callback instanceof View && ((View) callback).isInEditMode()) {
      // Don't auto play when in edit mode.
      return;
    }
    playAnimation();
  }

  @MainThread
  @Override
  public void stop() {
    endAnimation();
  }

  @Override
  public boolean isRunning() {
    return isAnimating();
  }

  /**
   * Plays the animation from the beginning. If speed is {@literal <} 0, it will start at the end
   * and play towards the beginning
   */
  @MainThread
  public void playAnimation() {
    if (compositionLayer == null) {
      lazyCompositionTasks.add(c -> playAnimation());
      return;
    }

    computeRenderMode();
    if (animationsEnabled() || getRepeatCount() == 0) {
      if (isVisible()) {
        animator.playAnimation();
        onVisibleAction = OnVisibleAction.NONE;
      } else {
        onVisibleAction = OnVisibleAction.PLAY;
      }
    }
    if (!animationsEnabled()) {
      setFrame((int) (getSpeed() < 0 ? getMinFrame() : getMaxFrame()));
      animator.endAnimation();
      if (!isVisible()) {
        onVisibleAction = OnVisibleAction.NONE;
      }
    }
  }

  @MainThread
  public void endAnimation() {
    lazyCompositionTasks.clear();
    animator.endAnimation();
    if (!isVisible()) {
      onVisibleAction = OnVisibleAction.NONE;
    }
  }

  /**
   * Continues playing the animation from its current position. If speed {@literal <} 0, it will play backwards
   * from the current position.
   */
  @MainThread
  public void resumeAnimation() {
    if (compositionLayer == null) {
      lazyCompositionTasks.add(c -> resumeAnimation());
      return;
    }

    computeRenderMode();
    if (animationsEnabled() || getRepeatCount() == 0) {
      if (isVisible()) {
        animator.resumeAnimation();
        onVisibleAction = OnVisibleAction.NONE;
      } else {
        onVisibleAction = OnVisibleAction.RESUME;
      }
    }
    if (!animationsEnabled()) {
      setFrame((int) (getSpeed() < 0 ? getMinFrame() : getMaxFrame()));
      animator.endAnimation();
      if (!isVisible()) {
        onVisibleAction = OnVisibleAction.NONE;
      }
    }
  }

  /**
   * Sets the minimum frame that the animation will start from when playing or looping.
   */
  public void setMinFrame(final int minFrame) {
    if (composition == null) {
      lazyCompositionTasks.add(c -> setMinFrame(minFrame));
      return;
    }
    animator.setMinFrame(minFrame);
  }

  /**
   * Returns the minimum frame set by {@link #setMinFrame(int)} or {@link #setMinProgress(float)}
   */
  public float getMinFrame() {
    return animator.getMinFrame();
  }

  /**
   * Sets the minimum progress that the animation will start from when playing or looping.
   */
  public void setMinProgress(final float minProgress) {
    if (composition == null) {
      lazyCompositionTasks.add(c -> setMinProgress(minProgress));
      return;
    }
    setMinFrame((int) MiscUtils.lerp(composition.getStartFrame(), composition.getEndFrame(), minProgress));
  }

  /**
   * Sets the maximum frame that the animation will end at when playing or looping.
   * <p>
   * The value will be clamped to the composition bounds. For example, setting Integer.MAX_VALUE would result in the same
   * thing as composition.endFrame.
   */
  public void setMaxFrame(final int maxFrame) {
    if (composition == null) {
      lazyCompositionTasks.add(c -> setMaxFrame(maxFrame));
      return;
    }
    animator.setMaxFrame(maxFrame + 0.99f);
  }

  /**
   * Returns the maximum frame set by {@link #setMaxFrame(int)} or {@link #setMaxProgress(float)}
   */
  public float getMaxFrame() {
    return animator.getMaxFrame();
  }

  /**
   * Sets the maximum progress that the animation will end at when playing or looping.
   */
  public void setMaxProgress(@FloatRange(from = 0f, to = 1f) final float maxProgress) {
    if (composition == null) {
      lazyCompositionTasks.add(c -> setMaxProgress(maxProgress));
      return;
    }
    animator.setMaxFrame(MiscUtils.lerp(composition.getStartFrame(), composition.getEndFrame(), maxProgress));
  }

  /**
   * Sets the minimum frame to the start time of the specified marker.
   *
   * @throws IllegalArgumentException if the marker is not found.
   */
  public void setMinFrame(final String markerName) {
    if (composition == null) {
      lazyCompositionTasks.add(c -> setMinFrame(markerName));
      return;
    }
    Marker marker = composition.getMarker(markerName);
    if (marker == null) {
      throw new IllegalArgumentException("Cannot find marker with name " + markerName + ".");
    }
    setMinFrame((int) marker.startFrame);
  }

  /**
   * Sets the maximum frame to the start time + duration of the specified marker.
   *
   * @throws IllegalArgumentException if the marker is not found.
   */
  public void setMaxFrame(final String markerName) {
    if (composition == null) {
      lazyCompositionTasks.add(c -> setMaxFrame(markerName));
      return;
    }
    Marker marker = composition.getMarker(markerName);
    if (marker == null) {
      throw new IllegalArgumentException("Cannot find marker with name " + markerName + ".");
    }
    setMaxFrame((int) (marker.startFrame + marker.durationFrames));
  }

  /**
   * Sets the minimum and maximum frame to the start time and start time + duration
   * of the specified marker.
   *
   * @throws IllegalArgumentException if the marker is not found.
   */
  public void setMinAndMaxFrame(final String markerName) {
    if (composition == null) {
      lazyCompositionTasks.add(c -> setMinAndMaxFrame(markerName));
      return;
    }
    Marker marker = composition.getMarker(markerName);
    if (marker == null) {
      throw new IllegalArgumentException("Cannot find marker with name " + markerName + ".");
    }
    int startFrame = (int) marker.startFrame;
    setMinAndMaxFrame(startFrame, startFrame + (int) marker.durationFrames);
  }

  /**
   * Sets the minimum and maximum frame to the start marker start and the maximum frame to the end marker start.
   * playEndMarkerStartFrame determines whether or not to play the frame that the end marker is on. If the end marker
   * represents the end of the section that you want, it should be true. If the marker represents the beginning of the
   * next section, it should be false.
   *
   * @throws IllegalArgumentException if either marker is not found.
   */
  public void setMinAndMaxFrame(final String startMarkerName, final String endMarkerName, final boolean playEndMarkerStartFrame) {
    if (composition == null) {
      lazyCompositionTasks.add(c -> setMinAndMaxFrame(startMarkerName, endMarkerName, playEndMarkerStartFrame));
      return;
    }
    Marker startMarker = composition.getMarker(startMarkerName);
    if (startMarker == null) {
      throw new IllegalArgumentException("Cannot find marker with name " + startMarkerName + ".");
    }
    int startFrame = (int) startMarker.startFrame;

    final Marker endMarker = composition.getMarker(endMarkerName);
    if (endMarker == null) {
      throw new IllegalArgumentException("Cannot find marker with name " + endMarkerName + ".");
    }
    int endFrame = (int) (endMarker.startFrame + (playEndMarkerStartFrame ? 1f : 0f));

    setMinAndMaxFrame(startFrame, endFrame);
  }

  /**
   * @see #setMinFrame(int)
   * @see #setMaxFrame(int)
   */
  public void setMinAndMaxFrame(final int minFrame, final int maxFrame) {
    if (composition == null) {
      lazyCompositionTasks.add(c -> setMinAndMaxFrame(minFrame, maxFrame));
      return;
    }
    // Adding 0.99 ensures that the maxFrame itself gets played.
    animator.setMinAndMaxFrames(minFrame, maxFrame + 0.99f);
  }

  /**
   * @see #setMinProgress(float)
   * @see #setMaxProgress(float)
   */
  public void setMinAndMaxProgress(
      @FloatRange(from = 0f, to = 1f) final float minProgress,
      @FloatRange(from = 0f, to = 1f) final float maxProgress) {
    if (composition == null) {
      lazyCompositionTasks.add(c -> setMinAndMaxProgress(minProgress, maxProgress));
      return;
    }

    setMinAndMaxFrame((int) MiscUtils.lerp(composition.getStartFrame(), composition.getEndFrame(), minProgress),
        (int) MiscUtils.lerp(composition.getStartFrame(), composition.getEndFrame(), maxProgress));
  }

  /**
   * Reverses the current animation speed. This does NOT play the animation.
   *
   * @see #setSpeed(float)
   * @see #playAnimation()
   * @see #resumeAnimation()
   */
  public void reverseAnimationSpeed() {
    animator.reverseAnimationSpeed();
  }

  /**
   * Sets the playback speed. If speed {@literal <} 0, the animation will play backwards.
   */
  public void setSpeed(float speed) {
    animator.setSpeed(speed);
  }

  /**
   * Returns the current playback speed. This will be {@literal <} 0 if the animation is playing backwards.
   */
  public float getSpeed() {
    return animator.getSpeed();
  }

  public void addAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
    animator.addUpdateListener(updateListener);
  }

  public void removeAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
    animator.removeUpdateListener(updateListener);
  }

  public void removeAllUpdateListeners() {
    animator.removeAllUpdateListeners();
    animator.addUpdateListener(progressUpdateListener);
  }

  public void addAnimatorListener(Animator.AnimatorListener listener) {
    animator.addListener(listener);
  }

  public void removeAnimatorListener(Animator.AnimatorListener listener) {
    animator.removeListener(listener);
  }

  public void removeAllAnimatorListeners() {
    animator.removeAllListeners();
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public void addAnimatorPauseListener(Animator.AnimatorPauseListener listener) {
    animator.addPauseListener(listener);
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public void removeAnimatorPauseListener(Animator.AnimatorPauseListener listener) {
    animator.removePauseListener(listener);
  }

  /**
   * Sets the progress to the specified frame.
   * If the composition isn't set yet, the progress will be set to the frame when
   * it is.
   */
  public void setFrame(final int frame) {
    if (composition == null) {
      lazyCompositionTasks.add(c -> setFrame(frame));
      return;
    }

    animator.setFrame(frame);
  }

  /**
   * Get the currently rendered frame.
   */
  public int getFrame() {
    return (int) animator.getFrame();
  }

  public void setProgress(@FloatRange(from = 0f, to = 1f) final float progress) {
    if (composition == null) {
      lazyCompositionTasks.add(c -> setProgress(progress));
      return;
    }
    L.beginSection("Drawable#setProgress");
    animator.setFrame(composition.getFrameForProgress(progress));
    L.endSection("Drawable#setProgress");
  }

  /**
   * @see #setRepeatCount(int)
   */
  @Deprecated
  public void loop(boolean loop) {
    animator.setRepeatCount(loop ? ValueAnimator.INFINITE : 0);
  }

  /**
   * Defines what this animation should do when it reaches the end. This
   * setting is applied only when the repeat count is either greater than
   * 0 or {@link #INFINITE}. Defaults to {@link #RESTART}.
   *
   * @param mode {@link #RESTART} or {@link #REVERSE}
   */
  public void setRepeatMode(@RepeatMode int mode) {
    animator.setRepeatMode(mode);
  }

  /**
   * Defines what this animation should do when it reaches the end.
   *
   * @return either one of {@link #REVERSE} or {@link #RESTART}
   */
  @SuppressLint("WrongConstant")
  @RepeatMode
  public int getRepeatMode() {
    return animator.getRepeatMode();
  }

  /**
   * Sets how many times the animation should be repeated. If the repeat
   * count is 0, the animation is never repeated. If the repeat count is
   * greater than 0 or {@link #INFINITE}, the repeat mode will be taken
   * into account. The repeat count is 0 by default.
   *
   * @param count the number of times the animation should be repeated
   */
  public void setRepeatCount(int count) {
    animator.setRepeatCount(count);
  }

  /**
   * Defines how many times the animation should repeat. The default value
   * is 0.
   *
   * @return the number of times the animation should repeat, or {@link #INFINITE}
   */
  public int getRepeatCount() {
    return animator.getRepeatCount();
  }


  @SuppressWarnings("unused")
  public boolean isLooping() {
    return animator.getRepeatCount() == ValueAnimator.INFINITE;
  }

  public boolean isAnimating() {
    // On some versions of Android, this is called from the LottieAnimationView constructor, before animator was created.
    // https://github.com/airbnb/lottie-android/issues/1430
    //noinspection ConstantConditions
    if (animator == null) {
      return false;
    }
    return animator.isRunning();
  }

  boolean isAnimatingOrWillAnimateOnVisible() {
    if (isVisible()) {
      return animator.isRunning();
    } else {
      return onVisibleAction == OnVisibleAction.PLAY || onVisibleAction == OnVisibleAction.RESUME;
    }
  }

  private boolean animationsEnabled() {
    return systemAnimationsEnabled || ignoreSystemAnimationsDisabled;
  }

  /**
   * Tell Lottie that system animations are disabled. When using {@link LottieAnimationView} or Compose {@code LottieAnimation}, this is done
   * automatically. However, if you are using LottieDrawable on its own, you should set this to false when
   * {@link com.airbnb.lottie.utils.Utils#getAnimationScale(Context)} is 0.
   */
  public void setSystemAnimationsAreEnabled(Boolean areEnabled) {
    systemAnimationsEnabled = areEnabled;
  }

// </editor-fold>

  /**
   * Allows ignoring system animations settings, therefore allowing animations to run even if they are disabled.
   * <p>
   * Defaults to false.
   *
   * @param ignore if true animations will run even when they are disabled in the system settings.
   */
  public void setIgnoreDisabledSystemAnimations(boolean ignore) {
    ignoreSystemAnimationsDisabled = ignore;
  }

  /**
   * Lottie files can specify a target frame rate. By default, Lottie ignores it and re-renders
   * on every frame. If that behavior is undesirable, you can set this to true to use the composition
   * frame rate instead.
   * <p>
   * Note: composition frame rates are usually lower than display frame rates
   * so this will likely make your animation feel janky. However, it may be desirable
   * for specific situations such as pixel art that are intended to have low frame rates.
   */
  public void setUseCompositionFrameRate(boolean useCompositionFrameRate) {
    animator.setUseCompositionFrameRate(useCompositionFrameRate);
  }

  /**
   * Use this if you can't bundle images with your app. This may be useful if you download the
   * animations from the network or have the images saved to an SD Card. In that case, Lottie
   * will defer the loading of the bitmap to this delegate.
   * <p>
   * Be wary if you are using many images, however. Lottie is designed to work with vector shapes
   * from After Effects. If your images look like they could be represented with vector shapes,
   * see if it is possible to convert them to shape layers and re-export your animation. Check
   * the documentation at <a href="http://airbnb.io/lottie">http://airbnb.io/lottie</a> for more information about importing shapes from
   * Sketch or Illustrator to avoid this.
   */
  public void setImageAssetDelegate(ImageAssetDelegate assetDelegate) {
    this.imageAssetDelegate = assetDelegate;
    if (imageAssetManager != null) {
      imageAssetManager.setDelegate(assetDelegate);
    }
  }

  /**
   * Use this to manually set fonts.
   */
  public void setFontAssetDelegate(FontAssetDelegate assetDelegate) {
    this.fontAssetDelegate = assetDelegate;
    if (fontAssetManager != null) {
      fontAssetManager.setDelegate(assetDelegate);
    }
  }

  /**
   * Set a map from font name keys to Typefaces.
   * The keys can be in the form:
   * * fontFamily
   * * fontFamily-fontStyle
   * * fontName
   * All 3 are defined as fName, fFamily, and fStyle in the Lottie file.
   * <p>
   * If you change a value in fontMap, create a new map or call
   * {@link #invalidateSelf()}. Setting the same map again will noop.
   */
  public void setFontMap(@Nullable Map<String, Typeface> fontMap) {
    if (fontMap == this.fontMap) {
      return;
    }
    this.fontMap = fontMap;
    invalidateSelf();
  }

  public void setTextDelegate(@SuppressWarnings("NullableProblems") TextDelegate textDelegate) {
    this.textDelegate = textDelegate;
  }

  @Nullable
  public TextDelegate getTextDelegate() {
    return textDelegate;
  }

  public boolean useTextGlyphs() {
    return fontMap == null && textDelegate == null && composition.getCharacters().size() > 0;
  }

  public LottieComposition getComposition() {
    return composition;
  }

  public void cancelAnimation() {
    lazyCompositionTasks.clear();
    animator.cancel();
    if (!isVisible()) {
      onVisibleAction = OnVisibleAction.NONE;
    }
  }

  public void pauseAnimation() {
    lazyCompositionTasks.clear();
    animator.pauseAnimation();
    if (!isVisible()) {
      onVisibleAction = OnVisibleAction.NONE;
    }
  }

  @FloatRange(from = 0f, to = 1f)
  public float getProgress() {
    return animator.getAnimatedValueAbsolute();
  }

  @Override
  public int getIntrinsicWidth() {
    return composition == null ? -1 : composition.getBounds().width();
  }

  @Override
  public int getIntrinsicHeight() {
    return composition == null ? -1 : composition.getBounds().height();
  }

  /**
   * Takes a {@link KeyPath}, potentially with wildcards or globstars and resolve it to a list of
   * zero or more actual {@link KeyPath Keypaths} that exist in the current animation.
   * <p>
   * If you want to set value callbacks for any of these values, it is recommend to use the
   * returned {@link KeyPath} objects because they will be internally resolved to their content
   * and won't trigger a tree walk of the animation contents when applied.
   */
  public List<KeyPath> resolveKeyPath(KeyPath keyPath) {
    if (compositionLayer == null) {
      Logger.warning("Cannot resolve KeyPath. Composition is not set yet.");
      return Collections.emptyList();
    }
    List<KeyPath> keyPaths = new ArrayList<>();
    compositionLayer.resolveKeyPath(keyPath, 0, keyPaths, new KeyPath());
    return keyPaths;
  }

  /**
   * Add an property callback for the specified {@link KeyPath}. This {@link KeyPath} can resolve
   * to multiple contents. In that case, the callback's value will apply to all of them.
   * <p>
   * Internally, this will check if the {@link KeyPath} has already been resolved with
   * {@link #resolveKeyPath(KeyPath)} and will resolve it if it hasn't.
   */
  public <T> void addValueCallback(
      final KeyPath keyPath, final T property, @Nullable final LottieValueCallback<T> callback) {
    if (compositionLayer == null) {
      lazyCompositionTasks.add(c -> addValueCallback(keyPath, property, callback));
      return;
    }
    boolean invalidate;
    if (keyPath == KeyPath.COMPOSITION) {
      compositionLayer.addValueCallback(property, callback);
      invalidate = true;
    } else if (keyPath.getResolvedElement() != null) {
      keyPath.getResolvedElement().addValueCallback(property, callback);
      invalidate = true;
    } else {
      List<KeyPath> elements = resolveKeyPath(keyPath);

      for (int i = 0; i < elements.size(); i++) {
        //noinspection ConstantConditions
        elements.get(i).getResolvedElement().addValueCallback(property, callback);
      }
      invalidate = !elements.isEmpty();
    }
    if (invalidate) {
      invalidateSelf();
      if (property == LottieProperty.TIME_REMAP) {
        // Time remapping values are read in setProgress. In order for the new value
        // to apply, we have to re-set the progress with the current progress so that the
        // time remapping can be reapplied.
        setProgress(getProgress());
      }
    }
  }

  /**
   * Overload of {@link #addValueCallback(KeyPath, Object, LottieValueCallback)} that takes an interface. This allows you to use a single abstract
   * method code block in Kotlin such as:
   * drawable.addValueCallback(yourKeyPath, LottieProperty.COLOR) { yourColor }
   */
  public <T> void addValueCallback(KeyPath keyPath, T property,
      final SimpleLottieValueCallback<T> callback) {
    addValueCallback(keyPath, property, new LottieValueCallback<>() {
      @Override
      public T getValue(LottieFrameInfo<T> frameInfo) {
        return callback.getValue(frameInfo);
      }
    });
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
      Logger.warning("Cannot update bitmap. Most likely the drawable is not added to a View " +
          "which prevents Lottie from getting a Context.");
      return null;
    }
    Bitmap ret = bm.updateBitmap(id, bitmap);
    invalidateSelf();
    return ret;
  }

  /**
   * @deprecated use {@link #getBitmapForId(String)}.
   */
  @Nullable
  @Deprecated
  public Bitmap getImageAsset(String id) {
    ImageAssetManager bm = getImageAssetManager();
    if (bm != null) {
      return bm.bitmapForId(id);
    }
    LottieImageAsset imageAsset = composition == null ? null : composition.getImages().get(id);
    if (imageAsset != null) {
      return imageAsset.getBitmap();
    }
    return null;
  }

  /**
   * Returns the bitmap that will be rendered for the given id in the Lottie animation file.
   * The id is the asset reference id stored in the "id" property of each object in the "assets" array.
   * <p>
   * The returned bitmap could be from:
   * * Embedded in the animation file as a base64 string.
   * * In the same directory as the animation file.
   * * In the same zip file as the animation file.
   * * Returned from an {@link ImageAssetDelegate}.
   * or null if the image doesn't exist from any of those places.
   */
  @Nullable
  public Bitmap getBitmapForId(String id) {
    ImageAssetManager assetManager = getImageAssetManager();
    if (assetManager != null) {
      return assetManager.bitmapForId(id);
    }
    return null;
  }

  /**
   * Returns the {@link LottieImageAsset} that will be rendered for the given id in the Lottie animation file.
   * The id is the asset reference id stored in the "id" property of each object in the "assets" array.
   * <p>
   * The returned bitmap could be from:
   * * Embedded in the animation file as a base64 string.
   * * In the same directory as the animation file.
   * * In the same zip file as the animation file.
   * * Returned from an {@link ImageAssetDelegate}.
   * or null if the image doesn't exist from any of those places.
   */
  @Nullable
  public LottieImageAsset getLottieImageAssetForId(String id) {
    LottieComposition composition = this.composition;
    if (composition == null) {
      return null;
    }
    return composition.getImages().get(id);
  }

  private ImageAssetManager getImageAssetManager() {
    if (imageAssetManager != null && !imageAssetManager.hasSameContext(getContext())) {
      imageAssetManager = null;
    }

    if (imageAssetManager == null) {
      imageAssetManager = new ImageAssetManager(getCallback(),
          imageAssetsFolder, imageAssetDelegate, composition.getImages());
    }

    return imageAssetManager;
  }

  @Nullable
  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public Typeface getTypeface(Font font) {
    Map<String, Typeface> fontMap = this.fontMap;
    if (fontMap != null) {
      String key = font.getFamily();
      if (fontMap.containsKey(key)) {
        return fontMap.get(key);
      }
      key = font.getName();
      if (fontMap.containsKey(key)) {
        return fontMap.get(key);
      }
      key = font.getFamily() + "-" + font.getStyle();
      if (fontMap.containsKey(key)) {
        return fontMap.get(key);
      }
    }

    FontAssetManager assetManager = getFontAssetManager();
    if (assetManager != null) {
      return assetManager.getTypeface(font);
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
      String defaultExtension = this.defaultFontFileExtension;
      if (defaultExtension != null) {
        fontAssetManager.setDefaultFontFileExtension(defaultFontFileExtension);
      }
    }

    return fontAssetManager;
  }

  /**
   * By default, Lottie will look in src/assets/fonts/FONT_NAME.ttf
   * where FONT_NAME is the fFamily specified in your Lottie file.
   * If your fonts have a different extension, you can override the
   * default here.
   * <p>
   * Alternatively, you can use {@link #setFontAssetDelegate(FontAssetDelegate)}
   * for more control.
   *
   * @see #setFontAssetDelegate(FontAssetDelegate)
   */
  public void setDefaultFontFileExtension(String extension) {
    defaultFontFileExtension = extension;
    FontAssetManager fam = getFontAssetManager();
    if (fam != null) {
      fam.setDefaultFontFileExtension(extension);
    }
  }

  @Nullable
  private Context getContext() {
    Callback callback = getCallback();
    if (callback == null) {
      return null;
    }

    if (callback instanceof View) {
      return ((View) callback).getContext();
    }
    return null;
  }

  @Override public boolean setVisible(boolean visible, boolean restart) {
    // Sometimes, setVisible(false) gets called twice in a row. If we don't check wasNotVisibleAlready, we could
    // wind up clearing the onVisibleAction value for the second call.
    boolean wasNotVisibleAlready = !isVisible();
    boolean ret = super.setVisible(visible, restart);

    if (visible) {
      if (onVisibleAction == OnVisibleAction.PLAY) {
        playAnimation();
      } else if (onVisibleAction == OnVisibleAction.RESUME) {
        resumeAnimation();
      }
    } else {
      if (animator.isRunning()) {
        pauseAnimation();
        onVisibleAction = OnVisibleAction.RESUME;
      } else if (!wasNotVisibleAlready) {
        onVisibleAction = OnVisibleAction.NONE;
      }
    }
    return ret;
  }

  /**
   * These Drawable.Callback methods proxy the calls so that this is the drawable that is
   * actually invalidated, not a child one which will not pass the view's validateDrawable check.
   */
  @Override
  public void invalidateDrawable(@NonNull Drawable who) {
    Callback callback = getCallback();
    if (callback == null) {
      return;
    }
    callback.invalidateDrawable(this);
  }

  @Override
  public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
    Callback callback = getCallback();
    if (callback == null) {
      return;
    }
    callback.scheduleDrawable(this, what, when);
  }

  @Override
  public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
    Callback callback = getCallback();
    if (callback == null) {
      return;
    }
    callback.unscheduleDrawable(this, what);
  }

  /**
   * Hardware accelerated render path.
   */
  private void drawDirectlyToCanvas(Canvas canvas) {
    CompositionLayer compositionLayer = this.compositionLayer;
    LottieComposition composition = this.composition;
    if (compositionLayer == null || composition == null) {
      return;
    }

    long start = System.nanoTime();
    renderingMatrix.reset();
    Rect bounds = getBounds();
    if (!bounds.isEmpty()) {
      // In fitXY mode, the scale doesn't take effect.
      float scaleX = bounds.width() / (float) composition.getBounds().width();
      float scaleY = bounds.height() / (float) composition.getBounds().height();

      renderingMatrix.preScale(scaleX, scaleY);
      renderingMatrix.preTranslate(bounds.left, bounds.top);
    }
    compositionLayer.draw(canvas, renderingMatrix, alpha);
    long end = System.nanoTime();
    L.drawTimeNs.getAndAdd(end - start);
  }

  /**
   * Software accelerated render path.
   * <p>
   * This draws the animation to an internally managed bitmap and then draws the bitmap to the original canvas.
   *
   * @see LottieAnimationView#setRenderMode(RenderMode)
   */
  private void renderAndDrawAsBitmap(Canvas originalCanvas, CompositionLayer compositionLayer) {
    if (composition == null || compositionLayer == null) {
      return;
    }
    ensureSoftwareRenderingObjectsInitialized();

    //noinspection deprecation
    originalCanvas.getMatrix(softwareRenderingOriginalCanvasMatrix);

    // Get the canvas clip bounds and map it to the coordinate space of canvas with it's current transform.
    originalCanvas.getClipBounds(canvasClipBounds);
    convertRect(canvasClipBounds, canvasClipBoundsRectF);
    softwareRenderingOriginalCanvasMatrix.mapRect(canvasClipBoundsRectF);
    convertRect(canvasClipBoundsRectF, canvasClipBounds);

    if (clipToCompositionBounds) {
      // Start with the intrinsic bounds. This will later be unioned with the clip bounds to find the
      // smallest possible render area.
      softwareRenderingTransformedBounds.set(0f, 0f, getIntrinsicWidth(), getIntrinsicHeight());
    } else {
      // Calculate the full bounds of the animation.
      compositionLayer.getBounds(softwareRenderingTransformedBounds, null, false);
    }
    // Transform the animation bounds to the bounds that they will render to on the canvas.
    softwareRenderingOriginalCanvasMatrix.mapRect(softwareRenderingTransformedBounds);

    // The bounds are usually intrinsicWidth x intrinsicHeight. If they are different, an external source is scaling this drawable.
    // This is how ImageView.ScaleType.FIT_XY works.
    Rect bounds = getBounds();
    float scaleX = bounds.width() / (float) getIntrinsicWidth();
    float scaleY = bounds.height() / (float) getIntrinsicHeight();
    scaleRect(softwareRenderingTransformedBounds, scaleX, scaleY);

    if (!ignoreCanvasClipBounds()) {
      softwareRenderingTransformedBounds.intersect(canvasClipBounds.left, canvasClipBounds.top, canvasClipBounds.right, canvasClipBounds.bottom);
    }

    int renderWidth = (int) Math.ceil(softwareRenderingTransformedBounds.width());
    int renderHeight = (int) Math.ceil(softwareRenderingTransformedBounds.height());

    if (renderWidth == 0 || renderHeight == 0) {
      return;
    }

    ensureSoftwareRenderingBitmap(renderWidth, renderHeight);

    if (isDirty) {
      renderingMatrix.set(softwareRenderingOriginalCanvasMatrix);
      renderingMatrix.preScale(scaleX, scaleY);
      // We want to render the smallest bitmap possible. If the animation doesn't start at the top left, we translate the canvas and shrink the
      // bitmap to avoid allocating and copying the empty space on the left and top. renderWidth and renderHeight take this into account.
      renderingMatrix.postTranslate(-softwareRenderingTransformedBounds.left, -softwareRenderingTransformedBounds.top);

      softwareRenderingBitmap.eraseColor(0);
      compositionLayer.draw(softwareRenderingCanvas, renderingMatrix, alpha);

      // Calculate the dst bounds.
      // We need to map the rendered coordinates back to the canvas's coordinates. To do so, we need to invert the transform
      // of the original canvas.
      // Take the bounds of the rendered animation and map them to the canvas's coordinates.
      // This is similar to the src rect above but the src bound may have a left and top offset.
      softwareRenderingOriginalCanvasMatrix.invert(softwareRenderingOriginalCanvasMatrixInverse);
      softwareRenderingOriginalCanvasMatrixInverse.mapRect(softwareRenderingDstBoundsRectF, softwareRenderingTransformedBounds);
      convertRect(softwareRenderingDstBoundsRectF, softwareRenderingDstBoundsRect);
    }

    softwareRenderingSrcBoundsRect.set(0, 0, renderWidth, renderHeight);
    originalCanvas.drawBitmap(softwareRenderingBitmap, softwareRenderingSrcBoundsRect, softwareRenderingDstBoundsRect, softwareRenderingPaint);
  }

  private void ensureSoftwareRenderingObjectsInitialized() {
    if (softwareRenderingCanvas != null) {
      return;
    }
    softwareRenderingCanvas = new Canvas();
    softwareRenderingTransformedBounds = new RectF();
    softwareRenderingOriginalCanvasMatrix = new Matrix();
    softwareRenderingOriginalCanvasMatrixInverse = new Matrix();
    canvasClipBounds = new Rect();
    canvasClipBoundsRectF = new RectF();
    softwareRenderingPaint = new LPaint();
    softwareRenderingSrcBoundsRect = new Rect();
    softwareRenderingDstBoundsRect = new Rect();
    softwareRenderingDstBoundsRectF = new RectF();
  }

  private void ensureSoftwareRenderingBitmap(int renderWidth, int renderHeight) {
    if (softwareRenderingBitmap == null ||
        softwareRenderingBitmap.getWidth() < renderWidth ||
        softwareRenderingBitmap.getHeight() < renderHeight) {
      // The bitmap is larger. We need to create a new one.
      softwareRenderingBitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888);
      softwareRenderingCanvas.setBitmap(softwareRenderingBitmap);
      isDirty = true;
    } else if (softwareRenderingBitmap.getWidth() > renderWidth || softwareRenderingBitmap.getHeight() > renderHeight) {
      // The bitmap is smaller. Take subset of the original.
      softwareRenderingBitmap = Bitmap.createBitmap(softwareRenderingBitmap, 0, 0, renderWidth, renderHeight);
      softwareRenderingCanvas.setBitmap(softwareRenderingBitmap);
      isDirty = true;
    }
  }

  /**
   * Convert a RectF to a Rect
   */
  private void convertRect(RectF src, Rect dst) {
    dst.set(
        (int) Math.floor(src.left),
        (int) Math.floor(src.top),
        (int) Math.ceil(src.right),
        (int) Math.ceil(src.bottom)
    );
  }

  /**
   * Convert a Rect to a RectF
   */
  private void convertRect(Rect src, RectF dst) {
    dst.set(
        src.left,
        src.top,
        src.right,
        src.bottom);
  }

  private void scaleRect(RectF rect, float scaleX, float scaleY) {
    rect.set(
        rect.left * scaleX,
        rect.top * scaleY,
        rect.right * scaleX,
        rect.bottom * scaleY
    );
  }

  /**
   * When a View's parent has clipChildren set to false, it doesn't affect the clipBound
   * of its child canvases so we should explicitly check for it and draw the full animation
   * bounds instead.
   */
  private boolean ignoreCanvasClipBounds() {
    Callback callback = getCallback();
    if (!(callback instanceof View)) {
      // If the callback isn't a view then respect the canvas's clip bounds.
      return false;
    }
    ViewParent parent = ((View) callback).getParent();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && parent instanceof ViewGroup) {
      return !((ViewGroup) parent).getClipChildren();
    }
    // Unlikely to ever happen. If the callback is a View, its parent should be a ViewGroup.
    return false;
  }
}
