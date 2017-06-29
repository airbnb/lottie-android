package com.airbnb.lottie;


import android.support.annotation.Nullable;

interface ContentModel {
  @Nullable Content toContent(LottieDrawable drawable, BaseLayer layer);
}
