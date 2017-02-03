package com.airbnb.lottie.samples;

import android.support.v4.util.Pair;

interface ILottieApplication {
  void startRecordingDroppedFrames();

  /**
   * Returns the number of frames dropped since starting
   **/
  Pair<Integer, Long> stopRecordingDroppedFrames();
}
