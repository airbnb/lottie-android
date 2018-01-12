package com.airbnb.lottie.model;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;

import java.io.IOException;

public final class JsonCompositionLoader extends CompositionLoader<JsonReader> {
  private final OnCompositionLoadedListener loadedListener;

  @SuppressWarnings("WeakerAccess") public JsonCompositionLoader(OnCompositionLoadedListener loadedListener) {
    this.loadedListener = loadedListener;
  }

  @Override protected LottieComposition doInBackground(JsonReader... params) {
    try {
      return LottieComposition.Factory.fromJsonSync(params[0]);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override protected void onPostExecute(LottieComposition composition) {
    loadedListener.onCompositionLoaded(composition);
  }
}
