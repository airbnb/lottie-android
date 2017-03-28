package com.airbnb.lottie;

import android.content.res.Resources;

import java.io.InputStream;

final class ScaledFileCompositionLoader extends CompositionLoader<InputStream> {
  private final Resources res;
  private final OnCompositionLoadedListener loadedListener;
  private float scale;

  ScaledFileCompositionLoader(float scale, Resources res,
      OnCompositionLoadedListener loadedListener) {
    this.scale = scale;
    this.res = res;
    this.loadedListener = loadedListener;
  }

  @Override protected LottieComposition doInBackground(InputStream... params) {
    return LottieComposition.Factory.fromInputStreamScaled(res, params[0],scale);
  }

  @Override protected void onPostExecute(LottieComposition composition) {
    loadedListener.onCompositionLoaded(composition);
  }
}
