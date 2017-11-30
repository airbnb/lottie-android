package com.airbnb.lottie;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;

import com.airbnb.lottie.utils.Utils;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * This view will load, deserialize, and display an After Effects animation exported with
 * bodymovin (https://github.com/bodymovin/bodymovin).
 * <p>
 * You may set the animation in one of two ways:
 * 1) Attrs: {@link R.styleable#LottieAnimationView_lottie_fileName}
 * 2) Programatically: {@link #setAnimation(String)}, {@link #setComposition(LottieComposition)},
 * or {@link #setAnimation(JSONObject)}.
 * <p>
 * You can set a default cache strategy with {@link R.attr#lottie_cacheStrategy}.
 * <p>
 * You can manually set the progress of the animation with {@link #setProgress(float)} or
 * {@link R.attr#lottie_progress}
 */
@SuppressWarnings({"unused", "WeakerAccess"}) public class LottieAnimationView extends AppCompatImageView {
  private static final String TAG = LottieAnimationView.class.getSimpleName();

  /**
   * Caching strategy for compositions that will be reused frequently.
   * Weak or Strong indicates the GC reference strength of the composition in the cache.
   */
  public enum CacheStrategy {
    None,
    Weak,
    Strong
  }

  private static final SparseArray<LottieComposition> RAW_RES_STRONG_REF_CACHE = new SparseArray<>();
  private static final SparseArray<WeakReference<LottieComposition>> RAW_RES_WEAK_REF_CACHE =
      new SparseArray<>();

  private static final Map<String, LottieComposition> ASSET_STRONG_REF_CACHE = new HashMap<>();
  private static final Map<String, WeakReference<LottieComposition>> ASSET_WEAK_REF_CACHE =
      new HashMap<>();

  private final OnCompositionLoadedListener loadedListener =
      new OnCompositionLoadedListener() {
        @Override public void onCompositionLoaded(@Nullable LottieComposition composition) {
          if (composition != null) {
            setComposition(composition);
          }
          compositionLoader = null;
        }
      };

  private final LottieDrawable lottieDrawable = new LottieDrawable();
  private CacheStrategy defaultCacheStrategy;
  private String animationName;
  private @RawRes int animationResId;
  private boolean wasAnimatingWhenDetached = false;
  private boolean autoPlay = false;
  private boolean useHardwareLayer = false;

  @Nullable private Cancellable compositionLoader;
  /** Can be null because it is created async */
  @Nullable private LottieComposition composition;

  public LottieAnimationView(Context context) {
    super(context);
    init(null);
  }

  public LottieAnimationView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public LottieAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  private void init(@Nullable AttributeSet attrs) {
    TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.LottieAnimationView);
    int cacheStrategy = ta.getInt(
        R.styleable.LottieAnimationView_lottie_cacheStrategy,
        CacheStrategy.Weak.ordinal());
    defaultCacheStrategy = CacheStrategy.values()[cacheStrategy];
    if (!isInEditMode()) {
      boolean hasRawRes = ta.hasValue(R.styleable.LottieAnimationView_lottie_rawRes);
      boolean hasFileName = ta.hasValue(R.styleable.LottieAnimationView_lottie_fileName);
      if (hasRawRes && hasFileName) {
        throw new IllegalArgumentException("lottie_rawRes and lottie_fileName cannot be used at " +
            "the same time. Please use use only one at once.");
      } else if (hasRawRes) {
        int rawResId = ta.getResourceId(R.styleable.LottieAnimationView_lottie_rawRes, 0);
        if (rawResId != 0) {
          setAnimation(rawResId);
        }
      } else if (hasFileName) {
        String fileName = ta.getString(R.styleable.LottieAnimationView_lottie_fileName);
        if (fileName != null) {
          setAnimation(fileName);
        }
      }
    }
    if (ta.getBoolean(R.styleable.LottieAnimationView_lottie_autoPlay, false)) {
      lottieDrawable.playAnimation();
      autoPlay = true;
    }
    lottieDrawable.loop(ta.getBoolean(R.styleable.LottieAnimationView_lottie_loop, false));
    setLoopMode(ta.getInt(R.styleable.LottieAnimationView_lottie_loopMode,
        LottieDrawable.NONE));

    if (ta.hasValue(R.styleable.LottieAnimationView_lottie_loopCount)) {
      setLoopCount(ta.getInt(R.styleable.LottieAnimationView_lottie_loopCount,
          LottieDrawable.INFINITE));
    }

    setImageAssetsFolder(ta.getString(R.styleable.LottieAnimationView_lottie_imageAssetsFolder));
    setProgress(ta.getFloat(R.styleable.LottieAnimationView_lottie_progress, 0));
    enableMergePathsForKitKatAndAbove(ta.getBoolean(
        R.styleable.LottieAnimationView_lottie_enableMergePathsForKitKatAndAbove, false));
    if (ta.hasValue(R.styleable.LottieAnimationView_lottie_colorFilter)) {
      addColorFilter(new SimpleColorFilter(ta.getColor(
          R.styleable.LottieAnimationView_lottie_colorFilter, Color.TRANSPARENT)));
    }
    if (ta.hasValue(R.styleable.LottieAnimationView_lottie_scale)) {
      lottieDrawable.setScale(ta.getFloat(R.styleable.LottieAnimationView_lottie_scale, 1f));
    }

    ta.recycle();

    if (Utils.getAnimationScale(getContext()) == 0f) {
      lottieDrawable.systemAnimationsAreDisabled();
    }

    enableOrDisableHardwareLayer();
  }

  @Override public void setImageResource(int resId) {
    recycleBitmaps();
    cancelLoaderTask();
    super.setImageResource(resId);
  }

  @Override public void setImageDrawable(Drawable drawable) {
    if (drawable != lottieDrawable) {
      recycleBitmaps();
    }
    cancelLoaderTask();
    super.setImageDrawable(drawable);
  }

  @Override public void setImageBitmap(Bitmap bm) {
    recycleBitmaps();
    cancelLoaderTask();
    super.setImageBitmap(bm);
  }

  /**
   * Add a color filter to specific content on a specific layer.
   * @param layerName name of the layer where the supplied content name lives
   * @param contentName name of the specific content that the color filter is to be applied
   * @param colorFilter the color filter, null to clear the color filter
   */
  public void addColorFilterToContent(
      String layerName, String contentName, @Nullable ColorFilter colorFilter) {
    lottieDrawable.addColorFilterToContent(layerName, contentName, colorFilter);
  }

  /**
   * Add a color filter to a whole layer
   * @param layerName name of the layer that the color filter is to be applied
   * @param colorFilter the color filter, null to clear the color filter
   */
  public void addColorFilterToLayer(
      String layerName, @Nullable ColorFilter colorFilter) {
    lottieDrawable.addColorFilterToLayer(layerName, colorFilter);
  }

  /**
   * Add a color filter to all layers
   * @param colorFilter the color filter, null to clear all color filters
   */
  public void addColorFilter(@Nullable ColorFilter colorFilter) {
    lottieDrawable.addColorFilter(colorFilter);
  }

  /**
   * Clear all color filters on all layers and all content in the layers
   */
  public void clearColorFilters() {
    lottieDrawable.clearColorFilters();
  }

  @Override public void invalidateDrawable(@NonNull Drawable dr) {
    if (getDrawable() == lottieDrawable) {
      // We always want to invalidate the root drawable so it redraws the whole drawable.
      // Eventually it would be great to be able to invalidate just the changed region.
      super.invalidateDrawable(lottieDrawable);
    } else {
      // Otherwise work as regular ImageView
      super.invalidateDrawable(dr);
    }
  }

  @Override protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState ss = new SavedState(superState);
    ss.animationName = animationName;
    ss.animationResId = animationResId;
    ss.progress = lottieDrawable.getProgress();
    ss.isAnimating = lottieDrawable.isAnimating();
    ss.imageAssetsFolder = lottieDrawable.getImageAssetsFolder();
    ss.loopMode = lottieDrawable.getLoopMode();
    ss.loopCount = lottieDrawable.getLoopCount();
    return ss;
  }

  @Override protected void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());
    animationName = ss.animationName;
    if (!TextUtils.isEmpty(animationName)) {
      setAnimation(animationName);
    }
    animationResId = ss.animationResId;
    if (animationResId != 0) {
      setAnimation(animationResId);
    }
    setProgress(ss.progress);
    if (ss.isAnimating) {
      playAnimation();
    }
    lottieDrawable.setImagesAssetsFolder(ss.imageAssetsFolder);
    setLoopMode(ss.loopMode);
    setLoopCount(ss.loopCount);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (autoPlay && wasAnimatingWhenDetached) {
      playAnimation();
    }
  }

  @Override protected void onDetachedFromWindow() {
    if (isAnimating()) {
      cancelAnimation();
      wasAnimatingWhenDetached = true;
    }
    recycleBitmaps();
    super.onDetachedFromWindow();
  }

  @VisibleForTesting void recycleBitmaps() {
    // AppCompatImageView constructor will set the image when set from xml
    // before LottieDrawable has been initialized
    if (lottieDrawable != null) {
      lottieDrawable.recycleBitmaps();
    }
  }

  /**
   * Enable this to get merge path support for devices running KitKat (19) and above.
   *
   * Merge paths currently don't work if the the operand shape is entirely contained within the
   * first shape. If you need to cut out one shape from another shape, use an even-odd fill type
   * instead of using merge paths.
   */
  public void enableMergePathsForKitKatAndAbove(boolean enable) {
    lottieDrawable.enableMergePathsForKitKatAndAbove(enable);
  }

  /**
   * @see #useHardwareAcceleration(boolean)
   */
  @Deprecated
  public void useExperimentalHardwareAcceleration() {
    useHardwareAcceleration(true);
  }


  /**
   * @see #useHardwareAcceleration(boolean)
   */
  @Deprecated
  public void useExperimentalHardwareAcceleration(boolean use) {
    useHardwareAcceleration(use);
  }

  /**
   * @see #useHardwareAcceleration(boolean)
   */
  public void useHardwareAcceleration() {
    useHardwareAcceleration(true);
  }

  /**
   * Enable hardware acceleration for this view.
   * READ THIS BEFORE ENABLING HARDWARE ACCELERATION:
   * 1) Test your animation on the minimum API level you support. Some drawing features such as
   *    dashes and stroke caps have min api levels
   *    (https://developer.android.com/guide/topics/graphics/hardware-accel.html#unsupported)
   * 2) Enabling hardware acceleration is not always more performant. Check it with your specific
   *    animation only if you are having performance issues with software rendering.
   * 3) Software rendering is safer and will be consistent across devices. Manufacturers can
   *    potentially break hardware rendering with bugs in their SKIA engine. Lottie cannot do
   *    anything about that.
   */
  public void useHardwareAcceleration(boolean use) {
    useHardwareLayer = use;
    enableOrDisableHardwareLayer();
  }

  /**
   * Sets the animation from a file in the raw directory.
   * This will load and deserialize the file asynchronously.
   * <p>
   * Will not cache the composition once loaded.
   */
  public void setAnimation(@RawRes int animationResId) {
    setAnimation(animationResId, defaultCacheStrategy);
  }

  /**
   * Sets the animation from a file in the raw directory.
   * This will load and deserialize the file asynchronously.
   * <p>
   * You may also specify a cache strategy. Specifying {@link CacheStrategy#Strong} will hold a
   * strong reference to the composition once it is loaded
   * and deserialized. {@link CacheStrategy#Weak} will hold a weak reference to said composition.
   */
  public void setAnimation(@RawRes final int animationResId, final CacheStrategy cacheStrategy) {
    this.animationResId = animationResId;
    animationName = null;
    if (RAW_RES_WEAK_REF_CACHE.indexOfKey(animationResId) > 0) {
      WeakReference<LottieComposition> compRef = RAW_RES_WEAK_REF_CACHE.get(animationResId);
      LottieComposition ref = compRef.get();
      if (ref != null) {
        setComposition(ref);
        return;
      }
    } else if (RAW_RES_STRONG_REF_CACHE.indexOfKey(animationResId) > 0) {
      setComposition(RAW_RES_STRONG_REF_CACHE.get(animationResId));
      return;
    }

    lottieDrawable.cancelAnimation();
    cancelLoaderTask();
    compositionLoader = LottieComposition.Factory.fromRawFile(getContext(), animationResId,
        new OnCompositionLoadedListener() {
          @Override public void onCompositionLoaded(LottieComposition composition) {
            if (cacheStrategy == CacheStrategy.Strong) {
              RAW_RES_STRONG_REF_CACHE.put(animationResId, composition);
            } else if (cacheStrategy == CacheStrategy.Weak) {
              RAW_RES_WEAK_REF_CACHE.put(animationResId, new WeakReference<>(composition));
            }

            setComposition(composition);
          }
        });
  }

  /**
   * Sets the animation from a file in the assets directory.
   * This will load and deserialize the file asynchronously.
   * <p>
   * Will not cache the composition once loaded.
   */
  public void setAnimation(String animationName) {
    setAnimation(animationName, defaultCacheStrategy);
  }

  /**
   * Sets the animation from a file in the assets directory.
   * This will load and deserialize the file asynchronously.
   * <p>
   * You may also specify a cache strategy. Specifying {@link CacheStrategy#Strong} will hold a
   * strong reference to the composition once it is loaded
   * and deserialized. {@link CacheStrategy#Weak} will hold a weak reference to said composition.
   */
  public void setAnimation(final String animationName, final CacheStrategy cacheStrategy) {
    this.animationName = animationName;
    animationResId = 0;
    if (ASSET_WEAK_REF_CACHE.containsKey(animationName)) {
      WeakReference<LottieComposition> compRef = ASSET_WEAK_REF_CACHE.get(animationName);
      LottieComposition ref = compRef.get();
      if (ref != null) {
        setComposition(ref);
        return;
      }
    } else if (ASSET_STRONG_REF_CACHE.containsKey(animationName)) {
      setComposition(ASSET_STRONG_REF_CACHE.get(animationName));
      return;
    }

    lottieDrawable.cancelAnimation();
    cancelLoaderTask();
    compositionLoader = LottieComposition.Factory.fromAssetFileName(getContext(), animationName,
        new OnCompositionLoadedListener() {
          @Override public void onCompositionLoaded(LottieComposition composition) {
            if (cacheStrategy == CacheStrategy.Strong) {
              ASSET_STRONG_REF_CACHE.put(animationName, composition);
            } else if (cacheStrategy == CacheStrategy.Weak) {
              ASSET_WEAK_REF_CACHE.put(animationName, new WeakReference<>(composition));
            }

            setComposition(composition);
          }
        });
  }

  /**
   * Sets the animation from a JSONObject.
   * This will load and deserialize the file asynchronously.
   * <p>
   * This is particularly useful for animations loaded from the network. You can fetch the
   * bodymovin json from the network and pass it directly here.
   */
  public void setAnimation(final JSONObject json) {
    cancelLoaderTask();
    compositionLoader = LottieComposition.Factory.fromJson(getResources(), json, loadedListener);
  }

  private void cancelLoaderTask() {
    if (compositionLoader != null) {
      compositionLoader.cancel();
      compositionLoader = null;
    }
  }

  /**
   * Sets a composition.
   * You can set a default cache strategy if this view was inflated with xml by
   * using {@link R.attr#lottie_cacheStrategy}.
   */
  public void setComposition(@NonNull LottieComposition composition) {
    if (L.DBG) {
      Log.v(TAG, "Set Composition \n" + composition);
    }
    lottieDrawable.setCallback(this);

    boolean isNewComposition = lottieDrawable.setComposition(composition);
    enableOrDisableHardwareLayer();
    if (!isNewComposition) {
      // We can avoid re-setting the drawable, and invalidating the view, since the composition
      // hasn't changed.
      return;
    }

    // If you set a different composition on the view, the bounds will not update unless
    // the drawable is different than the original.
    setImageDrawable(null);
    setImageDrawable(lottieDrawable);

    this.composition = composition;

    requestLayout();
  }

  /**
   * Returns whether or not any layers in this composition has masks.
   */
  public boolean hasMasks() {
    return lottieDrawable.hasMasks();
  }

  /**
   * Returns whether or not any layers in this composition has a matte layer.
   */
  public boolean hasMatte() {
    return lottieDrawable.hasMatte();
  }

  /**
   * Plays the animation from the beginning. If speed is < 0, it will start at the end
   * and play towards the beginning
   */
  public void playAnimation() {
    lottieDrawable.playAnimation();
    enableOrDisableHardwareLayer();
  }

  /**
   * Continues playing the animation from its current position. If speed < 0, it will play backwards
   * from the current position.
   */
  public void resumeAnimation() {
    lottieDrawable.resumeAnimation();
    enableOrDisableHardwareLayer();
  }

  /**
   * Sets the minimum frame that the animation will start from when playing or looping.
   */
  public void setMinFrame(int startFrame) {
    lottieDrawable.setMinFrame(startFrame);
  }

  /**
   * Sets the minimum progress that the animation will start from when playing or looping.
   */
  public void setMinProgress(float startProgress) {
    lottieDrawable.setMinProgress(startProgress);
  }

  /**
   * Sets the maximum frame that the animation will end at when playing or looping.
   */
  public void setMaxFrame(int endFrame) {
    lottieDrawable.setMaxFrame(endFrame);
  }

  /**
   * Sets the maximum progress that the animation will end at when playing or looping.
   */
  public void setMaxProgress(@FloatRange(from = 0f, to = 1f) float endProgress) {
    lottieDrawable.setMaxProgress(endProgress);
  }

  /**
   * @see #setMinFrame(int)
   * @see #setMaxFrame(int)
   */
  public void setMinAndMaxFrame(int minFrame, int maxFrame) {
    lottieDrawable.setMinAndMaxFrame(minFrame, maxFrame);
  }

  /**
   * @see #setMinProgress(float)
   * @see #setMaxProgress(float)
   */
  public void setMinAndMaxProgress(
      @FloatRange(from = 0f, to = 1f) float minProgress,
      @FloatRange(from = 0f, to = 1f) float maxProgress) {
    lottieDrawable.setMinAndMaxProgress(minProgress, maxProgress);
  }

  /**
   * Reverses the current animation speed. This does NOT play the animation.
   * @see #setSpeed(float)
   * @see #playAnimation()
   * @see #resumeAnimation()
   */
  public void reverseAnimationSpeed() {
    lottieDrawable.reverseAnimationSpeed();
  }

  /**
   * Sets the playback speed. If speed < 0, the animation will play backwards.
   */
  public void setSpeed(float speed) {
    lottieDrawable.setSpeed(speed);
  }

  /**
   * Returns the current playback speed. This will be < 0 if the animation is playing backwards.
   */
  public float getSpeed() {
    return lottieDrawable.getSpeed();
  }

  public void addAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
    lottieDrawable.addAnimatorUpdateListener(updateListener);
  }

  public void removeUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
    lottieDrawable.removeAnimatorUpdateListener(updateListener);
  }

  public void addAnimatorListener(Animator.AnimatorListener listener) {
    lottieDrawable.addAnimatorListener(listener);
  }

  public void removeAnimatorListener(Animator.AnimatorListener listener) {
    lottieDrawable.removeAnimatorListener(listener);
  }

  /**
   * @see #setLoopMode(int)
   */
  @Deprecated
  public void loop(boolean loop) {
    lottieDrawable.setLoopMode(loop ? LottieDrawable.RESTART : LottieDrawable.NONE);
  }

  /**
   * Defines what this animation should do when it reaches the end. This
   * setting is applied only when loop is enabled
   * Defaults to {@link LottieDrawable#NONE}.
   *
   * @param loopMode {@link LottieDrawable#NONE}, {@link LottieDrawable#RESTART} or
   * {@link LottieDrawable#REVERSE}
   */
  public void setLoopMode(@LottieDrawable.LoopMode int loopMode) {
    lottieDrawable.setLoopMode(loopMode);
  }

  /**
   * Defines what this animation should do when it reaches the end.
   *
   * @return either one of {@link LottieDrawable#NONE}, {@link LottieDrawable#RESTART} or
   * {@link LottieDrawable#REVERSE}
   */
  @LottieDrawable.LoopMode
  public int getLoopMode() {
    return lottieDrawable.getLoopMode();
  }

  /**
   * Sets how many times the animation should be looped. If the loop
   * count is 0, the animation is never looped. If the loop count is
   * greater than 0 or {@link LottieDrawable#INFINITE}, the loop mode will be taken
   * into account. The loop count is 0 by default.
   *
   * @param count the number of times the animation should be looped
   */
  public void setLoopCount(int count) {
    lottieDrawable.setLoopCount(count);
  }

  /**
   * Defines how many times the animation should loop. The default value
   * is 0.
   *
   * @return the number of times the animation should loop, or {@link LottieDrawable#INFINITE}
   */
  public int getLoopCount() {
    return lottieDrawable.getLoopCount();
  }

  public boolean isAnimating() {
    return lottieDrawable.isAnimating();
  }

  /**
   * If you use image assets, you must explicitly specify the folder in assets/ in which they are
   * located because bodymovin uses the name filenames across all compositions (img_#).
   * Do NOT rename the images themselves.
   *
   * If your images are located in src/main/assets/airbnb_loader/ then call
   * `setImageAssetsFolder("airbnb_loader/");`.
   */
  public void setImageAssetsFolder(String imageAssetsFolder) {
    lottieDrawable.setImagesAssetsFolder(imageAssetsFolder);
  }

  @Nullable
  public String getImageAssetsFolder() {
    return lottieDrawable.getImageAssetsFolder();
  }

  /**
   * Allows you to modify or clear a bitmap that was loaded for an image either automatically
   * through {@link #setImageAssetsFolder(String)} or with an {@link ImageAssetDelegate}.
   *
   * @return the previous Bitmap or null.
   */
  @Nullable
  public Bitmap updateBitmap(String id, @Nullable Bitmap bitmap) {
    return lottieDrawable.updateBitmap(id, bitmap);
  }

  /**
   * Use this if you can't bundle images with your app. This may be useful if you download the
   * animations from the network or have the images saved to an SD Card. In that case, Lottie
   * will defer the loading of the bitmap to this delegate.
   */
  public void setImageAssetDelegate(ImageAssetDelegate assetDelegate) {
    lottieDrawable.setImageAssetDelegate(assetDelegate);
  }

  /**
   * Use this to manually set fonts.
   */
  public void setFontAssetDelegate(
      @SuppressWarnings("NullableProblems") FontAssetDelegate assetDelegate) {
    lottieDrawable.setFontAssetDelegate(assetDelegate);
  }

  /**
   * Set this to replace animation text with custom text at runtime
   */
  public void setTextDelegate(TextDelegate textDelegate) {
    lottieDrawable.setTextDelegate(textDelegate);
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
    lottieDrawable.setScale(scale);
    if (getDrawable() == lottieDrawable) {
      setImageDrawable(null);
      setImageDrawable(lottieDrawable);
    }
  }

  public float getScale() {
    return lottieDrawable.getScale();
  }

  public void cancelAnimation() {
    lottieDrawable.cancelAnimation();
    enableOrDisableHardwareLayer();
  }

  public void pauseAnimation() {
    lottieDrawable.pauseAnimation();
    enableOrDisableHardwareLayer();
  }

  /**
   * Sets the progress to the specified frame.
   * If the composition isn't set yet, the progress will be set to the frame when
   * it is.
   */
  public void setFrame(int frame) {
    lottieDrawable.setFrame(frame);
  }

  /**
   * Get the currently rendered frame.
   */
  public int getFrame() {
    return lottieDrawable.getFrame();
  }

  public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    lottieDrawable.setProgress(progress);
  }

  @FloatRange(from = 0.0f, to = 1.0f) public float getProgress() {
    return lottieDrawable.getProgress();
  }

  public long getDuration() {
    return composition != null ? composition.getDuration() : 0;
  }

  public void setPerformanceTrackingEnabled(boolean enabled) {
    lottieDrawable.setPerformanceTrackingEnabled(enabled);
  }

  @Nullable
  public PerformanceTracker getPerformanceTracker() {
    return lottieDrawable.getPerformanceTracker();
  }

  private void enableOrDisableHardwareLayer() {
    boolean useHardwareLayer = this.useHardwareLayer && lottieDrawable.isAnimating();
    setLayerType(useHardwareLayer ? LAYER_TYPE_HARDWARE : LAYER_TYPE_SOFTWARE, null);
  }

  private static class SavedState extends BaseSavedState {
    String animationName;
    int animationResId;
    float progress;
    boolean isAnimating;
    String imageAssetsFolder;
    int loopMode;
    int loopCount;

    SavedState(Parcelable superState) {
      super(superState);
    }

    private SavedState(Parcel in) {
      super(in);
      animationName = in.readString();
      progress = in.readFloat();
      isAnimating = in.readInt() == 1;
      imageAssetsFolder = in.readString();
      loopMode = in.readInt();
      loopCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeString(animationName);
      out.writeFloat(progress);
      out.writeInt(isAnimating ? 1 : 0);
      out.writeString(imageAssetsFolder);
      out.writeInt(loopMode);
      out.writeInt(loopCount);
    }

    public static final Parcelable.Creator<SavedState> CREATOR =
        new Parcelable.Creator<SavedState>() {
          @Override
          public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
          }

          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}
