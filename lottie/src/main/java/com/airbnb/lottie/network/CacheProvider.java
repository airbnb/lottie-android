package com.airbnb.lottie.network;

import java.io.File;

import androidx.annotation.NonNull;

public interface CacheProvider {
  @NonNull File getCacheDir();
}