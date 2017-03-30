package com.airbnb.lottie;

import android.content.res.Resources;

import org.json.JSONObject;

final class ScaledJsonCompositionLoader extends CompositionLoader<JSONObject> {
  private float scale;
  private final Resources res;
  private final OnCompositionLoadedListener loadedListener;

  ScaledJsonCompositionLoader(float scale,Resources res, OnCompositionLoadedListener
      loadedListener) {
    this.scale = scale;
    this.res = res;
    this.loadedListener = loadedListener;
  }

  @Override protected LottieComposition doInBackground(JSONObject... params) {
    return LottieComposition.Factory.fromJsonSyncScaled(res,scale, params[0]);
  }

  @Override protected void onPostExecute(LottieComposition composition) {
    loadedListener.onCompositionLoaded(composition);
  }
}
