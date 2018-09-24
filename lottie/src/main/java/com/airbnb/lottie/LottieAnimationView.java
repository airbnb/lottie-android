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
import androidx.annotation.FloatRange;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.JsonReader;
import android.util.Log;

import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.LottieValueCallback;
import com.airbnb.lottie.value.SimpleLottieValueCallback;

import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This view will load, deserialize, and display an After Effects animation exported with
 * bodymovin (https://github.com/bodymovin/bodymovin).
 * <p>
 * You may set the animation in one of two ways:
 * 1) Attrs: {@link R.styleable#LottieAnimationView_lottie_fileName}
 * 2) Programmatically:
 *      {@link #setAnimation(String)}
 *      {@link #setAnimation(JsonReader, String)}
 *      {@link #setAnimationFromJson(String, String)}
 *      {@link #setAnimationFromUrl(String)}
 *      {@link #setComposition(LottieComposition)}
 * <p>
 * You can set a default cache strategy with {@link R.attr#lottie_cacheStrategy}.
 * <p>
 * You can manually set the progress of the animation with {@link #setProgress(float)} or
 * {@link R.attr#lottie_progress}
 */
@SuppressWarnings({"unused", "WeakerAccess"}) public class LottieAnimationView extends AppCompatImageView {

  private static final String TAG = LottieAnimationView.class.getSimpleName();

  private final LottieListener<LottieComposition> loadedListener = new LottieListener<LottieComposition>() {
    @Override public void onResult(LottieComposition composition) {
      setComposition(composition);
    }
  };

  private final LottieListener<Throwable> failureListener = new LottieListener<Throwable>() {
    @Override public void onResult(Throwable throwable) {
      throw new IllegalStateException("Unable to parse composition", throwable);
    }
  };

  private final LottieDrawable lottieDrawable = new LottieDrawable();
  private String animationName;
  private @RawRes int animationResId;
  private boolean wasAnimatingWhenDetached = false;
  private boolean autoPlay = false;
  private boolean useHardwareLayer = false;
  private Set<LottieOnCompositionLoadedListener> lottieOnCompositionLoadedListeners = new HashSet<>();

  @Nullable private LottieTask<LottieComposition> compositionTask;
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
    if (!isInEditMode()) {
      boolean hasRawRes = ta.hasValue(R.styleable.LottieAnimationView_lottie_rawRes);
      boolean hasFileName = ta.hasValue(R.styleable.LottieAnimationView_lottie_fileName);
      boolean hasUrl = ta.hasValue(R.styleable.LottieAnimationView_lottie_url);
      if (hasRawRes && hasFileName) {
        throw new IllegalArgumentException("lottie_rawRes and lottie_fileName cannot be used at " +
            "the same time. Please use only one at once.");
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
      } else if (hasUrl) {
        String url = ta.getString(R.styleable.LottieAnimationView_lottie_url);
        if (url != null) {
          setAnimationFromUrl(url);
        }
      }
    }
    if (ta.getBoolean(R.styleable.LottieAnimationView_lottie_autoPlay, false)) {
      wasAnimatingWhenDetached = true;
      autoPlay = true;
    }

    if (ta.getBoolean(R.styleable.LottieAnimationView_lottie_loop, false)) {
      lottieDrawable.setRepeatCount(LottieDrawable.INFINITE);
    }

    if (ta.hasValue(R.styleable.LottieAnimationView_lottie_repeatMode)) {
      setRepeatMode(ta.getInt(R.styleable.LottieAnimationView_lottie_repeatMode,
          LottieDrawable.RESTART));
    }

    if (ta.hasValue(R.styleable.LottieAnimationView_lottie_repeatCount)) {
      setRepeatCount(ta.getInt(R.styleable.LottieAnimationView_lottie_repeatCount,
          LottieDrawable.INFINITE));
    }

    setImageAssetsFolder(ta.getString(R.styleable.LottieAnimationView_lottie_imageAssetsFolder));
    setProgress(ta.getFloat(R.styleable.LottieAnimationView_lottie_progress, 0));
    enableMergePathsForKitKatAndAbove(ta.getBoolean(
        R.styleable.LottieAnimationView_lottie_enableMergePathsForKitKatAndAbove, false));
    if (ta.hasValue(R.styleable.LottieAnimationView_lottie_colorFilter)) {
      SimpleColorFilter filter = new SimpleColorFilter(
          ta.getColor(R.styleable.LottieAnimationView_lottie_colorFilter, Color.TRANSPARENT));
      KeyPath keyPath = new KeyPath("**");
      LottieValueCallback<ColorFilter> callback = new LottieValueCallback<ColorFilter>(filter);
      addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback);
    }
    if (ta.hasValue(R.styleable.LottieAnimationView_lottie_scale)) {
      lottieDrawable.setScale(ta.getFloat(R.styleable.LottieAnimationView_lottie_scale, 1f));
    }

    ta.recycle();

    enableOrDisableHardwareLayer();
  }

  @Override public void setImageResource(int resId) {
    recycleBitmaps();
    cancelLoaderTask();
    super.setImageResource(resId);
  }

  @Override public void setImageDrawable(Drawable drawable) {
    setImageDrawable(drawable, true);
  }

  private void setImageDrawable(Drawable drawable, boolean recycle) {
    if (recycle && drawable != lottieDrawable) {
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
    ss.repeatMode = lottieDrawable.getRepeatMode();
    ss.repeatCount = lottieDrawable.getRepeatCount();
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
    setRepeatMode(ss.repeatMode);
    setRepeatCount(ss.repeatCount);
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
    lottieDrawable.recycleBitmaps();
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
   * Returns whether merge paths are enabled for KitKat and above.
   */
  public boolean isMergePathsEnabledForKitKatAndAbove() {
    return lottieDrawable.isMergePathsEnabledForKitKatAndAbove();
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
    if (useHardwareLayer == use) {
      return;
    }
    useHardwareLayer = use;
    enableOrDisableHardwareLayer();
  }

  public boolean getUseHardwareAcceleration() {
    return useHardwareLayer;
  }

  /**
   * Sets the animation from a file in the raw directory.
   * This will load and deserialize the file asynchronously.
   */
  public void setAnimation(@RawRes final int rawRes) {
    this.animationResId = rawRes;
    animationName = null;
    setCompositionTask(LottieCompositionFactory.fromRawRes(getContext(), rawRes));
  }

  public void setAnimation(final String assetName) {
    this.animationName = assetName;
    animationResId = 0;
    setCompositionTask(LottieCompositionFactory.fromAsset(getContext(), assetName));
  }

  /**
   * @see #setAnimationFromJson(String, String)
   */
  @Deprecated
  public void setAnimationFromJson(String jsonString) {
    setAnimationFromJson(jsonString, null);
  }

  /**
   * Sets the animation from json string. This is the ideal API to use when loading an animation
   * over the network because you can use the raw response body here and a conversion to a
   * JSONObject never has to be done.
   */
  public void setAnimationFromJson(String jsonString, @Nullable String cacheKey) {
    setAnimation(new JsonReader(new StringReader(jsonString)), cacheKey);
  }

  /**
   * Sets the animation from a JSONReader.
   * This will load and deserialize the file asynchronously.
   * <p>
   * This is particularly useful for animations loaded from the network. You can fetch the
   * bodymovin json from the network and pass it directly here.
   */
  public void setAnimation(JsonReader reader, @Nullable String cacheKey) {
    setCompositionTask(LottieCompositionFactory.fromJsonReader(reader, cacheKey));
  }

  /**
   * Load a lottie animation from a url. The url can be a json file or a zip file. Use a zip file if you have images. Simply zip them together and lottie
   * will unzip and link the images automatically.
   *
   * Under the hood, Lottie uses Java HttpURLConnection because it doesn't require any transitive networking dependencies. It will download the file
   * to the application cache under a temporary name. If the file successfully parses to a composition, it will rename the temporary file to one that
   * can be accessed immediately for subsequent requests. If the file does not parse to a composition, the temporary file will be deleted.
   */
  public void setAnimationFromUrl(String url) {
    setCompositionTask(LottieCompositionFactory.fromUrl(getContext(), url));
  }

  private void setCompositionTask(LottieTask<LottieComposition> compositionTask) {
    clearComposition();
    cancelLoaderTask();
    this.compositionTask = compositionTask
            .addListener(loadedListener)
            .addFailureListener(failureListener);
  }

  private void cancelLoaderTask() {
    if (compositionTask != null) {
      compositionTask.removeListener(loadedListener);
      compositionTask.removeFailureListener(failureListener);
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

    this.composition = composition;
    boolean isNewComposition = lottieDrawable.setComposition(composition);
    enableOrDisableHardwareLayer();
    if (getDrawable() == lottieDrawable && !isNewComposition) {
      // We can avoid re-setting the drawable, and invalidating the view, since the composition
      // hasn't changed.
      return;
    }

    // If you set a different composition on the view, the bounds will not update unless
    // the drawable is different than the original.
    setImageDrawable(null);
    setImageDrawable(lottieDrawable);

    requestLayout();

    for (LottieOnCompositionLoadedListener lottieOnCompositionLoadedListener : lottieOnCompositionLoadedListeners) {
        lottieOnCompositionLoadedListener.onCompositionLoaded(composition);
    }

  }

  @Nullable public LottieComposition getComposition() {
    return composition;
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
  @MainThread
  public void playAnimation() {
    lottieDrawable.playAnimation();
    enableOrDisableHardwareLayer();
  }

  /**
   * Continues playing the animation from its current position. If speed < 0, it will play backwards
   * from the current position.
   */
  @MainThread
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
   * Returns the minimum frame set by {@link #setMinFrame(int)} or {@link #setMinProgress(float)}
   */
  public float getMinFrame() {
    return lottieDrawable.getMinFrame();
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
   * Returns the maximum frame set by {@link #setMaxFrame(int)} or {@link #setMaxProgress(float)}
   */
  public float getMaxFrame() {
    return lottieDrawable.getMaxFrame();
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

  public void removeAllUpdateListeners() {
    lottieDrawable.removeAllUpdateListeners();
  }

  public void addAnimatorListener(Animator.AnimatorListener listener) {
    lottieDrawable.addAnimatorListener(listener);
  }

  public void removeAnimatorListener(Animator.AnimatorListener listener) {
    lottieDrawable.removeAnimatorListener(listener);
  }

  public void removeAllAnimatorListeners() {
    lottieDrawable.removeAllAnimatorListeners();
  }

  /**
   * @see #setRepeatCount(int)
   */
  @Deprecated
  public void loop(boolean loop) {
    lottieDrawable.setRepeatCount(loop ? ValueAnimator.INFINITE : 0);
  }

  /**
   * Defines what this animation should do when it reaches the end. This
   * setting is applied only when the repeat count is either greater than
   * 0 or {@link LottieDrawable#INFINITE}. Defaults to {@link LottieDrawable#RESTART}.
   *
   * @param mode {@link LottieDrawable#RESTART} or {@link LottieDrawable#REVERSE}
   */
  public void setRepeatMode(@LottieDrawable.RepeatMode int mode) {
    lottieDrawable.setRepeatMode(mode);
  }

  /**
   * Defines what this animation should do when it reaches the end.
   *
   * @return either one of {@link LottieDrawable#REVERSE} or {@link LottieDrawable#RESTART}
   */
  @LottieDrawable.RepeatMode
  public int getRepeatMode() {
    return lottieDrawable.getRepeatMode();
  }

  /**
   * Sets how many times the animation should be repeated. If the repeat
   * count is 0, the animation is never repeated. If the repeat count is
   * greater than 0 or {@link LottieDrawable#INFINITE}, the repeat mode will be taken
   * into account. The repeat count is 0 by default.
   *
   * @param count the number of times the animation should be repeated
   */
  public void setRepeatCount(int count) {
    lottieDrawable.setRepeatCount(count);
  }

  /**
   * Defines how many times the animation should repeat. The default value
   * is 0.
   *
   * @return the number of times the animation should repeat, or {@link LottieDrawable#INFINITE}
   */
  public int getRepeatCount() {
    return lottieDrawable.getRepeatCount();
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
   *
   * Be wary if you are using many images, however. Lottie is designed to work with vector shapes
   * from After Effects. If your images look like they could be represented with vector shapes,
   * see if it is possible to convert them to shape layers and re-export your animation. Check
   * the documentation at http://airbnb.io/lottie for more information about importing shapes from
   * Sketch or Illustrator to avoid this.
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
   *
   * Be wary if you are using many images, however. Lottie is designed to work with vector shapes
   * from After Effects. If your images look like they could be represented with vector shapes,
   * see if it is possible to convert them to shape layers and re-export your animation. Check
   * the documentation at http://airbnb.io/lottie for more information about importing shapes from
   * Sketch or Illustrator to avoid this.
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
   * Takes a {@link KeyPath}, potentially with wildcards or globstars and resolve it to a list of
   * zero or more actual {@link KeyPath Keypaths} that exist in the current animation.
   *
   * If you want to set value callbacks for any of these values, it is recommended to use the
   * returned {@link KeyPath} objects because they will be internally resolved to their content
   * and won't trigger a tree walk of the animation contents when applied.
   */
  public List<KeyPath> resolveKeyPath(KeyPath keyPath) {
    return lottieDrawable.resolveKeyPath(keyPath);
  }

  /**
   * Add a property callback for the specified {@link KeyPath}. This {@link KeyPath} can resolve
   * to multiple contents. In that case, the callback's value will apply to all of them.
   *
   * Internally, this will check if the {@link KeyPath} has already been resolved with
   * {@link #resolveKeyPath(KeyPath)} and will resolve it if it hasn't.
   */
  public <T> void addValueCallback(KeyPath keyPath, T property, LottieValueCallback<T> callback) {
    lottieDrawable.addValueCallback(keyPath, property, callback);
  }

  /**
   * Overload of {@link #addValueCallback(KeyPath, Object, LottieValueCallback)} that takes an interface. This allows you to use a single abstract
   * method code block in Kotlin such as:
   * animationView.addValueCallback(yourKeyPath, LottieProperty.COLOR) { yourColor }
   */
  public <T> void addValueCallback(KeyPath keyPath, T property,
      final SimpleLottieValueCallback<T> callback) {
    lottieDrawable.addValueCallback(keyPath, property, new LottieValueCallback<T>() {
      @Override public T getValue(LottieFrameInfo<T> frameInfo) {
        return callback.getValue(frameInfo);
      }
    });
  }

  /**
   * Set the scale on the current composition. The only cost of this function is re-rendering the
   * current frame so you may call it frequent to scale something up or down.
   *
   * The smaller the animation is, the better the performance will be. You may find that scaling an
   * animation down then rendering it in a larger ImageView and letting ImageView scale it back up
   * with a scaleType such as centerInside will yield better performance with little perceivable
   * quality loss.
   *
   * You can also use a fixed view width/height in conjunction with the normal ImageView
   * scaleTypes centerCrop and centerInside.
   */
  public void setScale(float scale) {
    lottieDrawable.setScale(scale);
    if (getDrawable() == lottieDrawable) {
      setImageDrawable(null, false);
      setImageDrawable(lottieDrawable, false);
    }
  }

  public float getScale() {
    return lottieDrawable.getScale();
  }

  @MainThread
  public void cancelAnimation() {
    lottieDrawable.cancelAnimation();
    enableOrDisableHardwareLayer();
  }

  @MainThread
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
    return composition != null ? (long) composition.getDuration() : 0;
  }

  public void setPerformanceTrackingEnabled(boolean enabled) {
    lottieDrawable.setPerformanceTrackingEnabled(enabled);
  }

  @Nullable
  public PerformanceTracker getPerformanceTracker() {
    return lottieDrawable.getPerformanceTracker();
  }

  private void clearComposition() {
    composition = null;
    lottieDrawable.clearComposition();
  }

  private void enableOrDisableHardwareLayer() {
    boolean useHardwareLayer = this.useHardwareLayer && lottieDrawable.isAnimating();
    setLayerType(useHardwareLayer ? LAYER_TYPE_HARDWARE : LAYER_TYPE_SOFTWARE, null);
  }

  public boolean addLottieOnCompositionLoadedListener(@NonNull LottieOnCompositionLoadedListener lottieOnCompositionLoadedListener) {
    return lottieOnCompositionLoadedListeners.add(lottieOnCompositionLoadedListener);
  }

  public boolean removeLottieOnCompositionLoadedListener(@NonNull LottieOnCompositionLoadedListener lottieOnCompositionLoadedListener) {
    return lottieOnCompositionLoadedListeners.remove(lottieOnCompositionLoadedListener);
  }

  public void removeAllLottieOnCompositionLoadedListener() {
    lottieOnCompositionLoadedListeners.clear();
  }

  private static class SavedState extends BaseSavedState {
    String animationName;
    int animationResId;
    float progress;
    boolean isAnimating;
    String imageAssetsFolder;
    int repeatMode;
    int repeatCount;

    SavedState(Parcelable superState) {
      super(superState);
    }

    private SavedState(Parcel in) {
      super(in);
      animationName = in.readString();
      progress = in.readFloat();
      isAnimating = in.readInt() == 1;
      imageAssetsFolder = in.readString();
      repeatMode = in.readInt();
      repeatCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeString(animationName);
      out.writeFloat(progress);
      out.writeInt(isAnimating ? 1 : 0);
      out.writeString(imageAssetsFolder);
      out.writeInt(repeatMode);
      out.writeInt(repeatCount);
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
