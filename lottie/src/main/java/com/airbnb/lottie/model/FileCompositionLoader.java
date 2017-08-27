package com.airbnb.lottie.model;

import android.content.res.Resources;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;

import java.io.InputStream;

public final class FileCompositionLoader extends CompositionLoader<InputStream> {
  private final Resources res;
  private final OnCompositionLoadedListener loadedListener;

  public FileCompositionLoader(Resources res, OnCompositionLoadedListener loadedListener) {
    this.res = res;
    this.loadedListener = loadedListener;
  }

  @Override protected LottieComposition doInBackground(InputStream... params) {
    return LottieComposition.Factory.fromInputStream(res, params[0]);
  }

  @Override protected void onPostExecute(LottieComposition composition) {
    loadedListener.onCompositionLoaded(composition);
  }
}
