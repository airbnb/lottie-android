package com.airbnb.lottie.network;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

/**
 * Implement this interface to handle network fetching manually when animations are requested via url. By default, Lottie will use an
 * {@link java.net.HttpURLConnection} under the hood but this enables you to hook into your own network stack. By default, Lottie will also handle caching the
 * animations but if you want to provide your own cache directory, you may implement {@link LottieNetworkCacheProvider}.
 *
 * @see com.airbnb.lottie.Lottie#initialize
 */
public interface LottieNetworkFetcher {
    @WorkerThread
    @NonNull
    LottieFetchResult fetchSync(@NonNull String url) throws IOException;
}
