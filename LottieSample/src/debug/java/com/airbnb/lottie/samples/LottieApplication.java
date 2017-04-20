package com.airbnb.lottie.samples;

import android.app.Application;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatDelegate;
import android.view.Gravity;

import com.airbnb.lottie.L;
import com.codemonkeylabs.fpslibrary.FrameDataCallback;
import com.codemonkeylabs.fpslibrary.TinyDancer;

public class LottieApplication extends Application implements ILottieApplication {
  private int droppedFrames;
  private long droppedFramesStartingNs;
  private long currentFrameNs;
  @Override public void onCreate() {
    super.onCreate();
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    if (L.DBG) {
      TinyDancer.create()
          .startingGravity(Gravity.TOP | Gravity.END)
          .startingXPosition(0)
          .startingYPosition(0)
          .addFrameDataCallback(new FrameDataCallback() {
            @Override
            public void doFrame(long previousFrameNs, long currentFrameNs, int droppedFrames) {
              LottieApplication.this.droppedFrames += droppedFrames;
              LottieApplication.this.currentFrameNs = currentFrameNs;
            }
          })
          .show(this);
    }
  }

  @Override public void startRecordingDroppedFrames() {
    droppedFrames = 0;
    droppedFramesStartingNs = currentFrameNs;
  }

  @Override public Pair<Integer, Long> stopRecordingDroppedFrames() {
    long duration = currentFrameNs - droppedFramesStartingNs;
    Pair<Integer, Long> ret = new Pair<>(droppedFrames, duration);
    droppedFrames = 0;
    return ret;
  }
}
