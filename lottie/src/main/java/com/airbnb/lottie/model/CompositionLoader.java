package com.airbnb.lottie.model;

import android.os.AsyncTask;

import com.airbnb.lottie.Cancellable;
import com.airbnb.lottie.LottieComposition;

public abstract class CompositionLoader<Params> extends AsyncTask<Params, Void, LottieComposition>
    implements Cancellable {
  @Override public void cancel() {
    cancel(true);
  }
}
