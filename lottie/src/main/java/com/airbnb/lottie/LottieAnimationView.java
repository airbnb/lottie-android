package com.airbnb.lottie;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.LottieValueCallback;
import com.airbnb.lottie.value.SimpleLottieValueCallback;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
 * {@link #setAnimation(String)}
 * {@link #setAnimation(int)}
 * {@link #setAnimation(InputStream, String)}
 * {@link #setAnimationFromJson(String, String)}
 * {@link #setAnimationFromUrl(String)}
 * {@link #setComposition(LottieComposition)}
 * <p>
 * You can set a default cache strategy with {@link R.attr#lottie_cacheComposition}.
 * <p>
 * You can manually set the progress of the animation with {@link #setProgress(float)} or
 * {@link R.attr#lottie_progress}
 *
 * @see <a href="http://airbnb.io/lottie">Full Documentation</a>
 */
@SuppressWarnings({"WeakerAccess", "unused"}) public class LottieAnimationView extends AppCompatImageView {

  private static final String TAG = LottieAnimationView.class.getSimpleName();
  private static final LottieListener<Throwable> DEFAULT_FAILURE_LISTENER = throwable -> {
    // By default, fail silently for network errors.
    if (Utils.isNetworkException(throwable)) {
      Logger.warning("Unable to load composition.", throwable);
      return;
    }
    throw new IllegalStateException("Unable to parse composition", throwable);
  };

  private final LottieListener<LottieComposition> loadedListener = this::setComposition;

  private final LottieListener<Throwable> wrappedFailureListener = new LottieListener<Throwable>() {
    @Override
    public void onResult(Throwable result) {
      if (fallbackResource != 0) {
        setImageResource(fallbackResource);
      }
      LottieListener<Throwable> l = failureListener == null ? DEFAULT_FAILURE_LISTENER : failureListener;
      l.onResult(result);
    }
  };
  @Nullable private LottieListener<Throwable> failureListener;
  @DrawableRes private int fallbackResource = 0;

  private final LottieDrawable lottieDrawable = new LottieDrawable();
  private String animationName;
  private @RawRes int animationResId;

  /**
   * When we set a new composition, we set LottieDrawable to null then back again so that ImageView re-checks its bounds.
   * However, this causes the drawable to get unscheduled briefly. Normally, we would pause the animation but in this case, we don't want to.
   */
  private boolean ignoreUnschedule = false;

  private boolean autoPlay = false;
  private boolean cacheComposition = true;
  /**
   * Keeps track of explicit user actions taken and prevents onRestoreInstanceState from overwriting already set values.
   */
  private final Set<UserActionTaken> userActionsTaken = new HashSet<>();
  private final Set<LottieOnCompositionLoadedListener> lottieOnCompositionLoadedListeners = new HashSet<>();

  @Nullable private LottieTask<LottieComposition> compositionTask;
  /**
   * Can be null because it is created async
   */
  @Nullable private LottieComposition composition;

  public LottieAnimationView(Context context) {
    super(context);
    init(null, R.attr.lottieAnimationViewStyle);
  }

  public LottieAnimationView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, R.attr.lottieAnimationViewStyle);
  }

  public LottieAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs, defStyleAttr);
  }

  private void init(@Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.LottieAnimationView, defStyleAttr, 0);
    cacheComposition = ta.getBoolean(R.styleable.LottieAnimationView_lottie_cacheComposition, true);
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

    setFallbackResource(ta.getResourceId(R.styleable.LottieAnimationView_lottie_fallbackRes, 0));
    if (ta.getBoolean(R.styleable.LottieAnimationView_lottie_autoPlay, false)) {
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

    if (ta.hasValue(R.styleable.LottieAnimationView_lottie_speed)) {
      setSpeed(ta.getFloat(R.styleable.LottieAnimationView_lottie_speed, 1f));
    }

    if (ta.hasValue(R.styleable.LottieAnimationView_lottie_clipToCompositionBounds)) {
      setClipToCompositionBounds(ta.getBoolean(R.styleable.LottieAnimationView_lottie_clipToCompositionBounds, true));
    }

    if (ta.hasValue(R.styleable.LottieAnimationView_lottie_defaultFontFileExtension)) {
      setDefaultFontFileExtension(ta.getString(R.styleable.LottieAnimationView_lottie_defaultFontFileExtension));
    }

    setImageAssetsFolder(ta.getString(R.styleable.LottieAnimationView_lottie_imageAssetsFolder));

    boolean hasProgress = ta.hasValue(R.styleable.LottieAnimationView_lottie_progress);
    setProgressInternal(ta.getFloat(R.styleable.LottieAnimationView_lottie_progress, 0f), hasProgress);

    enableMergePathsForKitKatAndAbove(ta.getBoolean(
        R.styleable.LottieAnimationView_lottie_enableMergePathsForKitKatAndAbove, false));
    if (ta.hasValue(R.styleable.LottieAnimationView_lottie_colorFilter)) {
      int colorRes = ta.getResourceId(R.styleable.LottieAnimationView_lottie_colorFilter, -1);
      ColorStateList csl = AppCompatResources.getColorStateList(getContext(), colorRes);
      SimpleColorFilter filter = new SimpleColorFilter(csl.getDefaultColor());
      KeyPath keyPath = new KeyPath("**");
      LottieValueCallback<ColorFilter> callback = new LottieValueCallback<>(filter);
      addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback);
    }

    if (ta.hasValue(R.styleable.LottieAnimationView_lottie_renderMode)) {
      int renderModeOrdinal = ta.getInt(R.styleable.LottieAnimationView_lottie_renderMode, RenderMode.AUTOMATIC.ordinal());
      if (renderModeOrdinal >= RenderMode.values().length) {
        renderModeOrdinal = RenderMode.AUTOMATIC.ordinal();
      }
      setRenderMode(RenderMode.values()[renderModeOrdinal]);
    }

    setIgnoreDisabledSystemAnimations(
        ta.getBoolean(
            R.styleable.LottieAnimationView_lottie_ignoreDisabledSystemAnimations,
            false
        )
    );

    ta.recycle();

    lottieDrawable.setSystemAnimationsAreEnabled(Utils.getAnimationScale(getContext()) != 0f);
  }

  @Override public void setImageResource(int resId) {
    cancelLoaderTask();
    super.setImageResource(resId);
  }

  @Override public void setImageDrawable(Drawable drawable) {
    cancelLoaderTask();
    super.setImageDrawable(drawable);
  }

  @Override public void setImageBitmap(Bitmap bm) {
    cancelLoaderTask();
    super.setImageBitmap(bm);
  }

  @Override public void unscheduleDrawable(Drawable who) {
    if (!ignoreUnschedule && who == lottieDrawable && lottieDrawable.isAnimating()) {
      pauseAnimation();
    } else if (!ignoreUnschedule && who instanceof LottieDrawable && ((LottieDrawable) who).isAnimating()) {
      ((LottieDrawable) who).pauseAnimation();
    }
    super.unscheduleDrawable(who);
  }

  @Override public void invalidate() {
    super.invalidate();
    Drawable d = getDrawable();
    if (d instanceof LottieDrawable && ((LottieDrawable) d).getRenderMode() == RenderMode.SOFTWARE) {
      // This normally isn't needed. However, when using software rendering, Lottie caches rendered bitmaps
      // and updates it when the animation changes internally.
      // If you have dynamic properties with a value callback and want to update the value of the dynamic property, you need a way
      // to tell Lottie that the bitmap is dirty and it needs to be re-rendered. Normal drawables always re-draw the actual shapes
      // so this isn't an issue but for this path, we have to take the extra step of setting the dirty flag.
      lottieDrawable.invalidateSelf();
    }
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
    ss.isAnimating = lottieDrawable.isAnimatingOrWillAnimateOnVisible();
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
    if (!userActionsTaken.contains(UserActionTaken.SET_ANIMATION) && !TextUtils.isEmpty(animationName)) {
      setAnimation(animationName);
    }
    animationResId = ss.animationResId;
    if (!userActionsTaken.contains(UserActionTaken.SET_ANIMATION) && animationResId != 0) {
      setAnimation(animationResId);
    }
    if (!userActionsTaken.contains(UserActionTaken.SET_PROGRESS)) {
      setProgressInternal(ss.progress, false);
    }
    if (!userActionsTaken.contains(UserActionTaken.PLAY_OPTION) && ss.isAnimating) {
      playAnimation();
    }
    if (!userActionsTaken.contains(UserActionTaken.SET_IMAGE_ASSETS)) {
      setImageAssetsFolder(ss.imageAssetsFolder);
    }
    if (!userActionsTaken.contains(UserActionTaken.SET_REPEAT_MODE)) {
      setRepeatMode(ss.repeatMode);
    }
    if (!userActionsTaken.contains(UserActionTaken.SET_REPEAT_COUNT)) {
      setRepeatCount(ss.repeatCount);
    }
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (!isInEditMode() && autoPlay) {
      lottieDrawable.playAnimation();
    }
  }

  /**
   * Allows ignoring system animations settings, therefore allowing animations to run even if they are disabled.
   * <p>
   * Defaults to false.
   *
   * @param ignore if true animations will run even when they are disabled in the system settings.
   */
  public void setIgnoreDisabledSystemAnimations(boolean ignore) {
    lottieDrawable.setIgnoreDisabledSystemAnimations(ignore);
  }

  /**
   * Enable this to get merge path support for devices running KitKat (19) and above.
   * <p>
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
   * Sets whether or not Lottie should clip to the original animation composition bounds.
   *
   * When set to true, the parent view may need to disable clipChildren so Lottie can render outside of the LottieAnimationView bounds.
   *
   * Defaults to true.
   */
  public void setClipToCompositionBounds(boolean clipToCompositionBounds) {
    lottieDrawable.setClipToCompositionBounds(clipToCompositionBounds);
  }

  /**
   * Gets whether or not Lottie should clip to the original animation composition bounds.
   *
   * Defaults to true.
   */
  public boolean getClipToCompositionBounds() {
    return lottieDrawable.getClipToCompositionBounds();
  }

  /**
   * If set to true, all future compositions that are set will be cached so that they don't need to be parsed
   * next time they are loaded. This won't apply to compositions that have already been loaded.
   * <p>
   * Defaults to true.
   * <p>
   * {@link R.attr#lottie_cacheComposition}
   */
  public void setCacheComposition(boolean cacheComposition) {
    this.cacheComposition = cacheComposition;
  }

  /**
   * Enable this to debug slow animations by outlining masks and mattes. The performance overhead of the masks and mattes will
   * be proportional to the surface area of all of the masks/mattes combined.
   * <p>
   * DO NOT leave this enabled in production.
   */
  public void setOutlineMasksAndMattes(boolean outline) {
    lottieDrawable.setOutlineMasksAndMattes(outline);
  }

  /**
   * Sets the animation from a file in the raw directory.
   * This will load and deserialize the file asynchronously.
   */
  public void setAnimation(@RawRes final int rawRes) {
    this.animationResId = rawRes;
    animationName = null;
    setCompositionTask(fromRawRes(rawRes));
  }


  private LottieTask<LottieComposition> fromRawRes(@RawRes final int rawRes) {
    if (isInEditMode()) {
      return new LottieTask<>(() -> cacheComposition
          ? LottieCompositionFactory.fromRawResSync(getContext(), rawRes) : LottieCompositionFactory.fromRawResSync(getContext(), rawRes, null), true);
    } else {
      return cacheComposition ?
          LottieCompositionFactory.fromRawRes(getContext(), rawRes) : LottieCompositionFactory.fromRawRes(getContext(), rawRes, null);
    }
  }

  public void setAnimation(final String assetName) {
    this.animationName = assetName;
    animationResId = 0;
    setCompositionTask(fromAssets(assetName));
  }

  private LottieTask<LottieComposition> fromAssets(final String assetName) {
    if (isInEditMode()) {
      return new LottieTask<>(() -> cacheComposition ?
          LottieCompositionFactory.fromAssetSync(getContext(), assetName) : LottieCompositionFactory.fromAssetSync(getContext(), assetName, null), true);
    } else {
      return cacheComposition ?
          LottieCompositionFactory.fromAsset(getContext(), assetName) : LottieCompositionFactory.fromAsset(getContext(), assetName, null);
    }
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
    setAnimation(new ByteArrayInputStream(jsonString.getBytes()), cacheKey);
  }

  /**
   * Sets the animation from an arbitrary InputStream.
   * This will load and deserialize the file asynchronously.
   * <p>
   * This is particularly useful for animations loaded from the network. You can fetch the
   * bodymovin json from the network and pass it directly here.
   */
  public void setAnimation(InputStream stream, @Nullable String cacheKey) {
    setCompositionTask(LottieCompositionFactory.fromJsonInputStream(stream, cacheKey));
  }

  /**
   * Load a lottie animation from a url. The url can be a json file or a zip file. Use a zip file if you have images. Simply zip them together and
   * lottie
   * will unzip and link the images automatically.
   * <p>
   * Under the hood, Lottie uses Java HttpURLConnection because it doesn't require any transitive networking dependencies. It will download the file
   * to the application cache under a temporary name. If the file successfully parses to a composition, it will rename the temporary file to one that
   * can be accessed immediately for subsequent requests. If the file does not parse to a composition, the temporary file will be deleted.
   * <p>
   * You can replace the default network stack or cache handling with a global {@link LottieConfig}
   *
   * @see LottieConfig.Builder
   * @see Lottie#initialize(LottieConfig)
   */
  public void setAnimationFromUrl(String url) {
    LottieTask<LottieComposition> task = cacheComposition ?
        LottieCompositionFactory.fromUrl(getContext(), url) : LottieCompositionFactory.fromUrl(getContext(), url, null);
    setCompositionTask(task);
  }

  /**
   * Load a lottie animation from a url. The url can be a json file or a zip file. Use a zip file if you have images. Simply zip them together and
   * lottie
   * will unzip and link the images automatically.
   * <p>
   * Under the hood, Lottie uses Java HttpURLConnection because it doesn't require any transitive networking dependencies. It will download the file
   * to the application cache under a temporary name. If the file successfully parses to a composition, it will rename the temporary file to one that
   * can be accessed immediately for subsequent requests. If the file does not parse to a composition, the temporary file will be deleted.
   * <p>
   * You can replace the default network stack or cache handling with a global {@link LottieConfig}
   *
   * @see LottieConfig.Builder
   * @see Lottie#initialize(LottieConfig)
   */
  public void setAnimationFromUrl(String url, @Nullable String cacheKey) {
    LottieTask<LottieComposition> task = LottieCompositionFactory.fromUrl(getContext(), url, cacheKey);
    setCompositionTask(task);
  }

  /**
   * Set a default failure listener that will be called if any of the setAnimation APIs fail for any reason.
   * This can be used to replace the default behavior.
   * <p>
   * The default behavior will log any network errors and rethrow all other exceptions.
   * <p>
   * If you are loading an animation from the network, errors may occur if your user has no internet.
   * You can use this listener to retry the download or you can have it default to an error drawable
   * with {@link #setFallbackResource(int)}.
   * <p>
   * Unless you are using {@link #setAnimationFromUrl(String)}, errors are unexpected.
   * <p>
   * Set the listener to null to revert to the default behavior.
   */
  public void setFailureListener(@Nullable LottieListener<Throwable> failureListener) {
    this.failureListener = failureListener;
  }

  /**
   * Set a drawable that will be rendered if the LottieComposition fails to load for any reason.
   * Unless you are using {@link #setAnimationFromUrl(String)}, this is an unexpected error and
   * you should handle it with {@link #setFailureListener(LottieListener)}.
   * <p>
   * If this is a network animation, you may use this to show an error to the user or
   * you can use a failure listener to retry the download.
   */
  public void setFallbackResource(@DrawableRes int fallbackResource) {
    this.fallbackResource = fallbackResource;
  }

  private void setCompositionTask(LottieTask<LottieComposition> compositionTask) {
    userActionsTaken.add(UserActionTaken.SET_ANIMATION);
    clearComposition();
    cancelLoaderTask();
    this.compositionTask = compositionTask
        .addListener(loadedListener)
        .addFailureListener(wrappedFailureListener);
  }

  private void cancelLoaderTask() {
    if (compositionTask != null) {
      compositionTask.removeListener(loadedListener);
      compositionTask.removeFailureListener(wrappedFailureListener);
    }
  }

  /**
   * Sets a composition.
   * You can set a default cache strategy if this view was inflated with xml by
   * using {@link R.attr#lottie_cacheComposition}.
   */
  public void setComposition(@NonNull LottieComposition composition) {
    if (L.DBG) {
      Log.v(TAG, "Set Composition \n" + composition);
    }
    lottieDrawable.setCallback(this);

    this.composition = composition;
    ignoreUnschedule = true;
    boolean isNewComposition = lottieDrawable.setComposition(composition);
    ignoreUnschedule = false;
    if (getDrawable() == lottieDrawable && !isNewComposition) {
      // We can avoid re-setting the drawable, and invalidating the view, since the composition
      // hasn't changed.
      return;
    } else if (!isNewComposition) {
      // The current drawable isn't lottieDrawable but the drawable already has the right composition.
      setLottieDrawable();
    }

    // This is needed to makes sure that the animation is properly played/paused for the current visibility state.
    // It is possible that the drawable had a lazy composition task to play the animation but this view subsequently
    // became invisible. Comment this out and run the espresso tests to see a failing test.
    onVisibilityChanged(this, getVisibility());

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
   * Plays the animation from the beginning. If speed is {@literal <} 0, it will start at the end
   * and play towards the beginning
   */
  @MainThread
  public void playAnimation() {
    userActionsTaken.add(UserActionTaken.PLAY_OPTION);
    lottieDrawable.playAnimation();
  }

  /**
   * Continues playing the animation from its current position. If speed {@literal <} 0, it will play backwards
   * from the current position.
   */
  @MainThread
  public void resumeAnimation() {
    userActionsTaken.add(UserActionTaken.PLAY_OPTION);
    lottieDrawable.resumeAnimation();
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
   * <p>
   * The value will be clamped to the composition bounds. For example, setting Integer.MAX_VALUE would result in the same
   * thing as composition.endFrame.
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
   * Sets the minimum frame to the start time of the specified marker.
   *
   * @throws IllegalArgumentException if the marker is not found.
   */
  public void setMinFrame(String markerName) {
    lottieDrawable.setMinFrame(markerName);
  }

  /**
   * Sets the maximum frame to the start time + duration of the specified marker.
   *
   * @throws IllegalArgumentException if the marker is not found.
   */
  public void setMaxFrame(String markerName) {
    lottieDrawable.setMaxFrame(markerName);
  }

  /**
   * Sets the minimum and maximum frame to the start time and start time + duration
   * of the specified marker.
   *
   * @throws IllegalArgumentException if the marker is not found.
   */
  public void setMinAndMaxFrame(String markerName) {
    lottieDrawable.setMinAndMaxFrame(markerName);
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
    lottieDrawable.setMinAndMaxFrame(startMarkerName, endMarkerName, playEndMarkerStartFrame);
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
   *
   * @see #setSpeed(float)
   * @see #playAnimation()
   * @see #resumeAnimation()
   */
  public void reverseAnimationSpeed() {
    lottieDrawable.reverseAnimationSpeed();
  }

  /**
   * Sets the playback speed. If speed {@literal <} 0, the animation will play backwards.
   */
  public void setSpeed(float speed) {
    lottieDrawable.setSpeed(speed);
  }

  /**
   * Returns the current playback speed. This will be {@literal <} 0 if the animation is playing backwards.
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

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public void addAnimatorPauseListener(Animator.AnimatorPauseListener listener) {
    lottieDrawable.addAnimatorPauseListener(listener);
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public void removeAnimatorPauseListener(Animator.AnimatorPauseListener listener) {
    lottieDrawable.removeAnimatorPauseListener(listener);
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
    userActionsTaken.add(UserActionTaken.SET_REPEAT_MODE);
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
    userActionsTaken.add(UserActionTaken.SET_REPEAT_COUNT);
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
   * <p>
   * If your images are located in src/main/assets/airbnb_loader/ then call
   * `setImageAssetsFolder("airbnb_loader/");`.
   * <p>
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
   * When true, dynamically set bitmaps will be drawn with the exact bounds of the original animation, regardless of the bitmap size.
   * When false, dynamically set bitmaps will be drawn at the top left of the original image but with its own bounds.
   *
   * Defaults to false.
   */
  public void setMaintainOriginalImageBounds(boolean maintainOriginalImageBounds) {
    lottieDrawable.setMaintainOriginalImageBounds(maintainOriginalImageBounds);
  }

  /**
   * When true, dynamically set bitmaps will be drawn with the exact bounds of the original animation, regardless of the bitmap size.
   * When false, dynamically set bitmaps will be drawn at the top left of the original image but with its own bounds.
   *
   * Defaults to false.
   */
  public boolean getMaintainOriginalImageBounds() {
    return lottieDrawable.getMaintainOriginalImageBounds();
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
   * <p>
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
   * By default, Lottie will look in src/assets/fonts/FONT_NAME.ttf
   * where FONT_NAME is the fFamily specified in your Lottie file.
   * If your fonts have a different extension, you can override the
   * default here.
   *
   * Alternatively, you can use {@link #setFontAssetDelegate(FontAssetDelegate)}
   * for more control.
   *
   * @see #setFontAssetDelegate(FontAssetDelegate)
   */
  public void setDefaultFontFileExtension(String extension) {
    lottieDrawable.setDefaultFontFileExtension(extension);
  }

  /**
   * Use this to manually set fonts.
   */
  public void setFontAssetDelegate(FontAssetDelegate assetDelegate) {
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
   * <p>
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
   * <p>
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

  @MainThread
  public void cancelAnimation() {
    userActionsTaken.add(UserActionTaken.PLAY_OPTION);
    lottieDrawable.cancelAnimation();
  }

  @MainThread
  public void pauseAnimation() {
    autoPlay = false;
    lottieDrawable.pauseAnimation();
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
    setProgressInternal(progress, true);
  }

  private void setProgressInternal(
      @FloatRange(from = 0f, to = 1f) float progress,
      boolean fromUser) {
    if (fromUser) {
      userActionsTaken.add(UserActionTaken.SET_PROGRESS);
    }
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

  /**
   * If you are experiencing a device specific crash that happens during drawing, you can set this to true
   * for those devices. If set to true, draw will be wrapped with a try/catch which will cause Lottie to
   * render an empty frame rather than crash your app.
   * <p>
   * Ideally, you will never need this and the vast majority of apps and animations won't. However, you may use
   * this for very specific cases if absolutely necessary.
   * <p>
   * There is no XML attr for this because it should be set programmatically and only for specific devices that
   * are known to be problematic.
   */
  public void setSafeMode(boolean safeMode) {
    lottieDrawable.setSafeMode(safeMode);
  }

  /**
   * Call this to set whether or not to render with hardware or software acceleration.
   * Lottie defaults to Automatic which will use hardware acceleration unless:
   * 1) There are dash paths and the device is pre-Pie.
   * 2) There are more than 4 masks and mattes.
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
    lottieDrawable.setRenderMode(renderMode);
  }

  /**
   * Returns the actual render mode being used. It will always be {@link RenderMode#HARDWARE} or {@link RenderMode#SOFTWARE}.
   * When the render mode is set to AUTOMATIC, the value will be derived from {@link RenderMode#useSoftwareRendering(int, boolean, int)}.
   */
  public RenderMode getRenderMode() {
    return lottieDrawable.getRenderMode();
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
   * @see #setRenderMode(RenderMode)
   */
  public void setApplyingOpacityToLayersEnabled(boolean isApplyingOpacityToLayersEnabled) {
    lottieDrawable.setApplyingOpacityToLayersEnabled(isApplyingOpacityToLayersEnabled);
  }

  /**
   * This API no longer has any effect.
   */
  @Deprecated
  public void disableExtraScaleModeInFitXY() {
    //noinspection deprecation
    lottieDrawable.disableExtraScaleModeInFitXY();
  }

  public boolean addLottieOnCompositionLoadedListener(@NonNull LottieOnCompositionLoadedListener lottieOnCompositionLoadedListener) {
    LottieComposition composition = this.composition;
    if (composition != null) {
      lottieOnCompositionLoadedListener.onCompositionLoaded(composition);
    }
    return lottieOnCompositionLoadedListeners.add(lottieOnCompositionLoadedListener);
  }

  public boolean removeLottieOnCompositionLoadedListener(@NonNull LottieOnCompositionLoadedListener lottieOnCompositionLoadedListener) {
    return lottieOnCompositionLoadedListeners.remove(lottieOnCompositionLoadedListener);
  }

  public void removeAllLottieOnCompositionLoadedListener() {
    lottieOnCompositionLoadedListeners.clear();
  }

  private void setLottieDrawable() {
    boolean wasAnimating = isAnimating();
    // Set the drawable to null first because the underlying LottieDrawable's intrinsic bounds can change
    // if the composition changes.
    setImageDrawable(null);
    setImageDrawable(lottieDrawable);
    if (wasAnimating) {
      // This is necessary because lottieDrawable will get unscheduled and canceled when the drawable is set to null.
      lottieDrawable.resumeAnimation();
    }
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

  private enum UserActionTaken {
    SET_ANIMATION,
    SET_PROGRESS,
    SET_REPEAT_MODE,
    SET_REPEAT_COUNT,
    SET_IMAGE_ASSETS,
    PLAY_OPTION,
  }
}
