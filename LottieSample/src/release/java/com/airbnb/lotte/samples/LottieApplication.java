package com.airbnb.lottie.samples;

import android.app.Application;
import android.support.v4.util.Pair;

public class LottieApplication extends Application implements ILottieApplication {
  @Override
  public void startRecordingDroppedFrames() {
  }

  @Override
  public Pair<Integer, Long> stopRecordingDroppedFrames() {
    return new Pair<>(0, 0L);
  }
}
