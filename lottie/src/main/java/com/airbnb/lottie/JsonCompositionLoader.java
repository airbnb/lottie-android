package com.airbnb.lottie;

import android.content.res.Resources;

import org.json.JSONObject;

final class JsonCompositionLoader extends CompositionLoader<JSONObject> {
  private final Resources res;
  private final OnCompositionLoadedListener loadedListener;

  JsonCompositionLoader(Resources res, OnCompositionLoadedListener loadedListener) {
    this.res = res;
    this.loadedListener = loadedListener;
  }

  @Override protected LottieComposition doInBackground(JSONObject... params) {
    return LottieComposition.Factory.fromJsonSync(res, params[0]);
  }

  @Override protected void onPostExecute(LottieComposition composition) {
    loadedListener.onCompositionLoaded(composition);
  }
}
