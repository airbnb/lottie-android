package com.airbnb.lotte;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.widget.ImageView;

import com.airbnb.lotte.layers.LotteLayer;
import com.airbnb.lotte.layers.LotteLayerView;
import com.airbnb.lotte.layers.RootLotteAnimatableLayer;
import com.airbnb.lotte.model.LotteComposition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class LotteAnimationView extends ImageView {
    private final LongSparseArray<LotteLayerView> layerMap = new LongSparseArray<>();
    private final RootLotteAnimatableLayer rootAnimatableLayer = new RootLotteAnimatableLayer(this);

    private LotteComposition composition;
    private boolean hasInvalidatedThisFrame;

    public LotteAnimationView(Context context) {
        super(context);
        init(null);
    }

    public LotteAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LotteAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.LotteAnimationView);
        String fileName = ta.getString(R.styleable.LotteAnimationView_fileName);
        if (fileName != null) {
            setAnimation(fileName);
        }
        ta.recycle();
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (rootAnimatableLayer != null && MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY && MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            setMeasuredDimension(rootAnimatableLayer.getBounds().width(), rootAnimatableLayer.getBounds().height());
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable drawable) {
        return true;
    }

    @Override
    public void invalidateDrawable(Drawable dr) {
        if (!hasInvalidatedThisFrame) {
            super.invalidateDrawable(rootAnimatableLayer);
            hasInvalidatedThisFrame = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        hasInvalidatedThisFrame = false;
        super.onDraw(canvas);
    }

    public void setAnimation(String animationName) {
        InputStream file;
        try {
            file = getContext().getAssets().open(animationName);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to find file " + animationName, e);
        }
        new AsyncTask<InputStream, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(InputStream... params) {
                //noinspection WrongThread
                return setAnimationSync(params[0]);
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                setJson(jsonObject);
            }
        }.execute(file);
    }

    public void setAnimationSync(String animationName) {
        InputStream file;
        try {
            file = getContext().getAssets().open(animationName);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to find file " + animationName, e);
        }
        setJsonSync(setAnimationSync(file));
    }

    private JSONObject setAnimationSync(InputStream file) {
        try {
            int size = file.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            file.read(buffer);
            file.close();
            String json = new String(buffer, "UTF-8");

            return new JSONObject(json);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to find file.", e);
        } catch (JSONException e) {
            throw new IllegalStateException("Unable to load JSON.", e);
        }
    }

    public void setJson(JSONObject json) {
        // TODO: cancel these if the iew gets detached.
        new AsyncTask<JSONObject, Void, LotteComposition>() {

            @Override
            protected LotteComposition doInBackground(JSONObject... params) {
                return LotteComposition.fromJson(params[0]);
            }

            @Override
            protected void onPostExecute(LotteComposition model) {
                setComposition(model);
            }
        }.execute(json);
    }

    private void setJsonSync(JSONObject json) {
        LotteComposition composition = LotteComposition.fromJson(json);
        setComposition(composition);
    }

    public void setComposition(LotteComposition composition) {
        this.composition = composition;
        rootAnimatableLayer.setCompDuration(composition.getDuration());
        rootAnimatableLayer.setBounds(0, 0, composition.getBounds().width(), composition.getBounds().height());
        buildSubviewsForComposition();
        requestLayout();
        setImageDrawable(rootAnimatableLayer);
    }

    private void buildSubviewsForComposition() {
        List<LotteLayer> reversedLayers = composition.getLayers();
        Collections.reverse(reversedLayers);

        Rect bounds = composition.getBounds();
        Bitmap mainBitmap = composition.hasMasks() ? Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888) : null;
        Bitmap maskBitmap = composition.hasMasks() ? Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ALPHA_8) : null;
        Bitmap matteBitmap = composition.hasMattes() ? Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888) : null;

        Bitmap mainBitmapForMatte = null;
        Bitmap maskBitmapForMatte = null;
        Bitmap matteBitmapForMatte = null;
        LotteLayerView maskedLayer = null;
        for (int i = 0; i < reversedLayers.size(); i++) {
            LotteLayer layer = reversedLayers.get(i);
            LotteLayerView layerView;
            if (maskedLayer == null) {
                layerView = new LotteLayerView(layer, composition, this, mainBitmap, maskBitmap, matteBitmap);
            } else {
                if (mainBitmapForMatte == null) {
                    mainBitmapForMatte = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ALPHA_8);
                    // TODO: only create matte mask and matte if necessary.
                    maskBitmapForMatte = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ALPHA_8);
                    matteBitmapForMatte = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ALPHA_8);
                }
                layerView = new LotteLayerView(layer, composition, this, mainBitmapForMatte, maskBitmapForMatte, matteBitmapForMatte);
            }
            layerMap.put(layerView.getId(), layerView);
            if (maskedLayer != null) {
                maskedLayer.setMatte(layerView);
                maskedLayer = null;
            } else {
                if (layer.getMatteType() == LotteLayer.MatteType.Add) {
                    maskedLayer = layerView;
                }
                rootAnimatableLayer.addLayer(layerView);
            }
        }
    }


    public void addAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
        rootAnimatableLayer.addAnimatorUpdateListener(updateListener);
    }

    public void removeUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
        rootAnimatableLayer.removeAnimatorUpdateListener(updateListener);
    }

    public void addAnimatorListener(Animator.AnimatorListener listener) {
        rootAnimatableLayer.addAnimatorListener(listener);
    }

    public void removeAnimatorListener(Animator.AnimatorListener listener) {
        rootAnimatableLayer.removeAnimatorListener(listener);
    }

    public void loop(boolean loop) {
        rootAnimatableLayer.loop(loop);
    }

    public void play() {
        rootAnimatableLayer.play();
    }

    public void pause() {
        rootAnimatableLayer.pause();
    }

    public void setProgress(@FloatRange(from=0f, to=1f) float progress) {
        rootAnimatableLayer.setProgress(progress);
    }
}
