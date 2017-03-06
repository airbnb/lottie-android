package com.airbnb.lottie;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

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
public class LottieAnimationView extends AppCompatImageView {
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

  private static final Map<String, LottieComposition> strongRefCache = new HashMap<>();
  private static final Map<String, WeakReference<LottieComposition>> weakRefCache =
      new HashMap<>();

  private final OnCompositionLoadedListener loadedListener =
      new OnCompositionLoadedListener() {
        @Override
        public void onCompositionLoaded(LottieComposition composition) {
          setComposition(composition);
          compositionLoader = null;
        }
      };

  private final LottieDrawable lottieDrawable = new LottieDrawable();
  private CacheStrategy defaultCacheStrategy;
  private String animationName;

  @Nullable private Cancellable compositionLoader;
  /**
   * Can be null because it is created async
   */
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
    String fileName = ta.getString(R.styleable.LottieAnimationView_lottie_fileName);
    if (!isInEditMode() && fileName != null) {
      setAnimation(fileName);
    }
    if (ta.getBoolean(R.styleable.LottieAnimationView_lottie_autoPlay, false)) {
      lottieDrawable.playAnimation();
    }
    lottieDrawable.loop(ta.getBoolean(R.styleable.LottieAnimationView_lottie_loop, false));
    setImageAssetsFolder(ta.getString(R.styleable.LottieAnimationView_lottie_imageAssetsFolder));
    setProgress(ta.getFloat(R.styleable.LottieAnimationView_lottie_progress, 0));
    int cacheStrategy = ta.getInt(
        R.styleable.LottieAnimationView_lottie_cacheStrategy,
        CacheStrategy.None.ordinal());
    defaultCacheStrategy = CacheStrategy.values()[cacheStrategy];
    ta.recycle();
    setLayerType(LAYER_TYPE_SOFTWARE, null);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
    float systemAnimationScale = Settings.Global.getFloat(getContext().getContentResolver(),
        Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f);
      if (systemAnimationScale == 0f) {
        lottieDrawable.systemAnimationsAreDisabled();
      }
    }
  }

  @Override public void setImageResource(int resId) {
    super.setImageResource(resId);
    recycleBitmaps();
  }

  @Override public void setImageDrawable(Drawable drawable) {
    if (drawable != lottieDrawable) {
      recycleBitmaps();
    }
    super.setImageDrawable(drawable);
  }

  @Override public void invalidateDrawable(Drawable dr) {
    if (getDrawable() == lottieDrawable) {
      // We always want to invalidate the root drawable to it redraws the whole drawable.
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
    ss.progress = lottieDrawable.getProgress();
    ss.isAnimating = lottieDrawable.isAnimating();
    ss.isLooping = lottieDrawable.isLooping();
    return ss;
  }

  @Override protected void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());
    this.animationName = ss.animationName;
    if (!TextUtils.isEmpty(animationName)) {
      setAnimation(animationName);
    }
    setProgress(ss.progress);
    loop(ss.isLooping);
    if (ss.isAnimating) {
      playAnimation();
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    recycleBitmaps();
    super.onDetachedFromWindow();
  }

  @VisibleForTesting void recycleBitmaps() {
    lottieDrawable.recycleBitmaps();
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
  @SuppressWarnings("WeakerAccess")
  public void setAnimation(final String animationName, final CacheStrategy cacheStrategy) {
    this.animationName = animationName;
    if (weakRefCache.containsKey(animationName)) {
      WeakReference<LottieComposition> compRef = weakRefCache.get(animationName);
      if (compRef.get() != null) {
        setComposition(compRef.get());
        return;
      }
    } else if (strongRefCache.containsKey(animationName)) {
      setComposition(strongRefCache.get(animationName));
      return;
    }

    this.animationName = animationName;
    lottieDrawable.cancelAnimation();
    cancelLoaderTask();
    compositionLoader = LottieComposition.Factory.fromAssetFileName(getContext(), animationName,
        new OnCompositionLoadedListener() {
          @Override
          public void onCompositionLoaded(LottieComposition composition) {
            if (cacheStrategy == CacheStrategy.Strong) {
              strongRefCache.put(animationName, composition);
            } else if (cacheStrategy == CacheStrategy.Weak) {
              weakRefCache.put(animationName, new WeakReference<>(composition));
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
    if (!isNewComposition) {
      // We can avoid re-setting the drawable, and invalidating the view, since the composition
      // hasn't changed.
      return;
    }

    int screenWidth = Utils.getScreenWidth(getContext());
    int screenHeight = Utils.getScreenHeight(getContext());
    int compWidth = composition.getBounds().width();
    int compHeight = composition.getBounds().height();
    if (compWidth > screenWidth ||
        compHeight > screenHeight) {
      float xScale = screenWidth / (float) compWidth;
      float yScale = screenHeight / (float) compHeight;
      setScale(Math.min(xScale, yScale));
      Log.w(L.TAG, String.format(
          "Composition larger than the screen %dx%d vs %dx%d. Scaling down.",
          compWidth, compHeight, screenWidth, screenHeight));
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
  @SuppressWarnings("unused") public boolean hasMasks() {
    return lottieDrawable.hasMasks();
  }

  /**
   * Returns whether or not any layers in this composition has a matte layer.
   */
  @SuppressWarnings("unused") public boolean hasMatte() {
    return lottieDrawable.hasMatte();
  }

  /**
   * If you use image assets, you must explicitly specify the folder in assets/ in which they are
   * located because bodymovin uses the name filenames across all compositions (img_#).
   * Do NOT rename the images themselves.
   *
   * If your images are located in src/main/assets/airbnb_loader/ then call
   * `setImageAssetsFolder("airbnb_loader/");`.
   */
  @SuppressWarnings("WeakerAccess") public void setImageAssetsFolder(String imageAssetsFolder) {
    lottieDrawable.setImagesAssetsFolder(imageAssetsFolder);
  }

  public void addAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
    lottieDrawable.addAnimatorUpdateListener(updateListener);
  }

  @SuppressWarnings("unused")
  public void removeUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
    lottieDrawable.removeAnimatorUpdateListener(updateListener);
  }

  public void addAnimatorListener(Animator.AnimatorListener listener) {
    lottieDrawable.addAnimatorListener(listener);
  }

  @SuppressWarnings("unused")
  public void removeAnimatorListener(Animator.AnimatorListener listener) {
    lottieDrawable.removeAnimatorListener(listener);
  }

  public void loop(boolean loop) {
    lottieDrawable.loop(loop);
  }

  public boolean isAnimating() {
    return lottieDrawable.isAnimating();
  }

  public void playAnimation() {
    lottieDrawable.playAnimation();
  }

  public void resumeAnimation() {
    lottieDrawable.resumeAnimation();
  }

  @SuppressWarnings("unused") public void reverseAnimation() {
    lottieDrawable.reverseAnimation();
  }

  @SuppressWarnings("unused") public void resumeReverseAnimation() {
    lottieDrawable.resumeReverseAnimation();
  }

  @SuppressWarnings("unused") public void setSpeed(float speed) {
    lottieDrawable.setSpeed(speed);
  }

  /**
   * Use this if you can't bundle images with your app. This may be useful if you download the
   * animations from the network or have the images saved to an SD Card. In that case, Lottie
   * will defer the loading of the bitmap to this delegate.
   */
  public void setImageAssetDelegate(ImageAssetDelegate assetDelegate) {
    lottieDrawable.setImageAssetDelegate(assetDelegate);
  }

  void setScale(float scale) {
    lottieDrawable.setScale(scale);
    setImageDrawable(null);
    setImageDrawable(lottieDrawable);
  }

  public void cancelAnimation() {
    lottieDrawable.cancelAnimation();
  }

  public void pauseAnimation() {
    float progress = getProgress();
    lottieDrawable.cancelAnimation();
    setProgress(progress);
  }

  public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    lottieDrawable.setProgress(progress);
  }

  @FloatRange(from = 0.0f, to = 1.0f)
  public float getProgress() {
    return lottieDrawable.getProgress();
  }

  @SuppressWarnings("unused") public long getDuration() {
    return composition != null ? composition.getDuration() : 0;
  }

  private static class SavedState extends BaseSavedState {
    String animationName;
    float progress;
    boolean isAnimating;
    boolean isLooping;

    SavedState(Parcelable superState) {
      super(superState);
    }

    private SavedState(Parcel in) {
      super(in);
      animationName = in.readString();
      progress = in.readFloat();
      isAnimating = in.readInt() == 1;
      isLooping = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeString(animationName);
      out.writeFloat(progress);
      out.writeInt(isAnimating ? 1 : 0);
      out.writeInt(isLooping ? 1 : 0);

    }

    public static final Parcelable.Creator<SavedState> CREATOR =
        new Parcelable.Creator<SavedState>() {
          public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
          }

          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}