package com.airbnb.lottie;

import android.graphics.Path;
import android.support.annotation.FloatRange;

/**
 * Created by Administrator on 2017/2/23 0023.
 */

public interface IPathKeyframe {


 public void setProgress(@FloatRange(from = 0f, to = 1f) float progress);

 Path getPath();
}
