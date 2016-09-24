package com.airbnb.lotte;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.widget.ImageView;

import com.airbnb.lotte.layers.LotteAnimatableLayer;
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
    public interface OnAnimationCompletedListener {
        void onAnimationCompleted();
    }

    private final LongSparseArray<LotteLayerView> layerMap = new LongSparseArray<>();
    private final RootLotteAnimatableLayer animationContainer = new RootLotteAnimatableLayer(this);

    private LotteComposition sceneModel;
    private boolean isPlaying;
    private boolean loop;
    private float progress;
    private float animationSpeed;
    // TODO: not supported yet.
    private boolean autoReverseAnimation;

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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (animationContainer != null) {
            animationContainer.setBounds(0, 0, w, h);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (animationContainer != null && MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY && MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            setMeasuredDimension(animationContainer.getBounds().width(), animationContainer.getBounds().height());
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable drawable) {
        return true;
    }

    public void setAnimation(String animationName) {
        setImageDrawable(null);
        InputStream file;
        try {
            file = getContext().getAssets().open(animationName);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to find file " + animationName, e);
        }

        new AsyncTask<InputStream, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(InputStream... params) {
                try {
                    InputStream file = params[0];
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

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                setJson(jsonObject);
            }
        }.execute(file);
    }

    public void setJson(JSONObject json) {
        new AsyncTask<JSONObject, Void, LotteComposition>() {

            @Override
            protected LotteComposition doInBackground(JSONObject... params) {
                return LotteComposition.fromJson(params[0]);
            }

            @Override
            protected void onPostExecute(LotteComposition model) {
                setModel(model);
                setImageDrawable(animationContainer);
                buildSubviewsForModel();
            }
        }.execute(json);
    }

    public void setModel(LotteComposition model) {
        sceneModel = model;
        animationSpeed = 1f;
        animationContainer.setBounds(0, 0, getWidth(), getHeight());
        animationContainer.setSpeed(0f);
    }

    public void play() {
        play(null);
    }

    public void play(@Nullable OnAnimationCompletedListener listener) {
        animationContainer.play();
    }

    public void setProgress(@FloatRange(from=0f, to=1f) float progress) {
        animationContainer.setProgress(progress);
    }

    public void pause() {

    }

    private void buildSubviewsForModel() {
        List<LotteLayer> reversedLayers = sceneModel.getLayers();
        Collections.reverse(reversedLayers);

        boolean needsMatte = false;
        boolean needsMask = false;
        for (LotteLayer layer : reversedLayers) {
            if (layer.getMatteType() != null && layer.getMatteType() != LotteLayer.MatteType.None) {
                needsMatte = true;
            }
            if (!layer.getMasks().isEmpty()) {
                needsMask = true;
            }
            if (needsMatte && needsMask) {
                break;
            }
        }

        Bitmap mainBitmap = Bitmap.createBitmap(sceneModel.getBounds().width(), sceneModel.getBounds().height(), Bitmap.Config.ARGB_8888);
        Bitmap maskBitmap = needsMask ? Bitmap.createBitmap(sceneModel.getBounds().width(), sceneModel.getBounds().height(), Bitmap.Config.ALPHA_8) : null;
        Bitmap matteBitmap = needsMatte ? Bitmap.createBitmap(sceneModel.getBounds().width(), sceneModel.getBounds().height(), Bitmap.Config.ARGB_8888) : null;

        Bitmap mainBitmapForMatte = null;
        Bitmap maskBitmapForMatte = null;
        Bitmap matteBitmapForMatte = null;
        LotteLayerView maskedLayer = null;
        for (LotteLayer layer : reversedLayers) {
            LotteLayerView layerDrawable;
            if (maskedLayer == null) {
                layerDrawable = new LotteLayerView(layer, sceneModel, this, mainBitmap, maskBitmap, matteBitmap);
            } else {
                if (mainBitmapForMatte == null) {
                    mainBitmapForMatte = Bitmap.createBitmap(sceneModel.getBounds().width(), sceneModel.getBounds().height(), Bitmap.Config.ALPHA_8);
                    maskBitmapForMatte = Bitmap.createBitmap(sceneModel.getBounds().width(), sceneModel.getBounds().height(), Bitmap.Config.ALPHA_8);
                    matteBitmapForMatte = Bitmap.createBitmap(sceneModel.getBounds().width(), sceneModel.getBounds().height(), Bitmap.Config.ALPHA_8);
                }
                layerDrawable = new LotteLayerView(layer, sceneModel, this, mainBitmapForMatte, maskBitmapForMatte, matteBitmapForMatte);
            }
            layerMap.put(layerDrawable.getId(), layerDrawable);
            if (maskedLayer != null) {
                maskedLayer.setMatte(layerDrawable);
                maskedLayer = null;
            } else {
                if (layer.getMatteType() == LotteLayer.MatteType.Add) {
                    maskedLayer = layerDrawable;
                }
                ((LotteAnimatableLayer) getDrawable()).addLayer(layerDrawable);
            }
        }
    }
}
