package com.airbnb.lottie.network;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

/**
 * Interface for custom fetcher
 */
public interface LottieNetworkFetcher {
    @WorkerThread
    @NonNull
    LottieNetworkResult fetchSync(@NonNull String url) throws IOException;
}
