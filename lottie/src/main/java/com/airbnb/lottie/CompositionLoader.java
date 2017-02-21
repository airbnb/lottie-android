package com.airbnb.lottie;

import android.os.AsyncTask;

abstract class CompositionLoader<Params> extends AsyncTask<Params, Void, LottieComposition>
    implements Cancellable {
  @Override public void cancel() {
    cancel(true);
  }
}
