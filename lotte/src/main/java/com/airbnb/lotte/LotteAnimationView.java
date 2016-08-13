package com.airbnb.lotte;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.widget.FrameLayout;

import com.airbnb.lotte.layers.LotteLayer;
import com.airbnb.lotte.layers.LotteLayerDrawable;
import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.utils.LayerDrawableCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class LotteAnimationView extends FrameLayout {
    public interface OnAnimationCompletedListener {
        void onAnimationCompleted();
    }

    private final LayerDrawableCompat background = new LayerDrawableCompat();
    private final LongSparseArray<LotteLayerDrawable> layerMap = new LongSparseArray<>();

    private boolean isPlaying;
    private boolean loop;
    private float progress;
    private float speed;
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

    public void setAnimationName(String animationName) {
        InputStream file = null;
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
        setModel(new LotteComposition(json));
    }

    public void setModel(LotteComposition model) {

    }

    public void play() {
        play(null);
    }

    public void play(@Nullable OnAnimationCompletedListener listener) {

    }

    public void pause() {

    }

    private void buildViewsForModel(LotteComposition composition) {
        List<LotteLayer> reversedLayers = composition.getLayers();
        Collections.reverse(reversedLayers);

        LotteLayerDrawable maskedLayer = null;
        for (LotteLayer layer : reversedLayers) {
            LotteLayerDrawable layerDrawable = new LotteLayerDrawable(layer, composition);
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
