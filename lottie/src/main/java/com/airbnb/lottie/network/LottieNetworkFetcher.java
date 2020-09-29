package com.airbnb.lottie.network;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

/**
 * Implement this interface to handle network fetching manually when animations are requested via url. By default, Lottie will use an
 * HttpUrlConnection under the hood but this enables you to hook into your own network stack. By default, Lottie will also handle caching the
 * animations but if you want to provide your own cache directory, you may implement `LottieNetworkCacheProvider`.
 *
 * @see com.airbnb.lottie.Lottie#initialize
 */
public interface LottieNetworkFetcher {
    @WorkerThread
    @NonNull
    LottieNetworkResult fetchSync(@NonNull String url) throws IOException;

    /**
     * The method will be called after the animation has loaded.
     * You can close your connection here.
     */
    void disconnect(@NonNull String url);
}
