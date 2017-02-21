package com.airbnb.lottie;

import android.content.res.Resources;

import java.io.InputStream;

final class FileCompositionLoader extends CompositionLoader<InputStream> {
  private final Resources res;
  private final OnCompositionLoadedListener loadedListener;

  FileCompositionLoader(Resources res, OnCompositionLoadedListener loadedListener) {
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
