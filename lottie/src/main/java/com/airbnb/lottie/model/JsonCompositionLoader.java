package com.airbnb.lottie.model;

import android.content.res.Resources;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;

import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

public final class JsonCompositionLoader extends CompositionLoader<JSONObject> {
  private final OnCompositionLoadedListener loadedListener;

  /**
   * Use {@link #JsonCompositionLoader(OnCompositionLoadedListener)}
   */
  @Deprecated
  public JsonCompositionLoader(Resources res, OnCompositionLoadedListener loadedListener) {
    this(loadedListener);
  }

  @SuppressWarnings("WeakerAccess") public JsonCompositionLoader(OnCompositionLoadedListener loadedListener) {
    this.loadedListener = loadedListener;
  }

  @Override protected LottieComposition doInBackground(JSONObject... params) {
    try {
      JsonReader reader = new JsonReader(new StringReader(params[0].toString()));
      return LottieComposition.Factory.fromJsonSync(reader);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override protected void onPostExecute(LottieComposition composition) {
    loadedListener.onCompositionLoaded(composition);
  }
}
