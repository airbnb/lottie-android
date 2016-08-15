package com.airbnb.lotte;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.widget.ImageView;

import com.airbnb.lotte.layers.LotteAnimatableLayer;
import com.airbnb.lotte.layers.LotteLayer;
import com.airbnb.lotte.layers.LotteLayerView;
import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.utils.LayerDrawableCompat;

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

    private final LayerDrawableCompat background = new LayerDrawableCompat();
    private final LongSparseArray<LotteLayerView> layerMap = new LongSparseArray<>();

    private LotteAnimatableLayer animationContainer;
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
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (animationContainer != null) {
            animationContainer.setBounds(0, 0, w, h);
        }
    }

    public void setAnimation(String animationName) {
        InputStream file;
        try {
            file = getContext().getAssets().open(animationName);
            int size = file.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            file.read(buffer);
            file.close();
            String json = new String(buffer, "UTF-8");

            setJson(new JSONObject(json));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to find file.", e);
        } catch (JSONException e) {
            throw new IllegalStateException("Unable to load JSON.", e);
        }
    }

    public void setJson(JSONObject json) {
        setModel(LotteComposition.fromJson(json));
    }

    public void setModel(LotteComposition model) {
        sceneModel = model;
        animationSpeed = 1f;
        animationContainer = new LotteAnimatableLayer(0);
        animationContainer.setBounds(0, 0, getWidth(), getHeight());
        animationContainer.setSpeed(0f);
        setImageDrawable(animationContainer);
        buildSubviewsForModel();
    }

    public void play() {
        play(null);
    }

    public void play(@Nullable OnAnimationCompletedListener listener) {

    }

    public void pause() {

    }

    private void buildSubviewsForModel() {
        List<LotteLayer> reversedLayers = sceneModel.getLayers();
        Collections.reverse(reversedLayers);

        LotteLayerView maskedLayer = null;
        for (LotteLayer layer : reversedLayers) {
            LotteLayerView layerDrawable = new LotteLayerView(layer, sceneModel);
            layerMap.put(layerDrawable.getId(), layerDrawable);
            if (maskedLayer != null) {
                maskedLayer.setMask(layerDrawable);
            } else {
                if (layer.getMatteType() == LotteLayer.MatteType.Add) {
                    maskedLayer = layerDrawable;
                }
                background.addDrawable(layerDrawable);
            }
        }
    }
}
