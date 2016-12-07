package com.airbnb.lottie;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.airbnb.lottie.layers.LottieDrawable;
import com.airbnb.lottie.model.LottieComposition;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * This view will load, deserialize, and display an After Effects animation exported with
 * bodymovin (https://github.com/bodymovin/bodymovin).
 *
 * You may set the animation in one of two ways:
 * 1) Attrs: {@link R.styleable#LottieAnimationView_lottie_fileName}
 * 2) Programatically: {@link #setAnimation(String)} or {@link #setComposition(LottieComposition)} (JSONObject)}.
 *
 * You may manually set the progress of the animation with {@link #setProgress(float)}
 */
public class LottieAnimationView extends ImageView {
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

    /**
     * Returns a {@link LottieAnimationView} that will allow it to be used without being attached to a window.
     * Normally this isn't possible.
     */
    @VisibleForTesting
    public static LottieAnimationView forScreenshotTest(Context context) {
        LottieAnimationView view = new LottieAnimationView(context);
        view.isScreenshotTest = true;
        return view;
    }

    @Nullable private static Map<String, LottieComposition> strongRefCache;
    @Nullable private static Map<String, WeakReference<LottieComposition>> weakRefCache;

    private final LottieComposition.OnCompositionLoadedListener loadedListener = new LottieComposition.OnCompositionLoadedListener() {
        @Override
        public void onCompositionLoaded(LottieComposition composition) {
            setComposition(composition);
            compositionLoader = null;
        }
    };

    private final LottieDrawable lottieDrawable = new LottieDrawable(this);
    @FloatRange(from=0f, to=1f) private float progress;
    private String animationName;
    private boolean isScreenshotTest;
    private boolean isAnimationLoading;
    private boolean setProgressWhenCompositionSet;
    private boolean playAnimationWhenCompositionSet;

    @Nullable private LottieComposition.Cancellable compositionLoader;
    /** Can be null because it is created async */
    @Nullable private LottieComposition composition;
    private boolean hasInvalidatedThisFrame;

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
        if (fileName != null) {
            setAnimation(fileName);
        }
        if (ta.getBoolean(R.styleable.LottieAnimationView_lottie_autoPlay, false)) {
            lottieDrawable.playAnimation();
        }
        lottieDrawable.loop(ta.getBoolean(R.styleable.LottieAnimationView_lottie_loop, false));
        ta.recycle();
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.animationName = animationName;
        ss.progress = lottieDrawable.getProgress();
        ss.isAnimating = lottieDrawable.isAnimating();
        ss.isLooping = lottieDrawable.isLooping();
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
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

    @SuppressLint("MissingSuperCall")
    @Override
    protected boolean verifyDrawable(@NonNull Drawable drawable) {
        return true;
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable dr) {
        if (!hasInvalidatedThisFrame && lottieDrawable != null) {
            super.invalidateDrawable(lottieDrawable);
            hasInvalidatedThisFrame = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        hasInvalidatedThisFrame = false;
        super.onDraw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        recycleBitmaps();

        super.onDetachedFromWindow();
    }

    @VisibleForTesting
    public void recycleBitmaps() {
        lottieDrawable.recycleBitmaps();
    }

    /**
     * Sets the animation from a file in the assets directory.
     * This will load and deserialize the file asynchronously.
     *
     * Will not cache the composition once loaded.
     */
    public void setAnimation(String animationName) {
        setAnimation(animationName, CacheStrategy.None);
    }

    /**
     * Sets the animation from a file in the assets directory.
     * This will load and deserialize the file asynchronously.
     */
    @SuppressWarnings("WeakerAccess")
    public void setAnimation(final String animationName, final CacheStrategy cacheStrategy) {
        this.animationName = animationName;
        if (weakRefCache != null && weakRefCache.containsKey(animationName)) {
            WeakReference<LottieComposition> compRef = weakRefCache.get(animationName);
            if (compRef.get() != null) {
                setComposition(compRef.get());
                return;
            }
        } else if (strongRefCache != null && strongRefCache.containsKey(animationName)) {
            setComposition(strongRefCache.get(animationName));
            return;
        }
        isAnimationLoading = true;
        setProgressWhenCompositionSet = false;
        playAnimationWhenCompositionSet = false;

        this.animationName = animationName;
        cancelLoaderTask();
        compositionLoader = LottieComposition.fromAssetFileName(getContext(), animationName, new LottieComposition.OnCompositionLoadedListener() {
            @Override
            public void onCompositionLoaded(LottieComposition composition) {
                if (cacheStrategy == CacheStrategy.Strong) {
                    if (strongRefCache == null) {
                        strongRefCache = new HashMap<>();
                    }
                    strongRefCache.put(animationName, composition);
                } else if (cacheStrategy == CacheStrategy.Weak) {
                    if (weakRefCache == null) {
                        weakRefCache = new HashMap<>();
                    }
                    weakRefCache.put(animationName, new WeakReference<>(composition));
                }

                setComposition(composition);
            }
        });
    }

    /**
     * Sets the animation from a JSONObject.
     * This will load and deserialize the file asynchronously.
     */
    public void setAnimation(final JSONObject json) {
        isAnimationLoading = true;
        setProgressWhenCompositionSet = false;
        playAnimationWhenCompositionSet = false;

        cancelLoaderTask();
        compositionLoader = LottieComposition.fromJson(getResources(), json, loadedListener);
    }

    private void cancelLoaderTask() {
        if (compositionLoader != null) {
            compositionLoader.cancel();
            compositionLoader = null;
        }
    }

    public void setComposition(@NonNull LottieComposition composition) {
        if (L.DBG) {
            Log.v(TAG, "Set Composition \n" + composition);
        }

        lottieDrawable.setComposition(composition);

        isAnimationLoading = false;

        if (setProgressWhenCompositionSet) {
            setProgressWhenCompositionSet = false;
            setProgress(progress);
        } else {
            setProgress(0f);
        }

        this.composition = composition;
        setImageDrawable(lottieDrawable);

        if (playAnimationWhenCompositionSet) {
            playAnimationWhenCompositionSet = false;
            playAnimation();
        }

        requestLayout();
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
        if (isAnimationLoading) {
            playAnimationWhenCompositionSet = true;
            return;
        }
        lottieDrawable.playAnimation();
    }

    public void cancelAnimation() {
        setProgressWhenCompositionSet = false;
        playAnimationWhenCompositionSet = false;
        lottieDrawable.cancelAnimation();
    }

    public void setProgress(@FloatRange(from=0f, to=1f) float progress) {
        this.progress = progress;
        if (isAnimationLoading) {
            setProgressWhenCompositionSet = true;
            return;
        }
        lottieDrawable.setProgress(progress);
    }

    public long getDuration() {
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
