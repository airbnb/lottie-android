package com.airbnb.lottie;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.manager.FontAssetManager;
import com.airbnb.lottie.manager.ImageAssetManager;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.Marker;
import com.airbnb.lottie.model.layer.CompositionLayer;
import com.airbnb.lottie.parser.LayerParser;
import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.LottieValueAnimator;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.LottieValueCallback;
import com.airbnb.lottie.value.SimpleLottieValueCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This can be used to show an lottie animation in any place that would normally take a drawable.
 *
 * @see <a href="http://airbnb.io/lottie">Full Documentation</a>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class LottieDrawable extends Drawable implements Drawable.Callback, Animatable {
  private static final String TAG = LottieDrawable.class.getSimpleName();

  private interface LazyCompositionTask {
    void run(LottieComposition composition);
  }

  private final Matrix matrix = new Matrix();
  private LottieComposition composition;
  private final LottieValueAnimator animator = new LottieValueAnimator();
  private float scale = 1f;
  private boolean systemAnimationsEnabled = true;
  private boolean safeMode = false;

  private final Set<ColorFilterData> colorFilterData = new HashSet<>();
  private final ArrayList<LazyCompositionTask> lazyCompositionTasks = new ArrayList<>();
  private final ValueAnimator.AnimatorUpdateListener  progressUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
      if (compositionLayer != null) {
        compositionLayer.setProgress(animator.getAnimatedValueAbsolute());
      }
    }
  };
  @Nullable
  private ImageView.ScaleType scaleType;
  @Nullable
  private ImageAssetManager imageAssetManager;
  @Nullable
  private String imageAssetsFolder;
  @Nullable
  private ImageAssetDelegate imageAssetDelegate;
  @Nullable
  private FontAssetManager fontAssetManager;
  @Nullable
  FontAssetDelegate fontAssetDelegate;
  @Nullable
  TextDelegate textDelegate;
  private boolean enableMergePaths;
  @Nullable
  private CompositionLayer compositionLayer;
  private int alpha = 255;
  private boolean performanceTrackingEnabled;
  private boolean isApplyingOpacityToLayersEnabled;
  private boolean isExtraScaleEnabled = true;
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
   * If you use image assets, you must explicitly specify the folder in assets/ in which they are
   * located because bodymovin uses the name filenames across all compositions (img_#).
   * Do NOT rename the images themselves.
   * <p>
   * If your images are located in src/main/assets/airbnb_loader/ then call
   * `setImageAssetsFolder("airbnb_loader/");`.
   * <p>
   * <p>
   * If you use LottieDrawable directly, you MUST call {@link #recycleBitmaps()} when you
   * are done. Calling {@link #recycleBitmaps()} doesn't have to be final and {@link LottieDrawable}
   * will recreate the bitmaps if needed but they will leak if you don't recycle them.
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
   * Create a composition with {@link LottieCompositionFactory}
   *
   * @return True if the composition is different from the previously set composition, false otherwise.
   */
  public boolean setComposition(LottieComposition composition) {
    if (this.composition == composition) {
      return false;
    }

    isDirty = false;
    clearComposition();
    this.composition = composition;
    buildCompositionLayer();
    animator.setComposition(composition);
    setProgress(animator.getAnimatedFraction());
    setScale(scale);
    updateBounds();

    // We copy the tasks to a new ArrayList so that if this method is called from multiple threads,
    // then there won't be two iterators iterating and removing at the same time.
    Iterator<LazyCompositionTask> it = new ArrayList<>(lazyCompositionTasks).iterator();
    while (it.hasNext()) {
      LazyCompositionTask t = it.next();
      t.run(composition);
      it.remove();
    }
    lazyCompositionTasks.clear();

    composition.setPerformanceTrackingEnabled(performanceTrackingEnabled);

    // Ensure that ImageView updates the drawable width/height so it can
    // properly calculate its drawable matrix.
    Callback callback = getCallback();
    if (callback instanceof ImageView) {
      ((ImageView) callback).setImageDrawable(null);
      ((ImageView) callback).setImageDrawable(this);
    }

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
   * Disable the extraScale mode in {@link #draw(Canvas)} function when scaleType is FitXY. It doesn't affect the rendering with other scaleTypes.
   *
   * <p>When there are 2 animation layout side by side, the default extra scale mode might leave 1 pixel not drawn between 2 animation, and
   * disabling the extraScale mode can fix this problem</p>
   *
   * <b>Attention:</b> Disable the extra scale mode can downgrade the performance and may lead to larger memory footprint. Please only disable this
   * mode when using animation with a reasonable dimension (smaller than screen size).
   *
   * @see #drawWithNewAspectRatio(Canvas)
   */
  public void disableExtraScaleModeInFitXY() {
    this.isExtraScaleEnabled = false;
  }

  public boolean isApplyingOpacityToLayersEnabled() {
    return isApplyingOpacityToLayersEnabled;
  }

  private void buildCompositionLayer() {
    compositionLayer = new CompositionLayer(
        this, LayerParser.parse(composition), composition.getLayers(), composition);
  }

  public void clearComposition() {
    if (animator.isRunning()) {
      animator.cancel();
    }
    composition = null;
    compositionLayer = null;
    imageAssetManager = null;
    animator.clearComposition();
    invalidateSelf();
  }

  /**
   * If you are experiencing a device specific crash that happens during drawing, you can set this to true
   * for those devices. If set to true, draw will be wrapped with a try/catch which will cause Lottie to
   * render an empty frame rather than crash your app.
   *
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

  @Override
  public void draw(@NonNull Canvas canvas) {
    isDirty = false;

    L.beginSection("Drawable#draw");

    if (safeMode) {
      try {
        drawInternal(canvas);
      } catch (Throwable e) {
        Logger.error("Lottie crashed in draw!", e);
      }
    } else {
      drawInternal(canvas);
    }

    L.endSection("Drawable#draw");
  }

  private void drawInternal(@NonNull Canvas canvas) {
    if (ImageView.ScaleType.FIT_XY == scaleType) {
      drawWithNewAspectRatio(canvas);
    } else {
      drawWithOriginalAspectRatio(canvas);
    }
  }

// <editor-fold desc="animator">

  @MainThread
  @Override
  public void start() {
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
   * Plays the animation from the beginning. If speed is < 0, it will start at the end
   * and play towards the beginning
   */
  @MainThread
  public void playAnimation() {
    if (compositionLayer == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          playAnimation();
        }
      });
      return;
    }

    if (systemAnimationsEnabled || getRepeatCount() == 0) {
      animator.playAnimation();
    }
    if (!systemAnimationsEnabled) {
      setFrame((int) (getSpeed() < 0 ? getMinFrame() : getMaxFrame()));
      animator.endAnimation();
    }
  }

  @MainThread
  public void endAnimation() {
    lazyCompositionTasks.clear();
    animator.endAnimation();
  }

  /**
   * Continues playing the animation from its current position. If speed < 0, it will play backwards
   * from the current position.
   */
  @MainThread
  public void resumeAnimation() {
    if (compositionLayer == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          resumeAnimation();
        }
      });
      return;
    }

    if (systemAnimationsEnabled || getRepeatCount() == 0) {
      animator.resumeAnimation();
    }
    if (!systemAnimationsEnabled) {
      setFrame((int) (getSpeed() < 0 ? getMinFrame() : getMaxFrame()));
      animator.endAnimation();
    }
  }

  /**
   * Sets the minimum frame that the animation will start from when playing or looping.
   */
  public void setMinFrame(final int minFrame) {
    if (composition == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          setMinFrame(minFrame);
        }
      });
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
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          setMinProgress(minProgress);
        }
      });
      return;
    }
    setMinFrame((int) MiscUtils.lerp(composition.getStartFrame(), composition.getEndFrame(), minProgress));
  }

  /**
   * Sets the maximum frame that the animation will end at when playing or looping.
   */
  public void setMaxFrame(final int maxFrame) {
    if (composition == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          setMaxFrame(maxFrame);
        }
      });
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
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          setMaxProgress(maxProgress);
        }
      });
      return;
    }
    setMaxFrame((int) MiscUtils.lerp(composition.getStartFrame(), composition.getEndFrame(), maxProgress));
  }

  /**
   * Sets the minimum frame to the start time of the specified marker.
   * @throws IllegalArgumentException if the marker is not found.
   */
  public void setMinFrame(final String markerName) {
    if (composition == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          setMinFrame(markerName);
        }
      });
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
   * @throws IllegalArgumentException if the marker is not found.
   */
  public void setMaxFrame(final String markerName) {
    if (composition == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          setMaxFrame(markerName);
        }
      });
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
   * @throws IllegalArgumentException if the marker is not found.
   */
  public void setMinAndMaxFrame(final String markerName) {
    if (composition == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          setMinAndMaxFrame(markerName);
        }
      });
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
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          setMinAndMaxFrame(startMarkerName, endMarkerName, playEndMarkerStartFrame);
        }
      });
      return;
    }
    Marker startMarker = composition.getMarker(startMarkerName);
    if (startMarker == null) {
      throw new IllegalArgumentException("Cannot find marker with name " + startMarkerName + ".");
    }
    int startFrame = (int) startMarker.startFrame;

    Marker endMarker = composition.getMarker(endMarkerName);
    if (endMarkerName == null) {
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
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          setMinAndMaxFrame(minFrame, maxFrame);
        }
      });
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
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          setMinAndMaxProgress(minProgress, maxProgress);
        }
      });
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
   * Sets the playback speed. If speed < 0, the animation will play backwards.
   */
  public void setSpeed(float speed) {
    animator.setSpeed(speed);
  }

  /**
   * Returns the current playback speed. This will be < 0 if the animation is playing backwards.
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

  /**
   * Sets the progress to the specified frame.
   * If the composition isn't set yet, the progress will be set to the frame when
   * it is.
   */
  public void setFrame(final int frame) {
    if (composition == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          setFrame(frame);
        }
      });
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
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          setProgress(progress);
        }
      });
      return;
    }
    L.beginSection("Drawable#setProgress");
    animator.setFrame(MiscUtils.lerp(composition.getStartFrame(), composition.getEndFrame(), progress));
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


  public boolean isLooping() {
    return animator.getRepeatCount() == ValueAnimator.INFINITE;
  }

  public boolean isAnimating() {
    // On some versions of Android, this is called from the LottieAnimationView constructor, before animator was created.
    // https://github.com/airbnb/lottie-android/issues/1430
    if (animator == null) {
      return false;
    }
    return animator.isRunning();
  }

  void setSystemAnimationsAreEnabled(Boolean areEnabled) {
    systemAnimationsEnabled = areEnabled;
  }

// </editor-fold>

  /**
   * Set the scale on the current composition. The only cost of this function is re-rendering the
   * current frame so you may call it frequent to scale something up or down.
   * <p>
   * The smaller the animation is, the better the performance will be. You may find that scaling an
   * animation down then rendering it in a larger ImageView and letting ImageView scale it back up
   * with a scaleType such as centerInside will yield better performance with little perceivable
   * quality loss.
   * <p>
   * You can also use a fixed view width/height in conjunction with the normal ImageView
   * scaleTypes centerCrop and centerInside.
   */
  public void setScale(float scale) {
    this.scale = scale;
    updateBounds();
  }

  /**
   * Use this if you can't bundle images with your app. This may be useful if you download the
   * animations from the network or have the images saved to an SD Card. In that case, Lottie
   * will defer the loading of the bitmap to this delegate.
   * <p>
   * Be wary if you are using many images, however. Lottie is designed to work with vector shapes
   * from After Effects. If your images look like they could be represented with vector shapes,
   * see if it is possible to convert them to shape layers and re-export your animation. Check
   * the documentation at http://airbnb.io/lottie for more information about importing shapes from
   * Sketch or Illustrator to avoid this.
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

  @Nullable
  public TextDelegate getTextDelegate() {
    return textDelegate;
  }

  public boolean useTextGlyphs() {
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

  public void pauseAnimation() {
    lazyCompositionTasks.clear();
    animator.pauseAnimation();
  }

  @FloatRange(from = 0f, to = 1f)
  public float getProgress() {
    return animator.getAnimatedValueAbsolute();
  }

  @Override
  public int getIntrinsicWidth() {
    return composition == null ? -1 : (int) (composition.getBounds().width() * getScale());
  }

  @Override
  public int getIntrinsicHeight() {
    return composition == null ? -1 : (int) (composition.getBounds().height() * getScale());
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
   * to multiple contents. In that case, the callbacks's value will apply to all of them.
   * <p>
   * Internally, this will check if the {@link KeyPath} has already been resolved with
   * {@link #resolveKeyPath(KeyPath)} and will resolve it if it hasn't.
   */
  public <T> void addValueCallback(
      final KeyPath keyPath, final T property, final LottieValueCallback<T> callback) {
    if (compositionLayer == null) {
      lazyCompositionTasks.add(new LazyCompositionTask() {
        @Override
        public void run(LottieComposition composition) {
          addValueCallback(keyPath, property, callback);
        }
      });
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
    addValueCallback(keyPath, property, new LottieValueCallback<T>() {
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

  @Nullable
  public Bitmap getImageAsset(String id) {
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
      imageAssetManager = null;
    }

    if (imageAssetManager == null) {
      imageAssetManager = new ImageAssetManager(getCallback(),
          imageAssetsFolder, imageAssetDelegate, composition.getImages());
    }

    return imageAssetManager;
  }

  @Nullable
  public Typeface getTypeface(String fontFamily, String style) {
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

  void setScaleType(ImageView.ScaleType scaleType) {
    this.scaleType = scaleType;
  }

  /**
   * If the composition is larger than the canvas, we have to use a different method to scale it up.
   * See the comments in {@link #draw(Canvas)} for more info.
   */
  private float getMaxScale(@NonNull Canvas canvas) {
    float maxScaleX = canvas.getWidth() / (float) composition.getBounds().width();
    float maxScaleY = canvas.getHeight() / (float) composition.getBounds().height();
    return Math.min(maxScaleX, maxScaleY);
  }

  private void drawWithNewAspectRatio(Canvas canvas) {
    if (compositionLayer == null) {
      return;
    }

    int saveCount = -1;
    Rect bounds = getBounds();
    // In fitXY mode, the scale doesn't take effect.
    float scaleX = bounds.width() / (float) composition.getBounds().width();
    float scaleY = bounds.height() / (float) composition.getBounds().height();

    if (isExtraScaleEnabled) {
      float maxScale = Math.min(scaleX, scaleY);
      float extraScale = 1f;
      if (maxScale < 1f) {
        extraScale = extraScale / maxScale;
        scaleX = scaleX / extraScale;
        scaleY = scaleY / extraScale;
      }

      if (extraScale > 1) {
        saveCount = canvas.save();
        float halfWidth = bounds.width() / 2f;
        float halfHeight = bounds.height() / 2f;
        float scaledHalfWidth = halfWidth * maxScale;
        float scaledHalfHeight = halfHeight * maxScale;

        canvas.translate(
            halfWidth - scaledHalfWidth,
            halfHeight - scaledHalfHeight);
        canvas.scale(extraScale, extraScale, scaledHalfWidth, scaledHalfHeight);
      }
    }

    matrix.reset();
    matrix.preScale(scaleX, scaleY);
    compositionLayer.draw(canvas, matrix, alpha);

    if (saveCount > 0) {
      canvas.restoreToCount(saveCount);
    }
  }

  private void drawWithOriginalAspectRatio(Canvas canvas) {
    if (compositionLayer == null) {
      return;
    }

    float scale = this.scale;
    float extraScale = 1f;
    float maxScale = getMaxScale(canvas);
    if (scale > maxScale) {
      scale = maxScale;
      extraScale = this.scale / scale;
    }

    int saveCount = -1;
    if (extraScale > 1) {
      // This is a bit tricky...
      // We can't draw on a canvas larger than ViewConfiguration.get(context).getScaledMaximumDrawingCacheSize()
      // which works out to be roughly the size of the screen because Android can't generate a
      // bitmap large enough to render to.
      // As a result, we cap the scale such that it will never be wider/taller than the screen
      // and then only render in the top left corner of the canvas. We then use extraScale
      // to scale up the rest of the scale. However, since we rendered the animation to the top
      // left corner, we need to scale up and translate the canvas to zoom in on the top left
      // corner.
      saveCount = canvas.save();
      float halfWidth = composition.getBounds().width() / 2f;
      float halfHeight = composition.getBounds().height() / 2f;
      float scaledHalfWidth = halfWidth * scale;
      float scaledHalfHeight = halfHeight * scale;

      canvas.translate(
          getScale() * halfWidth - scaledHalfWidth,
          getScale() * halfHeight - scaledHalfHeight);
      canvas.scale(extraScale, extraScale, scaledHalfWidth, scaledHalfHeight);
    }

    matrix.reset();
    matrix.preScale(scale, scale);
    compositionLayer.draw(canvas, matrix, alpha);

    if (saveCount > 0) {
      canvas.restoreToCount(saveCount);
    }
  }

  private static class ColorFilterData {

    final String layerName;
    @Nullable
    final String contentName;
    @Nullable
    final ColorFilter colorFilter;

    ColorFilterData(@Nullable String layerName, @Nullable String contentName,
                    @Nullable ColorFilter colorFilter) {
      this.layerName = layerName;
      this.contentName = contentName;
      this.colorFilter = colorFilter;
    }

    @Override
    public int hashCode() {
      int hashCode = 17;
      if (layerName != null) {
        hashCode = hashCode * 31 * layerName.hashCode();
      }

      if (contentName != null) {
        hashCode = hashCode * 31 * contentName.hashCode();
      }
      return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
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
