package com.airbnb.lottie;


public interface LottieValueCallback<T> {

  T getValue(int startFrame, int endFrame, T startValue, T endValue, float linearKeyframeProgress,
      float interpolatedKeyframeProgress, float overallProgress);
}
