package com.airbnb.lottie.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.LongSparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LottieComposition {

    public interface OnCompositionLoadedListener {
        void onCompositionLoaded(LottieComposition composition);
    }

    public interface Cancellable {
        void cancel();
    }

    /**
     * The largest bitmap drawing cache can be is 8,294,400 bytes. There are 4 bytes per pixel leaving ~2.3M pixels available.
     * Reduce the number a little bit for safety.
     *
     * Hopefully this can be hardware accelerated someday.
     */
    private static final int MAX_PIXELS = 1000;

    public static Cancellable fromFile(Context context, String fileName, OnCompositionLoadedListener loadedListener) {
        InputStream file;
        try {
            file = context.getAssets().open(fileName);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to find file " + fileName, e);
        }
        FileCompositionLoader loader = new FileCompositionLoader(context.getResources(), loadedListener);
        loader.execute(file);
        return loader;
    }

    public static LottieComposition fromFileSync(Context context, String fileName) {
        InputStream file;
        try {
            file = context.getAssets().open(fileName);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to find file " + fileName, e);
        }
        return fromInputStream(context.getResources(), file);
    }

    public static Cancellable fromJson(Resources res, JSONObject json, OnCompositionLoadedListener loadedListener) {
        JsonCompositionLoader loader = new JsonCompositionLoader(res, loadedListener);
        loader.execute(json);
        return loader;
    }

    private static LottieComposition fromInputStream(Resources res, InputStream file) {
        try {
            int size = file.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            file.read(buffer);
            file.close();
            String json = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(json);
            return LottieComposition.fromJsonSync(res,jsonObject);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to find file.", e);
        } catch (JSONException e) {
            throw new IllegalStateException("Unable to load JSON.", e);
        }
    }

    private static LottieComposition fromJsonSync(Resources res, JSONObject json) {
        LottieComposition composition = new LottieComposition(res);

        int width = -1;
        int height = -1;
        try {
            width = json.getInt("w");
            height = json.getInt("h");
        } catch (JSONException e) {
            // ignore.
        }
        if (width != -1 && height != -1) {
            int scaledWidth = (int) (width * composition.scale);
            int scaledHeight = (int) (height * composition.scale);
            if (scaledWidth * scaledHeight > MAX_PIXELS) {
                float factor = (float) MAX_PIXELS / (float) Math.max(scaledWidth, scaledHeight);
                scaledWidth *= factor;
                scaledHeight *= factor;
                composition.scale *= factor;
            }
            composition.bounds = new Rect(0, 0, scaledWidth, scaledHeight);
        }

        try {
            composition.startFrame = json.getLong("ip");
            composition.endFrame = json.getLong("op");
            composition.frameRate = json.getInt("fr");
        } catch (JSONException e) {
            //
        }

        if (composition.endFrame != 0 && composition.frameRate != 0) {
            long frameDuration = composition.endFrame - composition.startFrame;
            composition.duration = (long) (frameDuration / (float) composition.frameRate * 1000);
        }

        try {
            JSONArray jsonLayers = json.getJSONArray("layers");
            for (int i = 0; i < jsonLayers.length(); i++) {
                Layer layer = Layer.fromJson(jsonLayers.getJSONObject(i), composition);
                composition.layers.add(layer);
                composition.layerMap.put(layer.getId(), layer);
                if (!layer.getMasks().isEmpty()) {
                    composition.hasMasks = true;
                }
                if (layer.getMatteType() != null && layer.getMatteType() != Layer.MatteType.None) {
                    composition.hasMattes = true;
                }
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Unable to find layers.", e);
        }


        return composition;
    }

    private final LongSparseArray<Layer> layerMap = new LongSparseArray<>();
    private final List<Layer> layers = new ArrayList<>();
    private Rect bounds;
    private long startFrame;
    private long endFrame;
    private int frameRate;
    private long duration;
    private boolean hasMasks;
    private boolean hasMattes;
    private float scale;

    private LottieComposition(Resources res) {
        scale = res.getDisplayMetrics().density;
    }


    public Layer layerModelForId(long id) {
        return layerMap.get(id);
    }

    public Rect getBounds() {
        return bounds;
    }

    public long getDuration() {
        return duration;
    }

    long getEndFrame() {
        return endFrame;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    long getStartFrame() {
        return startFrame;
    }

    public boolean hasMasks() {
        return hasMasks;
    }

    public boolean hasMattes() {
        return hasMattes;
    }

    public float getScale() {
        return scale;
    }

    private static final class FileCompositionLoader extends CompositionLoader<InputStream> {

        private final Resources res;
        private final OnCompositionLoadedListener loadedListener;

        FileCompositionLoader(Resources res, OnCompositionLoadedListener loadedListener) {
            this.res = res;
            this.loadedListener = loadedListener;
        }

        @Override
        protected LottieComposition doInBackground(InputStream... params) {
            return fromInputStream(res, params[0]);
        }

        @Override
        protected void onPostExecute(LottieComposition composition) {
            loadedListener.onCompositionLoaded(composition);
        }
    }

    private static final class JsonCompositionLoader extends CompositionLoader<JSONObject> {

        private final Resources res;
        private final OnCompositionLoadedListener loadedListener;

        JsonCompositionLoader(Resources res, OnCompositionLoadedListener loadedListener) {
            this.res = res;
            this.loadedListener = loadedListener;
        }

        @Override
        protected LottieComposition doInBackground(JSONObject... params) {
            return fromJsonSync(res, params[0]);
        }

        @Override
        protected void onPostExecute(LottieComposition composition) {
            loadedListener.onCompositionLoaded(composition);
        }
    }

    private abstract static class CompositionLoader<Params>
            extends AsyncTask<Params, Void, LottieComposition>
            implements Cancellable {

        @Override
        public void cancel() {
            cancel(true);
        }
    }
}
