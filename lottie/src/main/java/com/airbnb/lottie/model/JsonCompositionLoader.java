package com.airbnb.lottie.model;

import android.content.res.Resources;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.airbnb.lottie.model.CompositionLoader;

import org.json.JSONObject;

public final class JsonCompositionLoader extends CompositionLoader<JSONObject> {
  private final Resources res;
  private final OnCompositionLoadedListener loadedListener;

  public JsonCompositionLoader(Resources res, OnCompositionLoadedListener loadedListener) {
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
