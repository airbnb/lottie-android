package com.airbnb.lottie.model.content;


import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.model.layer.BaseLayer;

public interface ContentModel {
  @Nullable Content toContent(LottieDrawable drawable, BaseLayer layer);
}
